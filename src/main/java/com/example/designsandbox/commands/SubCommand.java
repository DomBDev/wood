package com.example.designsandbox.commands;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public interface SubCommand {
    /**
     * Executes the subcommand
     * @param player The player executing the command
     * @param args The command arguments
     * @return true if the command was executed successfully, false otherwise
     */
    boolean execute(Player player, String[] args);

    /**
     * Gets the permission required to use this command
     * @return The permission node
     */
    String getPermission();

    /**
     * Gets the usage syntax for this command
     * @return The command usage
     */
    String getUsage();

    /**
     * Gets a brief description of what this command does
     * @return The command description
     */
    String getDescription();

    /**
     * Gets the name of the subcommand
     */
    String getName();

    /**
     * Gets tab completions for the subcommand
     * @param player The player requesting tab completions
     * @param args The current command arguments
     * @return A list of possible completions
     */
    default List<String> getTabCompletions(Player player, String[] args) {
        return Collections.emptyList();
    }
} 