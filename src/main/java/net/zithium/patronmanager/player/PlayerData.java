package net.zithium.patronmanager.player;

import net.zithium.patronmanager.PatronManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {

    private final PatronManager plugin = PatronManager.getPlugin(PatronManager.class);
    private final Set<Integer> completedGoals;

    private final UUID uuid;
    private double balance;
    private final Map<Integer, Double> goals;

    public PlayerData(UUID uuid, double balance) {
        this.uuid = uuid;
        this.balance = balance;
        this.goals = parseGoals();
        this.completedGoals = new HashSet<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setBalance(double balance) {
        if (balance < 0) balance = 0.0;
        this.balance = balance;
    }

    public void resetBalance() {
        this.balance = 0.0;
    }

    public double getPercentage(double targetAmount) {
        if (targetAmount <= 0.0) {
            return 0.0;
        }

        double currentBalance = getBalance();
        if (currentBalance >= targetAmount) {
            return 100.0;
        } else {
            return (currentBalance / targetAmount) * 100.0;
        }
    }

    public Map<Integer, Double> getGoals() {
        return goals;
    }

    public boolean hasReachedGoal(double targetGoal, Player player) {
        return balance >= targetGoal;
    }

    public double getBalance() {
        return balance;
    }

    public void addBalance(double amount, Player player) {
        balance += amount;

        for (Map.Entry<Integer, Double> goalEntry : goals.entrySet()) {
            int goalNumber = goalEntry.getKey();
            double goalAmount = goalEntry.getValue();

            if (balance >= goalAmount && !completedGoals.contains(goalNumber)) {
                // Player has reached this goal, and it hasn't been completed yet
                performGoalCommands(goalNumber, player);
                completedGoals.add(goalNumber);
            } else {
                // Log if the goal has already been completed
                if (completedGoals.contains(goalNumber)) {
                }
            }
        }
    }


    private Map<Integer, Double> parseGoals() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("patron_goals");
        if (section == null) {
            plugin.getLogger().warning("No 'patron_goals' section found in the configuration.");
        }
        Map<Integer, Double> goalMap = new HashMap<>();
        if (section != null) {
            for (String goalKey : section.getKeys(false)) {
                int goalNumber = Integer.parseInt(goalKey);
                double goalAmount = section.getDouble(goalKey + ".goal", 0.0);
                goalMap.put(goalNumber, goalAmount);
            }
        }
        return goalMap;
    }

    public void performGoalCommands(int goalNumber, Player player) {
        if (completedGoals.contains(goalNumber)) {
            plugin.getLogger().info("Goal number: " + goalNumber + " is already completed.");
            return;
        }
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("patron_goals." + goalNumber);
        if (section == null) {
            plugin.getLogger().warning("No configuration section found for goal number: " + goalNumber);
        }
        if (section != null) {
            List<String> commands = section.getStringList("commands");
            double goalAmount = section.getDouble("goal", 0.0);

            if (balance >= goalAmount) {
                // Player has reached this goal, perform associated commands.
                for (String command : commands) {
                    String replacedCommand = command.replace("{player}", player.getName());
                    List<String> commandList = new ArrayList<>();
                    commandList.add(replacedCommand);
                    // Dispatch the command on the main server thread
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getActionManager().executeActions(player, commandList));
                }

                completedGoals.add(goalNumber);
            }
        }
    }

    public Set<Integer> getCompletedGoals() {
        return completedGoals;
    }

    public void setCompletedGoals(PlayerData playerData, Set<Integer> completedGoals) {
        plugin.getLogger().info("Attempting to set completed goals.");
        if (completedGoals.isEmpty()) {
            plugin.getLogger().info("No completed goals to set.");
        } else {
            plugin.getLogger().info("Found completed goals to set.");
        }
        this.completedGoals.clear();
        this.completedGoals.addAll(playerData.getCompletedGoals());
        this.completedGoals.addAll(completedGoals);
    }

}
