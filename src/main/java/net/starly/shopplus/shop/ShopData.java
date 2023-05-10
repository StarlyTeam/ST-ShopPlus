package net.starly.shopplus.shop;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.starly.core.data.Config;
import net.starly.shopplus.ShopPlusMain;
import net.starly.shopplus.context.ConfigContext;
import net.starly.shopplus.enums.ButtonType;
import net.starly.shopplus.util.GUIStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class ShopData {

    private final File configFile;
    private final FileConfiguration config;

    protected ShopData(File configFile) {
        this.configFile = configFile;
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    /* Config
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /* Enable
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public String getName() {
        return configFile.getName().replace(".yml", "");
    }

    public boolean isEnabled() {
        return config.getBoolean("shop.enabled");
    }

    public void setEnabled(boolean enabled) {
        config.set("shop.enabled", enabled);
    }

    public boolean isMarketPriceEnabled() {
        return config.getBoolean("shop.marketPrice");
    }

    public void setMarketPriceEnabled(boolean enabled) {
        config.set("shop.marketPrice", enabled);
    }


    /* Sell
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean isSellable(int slot) {
        return getSellPrice(slot) != -1;
    }

    public int getSellPrice(int slot) {
        if (isMarketPriceEnabled()) return config.getInt("shop.prices." + slot + ".sell.now");
        else return getOriginSellPrice(slot);
    }

    public void setSellPrice(int slot, int price) {
        config.set("shop.prices." + slot + ".sell.now", price);
    }

    public int getOriginSellPrice(int slot) {
        return config.getInt("shop.prices." + slot + ".sell.origin");
    }

    public void setOriginSellPrice(int slot, int price) {
        config.set("shop.prices." + slot + ".sell.origin", price);
    }

    public int getMinSellPrice(int slot) {
        return config.getInt("shop.prices." + slot + ".sell.min");
    }

    public void setMinSellPrice(int slot, int price) {
        config.set("shop.prices." + slot + ".sell.min", price);
    }

    public int getMaxSellPrice(int slot) {
        return config.getInt("shop.prices." + slot + ".sell.max");
    }

    public void setMaxSellPrice(int slot, int price) {
        config.set("shop.prices." + slot + ".sell.max", price);
    }


    /* Buy
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean isBuyable(int slot) {
        return getBuyPrice(slot) != -1;
    }

    public int getBuyPrice(int slot) {
        if (isMarketPriceEnabled()) return config.getInt("shop.prices." + slot + ".buy.now");
        else return getOriginBuyPrice(slot);
    }

    public void setBuyPrice(int slot, int price) {
        config.set("shop.prices." + slot + ".buy.now", price);
    }

    public int getOriginBuyPrice(int slot) {
        return config.getInt("shop.prices." + slot + ".buy.origin");
    }

    public void setOriginBuyPrice(int slot, int price) {
        config.set("shop.prices." + slot + ".buy.origin", price);
    }

    public int getMinBuyPrice(int slot) {
        return config.getInt("shop.prices." + slot + ".buy.min");
    }

    public void setMinBuyPrice(int slot, int price) {
        config.set("shop.prices." + slot + ".buy.min", price);
    }

    public int getMaxBuyPrice(int slot) {
        return config.getInt("shop.prices." + slot + ".buy.max");
    }

    public void setMaxBuyPrice(int slot, int price) {
        config.set("shop.prices." + slot + ".buy.max", price);
    }


    /* Stock
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean hasStock(int slot) {
        return getStock(slot) == -1 || getStock(slot) > 0;
    }

    public int getStock(int slot) {
        return config.getInt("shop.stocks." + slot);
    }

    public void setStock(int slot, int stock) {
        config.set("shop.stocks." + slot, stock);
    }


    /* Item
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public Map<Integer, ItemStack> getItems() {
        Object obj = config.get("shop.items");
        if (obj == null) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
                boos.writeObject(new HashMap<>());
                config.set("shop.items", bos.toByteArray());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return new HashMap<>();
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) obj); BukkitObjectInputStream bois = new BukkitObjectInputStream(bis)) {
            return (Map<Integer, ItemStack>) bois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    public void setItems(Map<Integer, ItemStack> items) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
            boos.writeObject(items);
            config.set("shop.items", bos.toByteArray());


            items.forEach((slot, itemStack) -> {
                if (itemStack == null) {
                    config.set("shop.items." + slot, null);
                    config.set("shop.prices." + slot, null);
                    config.set("shop.stocks." + slot, null);
                } else if (config.getInt("shop.prices." + slot + ".sell.now") == 0) {
                    config.set("shop.prices." + slot + ".sell.origin", -1);
                    config.set("shop.prices." + slot + ".sell.now", -1);
                    config.set("shop.prices." + slot + ".sell.min", -1);
                    config.set("shop.prices." + slot + ".sell.max", -1);
                    config.set("shop.prices." + slot + ".buy.origin", -1);
                    config.set("shop.prices." + slot + ".buy.now", -1);
                    config.set("shop.prices." + slot + ".buy.min", -1);
                    config.set("shop.prices." + slot + ".buy.max", -1);
                    config.set("shop.stocks." + slot, -1);
                }
            });

            config.save(configFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setItem(int slot, ItemStack itemStack) {
        Map<Integer, ItemStack> items = getItems();
        items.remove(slot);
        items.put(slot, itemStack);

        setItems(items);
    }


    /* Inventory
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public int getSize() {
        return config.getInt("shop.size");
    }

    public String getTitle() {
        return config.getString("shop.title");
    }

    public Inventory getShopInv() {
        Inventory inventory = Bukkit.createInventory(null, getSize(), getTitle());
        Map<Integer, ItemStack> items = getItems();

        ConfigContext configContext = ConfigContext.getInstance();
        String cannotBuy = configContext.get("text.cannotBuy", String.class);
        String cannotSell = configContext.get("text.cannotSell", String.class);
        String unlimited = configContext.get("text.unlimited", String.class);
        String soldOut = configContext.get("text.soldOut", String.class);

        items.forEach((slot, itemStack) -> {
            if (itemStack == null) return;

            int sellPrice = config.getInt("shop.prices." + slot + ".sell.now");
            int buyPrice = config.getInt("shop.prices." + slot + ".buy.now");
            int stock = config.getInt("shop.stocks." + slot);

            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
            if (!lore.isEmpty()) lore.add("§r");
            lore.addAll(((List<String>) configContext.get("lore.shopItem", List.class))
                    .stream()
                    .map(line ->
                            ChatColor
                                    .translateAlternateColorCodes('&', line)
                                    .replace("{sellPrice}", ChatColor
                                            .translateAlternateColorCodes('&',
                                                    (sellPrice != -1 ?
                                                            sellPrice : cannotSell) + ""))
                                    .replace("{buyPrice}", ChatColor
                                            .translateAlternateColorCodes('&',
                                                    (buyPrice != -1 ?
                                                            buyPrice : cannotBuy) + ""))
                                    .replace("{stock}", ChatColor
                                            .translateAlternateColorCodes('&',
                                                    stock != 0 ?
                                                            (stock == -1 ? unlimited : stock + "")
                                                            : soldOut)))
                    .collect(Collectors.toList()));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            inventory.setItem(slot, itemStack);
        });

        return inventory;
    }

    public Inventory getShopSettingInv() {
        Inventory inventory = Bukkit.createInventory(null, 27, getTitle() + "§r [상점 편집]");

        inventory.setItem(11, isEnabled() ? GUIStackUtil.getButton(ButtonType.SHOP_ENABLED) : GUIStackUtil.getButton(ButtonType.SHOP_DISABLED));
        inventory.setItem(12, isMarketPriceEnabled() ? GUIStackUtil.getButton(ButtonType.MARKET_PRICE_ENABLED) : GUIStackUtil.getButton(ButtonType.MARKET_PRICE_DISABLED));
        inventory.setItem(13, GUIStackUtil.getButton(ButtonType.ITEM_SETTING));
        inventory.setItem(14, GUIStackUtil.getButton(ButtonType.ITEM_DETAIL_SETTING));
        inventory.setItem(15, GUIStackUtil.getButton(ButtonType.SET_NPC));

        return inventory;
    }

    public Inventory getItemSettingInv() {

        Inventory inventory = Bukkit.createInventory(null, getSize(), getTitle() + "§r [아이템 설정]");
        getItems().forEach(inventory::setItem);

        return inventory;
    }

    public Inventory getItemDetailSettingInv() {
        Inventory inventory = Bukkit.createInventory(null, getSize(), getTitle() + "§r [아이템 세부설정]");
        Map<Integer, ItemStack> items = getItems();

        ConfigContext configContext = ConfigContext.getInstance();
        String cannotBuy = configContext.get("text.cannotBuy", String.class);
        String cannotSell = configContext.get("text.cannotSell", String.class);
        String unlimited = configContext.get("text.unlimited", String.class);

        items.forEach((slot, itemStack) -> {
            if (itemStack == null) return;

            int originSellPrice = config.getInt("shop.prices." + slot + ".sell.origin");
            int originBuyPrice = config.getInt("shop.prices." + slot + ".buy.origin");
            int sellPrice = config.getInt("shop.prices." + slot + ".sell.now");
            int buyPrice = config.getInt("shop.prices." + slot + ".buy.now");

            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = Arrays.asList("§r§7---------------------------------------",
                    "§r§e› §f구매가격 : " + (originBuyPrice == -1 ?
                            ChatColor.translateAlternateColorCodes('&', cannotBuy)
                            : "§6" + originBuyPrice + (isMarketPriceEnabled() ? " §7(시세 : §6" + buyPrice + "§7)" : "")),
                    "§r§e› §f판매가격 : " + (originSellPrice == -1 ?
                            ChatColor.translateAlternateColorCodes('&', cannotSell)
                            : "§6" + originSellPrice + (isMarketPriceEnabled() ? " §7(시세 : §6" + sellPrice + "§7)" : "")),
                    "§r§e› §f재고 : " + (hasStock(slot) ?
                            (getStock(slot) == -1 ?
                                    ChatColor.translateAlternateColorCodes('&', unlimited)
                                    : "§6" + getStock(slot) + "개")
                            : ChatColor.translateAlternateColorCodes('&', configContext.get("text.soldOut", String.class))),
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
                    "§r§e› §f시세 설정 초기화 : [5]",
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
    public String getNPC() {
        return config.getString("shop.npc");
    }

    public void setNPC(String name) {
        config.set("shop.npc", name);
    }

    public boolean hasNPC() {
        return getNPC() != null;
    }
}
