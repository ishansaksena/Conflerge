package conflerge.differ.ast;

import java.util.ArrayList;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.visitor.Visitable;

/**
 * 'Unwraps' a tree that has been wrapped by NodeListWrapperVisitor.
 */
public class NodeListUnwrapperVisitor extends ModifierVisitor<DiffResult>  {  
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Visitable visit(NodeList n, DiffResult arg) {
        if (n instanceof NodeListWrapper) {
            n = ((NodeListWrapper) n).nodeList;
        }
        for (Node node : new ArrayList<Node>(n)) {
            node.accept(this, arg);
        }
        return n;
    } 
}