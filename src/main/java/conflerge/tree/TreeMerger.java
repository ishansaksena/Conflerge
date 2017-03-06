package conflerge.tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import conflerge.tree.visitor.MergeVisitor;
import conflerge.tree.visitor.NodeListUnwrapperVisitor;
import conflerge.tree.visitor.NodeListWrapperVisitor;

public class TreeMerger {

    private Node base;
    private Node local;
    private Node remote;
    
    public static boolean conflict;
    private List<ImportDeclaration> imports;
    
    public TreeMerger(String baseFile, String localFile, String remoteFile) throws FileNotFoundException {
        this.base = JavaParser.parse(new File(baseFile));
        this.local = JavaParser.parse(new File(localFile));
        this.remote = JavaParser.parse(new File(remoteFile));
        
        this.imports = mergeImports((CompilationUnit) local, (CompilationUnit) remote);
        
        removeImports((CompilationUnit) base);
        removeImports((CompilationUnit) local);
        removeImports((CompilationUnit) remote);
    }

    public Node merge() {      
        base.accept(new NodeListWrapperVisitor(), "A"); 
        local.accept(new NodeListWrapperVisitor(), "B");
        remote.accept(new NodeListWrapperVisitor(), "C");
        
        DiffResult localDiff = new TreeDiffer(base, local).diff();
        DiffResult remoteDiff = new TreeDiffer(base, remote).diff();
        
        conflict = false;
        
        Pair<DiffResult, DiffResult> diffs = new Pair<>(localDiff, remoteDiff);
        base.accept(new MergeVisitor(), diffs);   
        
        if (conflict) {
            return null;
        }
        
        base.accept(new NodeListUnwrapperVisitor(), null); 
        
        addImports((CompilationUnit) base, imports);
        
        return base;
    }
    
    public static void reportConflict() {
        conflict = true;
    }
    
    public static List<ImportDeclaration> mergeImports(CompilationUnit local, CompilationUnit remote) {
        List<ImportDeclaration> imports = new ArrayList<>(local.getImports());
        for (ImportDeclaration imprt : remote.getImports()) {
            if (!imports.contains(imprt)) {
                imports.add(imprt);
            }
        }
        Collections.sort(imports, new Comparator<ImportDeclaration>() {
            @Override
            public int compare(ImportDeclaration o1, ImportDeclaration o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return imports;
    }
    
    public static void addImports(CompilationUnit cu, List<ImportDeclaration> imports) {
        for (ImportDeclaration imprt : imports) {
            ((CompilationUnit) cu).addImport(imprt);
        } 
    }
    
    public static void removeImports(CompilationUnit cu) {
        cu.getImports().clear();
    }

}