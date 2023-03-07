package net.starly.shop.context;

import net.starly.core.data.Config;
import net.starly.shop.ShopPlusMain;

public class ConfigContent {
    private static ConfigContent instance;
    private final Config config, msgConfig;

    private ConfigContent() {
        msgConfig = new Config("message", ShopPlusMain.getPlugin());
        msgConfig.loadDefaultConfig();
        msgConfig.setPrefix("prefix");

        config = new Config("config", ShopPlusMain.getPlugin());
        if (!config.isFileExist()) new Config("shop/Example-Shop", ShopPlusMain.getPlugin()).loadDefaultConfig();
        config.loadDefaultConfig();
    }

    public static ConfigContent getInstance() {
        if (instance == null) instance = new ConfigContent();
        return instance;
    }

    public Config getMsgConfig() {
        return msgConfig;
    }

    public Config getConfig() {
        return config;
    }
}
