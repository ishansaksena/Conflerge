package conflerge.differ.ast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;

/**
 * Uses the mmdiff algorithm (by Sudarshan S. Chawathe, available at http://www.vldb.org/conf/1999/P8.pdf)
 * to diff two ASTs (referred to as A and B throughout) and generate an edit script from tree A to tree B.
 * 
 * This algorithm gives the minimum-cost sequence of insert, delete, and replace edits between the two
 * trees, where all edits occur on the tree's leaf nodes. So although the edits themselves are single node
 * operations, after they are computed they can view them as entire subtree insertions, deletions, and 
 * replacements. This is particularly useful for merging.
 * 
 * TODO: explain list-nonlist insterts, nodelistwrapping, etc.
 */
public class ASTDiffer {
    
    /*
     * The cost of an edit operation.
     */
    public static final int editCost = 1;
    
    /*  
     * Nodes in A that were deleted in B. Logically this is a Set
     *  --not a Map--but we use IdentityHashMap because nodes are 
     *  tracked based on object identity rather than equality.
     */
    private Map<Node, Node> deletes = new IdentityHashMap<>();
    
    /*
     *  Mapping from nodes in B that replaced nodes in A and the 
     *  opposite mapping from nodes replaced in A to replacements 
     *  in B.
     */
    private Map<Node, Node> replacesA = new IdentityHashMap<>();
    private Map<Node, Node> replacesB = new IdentityHashMap<>();
    
    /*
     * Mappings between nodes in A and B that were aligned. 
     * The mmdiff algorithm enforces that key, values nodes MUST be 
     * shallowly equal. (otherwise it would be a replacement, 
     * not an alignment)
     */
    private Map<Node, Node> alignsA = new IdentityHashMap<>();
    private Map<Node, Node> alignsB = new IdentityHashMap<>();
    
    private Map<Node, EnumSet<Modifier>> modifiers = new IdentityHashMap<>();
    
    /*
     * A map from NodeListWrapperNodes to all nodes that were
     * inserted under their NodeList. These represent list
     * insertions, 
     */
    private Map<NodeListWrapperNode, List<Node>> listInserts  = new IdentityHashMap<>();
    
    /*
     *  Sometimes mmdiff will return insertions that do not correspond
     *  to inserting a node into a list of parameters. If these insertions
     *  are top-level (That is, there are no modifications on these insertion's
     *  ancestors) performing the merge is problematic. These insertions
     *  are stored separately so they can later be translated into replace
     *  operations and merged correctly.
     */
    private Map<Node, Node> nonListInserts = new IdentityHashMap<>();
    
    /*
     * 
     */
    public final Map<Node, Node> parentsA;
    public final Map<Node, Node> parentsB;
    
       
    /*
     * opt: Array of optimal subproblem edit distances.
     * m  : Length of opt's first dimension, equal to the number of nodes in A.
     * n  : Length of opt's second dimension, equal to the number of nodes in B.
     */
    private int[][] opt;
    private final int m;
    private final int n;
    
    /*
     * The nodes in A, B in pre-order.
     */
    private final Node[] aN;
    private final Node[] bN;
    
    /*
     * The depth of nodes in A, B in pre-order.
     * if Node n is the ith node in A and has depth d,
     * then: aD[i] = d
     */
    private final int[] aD;
    private final int[] bD;

    /**
     * Constructs a new ASTDiffer to operate on trees A, B.
     * A call to diff() will return a DiffResult containing
     * an edit script from A to B.
     * 
     * @param A 'Base' tree for the diff.
     * @param B 'Dest' tree for the diff.
     */
    public ASTDiffer(Node A, Node B) {        
        this.aN = ASTInputProcessing.getOrderedNodes(A);
        this.bN = ASTInputProcessing.getOrderedNodes(B);
       
        this.aD = ASTInputProcessing.getDepths(A);
        this.bD = ASTInputProcessing.getDepths(B);
        
        this.parentsA = ASTInputProcessing.getParentMap(A);
        this.parentsB = ASTInputProcessing.getParentMap(B);
        
        m = aN.length;
        n = bN.length;

        this.opt = new int[m][n];
    }

    /**
     * @return An edit script from this ASTDiffer's A tree to its B tree.
     */
    public DiffResult diff() {
        // Run mmdiff to compute the edit distance between trees A and B.
        computeEdits();
        
        // Run mmdiff backward to recover the edit script, stored in the
        // replace, align, and insert fields.
        recoverEdits();
        
        // mmDiff may return an edit script that includes an insertion that
        // isn't inserting a node into a list of nodes. Performing these inserts
        // is prohibitively complicated on trees where edges are labeled (eg., ASTs).
        // When this occurs, add a 'replace' operation for the inserted node's parent
        // to avoid performing the non-list insert.
        replaceNonListInserts();
         
        // Produce a mapping of NodeLists -> indexes to insert -> Nodes to insert.
        // This operation must be performed last because it depends on complete
        // alignment and replacement maps.
        Map<NodeListWrapper, Map<Integer, List<Node>>> indexInserts = processInserts(listInserts);
        
        return new DiffResult(deletes, replacesA, indexInserts, modifiers);
    }

    /*
     * Runs the mmdiff algorithm. Popluates opt with the computation's results.
     */
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
  
    /*
     * Recovers the edit script. Must not be called until opt has been
     * populated by a call to computeEdits.
     */
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
               addAlignOrReplace(i, j);
               i--;
               j--;
           } 
       }
       while (i > 0) { addDelete(i, j); i--; }
       while (j > 0) { addInsert(i, j); j--; }
    }
     
    private void addAlignOrReplace(int i, int j) {
        if (updateCost(aN[i], bN[j]) == 0)  {
            if (aN[i] instanceof NodeWithModifiers) {
                EnumSet<Modifier> aMods = ((NodeWithModifiers<?>) aN[i]).getModifiers();
                EnumSet<Modifier> bMods = ((NodeWithModifiers<?>) bN[j]).getModifiers();
                if (!aMods.equals(bMods)) {
                    modifiers.put(aN[i], bMods);
                }
            }
            alignsA.put(aN[i], bN[j]);
            alignsB.put(bN[j], aN[i]);
        } else {
            replacesA.put(aN[i], bN[j]);
            replacesB.put(bN[j], aN[i]);
        }
    }

    private void addDelete(int i, int j) {
        deletes.put(aN[i], aN[i]);
    }
    
    private void addInsert(int i, int j) {
        Node parent = parentsB.get(bN[j]);     
        if (parentsB.get(bN[j]) instanceof NodeListWrapperNode) {
            if (!listInserts.containsKey(parent)) {
              listInserts.put((NodeListWrapperNode) parent, new ArrayList<Node>());
            }
            listInserts.get(parent).add(bN[j]);
        } else {
            nonListInserts.put(bN[j], bN[j]);
        }
    }   
    
    //-Output-Processing---------------------------------------------
    
    /*
     *  Replaces any non-list insert operation with the replacement of that
     *  Node's parent. This operation spares our merge visitor from performing 
     *  inserts that are not into lists of nodes.
     */
    private void replaceNonListInserts() {
        for (Node n : nonListInserts.keySet()) {
            if (alignsB.containsKey(parentsB.get(n))) {
                replacesA.put(alignsB.get(parentsB.get(n)), parentsB.get(n));
            }
        }
    }
    
    /*
     * TODO: Document this method.
     */
    private Map<NodeListWrapper, Map<Integer, List<Node>>> processInserts(Map<NodeListWrapperNode, List<Node>> insertedUnder) {
        Map<NodeListWrapper, Map<Integer, List<Node>>> nlToIndexToInserts = new IdentityHashMap<>();        
        for (NodeListWrapperNode nlwn : insertedUnder.keySet()) {
            NodeListWrapperNode alignedNlwn = (NodeListWrapperNode) alignsB.get(nlwn);
            if (alignedNlwn == null) {
                continue;
            }
            NodeList<? extends Node> nl = nlwn.list.nodeList;
            Map<Integer, List<Node>> inserts = new HashMap<>();
            Collections.reverse(insertedUnder.get(nlwn));
            for (Node insert : insertedUnder.get(nlwn)) {               
                int i = indexOfObj(nl, insert);
                while (i >= 0 && !alignsB.containsKey(nl.get(i)) && !replacesB.containsKey(nl.get(i))) {
                    i--;
                }
                int insertIndex;
                if (i >= 0) {
                    Node matchedSibling = alignsB.containsKey(nl.get(i)) ? 
                                                alignsB.get(nl.get(i)) : 
                                                    replacesB.get(nl.get(i));      
                    NodeListWrapperNode matchedParent = (NodeListWrapperNode) parentsA.get(matchedSibling);
                    insertIndex = indexOfObj(matchedParent.list.nodeList, matchedSibling) + 1;
                } else {
                    insertIndex = 0;
                } 
                if (!inserts.containsKey(insertIndex)) {
                    inserts.put(insertIndex, new ArrayList<Node>());
                }
                inserts.get(insertIndex).add(insert);
            }
            nlToIndexToInserts.put(alignedNlwn.list, inserts);
        }
        return nlToIndexToInserts;
    }

    //-Utility-Methods---------------------------------------------
    
    /*
     * Nodes are identified strictly by object equality, so this
     * method is the only correct way to located a Node in a list.
     */
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
    
    /*
     * Returns the minimum value from the parameters.
     */
    private static int min(int... args) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < args.length; i++)
            min = args[i] < min ? args[i] : min;
          
        return min;
    }
      
    /*
     * Returns 1 iff Node n1, n2 are shallowly equal. 
     */
    private int updateCost(Node n1, Node n2) {
        return ShallowEqualsVisitor.equals(n1, n2) ? 0 : editCost;
    } 
}