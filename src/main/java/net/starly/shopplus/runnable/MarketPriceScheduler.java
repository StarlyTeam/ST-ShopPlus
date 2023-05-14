package net.starly.shopplus.runnable;

import lombok.Setter;
import net.starly.core.jb.util.Pair;
import net.starly.shopplus.ShopPlusMain;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.enums.InventoryType;
import net.starly.shopplus.shop.ShopData;
import net.starly.shopplus.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MarketPriceScheduler extends BukkitRunnable {
    @Setter
    private static InvOpenMap invOpenMap = new InvOpenMap();
    private static BukkitRunnable task;

    public static void start(long interval) {
        task = new MarketPriceScheduler();

        task.runTaskTimerAsynchronously(ShopPlusMain.getInstance(), 0, interval);
    }

    public static void stop() {
        task.cancel();
    }

    @Override
    public void run() {
        List<String> shops = ShopManager.getInstance().getShopNames();

        shops.forEach(shopName -> {
            ShopData shopData = ShopManager.getInstance().getShopData(shopName);
            if (shopData.isMarketPriceEnabled()) {
                shopData.getItems().forEach((slotData, itemStack) -> {
                    int page = slotData.getFirst();
                    int slot = slotData.getSecond();

                    ItemStack item = shopData.getItem(page, slot);
                    if (item == null) return;

                    // SELL
                    if (shopData.isSellable(page, slot)) {
                        int sellPrice_MIN = shopData.getMinSellPrice(page, slot);
                        int sellPrice_MAX = shopData.getMaxSellPrice(page, slot);
                        int newSellPrice = randomInt(sellPrice_MIN, sellPrice_MAX);

                        shopData.setSellPrice(page, slot, newSellPrice);
                    }

                    // BUY
                    if (shopData.isBuyable(page, slot)) {
                        int buyPrice_MIN = shopData.getMinBuyPrice(page, slot);
                        int buyPrice_MAX = shopData.getMaxBuyPrice(page, slot);
                        int newBuyPrice = randomInt(buyPrice_MIN, buyPrice_MAX);

                        shopData.setBuyPrice(page, slot, newBuyPrice);
                    }
                });
            }

            for (UUID playerId : invOpenMap.getKeys()) {
                Pair<InventoryType, String> data = invOpenMap.get(playerId);
                if (data.getFirst() == InventoryType.SHOP_SETTING || data.getFirst() == InventoryType.ITEM_SETTING) continue;

                String[] openData = data.getSecond().split("\\|");
                ShopData openShopData = ShopManager.getInstance().getShopData(openData[0]);
                int page = Integer.parseInt(openData[1]);

                if (openShopData != shopData) return;

                Player player = Bukkit.getPlayer(playerId);
                if (data.getFirst() == InventoryType.SHOP) player.getOpenInventory().getTopInventory().setContents(shopData.getShopInv(page).getContents());
                else if (data.getFirst() == InventoryType.ITEM_DETAIL_SETTING) player.getOpenInventory().getTopInventory().setContents(shopData.getItemDetailSettingInv(page).getContents());
            }
        });
    }


    private int randomInt(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        return new Random().nextInt(max - min + 1) + min;
    }
}
