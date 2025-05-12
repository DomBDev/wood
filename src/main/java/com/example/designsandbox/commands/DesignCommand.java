package com.example.designsandbox.commands;

import com.example.designsandbox.DesignSandboxPlugin;
import com.example.designsandbox.commands.subcommands.EnterCommand;
import com.example.designsandbox.commands.subcommands.ExitCommand;
import com.example.designsandbox.commands.subcommands.ResetCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DesignCommand implements CommandExecutor, TabCompleter {
    private final DesignSandboxPlugin plugin;
    private final Map<String, SubCommand> subCommands;
    private final Map<String, Long> cooldowns;

    public DesignCommand(DesignSandboxPlugin plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();
        this.cooldowns = new HashMap<>();

        // Register subcommands
        registerSubCommand(new EnterCommand(plugin));
        registerSubCommand(new ExitCommand(plugin));
        registerSubCommand(new ResetCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfig().getString("messages.player-only", "§cThis command can only be used by players."));
            return true;
        }

        Player player = (Player) sender;

        // Check base permission
        if (!player.hasPermission("designsandbox.use")) {
            player.sendMessage(plugin.getConfig().getString("messages.no-permission"));
            return true;
        }

        // Default to 'enter' if no subcommand provided
        String subCommandName = args.length > 0 ? args[0].toLowerCase() : "enter";
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sendUsage(player);
            return true;
        }

        // Check subcommand permission
        if (!player.hasPermission(subCommand.getPermission())) {
            player.sendMessage(plugin.getConfig().getString("messages.no-permission"));
            return true;
        }

        // Check cooldown
        if (hasCooldown(player, subCommandName)) {
            long remainingTime = getCooldownTime(player, subCommandName);
            String message = plugin.getConfig().getString("messages.cooldown", "§cPlease wait {time} seconds before using this command again.")
                    .replace("{time}", String.valueOf(remainingTime));
            player.sendMessage(message);
            return true;
        }

        // Execute subcommand
        try {
            boolean success = subCommand.execute(player, args);
            if (success) {
                applyCooldown(player, subCommandName);
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing command: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cAn error occurred while executing the command.");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return completions;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("designsandbox.use")) {
            return completions;
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String subCmd : subCommands.keySet()) {
                SubCommand subCommand = subCommands.get(subCmd);
                if (subCmd.startsWith(partial) && player.hasPermission(subCommand.getPermission())) {
                    completions.add(subCmd);
                }
            }
        } else if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && player.hasPermission(subCommand.getPermission())) {
                completions.addAll(subCommand.getTabCompletions(player, args));
            }
        }

        return completions;
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName(), subCommand);
    }

    private void sendUsage(Player player) {
        String usage = plugin.getConfig().getString("messages.usage", "§6Design-Sandbox Commands:\n" +
                "§f/design enter §7- Enter your design sandbox world\n" +
                "§f/design exit §7- Return to the survival world\n" +
                "§f/design reset §7- Reset your sandbox to match current survival world");
        player.sendMessage(usage);
    }

    private boolean hasCooldown(Player player, String subCommand) {
        String key = player.getUniqueId() + ":" + subCommand;
        if (!cooldowns.containsKey(key)) {
            return false;
        }

        long cooldownTime = getCooldownTime(player, subCommand);
        return cooldownTime > 0;
    }

    private long getCooldownTime(Player player, String subCommand) {
        String key = player.getUniqueId() + ":" + subCommand;
        if (!cooldowns.containsKey(key)) {
            return 0;
        }

        long lastUse = cooldowns.get(key);
        int cooldownSeconds = plugin.getConfig().getInt("cooldown." + subCommand, 0);
        long cooldownExpiry = lastUse + (cooldownSeconds * 1000);
        long remaining = (cooldownExpiry - System.currentTimeMillis()) / 1000;

        return Math.max(0, remaining);
    }

    private void applyCooldown(Player player, String subCommand) {
        int cooldownSeconds = plugin.getConfig().getInt("cooldown." + subCommand, 0);
        if (cooldownSeconds > 0) {
            String key = player.getUniqueId() + ":" + subCommand;
            cooldowns.put(key, System.currentTimeMillis());
        }
    }
} 