package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.starly.core.jb.util.Pair;
import net.starly.shop.ShopPlusMain;
import net.starly.shop.data.InvOpenMap;
import net.starly.shop.enums.InvOpenType;
import net.starly.shop.shop.ShopData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class InventoryCloseListener implements Listener {
    private final InvOpenMap invOpenMap;

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;
        if (!invOpenMap.has(player)) return;

        InvOpenType openType = invOpenMap.get(player).getFirst();
        ShopData shopData = invOpenMap.get(player).getSecond();
        invOpenMap.remove(player);

        switch (openType) {
            case ITEM_SETTING:
            case ITEM_DETAIL_SETTING: {
                if (openType == InvOpenType.ITEM_SETTING) {
                    Inventory inv = event.getInventory();
                    Map<Integer, ItemStack> items = new HashMap<>();
                    for (int i = 0; i < inv.getSize(); i++) items.put(i, inv.getItem(i));

                    shopData.setItems(items);
                }

                Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                    player.openInventory(shopData.getShopSettingInv());
                    invOpenMap.set(player, new Pair<>(InvOpenType.SHOP_SETTING, shopData));
                }, 1);
            }
        }
    }
}
