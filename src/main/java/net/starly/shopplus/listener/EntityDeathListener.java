package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.shop.ShopData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

@AllArgsConstructor
public class EntityDeathListener implements Listener {
    private final NPCMap npcMap;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) return;
        if (!npcMap.has(entity)) return;
        ShopData shopData = npcMap.get(entity);
        npcMap.remove(entity);
        shopData.setNPC(null);
    }
}
