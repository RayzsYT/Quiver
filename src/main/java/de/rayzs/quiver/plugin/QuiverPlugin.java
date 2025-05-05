package de.rayzs.quiver.plugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuiverPlugin extends JavaPlugin {

    private static Plugin PLUGIN;

    @Override
    public void onEnable() {
        PLUGIN = this;

    }

    @Override
    public void onDisable() {
        
    }

    public static final Plugin getPlugin() {
        return PLUGIN;
    }
}
