package com.surv;

import org.bukkit.plugin.java.JavaPlugin;

public class Magic extends JavaPlugin {

  // public int getDistance(int x1, int z1, int x2, int z2) {
  // int z = x2 - x1;
  // int x = z2 - z1;
  // return (int) Math.sqrt(x * x + z * z);
  // }

  @Override
  public void onEnable() {
    // getServer().getPluginManager().registerEvents(new Events(), this);
    getServer().getPluginManager().registerEvents(new magic_mirror(), this);
    getServer().getConsoleSender().sendMessage("fuck you we is live");
  }

  @Override
  public void onDisable() {
    getServer().getConsoleSender().sendMessage("hey now offline");
  }
}
