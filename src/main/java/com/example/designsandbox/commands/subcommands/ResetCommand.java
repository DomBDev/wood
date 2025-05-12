package com.example.designsandbox.commands.subcommands;

import com.example.designsandbox.DesignSandboxPlugin;
import com.example.designsandbox.commands.SubCommand;
import com.example.designsandbox.world.WorldManager;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ResetCommand implements SubCommand {
    private final DesignSandboxPlugin plugin;
    private final WorldManager worldManager;

    public ResetCommand(DesignSandboxPlugin plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
    }

    @Override
    public boolean execute(Player player, String[] args) {
        // Check if player is in a sandbox world
        World currentWorld = player.getWorld();
        if (!worldManager.isSandboxWorld(currentWorld)) {
            String message = plugin.getConfig().getString("messages.error.not-in-sandbox", 
                "§cYou must be in your sandbox world to use this command.");
            player.sendMessage(message);
            return false;
        }

        // Send initial message
        String message = plugin.getConfig().getString("messages.reset", "Resetting your sandbox world...");
        player.sendMessage(plugin.getConfig().getString("messages.prefix", "§8[§bDesign§8] §7") + message);

        // Reset the world
        worldManager.resetWorld(player, player.getLocation())
            .thenAccept(success -> {
                if (success) {
                    String successMessage = plugin.getConfig().getString("messages.world-reset", 
                        "Your sandbox has been reset to match the current survival world.");
                    player.sendMessage(plugin.getConfig().getString("messages.prefix", "§8[§bDesign§8] §7") + successMessage);
                } else {
                    String errorMessage = plugin.getConfig().getString("messages.error.world-reset", 
                        "§cFailed to reset sandbox world: {error}")
                        .replace("{error}", "Unknown error");
                    player.sendMessage(errorMessage);
                }
            });

        return true; // Return true since we've started the reset process
    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getPermission() {
        return "designsandbox.use";
    }

    @Override
    public String getUsage() {
        return "/design reset";
    }

    @Override
    public String getDescription() {
        return "Reset your sandbox to match current survival world";
    }
} 