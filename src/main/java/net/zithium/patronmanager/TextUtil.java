package net.zithium.patronmanager;
import java.util.List;

import static org.bukkit.ChatColor.stripColor;

public class TextUtil {

    public static String fromList(List<?> list) {
        if (list == null || list.isEmpty()) return null;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (stripColor(list.get(i).toString()).equals("")) builder.append("\n<reset>");
            else builder.append(list.get(i).toString()).append(i + 1 != list.size() ? "\n" : "");
        }

        return builder.toString();
    }

}
