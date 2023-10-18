package com.surv;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.surv.items.Item_Manager;

public class magic extends JavaPlugin {

  private static magic plugin;

  public static magic getPlugin() {
    return plugin;
  }

  @Override
  public void onEnable() {
    plugin = this;
    // getServer().getPluginManager().registerEvents(new Events(), this);
    // NamespacedKey key = new NamespacedKey(this, "magic_mirror");
    // ShapedRecipe recipe = new ShapedRecipe(key, Item_Manager.mm);
    // recipe.shape("E", "X", "Z");
    // recipe.setIngredient('E', Material.EXPERIENCE_BOTTLE);
    // recipe.setIngredient('X', Material.BOOK);
    // recipe.setIngredient('Z', Material.ENDER_PEARL);
    // Bukkit.addRecipe(recipe);
    Item_Manager.init();
    getServer().getPluginManager().registerEvents(new magic_mirror(), this);
    // getServer().getPluginManager().registerEvents(new menu(), this);
    getServer().getConsoleSender().sendMessage("Magic Mirror loaded");
  }

  @Override
  public void onDisable() {
    getServer().getConsoleSender().sendMessage("unloaded Magic Mirror");
  }
}
