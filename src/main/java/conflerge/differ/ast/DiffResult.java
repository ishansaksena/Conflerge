package conflerge.differ.ast;

import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.Visitable;

public class DiffResult {
    public final Map<Node, Node> deletes;
    public final Map<Node, Node> replaces;
    public final Map<NodeListWrapper, Map<Integer, List<Node>>> insertsUnder;
    public final Map<Node, EnumSet<Modifier>> modifiers;
      
    public DiffResult(Map<Node, Node> deletes, 
                      Map<Node, Node> replaces,
                      Map<NodeListWrapper, Map<Integer, List<Node>>> insertsUnder, Map<Node, EnumSet<Modifier>> modifiers) {
        
        this.deletes = deletes;
        this.replaces = replaces;
        this.insertsUnder = insertsUnder;
        this.modifiers = modifiers;
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