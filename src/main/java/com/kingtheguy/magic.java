package com.kingtheguy;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;
// import org.bukkit.scheduler.BukkitScheduler;

import com.kingtheguy.items.Item_Manager;

public class magic extends JavaPlugin {

  private static magic plugin;

  public static magic getPlugin() {
    return plugin;
  }
  // public static BukkitScheduler magic_sccheduler;
  // public static BukkitScheduler getScheduler() {
  // return magic_sccheduler;
  // }

  @Override
  public void onEnable() {
    plugin = this;
    // magic_sccheduler = getServer().getScheduler();

    File directory = new File(getDataFolder(), "../portalis");
    if (!directory.exists()) {
        directory.mkdirs();
    }

    Item_Manager.init();
    getServer().getPluginManager().registerEvents(new portalis(), this);
    getServer().getPluginManager().registerEvents(new CauldronBrewing(), this);
    getServer().getConsoleSender().sendMessage("Portalis loaded");
  }

  @Override
  public void onDisable() {
    getServer().getConsoleSender().sendMessage("unloaded Portalis");
  }
}
