package net.starly.shop;

import net.milkbowl.vault.economy.Economy;
import net.starly.core.bstats.Metrics;
import net.starly.shop.command.ShopCmd;
import net.starly.shop.command.tabcomplete.ShopTab;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.ChatInputMap;
import net.starly.shop.data.InventoryOpenMap;
import net.starly.shop.event.AsyncPlayerChatListener;
import net.starly.shop.event.InventoryClickListener;
import net.starly.shop.event.InventoryCloseListener;
import net.starly.shop.event.PlayerCommandPreprocessListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlusMain extends JavaPlugin {
    private static JavaPlugin plugin;
    private static Economy economy;

    @Override
    public void onEnable() {
        // DEPENDENCY
        if (Bukkit.getPluginManager().getPlugin("ST-Core") == null || !Bukkit.getPluginManager().getPlugin("ST-Core").isEnabled()) {
            Bukkit.getLogger().warning("[" + getName() + "] ST-Core 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : http://starly.kr/discord");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else if (!setupEconomy()) {
            Bukkit.getLogger().warning("[" + getName() + "] Vault 플러그인이 적용되지 않았습니다! (또는 Vault Economy 연동지원 플러그인) 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : http://starly.kr/discord");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        plugin = this;
        new Metrics(this, 12345); // TODO: 수정

        // CONFIG
        ConfigContent.getInstance();

        // VARIABLES
        InventoryOpenMap inventoryOpenMap = new InventoryOpenMap();
        ChatInputMap chatInputMap = new ChatInputMap();

        // COMMAND
        getServer().getPluginCommand("shop").setExecutor(new ShopCmd(inventoryOpenMap));
        getServer().getPluginCommand("shop").setTabCompleter(new ShopTab(inventoryOpenMap));

        // EVENT
        getServer().getPluginManager().registerEvents(new InventoryClickListener(inventoryOpenMap, economy, chatInputMap), plugin);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(inventoryOpenMap), plugin);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(inventoryOpenMap, chatInputMap), plugin);
        getServer().getPluginManager().registerEvents(new PlayerCommandPreprocessListener(chatInputMap), plugin);
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static Economy getEconomy() {
        return economy;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
}
