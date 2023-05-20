package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.starly.core.jb.util.Pair;
import net.starly.shopplus.ShopPlusMain;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.enums.InventoryType;
import net.starly.shopplus.shop.ShopData;
import net.starly.shopplus.shop.ShopManager;
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

        InventoryType openType = invOpenMap.get(player).getFirst();
        String[] openData = invOpenMap.get(player).getSecond().split("\\|");
        int page = openData.length == 2 ? Integer.parseInt(openData[1]) : 0;
        ShopData shopData = ShopManager.getInstance().getShopData(openData[0]);
        invOpenMap.remove(player);

        switch (openType) {
            case ITEM_SETTING:
            case ITEM_DETAIL_SETTING: {
                if (openType == InventoryType.ITEM_SETTING) {
                    Inventory inv = event.getInventory();
                    Map<Integer, ItemStack> items = new HashMap<>();

//                    for (int i = 0; i < inv.getSize(); i++) items.put(i, inv.getItem(i)); TODO: custom page btn
                    for (int i = 0; i < inv.getSize() - 9; i++) items.put(i, inv.getItem(i));
                    shopData.setItems(page, items);
                }

                Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                    player.openInventory(shopData.getShopSettingInv());
                    invOpenMap.set(player, new Pair<>(InventoryType.SHOP_SETTING, shopData.getName()));
                }, 1);
            }
        }
    }
}
