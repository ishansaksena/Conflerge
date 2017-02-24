package conflerge.differ;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaToken;

import conflerge.Edit;

/**
 * Provides a static method for 'diffing' lists of tokens: computing the 
 * smallest collection of Edits that transforms one list of into the other.
 */
public class TokenDiffer {

    /**
     * @param base
     * @param modified
     * @return Smallest list of Edits than transforms base into modified
     */
    public static List<Edit> diff(List<JavaToken> base, List<JavaToken> modified) {        
        int n = base.size();
        int m = modified.size();
        int[][] op = new int[n][m];

        System.out.println();
        
        // Base cases: align base[0..n] with [], corresponds to i deletions
        for (int i = 0; i < n; i++) {
            op[i][0] = i;
        }

        // Base cases: align modifed[0..m] with [], corresponds to j insertions
        for (int j = 0; j < m; j++) {
            op[0][j] = j;
        }

        // Invariant: op[0..i)[0..j) == minimum edit distance for aligning base[0..i), modified[0..j)
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
                op[i][j] = multMin(
                        op[i - 1][j] + 1, 
                        op[i][j - 1] + 1,
                        op[i - 1][j - 1] + cost(base.get(i), modified.get(j)));
            }
        }    
        
        // Return the smallest sequence of edits that transforms base -> modified
        return recoverEdits(op, base, modified);
    }

    /**
     * @param op
     * @param base
     * @param modified
     * @return List of edits corresponding to the computations in op.
     */
    private static List<Edit> recoverEdits(int[][] op, List<JavaToken> base, List<JavaToken> modified) {
        int i = op.length - 1;
        int j = op[0].length - 1;
        
        List<Edit> edits = new ArrayList<Edit>();
        
        while (i >= 0 && j >= 0) {
            // Case: delete at 'i'
            if (i >= 1 && op[i][j] == op[i - 1][j] + 1) {
                edits.add(new Edit(Edit.Type.DELETE, i, j));
                i--;

            // Case: insert at 'j'
            } else if (j >= 1 && op[i][j] == op[i][j - 1] + 1) {
                edits.add(new Edit(Edit.Type.INSERT, i, j));
                j--;

            // Case: match or replace at 'i', 'j'
            } else {
                Edit.Type type = cost(base.get(i), modified.get(j)) == 0 ? Edit.Type.MATCH : Edit.Type.REPLACE;
                edits.add(new Edit(type, i, j));
                i--;
                j--;
            }
        }
        return edits;
    }

    /**
     * @param t1
     * @param t2
     * @return The 'Edit Cost' of replacing t1 with t2: if t1 == t2 0, else 1
     */
    private static int cost(JavaToken t1, JavaToken t2) {
        return t1.getText().equals(t2.getText()) ? 0 : 1;
    }

    /**
     * @param vals
     * @return Minimum value in vals.
     */
    private static int multMin(int... vals) {
        int min = vals[0];
        for (int i = 1; i < vals.length; i++) {
            min = Math.min(min, vals[i]);
        }
        return min;
    }
}