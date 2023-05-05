package net.starly.shopplus;

import net.milkbowl.vault.economy.Economy;
import net.starly.core.bstats.Metrics;
import net.starly.shopplus.command.ShopCmd;
import net.starly.shopplus.command.tabcomplete.ShopTab;
import net.starly.shopplus.context.ConfigContext;
import net.starly.shopplus.data.InputMap;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.listener.*;
import net.starly.shopplus.message.MessageLoader;
import net.starly.shopplus.scheduler.MarketPriceTask;
import net.starly.shopplus.shop.ShopData;
import net.starly.shopplus.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ShopPlusMain extends JavaPlugin {
    private static ShopPlusMain instance;
    private static Economy economy;

    @Override
    public void onEnable() {
        /* DEPENDENCY
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        if (!isPluginEnabled("ST-Core")) {
            Bukkit.getLogger().warning("[" + getName() + "] ST-Core 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : http://starly.kr/");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else if (!isPluginEnabled("Vault")) {
            Bukkit.getLogger().warning("[" + getName() + "] Vault 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : https://www.spigotmc.org/resources/vault.34315/");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        if (economy == null) {
            Bukkit.getLogger().warning("[" + getName() + "] Vault와 연동되는 Economy 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : https://essentialsx.net/downloads.html");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        /* SETUP
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        instance = this;
        new Metrics(this, 17881);

        /* CONFIG
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                saveDefaultConfig();
                saveResource("shop/Example-Shop.yml", false);
            }
            ConfigContext.getInstance().initialize(YamlConfiguration.loadConfiguration(configFile));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            File messageFile = new File(getDataFolder(), "message.yml");
            if (!messageFile.exists()) saveResource("message.yml", false);
            MessageLoader.load(YamlConfiguration.loadConfiguration(messageFile));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /* VARIABLES
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        InvOpenMap invOpenMap = new InvOpenMap();
        InputMap inputMap = new InputMap();
        NPCMap npcMap = new NPCMap();

        /* TASK
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        MarketPriceTask.setInvOpenMap(invOpenMap);
        MarketPriceTask.start(ConfigContext.getInstance().get("marketPrice.updateInterval", Integer.class) * 20L);

        /* COMMAND
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        getServer().getPluginCommand("shop").setExecutor(new ShopCmd(invOpenMap, npcMap));
        getServer().getPluginCommand("shop").setTabCompleter(new ShopTab(invOpenMap));

        /* EVENT
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        getServer().getPluginManager().registerEvents(new ChatListener(invOpenMap, inputMap), instance);
        getServer().getPluginManager().registerEvents(new CommandListener(inputMap), instance);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(invOpenMap, inputMap, npcMap), instance);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(invOpenMap), instance);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(inputMap), instance);

        /* INITIALIZE
        ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        ShopManager shopManager = ShopManager.getInstance();
        Arrays.stream(new File(getDataFolder(), "shop/").listFiles()).forEach(configFile -> {
            String shopName = configFile.getName();
            shopName = shopName.substring(0, shopName.lastIndexOf('.'));

            shopManager.loadShop(shopName, configFile);
        });

        /* SUPPORT
        ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        if (isPluginEnabled("Citizens")) {
            getServer().getPluginManager().registerEvents(new NPCRemoveListener(npcMap), instance);
            getServer().getPluginManager().registerEvents(new NPCRightClickListener(invOpenMap, inputMap, npcMap), instance);

            /* INITIALIZE
            ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
            shopManager.getShopNames().forEach(shopName -> {
                ShopData shopData = shopManager.getShopData(shopName);
                if (shopData.hasNPC()) npcMap.set(shopData.getNPC(), shopName);
            });

            getLogger().info("성공적으로 모든 NPC를 불러왔습니다.");
        } else getLogger().warning("Citizens 플러그인이 적용되지 않았습니다! (NPC 기능 사용이 불가능합니다)");
    }

    @Override
    public void onDisable() {
        /* TASK
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        MarketPriceTask.stop();

        /* SAVE DATA
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        ShopManager.getInstance().saveAll();
    }

    public static ShopPlusMain getInstance() {
        return instance;
    }

    private boolean isPluginEnabled(String name) {
        Plugin plugin = getServer().getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    public static Economy getEconomy() {
        return economy;
    }
}
