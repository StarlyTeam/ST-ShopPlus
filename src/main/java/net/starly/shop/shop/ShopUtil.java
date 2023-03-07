package net.starly.shop.shop;

import net.starly.core.data.Config;
import net.starly.shop.ShopMain;

import java.util.HashMap;
import java.util.List;

public class ShopUtil {
    public static ShopData getShopData(String name) {
        return new ShopData(new Config("shop/" + name, ShopMain.getPlugin()));
    }

    public static List<String> getShops() {
        return new Config("shop/", ShopMain.getPlugin()).getFileNames();
    }

    public static void createShop(String name, int line, String title) {
        Config config = new Config("shop/" + name, ShopMain.getPlugin());
        config.setBoolean("shop.enabled", false);
        config.setString("shop.title", title);
        config.setInt("shop.size", line * 9);
        config.setObject("shop.items", new HashMap<>());
        config.setObject("shop.prices", new HashMap<>());
    }

    public static void deleteShop(String name) {
        Config config = new Config("shop/" + name, ShopMain.getPlugin());
        if (config.isFileExist()) config.delete();
    }
}
