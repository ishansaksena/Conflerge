package zhangshasha;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * This file is no longer part of the project. It's still neat, though, 
 * and should possibly be cleaned up and moved to its own repository at
 * some point. To my knowledge, there aren't any other publicly available
 * implementations of the Zhang-Shasha algorithm that include edit script
 * recovery.
 * 
 * Performs the Zhang-Shasha algorithm for tree edit distance between two
 * trees, A and B. Recovers the corresponding edit script.
 */
public class TreeDiffer {
    
    /**
     * The costs of the edit operations.
     */
    public static final int DELETE_COST  = 1;
    public static final int INSERT_COST  = 1;
    public static final int REPLACE_COST = 1;
    
    /**
     * Used for to index into a NUM_OPS size array of costs by operation.
     */
    private static final int DELETE_IDX  = 0;
    private static final int INSERT_IDX  = 1;
    private static final int ALIGN_IDX   = 2;
    private static final int NUM_OPS     = 3;
    
    /**
     * The nodes in A and B, in post-order.
     */
    private final Node[] aNodes;
    private final Node[] bNodes;
    
    /**
     * L values for A and B.
     * 
     *  aLs[i] = post-order index of the leftmost leaf descendent
     *           of the node with post-order index 'i', in tree A.
     * 
     */
    private final int[] aLs;
    private final int[] bLs;
    
    /**
     * Keyroots for A and B: post-order indexes of nodes that have a left sibling.
     */
    private final int[] aKR;
    private final int[] bKR;
    
    /**
     * treeDist[i][j] = Edit distance between the subtree in A rooted at
     * Node i (where i is a post-order index in A) and the subtree in B
     * rooted at Node j (where j is a post-order index in B)
     */
    private int[][] treeDist;   
    
    /**
     * Given two keyroots, get the forest distance matrix
     * associated with those keyroots. 
     * 
     * Storing the forest distance matricies is an optimization 
     * for speed but a major memory overhead: If this is applied
     * to large trees, it may be neccessary to discard the forest
     * distance matricies and recompute them in the edit-recovery step. 
     */
    private int[][][][] krToForestDist;
    
    /**
     * Given indexes i, j in treeDist, gives the keyroots for the 
     * forest distance calculation, as a Point, that set that index
     * in treeDist. 
     */
    private Point[][] treeDistIdxToForestKr;
    
    /**
     * Given indexes i, j in treeDist, gives the indexes in the 
     * forest distance calculation, as a Point, that set that index
     * in treeDist. 
     */
    private Point[][] treeDistToForestIndexes;
    
    /**
     * Construct a new TreeDiffer for A and B.
     * 
     * @param A
     * @param B
     */
    public TreeDiffer(Node A, Node B) {
        aNodes  = getOrderedNodes(A);
        bNodes  = getOrderedNodes(B);
        
        aLs  = getLValues(aNodes);
        bLs  = getLValues(bNodes);
        
        aKR = getKeyroots(A, aNodes);
        bKR = getKeyroots(B, bNodes);
        
        treeDist                = new int  [aNodes.length][bNodes.length];
        krToForestDist          = new int  [aNodes.length][bNodes.length][][];
        treeDistIdxToForestKr   = new Point[aNodes.length][bNodes.length];
        treeDistToForestIndexes = new Point[aNodes.length][bNodes.length];
    }
    
    /**
     * Performs the Zhang-Shasha algorithm with path recovery.
     * 
     * @return Set of strings corresponding to the edit operations.
     */
    public Set<String> runZhangShasha() {
        for (int i : aKR) {
            for (int j : bKR) {
                krToForestDist[i][j] = computeForestDist(i,j);
            }
        }
        return computeEdits();
    }
    
    /**
     * Computes the forest distance between keyroots i and j 
     * in A and B, respoectively.
     *
     * @param i
     * @param j
     * @return the computation matrix
     */
    private int[][] computeForestDist(int i, int j) {
        int m = i - aLs[i] + 2;
        int n = j - bLs[j] + 2; 
        int ioff = aLs[i] - 1;
        int joff = bLs[j] - 1;
        
        int[][] fd = new int[m][n];
        int[] ops = new int[NUM_OPS];
        
        for (int x = 1; x < m; x++) {
            fd[x][0] = fd[x-1][0] + DELETE_COST;
        }
        
        for (int y = 1; y < n; y++) {
            fd[0][y] = fd[0][y-1] + INSERT_COST;
        }
    
        for (int x = 1; x < m; x++) {         
            for (int y = 1; y < n; y++) {             
                
                boolean bothTrees = (aLs[i] == aLs[x+ioff] && bLs[j] == bLs[y+joff]);
                
                fd[x][y] = min(computeOps(fd, ops, x, y, ioff, joff, bothTrees));
                
                if (bothTrees) {
                    treeDist               [x+ioff][y+joff] = fd[x][y];          
                    treeDistIdxToForestKr  [x+ioff][y+joff] = new Point(i, j); 
                    treeDistToForestIndexes[x+ioff][y+joff] = new Point(x, y);
                }
            }
        }       
        return fd;
    }
    
    /**
     * Computes the optimal operation recurrence for computeForestDistance.
     */
    private int[] computeOps(int[][] fd, int[] ops, int x, int y, int ioff, int joff, boolean bothTrees) {
        ops[DELETE_IDX] = fd[x-1][y] + DELETE_COST;   
        ops[INSERT_IDX] = fd[x][y-1] + INSERT_COST;  
        if (bothTrees) {       
            ops[ALIGN_IDX] = fd[x-1][y-1] + (aNodes[x+ioff].label.equals(bNodes[y+joff].label) ? 0 : REPLACE_COST);
        } else {
            int p = aLs[x+ioff] - 1 - ioff;
            int q = bLs[y+joff] - 1 - joff;
            ops[ALIGN_IDX] = fd[p][q] + treeDist[x+ioff][y+joff];
        }
        return ops;
    }
    
    /**
     * 
     * @return
     */
    private Set<String> computeEdits() {
        Stack<Point> S = new Stack<>(); 
        Set<String> M = new HashSet<>();  
        S.add(new Point(aNodes.length - 1, bNodes.length - 1)); 
      
        while (!S.isEmpty()) {             
            Point treeIdx = S.pop();
            Point krs = treeDistIdxToForestKr[treeIdx.x][treeIdx.y];
            Point forestIdx = treeDistToForestIndexes[treeIdx.x][treeIdx.y];
            int[][] fd = krToForestDist[krs.x][krs.y];           
            recoverSoln(treeIdx.x, treeIdx.y, forestIdx.x, forestIdx.y, S, M, fd);
        }
        return M;
    }
    
    /**
     * Recovers the edit script for a given forest distance computation.
     */
    private void recoverSoln(int i, int j, int x, int y, Stack<Point> S, Set<String> M, int[][] fd) {
        int ioff = aLs[i] - 1;
        int joff = bLs[j] - 1;       
        
        int[] ops = new int[NUM_OPS];       
        
        while (x > 0 && y > 0) {        
            boolean bothTrees = (aLs[i] == aLs[x+ioff] && bLs[j] == bLs[y+joff]);          
            int min = min(computeOps(fd, ops, x, y, ioff, joff, bothTrees));     
        
            String edit = null;
            if (ops[DELETE_IDX] == min) {
                edit = "DELETE " + aNodes[x+ioff];
                x--;
            } else if (ops[INSERT_IDX] == min) {
                edit = "INSERT " + bNodes[y + joff];
                y--;
            } else {
                if (bothTrees) {     
                    if (aNodes[x+ioff].label.equals(bNodes[y+joff].label)) {
                        edit = "MATCH " + aNodes[x+ioff] + " " + bNodes[y+joff];
                    } else {
                        edit = "REPLACE " + aNodes[x+ioff] + " " + bNodes[y+joff];
                    }
                    x--;
                    y--;
                } else {
                    S.push(new Point(x + ioff, y + joff));
                    x = aLs[x+ioff] - 1 - ioff;
                    y = bLs[y+joff] - 1 - joff;
                }
            }
            if (edit != null) M.add(edit);
        }
        while (x > 0) {
            M.add("DELETE " + aNodes[x+ioff]);
            x--;
        }
        while (y > 0) {
            M.add("INSERT " + bNodes[y+joff]);
            y--;
        }
    }
    
    //-Pre-Processing----------------------------------------------------------
    
    /**
     * @param root
     * @return
     */
    private static Node[] getOrderedNodes(Node root) {
        List<Node> list = getOrderedNodes(root, new ArrayList<Node>());
        Node[] nodes = new Node[list.size()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = list.get(i);
        }
        return nodes;
    }
    
    /**
     * Helper method for getOrderedNodes
     */
    private static List<Node> getOrderedNodes(Node root, List<Node> list) {
        for (Node n : root.children) {
            getOrderedNodes(n, list);
        }
        list.add(root);
        return list;
    }

    /**
     * @param nodes
     * @return
     */
    private static int[] getLValues(Node[] nodes) {
        int[] l = new int[nodes.length];
        List<Node> nodeList = Arrays.asList(nodes);
        for (int i = 0; i < nodes.length; i++) {
            Node cur = nodes[i];
            while (cur.children.size() > 0) {
                cur = cur.children.get(0);
            }
            l[i] = nodeList.indexOf(cur);
        }
        return l;
    }

    /**
     * @param root
     * @param nodes
     * @return
     */
    private static int[] getKeyroots(Node root, Node[] nodes) {
        List<Node> KRNodes = getKeyroots(root, new ArrayList<Node>(), true);
        List<Node> nodeList = Arrays.asList(nodes);
        
        int[] KRs = new int[KRNodes.size()];
        for (int i = 0; i < KRs.length; i++) {
            KRs[i] = nodeList.indexOf(KRNodes.get(i));
        }
        return KRs;
    }
    
    /**
     * Helper method for getKeyroots
     */
    private static List<Node> getKeyroots(Node root, ArrayList<Node> krNodes, boolean kr) {
        for (int i = 0; i < root.children.size(); i++) {
            getKeyroots(root.children.get(i), krNodes, i > 0);
        }
        if (kr) krNodes.add(root);
        return krNodes;
    }
    
    //-Utility-Methods----------------------------------------------------------
  
    private static int min(int[] args) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < args.length; i++)
            min = args[i] < min ? args[i] : min;
        
        return min;
    }
    
    /**
     * A simple node class for demonstration.
     */
    public static class Node {
        String label;
        List<Node> children;
        
        public Node(String label, Node... children) {
            this.label = label;
            this.children = Arrays.asList(children);
        }
        
        public void printTree() {
            printTree("");
        }
        
        private void printTree(String pre) {
            System.out.println(pre + this);
            for (Node n : this.children) {
                n.printTree("  " + pre);
            }
        }
        
        @Override
        public String toString() { 
            return this.label; 
        }
    }
}