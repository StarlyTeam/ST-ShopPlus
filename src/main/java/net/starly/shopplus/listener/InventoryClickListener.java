package net.starly.shopplus.listener;

import lombok.AllArgsConstructor;
import net.starly.core.jb.util.Pair;
import net.starly.core.jb.version.nms.tank.NmsItemStackUtil;
import net.starly.core.jb.version.nms.wrapper.ItemStackWrapper;
import net.starly.core.jb.version.nms.wrapper.NBTTagCompoundWrapper;
import net.starly.core.util.InventoryUtil;
import net.starly.shopplus.ShopPlusMain;
import net.starly.shopplus.context.ConfigContext;
import net.starly.shopplus.data.InputMap;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.enums.ButtonType;
import net.starly.shopplus.enums.InputType;
import net.starly.shopplus.enums.InventoryType;
import net.starly.shopplus.message.MessageContext;
import net.starly.shopplus.message.enums.MessageType;
import net.starly.shopplus.shop.ShopData;
import net.starly.shopplus.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.starly.shopplus.ShopPlusMain.getEconomy;

@AllArgsConstructor
public class InventoryClickListener implements Listener {
    private final InvOpenMap invOpenMap;
    private final InputMap inputMap;
    private final NPCMap npcMap;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        if (player == null) return;
        if (!invOpenMap.has(player)) return;

        MessageContext msgContext = MessageContext.getInstance();
        ConfigContext configContext = ConfigContext.getInstance();

        InventoryType openType = invOpenMap.get(player).getFirst();
        String[] openData = invOpenMap.get(player).getSecond().split("\\|");
        ShopData shopData = ShopManager.getInstance().getShopData(openData[0]);
        int currentPage = openData.length == 2 ? Integer.parseInt(openData[1]) : 0;

        ItemStack currentStack = event.getCurrentItem();
        ClickType clickType = event.getClick();
        int slot = event.getSlot();
        
        if (openType != InventoryType.ITEM_SETTING) event.setCancelled(true);
        if (event.getClickedInventory() == player.getInventory()) return;

        if (openType != InventoryType.SHOP_SETTING) {
            if (openType == InventoryType.ITEM_SETTING && clickType.isRightClick()) return;

            event.setCancelled(true);

            int newPage;
            if (slot == shopData.getSize() - 4) {
                // NEXT_PAGE
                if (openType != InventoryType.ITEM_SETTING
                        && currentPage >= shopData.getMaxPage()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }

                newPage = currentPage + 1;
            } else if (slot == shopData.getSize() - 6) {
                // PREVIOUS_PAGE
                if (currentPage <= 1) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }

                newPage = currentPage - 1;
            } else newPage = 0;


            if (newPage != 0) {
                if (openType == InventoryType.ITEM_SETTING) {
                    Inventory inv = event.getInventory();
                    Map<Integer, ItemStack> items = new HashMap<>();
                    for (int i = 0; i < inv.getSize(); i++) items.put(i, inv.getItem(i));
                    shopData.setItems(currentPage, items);
                }

                invOpenMap.remove(player);
                Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                    player.openInventory(shopData.getInv(openType, newPage));
                    invOpenMap.set(player, new Pair<>(openType, shopData.getName() + "|" + newPage));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                }, 1);
                return;
            } else if (openType == InventoryType.ITEM_SETTING) event.setCancelled(false);
        }

        if (currentStack == null || currentStack.getType() == Material.AIR) return;

        switch (openType) {
            case SHOP: {
                if (event.getClickedInventory() == player.getInventory()) return;

                if (clickType.name().equals(configContext.get("click.buy", String.class))) {
                    if (!shopData.hasStock(currentPage, slot) || shopData.getBuyPrice(currentPage, slot) == -1) {
                        msgContext.get(MessageType.ERROR, "cannotBuyThisItem").send(player);
                        return;
                    }

                    ItemStack originStack = shopData.getItem(currentPage, slot);

                    if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1) {
                        if (Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).noneMatch(s -> originStack.isSimilar(s) && s.getAmount() < s.getType().getMaxStackSize())) {
                            msgContext.get(MessageType.ERROR, "inventorySpaceIsNotEnough").send(player);
                            return;
                        }
                    }
                    if (getEconomy().getBalance(player) < shopData.getBuyPrice(currentPage, slot)) {
                        msgContext.get(MessageType.ERROR, "moneyIsNotEnough").send(player);
                        return;
                    }

                    getEconomy().withdrawPlayer(player, shopData.getBuyPrice(currentPage, slot));
                    if (shopData.getStock(currentPage, slot) != -1) shopData.setStock(currentPage, slot, shopData.getStock(currentPage, slot) - 1);
                    player.getInventory().addItem(originStack);
                    msgContext.get(MessageType.NORMAL, "itemBuyed", msg -> msg.replace("{price}", String.valueOf(shopData.getBuyPrice(currentPage, slot))).replace("{amount}", "1")).send(player);
                } else if (clickType.name().equals(configContext.get("click.buy-64", String.class))) {
                    if (!shopData.hasStock(currentPage, slot) || shopData.getBuyPrice(currentPage, slot) == -1) {
                        msgContext.get(MessageType.ERROR, "cannotBuyThisItem").send(player);
                        return;
                    }

                    ItemStack originStack = shopData.getItem(currentPage, slot);

                    if (getEconomy().getBalance(player) < shopData.getBuyPrice(currentPage, slot)) {
                        msgContext.get(MessageType.ERROR, "moneyIsNotEnough").send(player);
                        return;
                    }
                    if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1) {
                        if (Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).noneMatch(s -> originStack.isSimilar(s) && s.getAmount() < s.getType().getMaxStackSize())) {
                            msgContext.get(MessageType.ERROR, "inventorySpaceIsNotEnough").send(player);
                            return;
                        }
                    }

                    int totalPurchased = 0;
                    for (int i = 0; i < 64; i++) {
                        if (totalPurchased > 64) return;
                        if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1
                                && Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).noneMatch(s -> originStack.isSimilar(s) && s.getAmount() < s.getType().getMaxStackSize()))
                            break;
                        if (!shopData.hasStock(currentPage, slot)) break;
                        if (getEconomy().getBalance(player) < shopData.getBuyPrice(currentPage, slot) * (totalPurchased + 1)) break;

                        player.getInventory().addItem(originStack);

                        if (shopData.getStock(currentPage, slot) != -1) shopData.setStock(currentPage, slot, shopData.getStock(currentPage, slot) - 1);
                        totalPurchased++;
                    }

                    getEconomy().withdrawPlayer(player, totalPurchased * shopData.getBuyPrice(currentPage, slot));


                    int finalTotalPurchased = totalPurchased;
                    msgContext.get(MessageType.NORMAL, "itemBuyed", msg -> msg.replace("{price}", String.valueOf(shopData.getBuyPrice(currentPage, slot) * finalTotalPurchased)).replace("{amount}", String.valueOf(finalTotalPurchased))).send(player);
                } else if (clickType.name().equals(configContext.get("click.sell", String.class))) {
                    if (shopData.getSellPrice(currentPage, slot) == -1) {
                        msgContext.get(MessageType.ERROR, "cannotSellThisItem").send(player);
                        return;
                    }

                    ItemStack originStack = shopData.getItem(currentPage, slot);

                    List<ItemStack> matches = Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).filter(originStack::isSimilar).collect(Collectors.toList());
                    if (matches.isEmpty()) {
                        msgContext.get(MessageType.ERROR, "noItemInInventory").send(player);
                        return;
                    }

                    for (int j = 0; j < 36; j++) {
                        ItemStack itemStack = player.getInventory().getItem(j);
                        if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                        if (!matches.contains(itemStack)) continue;
                        itemStack = itemStack.clone();

                        if (itemStack.getAmount() == 1) {
                            player.getInventory().setItem(j, null);
                        } else {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                            player.getInventory().setItem(j, itemStack);
                        }
                        break;
                    }

                    getEconomy().depositPlayer(player, shopData.getSellPrice(currentPage, slot));
                    if (shopData.getStock(currentPage, slot) != -1) shopData.setStock(currentPage, slot, shopData.getStock(currentPage, slot) + 1);
                    msgContext.get(MessageType.NORMAL, "itemSelled", msg -> msg.replace("{price}", String.valueOf(shopData.getSellPrice(currentPage, slot))).replace("{amount}", "1")).send(player);
                } else if (clickType.name().equals(configContext.get("click.sell-64", String.class))) {
                    if (shopData.getSellPrice(currentPage, slot) == -1) {
                        msgContext.get(MessageType.ERROR, "cannotSellThisItem").send(player);
                        return;
                    }

                    ItemStack originStack = shopData.getItem(currentPage, slot);

                    List<ItemStack> matches = Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).filter(stack -> originStack.isSimilar(stack)).collect(Collectors.toList());
                    if (matches.isEmpty()) {
                        msgContext.get(MessageType.ERROR, "noItemInInventory").send(player);
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

                    if (shopData.getStock(currentPage, slot) != -1)
                        shopData.setStock(currentPage, slot, shopData.getStock(currentPage, slot) + totalSelled.get());
                    getEconomy().depositPlayer(player, totalSelled.get() * shopData.getSellPrice(currentPage, slot));
                    msgContext.get(MessageType.NORMAL, "itemSelled", msg -> msg.replace("{price}", String.valueOf(totalSelled.get() * shopData.getSellPrice(currentPage, slot))).replace("{amount}", String.valueOf(totalSelled.get()))).send(player);
                } else return;

                event.getClickedInventory().setContents(shopData.getShopInv(currentPage).getContents());
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
                    case SHOP_ENABLED: {
                        shopData.setEnabled(false);
                        msgContext.get(MessageType.NORMAL, "shopDisabled").send(player);

                        event.getClickedInventory().setContents(shopData.getShopSettingInv().getContents());
                        break;
                    }

                    case SHOP_DISABLED: {
                        shopData.setEnabled(true);
                        msgContext.get(MessageType.NORMAL, "shopEnabled").send(player);

                        event.getClickedInventory().setContents(shopData.getShopSettingInv().getContents());
                        break;
                    }

                    case ITEM_SETTING: {
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.openInventory(shopData.getItemSettingInv(1));
                            invOpenMap.set(player, new Pair<>(InventoryType.ITEM_SETTING, shopData.getName() + "|" + 1));
                        }, 1);
                        break;
                    }

                    case ITEM_DETAIL_SETTING: {
                        Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                            player.openInventory(shopData.getItemDetailSettingInv(1));
                            invOpenMap.set(player, new Pair<>(InventoryType.ITEM_DETAIL_SETTING, shopData.getName() + "|" + 1));
                        }, 1);
                        break;
                    }

                    case SET_NPC: {
                        if (clickType == ClickType.SHIFT_LEFT) {
                            //삭제
                            if (!shopData.hasNPC()) {
                                msgContext.get(MessageType.ERROR, "noNPC").send(player);
                                return;
                            }

                            npcMap.remove(shopData.getNPC());
                            shopData.setNPC(null);
                            msgContext.get(MessageType.NORMAL, "NPCDeleted").send(player);
                        } else {
                            //설정
                            player.closeInventory();
                            inputMap.set(player, new Pair<>(InputType.SET_NPC, new Pair<>(shopData.getName(), null)));

                            if (shopData.hasNPC()) npcMap.remove(shopData.getNPC());
                            msgContext.get(MessageType.NORMAL, "enterNPC").send(player);
                        }
                        break;
                    }

                    case MARKET_PRICE_ENABLED: {
                        shopData.setMarketPriceEnabled(false);
                        msgContext.get(MessageType.NORMAL, "marketPriceDisabled").send(player);

                        event.getClickedInventory().setContents(shopData.getShopSettingInv().getContents());
                        break;
                    }

                    case MARKET_PRICE_DISABLED: {
                        shopData.setMarketPriceEnabled(true);
                        msgContext.get(MessageType.NORMAL, "marketPriceEnabled").send(player);

                        event.getClickedInventory().setContents(shopData.getShopSettingInv().getContents());
                        break;
                    }
                }
                break;
            }

            case ITEM_DETAIL_SETTING: {
                if (event.getClickedInventory() == player.getInventory()) return;

                if (clickType == ClickType.RIGHT) {
                    //구매가격
                    invOpenMap.remove(player);
                    player.closeInventory();

                    Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                        msgContext.get(MessageType.NORMAL, "enterBuyPrice").send(player);
                        inputMap.set(player, new Pair<>(InputType.ORIGIN_PRICE_BUY, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    }, 1);
                } else if (clickType == ClickType.LEFT) {
                    //판매가격
                    invOpenMap.remove(player);
                    player.closeInventory();

                    Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                        msgContext.get(MessageType.NORMAL, "enterSellPrice").send(player);
                        inputMap.set(player, new Pair<>(InputType.ORIGIN_PRICE_SELL, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    }, 1);
                } else if (clickType == ClickType.SHIFT_LEFT) {
                    //삭제
                    shopData.setItem(currentPage, slot, null);
                    event.getClickedInventory().setItem(slot, null);
                } else if (clickType == ClickType.DROP) {
                    //재고 추가
                    shopData.setStock(currentPage, slot, shopData.getStock(currentPage, slot) + 1);
                    event.getClickedInventory().setContents(shopData.getItemDetailSettingInv(currentPage).getContents());

                    msgContext.get(MessageType.NORMAL, "stockAdded").send(player);
                } else if (clickType == ClickType.SHIFT_RIGHT) {
                    // 재고 설정
                    invOpenMap.remove(player);
                    player.closeInventory();

                    Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                        msgContext.get(MessageType.NORMAL, "enterStock").send(player);
                        inputMap.set(player, new Pair<>(InputType.STOCK, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    }, 1);
                } else if (clickType == ClickType.NUMBER_KEY) {
                    int key = event.getHotbarButton();
                    if (key < 0 || 6 < key) return;

                    if (key == 4) {
                        // 시세 초기화
                        int originSell = shopData.getOriginSellPrice(currentPage, slot);
                        shopData.setSellPrice(currentPage, slot, originSell);
                        shopData.setMinSellPrice(currentPage, slot, originSell);
                        shopData.setMaxSellPrice(currentPage, slot, originSell);

                        int originBuy = shopData.getOriginBuyPrice(currentPage, slot);
                        shopData.setBuyPrice(currentPage, slot, originBuy);
                        shopData.setMinBuyPrice(currentPage, slot, originBuy);
                        shopData.setMaxBuyPrice(currentPage, slot, originBuy);

                        event.getClickedInventory().setContents(shopData.getItemDetailSettingInv(currentPage).getContents());

                        break;
                    }

                    invOpenMap.remove(player);
                    player.closeInventory();
                    if (key == 0) {
                        //최소 판매 시세 설정
                        msgContext.get(MessageType.NORMAL, "enterMarketPrice_MinSell").send(player);
                        inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_MIN_SELL, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    } else if (key == 1) {
                        //최대 판매 시세 설정
                        msgContext.get(MessageType.NORMAL, "enterMarketPrice_MaxSell").send(player);
                        inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_MAX_SELL, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    } else if (key == 2) {
                        //최소 구매 시세 설정
                        msgContext.get(MessageType.NORMAL, "enterMarketPrice_MinBuy").send(player);
                        inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_MIN_BUY, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    } else if (key == 3) {
                        //최대 구매 시세 설정
                        msgContext.get(MessageType.NORMAL, "enterMarketPrice_MaxBuy").send(player);
                        inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_MAX_BUY, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    } else if (key == 5) {
                        //판매 시세 설정
                        msgContext.get(MessageType.ERROR, "enterMarketPrice_Sell").send(player);
                        inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_SELL, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    } else if (key == 6) {
                        //구매 시세 설정
                        msgContext.get(MessageType.ERROR, "enterMarketPrice_Buy").send(player);
                        inputMap.set(player, new Pair<>(InputType.MARKET_PRICE_BUY, new Pair<>(shopData.getName(), new Pair<>(currentPage, slot))));
                    }
                }

                break;
            }
        }
    }
}
