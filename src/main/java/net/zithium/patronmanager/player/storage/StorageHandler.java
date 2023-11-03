package net.zithium.patronmanager.player.storage;

import net.zithium.patronmanager.PatronManager;
import net.zithium.patronmanager.player.PlayerData;

import java.util.Set;
import java.util.UUID;

public interface StorageHandler {


    boolean onEnable(final PatronManager plugin);

    void onDisable();

    PlayerData getPlayer(final UUID uuid);

    void savePlayer(final PlayerData playerData);

    void saveCompletedGoals(final UUID uuid, final Set<Integer> completedGoals);
    Set<Integer> loadCompletedGoals(final UUID uuid);
}
