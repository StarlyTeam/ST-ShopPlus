package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.shopplus.context.ConfigContent;
import net.starly.shopplus.data.InputMap;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.enums.InventoryOpenType;
import net.starly.shopplus.shop.ShopData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class NPCRightClickListener implements Listener {
    private final InvOpenMap invOpenMap;
    private final InputMap inputMap;
    private final NPCMap npcMap;

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        if (player == null) return;
        NPC npc = event.getNPC();
        if (npc == null) return;

        Config msgConfig = ConfigContent.getInstance().getMsgConfig();
        event.setCancelled(true);

        if (inputMap.has(player)) {
            if (npcMap.has(npc)) {
                player.sendMessage(msgConfig.getMessage("errorMessages.alreadySetNPC"));
                return;
            }

            ShopData shopData = inputMap.get(player).getSecond().getFirst();
            inputMap.remove(player);

            if (shopData.hasNPC()) npcMap.remove(shopData.getNPC());
            npcMap.set(npc, shopData);
            shopData.setNPC(npc);
            player.sendMessage(msgConfig.getMessage("messages.NPCSet"));
        } else if (npcMap.has(npc)) {
            ShopData shopData = npcMap.get(npc);
            if (player.isSneaking()
                    && player.hasPermission("starly.shop.edit." + shopData.getName())) {
                player.openInventory(shopData.getShopSettingInv());
                invOpenMap.set(player, new Pair<>(InventoryOpenType.SHOP_SETTING, shopData));
                return;
            }

            if (!(player.isOp() || shopData.isEnabled())) {
                player.sendMessage(msgConfig.getMessage("errorMessages.shopNotOpened"));
                return;
            }

            player.openInventory(shopData.getShopInv());
            invOpenMap.set(player, new Pair<>(InventoryOpenType.SHOP, shopData));
        } else {
            event.setCancelled(false);
        }
    }
}
