package conflerge;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 */
public class ConflergeUtil {
    
    /**
     * The number of expected command-line arguments.
     */
    static final int NUM_ARGS = 4;
    
    /**
     * Writes mergedFile to mergedFileDest.
     * @param mergedFile
     * @param mergedFileDest
     */
     static void writeFile(String fileContents, String fileDest) {
        try {            
            PrintWriter writer = new PrintWriter(fileDest, "UTF-8");
            writer.println(fileContents);
            writer.close();
            System.out.println("SUCCESS");
            
        } catch (IOException e) {
            fail("Unexpected error writing to file");
        }
    }

    /**
     * Display a failure message.
     */
    static void fail(String message) {
        System.out.println("FAILURE");
        System.err.println(message);
    }
}
