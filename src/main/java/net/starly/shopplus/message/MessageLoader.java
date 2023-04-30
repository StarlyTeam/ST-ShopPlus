package net.starly.shopplus.message;

import net.starly.core.util.PreCondition;
import net.starly.shopplus.message.enums.MessageType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Objects;

public class MessageLoader {

    private static boolean loaded = false;

    public static void load(FileConfiguration config) {
        if (loaded) {
            MessageContext.getInstance().reset();
            loaded = false;
        }

        Arrays.stream(MessageType.values()).forEach(type -> loadMessageSection(Objects.requireNonNull(config.getConfigurationSection(type.getPath())), type));

        loaded = true;
    }

    private static void loadMessageSection(ConfigurationSection section, MessageType type) {
        PreCondition.nonNull(section, "메세지를 로드할 수 없습니다. : " + type.name());

        MessageContext msgContext = MessageContext.getInstance();
        section.getKeys(false).forEach(key -> {
            msgContext.set(type, key, section.isList(key) ? String.join("\n", section.getStringList(key)) : section.getString(key));
        });
    }
}