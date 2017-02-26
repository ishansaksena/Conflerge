package conflerge.differ.ast;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;

/**
 * Stores the data associated with a Diff between two trees A and B.
 * The fields are sets of edit operations that transform A to B.
 */
public class DiffResult {
    /**
     * Set of Nodes in A that were deleted.
     */
    public final Map<Node, Node> deletes;
    
    /**
     * Map from Nodes in A -> replacements in B.
     */
    public final Map<Node, Node> replaces;
    
    /**
     * Map from NodeListWrapper -> index in list -> nodes inserted at index.
     */
    public final Map<NodeListWrapper, Map<Integer, List<Node>>> listInserts;
    
    /**
     * Map from nodes in A -> altered modifiers in B.
     */
    public final Map<Node, EnumSet<Modifier>> modifiers;
      
    public DiffResult(Map<Node, Node> deletes, 
                      Map<Node, Node> replaces,
                      Map<Node, EnumSet<Modifier>> modifiers,
                      Map<NodeListWrapper, Map<Integer, List<Node>>> listInserts) {
        
        this.deletes = deletes;
        this.replaces = replaces;
        this.listInserts = listInserts;
        this.modifiers = modifiers;
    }
      
    /**
     * @param n
     * @return true iff n was deleted
     */
    public boolean deleted(Node n) {
        return deletes.containsKey(n);
    }
    
    /**
     * @param n
     * @return true iff n was replaced
     */
    public boolean replaced(Node n) {
        return replaces.containsKey(n);
    }
}