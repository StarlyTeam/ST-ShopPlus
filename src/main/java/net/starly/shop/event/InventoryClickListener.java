package net.starly.shop.event;

import lombok.AllArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.core.jb.version.nms.tank.NmsItemStackUtil;
import net.starly.core.jb.version.nms.wrapper.ItemStackWrapper;
import net.starly.core.jb.version.nms.wrapper.NBTTagCompoundWrapper;
import net.starly.core.util.InventoryUtil;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.ChatInputMap;
import net.starly.shop.data.InventoryOpenMap;
import net.starly.shop.enums.ButtonType;
import net.starly.shop.enums.ChatInputType;
import net.starly.shop.shop.ShopData;
import net.starly.shop.enums.InventoryOpenType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static net.starly.shop.ShopMain.*;

@AllArgsConstructor
public class InventoryClickListener implements Listener {
    private final InventoryOpenMap inventoryOpenMap;
    private final Economy economy;
    private final ChatInputMap chatInputMap;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (player == null) return;
        if (!inventoryOpenMap.has(player)) return;

        ItemStack currentStack = event.getCurrentItem();
        int slot = event.getSlot();
        Config config = ConfigContent.getInstance().getConfig();
        ClickType clickType = event.getClick();
        InventoryOpenType openType = inventoryOpenMap.get(player).getFirst();
        ShopData shopData = inventoryOpenMap.get(player).getSecond();
        event.setCancelled(true);

        if (openType != InventoryOpenType.ITEM_SETTING && currentStack == null) return;

        switch (openType) {
            case SHOP: {
                if (event.getClickedInventory() == player.getInventory()) return;

                if (clickType.name().equals(config.getString("click.buy"))) {
                    if (shopData.getBuyPrice(slot) == -1) {
                        player.sendMessage("해당 상품은 구매하실 수 없습니다.");
                        return;
                    }

                    ItemStack originStack = shopData.getItem(slot);

                    if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1) {
                        player.sendMessage("인벤토리 공간이 부족합니다.");
                        return;
                    }

                    if (economy.getBalance(player) < shopData.getBuyPrice(slot)) {
                        player.sendMessage("돈이 부족합니다.");
                        return;
                    }

                    economy.withdrawPlayer(player, shopData.getBuyPrice(slot));
                    player.getInventory().addItem(originStack);
                    player.sendMessage("§a§n§l" + shopData.getBuyPrice(slot) + "§f원을 지불하고 §a§n§l" + originStack.getAmount() + "개§f를 구매했습니다.");
                } else if (clickType.name().equals(config.getString("click.buy-64"))) {
                    if (shopData.getBuyPrice(slot) == -1) {
                        player.sendMessage("해당 상품은 구매하실 수 없습니다.");
                        return;
                    }

                    ItemStack originStack = shopData.getItem(slot);

                    if (economy.getBalance(player) < shopData.getBuyPrice(slot)) {
                        player.sendMessage("돈이 부족합니다.");
                        return;
                    } else if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1) {
                        player.sendMessage("인벤토리 공간이 부족합니다.");
                        return;
                    }

                    int totalPurchased = 0;
                    for (int i = 0; i < 64; i++) {
                        if (InventoryUtil.getSpace(player.getInventory()) - 5 < 1) break;
                        if (economy.getBalance(player) < shopData.getBuyPrice(slot) * (totalPurchased + 1)) break;

                        player.getInventory().addItem(originStack);
                        totalPurchased++;
                    }

                    if (totalPurchased != 64) {
                        player.sendMessage("인벤토리 공간이 부족하거나 돈이 부족하여 " + totalPurchased + "개만 구매했습니다.");
                    }

                    economy.withdrawPlayer(player, totalPurchased * shopData.getBuyPrice(slot));
                    player.sendMessage("§a§n§l" + totalPurchased * shopData.getBuyPrice(slot) + "§f원을 지불하고 §a§n§l" + totalPurchased + "개§f를 구매했습니다.");
                } else if (clickType.name().equals(config.getString("click.sell"))) {
                    if (shopData.getSellPrice(slot) == -1) {
                        player.sendMessage("해당 상품은 판매하실 수 없습니다.");
                        return;
                    }

                    ItemStack originStack = shopData.getItem(slot);

                    if (Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).noneMatch(originStack::equals)) {
                        player.sendMessage("인벤토리에 아이템이 부족합니다.");
                        return;
                    }

                    for (int i = 0; i < 36; i++) {
                        ItemStack itemStack = player.getInventory().getItem(i);
                        if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                        if (itemStack.getType() != originStack.getType()) continue;

                        if (itemStack.getAmount() == 1) {
                            player.getInventory().setItem(i, null);
                        } else {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                            player.getInventory().setItem(i, itemStack);
                        }
                        break;
                    }

                    economy.depositPlayer(player, shopData.getSellPrice(slot));
                    player.sendMessage("§a§n§l" + shopData.getSellPrice(slot) + "§f원을 지급받고 §a§n§l1개§f를 판매했습니다.");
                } else if (clickType.name().equals(config.getString("click.sell-64"))) {
                    if (shopData.getSellPrice(slot) == -1) {
                        player.sendMessage("해당 상품은 판매하실 수 없습니다.");
                        return;
                    }

                    ItemStack originStack = shopData.getItem(slot);

                    if (Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).noneMatch(originStack::equals)) {
                        player.sendMessage("인벤토리에 아이템이 부족합니다.");
                        return;
                    }

                    AtomicInteger totalSelled = new AtomicInteger();
                    Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).filter(originStack::equals).forEach(s -> totalSelled.addAndGet(s.getAmount()));
                    if (totalSelled.get() < 64) player.sendMessage("인벤토리에 아이템이 부족하여 " + totalSelled + "개만 판매했습니다.");
                    else totalSelled.set(64);

                    int totalRemoved = 0;
                    for (int i = 0; i < 36; i++) {
                        if (totalSelled.get() == totalRemoved) break;

                        ItemStack itemStack = player.getInventory().getItem(i);
                        if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                        if (itemStack.getType() != originStack.getType()) continue;


                        if (itemStack.getAmount() == 64) {
                            player.getInventory().setItem(i, null);
                            totalRemoved += 64;
                        } else if (itemStack.getAmount() == 1) {
                            player.getInventory().setItem(i, null);
                            totalRemoved++;
                        } else {
                            if (itemStack.getAmount() <= (totalSelled.get() - totalRemoved)) {
                                totalRemoved += itemStack.getAmount();
                                itemStack.setAmount(0);
                            } else {
                                totalRemoved += (totalSelled.get() - totalRemoved);
                                itemStack.setAmount(itemStack.getAmount() - (totalSelled.get() - totalRemoved));
                            }
                        }
                    }

                    economy.depositPlayer(player, totalSelled.get() * shopData.getSellPrice(slot));
                    player.sendMessage("§a§n§l" + totalSelled.get() * shopData.getSellPrice(slot) + "§f원을 지급받고 §a§n§l" + totalSelled + "개§f를 판매했습니다.");
                }

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
                        player.sendMessage("상점을 §c§n§l비활성화§f 했습니다.");

                        Bukkit.getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                            player.openInventory(shopData.getShopSettingInv());
                            inventoryOpenMap.set(player, new Pair<>(InventoryOpenType.SHOP_SETTING, shopData));
                        }, 1);
                        break;
                    }

                    case SHOP_DISABLED: {
                        shopData.setEnabled(true);
                        player.sendMessage("상점을 §a§n§l활성화§f 했습니다.");

                        Bukkit.getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                            player.openInventory(shopData.getShopSettingInv());
                            inventoryOpenMap.set(player, new Pair<>(InventoryOpenType.SHOP_SETTING, shopData));
                        }, 1);
                        break;
                    }

                    case ITEM_SETTING: {
                        Bukkit.getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                            player.openInventory(shopData.getItemSettingInv());
                            inventoryOpenMap.set(player, new Pair<>(InventoryOpenType.ITEM_SETTING, shopData));
                        }, 1);
                        break;
                    }

                    case ITEM_DETAIL_SETTING: {
                        Bukkit.getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                            player.openInventory(shopData.getItemDetailSettingInv());
                            inventoryOpenMap.set(player, new Pair<>(InventoryOpenType.ITEM_DETAIL_SETTING, shopData));
                        }, 1);
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

                if (clickType == ClickType.LEFT) {
                    //판매가격
                    Bukkit.getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                        inventoryOpenMap.remove(player);
                        player.closeInventory();
                        chatInputMap.set(player, new Pair<>(ChatInputType.SELL_PRICE, new Pair<>(shopData, slot)));
                        player.sendMessage("변경할 판매가격을 채팅창에 입력해주세요. (판매하지 못하게 하려면 -1을 입력해주세요.)");
                    }, 1);
                } else if (clickType == ClickType.RIGHT) {
                    //구매가격
                    Bukkit.getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                        inventoryOpenMap.remove(player);
                        player.closeInventory();
                        chatInputMap.set(player, new Pair<>(ChatInputType.BUY_PRICE, new Pair<>(shopData, slot)));
                        player.sendMessage("변경할 구매가격을 채팅창에 입력해주세요. (구매하지 못하게 하려면 -1을 입력해주세요.)");
                    }, 1);
                } else if (clickType.isShiftClick()) {
                    //삭제
                    shopData.setItem(slot, null);
                    event.getClickedInventory().setItem(slot, null);
                }

                break;
            }
        }
    }
}
