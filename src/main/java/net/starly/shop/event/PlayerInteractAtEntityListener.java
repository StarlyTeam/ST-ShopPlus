package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.starly.core.jb.util.Pair;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.InputMap;
import net.starly.shop.data.InvOpenMap;
import net.starly.shop.data.NPCMap;
import net.starly.shop.enums.InvOpenType;
import net.starly.shop.shop.ShopData;
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

        event.setCancelled(true);

        if (inputMap.has(player)) {
            if (npcMap.has(entity)) {
                player.sendMessage("이미 다른 상점에 연결된 NPC잖아!"); //TODO: MESSAGE
                return;
            }

            ShopData shopData = inputMap.get(player).getSecond().getFirst();
            inputMap.remove(player);

            if (shopData.hasNPC()) npcMap.remove(shopData.getNPC());
            npcMap.set(entity, shopData);
            shopData.setNPC(entity);
            player.sendMessage("NPC 연결했음~"); //TODO: MESSAGE
        } else if (npcMap.has(entity)) {
            ShopData shopData = npcMap.get(entity);
            if (!(player.isOp() || shopData.isEnabled())) {
                player.sendMessage(ConfigContent.getInstance().getMsgConfig().getMessage("errorMessages.shopNotOpened"));
                return;
            }

            player.openInventory(shopData.getShopInv());
            invOpenMap.set(player, new Pair<>(InvOpenType.SHOP, shopData));
        } else {
            event.setCancelled(false);
        }
    }
}
