package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.npc.NPC;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.shop.ShopData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class NPCRemoveListener implements Listener {
    private final NPCMap npcMap;

    @EventHandler
    public void onEntityDeath(NPCRemoveEvent event) {
        NPC npc = event.getNPC();
        if (npc == null) return;

        if (!npcMap.has(npc)) return;

        ShopData shopData = npcMap.get(npc);
        npcMap.remove(npc);
        shopData.setNPC(null);
    }
}
