package net.starly.shopplus.context;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigContext {
    private static ConfigContext instance;

    public static ConfigContext getInstance() {
        if (instance == null) instance = new ConfigContext();
        return instance;
    }

    private final Map<String, Object> map = new HashMap<>();

    private ConfigContext() {
    }

    @Deprecated
    public void initialize(FileConfiguration config) {
        config.getKeys(true).forEach(key -> {
            if (config.isConfigurationSection(key)) return;

            map.put(key, config.get(key));
        });
    }

    public Object get(String key) {
        return map.get(key);
    }

    public <T> T get(String key, Class<T> def) {
        return def.cast(get(key));
    }

    public void set(String key, Object value) {
        map.put(key, value);
    }

    public void reset() {
        map.clear();
    }
}
