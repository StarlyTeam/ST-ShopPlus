package net.starly.shopplus.command;

import lombok.AllArgsConstructor;
import net.starly.core.jb.util.Pair;
import net.starly.shopplus.ShopPlusMain;
import net.starly.shopplus.context.ConfigContext;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.data.NPCMap;
import net.starly.shopplus.enums.InventoryType;
import net.starly.shopplus.message.MessageContext;
import net.starly.shopplus.message.MessageLoader;
import net.starly.shopplus.message.enums.MessageType;
import net.starly.shopplus.runnable.MarketPriceScheduler;
import net.starly.shopplus.shop.ShopData;
import net.starly.shopplus.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@AllArgsConstructor
public class ShopCmd implements CommandExecutor {
    private final InvOpenMap invOpenMap;
    private final NPCMap npcMap;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        MessageContext msgContext = MessageContext.getInstance();

        if (args.length == 0) {
            if (!player.hasPermission("starly.shop.help")) {
                msgContext.get(MessageType.ERROR, "noPermission").send(player);
                return true;
            }

            msgContext.get(MessageType.NORMAL, "help").send(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "생성":
            case "create": {
                if (!player.hasPermission("starly.shop.create")) {
                    msgContext.get(MessageType.ERROR, "noPermission").send(player);
                    return true;
                } else if (args.length == 1) {
                    msgContext.get(MessageType.ERROR, "noShopName").send(player);
                    return true;
                } else if (args.length == 2) {
                    msgContext.get(MessageType.ERROR, "noShopLine").send(player);
                    return true;
                } else if (args.length == 3) {
                    msgContext.get(MessageType.ERROR, "noShopTitle").send(player);
                    return true;
                }

                if (ShopManager.getInstance().getShopData(args[1]) != null) {
                    msgContext.get(MessageType.ERROR, "shopAlreadyExists").send(player);
                    return true;
                } else if (!args[1].matches("([^\\/:*?\"<>|])*")) {
                    player.sendMessage("EXCEPTION NAME ARGS.");
                    return true;
                }


                int line;
                try {
                    line = Integer.parseInt(args[2]);

                    if (line < 1 || line > 5) {
                        msgContext.get(MessageType.ERROR, "wrongShopLine").send(player);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    msgContext.get(MessageType.ERROR, "shopLineMustBeNumber").send(player);
                    return true;
                }

                ShopManager.getInstance().createShop(args[1], line + 1, ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.copyOfRange(args, 3, args.length))));
                msgContext.get(MessageType.NORMAL, "shopCreated").send(player);
                return true;
            }

            case "열기":
            case "open": {
                if (args.length == 1) {
                    msgContext.get(MessageType.ERROR, "noShopName").send(player);
                    return true;
                } else if (args.length != 2) {
                    msgContext.get(MessageType.ERROR, "wrongCommand").send(player);
                    return true;
                } else if (!player.hasPermission("starly.shop.open." + args[1])) {
                    msgContext.get(MessageType.ERROR, "noPermission").send(player);
                    return true;
                }

                ShopData shopData = ShopManager.getInstance().getShopData(args[1]);
                if (shopData == null) {
                    msgContext.get(MessageType.ERROR, "shopNotExists").send(player);
                    return true;
                } else if (!(player.isOp() || shopData.isEnabled())) {
                    msgContext.get(MessageType.ERROR, "shopNotOpened").send(player);
                    return true;
                }

                Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                    player.openInventory(shopData.getShopInv(1));
                    invOpenMap.set(player, new Pair<>(InventoryType.SHOP, shopData.getName() + "|" + 1));
                }, 1);
                break;
            }

            case "편집":
            case "edit": {
                if (args.length == 1) {
                    msgContext.get(MessageType.ERROR, "noShopName").send(player);
                    return true;
                } else if (args.length != 2) {
                    msgContext.get(MessageType.ERROR, "wrongCommand").send(player);
                    return true;
                }

                if (!player.hasPermission("starly.shop.edit." + args[1])) {
                    msgContext.get(MessageType.ERROR, "noPermission").send(player);
                    return true;
                } else if (!ShopManager.getInstance().getShopNames().contains(args[1])) {
                    msgContext.get(MessageType.ERROR, "shopNotExists").send(player);
                    return true;
                }

                ShopData shopData = ShopManager.getInstance().getShopData(args[1]);
                Bukkit.getServer().getScheduler().runTaskLater(ShopPlusMain.getInstance(), () -> {
                    player.openInventory(shopData.getShopSettingInv());
                    invOpenMap.set(player, new Pair<>(InventoryType.SHOP_SETTING, shopData.getName()));
                }, 1);
                break;
            }

            case "삭제":
            case "delete": {
                if (!player.hasPermission("starly.shop.delete")) {
                    msgContext.get(MessageType.ERROR, "noPermission").send(player);
                    return true;
                } else if (args.length == 1) {
                    msgContext.get(MessageType.ERROR, "noShopName").send(player);
                    return true;
                } else if (!ShopManager.getInstance().getShopNames().contains(args[1])) {
                    msgContext.get(MessageType.ERROR, "shopNotExists").send(player);
                    return true;
                }

                ShopData shopData = ShopManager.getInstance().getShopData(args[1]);
                if (shopData.hasNPC()) {
                    npcMap.remove(shopData.getNPC());
                    msgContext.get(MessageType.NORMAL, "NPCDeleted").send(player);
                }

                ShopManager.getInstance().deleteShop(args[1]);
                msgContext.get(MessageType.NORMAL, "shopDeleted").send(player);
                return true;
            }

            case "목록":
            case "list": {
                if (!player.hasPermission("starly.shop.list")) {
                    msgContext.get(MessageType.ERROR, "noPermission").send(player);
                    return true;
                }

                msgContext.get(MessageType.NORMAL, "shopList.message", (msg) -> msg.replace("{list}", String.join(msgContext.getOnlyString(MessageType.NORMAL, "shopList.delimiter"), ShopManager.getInstance().getShopNames()))).send(player);
                return true;
            }

            case "리로드":
            case "reload": {
                if (!player.hasPermission("starly.shop.reload")) {
                    msgContext.get(MessageType.ERROR, "noPermission").send(player);
                    return true;
                }

                ConfigContext.getInstance().reset();
                MessageContext.getInstance().reset();

                try {
                    File configFile = new File(ShopPlusMain.getInstance().getDataFolder(), "config.yml");
                    if (!configFile.exists()) configFile.createNewFile();
                    ConfigContext.getInstance().initialize(YamlConfiguration.loadConfiguration(configFile));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    File messageFile = new File(ShopPlusMain.getInstance().getDataFolder(), "message.yml");
                    if (!messageFile.exists()) messageFile.createNewFile();
                    MessageLoader.load(YamlConfiguration.loadConfiguration(messageFile));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                MarketPriceScheduler.stop();
                MarketPriceScheduler.start(ConfigContext.getInstance().get("marketPrice.updateInterval", Integer.class) * 20L);

                msgContext.get(MessageType.NORMAL, "reloadComplete").send(player);
                return true;
            }

            case "도움말":
            case "help":
            case "?": {
                if (!player.hasPermission("starly.shop.help")) {
                    msgContext.get(MessageType.ERROR, "noPermission").send(player);
                    return true;
                }

                msgContext.get(MessageType.NORMAL, "help").send(player);
                return true;
            }

            default: {
                msgContext.get(MessageType.ERROR, "wrongCommand").send(player);
                return true;
            }
        }

        return false;
    }
}
