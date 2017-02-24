package conflerge.differ.ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

/**
 * Unfortunately, Javaparser's trees don't play nicely with out algorithm. We need to add another layer
 * of indirection to correctly distinguish children of one type from children of another in nodes that
 * contain lists of children. That's the motivation for the hacky approach.
 */
@SuppressWarnings({ "rawtypes" })
public class NodeListWrapper extends NodeList {
    NodeList<? extends Node> nodeList;
    boolean removeIfEmpty = false;
    String id = "";
    
    public Node node;
    
    public String toString() {
        return "NodelistWrapper:" + id;
    }
    
    public NodeListWrapper(NodeList<? extends Node> nodeList) {
        this.nodeList = nodeList;
    }
    
    public NodeListWrapper(NodeList<? extends Node> nodeList, String id) {
        this.nodeList = nodeList;
        this.id = id;
    }
    
    public NodeListWrapper(NodeList<? extends Node> nodeList, boolean removeIfEmpty) {
        this.nodeList = nodeList;
        this.removeIfEmpty = removeIfEmpty;
    }
    
    @Override
    public boolean isEmpty() { return false; }
}

/**
 * A Node representation of the NodeListWrapper, used by the diff algorithm.
 */
class NodeListWrapperNode extends SimpleName {
    
    public final NodeListWrapper list;
    
    public NodeListWrapperNode(NodeListWrapper list) { 
        super("Node(" + list + ")");
        this.list = list;
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> arg0, A arg1) { return arg0.visit((SimpleName) this, arg1); }

    @Override
    public <A> void accept(VoidVisitor<A> arg0, A arg1) { arg0.visit((SimpleName) this, arg1); }
    
}

