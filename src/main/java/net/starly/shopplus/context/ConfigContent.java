package net.starly.shopplus.context;

import net.starly.core.data.Config;
import net.starly.shopplus.ShopPlusMain;

public class ConfigContent {
    private static ConfigContent instance;
    private final Config config, msgConfig;

    private ConfigContent() {
        msgConfig = new Config("message", ShopPlusMain.getInstance());
        msgConfig.loadDefaultConfig();
        msgConfig.setPrefix("prefix");

        config = new Config("config", ShopPlusMain.getInstance());
        if (!config.isFileExist()) new Config("shop/Example-Shop", ShopPlusMain.getInstance()).loadDefaultConfig();
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
