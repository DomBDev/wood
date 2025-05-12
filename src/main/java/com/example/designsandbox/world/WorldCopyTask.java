package com.example.designsandbox.world;

import com.example.designsandbox.DesignSandboxPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

public class WorldCopyTask {
    private final DesignSandboxPlugin plugin;
    private final Player player;
    private final World sourceWorld;
    private final String targetWorldName;
    private final RegionCalculator regionCalculator;
    private final CompletableFuture<Boolean> future;
    private int progress;
    private int totalFiles;

    public WorldCopyTask(DesignSandboxPlugin plugin, Player player, World sourceWorld, 
                        String targetWorldName, RegionCalculator regionCalculator) {
        this.plugin = plugin;
        this.player = player;
        this.sourceWorld = sourceWorld;
        this.targetWorldName = targetWorldName;
        this.regionCalculator = regionCalculator;
        this.future = new CompletableFuture<>();
        this.progress = 0;
        this.totalFiles = regionCalculator.getRegions().size() + 1; // +1 for level.dat
    }

    /**
     * Starts the world copy process
     */
    public CompletableFuture<Boolean> start() {
        // Save the source world to ensure all data is written to disk
        sourceWorld.save();

        // Start the copy process asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    copyWorld();
                    future.complete(true);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to copy world: " + e.getMessage());
                    e.printStackTrace();
                    future.complete(false);
                }
            }
        }.runTaskAsynchronously(plugin);

        // Start progress updates
        startProgressUpdates();

        return future;
    }

    /**
     * Copies the world files
     */
    private void copyWorld() throws IOException {
        File serverDir = plugin.getServer().getWorldContainer();
        File sourceWorldFolder = new File(serverDir, sourceWorld.getName());
        File targetWorldFolder = new File(serverDir, targetWorldName);

        // Create target directories
        targetWorldFolder.mkdirs();
        new File(targetWorldFolder, "region").mkdirs();

        // Copy level.dat
        copyFile(new File(sourceWorldFolder, "level.dat"),
                new File(targetWorldFolder, "level.dat"));
        updateProgress();

        // Copy region files
        File sourceRegionFolder = new File(sourceWorldFolder, "region");
        File targetRegionFolder = new File(targetWorldFolder, "region");

        for (RegionCalculator.RegionCoordinate region : regionCalculator.getRegions()) {
            String fileName = region.getFileName();
            File sourceRegion = new File(sourceRegionFolder, fileName);
            File targetRegion = new File(targetRegionFolder, fileName);

            if (sourceRegion.exists()) {
                copyFile(sourceRegion, targetRegion);
            }
            updateProgress();

            // Add delay between file operations if configured
            int copyDelay = plugin.getConfig().getInt("performance.copy-delay", 50);
            if (copyDelay > 0) {
                try {
                    Thread.sleep(copyDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("World copy interrupted");
                }
            }
        }
    }

    /**
     * Copies a file with progress tracking
     */
    private void copyFile(File source, File target) throws IOException {
        if (!source.exists()) return;

        // Use NIO for efficient file copying
        Files.copy(source.toPath(), target.toPath(), 
                  StandardCopyOption.REPLACE_EXISTING,
                  StandardCopyOption.COPY_ATTRIBUTES);
    }

    /**
     * Updates the progress counter and sends feedback
     */
    private void updateProgress() {
        progress++;
        int percentage = (progress * 100) / totalFiles;

        // Send progress message to player
        String message = plugin.getConfig().getString("messages.world-creating", "Creating your sandbox world... ({progress}%)");
        message = message.replace("{progress}", String.valueOf(percentage));
        
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendActionBar(message);
            }
        }.runTask(plugin);
    }

    /**
     * Starts periodic progress updates
     */
    private void startProgressUpdates() {
        int interval = plugin.getConfig().getInt("performance.progress-interval", 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (future.isDone()) {
                    cancel();
                    return;
                }

                int percentage = (progress * 100) / totalFiles;
                String message = plugin.getConfig().getString("messages.world-creating", 
                    "Creating your sandbox world... ({progress}%)");
                message = message.replace("{progress}", String.valueOf(percentage));
                player.sendActionBar(message);
            }
        }.runTaskTimer(plugin, 0, interval);
    }

    /**
     * Gets the future that will complete when the copy is done
     */
    public CompletableFuture<Boolean> getFuture() {
        return future;
    }

    /**
     * Gets the current progress percentage
     */
    public int getProgress() {
        return (progress * 100) / totalFiles;
    }
} 