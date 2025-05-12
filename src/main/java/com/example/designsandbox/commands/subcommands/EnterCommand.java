package com.example.designsandbox.commands.subcommands;

import com.example.designsandbox.DesignSandboxPlugin;
import com.example.designsandbox.commands.SubCommand;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class EnterCommand implements SubCommand {
    private final DesignSandboxPlugin plugin;

    public EnterCommand(DesignSandboxPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "enter";
    }

    @Override
    public boolean execute(Player player, String[] args) {
        // Check if player is already in a sandbox world
        World currentWorld = player.getWorld();
        if (plugin.getWorldManager().isSandboxWorld(currentWorld)) {
            player.sendMessage(plugin.getConfig().getString("messages.already-in-sandbox", 
                "§cYou are already in your sandbox world."));
            return false;
        }

        // Save current location for return
        Location returnLocation = player.getLocation();

        // Send initial message
        player.sendMessage(plugin.getConfig().getString("messages.enter"));

        // Create or load the sandbox world
        if (plugin.getWorldManager().worldExists(player)) {
            // World exists, just load and teleport
            if (!plugin.getWorldManager().loadWorld(player)) {
                player.sendMessage(plugin.getConfig().getString("messages.error.world-load", 
                    "§cFailed to load sandbox world."));
                return false;
            }
            teleportToSandbox(player);
        } else {
            // Create new world
            plugin.getWorldManager().createWorld(player, player.getLocation())
                .thenAccept(success -> {
                    if (success) {
                        teleportToSandbox(player);
                    } else {
                        player.sendMessage(plugin.getConfig().getString("messages.error.world-creation", 
                            "§cFailed to create sandbox world."));
                    }
                });
        }

        return true;
    }

    private void teleportToSandbox(Player player) {
        // Get the sandbox world
        World sandboxWorld = plugin.getWorldManager().getWorld(player);
        if (sandboxWorld == null) {
            player.sendMessage(plugin.getConfig().getString("messages.error.world-load", 
                "§cFailed to load sandbox world."));
            return;
        }

        // Set game mode to creative
        player.setGameMode(GameMode.CREATIVE);

        // Teleport to the same relative position
        Location currentLoc = player.getLocation();
        Location sandboxLoc = new Location(sandboxWorld,
            currentLoc.getX(),
            currentLoc.getY(),
            currentLoc.getZ(),
            currentLoc.getYaw(),
            currentLoc.getPitch()
        );

        // Ensure the chunk is loaded
        sandboxWorld.getChunkAtAsync(sandboxLoc).thenAccept(chunk -> {
            player.teleport(sandboxLoc);
            player.sendMessage(plugin.getConfig().getString("messages.world-created", 
                "Sandbox world created successfully!"));
        });
    }
} 