package com.surv;

import org.bukkit.plugin.java.JavaPlugin;
// import org.bukkit.scheduler.BukkitScheduler;

import com.surv.items.Item_Manager;

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
    Item_Manager.init();
    // magic_sccheduler = getServer().getScheduler();
    getServer().getPluginManager().registerEvents(new magic_mirror(), this);
    getServer().getPluginManager().registerEvents(new CauldronBrewing(), this);
    getServer().getConsoleSender().sendMessage("Magic Mirror loaded");
  }

  @Override
  public void onDisable() {
    getServer().getConsoleSender().sendMessage("unloaded Magic Mirror");
  }
}
