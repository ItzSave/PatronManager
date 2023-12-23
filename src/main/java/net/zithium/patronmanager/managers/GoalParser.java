package net.zithium.patronmanager.managers;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class GoalParser {
    public static Map<Integer, Double> parseGoals(ConfigurationSection section) {
        Map<Integer, Double> goals = new HashMap<>();
        if (section != null) {
            for (String goalKey : section.getKeys(false)) {
                int goalNumber = Integer.parseInt(goalKey);
                double goalAmount = section.getDouble(goalKey + ".goal", 0.0);
                goals.put(goalNumber, goalAmount);
            }
        }
        return goals;
    }
}
