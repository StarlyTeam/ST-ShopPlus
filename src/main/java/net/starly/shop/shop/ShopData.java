package net.starly.shop.shop;

import net.starly.core.data.Config;
import net.starly.core.data.ConfigSection;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.enums.ButtonType;
import net.starly.shop.util.EntityUtil;
import net.starly.shop.util.ItemUtil;
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

    public boolean hasStock(int slot) {
        return getStock(slot) == -1 || getStock(slot) > 0;
    }

    public int getStock(int slot) {
        return config.getInt("shop.stocks." + slot);
    }

    public void setStock(int slot, int stock) {
        config.setInt("shop.stocks." + slot, stock);
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
            config.setObject("shop.stocks." + slot, null);
            return;
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream boos = new BukkitObjectOutputStream(bos)) {
            boos.writeObject(itemStack);
            config.setObject("shop.items." + slot, bos.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (getSellPrice(slot) == 0) setSellPrice(slot, -1);
        if (getBuyPrice(slot) == 0) setBuyPrice(slot, -1);
        if (getStock(slot) == 0) setStock(slot, 0);
    }

    public Inventory getShopInv() {
        config.reloadConfig();
        Inventory inventory = Bukkit.createInventory(null, config.getInt("shop.size"), config.getString("shop.title"));

        Config mainConfig = ConfigContent.getInstance().getConfig();
        ConfigSection itemsSection = config.getSection("shop.items");
        itemsSection.getKeys().forEach(key -> {
            int slot = Integer.parseInt(key);
            int sellPrice = getSellPrice(slot);
            int buyPrice = getBuyPrice(slot);

            ItemStack itemStack = getItem(slot);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = ConfigContent.getInstance().getConfig().getStringList("lore.shopItem").stream().map(line -> ChatColor.translateAlternateColorCodes('&', line).replace("{sellPrice}", (isSellable(slot) ? sellPrice : mainConfig.getString("text.cannotSell")) + "").replace("{buyPrice}", (isBuyable(slot) ? buyPrice : mainConfig.getString("text.cannotBuy")) + "").replace("{stock}", ChatColor.translateAlternateColorCodes('&', hasStock(slot) ? getStock(slot) + "" : mainConfig.getString("text.soldOut")))).collect(Collectors.toList());
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
        inventory.setItem(15, ItemUtil.getButton(ButtonType.SET_NPC));

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

        Config mainConfig = ConfigContent.getInstance().getConfig();
        ConfigSection itemsSection = config.getSection("shop.items");
        itemsSection.getKeys().forEach(key -> {
            int slot = Integer.parseInt(key);
            int sellPrice = getSellPrice(slot);
            int buyPrice = getBuyPrice(slot);

            ItemStack itemStack = getItem(slot);
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = Arrays.asList("§r§7---------------------------------------", "§r§e› §f판매가격 : " + (sellPrice == -1 ? ChatColor.translateAlternateColorCodes('&', mainConfig.getString("text.cannotSell")) : "§6" + sellPrice), "§r§e› §f구매가격 : " + (buyPrice == -1 ? ChatColor.translateAlternateColorCodes('&', mainConfig.getString("text.cannotBuy")) : "§6" + buyPrice), "§r§e› §f재고 : " + (hasStock(slot) ? "§6" + getStock(slot) + "개" : ChatColor.translateAlternateColorCodes('&', mainConfig.getString("text.soldOut"))), "§r§7---------------------------------------", "§r§e› §f판매가격 : 우클릭", "§r§e› §f구매가격 : 좌클릭", "§r§e› §f삭제 : Shift + 클릭", "§r§e› §f재고 추가 : Q", "§r§e› §f재고 설정 : Ctrl + Q", "§r§7---------------------------------------");
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            inventory.setItem(slot, itemStack);
        });

        return inventory;
    }

    public void setNPC(Entity entity) {
        config.setString("shop.npc", entity == null ? "<none>" : entity.getUniqueId() + "");
    }

    public Entity getNPC() {
        String uuid = config.getString("shop.npc");
        return uuid.equals("<none>") ? null : EntityUtil.getEntity(UUID.fromString(uuid));
    }

    public boolean hasNPC() {
        return getNPC() != null;
    }
}
