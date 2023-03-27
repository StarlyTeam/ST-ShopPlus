package net.starly.shop.command.tabcomplete;

import lombok.AllArgsConstructor;
import net.starly.shop.data.InvOpenMap;
import net.starly.shop.shop.ShopUtil;
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
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            if (label.equals("shop")) tab.addAll(Arrays.asList("create", "open", "edit", "list", "delete"));
            else tab.addAll(Arrays.asList("생성", "열기", "편집", "목록", "삭제"));
        } else if (args.length == 2) {
            if (Arrays.asList("open", "열기", "edit", "편집", "delete", "삭제").contains(args[0].toLowerCase()))
                tab.addAll(ShopUtil.getShopNames());
            else if (args[0].equalsIgnoreCase("create")) tab.add("<Name>");
            else if (args[0].equalsIgnoreCase("생성")) tab.add("<이름>");
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) tab.addAll(Arrays.asList("<Line>", "1", "2", "3", "4", "5", "6"));
            else if (args[0].equalsIgnoreCase("생성")) tab.addAll(Arrays.asList("<줄>", "1", "2", "3", "4", "5", "6"));
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("create")) tab.add("<Title>");
            else if (args[0].equalsIgnoreCase("생성")) tab.add("<제목>");
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], tab, new ArrayList<>());
    }
}