package net.starly.shopplus.data;

import net.starly.shopplus.shop.ShopData;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCMap {
    private final Map<UUID, ShopData> map = new HashMap<>();

    public ShopData get(Entity entity) {
        return get(entity.getUniqueId());
    }

    public ShopData get(UUID uuid) {
        return map.get(uuid);
    }

    public void set(Entity key, ShopData value) {
        set(key.getUniqueId(), value);
    }

    public void set(UUID key, ShopData value) {
        map.put(key, value);
    }

    public boolean has(Entity entity) {
        return has(entity.getUniqueId());
    }

    public boolean has(UUID uuid) {
        return map.containsKey(uuid);
    }

    public void remove(Entity entity) {
        remove(entity.getUniqueId());
    }

    public void remove(UUID uuid) {
        map.remove(uuid);
    }

    public void clear() {
        map.clear();
    }
}
