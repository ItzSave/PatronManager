package net.zithium.patronmanager.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.zithium.patronmanager.PatronManager;
import net.zithium.patronmanager.player.PlayerData;
import net.zithium.patronmanager.player.PlayerManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Optional;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final PatronManager plugin;
    private final PlayerManager playerManager;

    public PlaceholderAPIHook(PatronManager plugin) {
        this.plugin = plugin;
        playerManager = plugin.getPlayerManager();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "patron";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ItzSave";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";

        final Optional<PlayerData> playerDataOptional = playerManager.getPlayerData(offlinePlayer.getUniqueId());
        if (playerDataOptional.isEmpty()) return "[player-data null]";
        PlayerData playerData = playerDataOptional.get();

        double balance = playerData.getBalance();

        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

        // %patron_balance%
        if (params.equals("balance")) {
            return decimalFormat.format(balance);
        }

        // %patron_raw_balance%
        if (params.equals("raw_balance")) {
            return String.valueOf(balance);
        }

        return "";
    }
}
