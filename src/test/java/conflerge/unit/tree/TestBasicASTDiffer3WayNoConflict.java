package conflerge.unit.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import conflerge.differ.ast.ASTDiffer;
import conflerge.differ.ast.DiffResult;
import conflerge.differ.ast.MergeVisitor;
import conflerge.differ.ast.NodeListUnwrapperVisitor;
import conflerge.differ.ast.NodeListWrapperVisitor;
import conflerge.merger.TreeMerger;

/**
 * TODO: update this file so it functions with the most recent modifications.
 *
 */
public class TestBasicASTDiffer3WayNoConflict {    
    
    private static void merge(String baseStr, String localStr, String remoteStr, String expectedStr) { 
        Node base   = JavaParser.parse(baseStr);
        Node local  = JavaParser.parse(localStr);    
        Node remote = JavaParser.parse(remoteStr);    

        base.accept(new NodeListWrapperVisitor(), "A"); 
        local.accept(new NodeListWrapperVisitor(), "B");
        remote.accept(new NodeListWrapperVisitor(), "C");
        
        DiffResult localDiff = new ASTDiffer(base, local).diff();
        DiffResult remoteDiff = new ASTDiffer(base, remote).diff();
        
        TreeMerger.conflict = false;
        
        base.accept(new MergeVisitor(), new Pair<DiffResult, DiffResult>(localDiff, remoteDiff));   
        base.accept(new NodeListUnwrapperVisitor(), null); 
        
        assertEquals(TreeMerger.conflict, false);
        assertEquals(JavaParser.parse(expectedStr), base);
        
    }
    
    
    @Test
    public void testIdentical() {
        merge(
                "class Foo { }",
                "class Foo { }",
                "class Foo { }",
                "class Foo { }"
        );
    }
    
    @Test
    public void testDeleteInsert() {
        merge(
                "class Foo { int x;}",
                "class Foo { int x; void foo() { } }",
                "class Foo { }",
                "class Foo { void foo() { } }"
        );
    }
    
    @Test
    public void testInsertEitherSide() {
        merge(
                "class Foo { int x; }",
                "class Foo { int x; void foo() { } }",
                "class Foo { int y; int x; }",
                "class Foo { int y; int x; void foo() { } }"
        );
    }
    
    @Test
    public void testDeleteParamChangeBody() {
        merge(
                "class Foo { void foo(int i) { } }",
                "class Foo { void foo(int i) { print(\"hello\"); } }",
                "class Foo { void foo() { } }",
                "class Foo { void foo() { print(\"hello\"); } }"
        );
    }
    
    @Test
    public void testInsertParamChangeBody() {
        merge(
                "class Foo { void foo(int i) { } }",
                "class Foo { void foo(int i) { print(\"hello\"); } }",
                "class Foo { void foo(int a, int i, int b) { } }",
                "class Foo { void foo(int a, int i, int b) { print(\"hello\"); } }"
        );
    }
    
    @Test
    public void testReplaceParamsChangeBody() {
        merge(
                "class Foo { void foo(int i) { } }",
                "class Foo { void foo(int i) { print(\"hello\"); } }",
                "class Foo { void foo(int a, int b) { } }",
                "class Foo { void foo(int a, int b) { print(\"hello\"); } }"
        );
    }
}