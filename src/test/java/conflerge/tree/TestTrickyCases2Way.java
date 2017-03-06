package conflerge.tree;

import static conflerge.tree.TestASTUtils.test;

import org.junit.Test;

public class TestTrickyCases2Way {
    
    @Test
    public void test1() {
        test("class Foo { int x = 3 + 1 + 2; }",
             "class Foo { int x = 1 + 2 + 3; }");
    }
    
    @Test
    public void test2() {
        test("class Foo { int x = foo(); }",
             "class Foo { int x = bar(); }");
    }
    
    @Test
    public void test3() {
        test("class Foo { void alert() { System.out.println(message); } }",
             "class Foo { void alert() { System.err.println(message); } }");
    }
    
    @Test
    public void test4() {     
        test("class Foo { void foo(Map<A> m) { } }",
             "class Foo { void foo(Map<B, C> m) { } }");
    }
    
    @Test
    public void test5() {
        test("class Foo { List l; }", "class Foo { List<T> l; }");
        test("class Foo { List<T> l; }", "class Foo { List l; }");
        test("class Foo { List<T> l; }", "class Foo { List<? extends T> l; }");
    }
    
    @Test
    public void testModifiers() {
        test("class Foo { void foo(int a) { } }",
             "public static class Foo { void foo(int a) { } }");
    }
    
    @Test
    public void testMethod() {
        String m1 = "public class Foo {"
                        + "private Map<NodeListWrapper, Map<Integer, List<Node>>> processInserts(Map<NodeListWrapperNode, List<Node>> insertedUnder) {"
                        + " Map<NodeListWrapper, Map<Integer, List<Node>>> nlToIndexToInserts = new IdentityHashMap<>();        "
                        + " "
                        + " for (NodeListWrapperNode nlwn : insertedUnder.keySet()) {"
                        + "     NodeList<? extends Node> nl = nlwn.list.nodeList;"
                        + "     Map<Integer, List<Node>> inserts = new HashMap<>();"
                        + "     Collections.reverse(insertedUnder.get(nlwn));"
                        + "     for (Node insert : insertedUnder.get(nlwn)) { "
                        + "         int i = indexOfObj(nl, insert);"
                        + "         while (i >= 0 && !alignsB.containsKey(nl.get(i))) {"
                        + "             i--;"
                        + "         }"
                        + "         int insertIndex;"
                        + "         if (i >= 0) {"
                        + "             Node match = alignsB.get(nl.get(i));"
                        + "             NodeListWrapperNode matchedParent = (NodeListWrapperNode) parentsA.get(match);"
                        + "             insertIndex = indexOfObj(matchedParent.list.nodeList, match) + 1;"
                        + "         } else {"
                        + "             insertIndex = 0;"
                        + "         }"
                        + "         "
                        + "         if (!inserts.containsKey(insertIndex)) {"
                        + "             inserts.put(insertIndex, new ArrayList<Node>());"
                        + "         }"
                        + "         inserts.get(insertIndex).add(insert);"
                        + "     }"
                        + "     "
                        + "     NodeListWrapperNode match = (NodeListWrapperNode) alignsB.get(nlwn);"
                        + "     if (match != null) {"
                        + "         nlToIndexToInserts.put(match.list, inserts);"
                        + "     }"
                        + "}"
                        + " return nlToIndexToInserts;"
                        + "}"
                        + "}";
        
                String m2 = "public class Fooo {"
                        + "private Map<NodeListWrapper, Map<Number, List<Node>>> process(Map<String> insertedUnder) {"
                        + " Map<NodeListWrapper, Map<Integer, List<Node>>> nlToIndexToInserts = new IdentityHashMap<NodeListWrapper, Map<Integer, List<Node>>>();        "
                        + " "
                        + " for (NodeListWrapperNode nlwn : insertedUnder.keySet()) {"
                        + "     NodeList<Visitable> nl = nlwn.nodeList;"
                        + "     Collections.reverse(insertedUnder.get(nlwn));"
                        + "     Map<Integer, List<Visitable>> inserts = new HashMap<>();"
                        + "     for (int i = 0; i < insertedUnder.size(); i++) { "
                        + "         int j = indexOfObj(nl, insertedUnder.get(i));"
                        + "         while (i >= 0 && !alignsB.containsKey(nl.get(i))) {"
                        + "             i = --i--;"
                        + "         }"
                        + "         int insertIndex;"
                        + "         if (i >= 0) {"
                        + "             Node match = alignsB.getVal(nl.get(i));"
                        + "             NodeListWrapperNode matchedParent = (NodeListWrapperNode) parentsA.get(match);"
                        + "             insertIndex = indexOfObj(matchedParent.list.nodeList, match) + 1;"
                        + "         } else if (i < 0) {"
                        + "             insertIndex = 1 * 0 + 5;"
                        + "         }"
                        + "         switch(i) {"
                        + "             case 1 : print(\"it's one!\"); break;"
                        + "             case 0 : print(\"it's zero!\"); break;"
                        + "         }"
                        + "         "
                        + "         if (inserts.containsKey(insertIndex)) {"
                        + "             inserts.put(null, new List<? extends Node>(), insertIndex);"
                        + "         }"
                        + "         inserts.get(insertIndex).add(insertedUnder.get(i));"
                        + "     }"
                        + "     "
                        + "     NodeListWrapperNode match = (NodeListWrapperNode) alignsB.get(nlwn);"
                        + "     nlToIndexToInserts.get(0).put(match, inserts);"
                        + "}"
                        + "  if (!true) return nlToIndexToInserts; else return null;"
                        + "}"
                        + "}";
                test(m1, m2);
    }
}
