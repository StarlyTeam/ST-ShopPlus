package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.starly.shopplus.data.InputMap;
import net.starly.shopplus.message.MessageContext;
import net.starly.shopplus.message.enums.MessageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@AllArgsConstructor
public class CommandListener implements Listener {
    private final InputMap inputMap;

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (!inputMap.has(player)) return;

        MessageContext msgContext = MessageContext.getInstance();
        msgContext.get(MessageType.NORMAL, "cancelledInput").send(player);
        inputMap.remove(player);
    }
}
