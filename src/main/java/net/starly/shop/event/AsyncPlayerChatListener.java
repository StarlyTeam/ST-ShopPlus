package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.shop.ShopMain;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.ChatInputMap;
import net.starly.shop.data.InventoryOpenMap;
import net.starly.shop.enums.ChatInputType;
import net.starly.shop.enums.InventoryOpenType;
import net.starly.shop.shop.ShopData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AsyncPlayerChatListener implements Listener {
    private final InventoryOpenMap inventoryOpenMap;
    private final ChatInputMap chatInputMap;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (!chatInputMap.has(player)) return;

        Config msgConfig = ConfigContent.getInstance().getMsgConfig();
        ChatInputType chatInputType = chatInputMap.get(player).getFirst();
        ShopData shopData = chatInputMap.get(player).getSecond().getFirst();
        int slot = chatInputMap.get(player).getSecond().getSecond();
        chatInputMap.remove(player);
        event.setCancelled(true);

        if (chatInputType == ChatInputType.BUY_PRICE) {
            try {
                int buyPrice = Integer.parseInt(event.getMessage());

                if (buyPrice != -1 && buyPrice < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongBuyPrice"));
                } else {
                    shopData.setBuyPrice(slot, buyPrice);
                    player.sendMessage(msgConfig.getMessage("messages.buyPriceSet").replace("{price}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongBuyPrice"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            chatInputMap.remove(player);

            Bukkit.getServer().getScheduler().runTaskLater(ShopMain.getPlugin(), () -> {
                player.openInventory(shopData.getItemDetailSettingInv());
                inventoryOpenMap.set(player, new Pair<>(InventoryOpenType.ITEM_DETAIL_SETTING, shopData));
            }, 1);
        } else if (chatInputType == ChatInputType.SELL_PRICE) {
            try {
                int sellPrice = Integer.parseInt(event.getMessage());

                if (sellPrice != -1 && sellPrice < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongSellPrice"));
                } else {
                    shopData.setSellPrice(slot, sellPrice);
                    player.sendMessage(msgConfig.getMessage("messages.sellPriceSet").replace("{price}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongSellPrice"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            chatInputMap.remove(player);

            Bukkit.getServer().getScheduler().runTaskLater(ShopMain.getPlugin(), () -> {
                player.openInventory(shopData.getItemDetailSettingInv());
                inventoryOpenMap.set(player, new Pair<>(InventoryOpenType.ITEM_DETAIL_SETTING, shopData));
            }, 1);
        }
    }
}
