package conflerge.unit.tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

import conflerge.differ.ast.ASTDiffer;
import conflerge.differ.ast.DiffResult;
import conflerge.differ.ast.NodeListWrapper;

public class TestBasicASTDifferConflictDetection {
    
    private static void merge(String baseStr, String localStr, String remoteStr, boolean canMerge) { 
        Node base   = JavaParser.parse(baseStr);
        Node local  = JavaParser.parse(localStr);    
        Node remote = JavaParser.parse(remoteStr);   
        
        NodeListWrapper.wrapAST(base);
        NodeListWrapper.wrapAST(local); 
        NodeListWrapper.wrapAST(remote);
        
        ASTDiffer localDiffer = new ASTDiffer(base, local);
        ASTDiffer remoteDiffer = new ASTDiffer(base, remote);
        
        DiffResult localDiff  = localDiffer.diff();
        DiffResult remoteDiff = remoteDiffer.diff();
        
        assertEquals(canMerge, DiffResult.merge(localDiff, remoteDiff, localDiffer, remoteDiffer));
    }
    
    //-Overlapping-Deletions------------------------------------
    
    @Test
    public void testOverlappingDeletions1() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { void foo() {  } }",
                "class Foo { }",
                false
        );
    }
    
    @Test
    public void testOverlappingDeletions2() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { }",
                "class Foo { void foo() {  } }",
                false
        );
    }
    
    @Test
    public void testOverlappingReplacements1() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { void foo() { int j; } }",
                "class Foo { void foo() { int k; } }",
                false
        );
    }
       
    @Test
    public void testOverlappingReplacements2() {
        merge(
                "class Foo { void foo() { int i;  } } ",
                "class Foo { void foo() { int j;  } }",
                "class Foo { void foo() { print(hello); } }",
                false
        );
    }
    
//  TODO: implement this    
//    @Test
//    public void testOverlappingDeleteReplace1() {
//        merge(
//                "class Foo { void foo() { int i;  } } ",
//                "class Foo { void foo() { int j; } }",
//                "class Foo { }",
//                false
//        );
//    }
//       
//    @Test
//    public void testOverlappingDeleteReplace2() {
//        merge(
//                "class Foo { void foo() { int i;  } } ",
//                "class Foo { }",
//                "class Foo { void foo() { int j; } }",
//                false
//        );
//    }
}