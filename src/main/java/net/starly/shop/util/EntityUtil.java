package net.starly.shop.util;

import org.bukkit.entity.Entity;

import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class EntityUtil {
    public static Entity getEntity(UUID uuid) {
        if (uuid == null) return null;
        return getServer().getEntity(uuid);
    }
}
