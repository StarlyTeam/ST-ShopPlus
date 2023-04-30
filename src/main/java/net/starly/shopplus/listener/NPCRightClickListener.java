package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.starly.core.jb.util.Pair;
import net.starly.shopplus.ShopPlusMain;
import net.starly.shopplus.data.InputMap;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.enums.InventoryOpenType;
import net.starly.shopplus.message.MessageContext;
import net.starly.shopplus.message.enums.MessageType;
import net.starly.shopplus.shop.ShopData;
import net.starly.shopplus.shop.ShopManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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

        MessageContext msgContext = MessageContext.getInstance();
        event.setCancelled(true);

        if (inputMap.has(player)) {
            if (npcMap.has(npc.getName())) {
                msgContext.get(MessageType.ERROR, "alreadySetNPC").send(player);
                return;
            }

            ShopData shopData = ShopManager.getInstance().getShopData(inputMap.get(player).getSecond().getFirst());
            inputMap.remove(player);

            if (shopData.hasNPC()) npcMap.remove(shopData.getNPC());
            npcMap.set(npc.getName(), shopData.getName());
            shopData.setNPC(npc.getName());
            msgContext.get(MessageType.NORMAL, "NPCSet").send(player);
        } else if (npcMap.has(npc.getName())) {
            ShopData shopData = ShopManager.getInstance().getShopData(npcMap.get(npc.getName()));
            if (player.isSneaking() && player.hasPermission("starly.shop.edit." + shopData.getName())) {
                player.openInventory(shopData.getShopSettingInv());
                invOpenMap.set(player, new Pair<>(InventoryOpenType.SHOP_SETTING, shopData));
                return;
            }

            if (!(player.isOp() || shopData.isEnabled())) {
                msgContext.get(MessageType.ERROR, "shopNotOpened").send(player);
                return;
            }

            player.openInventory(shopData.getShopInv());
            new BukkitRunnable() {

                @Override
                public void run() {
                    invOpenMap.set(player, new Pair<>(InventoryOpenType.SHOP, shopData));
                }
            }.runTaskLater(ShopPlusMain.getInstance(), 1L);
        } else {
            event.setCancelled(false);
        }
    }
}
