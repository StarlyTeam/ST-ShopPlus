package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.shopplus.ShopPlusMain;
import net.starly.shopplus.context.ConfigContext;
import net.starly.shopplus.data.InputMap;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.enums.InputType;
import net.starly.shopplus.enums.InventoryOpenType;
import net.starly.shopplus.message.MessageContext;
import net.starly.shopplus.message.enums.MessageType;
import net.starly.shopplus.shop.ShopData;
import net.starly.shopplus.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@AllArgsConstructor
public class ChatListener implements Listener {
    private final InvOpenMap invOpenMap;
    private final InputMap inputMap;

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (!inputMap.has(player)) return;

        MessageContext msgContext = MessageContext.getInstance();
        InputType inputType = inputMap.get(player).getFirst();
        ShopData shopData = ShopManager.getInstance().getShopData(inputMap.get(player).getSecond().getFirst());
        int slot = inputMap.get(player).getSecond().getSecond();
        inputMap.remove(player);
        event.setCancelled(true);

        if (inputType == InputType.ORIGIN_PRICE_SELL) {
            try {
                int sellPrice = Integer.parseInt(event.getMessage());

                if (sellPrice != -1 && sellPrice < 1) {
                    msgContext.get(MessageType.ERROR, "wrongSellPrice").send(player);
                } else {
                    shopData.setOriginSellPrice(slot, sellPrice);
                    if (!shopData.isMarketPriceEnabled()) shopData.setSellPrice(slot, sellPrice);
                    else if (sellPrice == -1) {
                        shopData.setMinSellPrice(slot, -1);
                        shopData.setMaxSellPrice(slot, -1);
                        shopData.setBuyPrice(slot, -1);
                    }

                    msgContext.get(MessageType.NORMAL, "sellPriceSet", msg -> msg.replace("{price}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongSellPrice").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.ORIGIN_PRICE_BUY) {
            try {
                int buyPrice = Integer.parseInt(event.getMessage());

                if (buyPrice != -1 && buyPrice < 1) {
                    msgContext.get(MessageType.ERROR, "wrongBuyPrice").send(player);
                } else {
                    shopData.setOriginBuyPrice(slot, buyPrice);
                    if (!shopData.isMarketPriceEnabled()) shopData.setBuyPrice(slot, buyPrice);
                    else if (buyPrice == -1) {
                        shopData.setMinBuyPrice(slot, -1);
                        shopData.setMaxBuyPrice(slot, -1);
                        shopData.setBuyPrice(slot, -1);
                    }

                    msgContext.get(MessageType.NORMAL, "buyPriceSet", msg -> msg.replace("{price}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongBuyPrice").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_SELL) {
            try {
                int sellPrice = Integer.parseInt(event.getMessage());

                if (sellPrice != -1 && sellPrice < 1) {
                    msgContext.get(MessageType.ERROR, "wrongSellPrice").send(player);
                } else {
                    shopData.setSellPrice(slot, sellPrice);
                    msgContext.get(MessageType.NORMAL, "sellPriceSet", msg -> msg.replace("{price}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongSellPrice").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_BUY) {
            try {
                int buyPrice = Integer.parseInt(event.getMessage());

                if (buyPrice != -1 && buyPrice < 1) {
                    msgContext.get(MessageType.ERROR, "wrongBuyPrice").send(player);
                } else {
                    shopData.setBuyPrice(slot, buyPrice);
                    msgContext.get(MessageType.NORMAL, "buyPriceSet", msg -> msg.replace("{price}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongBuyPrice").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.STOCK) {
            try {
                int stock = Integer.parseInt(event.getMessage());

                if (stock != -1 && stock < 1) {
                    msgContext.get(MessageType.ERROR, "wrongStock").send(player);
                } else {
                    shopData.setStock(slot, stock);
                    msgContext.get(MessageType.NORMAL, "stockSet", msg -> msg.replace("{stock}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongStock").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_MIN_SELL) {
            try {
                int price = Integer.parseInt(event.getMessage());

                if (price != -1 && price < 1) {
                    msgContext.get(MessageType.ERROR, "wrongMarketPrice").send(player);
                } else {
                    shopData.setMinSellPrice(slot, price);
                    msgContext.get(MessageType.NORMAL, "MarketPriceSet_MinSell", msg -> msg.replace("{price}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongMarketPrice").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_MAX_SELL) {
            try {
                int price = Integer.parseInt(event.getMessage());

                if (price != -1 && price < 1) {
                    msgContext.get(MessageType.ERROR, "wrongMarketPrice").send(player);
                } else {
                    shopData.setMaxSellPrice(slot, price);
                    msgContext.get(MessageType.NORMAL, "MarketPriceSet_MaxSell", msg -> msg.replace("{price}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongMarketPrice").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_MIN_BUY) {
            try {
                int price = Integer.parseInt(event.getMessage());

                if (price != -1 && price < 1) {
                    msgContext.get(MessageType.ERROR, "wrongMarketPrice").send(player);
                } else {
                    shopData.setMinBuyPrice(slot, price);
                    msgContext.get(MessageType.NORMAL, "MarketPriceSet_MinBuy", msg -> msg.replace("{price}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongMarketPrice").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if (inputType == InputType.MARKET_PRICE_MAX_BUY) {
            try {
                int price = Integer.parseInt(event.getMessage());

                if (price != -1 && price < 1) {
                    msgContext.get(MessageType.ERROR, "wrongMarketPrice").send(player);
                } else {
                    shopData.setMaxBuyPrice(slot, price);
                    msgContext.get(MessageType.NORMAL, "MarketPriceSet_MaxBuy", msg -> msg.replace("{price}", event.getMessage())).send(player);
                }
            } catch (NumberFormatException ignored) {
                msgContext.get(MessageType.ERROR, "wrongMarketPrice").send(player);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else return;

        inputMap.remove(player);
        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
            player.openInventory(shopData.getItemDetailSettingInv());
            invOpenMap.set(player, new Pair<>(InventoryOpenType.ITEM_DETAIL_SETTING, shopData));
        }, 1);
    }
}
