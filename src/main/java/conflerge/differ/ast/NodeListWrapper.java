package conflerge.differ.ast;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

/**
 * Unfortunately, Javaparser's trees don't play nicely with our algorithm. This Visitor adds a
 * layer of indirection to  distinguish between children of one type from children of another in 
 * nodes that contain lists of children.
 */
@SuppressWarnings({ "rawtypes" })
public class NodeListWrapper extends NodeList {
    NodeList<? extends Node> nodeList;
    
    public Node node;
    
    public NodeListWrapper(NodeList<? extends Node> nodeList) {
        this.nodeList = nodeList;
    }
    
    @Override
    public boolean isEmpty() { 
        return nodeList.isEmpty();
    }
}

/**
 * A Node representation of the NodeListWrapper, used by the diff algorithm.
 * These should only be used to keep track of NodeListWrappers in collections of 
 * Nodes, and should never be added to an ASTs.
 */
class NodeListWrapperNode extends Node {
    
    private static final Range dummy_range = new Range(new Position(0, 0), new Position(0, 0));
    
    public final NodeListWrapper list;
    
    public NodeListWrapperNode(NodeListWrapper list) { 
        super(dummy_range);
        this.list = list;
    }
    
    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) { return null; }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) { }
}

