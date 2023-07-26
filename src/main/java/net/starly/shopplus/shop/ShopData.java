package net.starly.shopplus.shop;

import net.starly.core.jb.util.Pair;
import net.starly.shopplus.context.ConfigContext;
import net.starly.shopplus.enums.ButtonType;
import net.starly.shopplus.enums.InventoryType;
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
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


public class ShopData {

    private final File configFile;
    private final FileConfiguration config;

    protected ShopData(File configFile) {
        this.configFile = configFile;
        this.config = YamlConfiguration.loadConfiguration(configFile);

        PREV_SLOT = getSize() - 6;
        NEXT_SLOT = getSize() - 4;
    }


    private final int PREV_SLOT;
    private final int NEXT_SLOT;


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

    /* Options
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
    public boolean isSellable(int page, int slot) {
        return getOriginSellPrice(page, slot) != -1;
    }

    public int getSellPrice(int page, int slot) {
        if (isMarketPriceEnabled()) return config.getInt("shop.prices." + page + "." + slot + ".sell.now");
        else return getOriginSellPrice(page, slot);
    }

    public void setSellPrice(int page, int slot, int price) {
        config.set("shop.prices." + page + "." + slot + ".sell.now", price);
    }

    public int getOriginSellPrice(int page, int slot) {
        return config.getInt("shop.prices." + page + "." + slot + ".sell.origin");
    }

    public void setOriginSellPrice(int page, int slot, int price) {
        config.set("shop.prices." + page + "." + slot + ".sell.origin", price);
    }

    public int getMinSellPrice(int page, int slot) {
        return config.getInt("shop.prices." + page + "." + slot + ".sell.min");
    }

    public void setMinSellPrice(int page, int slot, int price) {
        config.set("shop.prices." + page + "." + slot + ".sell.min", price);
    }

    public int getMaxSellPrice(int page, int slot) {
        return config.getInt("shop.prices." + page + "." + slot + ".sell.max");
    }

    public void setMaxSellPrice(int page, int slot, int price) {
        config.set("shop.prices." + page + "." + slot + ".sell.max", price);
    }


    /* Buy
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean isBuyable(int page, int slot) {
        return getOriginBuyPrice(page, slot) != -1;
    }

    public int getBuyPrice(int page, int slot) {
        if (isMarketPriceEnabled()) return config.getInt("shop.prices." + page + "." + slot + ".buy.now");
        else return getOriginBuyPrice(page, slot);
    }

    public void setBuyPrice(int page, int slot, int price) {
        config.set("shop.prices." + page + "." + slot + ".buy.now", price);
    }

    public int getOriginBuyPrice(int page, int slot) {
        return config.getInt("shop.prices." + page + "." + slot + ".buy.origin");
    }

    public void setOriginBuyPrice(int page, int slot, int price) {
        config.set("shop.prices." + page + "." + slot + ".buy.origin", price);
    }

    public int getMinBuyPrice(int page, int slot) {
        return config.getInt("shop.prices." + page + "." + slot + ".buy.min");
    }

    public void setMinBuyPrice(int page, int slot, int price) {
        config.set("shop.prices." + page + "." + slot + ".buy.min", price);
    }

    public int getMaxBuyPrice(int page, int slot) {
        return config.getInt("shop.prices." + page + "." + slot + ".buy.max");
    }

    public void setMaxBuyPrice(int page, int slot, int price) {
        config.set("shop.prices." + page + "." + slot + ".buy.max", price);
    }


    /* Stock
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public boolean hasStock(int page, int slot) {
        return getStock(page, slot) == -1 || getStock(page, slot) > 0;
    }

    public int getStock(int page, int slot) {
        return config.getInt("shop.stocks." + page + "." + slot);
    }

    public void setStock(int page, int slot, int stock) {
        config.set("shop.stocks." + page + "." + slot, stock);
    }


    /* Item
     ──────────────────────────────────────────────────────────────────────────────────────────────────────────────── */
    public int getMaxPage() {
        return getItems().keySet().stream().mapToInt(Pair::getFirst).max().orElse(0);
    }

    public ItemStack getItem(int page, int slot) {
        return getItems(page).get(slot);
    }

    public Map<Integer, ItemStack> getItems(int page) {
        List<Map.Entry<Pair<Integer, Integer>, ItemStack>> tempItems = new ArrayList<>(getItems().entrySet());
        tempItems.removeIf(value -> value.getKey().getFirst() != page);

        Map<Integer, ItemStack> items = new HashMap<>();
        tempItems.forEach(value -> items.put(value.getKey().getSecond(), value.getValue()));
        return items;
    }

    public Map<Pair<Integer, Integer>, ItemStack> getItems() {
        Object itemsData = config.get("shop.items");
        if (itemsData == null) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
                boos.writeObject(new HashMap<>());
                config.set("shop.items", bos.toByteArray());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return new HashMap<>();
        } else {
            try (ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) itemsData); BukkitObjectInputStream bois = new BukkitObjectInputStream(bis)) {
                return (Map<Pair<Integer, Integer>, ItemStack>) bois.readObject();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public void setItems(int page, Map<Integer, ItemStack> items) {
        if (items == null) {
            Map<Pair<Integer, Integer>, ItemStack> originItems = getItems();

            Map<Pair<Integer, Integer>, ItemStack> newItems = new HashMap<>();
            originItems.forEach((slotData, value) -> {
                int itemPage = slotData.getFirst();
                if (itemPage != page) newItems.put(slotData, value);
            });

            setItems(newItems);
        } else {
            Map<Pair<Integer, Integer>, ItemStack> newItems = getItems();
            items.forEach((slot, itemStack) -> newItems.put(new Pair<>(page, slot), itemStack));
            setItems(newItems);
        }
    }

    public void setItems(Map<Pair<Integer, Integer>, ItemStack> items) {
        Map<Pair<Integer, Integer>, ItemStack> filteredItems = new HashMap<>();
        items
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .peek(entry -> {
                    ItemStack itemStack = entry.getValue();
                    itemStack.setAmount(1);
                    entry.setValue(itemStack);
                })
                .forEach(entry -> filteredItems.put(entry.getKey(), entry.getValue()));
        items = filteredItems;


        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
            boos.writeObject(items);
            config.set("shop.items", bos.toByteArray());


            items.forEach((slotData, itemStack) -> {
                int page = slotData.getFirst();
                int slot = slotData.getSecond();

                if (!config.contains("shop.prices." + page + "." + slot)) {
                    setOriginSellPrice(page, slot, -1);
                    setSellPrice(page, slot, -1);
                    setMinSellPrice(page, slot, -1);
                    setMaxSellPrice(page, slot, -1);
                    setOriginBuyPrice(page, slot, -1);
                    setBuyPrice(page, slot, -1);
                    setMinBuyPrice(page, slot, -1);
                    setMaxBuyPrice(page, slot, -1);
                    setStock(page, slot, -1);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setItem(int page, int slot, ItemStack itemStack) {
        Map<Pair<Integer, Integer>, ItemStack> items = getItems();
        items.put(new Pair<>(page, slot), itemStack);
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

    public Inventory getShopInv(int page) {
        computeMaxPage();


        Inventory inventory = Bukkit.createInventory(null, getSize(), String.format("%s §r[%d]", getTitle(), page));
        Map<Integer, ItemStack> items = new HashMap<>();

        List<Map.Entry<Pair<Integer, Integer>, ItemStack>> tempItems = new ArrayList<>(getItems().entrySet());
        tempItems.removeIf(value -> value.getKey().getFirst() != page);
        tempItems.forEach(entry -> items.put(entry.getKey().getSecond(), entry.getValue()));

        ConfigContext configContext = ConfigContext.getInstance();
        String cannotBuy = configContext.get("text.cannotBuy", String.class);
        String cannotSell = configContext.get("text.cannotSell", String.class);
        String unlimited = configContext.get("text.unlimited", String.class);
        String soldOut = configContext.get("text.soldOut", String.class);

        items.forEach((slot, itemStack) -> {
            if (itemStack == null) return;

            if (!(slot == PREV_SLOT || slot == NEXT_SLOT)) {
                DecimalFormat decFormat = new DecimalFormat("###,###");

                int sellPrice = getSellPrice(page, slot);
                int buyPrice = getBuyPrice(page, slot);
                int stock = getStock(page, slot);

                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                if (!lore.isEmpty()) lore.add("§r");
                lore.addAll(((List<String>) configContext.get("lore.shopItem", List.class))
                        .stream()
                        .map(line ->
                                ChatColor
                                        .translateAlternateColorCodes('&', line)
                                        .replace("{sellPrice}", ChatColor
                                                .translateAlternateColorCodes('&', sellPrice != -1 ? decFormat.format(sellPrice) : cannotSell))
                                        .replace("{buyPrice}", ChatColor
                                                .translateAlternateColorCodes('&', buyPrice != -1 ? decFormat.format(buyPrice) : cannotBuy))
                                        .replace("{stock}", ChatColor
                                                .translateAlternateColorCodes('&', stock != 0 ? (stock == -1 ? unlimited : decFormat.format(stock)) : soldOut)))
                        .collect(Collectors.toList()));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
            }

            inventory.setItem(slot, itemStack);
        });

        if (inventory.getItem(PREV_SLOT) == null) inventory.setItem(PREV_SLOT, GUIStackUtil.getButton(ButtonType.PREV_PAGE));
        if (inventory.getItem(NEXT_SLOT) == null) inventory.setItem(NEXT_SLOT, GUIStackUtil.getButton(ButtonType.NEXT_PAGE));

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

    public Inventory getItemSettingInv(int page) {
        Inventory inventory = Bukkit.createInventory(null, getSize(), String.format("%s §r [아이템 설정: %d]", getTitle(), page));
        getItems(page).forEach(inventory::setItem);

        if (inventory.getItem(PREV_SLOT) == null) inventory.setItem(PREV_SLOT, GUIStackUtil.getButton(ButtonType.PREV_PAGE));
        if (inventory.getItem(NEXT_SLOT) == null) inventory.setItem(NEXT_SLOT, GUIStackUtil.getButton(ButtonType.NEXT_PAGE));

        return inventory;
    }

    public Inventory getItemDetailSettingInv(int page) {
        computeMaxPage();


        Inventory inventory = Bukkit.createInventory(null, getSize(), String.format("%s §r [아이템 세부설정: %d]", getTitle(), page));
        Map<Integer, ItemStack> items = getItems(page);

        ConfigContext configContext = ConfigContext.getInstance();
        final String cannotBuy = configContext.get("text.cannotBuy", String.class);
        final String cannotSell = configContext.get("text.cannotSell", String.class);
        final String unlimited = configContext.get("text.unlimited", String.class);
        final String soldOut = configContext.get("text.soldOut", String.class);

        items.forEach((slot, itemStack) -> {
            if (itemStack == null) return;

            if (!(slot == PREV_SLOT || slot == NEXT_SLOT)) {
                DecimalFormat decFormat = new DecimalFormat("###,###");

                int originSellPrice = getOriginSellPrice(page, slot);
                int originBuyPrice = getOriginBuyPrice(page, slot);
                int sellPrice = getSellPrice(page, slot);
                int buyPrice = getBuyPrice(page, slot);
                int stock = getStock(page, slot);

                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = Arrays.asList("§r§7---------------------------------------",
                        "§r§e› §f구매가격 : " + (originBuyPrice == -1 ? ChatColor.translateAlternateColorCodes('&', cannotBuy) : "§6" + originBuyPrice + (isMarketPriceEnabled() ? " §7(시세 : §6" + decFormat.format(buyPrice) + "§7)" : "")),
                        "§r§e› §f판매가격 : " + (originSellPrice == -1 ? ChatColor.translateAlternateColorCodes('&', cannotSell) : "§6" + originSellPrice + (isMarketPriceEnabled() ? " §7(시세 : §6" + decFormat.format(sellPrice) + "§7)" : "")),
                        "§r§e› §f재고 : " + (ChatColor.translateAlternateColorCodes('&', hasStock(page, slot) ? (stock == -1 ? unlimited : "&6" + stock + "개") : soldOut)),
                        "§r§7---------------------------------------",
                        "§r§e› §f구매가격 설정 : 우클릭",
                        "§r§e› §f판매가격 설정 : 좌클릭",
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
            }

            inventory.setItem(slot, itemStack);
        });

        if (inventory.getItem(PREV_SLOT) == null) inventory.setItem(PREV_SLOT, GUIStackUtil.getButton(ButtonType.PREV_PAGE));
        if (inventory.getItem(NEXT_SLOT) == null) inventory.setItem(NEXT_SLOT, GUIStackUtil.getButton(ButtonType.NEXT_PAGE));

        return inventory;
    }

    public Inventory getInv(InventoryType type, int page) {
        switch (type) {
            case SHOP: return getShopInv(page);
            case SHOP_SETTING: return getShopSettingInv();
            case ITEM_SETTING: return getItemSettingInv(page);
            case ITEM_DETAIL_SETTING: return getItemDetailSettingInv(page);
            default: return null;
        }
    }

    private void computeMaxPage() {
        int originMaxPage = getMaxPage();
        int maxPage = getMaxPage();

        Map<Integer, ItemStack> items = getItems(maxPage);
        items.remove(getSize() - 6);
        items.remove(getSize() - 4);

        while (items.values().stream().allMatch(Objects::isNull) && maxPage > 2) {
            items = getItems(maxPage);
            items.remove(getSize() - 6);
            items.remove(getSize() - 4);

            setItems(maxPage, null);
            maxPage--;
        }
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
