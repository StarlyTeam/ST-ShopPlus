package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.shop.ShopPlusMain;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.InputMap;
import net.starly.shop.data.InvOpenMap;
import net.starly.shop.enums.InputType;
import net.starly.shop.enums.InvOpenType;
import net.starly.shop.shop.ShopData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@AllArgsConstructor
public class AsyncPlayerChatListener implements Listener {
    private final InvOpenMap invOpenMap;
    private final InputMap inputMap;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (!inputMap.has(player)) return;

        Config msgConfig = ConfigContent.getInstance().getMsgConfig();
        InputType inputType = inputMap.get(player).getFirst();
        ShopData shopData = inputMap.get(player).getSecond().getFirst();
        int slot = inputMap.get(player).getSecond().getSecond();
        inputMap.remove(player);
        event.setCancelled(true);

        if (inputType == InputType.ORIGIN_PRICE_SELL) {
            try {
                int sellPrice = Integer.parseInt(event.getMessage());

                if (sellPrice != -1 && sellPrice < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongSellPrice"));
                } else {
                    shopData.setOriginSellPrice(slot, sellPrice);
                    if (!shopData.isMarketPriceEnabled()) shopData.setSellPrice(slot, sellPrice);
                    else if (sellPrice == -1) {
                        shopData.setMinSellPrice(slot, -1);
                        shopData.setMaxSellPrice(slot, -1);
                        shopData.setBuyPrice(slot, -1);
                    }

                    player.sendMessage(msgConfig.getMessage("messages.sellPriceSet").replace("{price}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongSellPrice"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.ORIGIN_PRICE_BUY) {
            try {
                int buyPrice = Integer.parseInt(event.getMessage());

                if (buyPrice != -1 && buyPrice < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongBuyPrice"));
                } else {
                    shopData.setOriginBuyPrice(slot, buyPrice);
                    if (!shopData.isMarketPriceEnabled()) shopData.setBuyPrice(slot, buyPrice);
                    else if (buyPrice == -1) {
                        shopData.setMinBuyPrice(slot, -1);
                        shopData.setMaxBuyPrice(slot, -1);
                        shopData.setBuyPrice(slot, -1);
                    }

                    player.sendMessage(msgConfig.getMessage("messages.buyPriceSet").replace("{price}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongBuyPrice"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_SELL) {
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
        } else if (inputType == InputType.MARKET_PRICE_BUY) {
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
        } else if (inputType == InputType.STOCK) {
            try {
                int stock = Integer.parseInt(event.getMessage());

                if (stock != -1 && stock < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongStock"));
                } else {
                    shopData.setStock(slot, stock);
                    player.sendMessage(msgConfig.getMessage("messages.stockSet").replace("{stock}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongStock"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_MIN_SELL) {
            try {
                int price = Integer.parseInt(event.getMessage());

                if (price != -1 && price < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongMarketPrice"));
                } else {
                    shopData.setMinSellPrice(slot, price);
                    player.sendMessage(msgConfig.getMessage("messages.MarketPriceSet_MinSell").replace("{price}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongMarketPrice"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_MAX_SELL) {
            try {
                int price = Integer.parseInt(event.getMessage());

                if (price != -1 && price < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongMarketPrice"));
                } else {
                    shopData.setMaxSellPrice(slot, price);
                    player.sendMessage(msgConfig.getMessage("messages.MarketPriceSet_MaxSell").replace("{price}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongMarketPrice"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_MIN_BUY) {
            try {
                int price = Integer.parseInt(event.getMessage());

                if (price != -1 && price < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongMarketPrice"));
                } else {
                    shopData.setMinBuyPrice(slot, price);
                    player.sendMessage(msgConfig.getMessage("messages.MarketPriceSet_MinBuy").replace("{price}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongMarketPrice"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_MAX_BUY) {
            try {
                int price = Integer.parseInt(event.getMessage());

                if (price != -1 && price < 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongMarketPrice"));
                } else {
                    shopData.setMaxBuyPrice(slot, price);
                    player.sendMessage(msgConfig.getMessage("messages.MarketPriceSet_MaxBuy").replace("{price}", event.getMessage()));
                }
            } catch (NumberFormatException ignored) {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongMarketPrice"));
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else return;

        inputMap.remove(player);
        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
            player.openInventory(shopData.getItemDetailSettingInv());
            invOpenMap.set(player, new Pair<>(InvOpenType.ITEM_DETAIL_SETTING, shopData));
        }, 1);
    }
}
