package net.starly.shopplus.data;

import net.starly.core.jb.util.Pair;
import net.starly.shopplus.enums.InputType;
import net.starly.shopplus.shop.ShopData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InputMap {
    private final Map<UUID, Pair<InputType, Pair<ShopData, Integer>>> map = new HashMap<>();

    public Pair<InputType, Pair<ShopData, Integer>> get(Player player) {
        return get(player.getUniqueId());
    }

    public Pair<InputType, Pair<ShopData, Integer>> get(UUID uuid) {
        return map.get(uuid);
    }

    public void set(Player key, Pair<InputType, Pair<ShopData, Integer>> value) {
        set(key.getUniqueId(), value);
    }

    public void set(UUID key, Pair<InputType, Pair<ShopData, Integer>> value) {
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
}
