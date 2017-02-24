package conflerge.differ.ast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.Pair;

/**
 * Uses the mmdiff algorithm to diff ASTs!
 */
public class ASTDiffer {
    
    public static void main(String[] args) {     
        Node base  = JavaParser.parse("class Foo { int x = 3 + 1 + 2; }");
        Node local = JavaParser.parse("class Foo { int x = 1 + 2 + 3; }");   

        base.accept(new NodeListWrapperVisitor(), "A"); 
        local.accept(new NodeListWrapperVisitor(), "B");
        
        DiffResult res1 = new ASTDiffer(base, local).diff();
        DiffResult res2 = new ASTDiffer(base, base).diff();
        
        base.accept(new MergeVisitor(), new Pair<DiffResult, DiffResult>(res1, res2));   
        base.accept(new NodeListUnwrapperVisitor(), null);  
        
        System.out.println("\n" + base.toString());
    }
    
    public static int editCost = 1;
    
    private Map<Node, Node> deletes = new IdentityHashMap<>();
    private Map<Node, Node> replaces = new IdentityHashMap<>();
    
    private Map<Node, Node> alignsA = new IdentityHashMap<>();
    private Map<Node, Node> alignsB = new IdentityHashMap<>();
    
    private Map<NodeListWrapperNode, List<Node>> insertedUnder  = new IdentityHashMap<>();
    private Map<Node, Node> shittyInserts = new IdentityHashMap<>();
    
    public final Map<Node, Node> parentsA;
    public final Map<Node, Node> parentsB;
    
    public static boolean conflict = false;
       
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
        
        // IMPORTANT: Inserts are highly problematic....
        
        for (Node n : pruneMap(shittyInserts).keySet()) {
            replaces.put(alignsB.get(parentsB.get(n)), parentsB.get(n));
        }

        return new DiffResult(pruneMap(deletes), pruneMap(replaces), processInserts(insertedUnder));
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
        //TODO: investigate why this is necessary
        if (updateCost(aN[i], bN[j]) == 0 || ((aN[i] instanceof NodeListWrapperNode) && (bN[j] instanceof NodeListWrapperNode)))  {
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
        if (parentsB.get(bN[j]) instanceof NodeListWrapperNode) {
            if (!insertedUnder.containsKey(parent)) {
              insertedUnder.put((NodeListWrapperNode) parent, new ArrayList<Node>());
            }
            insertedUnder.get(parent).add(bN[j]);
        } else {
            shittyInserts.put(bN[j], bN[j]);
        }
    }   
    
    //-Output-Processing---------------------------------------------

    private Map<Node, Node> pruneMap(Map<Node, Node> m) {
//       m.keySet().removeIf(node -> 
//           (!node.getParentNode().isPresent() || 
//                   !unmodified(node.getParentNode().get())));    
       return m;
   }
    
    private Map<NodeListWrapper, Map<Integer, List<Node>>> processInserts(Map<NodeListWrapperNode, List<Node>> insertedUnder) {
        Map<NodeListWrapper, Map<Integer, List<Node>>> nlToIndexToInserts = new IdentityHashMap<>();        
        for (NodeListWrapperNode nlwn : insertedUnder.keySet()) {
            NodeList<? extends Node> nl = nlwn.list.nodeList;
            Map<Integer, List<Node>> inserts = new HashMap<>();
            Collections.reverse(insertedUnder.get(nlwn));
            for (Node insert : insertedUnder.get(nlwn)) {               
                int i = indexOfObj(nl, insert);
                while (i >= 0 && !alignsB.containsKey(nl.get(i))) {
                    i--;
                }
                int insertIndex;
                if (i >= 0) {
                    Node match = alignsB.get(nl.get(i));
                    NodeListWrapperNode matchedParent = (NodeListWrapperNode) parentsA.get(match);
                    insertIndex = indexOfObj(matchedParent.list.nodeList, match) + 1;
                } else {
                    insertIndex = 0;
                }
                
                if (!inserts.containsKey(insertIndex)) {
                    inserts.put(insertIndex, new ArrayList<Node>());
                }
                inserts.get(insertIndex).add(insert);
            }
            
            //TODO: do this first, so you don't have to do tha computation if it's null.
            NodeListWrapperNode match = (NodeListWrapperNode) alignsB.get(nlwn);
            if (match != null) {
                nlToIndexToInserts.put(match.list, inserts);
            }
        }
        return nlToIndexToInserts;
    }
        
    //-Input-Processing----------------------------------------------
   
    private Map<Node, Node> getParentMap(Node a) {
        Map<Node, Node> parents = new IdentityHashMap<>();
        parents.put(a, null);
        return getParentMap(a, parents);
    }
    
    private Map<Node, Node> getParentMap(Node root, Map<Node, Node> parents) {
        List<Node> children = new ArrayList<>(root.getChildNodes());   
        List<NodeList<?>> nodeLists = root.getNodeLists();
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
        List<NodeList<?>> nodeLists = root.getNodeLists();
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
        List<NodeList<?>> nodeLists = root.getNodeLists(); 
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
  
    //-Utility-Methods---------------------------------------------
    
    private static int indexOfObj(Iterable<?> items, Object item) {
        int i = 0;
        for (Object o : items) {
            if (o == item) {
                return i;
            }
            i++;
        }
        return -1;
    }
    
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

    public static void handleConflict() {
        System.err.println("CONFLICT!");
        conflict = true;
    }
    
}