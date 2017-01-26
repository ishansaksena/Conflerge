package temp;

public class Edit {
	public enum Type { 
		MATCH, 
		REPLACE,
		INSERT, 
		DELETE
	}
	
	public final Type type;
	public final int n1;
	public final int n2;
	
	public Edit(Type type, int n1, int n2) {
		this.type = type;
		this.n1 = n1;
		this.n2 = n2;
	}
	
	@Override
	public String toString() {
		return "(" + type + ": " + n1 + ", " + n2 + ")";
	}
}