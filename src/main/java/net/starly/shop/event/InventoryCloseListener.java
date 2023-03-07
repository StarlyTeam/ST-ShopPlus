package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.starly.core.jb.util.Pair;
import net.starly.shop.ShopPlusMain;
import net.starly.shop.data.InventoryOpenMap;
import net.starly.shop.shop.ShopData;
import net.starly.shop.enums.InventoryOpenType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

@AllArgsConstructor
public class InventoryCloseListener implements Listener {
    private final InventoryOpenMap inventoryOpenMap;

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;
        if (!inventoryOpenMap.has(player)) return;

        InventoryOpenType openType = inventoryOpenMap.get(player).getFirst();
        ShopData shopData = inventoryOpenMap.get(player).getSecond();
        inventoryOpenMap.remove(player);

        switch (openType) {
            case ITEM_SETTING:
            case ITEM_DETAIL_SETTING: {
                if (openType == InventoryOpenType.ITEM_SETTING) {
                    Inventory inv = event.getInventory();
                    for (int i = 0; i < inv.getSize(); i++) shopData.setItem(i, inv.getItem(i));
                }

                Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getPlugin(), () -> {
                    player.openInventory(shopData.getShopSettingInv());
                    inventoryOpenMap.set(player, new Pair<>(InventoryOpenType.SHOP_SETTING, shopData));
                }, 1);
            }
        }
    }
}
