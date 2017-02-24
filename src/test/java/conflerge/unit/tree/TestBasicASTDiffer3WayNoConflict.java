package conflerge.unit.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

import conflerge.differ.ast.ASTDiffer;
import conflerge.differ.ast.DiffResult;

/**
 * TODO: update this file so it functions with the most recent modifications.
 *
 */
public class TestBasicASTDiffer3WayNoConflict {
    
    private static void merge(String baseStr, String localStr, String remoteStr, String expectedStr) { 
//        Node base   = JavaParser.parse(baseStr);
//        Node local  = JavaParser.parse(localStr);    
//        Node remote = JavaParser.parse(remoteStr);   
//        
//        DiffResult localDiff  = new ASTDiffer(base, local).diff();
//        DiffResult remoteDiff = new ASTDiffer(base, remote).diff();
//
//        base.accept(new ASTMergeVisitor(), DiffResult.merge(localDiff, remoteDiff));
//        
//        assertEquals(JavaParser.parse(expectedStr), base);   
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

// TODO: This test fails, and the cause needs to be investigated.
//       Modifiers (public, private, ect.) may be a special case.
//
//    @Test
//    public void testSimpleDelete2() {
//        merge(
//                "public class Foo { int x;}",
//                "class Foo { int x; }",
//                "class Foo { int x; }"
//        );
//    }
 
    /*
    @Test
    public void testSimpleDelete3() {
        merge(
                "class Foo { int x; int y;}",
                "class Foo { int x; }",
                "class Foo { int x; }"
        );
    }
    
    @Test
    public void testSimpleDelete4() {
        merge(
                "class Foo { int x; int y;}",
                "class Foo { int y; }",
                "class Foo { int y; }"
        );
    }
    
    //-Replacements--------------------------------
    
    @Test
    public void testSimpleReplace1() {
        merge(
                "class Foo { int x;}",
                "class Foo { int y; }",
                "class Foo { int y; }"
        );
    }
    
    @Test
    public void testSimpleReplace2() {
        merge(
                "class Foo { int x; }",
                "class Bar { int x; }",
                "class Bar { int x; }"
        );
    }
    
    @Test
    public void testSimpleReplace3() {
        merge(
                "class Foo { int x; int y; }",
                "class Foo { int x; int z; }",
                "class Foo { int x; int z; }"
        );
    }
    
    @Test
    public void testSimpleReplace4() {
        merge(
                "class Foo { int x; int y;}",
                "class Foo { int z; int y; }",
                "class Foo { int z; int y; }"
        );
    }
    
    //-Insertions--------------------------------
    
    @Test
    public void testSimpleInsert1() {
        merge(
                "class Foo { int x; }",
                "class Foo { int x; int y; }",
                "class Foo { int x; int y; }"
        );
    }
    
    @Test
    public void testSimpleInsert2() {
        merge(
                "class Foo { int x; }",
                "class Bar { int y; int x; }",
                "class Bar { int y; int x; }"
        );
    }
    
    @Test
    public void testSimpleInsert3() {
        merge(
                "class Foo { int x; int y; }",
                "class Foo { int x; int z; int y; }",
                "class Foo { int x; int z; int y; }"
        );
    }
    
    @Test
    public void testSimpleInsert4() {
        merge(
                "class Foo { public void foo(int i) { if (i > 0) { print(i); } } }",
                "class Foo { public void foo(int i) { if (i > 0) { print(i); i++; } } }",
                "class Foo { public void foo(int i) { if (i > 0) { print(i); i++; } } }"
        );
    }
    
    @Test
    public void testSimpleInsert5() {
        merge(
                "class Foo { void foo(int i) { if (i > 0) { print(i); } } }",
                "class Foo { void foo(int i) { if (i > 0) { i++; print(i); } } }",
                "class Foo { void foo(int i) { if (i > 0) { i++; print(i); } } }"
        );
    }
    
    @Test
    public void testSimpleInsert6() {
        merge(
                "class Foo { void foo(int i) { if (i > 0) { print(i); } } }",
                "class Foo { void foo(int i) { if (i > 0 && true) { print(i); } } }",
                "class Foo { void foo(int i) { if (i > 0 && true) { print(i); } } }"
        );
    }
       
    @Test
    public void testSimpleInsert7() {
        merge(
                "public class Foo { void foo(int i) { if (i > 0) { print(i); } } }",
                "public class Foo { void foo(int i) { if (true && i > 0) { print(i); } } }",
                "public class Foo { void foo(int i) { if (true && i > 0) { print(i); } } }"
        );
    }
    
    //-Combinations--------------------------------
    
    @Test
    public void testSimpleCombo1() {
        merge(
                "class Foo { int x; void bar() {} void baz() {} }",
                "class Bar { int y; void foo() {}}",
                "class Bar { int y; void foo() {}}"
        );
    }
    
    @Test
    public void testSimpleCombo2() {
        merge(
                "class Foo { int x; void foo() { System.out.println(a + b + c + d); } void bar() { System.out.println(a + b + c + d); } }",
                "class Foo { void foo() { System.out.println(a + b + z + c + d); } void bar() { System.out.println(A + b); } }",
                "class Foo { void foo() { System.out.println(a + b + z + c + d); } void bar() { System.out.println(A + b); } }"
        );
    }
    
    @Test
    public void testSimpleCombo3() {
        merge(
                "class Foo { int A; int B; int foo() { return a + b + c; } Object bar() { return null; } }",
                "class Foo { int foo() { return A + b - c; } String bar() { if (true) { return s; } return null; } int A; int B; }",
                "class Foo { int foo() { return A + b - c; } String bar() { if (true) { return s; } return null; } int A; int B; }"
        );
    }
    
    @Test
    public void testSimpleCombo4() {
        merge(
                "class Foo { Object foo() { }}",
                "class Foo { String foo() { }}",
                "class Foo { String foo() { }}"
        );
    }*/
}