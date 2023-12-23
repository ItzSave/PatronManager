package net.zithium.patronmanager.player.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.zithium.patronmanager.PatronManager;
import net.zithium.patronmanager.player.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;

public class MySQL implements StorageHandler {

    private HikariDataSource hikari;

    @Override
    public boolean onEnable(PatronManager plugin) {
        FileConfiguration config = plugin.getConfig();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://%host%:%port%/%database%"
                .replace("%host%", config.getString("mysql_settings.host"))
                .replace("%port%", String.valueOf(config.getInt("mysql_settings.port")))
                .replace("%database%", config.getString("mysql_settings.database")));

        hikariConfig.setUsername(config.getString("mysql_settings.username"));
        hikariConfig.setPassword(config.getString("mysql_settings.password"));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikari = new HikariDataSource(hikariConfig);

        createTable();
        return true;
    }

    @Override
    public void onDisable() {
        if (hikari != null) hikari.close();
    }

    private void createTable() {
        try (Connection connection = hikari.getConnection()) {
            String createPlayersTable = "CREATE TABLE IF NOT EXISTS `players` (uuid VARCHAR(255) NOT NULL PRIMARY KEY, balance DOUBLE);";
            String createPlayerGoalsTable = "CREATE TABLE IF NOT EXISTS `player_completed_goals` (player_uuid VARCHAR(255) NOT NULL, goal_number INT);";


            try (Statement statement = connection.createStatement()) {
                statement.execute(createPlayersTable);
                statement.execute(createPlayerGoalsTable);
                getLogger().info("Created database tables successfully.");
            }

        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Encountered an error while attempting to create database tables.", e);
        }
    }

    @Override
    public PlayerData getPlayer(UUID uuid) {
        PlayerData playerData = null;

        try (Connection connection = hikari.getConnection()) {
            String selectQuery = "SELECT uuid, balance FROM players WHERE uuid = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, uuid.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Player with the given UUID found
                        String playerUUID = resultSet.getString("uuid");
                        double playerBalance = resultSet.getDouble("balance");

                        // Create a PlayerData object with the retrieved data
                        playerData = new PlayerData(UUID.fromString(playerUUID), playerBalance);
                        getLogger().info("Player data found for UUID: " + uuid);
                    } else {
                        getLogger().warning("No player data found for UUID: " + uuid);
                    }
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Error fetching player data for UUID: " + uuid, e);
        }

        return playerData;
    }

    public void savePlayer(final PlayerData player) {
        try (Connection connection = hikari.getConnection()) {
            String updateQuery = "UPDATE players SET balance = ? WHERE uuid = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setDouble(1, player.getBalance());
                preparedStatement.setString(2, player.getUuid().toString());

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    getLogger().info("Player data updated successfully. Rows affected: " + rowsAffected);
                } else {
                    getLogger().warning("No player data found for UUID: " + player.getUuid() + ". No update performed.");
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Error updating player data", e);
        }
    }



    public Set<Integer> loadCompletedGoals(UUID uuid) {
        Set<Integer> completedGoals = new HashSet<>();

        try (Connection connection = hikari.getConnection()) {
            String selectQuery = "SELECT goal_number FROM player_completed_goals WHERE player_uuid = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, uuid.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int goalNumber = resultSet.getInt("goal_number");
                        completedGoals.add(goalNumber);
                    }
                }
            }
        } catch (SQLException e) {
            // Handle the exception
        }

        return completedGoals;
    }

    public void saveCompletedGoals(UUID uuid, Set<Integer> completedGoals) {
        try (Connection connection = hikari.getConnection()) {
            String insertQuery = "INSERT INTO player_completed_goals (player_uuid, goal_number) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                for (int goalNumber : completedGoals) {
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.setInt(2, goalNumber);
                    preparedStatement.addBatch();
                }

                // Execute the batch insert
                preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            // Handle the exception
        }
    }


}