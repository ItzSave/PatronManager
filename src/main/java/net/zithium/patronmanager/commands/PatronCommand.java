package net.zithium.patronmanager.commands;

import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import net.zithium.patronmanager.Messages;
import net.zithium.patronmanager.PatronManager;
import net.zithium.patronmanager.player.PlayerData;
import net.zithium.patronmanager.player.PlayerManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

@Command("patron")
@SuppressWarnings("unused")
public class PatronCommand extends CommandBase {

    private final PatronManager plugin;
    private final PlayerManager playerManager;

    public PatronCommand(PatronManager plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }


    @Default
    public void patronCommand(Player player) {
        java.util.Optional<PlayerData> playerDataOptional = playerManager.getPlayerData(player.getUniqueId());
        if (playerDataOptional.isPresent()) {
            PlayerData playerData = playerDataOptional.get();
            double currentBalance = playerData.getBalance();
            double targetAmount = playerData.getPercentage(plugin.getConfig().getDouble("settings.patron_price"));

            DecimalFormat decimalFormat = new DecimalFormat("#.33");

            String formattedBalance = decimalFormat.format(currentBalance);
            String formattedPercentage = decimalFormat.format(targetAmount);


            Messages.BALANCE_VIEW.send(player, "{balance}", formattedBalance, "{percentage}", formattedPercentage);
        } else {
            player.sendMessage("Player data not found.");
        }
    }


    @SubCommand("add")
    @Permission("patron.command.add")
    public void addCommand(CommandSender sender, @Completion("#players") Player player, String amount) {
        if (!verifyArgs(sender, player, amount)) return;

        try {
            double parsedAmount = Double.parseDouble(amount);

            java.util.Optional<PlayerData> playerDataOptional = playerManager.getPlayerData(player.getUniqueId());
            if (playerDataOptional.isPresent()) {
                PlayerData playerData = playerDataOptional.get();
                playerData.addBalance(parsedAmount, player);
                Messages.BALANCE_ADD_COMMAND.send(sender, "{balance}", parsedAmount, "{target}", player.getName());
            } else {
                createPlayerData(player.getUniqueId(), parsedAmount);
                Messages.BALANCE_ADD_COMMAND.send(sender, "{balance}", parsedAmount, "{target}", player.getName());
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount. Please provide a valid number.");
        }
    }

    @SubCommand("set")
    @Permission("patron.command.set")
    public void setCommand(CommandSender sender, @Completion("#players") Player player, String amount) {
        if (!verifyArgs(sender, player, amount)) return;

        try {
            double parsedAmount = Double.parseDouble(amount);

            java.util.Optional<PlayerData> playerDataOptional = playerManager.getPlayerData(player.getUniqueId());
            if (playerDataOptional.isPresent()) {
                PlayerData playerData = playerDataOptional.get();
                playerData.setBalance(parsedAmount);
                Messages.BALANCE_ADD_COMMAND.send(sender, "{balance}", parsedAmount, "{target}", player.getName());
            } else {
                createPlayerData(player.getUniqueId(), parsedAmount);
                Messages.BALANCE_ADD_COMMAND.send(sender, "{balance}", parsedAmount, "{target}", player.getName());
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount. Please provide a valid number.");
        }

    }

    @SubCommand("reset")
    @Permission("patron.command.reset")
    public void resetCommand(CommandSender sender, @Completion("#players") Player player) {
        java.util.Optional<PlayerData> playerDataOptional = playerManager.getPlayerData(player.getUniqueId());
        if (playerDataOptional.isPresent()) {
            PlayerData playerData = playerDataOptional.get();
            playerData.resetBalance();
            Messages.BALANCE_RESET.send(sender, "{target}", player.getName());
        }

    }


    /**
     *
     * @param uuid The target user's uuid.
     * @param balance The balance to set.
     */
    private void createPlayerData(UUID uuid, double balance) {
        PlayerData playerData = new PlayerData(uuid, balance);

        Map<UUID, PlayerData> players = playerManager.getPlayers();

        players.put(uuid, playerData);
        // Logging data creation just incase.
        plugin.getLogger().info("Player data created for UUID: " + uuid + " with balance: " + balance);
    }

    private boolean verifyArgs(CommandSender sender, Player player, String amountStr) {
        if (player == null) {
            sender.sendMessage("Invalid player");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            // Check if parsing succeeds
            if (amount < 0) {
                sender.sendMessage("Amount must be a positive number.");
                return false;
            }

        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount. Please provide a valid number.");
            return false;
        }

        return true;
    }


}
