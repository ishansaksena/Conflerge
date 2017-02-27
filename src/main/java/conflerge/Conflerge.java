package conflerge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;

import conflerge.merger.TokenMerger;
import conflerge.merger.TreeMerger;
import conflerge.parser.TokenParser;

/**
 * Runs Conflerge!
 */
public class Conflerge {
    
    public static final boolean MERGE_BY_TREE = false;
    
    public static final int MIN_ARGS = 4;
    
    /**
     * @param args [BASE, LOCAL, REMOTE, MERGED] files
     */
    public static void main(String[] args) {
        if (args.length < MIN_ARGS) {
            fail("Expected args: BASE LOCAL REMOTE MERGED");
            return;
        }
        
        if (MERGE_BY_TREE) {
            mergeTrees(args);
        } else {
            mergeTokens(args);
        }
    }
    
    /**
     * Attempts to merge LOCAL and REMOTE. Writes result to MERGED on success.
     * @param args
     */
    private static void mergeTrees(String[] args) {
        try {
            TreeMerger merger = new TreeMerger(args[0], args[1], args[2]);
            Node mergedTree = merger.merge();         
            if (mergedTree == null) {
                fail("Conflict encountered");
                return;
            }
            writeMergedFile(mergedTree.toString(), args[3]);
        } catch (FileNotFoundException e) {
            fail("Files not found");
        } catch (Exception e) {
            fail("Unexpected failure");
            e.printStackTrace();
        }
    }
    
    /**
     * Attempts to merge LOCAL and REMOTE. Writes result to MERGED on success.
     * @param args
     */
    private static void mergeTokens(String[] args) {
        try {
            TokenMerger merger = new TokenMerger(args[0], args[1], args[2]);
            List<JavaToken> mergedTokens = merger.merge();         
            if (mergedTokens == null) {
                fail("Conflict encountered");
                return;
            }
            writeMergedFile(TokenParser.unparseTokens(mergedTokens, merger),  args[3]);
        } catch (FileNotFoundException e) {
            fail("Files not found");
        } catch (Exception e) {
            fail("Unexpected failure");
            e.printStackTrace();
        }
    }
    
    /**
     * Writes mergedFile to mergedFileDest.
     * @param mergedFile
     * @param mergedFileDest
     */
    private static void writeMergedFile(String mergedFile, String mergedFileDest) {
        try {            
            PrintWriter writer = new PrintWriter(mergedFileDest, "UTF-8");
            writer.println(mergedFile);
            writer.close();
            System.out.println("SUCCESS");
            
        } catch (IOException e) {
            fail("Unexpected error writing to file");
        }
    }

    private static void fail(String message) {
        System.out.println("FAILURE");
        System.err.println(message);
    }
}
