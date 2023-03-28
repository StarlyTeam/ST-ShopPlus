package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.starly.shopplus.data.InputMap;
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
