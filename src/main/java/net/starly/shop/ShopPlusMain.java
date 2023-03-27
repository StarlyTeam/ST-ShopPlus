package net.starly.shop;

import net.milkbowl.vault.economy.Economy;
import net.starly.core.bstats.Metrics;
import net.starly.shop.command.ShopCmd;
import net.starly.shop.command.tabcomplete.ShopTab;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.InputMap;
import net.starly.shop.data.InvOpenMap;
import net.starly.shop.data.NPCMap;
import net.starly.shop.event.*;
import net.starly.shop.scheduler.MarketPriceTask;
import net.starly.shop.shop.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlusMain extends JavaPlugin {
    private static ShopPlusMain instance;
    private static Economy economy;

    @Override
    public void onEnable() {
        /* DEPENDENCY
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        if (!isPluginEnabled("ST-Core")) {
            Bukkit.getLogger().warning("[" + getName() + "] ST-Core 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : http://starly.kr/discord");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else if (!isPluginEnabled("Vault")) {
            Bukkit.getLogger().warning("[" + getName() + "] Vault 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : https://www.spigotmc.org/resources/vault.34315/");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        setupEconomy();
        if (economy == null) {
            Bukkit.getLogger().warning("[" + getName() + "] Vault와 연동되는 Economy 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : https://essentialsx.net/");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        /* SETUP
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        instance = this;
        new Metrics(this, 17881);

        /* CONFIG
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        ConfigContent.getInstance();

        /* VARIABLES
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        InvOpenMap invOpenMap = new InvOpenMap();
        InputMap inputMap = new InputMap();
        NPCMap npcMap = new NPCMap();

        /* TASK
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        MarketPriceTask.setInvOpenMap(invOpenMap);
        MarketPriceTask.start();

        /* COMMAND
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        getServer().getPluginCommand("shop").setExecutor(new ShopCmd(invOpenMap));
        getServer().getPluginCommand("shop").setTabCompleter(new ShopTab(invOpenMap));

        /* EVENT
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        getServer().getPluginManager().registerEvents(new InventoryClickListener(invOpenMap, inputMap, npcMap), instance);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(npcMap), instance);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(invOpenMap), instance);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(invOpenMap, inputMap), instance);
        getServer().getPluginManager().registerEvents(new PlayerCommandPreprocessListener(inputMap), instance);
        getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(invOpenMap, inputMap, npcMap), instance);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(inputMap), instance);

        /* INITIALIZE
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        ShopUtil.getShopNames().stream().map(ShopUtil::getShopData).forEach(shop -> {
            if (shop.hasNPC()) npcMap.set(shop.getNPC(), shop);
        });
    }

    @Override
    public void onDisable() {
        /* TASK
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        MarketPriceTask.stop();
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

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();
    }
}
