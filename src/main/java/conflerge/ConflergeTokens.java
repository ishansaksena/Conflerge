package conflerge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.github.javaparser.JavaToken;

import conflerge.token.TokenMerger;
import conflerge.token.TokenParser;

/**
 * Runs Conflerge with a token-based merging strategy.
 */
public class ConflergeTokens {
    
    /**
     * The number of expected command-line arguments.
     */
    public static final int NUM_ARGS = 4;
    
    /**
     * @param args [BASE, LOCAL, REMOTE, MERGED] files
     */
    public static void main(String[] args) {
        if (args.length != NUM_ARGS) {
            fail("Expected args: BASE LOCAL REMOTE MERGED");
            return;
        }
        mergeTokens(args);
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

    /**
     * Display a failure message and exit.
     */
    private static void fail(String message) {
        System.out.println("FAILURE");
        System.err.println(message);
    }
}
