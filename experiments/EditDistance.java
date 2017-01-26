package temp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditDistance {
	
	public static int CC = 1; // Insert cost
	public static int CD = 1; // Delete cost
	public static int CI = 1; // Change cost
	
	public static void main(String[] args) {
		
		// Trees used in the Zhang-Shasa paper; current imp. displays the same
		// intermediate and final results as the paper for this test case
		
		Node t1 = new Node("f",
						new Node("d",
								new Node("a"),
								new Node("c",
										new Node("b"))),
						new Node("e"));
									
		Node t2 = new Node("f",
						new Node("c",
								new Node("d",
									new Node("a"),
									new Node("b"))),
						new Node("e"));
		
		zhangShasa(t1, t2);		
	}
	
	public static void zhangShasa(Node t1, Node t2) {
		/* Precomputations */ 
		
		// -- T1, T2: list of nodes in tree1, tree2 ordered by postorder traversal, 1-indexed
		List<Node> T1 = getOrderedNodes(t1);
		List<Node> T2 = getOrderedNodes(t2);
		
		// -- l1, l2: l1(i) = index in T1 of LEFTMOST descendent of T1[i]
		List<Integer> l1 = getLValues(T1);
		List<Integer> l2 = getLValues(T2);
		
		// -- KR1, KR2: Set of nodes (indexes into T1/T2) that either (a) are the root or (b) have a left sibling
		List<Integer> KR1 = getKRs(t1, T1);
		List<Integer> KR2 = getKRs(t2, T2);
		
		int[][] forestDist = null;
		int[][] treeDist = new int[T1.size()][T2.size()];
		
		/* Main loop */  
		
		for (int i = 1; i < KR1.size(); i++) {
			for (int j = 1; j < KR2.size(); j++) {
				forestDist = computeTreeDist(KR1.get(i), KR2.get(j), T1, T2, l1, l2, treeDist);
			}
		}
		
		System.out.println("Edit Distance: " + treeDist[T1.size() - 1][T2.size() - 1]);
		
		/* Recover edit mapping */
		
		List<Edit> edits = recoverEdits(forestDist, T1, T2);
		
		System.out.println(edits);
		System.out.println();
	}
	
    private static int[][] computeTreeDist(int i, int j, 
    									List<Node> T1, List<Node> T2, 
    									List<Integer> l1, List<Integer> l2,  
    									int[][] treeDist) {
    	// Dimensions of this subproblem
    	int ilen = i - l1.get(i) + 2;
    	int jlen = j - l2.get(j) + 2;
    	
    	// Offset to align these valus with the whole tree
        int ioff = l1.get(i) - 1;
        int joff = l2.get(j) - 1;
        
    	// Base case: empty tree -> empty tree
    	int[][] forestDist = new int[ilen][jlen]; 
    	forestDist[0][0] = 0;
    	
    	// Cost of entirely deleting T1's current subforest
    	for (int i1 = 1; i1 < ilen; i1++) {
			forestDist[i1][0] = forestDist[i1-1][0] + CD;
		}
    	
    	// Cost of entirely inserting T2's current subforest
		for (int j1 = 1; j1 < jlen; j1++) {
			forestDist[0][j1] = forestDist[0][j1-1] + CI;
		}
		
		for (int i1 = 1; i1 < ilen; i1++) {	
			for (int j1 = 1; j1 < jlen; j1++) {			
				if (l1.get(i1 + ioff) == l1.get(i) && l2.get(j1 + joff) == l2.get(j)) {	
					treeDist[i1 + ioff][j1  + joff] =
						forestDist[i1][j1] = 
								multMin(
									forestDist[i1 - 1][j1] + CD,
									forestDist[i1][j1 - 1] + CI,
									forestDist[i1 - 1][j1 - 1] 
											+ (T1.get(i1 + ioff).equals(T2.get(j1 + joff)) ? 0 : CC)
								);
				} else {	
					forestDist[i1][j1] = 
							multMin(
								forestDist[i1 - 1][j1] + CD,
								forestDist[i1][j1 - 1] + CI,
								forestDist[i1 - 1][j1 - 1] + treeDist[i1 + ioff][j1 + joff]
							);
				}
			}
		}
		return forestDist;
	}
    
    private static List<Edit> recoverEdits(int[][] forestDist, List<Node> T1, List<Node> T2) {
		int i = forestDist.length - 1;
		int j = forestDist[0].length - 1;
		
		List<Edit> edits = new ArrayList<Edit>();
		while (i > 0 && j > 0) {
			int min = multMin(
						forestDist[i - 1][j] + CD,
						forestDist[i][j - 1] + CI,
						forestDist[i - 1][j - 1] 
								+ (T1.get(i).equals(T2.get(j)) ? 0 : CC)
						);
			if (min == forestDist[i - 1][j] + CD) {
				edits.add(new Edit(Edit.Type.DELETE, i, 0));
				i--;
			} else if (min == forestDist[i][j - 1] + CD) {
				edits.add(new Edit(Edit.Type.INSERT, 0, j));
				j--;
			}	else {
				if (T1.get(i).equals(T2.get(j))) {
					edits.add(new Edit(Edit.Type.MATCH, i, j));
				} else {
					edits.add(new Edit(Edit.Type.REPLACE, i, j));
				}
				i--;
				j--;
			}
		}
		return edits;
    }

	////// Preprocessing/utility methods: in the interest of time, they are not implemented efficiently (yet) //////
	
	private static List<Integer> getKRs(Node root, List<Node> T) {
		List<Node> KRNodes = getKRs(root, new ArrayList<Node>(), true);
		List<Integer> KRs = new ArrayList<>();
		KRs.add(null); // enforce 1-based index
		
		for (Node n : KRNodes) {
			KRs.add(T.indexOf(n));
		}
		return KRs;
	}
	
	private static List<Node> getKRs(Node root, ArrayList<Node> KRNodes, boolean kr) {
		for (int i = 0; i < root.children.size(); i++) {
			getKRs(root.children.get(i), KRNodes, i > 0);
		}
		if (kr) KRNodes.add(root);
		return KRNodes;
	}

	private static List<Integer> getLValues(List<Node> T) {
		List<Integer> l = new ArrayList<>();
		for (Node cur : T) {
			if (cur == null)  {
				l.add(null); 
			} else {
				while (cur.children.size() > 0) {
					cur = cur.children.get(0);
				}
				l.add(T.indexOf(cur));
			}
		}
		return l;
	}
	
	private static List<Node> getOrderedNodes(Node root) {
		List<Node> T = new ArrayList<Node>();
		T.add(null); // enforce 1-based index
		
		return getOrderedNodes(root, T);
	}
	
	private static List<Node> getOrderedNodes(Node root, List<Node> T) {
		for (Node n : root.children) {
			getOrderedNodes(n, T);
		}
		T.add(root);
		return T;
	}

	private static int multMin(int... vals) {
		int min = vals[0];
		for (int i = 1; i < vals.length; i++) {
			min = Math.min(min, vals[i]);
		}
		return min;
	}
}

////Pseudo-syntax trees to test 
//t1 = new Node("foo",
//		new Node("BLOCK",
//			new Node("print",
//				new Node ("hello"))));
//t2 = new Node("foo",
//		new Node("if",
//			new Node("greet"),
//			new Node("BLOCK",
//					new Node("print",
//							new Node ("hello")))));
//zhangShasa(t1, t2);

