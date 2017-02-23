package conflerge.differ.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EmptyMemberDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

/**
 * Performs the operations to turn one AST into another.
 */
@SuppressWarnings("deprecation")
public class ASTMergeVisitor extends ModifierVisitor<DiffResult> {
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Visitable visit(NodeList n, DiffResult arg) {
      if (n instanceof NodeListWrapper) {
          n = ((NodeListWrapper) n).nodeList;
          if (n.isEmpty()) { 
              if (arg.insertsUnder.containsKey(n)) {
                  for (Node node :arg.insertsUnder.get(n)) {
                      n.add((Node) node.accept(new CleanUpVisitor(), null));
                  }
              }
              return n;
              
          }
      } else if (n.isEmpty()) {
          return n;
      } 

      final List<Node> changeList = new ArrayList<>();
      for (Node node : new ArrayList<Node>(n)) {
          changeList.add((Node) node.accept(this, arg));
      } 
      n.clear();
      
      List<Node> nodes = new ArrayList<>();
      Stack<Node> s = new Stack<>();
      for (Node node : changeList) {
          s.add(node);
      }
      
      Map<Node, Boolean> added = new IdentityHashMap<>(); 
      while (!s.isEmpty()) {
          Node next = s.pop();
          if (next == null) {
              continue;
          }
          
          if (arg.insertsPre.containsKey(next)) {
              Node insert = arg.insertsPre.get(next);
              if (!added.containsKey(insert)) {
                  added.put(insert, true);
                  s.push(next);
                  s.push((Node) insert.accept(new CleanUpVisitor(), null));
                  arg.insertsPre.remove(next);
              } else { 
                  nodes.add(next);
              }
          
          } else if (arg.insertsPost.containsKey(next)) {
              Node insert = arg.insertsPost.get(next);
              if (!added.containsKey(insert)) {
                  added.put(insert, true);
                  s.push((Node) insert.accept(new CleanUpVisitor(), null));
                  s.push(next);
                  arg.insertsPost.remove(next);        
              } else {
                  nodes.add(next);
              }
          } else { 
              nodes.add(next);
          }
      }
      
      Collections.reverse(nodes);

      for (Node node : nodes) {
          n.add(node);
      }
      return n;
    }

    @Override
    public Visitable visit(final AnnotationDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private void visitAnnotations(NodeWithAnnotations<?> n, DiffResult arg) {
        n.setAnnotations((NodeList<AnnotationExpr>) n.getAnnotations().accept(this, arg));
    }

    @Override
    public Visitable visit(final AnnotationMemberDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ArrayAccessExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ArrayCreationExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ArrayInitializerExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final AssertStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final AssignExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final BinaryExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final BlockStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final BooleanLiteralExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final BreakStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final CastExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final CatchClause n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final CharLiteralExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final ClassExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ClassOrInterfaceDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        SimpleName name = (SimpleName) n.getName().accept(this, arg);
        n.setName(name);
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ClassOrInterfaceType n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final CompilationUnit n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ConditionalExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ConstructorDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ContinueStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final DoStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final DoubleLiteralExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final EmptyMemberDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final EmptyStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final EnclosedExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final EnumConstantDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final EnumDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ExplicitConstructorInvocationStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ExpressionStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final FieldAccessExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final FieldDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ForeachStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ForStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final IfStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final InitializerDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final InstanceOfExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final IntegerLiteralExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final JavadocComment n, final DiffResult arg) {
       
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final LabeledStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final LongLiteralExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final MarkerAnnotationExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final MemberValuePair n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final MethodCallExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final MethodDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        SimpleName name = (SimpleName) n.getName().accept(this, arg);
        n.setName(name);
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final NameExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        SimpleName name = (SimpleName) n.getName().accept(this, arg);
        if (name == null)
            return null;
        n.setName(name);
        return n;
    }

    @Override
    public Visitable visit(final NormalAnnotationExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final NullLiteralExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final ObjectCreationExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final PackageDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final Parameter n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final Name n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final PrimitiveType n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(SimpleName n, DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(ArrayType n, DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(ArrayCreationLevel n, DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final IntersectionType n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final UnionType n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ReturnStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final SingleMemberAnnotationExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final StringLiteralExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }

    @Override
    public Visitable visit(final SuperExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final SwitchEntryStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final SwitchStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final SynchronizedStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ThisExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final ThrowStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final TryStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final LocalClassDeclarationStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final TypeParameter n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final UnaryExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final UnknownType n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final VariableDeclarationExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final VariableDeclarator n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final VoidType n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final WhileStmt n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final WildcardType n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final LambdaExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final MethodReferenceExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final TypeExpr n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Node visit(final ImportDeclaration n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return (Node) arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final BlockComment n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(final LineComment n, final DiffResult arg) {
        if (arg.replaced(n)) {
            return arg.getReplacement(n).accept(new CleanUpVisitor(), null);
        }
        if (arg.deleted(n)) {
            return null;
        }
        return n;
    }
}
