package net.zithium.patronmanager;

import me.mattstudios.mf.base.CommandManager;
import net.luckperms.api.LuckPerms;
import net.zithium.library.action.ActionManager;
import net.zithium.patronmanager.commands.PatronCommand;
import net.zithium.patronmanager.hooks.PlaceholderAPIHook;
import net.zithium.patronmanager.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PatronManager extends JavaPlugin {


    private PlayerManager playerManager;
    public LuckPerms luckPerms;

    private ActionManager actionManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        (playerManager = new PlayerManager(this)).onEnable();


        CommandManager commandManager = new CommandManager(this);
        commandManager.getMessageHandler().register("cmd.no.permission", Messages.NO_PERMISSION::send);
        commandManager.register(new PatronCommand(this));

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        } else {
            getLogger().log(Level.SEVERE, "LuckPerms was not loaded!");
            getServer().getPluginManager().disablePlugin(this);
        }

        (actionManager = new ActionManager()).onEnable();

        checkForPlaceholderAPI();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (playerManager != null) playerManager.onDisable(false);
    }


    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    private void checkForPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI loading placeholders.");
            new PlaceholderAPIHook(this).register();
        } else {
            getLogger().severe("PlaceholderAPI was not found plugin will now disable");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }


}
