package conflerge.unit.tree;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import conflerge.differ.TreeDiffer;
import conflerge.differ.TreeDiffer.Node;

public class TestTreeDiffer {
    
    Node LARGE_TREE = 
            new Node("a0", 
                    new Node("a1",
                            new Node("a2"),
                            new Node("b2")),
                    new Node("b1",
                            new Node("a3"), 
                            new Node("b3")),
                    new Node("c1",
                            new Node("a4"),
                            new Node("b4")),
                    new Node("d1",
                            new Node("a5"), 
                            new Node("b5")));

    private void assertExpected(Node t1, Node t2, String[] expected) {
        TreeDiffer differ = new TreeDiffer(t1, t2);
        Set<String> actual = differ.diff();

        assertEquals(actual.size(), expected.length);
        for (String e : expected) {
            assertTrue(actual.contains(e));
        }
    }
    
    @Test
    public void testMatch() {
        Node t1 = new Node("A");
        String[] expected = { "MATCH A A" };
        assertExpected(t1, t1, expected);
    }
    
    @Test
    public void testReplace() {
        Node t1 = new Node("A");
        Node t2 = new Node("B");
        String[] expected = { "REPLACE A B" };
        assertExpected(t1, t2, expected);
    }
    
    @Test
    public void testDeleteChild() {
        Node t1 = new Node("A", new Node("B"));
        Node t2 = new Node("A");
        String[] expected = { "DELETE B", "MATCH A A" };
        assertExpected(t1, t2, expected);
    }
    
    @Test
    public void testDeleteParent() {
        Node t1 = new Node("A", new Node("B"));
        Node t2 = new Node("B");
        String[] expected = { "DELETE A", "MATCH B B" };
        assertExpected(t1, t2, expected);
    }
    
    @Test
    public void testInsertChild() {
        Node t1 = new Node("A");
        Node t2 = new Node("A", new Node("B"));
        String[] expected = { "INSERT B", "MATCH A A" };
        assertExpected(t1, t2, expected);
    }
    
    @Test
    public void testInsertParent() {
        Node t1 = new Node("B");
        Node t2 = new Node("A", new Node("B"));
        String[] expected = { "INSERT A", "MATCH B B" };
        assertExpected(t1, t2, expected);
    }
    
    @Test
    public void testMatchLargeTree() {
        String[] expected = { "MATCH a0 a0", 
                                "MATCH a1 a1", 
                                    "MATCH a2 a2", "MATCH b2 b2",
                                "MATCH b1 b1",
                                    "MATCH a3 a3", "MATCH b3 b3",
                                "MATCH c1 c1",
                                    "MATCH a4 a4", "MATCH b4 b4",
                                "MATCH d1 d1",
                                    "MATCH a5 a5",  "MATCH b5 b5",};
        assertExpected(LARGE_TREE, LARGE_TREE, expected);
    }
    
    @Test
    public void testDeleteLeavesLargeTree() {
        Node t = 
                new Node("a0", 
                        new Node("a1",
                                new Node("b2")),
                        new Node("b1",
                                new Node("a3")),
                        new Node("c1",
                                new Node("b4")),
                        new Node("d1",
                                new Node("a5")));
        
        String[] expected = { "MATCH a0 a0", 
                                "MATCH a1 a1", 
                                    "DELETE a2", "MATCH b2 b2",
                                "MATCH b1 b1",
                                    "MATCH a3 a3", "DELETE b3",
                                "MATCH c1 c1",
                                    "DELETE a4", "MATCH b4 b4",
                                "MATCH d1 d1",
                                    "MATCH a5 a5",  "DELETE b5",};
        assertExpected(LARGE_TREE, t, expected);
    }
    
    @Test
    public void testDeleteNonLeavesLargeTree() {
        Node t = new Node("a0", 
                    new Node("a1",
                        new Node("a2"),
                        new Node("b2")),       
                    new Node("a3"), 
                    new Node("b3"),
                    new Node("c1",
                        new Node("a4"),
                        new Node("b4")),
                    new Node("a5"), 
                    new Node("b5"));
        String[] expected = { "MATCH a0 a0", 
                                "MATCH a1 a1", 
                                    "MATCH a2 a2", "MATCH b2 b2",
                                "DELETE b1",
                                    "MATCH a3 a3", "MATCH b3 b3",
                                "MATCH c1 c1",
                                    "MATCH a4 a4", "MATCH b4 b4",
                                "DELETE d1",
                                    "MATCH a5 a5",  "MATCH b5 b5",};
        assertExpected(LARGE_TREE, t, expected);
    }
    
    @Test
    public void testInsertLeavesLargeTree() {
        Node t = new Node("a0", 
                        new Node("a1",
                                new Node("a2"),
                                new Node("b2"),
                                new Node("c2")),
                        new Node("b1",
                                new Node("a3"), 
                                new Node("b3"), new Node("c3")),
                        new Node("c1",
                                new Node("c4"),
                                new Node("a4"),
                                new Node("b4")),
                        new Node("d1",
                                new Node("a5"),
                                new Node("c5"),
                                new Node("b5")));

        String[] expected = { "MATCH a0 a0", 
                                    "MATCH a1 a1", 
                                        "MATCH a2 a2", "MATCH b2 b2",
                                    "MATCH b1 b1",
                                        "MATCH a3 a3", "MATCH b3 b3",
                                    "MATCH c1 c1",
                                        "MATCH a4 a4", "MATCH b4 b4",
                                    "MATCH d1 d1",
                                        "MATCH a5 a5",  "MATCH b5 b5",
                                    "INSERT c2", "INSERT c3", 
                                    "INSERT c4", "INSERT c5"};
        assertExpected(LARGE_TREE, t, expected);
    }
    
    @Test
    public void testInsertNonLeavesLargeTree() {
        Node t = 
                new Node("a0",
                        new Node("A1",
                            new Node("a1",
                                    new Node("a2"),
                                    new Node("b2")),
                            new Node("b1",
                                    new Node("A3",
                                            new Node("a3")), 
                                    new Node("b3"))),
                        new Node("c1",
                                new Node("C1",
                                    new Node("a4"),
                                    new Node("b4"))),
                        new Node("d1",
                                new Node("a5"),
                                new Node("B5",
                                        new Node("b5"))));
        
        String[] expected = { "MATCH a0 a0", 
                                "MATCH a1 a1", 
                                    "MATCH a2 a2", "MATCH b2 b2",
                                "MATCH b1 b1",
                                    "MATCH a3 a3", "MATCH b3 b3",
                                "MATCH c1 c1",
                                    "MATCH a4 a4", "MATCH b4 b4",
                                "MATCH d1 d1",
                                    "MATCH a5 a5",  "MATCH b5 b5",
                                "INSERT A1", "INSERT A3", 
                                "INSERT C1", "INSERT B5"};
        assertExpected(LARGE_TREE, t, expected);
    }

}
