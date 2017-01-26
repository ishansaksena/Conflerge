package temp;

import static com.github.javaparser.Providers.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseStart;
import com.github.javaparser.Token;
import com.github.javaparser.ast.CompilationUnit;

public class TokenAligner {
	public static void main(String[] args) {
		// Align files
		TokenAligner aligner = new TokenAligner("Foo0.java");
		List<JavaToken> mergedTokens = aligner.merge("Foo1.java", "Foo2.java");
		
		// Check for conflict
		if (mergedTokens == null) {
			System.err.println("Conflict encountered");
			System.exit(1);
		}
		
		// Assemble merged file
		StringBuilder sb = new StringBuilder();
		for (JavaToken t : mergedTokens) {
			sb.append(t.getText());
			sb.append(' ');
		}
		
		// Output pretty-printed merged text
		CompilationUnit cu = JavaParser.parse(sb.toString());
		System.out.println(cu.toString());
	}
	
	private List<JavaToken> base;
	
	public TokenAligner(String baseFile) {
		base = tokenizeFile(baseFile);
	}
	
	private List<JavaToken> tokenizeFile(String f) {
		List<JavaToken> res;
		try {
			res = new JavaParser().parse(ParseStart.COMPILATION_UNIT, provider(new File(f))).getTokens().get();
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + f);
			return null;
		}
		removeWhitespace(res);
		res.add(0, new JavaToken(new Token())); // add an empty 'start' token for alignment
		return res;
	}
	
	private List<Edit> align(List<JavaToken> diff) {
		int n = base.size();
		int m = diff.size();
		int[][] op = new int[n][m];
		
		// Base cases: align tokens1[0..n] with [], corresponds to i deletions
		for (int i = 0; i < n; i++) {
			op[i][0] = i;
		}
		
		// Base cases: align tokens2[0..m] with [], corresponds to j insertions
		for (int j = 0; j < m; j++) {
			op[0][j] = j;
		}
		
		// Inv: op[0-i][0.-j] == minimum edit distance for aligning base[0-i] and diff[0-j]
		for (int i = 1; i < n; i++) {
			for (int j = 1; j < m; j++) {
				op[i][j] = multMin(
						op[i - 1][j] + 1,  
						op[i][j - 1] + 1,
						op[i - 1][j - 1] + cost(base.get(i), diff.get(j)));
			}
		}
		
		// Return the smallest sequence of edits that transforms base -> diff
		return recoverEdits(op, diff);
	}
	
	private List<Edit> recoverEdits(int[][] op, List<JavaToken> diff) {
		int i = op.length - 1;
		int j = op[0].length - 1;	
		List<Edit> edits = new ArrayList<Edit>();
		
		while (i > 0 && j > 0) {	
			// Case: delete at 'i'
			if (op[i][j] == op[i - 1][j] + 1) {
				edits.add(new Edit(Edit.Type.DELETE, i, j));
				i--;
				
			// Case: insert at 'j'
			} else if (op[i][j] == op[i][j - 1] + 1) {
				edits.add(new Edit(Edit.Type.INSERT, i, j));
				j--;
			
			// Case: match or replace at 'i', 'j'
			} else {
				Edit.Type type = cost(base.get(i), diff.get(j)) == 0 
						? Edit.Type.MATCH : 
							Edit.Type.REPLACE;
				edits.add(new Edit(type, i, j));
				i--;
				j--;
			}
		}
		return edits;
	}

	public List<JavaToken> merge(String f1, String f2) {
		List<JavaToken> t1 = tokenizeFile(f1);
		List<JavaToken> t2 = tokenizeFile(f2);
		
		Stack<Edit> s1 = new Stack<Edit>();
		Stack<Edit> s2 = new Stack<Edit>();
		s1.addAll(align(t1));
		s2.addAll(align(t2));
		
		List<JavaToken> res = new ArrayList<JavaToken>();
		
		while (!s1.isEmpty() && !s2.isEmpty()) {
			Edit e1 = s1.pop();
			Edit e2 = s2.pop();
						
			// Case: Conflict
			if (e1.type != Edit.Type.MATCH && e2.type != Edit.Type.MATCH) {
				return null;
			}
			
			// Case: MATCH
			else if (e1.type == Edit.Type.MATCH && e2.type == Edit.Type.MATCH) { 
				res.add(base.get(e1.n1));
			} 
			
			// Case: REPLACE
			else if (e1.type == Edit.Type.REPLACE) {
				res.add(t1.get(e1.n2));
			} else if (e2.type == Edit.Type.REPLACE) {
				res.add(t2.get(e2.n2));
			}
			
			// Case: INSERT
			else if (e1.type == Edit.Type.INSERT) {
				res.add(t1.get(e1.n2));
				s2.push(e2);
			} else if (e2.type == Edit.Type.INSERT) {
				res.add(t2.get(e2.n2));
				s1.push(e1);
			} 
		}
		return res;
	}
	
	private void removeWhitespace(List<JavaToken> tokens) {
		Iterator<JavaToken> iter = tokens.iterator();
		while (iter.hasNext()) {
			JavaToken t = iter.next();
			if (t.getText().matches("\\s++")) {
					iter.remove();
			}
		}
	}

	private int cost(JavaToken t1, JavaToken t2) {
		return t1.getText().equals(t2.getText()) ? 0 : 1;
	}

	private static int multMin(int... vals) {
		int min = vals[0];
		for (int i = 1; i < vals.length; i++) {
			min = Math.min(min, vals[i]);
		}
		return min;
	}
}