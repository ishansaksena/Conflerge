package conflerge.unit.tree;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import conflerge.differ.ast.ASTDiffer;
import conflerge.differ.ast.DiffResult;
import conflerge.differ.ast.MergeVisitor;
import conflerge.differ.ast.NodeListUnwrapperVisitor;
import conflerge.differ.ast.NodeListWrapperVisitor;

/**
 * 
 *
 */
public class TestInserts {

    public void eval(String str1, String str2) {     
        Node n1  = JavaParser.parse(str1);
        Node n2 = JavaParser.parse(str2);   

        n1.accept(new NodeListWrapperVisitor(), "A"); 
        n2.accept(new NodeListWrapperVisitor(), "B");
        
        DiffResult res1 = new ASTDiffer(n1, n2).diff();
        DiffResult res2 = new ASTDiffer(n1, n1).diff();
        
        n1.accept(new MergeVisitor(), new Pair<DiffResult, DiffResult>(res1, res2));   
        n1.accept(new NodeListUnwrapperVisitor(), null); 
        
        assertEquals(JavaParser.parse(str2), n1);
    }
    
    public void test(String str1, String str2) {
        eval(str1, str2);
        eval(str2, str1);
    }
    
    //-Field-Declarations------------------------------------

    @Test
    public void testEmptyField() {
        test("class Foo { }",
             "class Foo { Foo f; }");
    }
    
    @Test
    public void testInsertBeforeField() {
        test("class Foo { Foo f; }",
             "class Foo { Bar b; Foo f; }");
    }
    
    @Test
    public void testInsertAfterField() {
        test("class Foo { Foo f; }",
             "class Foo { Foo f; Bar b; }");
    }
    
    @Test
    public void testInsertInterleaveField() {
        test("class Foo { Foo f; Bar b; }",
             "class Foo { int x; Foo f; int y; Bar b; int z; }");
    }
    
    //-Method-Declarations-----------------------------------

    @Test
    public void testEmptyMethod() {
        test("class Foo { }",
                
             "class Foo { "
             + "int foo() { return 0; } }");
    }
    
    @Test
    public void testInsertBeforeMethod() {
        test("class Foo { "
                + "int foo() { return 0; } }",
                
             "class Foo { "
             + "int bar() { return 0; } "
             + "int foo() { return 0; } }");
    }
    
    @Test
    public void testInsertAfterMethod() {
        test("class Foo { "
                + "int foo() { return 0; } }",
                
             "class Foo { "
             + "int foo() { return 0; } "
             + "int bar() { return 0; } }");
    }
    
    @Test
    public void testInsertInterleaveMethods() {
        test("class Foo { "
                + "int foo() { return 0; } "
                + "int bar() { return 0; } }",
                
             "class Foo { int x() { return 0; } "
             + "int foo() { return 0; } "
             + "int y() { return 0; } "
             + "int bar() { return 0; }  "
             + "int z() { return 0; } }");
    }
    
    //-Constructor-Call-Params---------------------------------
    
    @Test
    public void testEmptyConstructorCall() {
        test("class Foo { Foo f = new Foo();  }",
             "class Foo { Foo f = new Foo(a); }");
    }
    
    @Test
    public void testInsertBeforeConstructorCall() {
        test("class Foo { Foo f = new Foo(b);  }",
             "class Foo { Foo f = new Foo(a, b); }");
    }
    
    @Test
    public void testInsertAfterConstructorCall() {
        test("class Foo { Foo f = new Foo(a);  }",
             "class Foo { Foo f = new Foo(a, b); }");
    }
    
    @Test
    public void testInsertInterleaveConstructorCall() {
        test("class Foo { Foo f = new Foo(a, b);  }",
             "class Foo { Foo f = new Foo(x, a, y, b, z); }");
    }
    
    //-Method-Call-Params---------------------------------------
    
    @Test
    public void testEmptyMethodCall() {
        test("class Foo { Foo f = foo();  }",
             "class Foo { Foo f = foo(a); }");
    }
    
    @Test
    public void testInsertBeforeMethodCall() {
        test("class Foo { Foo f = foo(b);  }",
             "class Foo { Foo f = foo(a, b); }");
    }
    
    @Test
    public void testInsertAfterMethodCall() {
        test("class Foo { Foo f = foo(a);  }",
             "class Foo { Foo f = foo(a, b); }");
    }
    
    @Test
    public void testInsertInterleaveMethodCall() {
        test("class Foo { Foo f = foo(a, b);  }",
             "class Foo { Foo f = foo(x, a, y, b, z); }");
    }
    
    //-Constructor-Definition-Params------------------------------
    
    @Test
    public void testEmptyConstructorDef() {
        test("class Foo { public Foo() { } }",
             "class Foo { public Foo(int a) { } }");
    }
    
    
    @Test
    public void testBeforeConstructorDef() {
        test("class Foo { public Foo(int a) { } }",
             "class Foo { public Foo(int b, int a) { } }");
    }
    
    @Test
    public void testAfterConstructorDef() {
        test("class Foo { public Foo(int a) { } }",
             "class Foo { public Foo(int a, int b) { } }");
    }
    
    @Test
    public void testInterleaveConstructorDef() {
        test("class Foo { public Foo(int a, int b) { } }",
             "class Foo { public Foo(int x, int a, int y, int b, int z) { } }");
    }
    
    //-Method-Definition-Params--------------------------------------
    
    @Test
    public void testEmptyMethodDef() {
        test("class Foo { public void foo() { } }",
             "class Foo { public void foo(int a) { } }");
    }
    
    
    @Test
    public void testBeforeMethodDef() {
        test("class Foo { public void foo(int a) { } }",
             "class Foo { public void foo(int b, int a) { } }");
    }
    
    @Test
    public void testAfterMethodDef() {
        test("class Foo { public void foo(int a) { } }",
             "class Foo { public void foo(int a, int b) { } }");
    }
    
    @Test
    public void testInterleaveMethodDef() {
        test("class Foo { public void foo(int a, int b) { } }",
             "class Foo { public void foo(int x, int a, int y, int b, int z) { } }");
    }
    
    //-Method-Body-Statements------------------------------------
    
    @Test
    public void testEmptyMethodBody() {
        test("class Foo { public void foo() {} }",
                
             "class Foo { "
             +   "public void foo() {"
             +   "print(a);"
             + "} }"
             );
    }
    
    
    @Test
    public void testBeforeMethodBody() {
        test("class Foo { "
                + "public void foo() {"
                + "print(a);"
                + "} }",
                
             "class Foo { "
             + "public void foo() {"
             +   "print(b);"
             +   "print(a);"
             + "} }"
             );
    }
    
    @Test
    public void testAfterMethodBody() {
        test("class Foo { "
                + "public void foo() {"
                + "print(a);"
                + "} }",
                
             "class Foo { "
             + "public void foo() {"
             +   "print(a);"
             +   "print(b);"
             + "} }"
             );
    }
    
    @Test
    public void testInterleaveMethodBody() {
        test("class Foo { "
                + "public void foo() {"
                +   "print(a);"
                +   "print(b);"
                + "} }",
                
             "class Foo { "
             + "public void foo() {"
             +   "print(x);"
             +   "print(a);"
             +   "print(y);"
             +   "print(b);"
             +   "print(z);"
             + "} }"
             );
    }

    //-Array-Level-Definitions-------------------------------------
    
    @Test
    public void testAddArrayLevel() {
        test("class Foo { int[][] i = new int[1]; }",
             "class Foo { int[][] i = new int[1][x]; }");
        
        test("class Foo { int[][] i = new int[1]; }",
                "class Foo { int[][] i = new int[x][1]; }");
        
        test("class Foo { int[][] i = new int[1][2]; }",
                "class Foo { int[][] i = new int[x][1][y][2][z]; }");
    }
    
    @Test
    public void testArrayInitializerLevel() {
        test("class Foo { int[] i = new int[] { }; }",
             "class Foo { int[] i = new int[] { 1 }; }");
        
        test("class Foo { int[] i = new int[] { 1 }; }",
                "class Foo { int[] i = new int[] { 1, 2 }; }");
        
        test("class Foo { int[] i = new int[] { 1 }; }",
                "class Foo { int[] i = new int[] { 2, 1 }; }");
        
        test("class Foo { int[] i = new int[] { 1, 2 }; }",
                "class Foo { int[] i = new int[] { x, 1, y, 2, z }; }");
    }
    
    // TODO: Establish that insertions function in the cases below.
    // For now, I'm convinced that the basic mechanism is correct.
    
    //-Block-Statements--------------------------------------
    
    //-Class-Extends-----------------------------------------
    
    //-Class-Implements--------------------------------------
    
    //-Class-TypeParams--------------------------------------
    
    //-Import-Statements-------------------------------------
    
    //-Compilation-Unit-Types--------------------------------
    
    //-Constructor-Exceptions--------------------------------
    
    //-Constructor-Type-Params-------------------------------
    
    //-Enum-Definition-Params--------------------------------
    
    //-Enum-Definition-Body----------------------------------
    
    //-Enum-Declaration-Entries------------------------------
    
    //-Enum-Declaration-Implements---------------------------
    
    //-Enum-Declaration-Members------------------------------
    
    //-Constructor-Invocation-TypeParams---------------------
    
    //-Constructor-Invocation-Args---------------------------
    
    //-Field-Access-Type-Params------------------------------

    //-Field-Declaration-Variables-List----------------------
    
    //-For-Loop-Initialization-Stmts-------------------------
    
    //-Initializer-Declaration-------------------------------
    
    //-Method-Thrown-Exceptions------------------------------
    
    //-Method-Type-Parameter---------------------------------
    
    //-Object-Creation-Anonymous-Class-Body------------------
    
    //-Intersection-Type-Elems-------------------------------
    
    //-Union-Type-Elems--------------------------------------
    
    //-Switch-Stmt-Entries-----------------------------------
    
    //-Switch-Entry-Stmts------------------------------------
    
    //-Try-Catch-Clauses-------------------------------------
    
    //-Type-Param-Type-Bound---------------------------------
    
    //-Variable-DeclarationExpr-Variables--------------------
    
    //-Lambda-Params-----------------------------------------
    
    //-Method-Reference-Expr-Type-Arguments------------------
    
    
    
}
