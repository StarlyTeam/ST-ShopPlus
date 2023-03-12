package net.starly.shop.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackUtil {
    public static boolean equals(ItemStack A, ItemStack B) {
        if (A == null || B == null) return false;
        if (A.getType() == Material.AIR || B.getType() == Material.AIR) return false;
        ItemStack A_ = A.clone();
        ItemStack B_ = B.clone();
        A_.setAmount(1);
        B_.setAmount(1);
        return A_.equals(B_);
    }
}
