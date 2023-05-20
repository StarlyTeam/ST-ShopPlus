package net.starly.shopplus.util;

import org.bukkit.inventory.ItemStack;

public class ItemStackUtil {

    public static ItemStack setAmount(ItemStack itemStack, int amount) {
        itemStack.setAmount(amount);
        return itemStack;
    }
}
