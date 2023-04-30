package net.starly.shopplus.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.starly.shopplus.ShopPlusMain;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class STMessage {

    @Getter private final String prefix;
    @Getter private final String message;

    public String getText() {
        return prefix + message;
    }

    public void send(CommandSender target) {
        if (message.isEmpty()) return;
        target.sendMessage(prefix + message);
    }

    public void broadcast() {
        if (message.isEmpty()) return;
        ShopPlusMain.getInstance().getServer().broadcastMessage(message);
    }
}
