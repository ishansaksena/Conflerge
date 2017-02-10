package conflerge.differ;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * This file is still experimental. It needs to be converted to work with
 * ASTs and return real edits before it can be used in Conflerge.
 */
public class TreeDiffer {
    
    public static final int DELETE_COST  = 1;
    public static final int INSERT_COST  = 1;
    public static final int REPLACE_COST = 1;
    
    private static final int DELETE_IDX  = 0;
    private static final int INSERT_IDX  = 1;
    private static final int ALIGN_IDX   = 2;
    private static final int NUM_OPS     = 3;
    
    private final Node[] An;
    private final Node[] Bn;
    private final int[] Al;
    private final int[] Bl;
    private final int[] Akr;
    private final int[] Bkr;
    
    private int[][] treeDist;   
    private int[][][][] krToForestDist;
    private Point[][] treeDistIdxToForestKr;
    private Point[][] treeDistToForestIndexes;
    
    public TreeDiffer(Node A, Node B) {
        An  = getOrderedNodes(A);
        Bn  = getOrderedNodes(B);
        
        Al  = getLValues(An);
        Bl  = getLValues(Bn);
        
        Akr = getKeyroots(A, An);
        Bkr = getKeyroots(B, Bn);
        
        treeDist                = new int  [An.length][Bn.length];
        krToForestDist          = new int  [An.length][Bn.length][][];
        treeDistIdxToForestKr   = new Point[An.length][Bn.length];
        treeDistToForestIndexes = new Point[An.length][Bn.length];
    }
    
    public Set<String> diff() {
        for (int i : Akr) {
            for (int j : Bkr) {
                krToForestDist[i][j] = computeForestDist(i,j);
            }
        }
        return computeEdits();
    }
    
    private int[][] computeForestDist(int i, int j) {
        int m = i - Al[i] + 2;
        int n = j - Bl[j] + 2; 
        int ioff = Al[i] - 1;
        int joff = Bl[j] - 1;
        
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
                boolean bothTrees = (Al[i] == Al[x+ioff] && Bl[j] == Bl[y+joff]);
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
    
    private int[] computeOps(int[][] fd, int[] ops, int x, int y, int ioff, int joff, boolean bothTrees) {
        ops[DELETE_IDX] = fd[x-1][y] + DELETE_COST;   
        ops[INSERT_IDX] = fd[x][y-1] + INSERT_COST;  
        if (bothTrees) {       
            ops[ALIGN_IDX] = fd[x-1][y-1] + (An[x+ioff].label.equals(Bn[y+joff].label) ? 0 : REPLACE_COST);
        } else {
            int p = Al[x+ioff] - 1 - ioff;
            int q = Bl[y+joff] - 1 - joff;
            ops[ALIGN_IDX] = fd[p][q] + treeDist[x+ioff][y+joff];
        }
        return ops;
    }
    
    private Set<String> computeEdits() {
        Stack<Point> S = new Stack<>(); 
        Set<String> M = new HashSet<>();  
        S.add(new Point(An.length - 1, Bn.length - 1)); 
      
        while (!S.isEmpty()) {             
            Point treeIdx = S.pop();
            Point krs = treeDistIdxToForestKr[treeIdx.x][treeIdx.y];
            Point forestIdx = treeDistToForestIndexes[treeIdx.x][treeIdx.y];
            int[][] fd = krToForestDist[krs.x][krs.y];           
            recoverSoln(treeIdx.x, treeIdx.y, forestIdx.x, forestIdx.y, S, M, fd);
        }
        return M;
    }
    
    private void recoverSoln(int i, int j, int x, int y, Stack<Point> S, Set<String> M, int[][] fd) {
        int ioff = Al[i] - 1;
        int joff = Bl[j] - 1;       
        
        int[] ops = new int[NUM_OPS];       
        
        while (x > 0 && y > 0) {        
            boolean bothTrees = (Al[i] == Al[x+ioff] && Bl[j] == Bl[y+joff]);          
            int min = min(computeOps(fd, ops, x, y, ioff, joff, bothTrees));     
        
            String edit = null;
            if (ops[DELETE_IDX] == min) {
                edit = "DELETE " + An[x+ioff];
                x--;
            } else if (ops[INSERT_IDX] == min) {
                edit = "INSERT " + Bn[y + joff];
                y--;
            } else {
                if (bothTrees) {     
                    if (An[x+ioff].label.equals(Bn[y+joff].label)) {
                        edit = "MATCH " + An[x+ioff] + " " + Bn[y+joff];
                    } else {
                        edit = "REPLACE " + An[x+ioff] + " " + Bn[y+joff];
                    }
                    x--;
                    y--;
                } else {
                    S.push(new Point(x + ioff, y + joff));
                    x = Al[x+ioff] - 1 - ioff;
                    y = Bl[y+joff] - 1 - joff;
                }
            }
            addEdit(M, edit);
        }
        while (x > 0) {
            addEdit(M, "DELETE " + An[x+ioff]);
            x--;
        }
        while (y > 0) {
            addEdit(M, "INSERT " + Bn[y+joff]);
            y--;
        }
    }
    
    private void addEdit(Set<String> M, String edit) {
        if (edit != null) {
            if (M.contains(edit)) {
                System.err.println("Duplicate edit");
                throw new IllegalStateException();
            }
            M.add(edit);
        }
    }
    
    private static Node[] getOrderedNodes(Node root) {
        List<Node> list = getOrderedNodes(root, new ArrayList<Node>());
        Node[] nodes = new Node[list.size()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = list.get(i);
        }
        return nodes;
    }
    
    private static List<Node> getOrderedNodes(Node root, List<Node> list) {
        for (Node n : root.children) {
            getOrderedNodes(n, list);
        }
        list.add(root);
        return list;
    }

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

    private static int[] getKeyroots(Node root, Node[] nodes) {
        List<Node> KRNodes = getKeyroots(root, new ArrayList<Node>(), true);
        List<Node> nodeList = Arrays.asList(nodes);
        
        int[] KRs = new int[KRNodes.size()];
        for (int i = 0; i < KRs.length; i++) {
            KRs[i] = nodeList.indexOf(KRNodes.get(i)); //  PROBLEMATIC -- requires unique labels
        }
        return KRs;
    }
    
    private static List<Node> getKeyroots(Node root, ArrayList<Node> KRNodes, boolean kr) {
        for (int i = 0; i < root.children.size(); i++) {
            getKeyroots(root.children.get(i), KRNodes, i > 0);
        }
        if (kr) KRNodes.add(root);
        return KRNodes;
    }
    
    private static int min(int[] args) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < args.length; i++)
            min = args[i] < min ? args[i] : min;
        
        return min;
    }
    
    public static class Node {
        String label;
        List<Node> children;
        Node parent;
        
        public Node(String label, Node... children) {
            this.label = label;
            this.children = Arrays.asList(children);
            for (Node n : this.children) {
                n.parent = this;
            }
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
        
        @Override
        public boolean equals(Object o) { 
            if (o instanceof Node) {
                return this.label.equals(((Node) o).label);
            }
            return false;
        }
        
        @Override
        public int hashCode() { 
            return this.label.hashCode(); 
        }
    }
}