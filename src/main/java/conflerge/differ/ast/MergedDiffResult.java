package conflerge.differ.ast;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.Node;

public class MergedDiffResult extends DiffResult {
    public final DiffResult local; 
    public final DiffResult remote;
    
    public MergedDiffResult(DiffResult local, DiffResult remote) {
        super(mergeMaps(local.deletes, remote.deletes), 
              mergeMaps(local.replaces, remote.replaces), 
              mergeMaps(local.insertsUnder, remote.insertsUnder));
        
        this.local = local;
        this.remote = remote;
    }
    
    private static <K, V> Map<K, V> mergeMaps(Map<K, V> m1, Map<K, V> m2) {
        Map<K, V> res = new IdentityHashMap<>(m1);
        for (K key : m2.keySet()) {
            res.put(key, m2.get(key));
        }
        return res;
    }
}
