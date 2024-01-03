package net.zithium.patronmanager.player;

import net.zithium.patronmanager.PatronManager;
import net.zithium.patronmanager.player.storage.MySQL;
import net.zithium.patronmanager.player.storage.StorageHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public class PlayerManager {

    private final PatronManager plugin;
    private StorageHandler storageHandler;

    private final Map<UUID, PlayerData> players;

    public Set<Integer> completedGoals;

    public PlayerManager(PatronManager plugin) {
        this.plugin = plugin;
        this.players = new ConcurrentHashMap<>();
    }

    public void onEnable() {
        storageHandler = new MySQL();

        if (!storageHandler.onEnable(plugin)) {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Register event listeners
        Stream.of(
                new Listener() {
                    @EventHandler(priority = EventPriority.MONITOR)
                    public void onPlayerJoin(final PlayerJoinEvent event) {
                        final UUID uuid = event.getPlayer().getUniqueId();

                        // Check if player data is already loaded
                        if (!players.containsKey(uuid)) {
                            // Load player data asynchronously
                            // Load player data asynchronously
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    PlayerData playerData = storageHandler.getPlayer(uuid);
                                    if (playerData != null) {
                                        players.put(uuid, playerData);
                                    }

                                    try {
                                        completedGoals = storageHandler.loadCompletedGoals(uuid);
                                    } catch (Exception e) {
                                        plugin.getLogger().log(Level.SEVERE, "Error loading completed goals for: " + uuid, e);
                                    }

                                    if (completedGoals.isEmpty() && playerData != null) {
                                        // If the player has no completed goals in the database, perform the initial goal commands
                                        for (Integer goal : playerData.getGoals().keySet()) {
                                            playerData.performGoalCommands(goal, event.getPlayer());
                                        }
                                        // Save the completed goals in the database
                                        storageHandler.saveCompletedGoals(uuid, playerData.getCompletedGoals());
                                    } else if (!completedGoals.isEmpty() && playerData != null) {
                                        playerData.setCompletedGoals(playerData, completedGoals);
                                    }
                                } catch (Exception e) {
                                    plugin.getLogger().log(Level.SEVERE, "Error loading player data for: " + uuid, e);
                                }
                            });
                        }
                    }
                }, new Listener() {
                    @EventHandler(priority = EventPriority.MONITOR)
                    public void onPlayerQuit(final PlayerQuitEvent event) {
                        savePlayerStorage(event.getPlayer().getUniqueId(), true);
                    }
                }).forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));

        // Load player data for online players
        Bukkit.getOnlinePlayers().forEach(player -> loadPlayerData(player.getUniqueId()));
    }

    public void onDisable(boolean reload) {
        plugin.getLogger().info("Saving player data to the database...");
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            try {
                for (PlayerData player : new ArrayList<>(players.values())) {
                    storageHandler.savePlayer(player);
                }

                if (!reload) {
                    players.clear();
                    storageHandler.onDisable();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error while saving player data", e);
            } finally {
                executorService.shutdown();
            }
        });

        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
        }
    }

    public Optional<PlayerData> getPlayerData(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    private void loadPlayerData(UUID uuid) {
        // Use async scheduler to load player data in a separate thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PlayerData playerData = storageHandler.getPlayer(uuid);
                if (playerData != null) {
                    players.put(uuid, playerData);
                }

                try {
                    completedGoals = storageHandler.loadCompletedGoals(uuid);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error loading completed goals for: " + uuid, e);
                }

                if (!completedGoals.isEmpty() && playerData != null) {
                    playerData.setCompletedGoals(playerData, completedGoals);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error loading player data for: " + uuid, e);
            }
        });
    }

    public void savePlayerStorage(UUID uuid, boolean clearCache) {
        PlayerData playerData = players.get(uuid);
        if (playerData == null) return;

        // Use async scheduler to save player data in a separate thread

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            storageHandler.savePlayer(playerData);
            // Save completed goals in MySQL
            Set<Integer> oldCompletedGoals = storageHandler.loadCompletedGoals(uuid);
            if (!playerData.getCompletedGoals().equals(oldCompletedGoals)) {
                storageHandler.saveCompletedGoals(playerData.getUuid(), playerData.getCompletedGoals());
            }
            if (clearCache) {
                players.remove(uuid);
            }
        });
    }


    public Map<UUID, PlayerData> getPlayers() {
        return players;
    }
}
