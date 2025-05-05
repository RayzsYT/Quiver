package de.rayzs.quiver.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.rayzs.quiver.plugin.QuiverPlugin;

public class QuiverRecipeCreator {
  
  public static void registerAll() {
    for (Material material : Material.values()) {
        if (material.name().contains("BUNDLE"))
          register(material);
    }
  }

  private static void register(Material material) {
    ShapedRecipe shapedRecipe;

    ItemStack quiverStack = Quiver.createEmptyQuiver(material);
    String quiverKey = material.name();

    if (quiverKey.contains("_"))
      quiverKey = quiverKey.split("_")[0].toLowerCase() + "_";
    else 
      quiverKey = "";
    
    quiverKey += "quiver";

    shapedRecipe = new ShapedRecipe(new NamespacedKey(QuiverPlugin.getPlugin(), quiverKey), quiverStack);

    shapedRecipe.shape("AB ", "BCB", " BA")
                .setIngredient('A', Material.STRING)
                .setIngredient('B', Material.LEATHER)
                .setIngredient('C', material);

    if (Bukkit.getRecipe(shapedRecipe.getKey()) != null)
      Bukkit.removeRecipe(shapedRecipe.getKey());
    
    Bukkit.addRecipe(shapedRecipe);
  }
}