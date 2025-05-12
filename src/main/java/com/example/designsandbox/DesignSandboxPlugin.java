package com.example.designsandbox;

import com.example.designsandbox.commands.DesignCommand;
import com.example.designsandbox.world.WorldManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DesignSandboxPlugin extends JavaPlugin {
    private static DesignSandboxPlugin instance;
    private MultiverseCore multiverseCore;
    private LuckPerms luckPerms;
    private boolean perWorldInventoryEnabled;
    private WorldManager worldManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();

        // Check dependencies
        if (!checkDependencies()) {
            getLogger().severe("Failed to load required dependencies. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize world manager
        worldManager = new WorldManager(this);

        // Register commands
        DesignCommand designCommand = new DesignCommand(this);
        getCommand("design").setExecutor(designCommand);
        getCommand("design").setTabCompleter(designCommand);

        getLogger().info("Design-Sandbox has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean up world manager
        if (worldManager != null) {
            worldManager.cleanup();
        }

        getLogger().info("Design-Sandbox has been disabled!");
    }

    private boolean checkDependencies() {
        // Check Multiverse-Core
        Plugin mvPlugin = getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (mvPlugin == null || !(mvPlugin instanceof MultiverseCore)) {
            getLogger().severe("Multiverse-Core not found! This plugin requires Multiverse-Core to function.");
            return false;
        }
        multiverseCore = (MultiverseCore) mvPlugin;
        getLogger().info("Successfully hooked into Multiverse-Core!");

        // Check LuckPerms
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("Successfully hooked into LuckPerms!");
        } else {
            getLogger().warning("LuckPerms not found. Some permission features may be limited.");
        }

        // Check PerWorldInventory
        Plugin pwiPlugin = getServer().getPluginManager().getPlugin("PerWorldInventory");
        perWorldInventoryEnabled = pwiPlugin != null && pwiPlugin.isEnabled();
        if (perWorldInventoryEnabled) {
            getLogger().info("Successfully detected PerWorldInventory!");
        } else {
            getLogger().info("PerWorldInventory not found. Using built-in inventory management.");
        }

        return true; // Only Multiverse-Core is required
    }

    public static DesignSandboxPlugin getInstance() {
        return instance;
    }

    public MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public boolean isPerWorldInventoryEnabled() {
        return perWorldInventoryEnabled;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }
} 