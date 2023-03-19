package net.starly.shop.shop;

import net.starly.core.data.Config;
import net.starly.core.data.ConfigSection;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.enums.ButtonType;
import net.starly.shop.util.GUIStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class ShopData {
    private final Config config;

    protected ShopData(Config config) {
        this.config = config;
    }

    public boolean exists() {
        return getConfig().isFileExist();
    }

    public Config getConfig() {
        config.reloadConfig();
        return config;
    }


    /* Enable
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean isEnabled() {
        return getConfig().getBoolean("shop.enabled");
    }

    public void setEnabled(boolean enabled) {
        getConfig().setBoolean("shop.enabled", enabled);
    }

    public boolean isMarketPriceEnabled() {
        return getConfig().getBoolean("shop.marketPrice");
    }

    public void setMarketPriceEnabled(boolean enabled) {
        getConfig().setBoolean("shop.marketPrice", enabled);
    }


    /* Sell
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean isSellable(int slot) {
        return getSellPrice(slot) != -1;
    }

    public int getSellPrice(int slot) {
        if (isMarketPriceEnabled()) return getConfig().getInt("shop.prices." + slot + ".sell.now");
        else return getOriginSellPrice(slot);
    }

    public void setSellPrice(int slot, int price) {
        getConfig().setInt("shop.prices." + slot + ".sell.now", price);
    }

    public int getOriginSellPrice(int slot) {
        return getConfig().getInt("shop.prices." + slot + ".sell.origin");
    }

    public void setOriginSellPrice(int slot, int price) {
        getConfig().setInt("shop.prices." + slot + ".sell.origin", price);
    }

    public int getMinSellPrice(int slot) {
        return getConfig().getInt("shop.prices." + slot + ".sell.min");
    }

    public void setMinSellPrice(int slot, int price) {
        getConfig().setInt("shop.prices." + slot + ".sell.min", price);
    }

    public int getMaxSellPrice(int slot) {
        return getConfig().getInt("shop.prices." + slot + ".sell.max");
    }

    public void setMaxSellPrice(int slot, int price) {
        getConfig().setInt("shop.prices." + slot + ".sell.max", price);
    }


    /* Buy
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean isBuyable(int slot) {
        return getBuyPrice(slot) != -1;
    }

    public int getBuyPrice(int slot) {
        if (isMarketPriceEnabled()) return getConfig().getInt("shop.prices." + slot + ".buy.now");
        else return getOriginBuyPrice(slot);
    }

    public void setBuyPrice(int slot, int price) {
        getConfig().setInt("shop.prices." + slot + ".buy.now", price);
    }

    public int getOriginBuyPrice(int slot) {
        return getConfig().getInt("shop.prices." + slot + ".buy.origin");
    }

    public void setOriginBuyPrice(int slot, int price) {
        getConfig().setInt("shop.prices." + slot + ".buy.origin", price);
    }

    public int getMinBuyPrice(int slot) {
        return getConfig().getInt("shop.prices." + slot + ".buy.min");
    }

    public void setMinBuyPrice(int slot, int price) {
        getConfig().setInt("shop.prices." + slot + ".buy.min", price);
    }

    public int getMaxBuyPrice(int slot) {
        return getConfig().getInt("shop.prices." + slot + ".buy.max");
    }

    public void setMaxBuyPrice(int slot, int price) {
        getConfig().setInt("shop.prices." + slot + ".buy.max", price);
    }


    /* Stock
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean hasStock(int slot) {
        return getStock(slot) == -1 || getStock(slot) > 0;
    }

    public int getStock(int slot) {
        return getConfig().getInt("shop.stocks." + slot);
    }

    public void setStock(int slot, int stock) {
        getConfig().setInt("shop.stocks." + slot, stock);
    }


    /* Item
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public ItemStack getItem(int slot) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) getConfig().getObject("shop.items." + slot)); BukkitObjectInputStream bois = new BukkitObjectInputStream(bis)) {
            return (ItemStack) bois.readObject();
        } catch (NullPointerException ex) {
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void setItem(int slot, ItemStack itemStack) {
        if (itemStack == null) {
            getConfig().setObject("shop.items." + slot, null);
            getConfig().setObject("shop.prices." + slot, null);
            getConfig().setObject("shop.stocks." + slot, null);
            return;
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
            boos.writeObject(itemStack);
            getConfig().setObject("shop.items." + slot, bos.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (getSellPrice(slot) == 0) setSellPrice(slot, -1);
        if (getOriginSellPrice(slot) == 0) setOriginBuyPrice(slot, -1);
        if (getMinSellPrice(slot) == 0) setMinSellPrice(slot, -1);
        if (getMaxSellPrice(slot) == 0) setMaxSellPrice(slot, -1);

        if (getBuyPrice(slot) == 0) setBuyPrice(slot, -1);
        if (getOriginBuyPrice(slot) == 0) setOriginSellPrice(slot, -1);
        if (getMinBuyPrice(slot) == 0) setMinBuyPrice(slot, -1);
        if (getMaxBuyPrice(slot) == 0) setMaxBuyPrice(slot, -1);

        if (getStock(slot) == 0) setStock(slot, 0);
    }


    /* Inventory
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public int getSize() {
        return getConfig().getInt("shop.size");
    }

    public String getTitle() {
        return getConfig().getString("shop.title");
    }

    public Inventory getShopInv() {
        getConfig().reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, getSize(), getTitle());

        Config mainConfig = ConfigContent.getInstance().getConfig();
        ConfigSection itemsSection = getConfig().getSection("shop.items");
        itemsSection.getKeys().forEach(key -> {
            int slot = Integer.parseInt(key);
            int sellPrice = getSellPrice(slot);
            int buyPrice = getBuyPrice(slot);

            ItemStack itemStack = getItem(slot);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
            if (!lore.isEmpty()) lore.add("§r");
            lore.addAll(ConfigContent
                    .getInstance()
                    .getConfig()
                    .getStringList("lore.shopItem")
                    .stream()
                    .map(line ->
                            ChatColor
                                    .translateAlternateColorCodes('&', line)
                                    .replace("{sellPrice}", ChatColor
                                            .translateAlternateColorCodes('&',
                                                    (isSellable(slot) ?
                                                            sellPrice : mainConfig.getString("text.cannotSell")) + ""))
                                    .replace("{buyPrice}", ChatColor
                                            .translateAlternateColorCodes('&',
                                                    (isBuyable(slot) ?
                                                            buyPrice : mainConfig.getString("text.cannotBuy")) + ""))
                                    .replace("{stock}", ChatColor
                                            .translateAlternateColorCodes('&',
                                                    hasStock(slot) ?
                                                            (getStock(slot) == -1 ? mainConfig.getString("text.unlimited") : getStock(slot) + "")
                                                            : mainConfig.getString("text.soldOut"))))
                    .collect(Collectors.toList()));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            inventory.setItem(slot, itemStack);
        });

        return inventory;
    }

    public Inventory getShopSettingInv() {
        getConfig().reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, 27, getTitle() + "§r [상점 편집]");
        inventory.setItem(11, isEnabled() ? GUIStackUtil.getButton(ButtonType.SHOP_ENABLED) : GUIStackUtil.getButton(ButtonType.SHOP_DISABLED));
        inventory.setItem(12, isMarketPriceEnabled() ? GUIStackUtil.getButton(ButtonType.MARKET_PRICE_ENABLED) : GUIStackUtil.getButton(ButtonType.MARKET_PRICE_DISABLED));
        inventory.setItem(13, GUIStackUtil.getButton(ButtonType.ITEM_SETTING));
        inventory.setItem(14, GUIStackUtil.getButton(ButtonType.ITEM_DETAIL_SETTING));
        inventory.setItem(15, GUIStackUtil.getButton(ButtonType.SET_NPC));

        return inventory;
    }

    public Inventory getItemSettingInv() {
        getConfig().reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, getSize(), getTitle() + "§r [아이템 설정]");

        ConfigSection itemsSection = getConfig().getSection("shop.items");
        itemsSection.getKeys().forEach(key -> inventory.setItem(Integer.parseInt(key), getItem(Integer.parseInt(key))));

        return inventory;
    }

    public Inventory getItemDetailSettingInv() {
        getConfig().reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, getSize(), getTitle() + "§r [아이템 세부설정]");

        Config mainConfig = ConfigContent.getInstance().getConfig();
        ConfigSection itemsSection = getConfig().getSection("shop.items");
        itemsSection.getKeys().forEach(key -> {
            int slot = Integer.parseInt(key);
            int originSellPrice = getOriginSellPrice(slot);
            int originBuyPrice = getOriginBuyPrice(slot);
            int sellPrice = getSellPrice(slot);
            int buyPrice = getBuyPrice(slot);

            ItemStack itemStack = getItem(slot);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = Arrays.asList("§r§7---------------------------------------",
                    "§r§e› §f구매가격 : " + (originBuyPrice == -1 ?
                            ChatColor.translateAlternateColorCodes('&', mainConfig.getString("text.cannotBuy"))
                            : "§6" + originBuyPrice + (isMarketPriceEnabled() ? " §7(시세 : §6" + buyPrice + "§7)" : "")),
                    "§r§e› §f판매가격 : " + (originSellPrice == -1 ?
                            ChatColor.translateAlternateColorCodes('&', mainConfig.getString("text.cannotSell"))
                            : "§6" + originSellPrice + (isMarketPriceEnabled() ? " §7(시세 : §6" + sellPrice + "§7)" : "")),
                    "§r§e› §f재고 : " + (hasStock(slot) ?
                            (getStock(slot) == -1 ?
                                    ChatColor.translateAlternateColorCodes('&', mainConfig.getString("text.unlimited"))
                                    : "§6" + getStock(slot) + "개")
                            : ChatColor.translateAlternateColorCodes('&', mainConfig.getString("text.soldOut"))),
                    "§r§7---------------------------------------",
                    "§r§e› §f판매가격 설정 : 좌클릭",
                    "§r§e› §f구매가격 설정 : 우클릭",
                    "§r§e› §f슬롯 상품 삭제 : Shift + 좌클릭",
                    "§r§e› §f잔여재고 설정 : Shift + 우클릭",
                    "§r§e› §f잔여재고 추가 : Q",
                    "§r",
                    "§r§e› §f최소 판매시세 설정 : [1]",
                    "§r§e› §f최대 판매시세 설정 : [2]",
                    "§r§e› §f최소 구매시세 설정 : [3]",
                    "§r§e› §f최대 구매시세 설정 : [4]",
                    "§r§e› §f현재 시세 초기화 : [5]",
                    "§r§e› §f현재 판매시세 설정 : [6]",
                    "§r§e› §f현재 구매시세 설정 : [7]",
                    "§r§7---------------------------------------");
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            inventory.setItem(slot, itemStack);
        });

        return inventory;
    }


    /* NPC
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public Entity getNPC() {
        String uuid = getConfig().getString("shop.npc");
        return uuid.equals("<none>") ? null : getServer().getEntity(UUID.fromString(uuid));
    }

    public void setNPC(Entity entity) {
        getConfig().setString("shop.npc", entity == null ? "<none>" : entity.getUniqueId() + "");
    }

    public boolean hasNPC() {
        return getNPC() != null;
    }
}
