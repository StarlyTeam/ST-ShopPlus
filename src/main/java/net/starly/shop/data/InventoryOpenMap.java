package net.starly.shop.data;

import net.starly.core.jb.util.Pair;
import net.starly.shop.enums.InventoryOpenType;
import net.starly.shop.shop.ShopData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryOpenMap {
    private final Map<UUID, Pair<InventoryOpenType, ShopData>> shopOpenMap = new HashMap<>();

    public Pair<InventoryOpenType, ShopData> get(Player player) {
        return get(player.getUniqueId());
    }

    public Pair<InventoryOpenType, ShopData> get(UUID uuid) {
        return shopOpenMap.get(uuid);
    }

    public void set(Player key, Pair<InventoryOpenType, ShopData> value) {
        set(key.getUniqueId(), value);
    }

    public void set(UUID key, Pair<InventoryOpenType, ShopData> value) {
        shopOpenMap.put(key, value);
    }

    public boolean has(Player player) {
        return has(player.getUniqueId());
    }

    public boolean has(UUID uuid) {
        return shopOpenMap.containsKey(uuid);
    }

    public void remove(Player player) {
        remove(player.getUniqueId());
    }

    public void remove(UUID uuid) {
        shopOpenMap.remove(uuid);
    }
}
