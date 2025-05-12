package com.example.designsandbox.world;

import com.example.designsandbox.DesignSandboxPlugin;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WorldManager {
    private final DesignSandboxPlugin plugin;
    private final MVWorldManager mvWorldManager;
    private final Map<UUID, String> playerWorlds;
    private final Map<String, WorldCopyTask> activeCopyTasks;

    public WorldManager(DesignSandboxPlugin plugin) {
        this.plugin = plugin;
        this.mvWorldManager = plugin.getMultiverseCore().getMVWorldManager();
        this.playerWorlds = new HashMap<>();
        this.activeCopyTasks = new HashMap<>();
        
        // Create the designs directory if it doesn't exist
        String designsPath = plugin.getConfig().getString("world.directory", "designs");
        new File(plugin.getServer().getWorldContainer(), designsPath).mkdirs();
    }

    /**
     * Gets the sandbox world name for a player
     */
    public String getWorldName(Player player) {
        String prefix = plugin.getConfig().getString("world.name-prefix", "design_");
        return prefix + player.getUniqueId();
    }

    /**
     * Checks if a player's sandbox world exists
     */
    public boolean worldExists(Player player) {
        String worldName = getWorldName(player);
        return mvWorldManager.isMVWorld(worldName);
    }

    /**
     * Creates a new sandbox world for a player
     * @return CompletableFuture that completes when the world is ready
     */
    public CompletableFuture<Boolean> createWorld(Player player, Location center) {
        String worldName = getWorldName(player);
        
        // Check if world already exists
        if (worldExists(player)) {
            return CompletableFuture.completedFuture(true);
        }

        // Check if there's already a copy task running for this world
        if (activeCopyTasks.containsKey(worldName)) {
            return activeCopyTasks.get(worldName).getFuture();
        }

        // Calculate region files to copy
        RegionCalculator calculator = new RegionCalculator(center, 
            plugin.getConfig().getInt("world.radius", 1000));

        // Create and start the copy task
        WorldCopyTask copyTask = new WorldCopyTask(plugin, player, center.getWorld(), worldName, calculator);
        activeCopyTasks.put(worldName, copyTask);

        // Start the task and return its future
        return copyTask.start().thenApply(success -> {
            activeCopyTasks.remove(worldName);
            if (success) {
                setupWorld(worldName);
                playerWorlds.put(player.getUniqueId(), worldName);
            }
            return success;
        });
    }

    /**
     * Sets up a newly created world with proper settings
     */
    private void setupWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        // Set game rules
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);

        // Set time to day
        world.setTime(6000);

        // Set weather to clear
        world.setStorm(false);
        world.setThundering(false);

        // Configure world border
        WorldBorder border = world.getWorldBorder();
        int radius = plugin.getConfig().getInt("world.radius", 1000);
        border.setCenter(world.getSpawnLocation());
        border.setSize(radius * 2);
        border.setWarningDistance(50);
        border.setWarningTime(15);
    }

    /**
     * Loads a player's sandbox world if it exists
     */
    public boolean loadWorld(Player player) {
        String worldName = getWorldName(player);
        if (!worldExists(player)) {
            return false;
        }

        if (!mvWorldManager.loadWorld(worldName)) {
            return false;
        }

        playerWorlds.put(player.getUniqueId(), worldName);
        return true;
    }

    /**
     * Unloads a player's sandbox world if it exists
     */
    public boolean unloadWorld(Player player, boolean save) {
        String worldName = getWorldName(player);
        if (!worldExists(player)) {
            return false;
        }

        World world = Bukkit.getWorld(worldName);
        if (world != null && world.getPlayers().isEmpty()) {
            if (mvWorldManager.unloadWorld(worldName, save)) {
                playerWorlds.remove(player.getUniqueId());
                return true;
            }
        }
        return false;
    }

    /**
     * Resets a player's sandbox world
     */
    public CompletableFuture<Boolean> resetWorld(Player player, Location center) {
        // First unload the world
        unloadWorld(player, false);

        // Delete the world files
        String worldName = getWorldName(player);
        File worldFolder = new File(plugin.getServer().getWorldContainer(), worldName);
        if (worldFolder.exists()) {
            // Delete world files asynchronously
            return CompletableFuture.supplyAsync(() -> {
                deleteWorldFiles(worldFolder);
                return true;
            }).thenCompose(deleted -> createWorld(player, center));
        }

        // If world didn't exist, just create a new one
        return createWorld(player, center);
    }

    /**
     * Recursively deletes a world folder
     */
    private void deleteWorldFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteWorldFiles(child);
                }
            }
        }
        file.delete();
    }

    /**
     * Gets a player's sandbox world
     */
    public World getWorld(Player player) {
        String worldName = getWorldName(player);
        return Bukkit.getWorld(worldName);
    }

    /**
     * Checks if a world is a sandbox world
     */
    public boolean isSandboxWorld(World world) {
        String prefix = plugin.getConfig().getString("world.name-prefix", "design_");
        return world.getName().startsWith(prefix);
    }

    /**
     * Cleans up resources on plugin disable
     */
    public void cleanup() {
        // Unload all sandbox worlds
        for (String worldName : playerWorlds.values()) {
            if (mvWorldManager.isMVWorld(worldName)) {
                mvWorldManager.unloadWorld(worldName, true);
            }
        }
        playerWorlds.clear();
        activeCopyTasks.clear();
    }
} 