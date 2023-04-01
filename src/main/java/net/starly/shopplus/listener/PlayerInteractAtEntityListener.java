package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.shopplus.context.ConfigContent;
import net.starly.shopplus.data.InputMap;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.enums.InventoryOpenType;
import net.starly.shopplus.shop.ShopData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

@AllArgsConstructor
public class PlayerInteractAtEntityListener implements Listener {
    private final InvOpenMap invOpenMap;
    private final InputMap inputMap;
    private final NPCMap npcMap;

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        if (player == null) return;
        Entity entity = event.getRightClicked();
        if (entity == null) return;

        Config msgConfig = ConfigContent.getInstance().getMsgConfig();
        event.setCancelled(true);

        if (inputMap.has(player)) {
            if (npcMap.has(entity)) {
                player.sendMessage(msgConfig.getMessage("errorMessages.alreadySetNPC"));
                return;
            }

            ShopData shopData = inputMap.get(player).getSecond().getFirst();
            inputMap.remove(player);

            if (shopData.hasNPC()) npcMap.remove(shopData.getNPC());
            npcMap.set(entity, shopData);
            shopData.setNPC(entity);
            player.sendMessage(msgConfig.getMessage("messages.NPCSet"));
        } else if (npcMap.has(entity)) {
            ShopData shopData = npcMap.get(entity);
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
