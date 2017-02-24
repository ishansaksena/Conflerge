package conflerge.merger;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.JavaToken;

import conflerge.Edit;
import conflerge.differ.TokenDiffer;
import conflerge.parser.TokenParser;

/**
 * Merges Java files by token.
 */
public class TokenMerger {
    
    private List<JavaToken> base;
    private List<JavaToken> local;
    private List<JavaToken> remote;
    
    /**
     * Constructs a new TokenMerger for the given base, local, and remote files.
     * 
     * @param baseFile
     * @param localFile
     * @param remoteFile
     * @throws FileNotFoundException
     */
    public TokenMerger(String baseFile, String localFile, String remoteFile) throws FileNotFoundException {
        this.base = TokenParser.tokenizeFile(baseFile);
        this.local = TokenParser.tokenizeFile(localFile);
        this.remote = TokenParser.tokenizeFile(remoteFile);
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
                return null;
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
}