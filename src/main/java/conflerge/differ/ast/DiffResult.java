package conflerge.differ.ast;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.Visitable;

public class DiffResult {
    
    public static boolean merge(DiffResult local, DiffResult remote, ASTDiffer localDiffer, ASTDiffer remoteDiffer) {
          boolean canMerge = true; 
          
          // Verify Delete-Delete pairs are disjoint
          canMerge &= compareSets(local.deletes.keySet(),  remote.deletes.keySet(), localDiffer.parentsA);
          canMerge &= compareSets(remote.deletes.keySet(), local.deletes.keySet(),  localDiffer.parentsA);
            
          // Verify Delete-Replace pairs are disjoint
          canMerge &= compareSets(local.replaces.keySet(),  remote.deletes.keySet(),  localDiffer.parentsA);
          canMerge &= compareSets(remote.replaces.keySet(), local.deletes.keySet(),   localDiffer.parentsA);
          canMerge &= compareSets(local.deletes.keySet(),   remote.replaces.keySet(), localDiffer.parentsA);
          canMerge &= compareSets(remote.deletes.keySet(),  local.replaces.keySet(),  localDiffer.parentsA);
          
          // Verify Replace-Replace pairs are disjoint
          canMerge &= compareSets(local.replaces.keySet(),  remote.replaces.keySet(), localDiffer.parentsA);
          canMerge &= compareSets(remote.replaces.keySet(), local.replaces.keySet(),  localDiffer.parentsA);
                  
          Map<Node, Node> allParents = new IdentityHashMap<>();
          for (Node n : remoteDiffer.parentsA.keySet()) {
              allParents.put(n, remoteDiffer.parentsA.get(n));
          }
          for (Node n : localDiffer.parentsA.keySet()) {
              allParents.put(n, localDiffer.parentsA.get(n));
          }
          
          // Verify Delete/Replace-InsertUnder pairs are disjoint
          canMerge &= compareVisitableSets(local.replaces.keySet(),  remote.insertsUnder.keySet(), allParents);
          canMerge &= compareVisitableSets(local.deletes.keySet(),   remote.insertsUnder.keySet(), allParents);   
          canMerge &= compareVisitableSets(remote.replaces.keySet(), local.insertsUnder.keySet(),  allParents);
          canMerge &= compareVisitableSets(remote.deletes.keySet(),  local.insertsUnder.keySet(),  allParents);
          
//          canMerge &= compareVisitableSets(remote.insertsUnder.keySet(), local.replaces.keySet(),  allParents);
//          canMerge &= compareVisitableSets(remote.insertsUnder.keySet(), local.deletes.keySet(),   allParents);   
//          canMerge &= compareVisitableSets(local.insertsUnder.keySet(),  remote.replaces.keySet(), allParents);
//          canMerge &= compareVisitableSets(local.insertsUnder.keySet(),  remote.deletes.keySet(),  allParents);
          
          // Verify Replace-Insert pairs are disjoint
          
          // Verify Insert-Insert pairs are disjoint
          
          return canMerge;
    }
    
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
    
    private static boolean compareVisitableSets(Set<? extends Visitable> dA, Set<? extends Visitable> dB, Map<Node, Node> parents) {
        for (Visitable nA : dA) {
            Node nodeA = getNode(nA);
            for (Visitable nB : dB) {
                while (nB != null) {
                    Node nodeB = getNode(nB);
                    if (nodeA == nodeB) {
                        return false;
                    }
                    nB =  parents.get(nodeB);
                }
            }
        }
        return true;
    }
    
    private static Node getNode(Visitable n) {
        if (n instanceof NodeListWrapper) {
            return ((NodeListWrapper) n).node;
        } else {
            return (Node) n;
        }
    }
    
    public final Map<Node, Node> deletes;
    public final Map<Node, Node> replaces;
    public final Map<NodeListWrapper, Map<Integer, List<Node>>> insertsUnder;
      
    public DiffResult(Map<Node, Node> deletes, 
                      Map<Node, Node> replaces,
                      Map<NodeListWrapper, Map<Integer, List<Node>>> insertsUnder) {
        
        this.deletes = deletes;
        this.replaces = replaces;
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