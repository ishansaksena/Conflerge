package conflerge.differ.ast;

import java.util.ArrayList;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.visitor.Visitable;

public class NodeListWrapperVisitor extends ModifierVisitor<String> {
    
    int modcount = 0;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Visitable visit(NodeList n, String arg) {
        for (Node node : new ArrayList<Node>(n)) {
            node.accept(this, arg);
        }
        
        n = new NodeListWrapper(n, modcount  + arg);
        modcount++;
        
        //System.out.println("wrap   " + n);
       
        return n;
    }
}