package conflerge;

public class Edit {
    
    public enum Type {
        MATCH, REPLACE, INSERT, DELETE
    }
    
    public final int  ibase;
    public final int  icur;
    public final Type type;

    /**
     * @param type The type of this edit
     * @param ibase This edit's location in the base version
     * @param icur This edit's location in the current version
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