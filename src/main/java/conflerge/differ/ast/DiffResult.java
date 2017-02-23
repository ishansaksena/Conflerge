package conflerge.differ.ast;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.Visitable;

public class DiffResult {
    
    public static boolean merge(DiffResult local, DiffResult remote, ASTDiffer localDiffer, ASTDiffer remoteDiffer) {
          boolean canMerge = true; 
          
          // Verify Delete-Delete pairs are disjoint
          canMerge &= compareSets(local.deletes.keySet(), remote.deletes.keySet(), localDiffer.parentsA);
          canMerge &= compareSets(remote.deletes.keySet(), local.deletes.keySet(), localDiffer.parentsA);
            
          // Verify Delete-Replace pairs are disjoint
          canMerge &= compareSets(local.replaces.keySet(), remote.deletes.keySet(), localDiffer.parentsA);
          canMerge &= compareSets(remote.replaces.keySet(), local.deletes.keySet(), localDiffer.parentsA);
          
          // Verify Replace-Replace pairs are disjoint
          canMerge &= compareSets(local.replaces.keySet(), remote.replaces.keySet(), localDiffer.parentsA);
          canMerge &= compareSets(remote.replaces.keySet(), local.replaces.keySet(), localDiffer.parentsA);
                  
          // Verify Delete-Insert pairs are disjoint
          
          // Verify Replace-Insert pairs are disjoint
          
          // Verify Insert-Insert pairs are disjoint
          
          return canMerge;
    }
    
    // Verify that no item from dA is a equal to or a parent of any item from dB
    private static boolean compareSets(Set<Node> dA, Set<Node> dB, Map<Node, Node> parents) {
        for (Node nA : dA) {
            for (Node nB : dB) {
                while (nB != null) {
                    if (nB == nA) {
                        return false;
                    }
                    nB = parents.get(nB);
                }
            }
        }
        return true;
    }
    
    public final Map<Node, Node> deletes;
    public final Map<Node, Node> replaces;
    public final Map<Node, Node> insertsPre;
    public final Map<Node, Node> insertsPost;
    public final Map<Visitable, List<Node>> insertsUnder;
      
    public DiffResult(Map<Node, Node> deletes, 
                      Map<Node, Node> replaces,
                      Map<Node, Node> insertsPre,
                      Map<Node, Node> insertsPost,
                      Map<Visitable, List<Node>> insertsUnder) {
        
        this.deletes = deletes;
        this.replaces = replaces;
        this.insertsPre = insertsPre;
        this.insertsPost = insertsPost;
        this.insertsUnder = insertsUnder;
    }
      
    public boolean deleted(Node n) {
        return deletes.containsKey(n);
    }
    
    public boolean replaced(Node n) {
        return replaces.containsKey(n);
    }
    
    public Node getReplacement(Node n) {
        return replaces.get(n);
    }
}