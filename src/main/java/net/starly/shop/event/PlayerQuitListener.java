package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.starly.shop.data.InputMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class PlayerQuitListener implements Listener {
    private final InputMap inputMap;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        inputMap.remove(event.getPlayer());
    }
}
