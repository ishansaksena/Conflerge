package conflerge.tree.ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;

/**
 * Unfortunately, Javaparser's trees don't play nicely with our algorithm. This Visitor adds a
 * layer of indirection to  distinguish between children of one type from children of another in 
 * nodes that contain lists of children.
 */
@SuppressWarnings({ "rawtypes" })
public class NodeListWrapper extends NodeList {
    public NodeList<? extends Node> nodeList;
    
    public Node node;
    
    public NodeListWrapper(NodeList<? extends Node> nodeList) {
        this.nodeList = nodeList;
    }
    
    @Override
    public boolean isEmpty() { 
        return nodeList.isEmpty();
    }
}

