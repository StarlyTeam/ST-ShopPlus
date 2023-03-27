package net.starly.shop.command;

import lombok.AllArgsConstructor;
import net.starly.core.data.Config;
import net.starly.core.jb.util.Pair;
import net.starly.shop.ShopPlusMain;
import net.starly.shop.context.ConfigContent;
import net.starly.shop.data.InvOpenMap;
import net.starly.shop.enums.InvOpenType;
import net.starly.shop.shop.ShopData;
import net.starly.shop.shop.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@AllArgsConstructor
public class ShopCmd implements CommandExecutor {
    private final InvOpenMap invOpenMap;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        Config msgConfig = ConfigContent.getInstance().getMsgConfig();

        if (args.length == 0) {
            if (!player.hasPermission("starly.shop.help")) {
                player.sendMessage(msgConfig.getMessage("errorMessages.noPermission"));
                return true;
            }

            msgConfig.getMessages("messages.help").forEach(player::sendMessage);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "생성":
            case "create": {
                if (!player.hasPermission("starly.shop.create")) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noPermission"));
                    return true;
                } else if (args.length == 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noShopName"));
                    return true;
                } else if (args.length == 2) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noShopLine"));
                    return true;
                } else if (args.length == 3) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noShopTitle"));
                    return true;
                } else if (ShopUtil.getShopNames().contains(args[1])) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.shopAlreadyExists"));
                    return true;
                }

                int line;
                try {
                    line = Integer.parseInt(args[2]);

                    if (line < 1 || line > 6) {
                        player.sendMessage(msgConfig.getMessage("errorMessages.wrongShopLine"));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.shopLineMustBeNumber"));
                    return true;
                }

                ShopUtil.createShop(args[1], line, ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 3, args.length))));
                player.sendMessage(msgConfig.getMessage("messages.shopCreated"));
                return true;
            }

            case "열기":
            case "open": {
                if (args.length == 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noShopName"));
                    return true;
                } else if (args.length != 2) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongCommand"));
                    return true;
                }

                if (!player.hasPermission("starly.shop.open." + args[1])) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noPermission"));
                    return true;
                } else if (!ShopUtil.getShopNames().contains(args[1])) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.shopNotExists"));
                    return true;
                } else if (!(player.isOp() || ShopUtil.getShopData(args[1]).isEnabled())) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.shopNotOpened"));
                    return true;
                }

                ShopData shopData = ShopUtil.getShopData(args[1]);
                Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                    player.openInventory(shopData.getShopInv());
                    invOpenMap.set(player, new Pair<>(InvOpenType.SHOP_SETTING, shopData));
                }, 1);
                break;
            }

            case "편집":
            case "edit": {
                if (args.length == 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noShopName"));
                    return true;
                } else if (args.length != 2) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.wrongCommand"));
                    return true;
                }

                if (!player.hasPermission("starly.shop.edit." + args[1])) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noPermission"));
                    return true;
                } else if (!ShopUtil.getShopNames().contains(args[1])) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.shopNotExists"));
                    return true;
                }

                ShopData shopData = ShopUtil.getShopData(args[1]);
                Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                    player.openInventory(shopData.getShopSettingInv());
                    invOpenMap.set(player, new Pair<>(InvOpenType.SHOP_SETTING, shopData));
                }, 1);
                break;
            }

            case "삭제":
            case "delete": {
                if (!player.hasPermission("starly.shop.delete")) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noPermission"));
                    return true;
                } else if (args.length == 1) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noShopName"));
                    return true;
                } else if (!ShopUtil.getShopNames().contains(args[1])) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.shopNotExists"));
                    return true;
                }

                ShopUtil.deleteShop(args[1]);
                player.sendMessage(msgConfig.getMessage("messages.shopDeleted"));
                return true;
            }

            case "목록":
            case "list": {
                if (!player.hasPermission("starly.shop.list")) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noPermission"));
                    return true;
                }

                msgConfig.getMessages("messages.shopList.message").forEach(line -> player.sendMessage(line.replace("{list}", String.join(msgConfig.getString("messages.shopList.delimiter"), ShopUtil.getShopNames()))));
                return true;
            }

            case "도움말":
            case "help":
            case "?": {
                if (!player.hasPermission("starly.shop.help")) {
                    player.sendMessage(msgConfig.getMessage("errorMessages.noPermission"));
                    return true;
                }

                msgConfig.getMessages("messages.help").forEach(player::sendMessage);
                return true;
            }

            default: {
                player.sendMessage(msgConfig.getMessage("errorMessages.wrongCommand"));
                return true;
            }
        }

        return false;
    }
}
