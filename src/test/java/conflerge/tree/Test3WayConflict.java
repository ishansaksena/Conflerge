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

public class Test3WayConflict {
    
    /**
     * Perform a merge operation for baseStr, localStr, and remoteStr.
     * Asserts that the expected conflict status was correct.
     */
    private static void merge(String baseStr, String localStr, String remoteStr, boolean conflict) { 
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
        
        assertEquals(ConflergeUtil.conflict, conflict);
        
    }
    
    //-Overlapping-Deletions------------------------------------
    
    @Test
    public void testOverlappingDeletions1() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { void foo() {  } }",
                "class Foo { }",
                true
        );
    }
    
    @Test
    public void testOverlappingDeletions2() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { }",
                "class Foo { void foo() {  } }",
                true
        );
    }
    
    @Test
    public void testOverlappingReplacements1() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { void foo() { int j; } }",
                "class Foo { void foo() { int k; } }",
                true
        );
    }
       
    @Test
    public void testOverlappingReplacements2() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { void foo() { int j;  } }",
                "class Foo { void foo() { print(hello); } }",
                true
        );
    }
      
    @Test
    public void testOverlappingDeleteReplace1() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { void foo() { int j; } }",
                "class Foo { }",
                true
        );
    }
       
    @Test
    public void testOverlappingDeleteReplace2() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { }",
                "class Foo { void foo() { int j; } }",
                true
        );
    }
    
    @Test
    public void testOverlappingDeleteInsertUnder1() {
        merge(
                "class Foo { void foo() { }}",
                "class Foo { void foo(int a) { }}",
                "class Foo { }",
                true
        );
    }
    
    @Test
    public void testOverlappingDeleteInsertUnder2() {
        merge(
                "class Foo { void foo() { }}",
                "class Foo { }",
                "class Foo { void foo(int a) { }}",
                true
        );
    }
    
    @Test
    public void testOverlappingModifiers() {
        merge(
                "class Foo { void foo() { }}",
                "public class Foo { void foo() { }}",
                "private class Foo { void foo() { }}",
                true
        );
    }
}