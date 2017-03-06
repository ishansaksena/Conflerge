package conflerge.tree;

import static org.junit.Assert.assertEquals;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import conflerge.tree.visitor.MergeVisitor;
import conflerge.tree.visitor.NodeListUnwrapperVisitor;
import conflerge.tree.visitor.NodeListWrapperVisitor;

public class TestASTUtils {
    
    public static void eval(String str1, String str2) {     
        Node n1  = JavaParser.parse(str1);
        Node n2 = JavaParser.parse(str2);   

        n1.accept(new NodeListWrapperVisitor(), "A"); 
        n2.accept(new NodeListWrapperVisitor(), "B");
    
        DiffResult res1 = new TreeDiffer(n1, n2).diff();
        DiffResult res2 = new TreeDiffer(n1, n1).diff();
    
        n1.accept(new MergeVisitor(), new Pair<DiffResult, DiffResult>(res1, res2));   
        n1.accept(new NodeListUnwrapperVisitor(), null); 
    
        assertEquals(JavaParser.parse(str2), n1);
    }

    public static void test(String str1, String str2) {
        eval(str1, str2);
        eval(str2, str1);
    }
}
