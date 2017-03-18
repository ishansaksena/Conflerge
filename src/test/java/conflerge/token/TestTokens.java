package conflerge.token;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.github.javaparser.JavaToken;

import conflerge.ConflergeTokens;
import conflerge.ConflergeUtil;

/**
 * System tests for Conflerge's token merging.
 */
public class TestTokens {

    public static final String BASE = "BASE";
    public static final String LOCAL = "LOCAL";
    public static final String REMOTE = "REMOTE";
    public static final String MERGED = "MERGED";
    
    /**
     * Removes files created by testing.
     */
    @After 
    public void deleteFiles() {
        new File(BASE).delete();
        new File(LOCAL).delete();
        new File(REMOTE).delete();
        new File(MERGED).delete();
    }
    
    /**
     * Merge base, local, and remote. Fails if a conflict occurs or if the result
     * does not match expected.
     */
    public void checkMergeSucceeds(String base, String local, String remote, String expected) {
        runConflergeMain(base, local, remote);
        checkFileContents(MERGED, expected);
    }
    
    /**
     * Attempts to merge base, local, and remote. Fails if the merge succeeds.
     */
    public void checkMergeFails(String base, String local, String remote) {
        runConflergeMain(base, local, remote);
        File f = new File(MERGED);
        assertFalse(f.exists());
    }
    
    /**
     * Executes Conflerge on the given base, local, and remote file contents.
     */
    private void runConflergeMain(String base, String local, String remote) {
        writeFile(BASE, base);
        writeFile(LOCAL, local);
        writeFile(REMOTE, remote);
        ConflergeUtil.commentConflict = false;
        ConflergeUtil.conflict = false;
        ConflergeTokens.main(new String[] { BASE, LOCAL, REMOTE, MERGED });
    }
    
    /**
     * Verifies that the file filename's tokens are identical to the tokens
     * from expected.
     */
    private void checkFileContents(String filename, String expected) {
        try {
            List<JavaToken> mergedTokens = TokenParser.tokenizeFile("MERGED");
            List<JavaToken> expectedTokens = TokenParser.tokenizeString(expected);     
            if (mergedTokens.size() != expectedTokens.size()) {
                fail();
            }
            for (int i = 0; i < mergedTokens.size(); i++) {
                assertTrue(mergedTokens.get(i).text.equals(expectedTokens.get(i).text));
            }
        } catch (FileNotFoundException e) {
            fail("MERGED file not found");
        }
    }

    /**
     * Write file filename with the given contents.
     */
    private void writeFile(String filename, String contents) {
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println(contents);
            writer.close();
        } catch (IOException e) {
            fail("Unexpected error writing to file");
        }
    }
    
    @Test
    public void testBasicOperation() {
        String b = "class Foo { }";
        String l = "class Foo { }";
        String r = "class Foo { }";
        checkMergeSucceeds(b, l, r, "class Foo {  }");
    }
    
    @Test
    public void testCommentMerge() {
        String b = "class Foo { }";
        String l = "class Foo {/* comment */  }";
        String r = "class Foo { }";
        checkMergeSucceeds(b, l, r, "class Foo { /* comment */  }");
    }
    
    @Test
    public void testTrivialMerge() {
        String b = "class Foo { }";
        String l = "class Foo { }";
        String r = "class Bar { }";
        checkMergeSucceeds(b, l, r, "class Bar { }");
    }
    
    @Test
    public void testMergeFromProposal() {
        String b = "class Foo { void greet() { System.out.println(\"hello\"); } }";
        String l = "class Foo { void greet() { System.out.println(\"hi\"); } }";
        String r = "class Foo { void greet() { printer.println(\"hello\"); } }";
        checkMergeSucceeds(b, l, r, "class Foo { void greet() { printer.println(\"hi\"); } }");
    }
    
    @Test
    public void testMergeFirstToken() {
        String b = "class Foo { }";
        String l = "public class Foo { }";
        String r = "class Bar { }";
        checkMergeSucceeds(b, l, r, "public class Bar { }");
    }
    
    @Test
    public void testTrivialMergeFails() {
        String b = "class Foo { }";
        String l = "class Baz { }";
        String r = "class Bar { }";
        checkMergeFails(b, l, r);
    }
    
    @Test
    public void testCommentConflict() {
        String b = "class Foo { int x; }";
        String l = "class Foo { /* commentA */ int x;  }";
        String r = "class Foo { /* commentB */ int x; }";
        checkMergeSucceeds(b, l, r, "class Foo { /* >>>LOCAL:  commentA \n<<< REMOTE:  commentB */ int x; }");
    }

}
