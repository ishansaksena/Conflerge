package conflerge.token;

import static com.github.javaparser.ASTParserConstants.JAVA_DOC_COMMENT;
import static com.github.javaparser.ASTParserConstants.MULTI_LINE_COMMENT;
import static com.github.javaparser.ASTParserConstants.SINGLE_LINE_COMMENT;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;

import conflerge.Edit;
import conflerge.tree.TreeMerger;

/**
 * Merges Java files by token.
 */
public class TokenMerger {
    
    /**
     * Token sequences of the base, local, and remote files.
     */
    private List<JavaToken> base;
    private List<JavaToken> local;
    private List<JavaToken> remote;
    
    /**
     * The imports that will be included in the resulting merge, if successful.
     * Contains the union of import declarations from local and remote.
     */
    public final List<ImportDeclaration> imports;
    
    /**
     * Constructs a new TokenMerger for the given base, local, and remote files.
     * 
     * @param baseFile
     * @param localFile
     * @param remoteFile
     * @throws FileNotFoundException
     */
    public TokenMerger(String baseFile, String localFile, String remoteFile) throws FileNotFoundException {
        Node baseTree = JavaParser.parse(new File(baseFile));
        Node localTree = JavaParser.parse(new File(localFile));
        Node remoteTree = JavaParser.parse(new File(remoteFile));
        
        this.imports = TreeMerger.mergeImports((CompilationUnit) localTree, (CompilationUnit) remoteTree);
        
        TreeMerger.removeImports((CompilationUnit) baseTree);
        TreeMerger.removeImports((CompilationUnit) localTree);
        TreeMerger.removeImports((CompilationUnit) remoteTree);
    	
    	
    	this.base = TokenParser.tokenizeString(baseTree.toString());
        this.local = TokenParser.tokenizeString(localTree.toString());
        this.remote = TokenParser.tokenizeString(remoteTree.toString());
    }
    
    /**
     * @return null if a merge conflict is encountered, else
     *         a list of tokens representing a successful merge
     *         of local and remote.
     */
    public List<JavaToken> merge() {
        Stack<Edit> localEdits = new Stack<Edit>();
        Stack<Edit> remoteEdits = new Stack<Edit>();
        localEdits.addAll(TokenDiffer.diff(base, local));
        remoteEdits.addAll(TokenDiffer.diff(base, remote));
        
        return merge(localEdits, remoteEdits);
    }
    
    /**
     * @param localEdits
     * @param remoteEdits
     * @return null if a merge conflict is encountered, else
     *         a list of tokens representing a successful merge.
     */
    private List<JavaToken> merge(Stack<Edit> localEdits, Stack<Edit> remoteEdits) {             
        List<JavaToken> res = new ArrayList<JavaToken>();
        while (!localEdits.isEmpty() && !remoteEdits.isEmpty()) {
            Edit e1 = localEdits.pop();
            Edit e2 = remoteEdits.pop();
            
            // Case: Conflict
            if (e1.type != Edit.Type.MATCH && e2.type != Edit.Type.MATCH) {
            	
            	// Temporarily, we want to avoid letting comments cause conflicts.
            	// Otherwise, we won't be able to fairly compare token-based merging
            	// to tree based merging, which doesn't currently allow comments
            	// to conflict.
            	if (isComment(local.get(e1.icur))) {
            		res.add(local.get(e1.icur));
            		remoteEdits.push(e2);
            	} else if (isComment(remote.get(e2.icur))) {
            		res.add(remote.get(e2.icur));
            		localEdits.push(e1);
            	
            	} else {
            		return null;
            	}
            }
            
            // Case: Match
            else if (e1.type == Edit.Type.MATCH && e2.type == Edit.Type.MATCH) { 
                res.add(base.get(e1.ibase));
            } 
            
            // Case: Replacement
            else if (e1.type == Edit.Type.REPLACE) {
                res.add(local.get(e1.icur));
            } else if (e2.type == Edit.Type.REPLACE) {
                res.add(remote.get(e2.icur));
            }
            
            // Case: Insert
            else if (e1.type == Edit.Type.INSERT) {
                res.add(local.get(e1.icur));
                remoteEdits.push(e2);
            } else if (e2.type == Edit.Type.INSERT) {
                res.add(remote.get(e2.icur));
                localEdits.push(e1);
            }
            System.out.println();
        }
        res.remove(0);
        return res;
    }

    /**
     * @param token
     * @return true iff the given token corresponds to a comment.
     */
	private boolean isComment(JavaToken token) {
	    return token.kind == SINGLE_LINE_COMMENT ||  
	           token.kind == MULTI_LINE_COMMENT  ||  
	           token.kind == JAVA_DOC_COMMENT;
	}
}