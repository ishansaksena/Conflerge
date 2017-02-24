package conflerge.unit.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

import conflerge.differ.ast.ASTDiffer;
import conflerge.differ.ast.DiffResult;
import conflerge.differ.ast.MergeVisitor;
import conflerge.differ.ast.NodeListUnwrapperVisitor;
import conflerge.differ.ast.NodeListWrapperVisitor;

public class TestBasicASTDiffer2Way {
    
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
    
    @Test
    public void testIdentical() {
        test(
                "class Foo { int x; int y; }",
                "class Foo { int x; int y; }"
        );
    }
    
    //-Deletions--------------------------------
    
    @Test
    public void testSimpleDelete1() {
        test(
                "class Foo { int x;}",
                "class Foo { }"
        );
    }
    
    @Test
    public void testSimpleDelete3() {
        test(
                "class Foo { int x; int y;}",
                "class Foo { int x; }"
        );
    }
    
    @Test
    public void testSimpleDelete4() {
        test(
                "class Foo { int x; int y;}",
                "class Foo { int y; }"
        );
    }
    
    //-Replacements--------------------------------
    
    @Test
    public void testSimpleReplace1() {
        test(
                "class Foo { int x;}",
                "class Foo { int y; }"
        );
    }
    
    @Test
    public void testSimpleReplace2() {
        test(
                "class Foo { int x; }",
                "class Bar { int x; }"
        );
    }
    
    @Test
    public void testSimpleReplace3() {
        test(
                "class Foo { int x; int y; }",
                "class Foo { int x; int z; }"
        );
    }
    
    @Test
    public void testSimpleReplace4() {
        test(
                "class Foo { int x; int y;}",
                "class Foo { int z; int y; }"
        );
    }
    
    //-Insertions--------------------------------
    
    @Test
    public void testSimpleInsert1() {
        test(
                "class Foo { int x; }",
                "class Foo { int x; int y; }"
        );
    }
    
    @Test
    public void testSimpleInsert2() {
        test(
                "class Foo { int x; }",
                "class Bar { int y; int x; }"
        );
    }
    
    @Test
    public void testSimpleInsert3() {
        test(
                "class Foo { int x; int y; }",
                "class Foo { int x; int z; int y; }"
        );
    }
    
    @Test
    public void testSimpleInsert4() {
        test(
                "class Foo { void foo(int i) { if (i > 0) { print(i); } } }",
                "class Foo { void foo(int i) { if (i > 0) { print(i); i++; } } }"
        );
    }
    
    @Test
    public void testSimpleInsert5() {
        test(
                "class Foo { void foo(int i) { if (i > 0) { print(i); } } }",
                "class Foo { void foo(int i) { if (i > 0) { i++; print(i); } } }"
        );
    }
    
    @Test
    public void testSimpleInsert6() {
        test(
                "class Foo { void foo(int i) { if (i > 0) { print(i); } } }",
                "class Foo { void foo(int i) { if (i > 0 && true) { print(i); } } }"
        );
    }
       
    @Test
    public void testSimpleInsert7() {
        test(
                "public class Foo { void foo(int i) { if (i > 0) { print(i); } } }",
                "public class Foo { void foo(int i) { if (true && i > 0) { print(i); } } }"
        );
    }
    
    //-Combinations--------------------------------
    
    @Test
    public void testSimpleCombo1() {
        test(
                "class Foo { int x; void bar() {} void baz() {} }",
                "class Bar { int y; void foo() {}}"
        );
    }
    
    
    @Test
    public void testSimpleCombo2() {
        test(
                "class Foo { int x; void foo() { System.out.println(a + b + c + d); } void bar() { System.out.println(a + b + c + d); } }",
                "class Foo { void foo() { System.out.println(a + b + z + c + d); } void bar() { System.out.println(A + b); } }"
        );
    }
    
    @Test
    public void testSimpleCombo3() {
        test(
                "class Foo { int A; int B; int foo() { return a + b + c; } Object bar() { return null; } }",
                "class Foo { int foo() { return A + b - c; } String bar() { if (true) { return s; } return null; } int A; int B; }"
        );
    }
    
    @Test
    public void testSimpleCombo4() {
        test(
                "class Foo { Object foo() { }}",
                "class Foo { String foo() { }}"
        );
    }
    
    @Test
    public void testMultipleLastInsertions() {
        test(
                "class Foo { void foo() { int a; } }",
                "class Foo { void foo() { int a; int b; } int field; }"
        );
    }
    
    @Test
    public void testParameterInsertionAfter() {
        test(
                "class Foo { void foo(int a) {  } }",
                "class Foo { void foo(int a, int b) { } }"
        );
    }
    
    @Test
    public void testParameterInsertionBefore() {
        test(
                "class Foo { void foo(int a) {  } }",
                "class Foo { void foo(int b, int a) { } }"
        );
    }
    
    @Test
    public void testParameterInsertionBoth() {
        test(
                "class Foo { void foo(int a) {  } }",
                "class Foo { void foo(int b, int a, int c) { } }"
        );
    }
    
    @Test
    public void testMethodInsertionEmpty() {
        test(
                "class Foo {  }",
                "class Foo { void foo() { } }"
        );
    }
    
    @Test
    public void testMultipleMemberInsertionEmpty() {
        test(
                "class Foo {  }",
                "class Foo { int x; void foo() { } }"
        );
    }
    
    @Test
    public void testParameterInsertionEmpty() {
        test(
                "class Foo { void foo() {  } }",
                "class Foo { void foo(int a) { } }"
        );
    }
}
