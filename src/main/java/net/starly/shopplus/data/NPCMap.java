package net.starly.shopplus.data;

import java.util.HashMap;
import java.util.Map;

public class NPCMap {
    private final Map<String, String> map = new HashMap<>();

    public String get(String name) {
        return map.get(name);
    }

    public void set(String key, String value) {
        map.put(key, value);
    }

    public boolean has(String name) {
        return map.containsKey(name);
    }

    public void remove(String name) {
        map.remove(name);
    }

    public void clear() {
        map.clear();
    }
}
