package conflerge;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.JavaToken;

import conflerge.token.TokenDiffer;
import conflerge.token.TokenParser;

import static com.github.javaparser.ASTParserConstants.*;

/**
 * A useful tool for differencing files by token. Outputs SUCCESS if the
 * token-based edit distance between two files is 0. Otherwise it output
 * FAILURE if the edit distance > 0 or CRASH if an unexpected exception occurs.
 * 
 * Because Conflerge handles import declarations differently than other source code 
 * components, this tool always ignores them when evaluating files.
 * 
 * To ignore comments, run with -c, like: java DiffTool -c file_expected file_actual
 */
public class DiffTool {
    public static void main(String[] args) throws FileNotFoundException {
        List<String> argsList = Arrays.asList(args);
        try {
            List<JavaToken> t1 = TokenParser.tokenizeFileNoImports(args[args.length - 1]);
            List<JavaToken> t2 = TokenParser.tokenizeFileNoImports(args[args.length - 2]);

            if (argsList.contains("-c")) {
                removeComments(t1);
                removeComments(t2);
            }
            
            List<Edit> edits = TokenDiffer.diff(t1, t2);
            edits.removeIf(e -> e.type == Edit.Type.MATCH);
            System.out.println(edits.size());
            if (edits.size() == 0) {
                System.out.println("SUCCESS");
            } else {
                System.out.println("FAILURE");
            }
            
            for (Edit e : edits) {
                switch (e.type) {
                case INSERT:
                    System.out.println("INSERT: " + t2.get(e.icur).text + " " + t2.get(e.icur).range);
                    break;
                case DELETE:
                    System.out.println("DELETE: " + t1.get(e.ibase).text + " " + t1.get(e.ibase).range);
                    break;
                case REPLACE:
                    System.out.println("REPLACE: " 
                                    + t1.get(e.ibase).text + " " 
                                    + t1.get(e.ibase).range +  " " 
                                    + t2.get(e.icur).text + " " 
                                    + t2.get(e.icur).range);
                    break;
                default:
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("CRASH");
        }
    }

    private static void removeComments(List<JavaToken> t) {
        t.removeIf(token -> token.kind == SINGLE_LINE_COMMENT ||  
                   token.kind == MULTI_LINE_COMMENT ||  
                   token.kind == JAVA_DOC_COMMENT);
    }
}
