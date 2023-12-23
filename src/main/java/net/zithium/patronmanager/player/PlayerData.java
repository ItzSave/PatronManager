package net.zithium.patronmanager.player;

import net.zithium.patronmanager.PatronManager;
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

            if (balance >= goalAmount) {
                // Player has reached this goal, perform associated commands.
                performGoalCommands(goalNumber, player);
            }
        }
    }

    private Map<Integer, Double> parseGoals() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("patron_goals");
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

    private void performGoalCommands(int goalNumber, Player player) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("patron_goals." + goalNumber);
        if (section != null) {
            List<String> commands = section.getStringList("commands");
            double goalAmount = section.getDouble("goal", 0.0);

            if (balance >= goalAmount) {
                // Player has reached this goal, perform associated commands.
                for (String command : commands) {
                    String replacedCommand = command.replace("{PLAYER}", player.getName());
                    List<String> commandList = new ArrayList<>();
                    commandList.add(replacedCommand);
                    plugin.getActionManager().executeActions(player, commandList);
                }

                // After successfully executing the commands, you may want to mark this goal as completed
                // to prevent it from being executed again in the future. You can use a boolean flag or
                // a database entry for this purpose.
                completedGoals.add(goalNumber);
            }
        }
    }

    public Set<Integer> getCompletedGoals() {
        return completedGoals;
    }

    public void setCompletedGoals(PlayerData playerData, Set<Integer> completedGoals) {
        this.completedGoals.clear();
        this.completedGoals.addAll(completedGoals);
    }

}
