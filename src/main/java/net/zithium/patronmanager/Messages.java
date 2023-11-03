package net.zithium.patronmanager;

import org.bukkit.command.CommandSender;

import java.util.List;
import net.zithium.library.utils.Color;

public enum Messages {

    BALANCE_ADD_COMMAND("balance_added"),
    BALANCE_VIEW("balance_command"),
    NO_PERMISSION("no_permission"),
    BALANCE_RESET("balance_reset");


    private final String path;

    Messages(String path) {
        this.path = path;
    }


    public void send(CommandSender receiver, Object... replacements) {
        Object configMessage = PatronManager.getPlugin(PatronManager.class).getConfig().get("messages." + this.path);

        String finalMessage;

        if (configMessage == null) {
            finalMessage = "ERROR: Message not found (" + this.path + ");";
        } else {
            finalMessage = configMessage instanceof List ? TextUtil.fromList((List<?>) configMessage) : configMessage.toString();
        }

        if (!finalMessage.isEmpty()) {
            String formattedMessage = replace(finalMessage, replacements);
            receiver.sendMessage(Color.stringColor(formattedMessage));
        }

    }

    private String replace(String message, Object... replacements) {
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 >= replacements.length) break;
            message = message.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
        }

        return message;
    }
}
