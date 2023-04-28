package net.starly.shopplus.data;

import net.citizensnpcs.api.npc.NPC;
import net.starly.shopplus.shop.ShopData;

import java.util.HashMap;
import java.util.Map;

public class NPCMap {
    private final Map<NPC, ShopData> map = new HashMap<>();

    public ShopData get(NPC npc) {
        return map.get(npc);
    }

    public void set(NPC key, ShopData value) {
        map.put(key, value);
    }

    public boolean has(NPC npc) {
        return map.containsKey(npc);
    }

    public void remove(NPC npc) {
        map.remove(npc);
    }

    public void clear() {
        map.clear();
    }
}
