package conflerge.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import conflerge.ConflergeUtil;
import conflerge.tree.visitor.MergeVisitor;
import conflerge.tree.visitor.NodeListUnwrapperVisitor;
import conflerge.tree.visitor.NodeListWrapperVisitor;

public class Test3WayNoConflict {    
    
    /**
     * Perform a merge of baseStr, localStr, and remoteStr. Asserts that
     * the result of the merge matches expectedStr.
     */
    private static void merge(String baseStr, String localStr, String remoteStr, String expectedStr) { 
        Node base   = JavaParser.parse(baseStr);
        Node local  = JavaParser.parse(localStr);    
        Node remote = JavaParser.parse(remoteStr);    

        base.accept(new NodeListWrapperVisitor(), "A"); 
        local.accept(new NodeListWrapperVisitor(), "B");
        remote.accept(new NodeListWrapperVisitor(), "C");
        
        DiffResult localDiff = new TreeDiffer(base, local).diff();
        DiffResult remoteDiff = new TreeDiffer(base, remote).diff();
        
        ConflergeUtil.conflict = false;
        
        base.accept(new MergeVisitor(), new Pair<DiffResult, DiffResult>(localDiff, remoteDiff));   
        base.accept(new NodeListUnwrapperVisitor(), null); 
        
        assertEquals(ConflergeUtil.conflict, false);
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
    
    @Test
    public void testCommentInsert() {
        merge(
                "class Foo { int x; }",
                "class Foo { int x; }",
                "class Foo { /*Test*/ int x; }",
                "class Foo { /*Test*/ int x; }"
        );
    }
    
    @Test
    public void testCommentDelete() {
        merge(
                "class Foo { /*Test*/ int x; }",
                "class Foo { /*Test*/ int x; }",
                "class Foo {  int x; }",
                "class Foo {  int x; }"
        );
    }
    
    @Test
    public void testCommentConflict() {
        merge(
                "class Foo { int x; }",
                "class Foo { /*Test*/int x; }",
                "class Foo { /*Test*/ int x; }",
                "class Foo { /*>>> LOCAL: Test\n<<< REMOTE: Test*/ int x; }"
        );
    }
    
    @Test
    public void testCommentConflictDelete() {
        merge(
                "class Foo { /*Test*/ int x; }",
                "class Foo { /*Test One*/int x; }",
                "class Foo { int x; }",
                "class Foo { /*>>> LOCAL: Test One\n<<< REMOTE: */ int x; }"
        );
    }
}