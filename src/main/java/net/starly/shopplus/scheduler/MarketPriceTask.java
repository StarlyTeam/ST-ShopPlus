package net.starly.shopplus.scheduler;

import lombok.Setter;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.shopplus.ShopPlusMain;
import net.starly.shopplus.context.ConfigContent;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.enums.InventoryOpenType;
import net.starly.shopplus.shop.ShopData;
import net.starly.shopplus.shop.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MarketPriceTask extends BukkitRunnable {
    @Setter
    private static InvOpenMap invOpenMap = new InvOpenMap();
    private static MarketPriceTask instance;

    public static void start() {
        Config config = ConfigContent.getInstance().getConfig();
        instance = new MarketPriceTask();

        instance.runTaskTimerAsynchronously(ShopPlusMain.getInstance(), 0, config.getInt("marketPrice.updateInterval") * 20L);
    }

    public static void stop() {
        instance.cancel();
    }

    @Override
    public void run() {
        List<String> shops = ShopUtil.getShopNames();

        for (String shopName : shops) {
            ShopData shopData = ShopUtil.getShopData(shopName);
            if (!shopData.isMarketPriceEnabled()) return;

            for (int i = 0; i < shopData.getSize(); i++) {
                ItemStack item = shopData.getItem(i);
                if (item == null) continue;

                // SELL
                if (shopData.isSellable(i)) {
                    int sellPrice_MIN = shopData.getMinSellPrice(i);
                    int sellPrice_MAX = shopData.getMaxSellPrice(i);
                    int newSellPrice = randomInt(sellPrice_MIN, sellPrice_MAX);
                    shopData.setSellPrice(i, newSellPrice);
                }

                // BUY
                if (shopData.isBuyable(i)) {
                    int buyPrice_MIN = shopData.getMinBuyPrice(i);
                    int buyPrice_MAX = shopData.getMaxBuyPrice(i);
                    int newBuyPrice = randomInt(buyPrice_MIN, buyPrice_MAX);
                    shopData.setBuyPrice(i, newBuyPrice);
                }
            }

            for (UUID key : invOpenMap.getKeys()) {
                Pair<InventoryOpenType, ShopData> data = invOpenMap.get(key);
                if (data.getSecond().equals(shopData)) return;

                Player player = Bukkit.getPlayer(key);
                if (data.getFirst() == InventoryOpenType.SHOP)
                    player.getOpenInventory().getTopInventory().setContents(shopData.getShopInv().getContents());
                else if (data.getFirst() == InventoryOpenType.ITEM_DETAIL_SETTING)
                    player.getOpenInventory().getTopInventory().setContents(shopData.getItemDetailSettingInv().getContents());
            }
        }
    }


    private int randomInt(int min, int max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        return new Random().nextInt(max - min + 1) + min;
    }
}
