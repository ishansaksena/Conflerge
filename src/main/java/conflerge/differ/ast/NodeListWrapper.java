package conflerge.differ.ast;

import java.util.List;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

/**
 * Unfortunately, Javaparser's trees don't play nicely with out algorithm. We need to add another layer
 * of indirection to correctly distinguish children of one type from children of another in nodes that
 * contain lists of children. That's the motivation for the hacky approach.
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class NodeListWrapper extends NodeList {
    NodeList<? extends Node> nodeList;
    boolean removeIfEmpty = false;
    public Node node;
    
    public NodeListWrapper(NodeList<? extends Node> nodeList) {
        this.nodeList = nodeList;
    }
    
    public NodeListWrapper(NodeList<? extends Node> nodeList, boolean removeIfEmpty) {
        this.nodeList = nodeList;
        this.removeIfEmpty = removeIfEmpty;
    }

    @SuppressWarnings("unchecked")
    public static void wrapAST(Node root) {
        
        List<MethodDeclaration> methods = root.getNodesByType(MethodDeclaration.class);
        for (MethodDeclaration method : methods) {
            method.setParameters(new NodeListWrapper(method.getParameters()));
            method.setThrownExceptions(new NodeListWrapper(method.getThrownExceptions()));
            method.setTypeParameters(new NodeListWrapper(method.getTypeParameters()));
            method.setAnnotations(new NodeListWrapper(method.getAnnotations()));
        }   
        
        List<ClassOrInterfaceDeclaration> classes = root.getNodesByType(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration clazz : classes) {
            clazz.setExtendedTypes(new NodeListWrapper(clazz.getExtendedTypes()));
            clazz.setImplementedTypes(new NodeListWrapper(clazz.getImplementedTypes()));
            clazz.setTypeParameters(new NodeListWrapper(clazz.getTypeParameters()));
            clazz.setMembers(new NodeListWrapper(clazz.getMembers()));
            clazz.setAnnotations(new NodeListWrapper(clazz.getAnnotations()));
        } 
        
        List<ClassOrInterfaceType> classTypes = root.getNodesByType(ClassOrInterfaceType.class);
        for (ClassOrInterfaceType clazz : classTypes) {
//            NodeListWrapper typeArgs = clazz.getTypeArguments().isPresent() ? 
//                    new NodeListWrapper(clazz.getTypeArguments().get()) : 
//                        new NodeListWrapper(new NodeList<Type>(), true);
//            clazz.setTypeArguments(typeArgs);
            clazz.setAnnotations(new NodeListWrapper(clazz.getAnnotations()));
        } 
        
        List<AnnotationDeclaration> annotations = root.getNodesByType(AnnotationDeclaration.class);
        for (AnnotationDeclaration ann : annotations) {
            ann.setMembers(new NodeListWrapper(ann.getMembers()));
            ann.setAnnotations(new NodeListWrapper(ann.getAnnotations()));
        } 
        
        List<AnnotationMemberDeclaration> annotationMems = root.getNodesByType(AnnotationMemberDeclaration.class);
        for (AnnotationMemberDeclaration ann : annotationMems) {
            ann.setAnnotations(new NodeListWrapper(ann.getAnnotations()));
        } 
        
        List<ArrayCreationExpr> arrayCreate = root.getNodesByType(ArrayCreationExpr.class);
        for (ArrayCreationExpr arr : arrayCreate) {
            arr.setLevels(new NodeListWrapper(arr.getLevels()));
        }
        
        List<ArrayInitializerExpr> arrayInit = root.getNodesByType(ArrayInitializerExpr.class);
        for (ArrayInitializerExpr arr : arrayInit) {
            arr.setValues(new NodeListWrapper(arr.getValues()));
        }
        
        List<CompilationUnit> cus = root.getNodesByType(CompilationUnit.class);
        for (CompilationUnit cu : cus) {
            cu.setImports(new NodeListWrapper(cu.getImports()));
            cu.setTypes(new NodeListWrapper(cu.getTypes()));
        }
        
        List<ConstructorDeclaration> cons = root.getNodesByType(ConstructorDeclaration.class);
        for (ConstructorDeclaration con : cons) {
            con.setParameters(new NodeListWrapper(con.getParameters()));
            con.setThrownExceptions(new NodeListWrapper(con.getThrownExceptions()));
            con.setTypeParameters(new NodeListWrapper(con.getTypeParameters()));
            con.setAnnotations(new NodeListWrapper(con.getAnnotations()));
        }
        
        List<EnumConstantDeclaration> enums = root.getNodesByType(EnumConstantDeclaration.class);
        for (EnumConstantDeclaration num : enums) {
            num.setArguments(new NodeListWrapper(num.getArguments()));
            num.setClassBody(new NodeListWrapper(num.getClassBody()));
            num.setAnnotations(new NodeListWrapper(num.getAnnotations()));
        }
        
        List<EnumDeclaration> enumDecls = root.getNodesByType(EnumDeclaration.class);
        for (EnumDeclaration num : enumDecls) {
            num.setImplementedTypes(new NodeListWrapper(num.getImplementedTypes()));
            num.setMembers(new NodeListWrapper(num.getMembers()));
            num.setAnnotations(new NodeListWrapper(num.getAnnotations()));
        }
        
        List<ExplicitConstructorInvocationStmt> expCons = root.getNodesByType(ExplicitConstructorInvocationStmt.class);
        for (ExplicitConstructorInvocationStmt expCon : expCons) {
            NodeListWrapper typeArgs = expCon.getTypeArguments().isPresent() ? 
                    new NodeListWrapper(expCon.getTypeArguments().get()) : 
                        new NodeListWrapper(new NodeList<Type>(), true);
            expCon.setArguments(new NodeListWrapper(expCon.getArguments()));
            expCon.setTypeArguments(typeArgs);
        }
        
//        List<FieldAccessExpr> fieldAccs = root.getNodesByType(FieldAccessExpr.class);
//        for (FieldAccessExpr fieldAcc : fieldAccs) {
//            NodeListWrapper typeArgs = fieldAcc.getTypeArguments().isPresent() ? 
//                    new NodeListWrapper(fieldAcc.getTypeArguments().get()) : 
//                        new NodeListWrapper(new NodeList<Type>(), true);
//            fieldAcc.setTypeArguments(typeArgs);
//        }
//        
//        List<FieldDeclaration> fieldDecs = root.getNodesByType(FieldDeclaration.class);
//        for (FieldDeclaration fieldDec : fieldDecs) {
//            fieldDec.setVariables(new NodeListWrapper(fieldDec.getVariables()));
//            fieldDec.setAnnotations(new NodeListWrapper(fieldDec.getAnnotations()));
//        }
//        
        List<ForStmt> forStmts = root.getNodesByType(ForStmt.class);
        for (ForStmt forStmt : forStmts) {
            forStmt.setInitialization(new NodeListWrapper(forStmt.getInitialization()));
            forStmt.setUpdate(new NodeListWrapper(forStmt.getUpdate()));
        }
        
        List<InitializerDeclaration> initializers = root.getNodesByType(InitializerDeclaration.class);
        for (InitializerDeclaration initializer : initializers) {
            initializer.setAnnotations(new NodeListWrapper(initializer.getAnnotations()));
        }
        
        List<MethodCallExpr> methodCalls = root.getNodesByType(MethodCallExpr.class);
        for (MethodCallExpr methodCall : methodCalls) {
//            NodeListWrapper typeArgs = methodCall.getTypeArguments().isPresent() ? 
//                    new NodeListWrapper(methodCall.getTypeArguments().get()) : 
//                        new NodeListWrapper(new NodeList<Type>(), true);
//            methodCall.setTypeArguments(typeArgs);
            methodCall.setArguments(new NodeListWrapper(methodCall.getArguments()));
        }
        
        List<NormalAnnotationExpr> normalAnns = root.getNodesByType(NormalAnnotationExpr.class);
        for (NormalAnnotationExpr normalAnn : normalAnns) {
            normalAnn.setPairs(new NodeListWrapper(normalAnn.getPairs()));
        }
        
        List<ObjectCreationExpr> objs = root.getNodesByType(ObjectCreationExpr.class);
        for (ObjectCreationExpr obj : objs) {
//            NodeListWrapper typeArgs = obj.getTypeArguments().isPresent() ? 
//                    new NodeListWrapper(obj.getTypeArguments().get()) : 
//                        new NodeListWrapper(new NodeList<Type>(), true);
//                    
//            NodeListWrapper anon = obj.getAnonymousClassBody().isPresent() ? 
//                    new NodeListWrapper(obj.getAnonymousClassBody().get()) : 
//                        new NodeListWrapper(new NodeList(), true);
            
            //obj.setAnonymousClassBody(anon);  
            //obj.setTypeArguments(typeArgs);
            obj.setArguments(new NodeListWrapper(obj.getArguments()));
        }
        
        List<PackageDeclaration> pckgs = root.getNodesByType(PackageDeclaration.class);
        for (PackageDeclaration pckg : pckgs) {
            pckg.setAnnotations(new NodeListWrapper(pckg.getAnnotations()));
        }
        
        List<Parameter> params = root.getNodesByType(Parameter.class);
        for (Parameter param : params) {
            param.setAnnotations(new NodeListWrapper(param.getAnnotations()));
        }
        
        List<PrimitiveType> prims = root.getNodesByType(PrimitiveType.class);
        for (PrimitiveType prim : prims) {
            prim.setAnnotations(new NodeListWrapper(prim.getAnnotations()));
        }
        
        List<ArrayType> arrs = root.getNodesByType(ArrayType.class);
        for (ArrayType arr : arrs) {
            arr.setAnnotations(new NodeListWrapper(arr.getAnnotations()));
        }
        
        List<BlockStmt> blocks = root.getNodesByType(BlockStmt.class);
        for (BlockStmt block : blocks) {
            block.setStatements(new NodeListWrapper(block.getStatements()));
        }
        
        List<ArrayCreationLevel> arrls = root.getNodesByType(ArrayCreationLevel.class);
        for (ArrayCreationLevel arr : arrls) {
            arr.setAnnotations(new NodeListWrapper(arr.getAnnotations()));
        }
        
        List<IntersectionType> inters = root.getNodesByType(IntersectionType.class);
        for (IntersectionType inter : inters) {
            inter.setElements(new NodeListWrapper(inter.getElements()));
            inter.setAnnotations(new NodeListWrapper(inter.getAnnotations()));
        }
        
        List<UnionType> unions = root.getNodesByType(UnionType.class);
        for (UnionType union : unions) {
            union.setElements(new NodeListWrapper(union.getElements()));
            union.setAnnotations(new NodeListWrapper(union.getAnnotations()));
        }
        
        List<SwitchEntryStmt> switchEntries = root.getNodesByType(SwitchEntryStmt.class);
        for (SwitchEntryStmt switchEntry : switchEntries) {
            switchEntry.setStatements(new NodeListWrapper(switchEntry.getStatements()));
        }
        
        List<SwitchStmt> switchStmts= root.getNodesByType(SwitchStmt.class);
        for (SwitchStmt switchStmt : switchStmts) {
            switchStmt.setEntries(new NodeListWrapper(switchStmt.getEntries()));
        }
        
        List<TryStmt> tryStmts = root.getNodesByType(TryStmt.class);
        for (TryStmt tryStmt : tryStmts) {
            tryStmt.setCatchClauses(new NodeListWrapper(tryStmt.getCatchClauses()));
            tryStmt.setResources(new NodeListWrapper(tryStmt.getResources()));
        }
               
        List<TypeParameter> typeParams = root.getNodesByType(TypeParameter.class);
        for (TypeParameter typeParam : typeParams) {
            typeParam.setTypeBound(new NodeListWrapper(typeParam.getTypeBound()));
            typeParam.setAnnotations(new NodeListWrapper(typeParam.getAnnotations()));
        }
        
        List<UnknownType> unknowns = root.getNodesByType(UnknownType.class);
        for (UnknownType unknown : unknowns) {
            unknown.setAnnotations(new NodeListWrapper(unknown.getAnnotations()));
        }
        
//        List<VariableDeclarationExpr> varDecls = root.getNodesByType(VariableDeclarationExpr.class);
//        for (VariableDeclarationExpr varDecl : varDecls) {
//            varDecl.setVariables(new NodeListWrapper(varDecl.getVariables()));
//            varDecl.setAnnotations(new NodeListWrapper(varDecl.getAnnotations()));
//        }
        
        List<VoidType> voids = root.getNodesByType(VoidType.class);
        for (VoidType v : voids) {
            v.setAnnotations(new NodeListWrapper(v.getAnnotations()));
        }
        
        List<WildcardType> wilds = root.getNodesByType(WildcardType.class);
        for (WildcardType wild : wilds) {
            wild.setAnnotations(new NodeListWrapper(wild.getAnnotations()));
        }
        
        List<LambdaExpr> lambdas = root.getNodesByType(LambdaExpr.class);
        for (LambdaExpr lambda : lambdas) {
            lambda.setParameters(new NodeListWrapper(lambda.getParameters()));
        }
        
//        List<MethodReferenceExpr> methodRefs = root.getNodesByType(MethodReferenceExpr.class);
//        for (MethodReferenceExpr methodRef : methodRefs) {
//            NodeListWrapper typeArgs = methodRef.getTypeArguments().isPresent() ? 
//                    new NodeListWrapper(methodRef.getTypeArguments().get()) : 
//                        new NodeListWrapper(new NodeList<Type>(), true);
//
//           methodRef.setTypeArguments(typeArgs);
//        }       
    }
}

/**
 * A Node representation of the NodeListWrapper, used by the diff algorithm.
 */
class NodeListWrapperNode extends SimpleName {
    
    public final NodeListWrapper list;
    
    public NodeListWrapperNode(NodeListWrapper list) { 
        this.list = list;
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> arg0, A arg1) { return arg0.visit((SimpleName) this, arg1); }

    @Override
    public <A> void accept(VoidVisitor<A> arg0, A arg1) { arg0.visit((SimpleName) this, arg1); }
    
}

