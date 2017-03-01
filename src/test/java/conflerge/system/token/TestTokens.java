package conflerge.system.token;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
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
import conflerge.parser.TokenParser;

/**
 * Some straightforward system tests for Conflerge's token merging.
 *
 */
public class TestTokens {

    public static final String BASE = "BASE";
    public static final String LOCAL = "LOCAL";
    public static final String REMOTE = "REMOTE";
    public static final String MERGED = "MERGED";
    
    @After 
    public void deleteFiles() {
        new File(BASE).delete();
        new File(LOCAL).delete();
        new File(REMOTE).delete();
        new File(MERGED).delete();
    }
    
    public void checkMergeSucceeds(String base, String local, String remote, String expected) {
        runConflergeMain(base, local, remote);
        checkFileContents(MERGED, expected);
    }
    
    public void checkMergeFails(String base, String local, String remote) {
        runConflergeMain(base, local, remote);
        File f = new File(MERGED);
        assertFalse(f.exists());
    }
    
    private void runConflergeMain(String base, String local, String remote) {
        writeFile(BASE, base);
        writeFile(LOCAL, local);
        writeFile(REMOTE, remote);
        ConflergeTokens.main(new String[] { BASE, LOCAL, REMOTE, MERGED });
    }
    
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
    public void testTrivialCommentMerge() {
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

}
