package net.starly.shopplus.util;

import net.starly.core.jb.version.nms.tank.NmsItemStackUtil;
import net.starly.core.jb.version.nms.wrapper.ItemStackWrapper;
import net.starly.core.jb.version.nms.wrapper.NBTTagCompoundWrapper;
import net.starly.shopplus.enums.ButtonType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIStackUtil {
    public static ItemStack getButton(ButtonType type) {
        ItemStack itemStack;
        switch (type) {
            case SHOP_ENABLED: {
                try {
                    itemStack = new ItemStack(Material.valueOf("GREEN_WOOL"));
                } catch (Exception ex) {
                    itemStack = new ItemStack(Material.valueOf("WOOL"), 1, (short) 13);
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§6상점");
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                lore.addAll(Arrays.asList("§e› §f상점을 §a§l활성화§f/§c§l비활성화§f 할 수 있습니다.", "§e› §f현재 상태 : §a§l§n[활성화]"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                break;
            }

            case SHOP_DISABLED: {
                try {
                    itemStack = new ItemStack(Material.valueOf("RED_WOOL"));
                } catch (Exception ex) {
                    itemStack = new ItemStack(Material.valueOf("WOOL"), 1, (short) 14);
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§6상점");
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                lore.addAll(Arrays.asList("§e› §f상점을 §a§l활성화§f/§c§l비활성화§f 할 수 있습니다.", "§e› §f현재 상태 : §c§l§n[비활성화]"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                break;
            }

            case MARKET_PRICE_ENABLED: {
                try {
                    itemStack = new ItemStack(Material.valueOf("GREEN_WOOL"));
                } catch (Exception ex) {
                    itemStack = new ItemStack(Material.valueOf("WOOL"), 1, (short) 13);
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§6시세");
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                lore.addAll(Arrays.asList("§e› §f상점의 아이템 판매가격을 §a§l시세§f/§c§l고정가§f로 설정할 수 있습니다.", "§e› §f현재 상태 : §a§l§n[시세]"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                break;
            }

            case MARKET_PRICE_DISABLED: {
                try {
                    itemStack = new ItemStack(Material.valueOf("RED_WOOL"));
                } catch (Exception ex) {
                    itemStack = new ItemStack(Material.valueOf("WOOL"), 1, (short) 14);
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§6시세");
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                lore.addAll(Arrays.asList("§e› §f상점의 아이템 판매가격을 §a§l시세§f/§c§l고정가§f로 설정할 수 있습니다.", "§e› §f현재 상태 : §c§l§n[고정가]"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                break;
            }

            case ITEM_SETTING: {
                itemStack = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§6아이템 설정");
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                lore.addAll(Arrays.asList("§e› §f상점의 아이템을 설정할 수 있습니다.", "§e› §f클릭 후 열리는 인벤토리에 아이템을 배치하실 수 있습니다."));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                break;
            }

            case ITEM_DETAIL_SETTING: {
                itemStack = new ItemStack(Material.EMERALD);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§6아이템 세부 설정");
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                lore.addAll(Arrays.asList("§e› §f상점의 아이템 판매가격등을 설정할 수 있습니다.", "§e› §f클릭 후 열리는 인벤토리에 아이템을 선택하실 수 있습니다."));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                break;
            }

            case SET_NPC: {
                try {
                    itemStack = new ItemStack(Material.valueOf("VILLAGER_SPAWN_EGG"));
                } catch (Exception ex) {
                    itemStack = new ItemStack(Material.valueOf("MONSTER_EGG"), 1, (short) 100);
                    SpawnEggMeta itemMeta = (SpawnEggMeta) itemStack.getItemMeta();
                    itemMeta.setSpawnedType(EntityType.VILLAGER);
                    itemStack.setItemMeta(itemMeta);
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§6NPC 설정");
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                lore.addAll(Arrays.asList("§e› §f이 곳을 클릭하여 상점 NPC를 설정할 수 있습니다.", "§e› §fShift + 좌클릭시 NPC 설정을 초기화합니다.", "§e› §fShift + 우클릭시 설정된 NPC로 이동합니다."));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                break;
            }

            default: {
                return null;
            }
        }

        ItemStackWrapper nmsWrapper = NmsItemStackUtil.getInstance().asNMSCopy(itemStack);
        NBTTagCompoundWrapper nbtTagCompound = nmsWrapper.getTag();
        if (nbtTagCompound == null) nbtTagCompound = NmsItemStackUtil.getInstance().getNbtCompoundUtil().newInstance();
        nbtTagCompound.setString("buttonId", type.name());
        nmsWrapper.setTag(nbtTagCompound);
        itemStack = NmsItemStackUtil.getInstance().asBukkitCopy(nmsWrapper);

        return itemStack;
    }
}
