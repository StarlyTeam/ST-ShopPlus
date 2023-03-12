package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.starly.core.data.Config;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.InputMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@AllArgsConstructor
public class PlayerCommandPreprocessListener implements Listener {
    private final InputMap inputMap;

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (!inputMap.has(player)) return;

        Config msgConfig = ConfigContent.getInstance().getMsgConfig();
        player.sendMessage(msgConfig.getMessage("messages.cancelledInput"));
        inputMap.remove(player);
    }
}
