package conflerge.unit.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

import conflerge.differ.ast.ASTDiffer;
import conflerge.differ.ast.ASTMergeVisitor;
import conflerge.differ.ast.CleanUpVisitor;
import conflerge.differ.ast.DiffResult;
import conflerge.differ.ast.MergeVisitor;
import conflerge.differ.ast.NodeListUnwrapperVisitor;
import conflerge.differ.ast.NodeListWrapper;
import conflerge.differ.ast.NodeListWrapperVisitor;

public class TestBasicASTDiffer2WayLanguageConstructs {
    
    private static void eval(String str1, String str2) {   
        Node n1  = JavaParser.parse(str1);
        Node n2 = JavaParser.parse(str2);   

        n1.accept(new NodeListWrapperVisitor(), "A"); 
        n2.accept(new NodeListWrapperVisitor(), "B");
        
        DiffResult res = new ASTDiffer(n1, n2).diff();
        
        n1.accept(new MergeVisitor(), res);   
        n1.accept(new NodeListUnwrapperVisitor(), null); 
        
        assertEquals(JavaParser.parse(str2), n1);
    }
    
    public void test(String str1, String str2) {
        eval(str1, str2);
        eval(str2, str1);
    }
    //-Conditionals--------------------------------
    
    @Test
    public void testChangeCondition1() {
        test(
                "class Foo { void foo() { if (x) { print(hello); } } }",
                "class Foo { void foo() { if (x > 0) { print(hello); } } }"
        );
    }
    
    @Test
    public void testChangeCondition2() {
        test(
                "class Foo { void foo() { if (x) { print(hello); } } }",
                "class Foo { void foo() { if (y && x) { print(hello); } } }"
        );
    }
    
    @Test
    public void testReduceCondition1() {
        test(
                "class Foo { void foo() { if (y && x) { print(hello); } } }",
                "class Foo { void foo() { if (y) { print(hello); } } }"
        );
    }
    
    @Test
    public void testDeleteBody() {
        test(
                "class Foo { void foo() { if (y) { print(hello); } } }",
                "class Foo { void foo() { if (y) { } } }"
        );
    }
    
    @Test
    public void testInsertBody() {
        test(
                "class Foo { void foo() { if (y) { } } }",
                "class Foo { void foo() { if (y) { print(hello); } } }"
        );
    }
    
    @Test
    public void testInsertBodyAndCondition() {
        test(
                "class Foo { void foo() { if (y) { } } }",
                "class Foo { void foo() { if (y & x) { print(hello); } } }"
        );
    }
    
    //-Loops--------------------------------
    
    @Test
    public void testDeleteWhileBody() {
        test(
                "class Foo { void foo() { while (y) { print(hello); } } }",
                "class Foo { void foo() { while (y) { } } }"
        );
    }
    
    @Test
    public void testInsertWhileBody() {
        test(
                "class Foo { void foo() { while (y) { } } }",
                "class Foo { void foo() { while (y) { print(hello); } } }"
        );
    }
    
    @Test
    public void testDeleteForBody() {
        test(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { print(hello); } } }",
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }"
        );
    }
    
    @Test
    public void testInsertForBody() {
        test(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { print(hello); } } }"
        );
    }
    
    @Test
    public void testChangeWhileCondition1() {
        test(
                "class Foo { void foo() { while (y) { } } }",
                "class Foo { void foo() { while (x < 10) { } } }"
        );
    }
    
    @Test
    public void testChangeWhileCondition2() {
        test(
                "class Foo { void foo() { while (y) { } } }",
                "class Foo { void foo() { while (y && x) { } } }"
        );
    }
    
    @Test
    public void testChangeForCondition1() {
        test(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 0; i < 20; i++) { } } }"
        );
    }
    
    @Test
    public void testChangeForCondition2() {
        test(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 1; i < 10; i++) { } } }"
        );
    }
    
    @Test
    public void testChangeForCondition3() {
        test(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 1; i < 20; i++) { } } }"
        );
    }
    
    //-Arrays--------------------------------

    @Test
    public void testChangeArraySize() {
        test(
                "class Foo { int[] arr = new int[10]; }",
                "class Foo { int[] arr = new int[20]; }"
        );
    }
    
    @Test
    public void testChangeArrayName() {
        test(
                "class Foo { int[] arr = new int[10]; }",
                "class Foo { int[] a = new int[20]; }"
        );
    }
    
    @Test
    public void testChangeArrayAccess() {
        test(
                "class Foo { int foo() { return arr[0]; } }",
                "class Foo { int foo() { return arr[1]; } }"
        );
    }
    
    @Test
    public void testChangeArrayAccessName() {
        test(
                "class Foo { int foo() { return arr[0]; } }",
                "class Foo { int foo() { return a[1]; } }"
        );
    }
    
    @Test
    public void testReplaceExprWithArray() {
        test(
                "class Foo { int foo() { return i; } }",
                "class Foo { int foo() { return a[1]; } }"
        );
    }
    
    //-Constructor-Calls--------------------------------
    
    @Test
    public void testAddConstructorParamEnd() {
        test(
                "class Foo { int foo() { new Foo(a, b, c); } }",
                "class Foo { int foo() { new Foo(a, b, c, d); } }"
        );
    }
    
    @Test
    public void testAddConstructorParamBegin() {
        test(
                "class Foo { int foo() { new Foo(a, b, c); } }",
                "class Foo { int foo() { new Foo(d, a, b, c); } }"
        );
    }
    
    @Test
    public void testAddConstructorParamMid() {
        test(
                "class Foo { int foo() { new Foo(a, b, c); } }",
                "class Foo { int foo() { new Foo(a, b, d, c); } }"
        );
    }
    
    @Test
    public void testAddConstructorParamEmpty() {
        test(
                "class Foo { int foo() { new Foo(); } }",
                "class Foo { int foo() { new Foo(a); } }"
        );
    }
    
    @Test
    public void testRemoveConstructorParam() {
        test(
                "class Foo { int foo() { new Foo(a); } }",
                "class Foo { int foo() { new Foo(); } }"
        );
    }
}
