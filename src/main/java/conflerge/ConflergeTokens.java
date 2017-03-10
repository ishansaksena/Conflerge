package conflerge;
import static conflerge.ConflergeUtil.NUM_ARGS;
import static conflerge.ConflergeUtil.fail;
import static conflerge.ConflergeUtil.writeFile;

import java.io.FileNotFoundException;
import java.util.List;

import com.github.javaparser.JavaToken;

import conflerge.token.TokenMerger;
import conflerge.token.TokenParser;

/**
 * Runs Conflerge with a token-based merging strategy.
 */
public class ConflergeTokens {
    
    /**
     * @param args [BASE, LOCAL, REMOTE, MERGED] files
     */
    public static void main(String[] args) {
        if (args.length != NUM_ARGS) {
            fail("Expected args: BASE LOCAL REMOTE MERGED");
            return;
        }
        
        try {
            TokenMerger merger = new TokenMerger(args[0], args[1], args[2]);
            List<JavaToken> mergedTokens = merger.merge();         
            if (mergedTokens == null) {
                fail("Conflict encountered");
                return;
            }
            writeFile(TokenParser.unparseTokens(mergedTokens, merger),  args[3]);
        } catch (FileNotFoundException e) {
            fail("Files not found");
        } catch (Exception e) {
            fail("Unexpected failure");
        }
    }
}
