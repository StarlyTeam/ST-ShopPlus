package net.starly.shopplus.shop;

import net.starly.core.data.Config;
import net.starly.shopplus.ShopPlusMain;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ShopUtil {
    public static ShopData getShopData(String name) {
        return new ShopData(new Config("shop/" + name, ShopPlusMain.getInstance()));
    }

    public static List<String> getShops() {
        return new Config("shop/", ShopPlusMain.getInstance()).getFileNames();
    }

    public static List<String> getShopNames() {
        File shopFolder = new File(ShopPlusMain.getInstance().getDataFolder(), "shop/");
        if (shopFolder.list() == null) return Collections.emptyList();
        else return Arrays.stream(shopFolder.list()).map(s -> s.replace(".yml", "")).collect(Collectors.toList());
    }

    public static void createShop(String name, int line, String title) {
        Config config = new Config("shop/" + name, ShopPlusMain.getInstance());
        config.loadDefaultConfig();

        config.setBoolean("shop.enabled", false);
        config.setBoolean("shop.marketPrice", false);
        config.setString("shop.title", title);
        config.setInt("shop.size", line * 9);
        config.setObject("shop.prices", new HashMap<>());
        config.setString("shop.npc", "<none>");
    }

    public static void deleteShop(String name) {
        Config config = new Config("shop/" + name, ShopPlusMain.getInstance());
        if (config.isFileExist()) config.delete();
    }
}
