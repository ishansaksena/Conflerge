package conflerge.differ.ast;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

/**
 * This Visitor removes any NodeListWrappers remaining in an AST.
 */
@SuppressWarnings("rawtypes")
public class CleanUpVisitor extends ModifierVisitor<DiffResult>{

    @Override
    public Visitable visit(NodeList n, DiffResult arg) {
      if (n instanceof NodeListWrapper) {
          boolean remove = ((NodeListWrapper) n).removeIfEmpty;
          n = ((NodeListWrapper) n).nodeList;
          if (n.isEmpty() && remove) { 
              return null;
          }
          for (Object node : n) {
              ((Node) node).accept(this, arg);
          }
      } 
      return n;
    }
}
