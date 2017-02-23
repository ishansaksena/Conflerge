package conflerge.differ.ast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.Visitable;

/**
 * Uses the mmdiff algorithm to diff ASTs!
 */
public class ASTDiffer {
    
    public static void main(String[] args) {     
        Node base  = JavaParser.parse("class Foo { Foo foo = new Foo();  }");
        Node local = JavaParser.parse("class Foo { Foo foo =  new Foo(a); }");          
        NodeListWrapper.wrapAST(base);
        NodeListWrapper.wrapAST(local);        
        DiffResult localDiff  = new ASTDiffer(base, local).diff();
        base.accept(new ASTMergeVisitor(), localDiff);
        base.accept(new CleanUpVisitor(), null); 
        System.out.println("\n" + base.toString());
    }
    
    public static int editCost = 1;
    
    private Map<Node, Node> deletes = new IdentityHashMap<>();
    private Map<Node, Node> replaces = new IdentityHashMap<>();
    
    private Map<Node, Node> alignsA = new IdentityHashMap<>();
    private Map<Node, Node> alignsB = new IdentityHashMap<>();
    
    private Map<Node, List<Node>> insertPre  = new IdentityHashMap<>();
    private Map<Node, List<Node>> insertPost = new IdentityHashMap<>();
    private Map<NodeListWrapperNode, List<Node>> insertedUnder  = new IdentityHashMap<>();
    
    public final Map<Node, Node> parentsA;
    public final Map<Node, Node> parentsB;
       
    private int[][] opt;
    private final int m;
    private final int n;
    
    private final Node[] aN;
    private final Node[] bN;
    
    private final int[] aD;
    private final int[] bD;

    public ASTDiffer(Node a, Node b) {        
        this.aN = getOrderedNodes(a);
        this.bN = getOrderedNodes(b);
       
        this.aD = getDepths(a);
        this.bD = getDepths(b);
        
        this.parentsA = getParentMap(a);
        this.parentsB = getParentMap(b);
        
        m = aN.length;
        n = bN.length;

        this.opt = new int[m][n];
    }

    public DiffResult diff() {
        computeEdits();
        recoverEdits();

        return new DiffResult(pruneMap(deletes),
                              pruneMap(replaces),
                              splitMap(insertPre), 
                              splitMap(insertPost), 
                              getInsertedUnder(insertedUnder));
    }

    //-MMDiff-Algorithm---------------------------------------------

    private void computeEdits() {
       int max = m + n + 1;
       for (int i = 1; i < m; i++) {
           opt[i][0] = opt[i-1][0] + editCost;
       }
       for (int j = 1; j < n; j++) {
           opt[0][j] = opt[0][j-1] + editCost;
       }
       for (int i = 1; i < m; i++) {
           for (int j = 1; j < n; j++) {
               int align  = max;
               int delete = max; 
               int insert = max;           
               if (aD[i] == bD[j]) {
                   align = opt[i-1][j-1] + updateCost(aN[i], bN[j]);
               }
               if (j == n - 1 || bD[j+1] <= aD[i]) {
                   delete = opt[i-1][j] + editCost;
               }
               if (i == m - 1 || aD[i+1] <= bD[j]) {
                   insert = opt[i][j-1] + editCost;
               }
               opt[i][j] = min(align, delete, insert);
           }
       }
    }    
  
    //-Path-Recovery---------------------------------------------
    
    private void recoverEdits() {
       int i = m - 1;
       int j = n - 1;    
       while (i > 0 && j > 0) {
           if (opt[i][j] == opt[i-1][j] + editCost && (j == n-1 || bD[j+1] <= aD[i])) {
               addDelete(i, j);
               i--;
           } else if (opt[i][j] == opt[i][j-1] + editCost && (i == m-1 || aD[i+1] <= bD[j])) {             
               addInsert(i, j);
               j--;
           } else {
               addAlign(i, j);
               i--;
               j--;
           } 
       }
       while (i > 0) { addDelete(i, j); i--; }
       while (j > 0) { addInsert(i, j); j--; }
    }
     
    private void addAlign(int i, int j) {
        if (updateCost(aN[i], bN[j]) == 0) {
            alignsA.put(aN[i], bN[j]);
            alignsB.put(bN[j], aN[i]);
        } else {
            replaces.put(aN[i], bN[j]);
        }
    }

    private void addDelete(int i, int j) {
        deletes.put(aN[i], aN[i]);
    }
    
    private void addInsert(int i, int j) {
        Node parent = parentsB.get(bN[j]);
        List<? extends Node> children;
        if (parent instanceof NodeListWrapperNode) {
            NodeList<? extends Node> nl = ((NodeListWrapperNode)parent).list.nodeList;
            children = new ArrayList<>(nl);
            if (!insertedUnder.containsKey(parent)) {
                insertedUnder.put((NodeListWrapperNode) parent, new ArrayList<Node>());
            }
            insertedUnder.get(parent).add(bN[j]);
        } else {
            children = new ArrayList<>(parent.getChildNodes());
        }
        
        int idx = 0; 
        for (Node n : children) {
            if (n == bN[j]) break;
            idx++;
        }
        
        if (idx > 0) {
            Node pre = children.get(idx - 1);
            if (!insertPre.containsKey(pre)) {
                insertPre.put(pre, new ArrayList<Node>());
            }
            insertPre.get(pre).add(bN[j]);
        }
        
        if (idx < children.size() - 1) {
            Node post = children.get(idx + 1);
            if (!insertPost.containsKey(post)) {
                insertPost.put(post, new ArrayList<Node>());
            }
            insertPost.get(post).add(bN[j]);
        }
    }   
    
    //-Output-Processing---------------------------------------------

    private Map<Node, Node> pruneMap(Map<Node, Node> m) {
       m.keySet().removeIf(node -> 
           (!node.getParentNode().isPresent() || 
                   !unmodified(node.getParentNode().get())));    
       return m;
   }

    private Map<Visitable, List<Node>> getInsertedUnder(Map<NodeListWrapperNode, List<Node>> parents) {
       Map<Visitable, List<Node>> res = new IdentityHashMap<>();
       for (NodeListWrapperNode context : parents.keySet()) {
           if (parents.get(context).isEmpty()) continue;
           if (alignsB.containsKey(context)) {
               res.put(((NodeListWrapperNode) alignsB.get(context)).list.nodeList, parents.get(context));
               Collections.reverse(parents.get(context));
           }   
       }
       return res;
   }

   private Map<Node, Node> splitMap(Map<Node, List<Node>> m) {
       for (Visitable node : m.keySet()) {
           m.get(node).removeIf(n -> !unmodified(parentsB.get(n)));   
           Collections.reverse(m.get(node));
       }
       Map<Node, Node> res  = new IdentityHashMap<>();
       for (Node key : m.keySet()) {
           if (m.get(key).isEmpty()) continue;
           if (alignsB.containsKey(key)) {
               res.put(alignsB.get(key), m.get(key).get(0));
           } else {
               res.put(key, m.get(key).get(0));
           }      
       }
       return res;
   }
        
    //-Input-Processing----------------------------------------------
   
    private Map<Node, Node> getParentMap(Node a) {
        Map<Node, Node> parents = new IdentityHashMap<>();
        parents.put(a, null);
        return getParentMap(a, parents);
    }
    
    private Map<Node, Node> getParentMap(Node root, Map<Node, Node> parents) {
        List<Node> children = new ArrayList<>(root.getChildNodes());   
        List<NodeList<?>> nodeLists = getNodeLists(root);
        for (NodeList<?> nl : nodeLists) {
            if (nl instanceof NodeListWrapper) {
                NodeListWrapper nlw = (NodeListWrapper) nl;
                parents.put(nlw.node, root);
                for (Node n : nlw.nodeList) {
                    parents.put(n, nlw.node);
                    getParentMap(n, parents);       
                    children.remove(n);
                }
            }
        }
        for (Node n : children) {
            parents.put(n, root);
            getParentMap(n, parents);
        }
        return parents;
    }
    
    private static int[] getDepths(Node root) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(null); 
        getDepths(root, list, 0);
        int[] res = new int[list.size()];
        for (int i = 1; i < list.size(); i++) {
            res[i] = list.get(i);
        }
        return res;
    }
    
    private static void getDepths(Node root, List<Integer> list, int d) {
        list.add(d);
        List<Node> children = new ArrayList<>(root.getChildNodes());   
        List<NodeList<?>> nodeLists = getNodeLists(root); 
        for (NodeList<?> nl : nodeLists) {
            if (nl instanceof NodeListWrapper) {
                list.add(d + 1);
                for (Node n : ((NodeListWrapper)nl).nodeList) {
                    getDepths(n, list, d + 2);
                    children.remove(n);
                }
            }
        }
        for (Node n : children) {
            getDepths(n, list, d + 1);
        }
    }
    
    private Node[] getOrderedNodes(Node root) {
        List<Node> res = new ArrayList<Node>();
        res.add(null);
        getOrderedNodes(root, res);
        return res.toArray(new Node[res.size()]);
    }
      
    private  void getOrderedNodes(Node root, List<Node> list) {
        list.add(root);
        List<Node> children = new ArrayList<>(root.getChildNodes());   
        List<NodeList<?>> nodeLists = getNodeLists(root); 
        for (NodeList<?> nl : nodeLists) {
            if (nl instanceof NodeListWrapper) {
                NodeListWrapper nlw = (NodeListWrapper) nl;
                NodeListWrapperNode nlwn = new NodeListWrapperNode(nlw);
                nlw.node = nlwn;
                list.add(nlwn);
                for (Node n : nlw.nodeList) {
                    getOrderedNodes(n, list);
                    children.remove(n);
                }
            }
        }
        for (Node n : children) {
            getOrderedNodes(n, list);
        }
    }
    
    private static List<NodeList<?>> getNodeLists(Node root) {
        // This method SHOULDN't have to exist, but there's a strange behavior
        // that looks a lot like a javaparser bug that causes one type of node
        // to NOT return all its NodeLists. If more of this behavior is discovered, 
        // adopt a new pattern.
        List<NodeList<?>> nodeLists = new ArrayList<>(root.getNodeLists());
        
        // I do not understand. It even returns this field in the github source code.
        if (root instanceof ObjectCreationExpr) {
            nodeLists.add(((ObjectCreationExpr)root).getArguments());
        }
        return nodeLists;
    }

    //-Utility-Methods---------------------------------------------
    
    private static int min(int... args) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < args.length; i++)
            min = args[i] < min ? args[i] : min;
          
        return min;
    }
      
    private int updateCost(Node n1, Node n2) {
        return ShallowEqualsVisitor.equals(n1, n2) ? 0 : editCost;
    }
    
    private boolean unmodified(Visitable n) {
        return alignsA.containsKey(n) || alignsB.containsKey(n);
    }
    
}