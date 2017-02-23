package conflerge.unit.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

import conflerge.differ.ast.ASTDiffer;
import conflerge.differ.ast.ASTMergeVisitor;
import conflerge.differ.ast.CleanUpVisitor;
import conflerge.differ.ast.NodeListWrapper;

public class TestBasicASTDiffer2WayLanguageConstructs {
    
    private static void merge(String f1, String f2, String expected) { 
        Node t1 = JavaParser.parse(f1);
        Node t2 = JavaParser.parse(f2);     
        
        NodeListWrapper.wrapAST(t1);
        NodeListWrapper.wrapAST(t2); 
        
        t1.accept(new ASTMergeVisitor(), new ASTDiffer(t1, t2).diff());
        t1.accept(new CleanUpVisitor(), null);
        
        Node e = JavaParser.parse(expected);

        assertEquals(e, t1);   
    }
    
    //-Conditionals--------------------------------
    
    @Test
    public void testChangeCondition1() {
        merge(
                "class Foo { void foo() { if (x) { print(hello); } } }",
                "class Foo { void foo() { if (x > 0) { print(hello); } } }",
                "class Foo { void foo() { if (x > 0) { print(hello); } } }"
        );
    }
    
    @Test
    public void testChangeCondition2() {
        merge(
                "class Foo { void foo() { if (x) { print(hello); } } }",
                "class Foo { void foo() { if (y && x) { print(hello); } } }",
                "class Foo { void foo() { if (y && x) { print(hello); } } }"
        );
    }
    
    @Test
    public void testReduceCondition1() {
        merge(
                "class Foo { void foo() { if (y && x) { print(hello); } } }",
                "class Foo { void foo() { if (y) { print(hello); } } }",
                "class Foo { void foo() { if (y) { print(hello); } } }"
        );
    }
    
    @Test
    public void testDeleteBody() {
        merge(
                "class Foo { void foo() { if (y) { print(hello); } } }",
                "class Foo { void foo() { if (y) { } } }",
                "class Foo { void foo() { if (y) { } } }"
        );
    }
    
    @Test
    public void testInsertBody() {
        merge(
                "class Foo { void foo() { if (y) { } } }",
                "class Foo { void foo() { if (y) { print(hello); } } }",
                "class Foo { void foo() { if (y) { print(hello); } } }"
        );
    }
    
    @Test
    public void testInsertBodyAndCondition() {
        merge(
                "class Foo { void foo() { if (y) { } } }",
                "class Foo { void foo() { if (y & x) { print(hello); } } }",
                "class Foo { void foo() { if (y & x ) { print(hello); } } }"
        );
    }
    
    //-Loops--------------------------------
    
    @Test
    public void testDeleteWhileBody() {
        merge(
                "class Foo { void foo() { while (y) { print(hello); } } }",
                "class Foo { void foo() { while (y) { } } }",
                "class Foo { void foo() { while (y) { } } }"
        );
    }
    
    @Test
    public void testInsertWhileBody() {
        merge(
                "class Foo { void foo() { while (y) { } } }",
                "class Foo { void foo() { while (y) { print(hello); } } }",
                "class Foo { void foo() { while (y) { print(hello); } } }"
        );
    }
    
    @Test
    public void testDeleteForBody() {
        merge(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { print(hello); } } }",
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }"
        );
    }
    
    @Test
    public void testInsertForBody() {
        merge(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { print(hello); } } }",
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { print(hello); } } }"
        );
    }
    
    @Test
    public void testChangeWhileCondition1() {
        merge(
                "class Foo { void foo() { while (y) { } } }",
                "class Foo { void foo() { while (x < 10) { } } }",
                "class Foo { void foo() { while (x < 10) { } } }"
        );
    }
    
    @Test
    public void testChangeWhileCondition2() {
        merge(
                "class Foo { void foo() { while (y) { } } }",
                "class Foo { void foo() { while (y && x) { } } }",
                "class Foo { void foo() { while (y && x) {  } } }"
        );
    }
    
    @Test
    public void testChangeForCondition1() {
        merge(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 0; i < 20; i++) { } } }",
                "class Foo { void foo() { for (int i = 0; i < 20; i++) { } } }"
        );
    }
    
    @Test
    public void testChangeForCondition2() {
        merge(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 1; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 1; i < 10; i++) { } } }"
        );
    }
    
    @Test
    public void testChangeForCondition3() {
        merge(
                "class Foo { void foo() { for (int i = 0; i < 10; i++) { } } }",
                "class Foo { void foo() { for (int i = 1; i < 20; i++) { } } }",
                "class Foo { void foo() { for (int i = 1; i < 20; i++) { } } }"
        );
    }
    
    //-Arrays--------------------------------

    @Test
    public void testChangeArraySize() {
        merge(
                "class Foo { int[] arr = new int[10]; }",
                "class Foo { int[] arr = new int[20]; }",
                "class Foo { int[] arr = new int[20]; }"
        );
    }
    
    @Test
    public void testChangeArrayName() {
        merge(
                "class Foo { int[] arr = new int[10]; }",
                "class Foo { int[] a = new int[20]; }",
                "class Foo { int[] a = new int[20]; }"
        );
    }
    
    @Test
    public void testChangeArrayAccess() {
        merge(
                "class Foo { int foo() { return arr[0]; } }",
                "class Foo { int foo() { return arr[1]; } }",
                "class Foo { int foo() { return arr[1]; } }"
        );
    }
    
    @Test
    public void testChangeArrayAccessName() {
        merge(
                "class Foo { int foo() { return arr[0]; } }",
                "class Foo { int foo() { return a[1]; } }",
                "class Foo { int foo() { return a[1]; } }"
        );
    }
    
    @Test
    public void testReplaceExprWithArray() {
        merge(
                "class Foo { int foo() { return i; } }",
                "class Foo { int foo() { return a[1]; } }",
                "class Foo { int foo() { return a[1]; } }"
        );
    }
    
    //-Constructor-Calls--------------------------------
    
    @Test
    public void testAddConstructorParamEnd() {
        merge(
                "class Foo { int foo() { new Foo(a, b, c); } }",
                "class Foo { int foo() { new Foo(a, b, c, d); } }",
                "class Foo { int foo() { new Foo(a, b, c, d); } }"
        );
    }
    
    @Test
    public void testAddConstructorParamBegin() {
        merge(
                "class Foo { int foo() { new Foo(a, b, c); } }",
                "class Foo { int foo() { new Foo(d, a, b, c); } }",
                "class Foo { int foo() { new Foo(d, a, b, c); } }"
        );
    }
    
    @Test
    public void testAddConstructorParamMid() {
        merge(
                "class Foo { int foo() { new Foo(a, b, c); } }",
                "class Foo { int foo() { new Foo(a, b, d, c); } }",
                "class Foo { int foo() { new Foo(a, b, d, c); } }"
        );
    }
    
    @Test
    public void testAddConstructorParamEmpty() {
        merge(
                "class Foo { int foo() { new Foo(); } }",
                "class Foo { int foo() { new Foo(a); } }",
                "class Foo { int foo() { new Foo(a); } }"
        );
    }
    
    @Test
    public void testRemoveConstructorParam() {
        merge(
                "class Foo { int foo() { new Foo(a); } }",
                "class Foo { int foo() { new Foo(); } }",
                "class Foo { int foo() { new Foo(); } }"
        );
    }
}
