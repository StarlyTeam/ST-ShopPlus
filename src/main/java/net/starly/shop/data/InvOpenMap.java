package net.starly.shop.data;

import net.starly.core.jb.util.Pair;
import net.starly.shop.enums.InvOpenType;
import net.starly.shop.shop.ShopData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvOpenMap {
    private final Map<UUID, Pair<InvOpenType, ShopData>> map = new HashMap<>();

    public Pair<InvOpenType, ShopData> get(Player player) {
        return get(player.getUniqueId());
    }

    public Pair<InvOpenType, ShopData> get(UUID uuid) {
        return map.get(uuid);
    }

    public void set(Player key, Pair<InvOpenType, ShopData> value) {
        set(key.getUniqueId(), value);
    }

    public void set(UUID key, Pair<InvOpenType, ShopData> value) {
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
