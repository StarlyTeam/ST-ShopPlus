package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.core.jb.version.nms.tank.NmsItemStackUtil;
import net.starly.core.jb.version.nms.wrapper.ItemStackWrapper;
import net.starly.core.jb.version.nms.wrapper.NBTTagCompoundWrapper;
import net.starly.core.util.InventoryUtil;
import net.starly.shop.ShopPlusMain;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.InputMap;
import net.starly.shop.data.InvOpenMap;
import net.starly.shop.data.NPCMap;
import net.starly.shop.enums.ButtonType;
import net.starly.shop.enums.InputType;
import net.starly.shop.enums.InvOpenType;
import net.starly.shop.shop.ShopData;
import net.starly.shop.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.starly.shop.ShopPlusMain.getEconomy;

@AllArgsConstructor
public class InventoryClickListener implements Listener {
    private final InvOpenMap invOpenMap;
    private final InputMap inputMap;
    private final NPCMap npcMap;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (player == null) return;
        if (!invOpenMap.has(player)) return;

        Config msgConfig = ConfigContent.getInstance().getMsgConfig();
        ItemStack currentStack = event.getCurrentItem();
        int slot = event.getSlot();
        Config config = ConfigContent.getInstance().getConfig();
        ClickType clickType = event.getClick();
        InvOpenType openType = invOpenMap.get(player).getFirst();
        ShopData shopData = invOpenMap.get(player).getSecond();
        event.setCancelled(true);

        if (openType != InvOpenType.ITEM_SETTING && currentStack == null) return;

        switch (openType) {
            case SHOP: {
                if (event.getClickedInventory() == player.getInventory()) return;

                if (clickType.name().equals(config.getString("click.buy"))) {
                    if (!shopData.hasStock(slot) || shopData.getBuyPrice(slot) == -1) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.cannotBuyThisItem"));
                        return;
                    }

                    ItemStack originStack = shopData.getItem(slot);

                    if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1) {
                        if (Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).noneMatch(s -> ItemStackUtil.equals(originStack, s) && s.getAmount() < s.getType().getMaxStackSize())) {
                            player.sendMessage(msgConfig.getMessage("errorMessages.inventorySpaceIsNotEnough"));
                            return;
                        }
                    }
                    if (getEconomy().getBalance(player) < shopData.getBuyPrice(slot)) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.moneyIsNotEnough"));
                        return;
                    }

                    getEconomy().withdrawPlayer(player, shopData.getBuyPrice(slot));
                    if (shopData.getStock(slot) != -1) shopData.setStock(slot, shopData.getStock(slot) - 1);
                    player.getInventory().addItem(originStack);
                    player.sendMessage(msgConfig.getMessage("messages.itemBuyed").replace("{price}", shopData.getBuyPrice(slot) + "").replace("{amount}", 1 + ""));
                } else if (clickType.name().equals(config.getString("click.buy-64"))) {
                    if (!shopData.hasStock(slot) || shopData.getBuyPrice(slot) == -1) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.cannotBuyThisItem"));
                        return;
                    }

                    ItemStack originStack = shopData.getItem(slot);

                    if (getEconomy().getBalance(player) < shopData.getBuyPrice(slot)) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.moneyIsNotEnough"));
                        return;
                    }
                    if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1) {
                        if (Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).noneMatch(s -> ItemStackUtil.equals(originStack, s) && s.getAmount() < s.getType().getMaxStackSize())) {
                            player.sendMessage(msgConfig.getMessage("errorMessages.inventorySpaceIsNotEnough"));
                            return;
                        }
                    }

                    int totalPurchased = 0;
                    for (int i = 0; i < 64; i++) {
                        if (totalPurchased > 64) return;
                        if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1
                                && Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).noneMatch(s -> ItemStackUtil.equals(originStack, s) && s.getAmount() < s.getType().getMaxStackSize()))
                            break;
                        if (!shopData.hasStock(slot)) break;
                        if (getEconomy().getBalance(player) < shopData.getBuyPrice(slot) * (totalPurchased + 1)) break;

                        player.getInventory().addItem(originStack);

                        if (shopData.getStock(slot) != -1) shopData.setStock(slot, shopData.getStock(slot) - 1);
                        totalPurchased++;
                    }

                    getEconomy().withdrawPlayer(player, totalPurchased * shopData.getBuyPrice(slot));
                    player.sendMessage(msgConfig.getMessage("messages.itemBuyed").replace("{price}", (shopData.getBuyPrice(slot) * totalPurchased) + "").replace("{amount}", totalPurchased + ""));
                } else if (clickType.name().equals(config.getString("click.sell"))) {
                    if (shopData.getSellPrice(slot) == -1) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.cannotSellThisItem"));
                        return;
                    }

                    ItemStack originStack = shopData.getItem(slot);

                    List<ItemStack> matches = Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).filter(stack -> ItemStackUtil.equals(originStack, stack)).collect(Collectors.toList());
                    if (matches.isEmpty()) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.noItemInInventory"));
                        return;
                    }

                    for (int i = 0; i < 36; i++) {
                        ItemStack itemStack = player.getInventory().getItem(i);
                        if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                        if (!matches.contains(itemStack)) continue;
                        itemStack = itemStack.clone();

                        if (itemStack.getAmount() == 1) {
                            player.getInventory().setItem(i, null);
                        } else {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                            player.getInventory().setItem(i, itemStack);
                        }
                        break;
                    }

                    getEconomy().depositPlayer(player, shopData.getSellPrice(slot));
                    if (shopData.getStock(slot) != -1) shopData.setStock(slot, shopData.getStock(slot) + 1);
                    player.sendMessage(msgConfig.getMessage("messages.itemSelled").replace("{price}", shopData.getSellPrice(slot) + "").replace("{amount}", 1 + ""));
                } else if (clickType.name().equals(config.getString("click.sell-64"))) {
                    if (shopData.getSellPrice(slot) == -1) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.cannotSellThisItem"));
                        return;
                    }

                    ItemStack originStack = shopData.getItem(slot);

                    List<ItemStack> matches = Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).filter(stack -> ItemStackUtil.equals(originStack, stack)).collect(Collectors.toList());
                    if (matches.isEmpty()) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.noItemInInventory"));
                        return;
                    }

                    AtomicInteger totalSelled = new AtomicInteger();
                    matches.forEach(s -> totalSelled.addAndGet(s.getAmount()));
                    if (totalSelled.get() > 64) totalSelled.set(64);

                    int totalRemoved = 0;
                    for (int i = 0; i < 36; i++) {
                        if (totalSelled.get() == totalRemoved) break;

                        ItemStack itemStack = player.getInventory().getItem(i);
                        if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                        if (!matches.contains(itemStack)) continue;
                        itemStack = itemStack.clone();

                        if (itemStack.getAmount() <= (totalSelled.get() - totalRemoved)) {
                            player.getInventory().setItem(i, null);
                            totalRemoved += itemStack.getAmount();
                        } else {
                            itemStack.setAmount(itemStack.getAmount() - (totalSelled.get() - totalRemoved));
                            player.getInventory().setItem(i, itemStack);
                            totalRemoved += (totalSelled.get() - totalRemoved);
                        }
                    }

                    if (shopData.getStock(slot) != -1)
                        shopData.setStock(slot, shopData.getStock(slot) + totalSelled.get());
                    getEconomy().depositPlayer(player, totalSelled.get() * shopData.getSellPrice(slot));
                    player.sendMessage(msgConfig.getMessage("messages.itemSelled").replace("{price}", (totalSelled.get() * shopData.getSellPrice(slot)) + "").replace("{amount}", totalSelled.get() + ""));
                } else return;

                event.getClickedInventory().setContents(shopData.getShopInv().getContents());
                break;
            }

            case SHOP_SETTING: {
                ItemStack itemStack = event.getCurrentItem();
                if (itemStack == null || itemStack.getType() == Material.AIR || !itemStack.hasItemMeta()) return;
                ItemStackWrapper nmsStack = NmsItemStackUtil.getInstance().asNMSCopy(itemStack);
                NBTTagCompoundWrapper nbtTagCompound = nmsStack.getTag();
                if (nbtTagCompound == null) return;
                ButtonType buttonType = ButtonType.valueOf(nbtTagCompound.getString("buttonId"));

                switch (buttonType) {
                    case SHOP_ENABLED -> {
                        shopData.setEnabled(false);
                        player.sendMessage(msgConfig.getMessage("messages.shopDisabled"));

                        event.getClickedInventory().setContents(shopData.getShopSettingInv().getContents());
                        break;
                    }

                    case SHOP_DISABLED -> {
                        shopData.setEnabled(true);
                        player.sendMessage(msgConfig.getMessage("messages.shopEnabled"));

                        event.getClickedInventory().setContents(shopData.getShopSettingInv().getContents());
                        break;
                    }

                    case ITEM_SETTING -> {
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.openInventory(shopData.getItemSettingInv());
                            invOpenMap.set(player, new Pair<>(InvOpenType.ITEM_SETTING, shopData));
                        }, 1);
                        break;
                    }

                    case ITEM_DETAIL_SETTING -> {
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.openInventory(shopData.getItemDetailSettingInv());
                            invOpenMap.set(player, new Pair<>(InvOpenType.ITEM_DETAIL_SETTING, shopData));
                        }, 1);
                        break;
                    }

                    case SET_NPC -> {
                        if (clickType == ClickType.SHIFT_LEFT) {
                            //삭제
                            if (!shopData.hasNPC()) {
                                player.sendMessage(msgConfig.getMessage("errorMessages.noNPC"));
                                return;
                            }

                            npcMap.remove(shopData.getNPC());
                            shopData.setNPC(null);
                            player.sendMessage(msgConfig.getMessage("messages.npcDeleted"));
                        } else if (clickType == ClickType.SHIFT_RIGHT) {
                            //이동
                            if (!shopData.hasNPC()) {
                                player.sendMessage(msgConfig.getMessage("errorMessages.noNPC"));
                                return;
                            }

                            player.closeInventory();
                            player.teleport(shopData.getNPC());
                            player.sendMessage(msgConfig.getMessage("messages.teleportedToNPC"));
                        } else {
                            //설정
                            player.closeInventory();
                            inputMap.set(player, new Pair<>(InputType.SET_NPC, new Pair<>(shopData, null)));
                            player.sendMessage(msgConfig.getMessage("messages.enterNPC"));
                        }
                        break;
                    }

                    case MARKET_PRICE_ENABLED -> {
                        shopData.setMarketPriceEnabled(false);
                        player.sendMessage(msgConfig.getMessage("messages.marketPriceDisabled"));

                        event.getClickedInventory().setContents(shopData.getShopSettingInv().getContents());
                        break;
                    }

                    case MARKET_PRICE_DISABLED -> {
                        shopData.setMarketPriceEnabled(true);
                        player.sendMessage(msgConfig.getMessage("messages.marketPriceEnabled"));

                        event.getClickedInventory().setContents(shopData.getShopSettingInv().getContents());
                        break;
                    }
                }
                break;
            }

            case ITEM_SETTING: {
                event.setCancelled(false);
                break;
            }

            case ITEM_DETAIL_SETTING: {
                if (event.getClickedInventory() == player.getInventory()) return;

                if (clickType == ClickType.RIGHT) {
                    //구매가격
                    invOpenMap.remove(player);
                    player.closeInventory();

                    Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                        player.sendMessage(msgConfig.getMessage("messages.enterBuyPrice"));
                        inputMap.set(player, new Pair<>(InputType.ORIGIN_PRICE_BUY, new Pair<>(shopData, slot)));
                    }, 1);
                } else if (clickType == ClickType.LEFT) {
                    //판매가격
                    invOpenMap.remove(player);
                    player.closeInventory();

                    Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                        player.sendMessage(msgConfig.getMessage("messages.enterSellPrice"));
                        inputMap.set(player, new Pair<>(InputType.ORIGIN_PRICE_SELL, new Pair<>(shopData, slot)));
                    }, 1);
                } else if (clickType == ClickType.SHIFT_LEFT) {
                    //삭제
                    shopData.setItem(slot, null);
                    event.getClickedInventory().setItem(slot, null);
                } else if (clickType == ClickType.DROP) {
                    //재고 추가
                    shopData.setStock(slot, shopData.getStock(slot) + 1);
                    event.getClickedInventory().setContents(shopData.getItemDetailSettingInv().getContents());

                    player.sendMessage(msgConfig.getMessage("messages.stockAdded"));
                } else if (clickType == ClickType.SHIFT_RIGHT) {
                    // 재고 설정
                    invOpenMap.remove(player);
                    player.closeInventory();

                    Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                        player.sendMessage(msgConfig.getMessage("messages.enterStock"));
                        inputMap.set(player, new Pair<>(InputType.STOCK, new Pair<>(shopData, slot)));
                    }, 1);
                } else if (clickType == ClickType.NUMBER_KEY) {
                    int key = event.getHotbarButton();
                    if (key < 0 || 6 < key) return;

                    if (key == 4) {
                        // 시세 초기화
                        int originSell = shopData.getOriginSellPrice(slot);
                        shopData.setSellPrice(slot, originSell);
                        shopData.setMinSellPrice(slot, originSell);
                        shopData.setMaxSellPrice(slot, originSell);

                        int originBuy = shopData.getOriginBuyPrice(slot);
                        shopData.setBuyPrice(slot, originBuy);
                        shopData.setMinBuyPrice(slot, originBuy);
                        shopData.setMaxBuyPrice(slot, originBuy);

                        event.getClickedInventory().setContents(shopData.getItemDetailSettingInv().getContents());

                        break;
                    }

                    invOpenMap.remove(player);
                    player.closeInventory();
                    if (key == 0) {
                        //최소 판매 시세 설정
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.sendMessage(msgConfig.getMessage("messages.enterMarketPrice_MinSell"));
                            inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_MIN_SELL, new Pair<>(shopData, slot)));
                        }, 1);
                    } else if (key == 1) {
                        //최대 판매 시세 설정
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.sendMessage(msgConfig.getMessage("messages.enterMarketPrice_MaxSell"));
                            inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_MAX_SELL, new Pair<>(shopData, slot)));
                        }, 1);
                    } else if (key == 2) {
                        //최소 구매 시세 설정
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.sendMessage(msgConfig.getMessage("messages.enterMarketPrice_MinBuy"));
                            inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_MIN_BUY, new Pair<>(shopData, slot)));
                        }, 1);
                    } else if (key == 3) {
                        //최대 구매 시세 설정
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.sendMessage(msgConfig.getMessage("messages.enterMarketPrice_MaxBuy"));
                            inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_MAX_BUY, new Pair<>(shopData, slot)));
                        }, 1);
                    } else if (key == 5) {
                        //판매 시세 설정
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.sendMessage(msgConfig.getMessage("messages.enterMarketPrice_Sell"));
                            inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_SELL, new Pair<>(shopData, slot)));
                        }, 1);
                    } else if (key == 6) {
                        //구매 시세 설정
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.sendMessage(msgConfig.getMessage("messages.enterMarketPrice_Buy"));
                            inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_BUY, new Pair<>(shopData, slot)));
                        }, 1);
                    }
                }

                break;
            }
        }
    }
}
