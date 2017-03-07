package conflerge.tree;

import static conflerge.tree.TestASTUtils.test;

import org.junit.Test;

/**
 * Two way merges check the correctness of the edit script: they verify that the 
 * TreeDiffer algorithm produces an edit script that can transform tree A into tree B.
 */
public class Test2WayBasic {
    
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
