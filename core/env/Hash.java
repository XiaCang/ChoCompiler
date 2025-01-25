package core.env;

import java.util.HashMap;
import java.util.Map;

public class Hash extends Obj {
    HashMap<HashKey, HashPair> pairs = new HashMap<>();

    public Hash(HashMap<HashKey, HashPair> pairs) {
        this.pairs = pairs;
    }

    public int size() {
        return pairs.size();
    }

    public HashPair get(HashKey key) {
        return pairs.get(key);
    }

    public void put(HashKey key, HashPair value) {
        pairs.put(key, value);
    }

    @Override
    public String inspect() {
        String str = "{";
        if (pairs.size() == 0) {
            str += "}";
            return str;
        }
        for (Map.Entry<HashKey, HashPair> entry : pairs.entrySet()) {
            str += entry.getValue().getKey().inspect() + ":" + entry.getValue().getValue().inspect() + ",";
        }
        str = str.substring(0, str.length() - 1);
        str += "}";
        return str;
    }

    @Override
    public ObjType type() {
        return ObjType.HASH;
    }
}
