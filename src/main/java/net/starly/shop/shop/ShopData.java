package net.starly.shop.shop;

import net.starly.core.data.Config;
import net.starly.core.data.ConfigSection;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.enums.ButtonType;
import net.starly.shop.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShopData {
    private final Config config;

    protected ShopData(Config config) {
        this.config = config;
    }

    public boolean exists() {
        return config.isFileExist();
    }

    public boolean isEnabled() {
        return config.getBoolean("shop.enabled");
    }

    public void setEnabled(boolean enabled) {
        config.setBoolean("shop.enabled", enabled);
    }

    public boolean isSellable(int slot) {
        return getSellPrice(slot) != -1;
    }

    public boolean isBuyable(int slot) {
        return getBuyPrice(slot) != -1;
    }

    public int getSellPrice(int slot) {
        return config.getInt("shop.prices." + slot + ".sell");
    }

    public void setSellPrice(int slot, int price) {
        config.setInt("shop.prices." + slot + ".sell", price);
    }

    public int getBuyPrice(int slot) {
        return config.getInt("shop.prices." + slot + ".buy");
    }

    public void setBuyPrice(int slot, int price) {
        config.setInt("shop.prices." + slot + ".buy", price);
    }

    public ItemStack getItem(int slot) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) config.getObject("shop.items." + slot));  BukkitObjectInputStream bois = new BukkitObjectInputStream(bis)) {
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
            config.setObject("shop.items." + slot, null);
            config.setObject("shop.prices." + slot, null);
            return;
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
            boos.writeObject(itemStack);
            config.setObject("shop.items." + slot, bos.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setSellPrice(slot, -1);
        setBuyPrice(slot, -1);
    }

    public Inventory getShopInv() {
        config.reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, config.getInt("shop.size"), config.getString("shop.title"));

        ConfigSection itemsSection = config.getSection("shop.items");
        itemsSection.getKeys().forEach(key -> {
            int slot = Integer.parseInt(key);
            int sellPrice = getSellPrice(slot);
            int buyPrice = getBuyPrice(slot);

            ItemStack itemStack = getItem(slot);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = ConfigContent.getInstance().getConfig().getStringList("lore.shopItem").stream().map(line -> ChatColor.translateAlternateColorCodes('&', line).replace("{sellPrice}", (isSellable(slot) ? sellPrice : "§c판매 불가") + "").replace("{buyPrice}", (isBuyable(slot) ? buyPrice : "§c구매 불가") + "")).collect(Collectors.toList());
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            inventory.setItem(slot, itemStack);
        });

        return inventory;
    }

    public Inventory getShopSettingInv() {
        config.reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, 27, config.getString("shop.title") + "§r [상점 편집]");
        if (isEnabled()) inventory.setItem(11, ItemUtil.getButton(ButtonType.SHOP_ENABLED));
        else inventory.setItem(11, ItemUtil.getButton(ButtonType.SHOP_DISABLED));
        inventory.setItem(13, ItemUtil.getButton(ButtonType.ITEM_SETTING));
        inventory.setItem(14, ItemUtil.getButton(ButtonType.ITEM_DETAIL_SETTING));

        return inventory;
    }

    public Inventory getItemSettingInv() {
        config.reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, config.getInt("shop.size"), config.getString("shop.title") + "§r [아이템 설정]");

        ConfigSection itemsSection = config.getSection("shop.items");
        itemsSection.getKeys().forEach(key -> inventory.setItem(Integer.parseInt(key), getItem(Integer.parseInt(key))));

        return inventory;
    }

    public Inventory getItemDetailSettingInv() {
        config.reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, config.getInt("shop.size"), config.getString("shop.title") + "§r [아이템 세부설정]");

        ConfigSection itemsSection = config.getSection("shop.items");
        itemsSection.getKeys().forEach(key -> {
            int slot = Integer.parseInt(key);
            int sellPrice = getSellPrice(slot);
            int buyPrice = getBuyPrice(slot);

            ItemStack itemStack = getItem(slot);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = Arrays.asList("§r§7---------------------------------------", "§r§e› §f판매가격: " + (sellPrice == -1 ? "§c판매 불가" : "§6" + sellPrice), "§r§e› §f구매가격: " + (buyPrice == -1 ? "§c구매 불가" : "§6" + buyPrice), "§r§7---------------------------------------", "§r§e› §f판매가격 : 좌클릭", "§r§e› §f구매가격 : 우클릭", "§r§e› §f삭제 : Shift + 클릭", "§r§7---------------------------------------");
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            inventory.setItem(slot, itemStack);
        });

        return inventory;
    }
}
