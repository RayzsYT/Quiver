package de.rayzs.quiver.plugin;

import de.rayzs.quiver.plugin.listeners.BowListener;
import de.rayzs.quiver.plugin.listeners.QuiverArrowPickupListener;
import de.rayzs.quiver.plugin.listeners.QuiverInteractionListener;
import de.rayzs.quiver.utils.QuiverRecipeCreator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuiverPlugin extends JavaPlugin {

    private static Plugin PLUGIN;

    @Override
    public void onEnable() {
        PLUGIN = this;
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(new BowListener(), this);
        manager.registerEvents(new QuiverArrowPickupListener(), this);
        manager.registerEvents(new QuiverInteractionListener(), this);

        QuiverRecipeCreator.registerAll();
    }

    @Override
    public void onDisable() {
        
    }

    public static Plugin getPlugin() {
        return PLUGIN;
    }
}
