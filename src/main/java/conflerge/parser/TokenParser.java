package conflerge.parser;

import static com.github.javaparser.Providers.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseStart;
import com.github.javaparser.Provider;
import com.github.javaparser.Token;
import com.github.javaparser.ast.CompilationUnit;

/**
 * Contains static methods for tokenizing Java code. All token lists 
 * are currently returned with an empty "start token" as the first element,
 * which is convenient for alignment algorithms.
 */
public class TokenParser {

    /**
     * @param filename
     * @return List of tokens corresponding to the given file's Java code.
     * @throws FileNotFoundException
     */
    public static List<JavaToken> tokenizeFile(String filename) throws FileNotFoundException {
        return tokenize(provider(new File(filename)));
    }
    
    /**
     * @param code
     * @return List of tokens corresponding to code.
     */
    public static List<JavaToken> tokenizeString(String code) {
        return tokenize(provider(code));
    }
    
    /**
     * @param p
     * @return List of tokens corresponding to the given Provider.
     */
    private static List<JavaToken> tokenize(Provider p) {
        List<JavaToken> tokens = new JavaParser()
                .parse(ParseStart.COMPILATION_UNIT, p)
                .getTokens()
                .get();
        
        tokens = tokens.stream()
                .filter(t -> !t.getText().matches("\\s++"))
                .collect(Collectors.toList());
        
        tokens.add(0, new JavaToken(new Token(0, ""))); 
        return tokens;  
    }

    /**
     * @param tokens
     * @return Pretty-printed String representation of the given tokens.
     */
    public static String unparseTokens(List<JavaToken> tokens) {
        StringBuilder sb = new StringBuilder();
        for (JavaToken t : tokens) {
            sb.append(t.getText());
            sb.append(' ');
        }

        CompilationUnit cu = JavaParser.parse(sb.toString());
        return cu.toString();
    }
}