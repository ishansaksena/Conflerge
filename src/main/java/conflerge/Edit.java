package conflerge;

/**
 * Represents an edit in the token-based merging strategy.
 */
public class Edit {
    
    /**
     * The edit operations. Type.MATCH is not actually an
     * edit, but it's useful in practice.
     */
    public enum Type {
        MATCH, REPLACE, INSERT, DELETE
    }
    
    /**
     * This edit's index in the base token sequence.
     */
    public final int ibase;
    
    /**
     * This edit's index in the current (either local or remote)
     * token sequence.
     */
    public final int icur;
    
    /**
     * This edit's type.
     */
    public final Type type;

    /**
     * @param type
     * @param ibase
     * @param icur
     */
    public Edit(Type type, int ibase, int icur) {
        this.type = type;
        this.ibase = ibase;
        this.icur = icur;
    }

    @Override
    public String toString() {
        return "(" + type + ": " + ibase + ", " + icur + ")";
    }
}