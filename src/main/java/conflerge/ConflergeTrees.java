package conflerge;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.github.javaparser.ast.Node;

import conflerge.tree.TreeMerger;

/**
 * Runs Conflerge!
 */
public class ConflergeTrees {
     
    public static final int NUM_ARGS = 4;
    
    /**
     * @param args [BASE, LOCAL, REMOTE, MERGED] files
     */
    public static void main(String[] args) {
        if (args.length != NUM_ARGS) {
            fail("Expected args: BASE LOCAL REMOTE MERGED");
            return;
        }
        
        mergeTrees(args);
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
