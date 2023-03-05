package net.starly.shop.data;

import net.starly.core.jb.util.Pair;
import net.starly.shop.enums.ChatInputType;
import net.starly.shop.shop.ShopData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputMap {
    private final Map<UUID, Pair<ChatInputType, Pair<ShopData, Integer>>> chatInputTypeMap = new HashMap<>();

    public Pair<ChatInputType, Pair<ShopData, Integer>> get(Player player) {
        return get(player.getUniqueId());
    }

    public Pair<ChatInputType, Pair<ShopData, Integer>> get(UUID uuid) {
        return chatInputTypeMap.get(uuid);
    }

    public void set(Player key, Pair<ChatInputType, Pair<ShopData, Integer>> value) {
        set(key.getUniqueId(), value);
    }

    public void set(UUID key, Pair<ChatInputType, Pair<ShopData, Integer>> value) {
        chatInputTypeMap.put(key, value);
    }

    public boolean has(Player player) {
        return has(player.getUniqueId());
    }

    public boolean has(UUID uuid) {
        return chatInputTypeMap.containsKey(uuid);
    }

    public void remove(Player player) {
        remove(player.getUniqueId());
    }

    public void remove(UUID uuid) {
        chatInputTypeMap.remove(uuid);
    }
}
