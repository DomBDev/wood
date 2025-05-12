package com.example.designsandbox.commands.subcommands;

import com.example.designsandbox.DesignSandboxPlugin;
import com.example.designsandbox.commands.SubCommand;
import com.example.designsandbox.world.WorldManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ExitCommand implements SubCommand {
    private final DesignSandboxPlugin plugin;
    private final WorldManager worldManager;

    public ExitCommand(DesignSandboxPlugin plugin) {
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

        // Get the original world name (remove the prefix from sandbox world name)
        String prefix = plugin.getConfig().getString("world.name-prefix", "design_");
        String originalWorldName = currentWorld.getName().substring(prefix.length());
        World originalWorld = plugin.getServer().getWorld(originalWorldName);

        if (originalWorld == null) {
            String message = plugin.getConfig().getString("messages.error.world-not-found", 
                "§cCould not find your original world. Please contact an administrator.");
            player.sendMessage(message);
            return false;
        }

        // Save the player's location and state
        Location currentLocation = player.getLocation();
        
        // Switch to survival mode before teleporting
        player.setGameMode(GameMode.SURVIVAL);

        // Teleport player back to original world
        Location originalLocation = currentLocation.clone();
        originalLocation.setWorld(originalWorld);
        player.teleport(originalLocation);

        // Send success message
        String message = plugin.getConfig().getString("messages.exit", "Returning to survival world...");
        player.sendMessage(plugin.getConfig().getString("messages.prefix", "§8[§bDesign§8] §7") + message);

        // Try to unload the sandbox world if it's empty
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (currentWorld.getPlayers().isEmpty()) {
                worldManager.unloadWorld(player, true);
            }
        }, 20L); // Wait 1 second to ensure teleport is complete

        return true;
    }

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getPermission() {
        return "designsandbox.use";
    }

    @Override
    public String getUsage() {
        return "/design exit";
    }

    @Override
    public String getDescription() {
        return "Exit your design sandbox world";
    }
} 