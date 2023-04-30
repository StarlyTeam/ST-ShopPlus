package net.starly.shopplus.command.tabcomplete;

import lombok.AllArgsConstructor;
import net.starly.shopplus.data.InvOpenMap;
import net.starly.shopplus.shop.ShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class ShopTab implements TabCompleter {
    private final InvOpenMap invOpenMap;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("starly.shop.create")) completions.add(label.equals("shop") ? "create" : "생성");
            if (sender.hasPermission("starly.shop.open")) completions.add(label.equals("shop") ? "open" : "열기");
            completions.add(label.equals("shop") ? "edit" : "편집");
            if (sender.hasPermission("starly.shop.list")) completions.add(label.equals("shop") ? "list" : "목록");
            if (sender.hasPermission("starly.shop.delete")) completions.add(label.equals("shop") ? "delete" : "삭제");
        } else if (args.length == 2) {
            if (Arrays.asList("open", "열기", "edit", "편집", "delete", "삭제").contains(args[0].toLowerCase())) completions.addAll(ShopManager.getInstance().getShopNames());
            else if (args[0].equalsIgnoreCase("create")) completions.add("<Name>");
            else if (args[0].equalsIgnoreCase("생성")) completions.add("<이름>");
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) completions.addAll(Arrays.asList("<Line>", "1", "2", "3", "4", "5", "6"));
            else if (args[0].equalsIgnoreCase("생성")) completions.addAll(Arrays.asList("<줄>", "1", "2", "3", "4", "5", "6"));
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("create")) completions.add("<Title>");
            else if (args[0].equalsIgnoreCase("생성")) completions.add("<제목>");
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
    }
}