package net.starly.shopplus.data;

import net.starly.core.jb.util.Pair;
import net.starly.shopplus.enums.InventoryOpenType;
import net.starly.shopplus.shop.ShopData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class InvOpenMap {
    private final Map<UUID, Pair<InventoryOpenType, String>> map = new HashMap<>();

    public Pair<InventoryOpenType, String> get(Player player) {
        return get(player.getUniqueId());
    }

    public Pair<InventoryOpenType, String> get(UUID uuid) {
        return map.get(uuid);
    }

    public void set(Player key, Pair<InventoryOpenType, String> value) {
        set(key.getUniqueId(), value);
    }

    public void set(UUID key, Pair<InventoryOpenType, String> value) {
        map.put(key, value);
    }

    public boolean has(Player player) {
        return has(player.getUniqueId());
    }

    public boolean has(UUID uuid) {
        return map.containsKey(uuid);
    }

    public void remove(Player player) {
        remove(player.getUniqueId());
    }

    public void remove(UUID uuid) {
        map.remove(uuid);
    }

    public List<UUID> getKeys() {
        return map.keySet().stream().collect(Collectors.toList());
    }
}
