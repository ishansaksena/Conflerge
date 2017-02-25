package conflerge.merger;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import conflerge.differ.ast.ASTDiffer;
import conflerge.differ.ast.DiffResult;
import conflerge.differ.ast.MergeVisitor;
import conflerge.differ.ast.NodeListUnwrapperVisitor;
import conflerge.differ.ast.NodeListWrapperVisitor;

public class TreeMerger {

    private Node base;
    private Node local;
    private Node remote;
    
    public static boolean conflict;
    
    public TreeMerger(String baseFile, String localFile, String remoteFile) throws FileNotFoundException {
        this.base = JavaParser.parse(new File(baseFile));
        this.local = JavaParser.parse(new File(localFile));
        this.remote = JavaParser.parse(new File(remoteFile));
        
        ((CompilationUnit) base).getImports().clear();
        ((CompilationUnit) local).getImports().clear();
        ((CompilationUnit) remote).getImports().clear();
    }

    public Node merge() {
        
        base.accept(new NodeListWrapperVisitor(), "A"); 
        local.accept(new NodeListWrapperVisitor(), "B");
        remote.accept(new NodeListWrapperVisitor(), "C");
        
        DiffResult localDiff = new ASTDiffer(base, local).diff();
        DiffResult remoteDiff = new ASTDiffer(base, remote).diff();
        
        conflict = false;
        
        base.accept(new MergeVisitor(), new Pair<DiffResult, DiffResult>(localDiff, remoteDiff));   
        
        if (conflict) {
            return null;
        }
        
        base.accept(new NodeListUnwrapperVisitor(), null); 
        
        return base;
    }
    
    public static void reportConflict() {
        conflict = true;
    }

}
