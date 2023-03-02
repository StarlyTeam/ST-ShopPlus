package net.starly.boilerplate;

import net.starly.core.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BoilerPlateMain extends JavaPlugin {
    private static JavaPlugin plugin;

    @Override
    public void onEnable() {
        // DEPENDENCY
        if (Bukkit.getPluginManager().getPlugin("ST-Core") == null) {
            Bukkit.getLogger().warning("[" + getName() + "] ST-Core 플러그인이 적용되지 않았습니다! 플러그인을 비활성화합니다.");
            Bukkit.getLogger().warning("[" + getName() + "] 다운로드 링크 : http://starly.kr/discord");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        plugin = this;
        new Metrics(this, 12345); // TODO: 수정

        // CONFIG
        // TODO: 작성

        // COMMAND
        // TODO: 작성

        // EVENT
        // TODO: 작성
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
