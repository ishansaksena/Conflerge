package conflerge.differ.ast;

import java.util.ArrayList;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.visitor.Visitable;

public class NodeListUnwrapperVisitor extends ModifierVisitor<DiffResult>  {  
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Visitable visit(NodeList n, DiffResult arg) {
        if (n instanceof NodeListWrapper) {
            
           // System.out.println("unwrap " + n);
            
            n = ((NodeListWrapper) n).nodeList;
        }
        for (Node node : new ArrayList<Node>(n)) {
            node.accept(this, arg);
        }
        return n;
    } 
}