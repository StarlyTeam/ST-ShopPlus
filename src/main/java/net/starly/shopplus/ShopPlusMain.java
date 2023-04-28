package net.starly.shopplus;

import net.citizensnpcs.api.CitizensAPI;
import net.milkbowl.vault.economy.Economy;
import net.starly.core.bstats.Metrics;
import net.starly.shopplus.command.ShopCmd;
import net.starly.shopplus.command.tabcomplete.ShopTab;
import net.starly.shopplus.context.ConfigContent;
import net.starly.shopplus.data.InputMap;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.listener.*;
import net.starly.shopplus.scheduler.MarketPriceTask;
import net.starly.shopplus.shop.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ShopPlusMain extends JavaPlugin {
    private static ShopPlusMain instance;
    private static Economy economy;

    @Override
    public void onEnable() {
        /* DEPENDENCY
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        if (!isPluginEnabled("net.starly.core.StarlyCore")) {
            Bukkit.getLogger().warning("[" + getName() + "] ST-Core 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : http://starly.kr/");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else if (!isPluginEnabled("net.milkbowl.vault.Vault")) {
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
        getServer().getPluginCommand("shop").setExecutor(new ShopCmd(invOpenMap, npcMap));
        getServer().getPluginCommand("shop").setTabCompleter(new ShopTab(invOpenMap));

        /* EVENT
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        getServer().getPluginManager().registerEvents(new ChatListener(invOpenMap, inputMap), instance);
        getServer().getPluginManager().registerEvents(new CommandListener(inputMap), instance);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(invOpenMap, inputMap, npcMap), instance);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(invOpenMap), instance);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(inputMap), instance);

        if (!isPluginEnabled("net.citizensnpcs.Citizens")) {
            Bukkit.getLogger().warning("[" + getName() + "] Citizens 플러그인이 적용되지 않았습니다! (NPC 기능 사용이 불가능합니다)");
        } else {
            getServer().getPluginManager().registerEvents(new NPCRemoveListener(npcMap), instance);
            getServer().getPluginManager().registerEvents(new NPCRightClickListener(invOpenMap, inputMap, npcMap), instance);

            /* INITIALIZE
            ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
            new BukkitRunnable() {

                @Override
                public void run() {
                    ShopUtil.getShopNames().stream().map(ShopUtil::getShopData).forEach(shop -> {
                        if (shop.hasNPC()) npcMap.set(shop.getNPC(), shop);
                    });

                    getLogger().info("성공적으로 모든 NPC를 불러왔습니다.");
                }
            }.runTaskLater(instance, 3 * 20L);
        }
    }

    @Override
    public void onDisable() {
        /* TASK
         ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
        try { MarketPriceTask.stop(); } catch (Exception ignored) {}
    }

    public static ShopPlusMain getInstance() {
        return instance;
    }

    private boolean isPluginEnabled(String path) {
        try {
            Class.forName(path);
            return true;
        } catch (ClassNotFoundException ignored) {
        } catch (Exception ex) { ex.printStackTrace(); }
        return false;
    }

    public static Economy getEconomy() {
        return economy;
    }
}
