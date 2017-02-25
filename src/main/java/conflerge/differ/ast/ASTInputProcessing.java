package conflerge.differ.ast;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;

/**
 * The diff algorithm requires several preprocessing steps on the ASTs.
 * They've been delegated to this file to keep ASTDiffer.java a bit more
 * managable.
 */
public class ASTInputProcessing {

    public static Map<Node, Node> getParentMap(Node a) {
        Map<Node, Node> parents = new IdentityHashMap<>();
        parents.put(a, null);
        return getParentMap(a, parents);
    }
    
    private static Map<Node, Node> getParentMap(Node root, Map<Node, Node> parents) {
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
    
    public static int[] getDepths(Node root) {
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
    
    public static Node[] getOrderedNodes(Node root) {
        List<Node> res = new ArrayList<Node>();
        res.add(null);
        getOrderedNodes(root, res);
        return res.toArray(new Node[res.size()]);
    }
      
    private static void getOrderedNodes(Node root, List<Node> list) {
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
}
