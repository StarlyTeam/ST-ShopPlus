package net.starly.shopplus.shop;

import net.starly.shopplus.ShopPlusMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ShopManager {

    private static ShopManager instance;
    public static ShopManager getInstance() {
        if (instance == null) instance = new ShopManager();
        return instance;
    }


    private final Map<String, ShopData> map = new HashMap<>();


    public List<String> getShopNames() {
        try {
            return Arrays.stream(new File(ShopPlusMain.getInstance().getDataFolder(), "shop/").list()).map(name -> name.replace(".yml", "")).collect(Collectors.toList());
        } catch (NullPointerException ex) {
            new File(ShopPlusMain.getInstance().getDataFolder(), "shop/").mkdirs();
            return new ArrayList<>();
        }
    }

    public ShopData getShopData(String name) {
        if (!map.containsKey(name)) map.put(name, new ShopData(new File(ShopPlusMain.getInstance().getDataFolder(), "shop/" + name + ".yml")));
        return map.get(name);
    }

    public void createShop(String name, int line, String title) {
        try {
            File configFile = new File(ShopPlusMain.getInstance().getDataFolder(), "shop/" + name + ".yml");
            configFile.createNewFile();


            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            config.set("shop.enabled", false);
            config.set("shop.marketPrice", false);
            config.set("shop.title", title);
            config.set("shop.size", line * 9);
            config.set("shop.prices", new HashMap<>());
            config.set("shop.npc", null);

            config.save(configFile);


            map.put(name, new ShopData(configFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteShop(String name) {
        File configFile = new File(ShopPlusMain.getInstance().getDataFolder(), "shop/" + name + ".yml");
        if (configFile.exists()) configFile.delete();
    }
}
