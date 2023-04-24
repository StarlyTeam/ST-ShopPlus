package net.starly.shopplus.shop;

import net.starly.core.data.Config;
import net.starly.shopplus.context.ConfigContent;
import net.starly.shopplus.enums.ButtonType;
import net.starly.shopplus.util.GUIStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class ShopData {
    private final Config config;

    protected ShopData(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        config.reloadConfig();
        return config;
    }


    /* Enable
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public String getName() {
        return config.getConfig().getName().replace(".yml", "");
    }

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
    public Map<Integer, ItemStack> getItems() {
        Object obj = getConfig().getObject("shop.items");
        if (obj == null) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
                boos.writeObject(new HashMap<>());
                getConfig().setObject("shop.items", bos.toByteArray());
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
            config.setObject("shop.items", bos.toByteArray());


            FileConfiguration config_ = getConfig().getConfig();
            items.forEach((slot, itemStack) -> {
                if (itemStack == null) {
                    config_.set("shop.items." + slot, null);
                    config_.set("shop.prices." + slot, null);
                    config_.set("shop.stocks." + slot, null);
                } else if (config_.getInt("shop.prices." + slot + ".sell.now") == 0) {
                    config_.set("shop.prices." + slot + ".sell.origin", -1);
                    config_.set("shop.prices." + slot + ".sell.now", -1);
                    config_.set("shop.prices." + slot + ".sell.min", -1);
                    config_.set("shop.prices." + slot + ".sell.max", -1);
                    config_.set("shop.prices." + slot + ".buy.origin", -1);
                    config_.set("shop.prices." + slot + ".buy.now", -1);
                    config_.set("shop.prices." + slot + ".buy.min", -1);
                    config_.set("shop.prices." + slot + ".buy.max", -1);
                    config_.set("shop.stocks." + slot, 0);
                }
            });

            config.saveConfig();
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
        return getConfig().getInt("shop.size");
    }

    public String getTitle() {
        return getConfig().getString("shop.title");
    }

    public Inventory getShopInv() {
        Inventory inventory = Bukkit.createInventory(null, getSize(), getTitle());
        Map<Integer, ItemStack> items = getItems();

        Config mainConfig = ConfigContent.getInstance().getConfig();
        String cannotBuy = mainConfig.getString("text.cannotBuy");
        String cannotSell = mainConfig.getString("text.cannotSell");
        String unlimited = mainConfig.getString("text.unlimited");
        String soldOut = mainConfig.getString("text.soldOut");

        FileConfiguration config_ = getConfig().getConfig();

        items.forEach((slot, itemStack) -> {
            if (itemStack == null) return;

            int sellPrice = config_.getInt("shop.prices." + slot + ".sell.now");
            int buyPrice = config_.getInt("shop.prices." + slot + ".buy.now");
            int stock = config_.getInt("shop.stocks." + slot);

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

        Config mainConfig = ConfigContent.getInstance().getConfig();
        String cannotBuy = mainConfig.getString("text.cannotBuy");
        String cannotSell = mainConfig.getString("text.cannotSell");
        String unlimited = mainConfig.getString("text.unlimited");

        Config shopConfig = getConfig();
        items.forEach((slot, itemStack) -> {
            if (itemStack == null) return;

            int originSellPrice = shopConfig.getInt("shop.prices." + slot + ".sell.origin");
            int originBuyPrice = shopConfig.getInt("shop.prices." + slot + ".buy.origin");
            int sellPrice = shopConfig.getInt("shop.prices." + slot + ".sell.now");
            int buyPrice = shopConfig.getInt("shop.prices." + slot + ".buy.now");

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
    public Entity getNPC() {
        String uuid = getConfig().getString("shop.npc");
        if (uuid == null) return null;
        return uuid.equals("<none>") ? null : getServer().getEntity(UUID.fromString(uuid));
    }

    public void setNPC(Entity entity) {
        getConfig().setString("shop.npc", entity == null ? "<none>" : entity.getUniqueId() + "");
    }

    public boolean hasNPC() {
        return getNPC() != null;
    }
}
