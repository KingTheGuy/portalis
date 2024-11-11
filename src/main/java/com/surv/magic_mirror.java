package com.surv;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.surv.items.Item_Manager;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

// import com.moandjiezana.toml.Toml;
// import com.moandjiezana.toml.TomlWriter;
// import com.surv.menu;
import com.surv.DialMenu.PlayerDialMenu;

// import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

//DOING//
//TODO: particle minecraft:soul after at teleport
//TODO: save last death to file and have it load back up on restart

/////////

//SOME TIME//
//IDEA: to add custome warps the player need to have a paining in their inventory
//TODO: clean/ do re-something everything
//TODO: change the way the player recives a magic mirror 
//maybe some in world recipe
//TODO: figure out how to "add" my own item. so that the magic mirror is stricktly made
//TODO: implement save/ reload data.

//TODO: i could possibly add in the TP to other players again..

//TODO: add a confirmation menu or sub menu.. not sure
//will be needed for reseting home tp or teleporting somewhere.

//TODO: move SPAWN, SHOPS.. and any other server wide warps to its own menu

//TODO: reset/increase the blindness effect when the player selectes a menu option

//FIXME: let player know not to have a shield in hand.. 
//or add code to ignore the shield or any other item in offhand

public class magic_mirror implements Listener {

  ///// Config////
  // String TheItemName = "Magic Mirror";
  // Material TheItemType = Material.BOOK;
  //// END////

  // TODO: add a delay, on item use.. will cause some problems otherwise.
  // may need to change the confirmation on select to be crouch.
  // WHAT AM I DOING WRONG HERE?//
  String global_warps_file = "global_warps.json";
  String player_warps_file = "player_warps.json";
  public static List<player_deaths> deaths = new ArrayList<>();

  public static List<GlobalWarps> global_warps = new ArrayList<>();
  public static List<PlayerWarps> player_warps = new ArrayList<>();
  public static List<delayTimer> delay_timer_list = new ArrayList<>();

  // TODO: when a player warps to a location add then to the used_lode list
  // when they walk out of its location remove them from the list
  // TODO: when a player walk to a location of a warp open the warp locations menu
  // and add them to the used_lode list.
  // make sure they have "access" to the warp before opening the menu
  public static List<UsedWarp> just_used_lode_warp = new ArrayList<>();

  public class UsedWarp {
    Player player;
    String warp_name;
    Location loc;
    // nvm should not do this here?? because the the tp sound also needs to play for
    // when using the MM. so i makes not sense to have the "logic" in seperate
    // places.
    // boolean at_destinatoin; //TODO: once the player's location changes to the
    // destination (at which point play the tp sound)
    boolean off_lodestone; // TODO: once at_destination is true and the player steps off the their new
                           // location (at which point them menu can be prompt again)
  }

  // TODO: implement this timmer thing
  public class delayTimer {
    String player;
    int timer;

    public void countDown() {
      timer--;
      if (timer > 0) {
        delay_timer_list.remove(this);
      }
    }

  }

  public void addWarp(Location location, String player_name, String warp_name, Audience audience) {
    GlobalWarps warp_to_add = new GlobalWarps();
    warp_to_add.location = new Vector3();
    warp_to_add.location.X = location.getBlockX();
    warp_to_add.location.Y = location.getBlockY();
    warp_to_add.location.Z = location.getBlockZ();
    warp_to_add.dimension_name = location.getWorld().getName();
    warp_to_add.creator = player_name;
    // FIXME: this should take into account if the player is in creative mode. if
    // they are dont add the prefix to the name.
    warp_to_add.name = warp_name;
    for (GlobalWarps w : global_warps) {
      // System.out.printf("warps:\n\n");
      // System.out.printf("new: [%s]\n\n", warp_to_add.location.toString());
      // System.out.printf("found: [%s]\n\n", w.location.toString());
      if (w.location.toString().equals(warp_to_add.location.toString())) {
        if (w.dimension_name.equals(warp_to_add.dimension_name)) {
          // System.out.println("location already exists");
          return;
        }
      }
    }
    for (GlobalWarps w : global_warps) {
      if (warp_name.equals(w.name)) {
        Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
            Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 1f);
        audience.sendActionBar(
            () -> Component.text(ChatColor.LIGHT_PURPLE + "Choose another name"));
        return;
      }
    }
    Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
        Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 1f);
    audience.sendActionBar(
        () -> Component.text("Warp created"));
    global_warps.add(warp_to_add);
    addPlayerWarp(location, player_name);
    saveGlobalWarpsToFile(global_warps_file);
  }

  List<UsedWarp> just_warped = new ArrayList<>();

  public void playerLocationChanged() {
    List<UsedWarp> to_remove = new ArrayList<>();
    for (UsedWarp used_warp : just_warped) {
      if (!used_warp.loc.equals(used_warp.player.getLocation())) {
        // System.out.println(String.format("was at %s, played sound at
        // %s",used_warp.loc,used_warp.player.getLocation()));
        teleportEffectSound(used_warp.player, used_warp.player.getLocation());
        used_warp.player.addPotionEffect(
            new PotionEffect(PotionEffectType.CONFUSION, 120, 1).withAmbient(false).withParticles(false));
        used_warp.player.addPotionEffect(
            new PotionEffect(PotionEffectType.DARKNESS, 60, 1).withAmbient(false).withParticles(false));

        // just_warped.remove(used_warp);
        to_remove.add(used_warp);
      }
    }
    for (UsedWarp remove_this : to_remove) {
      just_warped.remove(remove_this);
    }
    // System.out.println(String.format("just_warped size: %s",just_warped.size()));
  }

  public void addPlayerWarp(Location location, String player) {
    // TODO(done): check if player is in the list (what list?)
    // TODO: check if the player already has this warp spot
    GlobalWarps warp_to_add = new GlobalWarps();
    warp_to_add.location = new Vector3();
    warp_to_add.location.X = location.getBlockX();
    warp_to_add.location.Y = location.getBlockY();
    warp_to_add.location.Z = location.getBlockZ();
    warp_to_add.dimension_name = location.getWorld().getName();
    // check if the warp has been created
    String found_player = null; // check if player had a list
    for (PlayerWarps pw : player_warps) {
      if (pw.player_name.equals(player)) {
        // System.out.println("found the player");
        found_player = pw.player_name;
      } else {
        // System.out.println("did not find the player..");
      }
    }
    for (GlobalWarps w : global_warps) {
      if (w.location.toString().equals(warp_to_add.location.toString())) {
        if (w.dimension_name.equals(warp_to_add.dimension_name)) {

          // check if player is in warp add them
          if (found_player == null) {
            // System.out.println("did not find the player, creating new list..");
            PlayerWarps new_player_warp = new PlayerWarps();
            found_player = player;
            new_player_warp.player_name = player;
            player_warps.add(new_player_warp);
          }
          for (PlayerWarps pw : player_warps) {
            if (pw.player_name.equals(found_player)) {
              // System.out.println("player is in fact found");
              for (GlobalWarps pww : pw.known_warps) {
                if (w.location.toString().equals(pww.location.toString())) {
                  if (w.dimension_name.equals(pww.dimension_name)) {
                    // System.out.println("the player is already aware of this warp.");
                    return;
                  }
                }
              }
              pw.known_warps.add(w);
              savePlayerWarpsToFile(player_warps_file);
              break;
            }
          }
          return;
        }
      }
    }
    // new_player_warp.known_warps

    // global_warps.add(warp_to_add);
    // saveGlobalWarpsToFile(global_warps_file);
    // System.out.println("new location saved");
  }

  class Vector3 implements Serializable {
    int X;
    int Y;
    int Z;

    @Override
    public String toString() {
      return String.format("%s,%s,%s", X, Y, Z);
    }
  }

  class GlobalWarps implements Serializable {
    String creator;
    Vector3 location = new Vector3();
    String dimension_name;
    String name;

    @Override
    public String toString() {
      return String.format("[warp] <%s>, <%s>, <%s>\n", location, dimension_name, name);

    }

    public void particlesAndLight() {
      int min = 2;
      int max = 8;
      float x = (new Random().nextInt(max - min + 1) + min);
      float y = (new Random().nextInt(max - min + 1) + min);
      float z = (new Random().nextInt(max - min + 1) + min);
      Location location = new Location(Bukkit.getWorld(this.dimension_name), this.location.X,
          this.location.Y, this.location.Z);
      Location location_above = new Location(Bukkit.getWorld(this.dimension_name), this.location.X,
          this.location.Y + 1, this.location.Z);
      if (location_above.getBlock().getType().equals(Material.AIR)) {
        location_above.getBlock().setType(Material.LIGHT);
        Light light_level = (Light) location_above.getBlock().getBlockData();
        light_level.setLevel(8);
        location_above.getBlock().setBlockData(light_level);
      }
      // location.getWorld().playSound(location, Sound.BLOCK_PORTAL_TRAVEL, 1,
      // (float) 0.5);
      // Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.BUBBLE_POP,
      // location.getBlockX() + (x / 10),
      // location.getBlockY() + 1, location.getBlockZ() + (z / 10), 0);
      Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.END_ROD,
          location.getBlockX() + (x / 10),
          location.getBlockY() + 1.5 + (y / 10), location.getBlockZ() + (z / 10), 0);
      // location.getBlockY() + 2.2, location.getBlockZ() + (z / 10), 0);
    }

    public void PromptMenu() {
      // System.out.println(this.dimension_name);
      // Location location = new Location(Bukkit.getWorld(this.dimension_name),
      // this.location.X,
      // this.location.Y + 1, this.location.Z);
      Location location = new Location(Bukkit.getWorld(this.dimension_name), this.location.X,
          this.location.Y + 1, this.location.Z);
      Collection<Entity> entities = location.getNearbyEntities(location.getBlockX(), location.getBlockY(),
          location.getBlockZ());
      if (entities.size() > 0) {
        entities.forEach(entity -> {
          if (entity instanceof Player) {
            // System.out.println(String.format("is location null? %s",location));
            // System.out.println(String.format("is entity locatoin null? %s",location));
            if (Utils.get3DDistance(location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                entity.getLocation().getBlockX(), entity.getLocation().getBlockY(),
                entity.getLocation().getBlockZ()) < 1) {
              // System.out.println("yes this mf is close to a warp location");
              boolean found = false;
              for (UsedWarp used_w : just_used_lode_warp) {
                if (used_w.player == (Player) entity) {
                  found = true;
                  continue;
                }
              }
              if (found == false) {
                UsedWarp new_user = new UsedWarp();
                new_user.player = (Player) entity;
                new_user.warp_name = this.name;
                just_used_lode_warp.add(new_user);
              }
              // System.out.println(String.format("size: %s", just_used_lode_warp.size()));

            }
          }
        });
      }
    }

    public void RemoveWarp() {
      Location location = new Location(Bukkit.getWorld(this.dimension_name), this.location.X,
          this.location.Y, this.location.Z);
      Location location_above = new Location(Bukkit.getWorld(this.dimension_name), this.location.X,
          this.location.Y + 1, this.location.Z);
      if (location.getBlock().getType().equals(Material.LODESTONE)) {
        if (location_above.getBlock().getType().equals(Material.LIGHT)) {
          location_above.getBlock().setType(Material.AIR);
        }
      }
      if (!location.getBlock().getType().equals(Material.LODESTONE)) {
        warp_remove_list.add(this);
      }
    }
  }

  static List<GlobalWarps> warp_remove_list = new ArrayList<>();

  @EventHandler
  public void tick(ServerTickStartEvent event) {
    if (global_warps.size() > 0) {
      if (event.getTickNumber() % 10 == 1) {
        for (GlobalWarps w : global_warps) {
          w.PromptMenu();
        }
        // check if the player has a lode warp beneath them
        if (just_used_lode_warp.size() > 0) {
          // boolean on_warp = false;
          List<UsedWarp> remove_this = new ArrayList<>();
          for (UsedWarp player_used_warp : just_used_lode_warp) {
            // System.out.println("yes this is running");
            if (player_used_warp.player.getLocation().subtract(0, 1, 0).getBlock().getType()
                .equals(Material.LODESTONE)) {
              // on_warp = true;
              // for each player's warp compare to global warps, remove if needed, rename if
              // needed.
              List<String> prompt_list = new ArrayList<>();
              List<GlobalWarps> to_remove = new ArrayList<>();
              for (PlayerWarps player_know_warp : player_warps) {
                if (player_know_warp.player_name.equals(player_used_warp.player.getName())) {
                  for (GlobalWarps player_global_warps : player_know_warp.known_warps) {
                    if (findPlayerWarpInGlobalWarps(player_global_warps) == null) {
                      to_remove.add(player_global_warps);
                    } else {
                      if (player_global_warps.name == player_used_warp.warp_name) {
                        // this prevents the current location from being showned right??
                      } else {
                        if (player_global_warps.name != null) {
                          prompt_list.add(player_global_warps.name);
                        } else {
                          prompt_list.add(player_global_warps.location.toString());

                        }
                      }
                    }
                  }
                  for (GlobalWarps w : to_remove) {
                    player_know_warp.known_warps.remove(w);
                  }
                  continue; // found the player
                }
              }
              Collections.sort(prompt_list, String.CASE_INSENSITIVE_ORDER);
              dialMenu.openDialMenu("MM:locations", prompt_list, player_used_warp.player, null);
              saveGlobalWarpsToFile(global_warps_file);
              savePlayerWarpsToFile(player_warps_file);
              continue;
            }
            remove_this.add(player_used_warp);
          }
          for (UsedWarp removeing_usedWarp : remove_this) {
            dialMenu.closeMenu(removeing_usedWarp.player);
            just_used_lode_warp.remove(removeing_usedWarp);
          }
        }
        // NOTE: these should be closer to the end right??
        playerLocationChanged();
        ToBeRemoved();
      }
    }
  }

  private void ToBeRemoved() {
    for (GlobalWarps w : global_warps) {
      w.RemoveWarp();
      w.particlesAndLight(); // show some particles
    }
    for (GlobalWarps w : warp_remove_list) {
      // System.out.println("warp has been removed.");
      global_warps.remove(w);
      saveGlobalWarpsToFile(global_warps_file);
    }
    warp_remove_list.clear();
  }

  public GlobalWarps findPlayerWarpInGlobalWarps(GlobalWarps player_warp) {
    for (GlobalWarps w : global_warps) {
      if (w.location.toString().equals(player_warp.location.toString())) {
        if (w.dimension_name.equals(player_warp.dimension_name)) {
          if (w.name != null) {
            player_warp.name = w.name; // rename it
          }
          return w;
        }
      }
    }
    return null;
  }

  public GlobalWarps isThisAWarpLocation(Location location) {
    Vector3 location_xyz = new Vector3();
    location_xyz.X = location.blockX();
    location_xyz.Y = location.blockY();
    location_xyz.Z = location.blockZ();
    for (GlobalWarps w : global_warps) {
      if (w.location.toString().equals(location_xyz.toString())) {
        if (w.dimension_name.equals(location.getWorld().getName().toString())) {
          return w;
        }
      }
    }
    return null;
  }

  class PlayerWarps implements Serializable {
    String player_name;
    List<GlobalWarps> known_warps = new ArrayList<>();
  }

  class player_deaths {
    public Player name;
    public Location loc;

  }

  public String PLAYER_WARPS = "player_warps.yaml";

  public class warp_options_cost {
    private String name;
    private int xp_cost;

    public warp_options_cost(String name, int xp) {
      this.name = name;
      this.xp_cost = xp;

    }

    public String getName() {
      return this.name;

    }

    public int getXPCost() {
      return this.xp_cost;
    }
  }

  ArrayList<WarpPlayer> avalible_for_warp = new ArrayList<>();

  public class WarpPlayer {
    Player player;

    public void removePlayer() {
      avalible_for_warp.remove(this);
    }
  }

  public void addPlayerToAvalibleForWarp(Player player) {
    WarpPlayer warpable_player = new WarpPlayer();
    warpable_player.player = player;
    avalible_for_warp.add(warpable_player);
  }

  ArrayList<warp_options_cost> main_menu_selections = new ArrayList<>();

  // HUH//
  // menu prompt = new menu();
  // Menu_updated new_prompt = new Menu_updated();
  DialMenu dialMenu = new DialMenu();

  @EventHandler
  public void onLeave(PlayerQuitEvent ev) {
    Player player = ev.getPlayer();
    dialMenu.closeMenu(player);
    UsedWarp remove_this = new UsedWarp();
    for (UsedWarp p : just_used_lode_warp)
      if (just_used_lode_warp.contains(p)) {
        remove_this = p;
      }
    just_used_lode_warp.remove(remove_this);
    // new_prompt.closeMenu(player);
  }

  public void saveGlobalWarpsToFile(String file) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      Gson gson = new Gson();
      String json = gson.toJson(global_warps);
      // System.out.println(json);
      // for (GlobalWarps w : global_warps) {
      // writer.write("[warp]\n");
      // writer.write(String.format("\t-location:%s\n", w.location.toString()));
      // writer.write(String.format("\t-dimension:%s\n", w.dimension_name));
      // writer.write(String.format("\t-name:%s\n", w.name));
      // writer.write(String.format("\t-creator:%s\n", w.creator));
      // writer.write("\n");
      // }
      writer.write(json);
      writer.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void savePlayerWarpsToFile(String file) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      Gson gson = new Gson();
      String json = gson.toJson(player_warps);
      // System.out.println(json);
      // for (PlayerWarps pw : player_warps) {
      // writer.write(String.format("PLAYER:%s\n", pw.player_name));
      // for (GlobalWarps w : pw.known_warps) {
      // writer.write("\t[warp]\n");
      // writer.write(String.format("\t\t-location:%s\n", w.location.toString()));
      // writer.write(String.format("\t\t-dimension:%s\n", w.dimension_name));
      // writer.write(String.format("\t\t-name:%s\n", w.name));
      // writer.write(String.format("\t\t-creator:%s\n", w.creator));
      // writer.write("\n");
      // }
      // writer.write("\n");
      // }
      writer.write(json);
      writer.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void loadGlobalWarpsFromFile(String file) {
    try {
      Gson gson = new Gson();
      JsonReader reader = new JsonReader(new FileReader(file));
      // BufferedReader reader = new BufferedReader(new FileReader(file));
      // String line;
      Type listOfMyClassObject = new TypeToken<List<GlobalWarps>>() {
      }.getType();

      List<GlobalWarps> warps = gson.fromJson(reader, listOfMyClassObject);
      for (GlobalWarps w : warps) {
        global_warps.add(w);
      }
      // GlobalWarps warp_from_file = new GlobalWarps();
      // while ((line = reader.readLine()) != null) {
      // String trimmed = line.trim();
      // if (trimmed.startsWith("[warp")) {
      // warp_from_file = new GlobalWarps();
      // }
      // if (trimmed.startsWith("-location")) {
      // String[] split = trimmed.split(":", -1);
      // String[] xyz = split[1].split(",", -1);
      // warp_from_file.location = new Vector3();
      // warp_from_file.location.X = Integer.parseInt(xyz[0]);
      // warp_from_file.location.Y = Integer.parseInt(xyz[1]);
      // warp_from_file.location.Z = Integer.parseInt(xyz[2]);
      // }
      // if (trimmed.startsWith("-dimension")) {
      // String[] split = trimmed.split(":", -1);
      // warp_from_file.dimension_name = split[1];
      // }
      // if (trimmed.startsWith("-name")) {
      // String[] split = trimmed.split(":", -1);
      // if (split[1] == "null") {
      // warp_from_file.name = null;
      // } else {
      // warp_from_file.name = split[1];
      // }
      // }
      // if (trimmed.startsWith("-creator")) {
      // String[] split = trimmed.split(":", -1);
      // warp_from_file.creator = split[1];

      // }
      // if (trimmed.isBlank()) {
      // if (warp_from_file != null) {
      // global_warps.add(warp_from_file);
      // warp_from_file = new GlobalWarps();
      // }
      // }
      // }
      reader.close();
    } catch (IOException e) {
      System.out.println(e);
    }

  }

  // FIXME: i just need to redo the loading and saving
  public void loadPlayerWarpsFromFile(String file) {
    try {
      Gson gson = new Gson();
      JsonReader reader = new JsonReader(new FileReader(file));
      // player_warps = gson.fromJson(reader, PlayerWarps.class);
      Type listOfMyClassObject = new TypeToken<List<PlayerWarps>>() {
      }.getType();
      List<PlayerWarps> warps = gson.fromJson(reader, listOfMyClassObject);
      for (PlayerWarps w : warps) {
        player_warps.add(w);
      }
      // String line;
      // PlayerWarps player_warp_from_file = new PlayerWarps();
      // // PlayerWarps player_warp_from_file;
      // GlobalWarps warp = new GlobalWarps();
      // while ((line = reader.readLine()) != null) {
      // String trimmed = line.trim();
      // // start here
      // if (trimmed.startsWith("PLAYER:")) {
      // // this means that player has some usefull data that i shold save
      // if (player_warp_from_file.known_warps.size() > 0) {
      // player_warps.add(player_warp_from_file);
      // player_warp_from_file = new PlayerWarps();
      // }
      // player_warp_from_file = new PlayerWarps();
      // String[] split = trimmed.split(":", -1);
      // player_warp_from_file.player_name = split[1];
      // player_warp_from_file.known_warps = new ArrayList<GlobalWarps>();
      // }
      // if (trimmed.startsWith("[warp")) {
      // warp = new GlobalWarps();
      // }
      // if (trimmed.startsWith("-location")) {
      // String[] split = trimmed.split(":", -1);
      // String[] xyz = split[1].split(",", -1);
      // warp.location = new Vector3();
      // warp.location.X = Integer.parseInt(xyz[0]);
      // warp.location.Y = Integer.parseInt(xyz[1]);
      // warp.location.Z = Integer.parseInt(xyz[2]);
      // }
      // if (trimmed.startsWith("-dimension")) {
      // String[] split = trimmed.split(":", -1);
      // warp.dimension_name = split[1];
      // }
      // if (trimmed.startsWith("-name")) {
      // String[] split = trimmed.split(":", -1);
      // if (split[1] == "null") {
      // warp.name = null;
      // } else {
      // warp.name = split[1];
      // }
      // }
      // if (trimmed.startsWith("-creator")) {
      // String[] split = trimmed.split(":", -1);
      // warp.creator = split[1];
      // }
      // // add the new warp to the player's
      // if (trimmed.isEmpty()) {
      // if (warp != null) {
      // player_warp_from_file.known_warps.add(warp);
      // warp = null;
      // }
      // }
      // }
      // if (player_warp_from_file.known_warps.size() > 0) {
      // player_warps.add(player_warp_from_file);
      // }
      reader.close();
      // savePlayerWarpsToFile(player_warps_file); // TODO: remove this line
      // System.out.printf("player warp spots: %s\n", player_warps.toString());
    } catch (IOException e) {
      System.out.println(e);
    }

  }

  @EventHandler
  public void onServerStart(ServerLoadEvent ev) {
    loadGlobalWarpsFromFile(global_warps_file);
    loadPlayerWarpsFromFile(player_warps_file);
    // try {
    // global_warps = Data.deserializeList(global_warps_file);
    // } catch (IOException | ClassNotFoundException e) {
    // System.out.println("this shit did not load right");
    // System.out.printf("here is the error: %s\n", e);
    // }
    // try {
    // player_warps = Data.deserializeList(player_warps_file);
    // } catch (IOException | ClassNotFoundException e) {
    // }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent ev) {
    dialMenu.refreshDialMenu(ev.getPlayer());
  }

  // public void saveToFile() {
  // StringBuilder builder = new StringBuilder();
  // builder.append()

  // }

  // @EventHandler
  // public void onPlayerAttac(PlayerInteractEvent event) {
  // Player player = event.getPlayer();
  // if (player.getInventory().getItemInMainHand().isEmpty()) {
  // return;
  // }
  // if
  // (player.getInventory().getItemInMainHand().getType().equals(Item_Manager.magic_mirror_book.getType()))
  // {
  // System.out.println("YES this should be working..");
  // }
  // }

  // FIXME: Issue when player clicks blocks with the item.
  // NOTE: fixed this by changing click to crouch. to confirm selection
  // @EventHandler
  // public void onPlayerSneak(PlayerInteractEvent ev) {

  // }

  // public void createItem(CraftItemEvent event) {
  // magic.getPlugin().getLogger().warning("is this working?");
  // event.setResult(null);
  // ItemStack gold_ingot = new ItemStack(Material.GOLD_INGOT);
  // if (event.getRecipe().getResult().equals(gold_ingot)) {
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().addItem(Item_Manager.coin);
  // event.getInventory().remove(Material.GOLD_INGOT);
  // }
  // }

  public void useBook(Player player) {
    if (player.getGameMode().equals(GameMode.CREATIVE)) {
      return; // let them work
    }
    ItemStack item = player.getInventory().getItemInMainHand();
    if (item.isEmpty()) {
      return;
    }
    ItemStack magic_mirror_no_data = new ItemStack(Item_Manager.magic_mirror_book);
    ItemMeta magic_meta = magic_mirror_no_data.getItemMeta();

    ItemStack c_hand_item = item.clone();
    c_hand_item.setItemMeta(magic_meta);
    if (!c_hand_item.equals(magic_mirror_no_data)) {
      // System.out.println("not the same!!");
      return;
    }

    NamespacedKey key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();
    Integer cur_value = container.get(key, PersistentDataType.INTEGER);
    Integer max_uses = 6;
    Integer new_value = cur_value;
    if (cur_value > 0) {
      new_value = cur_value - 1;
    }
    container.set(key, PersistentDataType.INTEGER, new_value);
    List<Component> lore = new ArrayList<>();
    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, new_value);
    item.setItemMeta(meta);
    lore.add(Component.text(String.format("%s/%s uses", new_value, max_uses)));
    item.lore().clear();
    item.lore(lore);
  }

  public void teleportEffectSound(Player player, Location location) {
    boolean in_just_warped = false;
    for (UsedWarp p : just_warped) {
      if (p.player.getName().equals(player.getName())) {
        in_just_warped = true;
        break;
      }
    }
    if (in_just_warped == false) {
      UsedWarp new_warping_player = new UsedWarp();
      new_warping_player.player = player;
      new_warping_player.loc = location;
      just_warped.add(new_warping_player);
    }
    // play this for everyone
    Bukkit.getWorld(location.getWorld().getUID()).playSound(location, Sound.ENTITY_SHULKER_TELEPORT,
        SoundCategory.BLOCKS, 1f, 1f);
    // play this just for the player
    // player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1f, 1f);

    // Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
    // Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    // Bukkit.getWorld(location.getWorld().getUID());
    // Audience audience = Audience.audience(player);
    // audience.playSound(Sound.ENTITY_SHULKER_TELEPORT,
    // location.getX(),location.getY(),location.getZ());
    // player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f,
    // 1f);
  }

  public boolean isBookOutOfPages(PlayerInteractEvent ev) {
    Player player = ev.getPlayer();
    Audience audience = Audience.audience(player);
    NamespacedKey key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
    ItemMeta meta = ev.getItem().getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();
    Integer cur_value = container.get(key, PersistentDataType.INTEGER);
    if (cur_value == 0) {
      dialMenu.closeMenu(player);
      audience.sendActionBar(() -> Component.text("out of pages").color(NamedTextColor.RED));
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
      return true;
    }
    return false;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent ev) {
    Player player = ev.getPlayer();
    if (ev.getAction().equals(Action.PHYSICAL)) {
      return;
    }

    if (!ev.getHand().equals(EquipmentSlot.HAND)) {
      return;
    }
    ItemStack item = player.getInventory().getItemInMainHand();
    Material itemType = item.getType();
    Action action = ev.getAction();
    Audience audience = Audience.audience(player);

    // if (ev.getClickedBlock().getType().equals(Material.LODESTONE)) {
    // ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
    // player.openBook(book);
    // }
    PlayerDialMenu dm = dialMenu.getPlayerDialMenu(player);

    // FIXME: ive got too many of thse dm!=null stuff, just one is needed(or check
    // for item in hand before hand)
    final String go_back_prompt = "~GO-BACK~";
    final String cancel_prompt = "~CLOSE-BOOK~";

    final String del_prompt = "~DEL~";
    final String done_prompt = "~DONE~";
    if (dm != null) {
      dm.playerChooseSelection(ev);
      ev.setCancelled(true);
      switch (dm.dial_id) {
        case "MM:locations":
          switch (dm.selection_answer) {
            case cancel_prompt:
              dialMenu.closeMenu(player);
              return;
            default:
              // System.out.println("ok so default it is.");
              for (PlayerWarps pw : player_warps) {
                if (pw.player_name.equals(ev.getPlayer().getName())) {
                  // System.out.println("found the player's stuff.");
                  for (GlobalWarps w : pw.known_warps) {
                    if (w.name.equals(dm.selection_answer)) {
                      // System.out.println("found the player's selected warp.");
                      Location teleport_location = new Location(Bukkit.getWorld(w.dimension_name), w.location.X + 0.5,
                          w.location.Y + 1, w.location.Z + 0.5);
                      player.teleportAsync(teleport_location);
                      teleportEffectSound(player, player.getLocation());
                      dialMenu.closeMenu(player);
                      useBook(player);
                      // teleportEffectSound(player, teleport_location);
                      return;
                    }
                  }
                  return;
                }
              }
              return;
          }
        case String s when s.contains("MM:create_warp_name_prefix"):
          switch (dm.selection_answer) {
            case "create warp":
              // player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1f);
              String player_name = ev.getPlayer().getName();
              // String[] split = dm.dial_id.split(":",-1);
              // String dial_id = String.format("MM:create_warp_name_subname:%s", split[2]);
              String dial_id = String.format("MM:create_warp_name_subname:%s", dm.getDataFromId());
              System.out.println(String.format("the id looks like this: %s", dial_id));
              dialMenu.openDialMenu(dial_id,
                  List.of(
                      player_name.substring(0, 2),
                      player_name.substring(0, 3),
                      player_name.substring(0, 4),
                      player_name.substring(0, 5),
                      player_name,
                      "-CUSTOM-"),
                  player,
                  Item_Manager.magic_mirror_book);
              return;
            case "cancel":
              dialMenu.closeMenu(player);
              return;
          }
          return;
        case String s when s.contains("MM:create_warp_name_subcustom"):
          player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, 1f);
          if (dm.selection_answer == del_prompt) {
            StringBuilder w = new StringBuilder(dm.custom_answer);
            dm.custom_answer = w.deleteCharAt(w.length() - 1).toString();
            dialMenu.openDialMenu(String.format("MM:create_warp_name_subcustom:%s", dm.getDataFromId()),
                dialMenu.alphabet(), player, null);
            return;
          }
          if (dm.selection_answer == done_prompt) {
            // dialMenu.openDialMenu(, , , );
            dm.selection_answer = dm.custom_answer;
            dialMenu.openDialMenu(String.format("MM:create_warp_name_done:%s", dm.getDataFromId()), List.of(
                dm.selection_answer), player, Item_Manager.magic_mirror_book);
            dm.custom_answer = "";
            return;
          }
          dm.custom_answer = String.format("%s%s", dm.custom_answer, dm.selection_answer);
          dialMenu.openDialMenu(String.format("MM:create_warp_name_subcustom:%s", dm.getDataFromId()),
              dialMenu.alphabet(), player, null);
          return;
        case String s when s.contains("MM:create_warp_name_subname"):
          // String player_name = ev.getPlayer().getName();
          String prefix = dm.selection_answer;
          // String[] split_again = dm.dial_id.split(":",-1);
          if (prefix == "-CUSTOM-") {
            dialMenu.openDialMenu(String.format("MM:create_warp_name_subcustom:%s", dm.getDataFromId()),
                dialMenu.alphabet(), player, null);
            // player.playSound(player.getLocation(),
            // Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, 1f);
            return;
          }
          String dial_id = String.format("MM:create_warp_name_done:%s", dm.getDataFromId());
          System.out.println(String.format("the id looks like this: %s", dial_id));
          dialMenu.openDialMenu(dial_id,
              List.of(
                  String.format("%s's %s", prefix, "Base"),
                  String.format("%s's %s", prefix, "Home"),
                  String.format("%s's %s", prefix, "Castle"),
                  String.format("%s's %s", prefix, "Fort"),
                  String.format("%s's %s", prefix, "Fortress"),
                  String.format("%s's %s", prefix, "Stronghold"),
                  String.format("%s's %s", prefix, "Camp"),
                  String.format("%s's %s", prefix, "Bunker"),
                  String.format("%s's %s", prefix, "Mansion"),
                  String.format("%s's %s", prefix, "Village"),
                  String.format("%s's %s", prefix, "Farm")),
              player,
              Item_Manager.magic_mirror_book);
          return;
        case String s when s.contains("MM:create_warp_name_done"):

          // player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f,
          // 1f);
          String warp_name = dm.selection_answer;
          // String[] split_3 = dm.dial_id.split(":",-1);
          String[] split_loc = dm.getDataFromId().split("_", -1);
          Location location = player.getLocation();
          location.setX(Double.parseDouble(split_loc[0]));
          location.setY(Double.parseDouble(split_loc[1]));
          location.setZ(Double.parseDouble(split_loc[2]));
          // CREATE THE WARP
          dialMenu.closeMenu(player);
          addWarp(location, ev.getPlayer().getName(), warp_name, audience);
          return;
        default:
      }
    }

    if (itemType.equals(Material.STICK)) {
      // DialMenu.player_with_menu();
      ev.setCancelled(true);
      if (dm != null) {
        dm.playerChooseSelection(ev);
        switch (dm.dial_id) {
          case "mm:stick_main":
            System.out.println(String.format("what was the answer?", dm.selection_answer));
            if (dm.selection_answer == "/spark tps") {
              dialMenu.openDialMenu("mm:i_see", List.of("you chose tps", "good for you", "menu_is_i_see"), player,
                  new ItemStack(Material.STICK));
              // dialMenu.closeMenu(player);
            } else {
              dialMenu.openDialMenu("mm:stick_now", List.of("so what", "now then", "you or me"), player,
                  new ItemStack(Material.STICK));
            }
            return;
          case "mm:stick_now":
            dialMenu.openDialMenu("mm:stick_two", List.of("rename-warp", "remove-warp"), player,
                new ItemStack(Material.STICK));
            return;
          case "mm:stick_two":
            if (dm.selection_answer == "rename-warp") {
              String player_name = ev.getPlayer().getName();
              dialMenu.openDialMenu("mm:rename-warp",
                  List.of(player_name.substring(0, 2), player_name.substring(0, 3), player_name.substring(0, 4),
                      player_name),
                  player,
                  new ItemStack(Material.STICK));
            }
            if (dm.selection_answer == "remove-warp") {

            }
            // dialMenu.closeMenu(player);
            return;
          case "mm:rename-warp":
            String prefix = dm.selection_answer;
            dialMenu.openDialMenu("mm:renamed-warp", List.of(
                String.format("%s's %s", prefix, "BASE"),
                String.format("%s's %s", prefix, "CASTLE"),
                String.format("%s's %s", prefix, "HOME"),
                String.format("%s's %s", prefix, "VILLAGE"),
                String.format("%s's %s", prefix, "FARM")), player,
                new ItemStack(Material.STICK));
            return;
          case "mm:renamed-warp":
            dialMenu.closeMenu(player);
            audience.sendActionBar(
                () -> Component.text(ChatColor.AQUA + String.format("warp renamed to [ %s ]", dm.selection_answer)));
            return;
        }
      } else {
        dialMenu.openDialMenu("mm:stick_main", List.of("/co near", "/co inspect", "/spark tps", "/vote kick", "CLOSE"),
            player, new ItemStack(Material.STICK));
      }
      return;
    }

    // TODO: min level of xp needed to store is 10 levels.. should also give back 10
    // levels
    { // Bottled xp
      if (itemType.equals(Material.GLASS_BOTTLE)) {
        if (player.isSneaking()) {
          int xp = player.getLevel();
          if (xp >= 10) {
            player.setLevel(xp - 10);
            // item.setType(Material.EXPERIENCE_BOTTLE);
            player.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE));

            player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.EXPERIENCE_BOTTLE));
            player.playSound(player.getLocation(), Sound.BLOCK_BEEHIVE_DRIP, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1f, 1f);
          } else {
            audience.sendActionBar(() -> Component.text(ChatColor.RED + "Need 10xp levels"));
            player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1f, 1f);
          }
        }
      }
      if (itemType.equals(Material.EXPERIENCE_BOTTLE)) {
        if (player.isSneaking()) {
          int xp = player.getLevel();
          player.setLevel(xp + 10);
          // player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
          player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.GLASS_BOTTLE));
          player.getInventory().removeItem(new ItemStack(Material.EXPERIENCE_BOTTLE));
          // player.playSound(player.getLocation(), Sound.AMBIENT_UNDERWATER_EXIT, 0.5f,
          // 1f);
          player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_EMPTY, 1f, 1f);

          player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1f);
          ev.setCancelled(true);

        }
      }
    }

    {
      // TODO:open with id of addingwarp with the location.. then at some point just
      // just wait for the dial id to be addingwarp_answer
      // so use this as a way to get get to that first dial part
      if (ev.getPlayer().isSneaking()) {
        if (ev.getClickedBlock().getType().equals(Material.LODESTONE)) {
          NamespacedKey key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
          ItemMeta meta = ev.getItem().getItemMeta();
          PersistentDataContainer container = meta.getPersistentDataContainer();
          Integer cur_value = container.get(key, PersistentDataType.INTEGER);
          if (cur_value != null) {
            if (dm == null) {
              // TODO: need to first check if this is a warp already or not
              Location location = ev.getClickedBlock().getLocation();
              GlobalWarps warp = isThisAWarpLocation(ev.getClickedBlock().getLocation());
              if (warp == null) {
                String loc_string = String.format("%s_%s_%s", location.getBlockX(), location.blockY(),
                    location.getBlockZ());
                String dial_id = String.format("MM:create_warp_name_prefix:%s", loc_string);
                System.out.println(String.format("the id looks like this: %s", dial_id));
                dialMenu.openDialMenu(dial_id,
                    List.of(
                        "create warp",
                        "cancel"),
                    player, Item_Manager.magic_mirror_book);
                ev.setCancelled(true);
              } else {
                String nametag_name = "ass";
                // rename or added warp to locations
                if (warp.creator.equals(ev.getPlayer().getName())) {
                  boolean name_in_use = false;
                  for (GlobalWarps w : global_warps) {
                    if (nametag_name.equals(w.name)) {
                      Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
                          Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 1f);
                      audience.sendActionBar(
                          () -> Component.text(ChatColor.LIGHT_PURPLE + "Choose another name"));
                      name_in_use = true;
                      break;
                    }
                  }
                  if (!name_in_use) {
                    Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
                        Sound.BLOCK_SMITHING_TABLE_USE, 1f, 1f);
                  }
                } else {
                  audience.sendActionBar(
                      () -> Component.text(String.format("Cannot rename. %s's Warp.", warp.creator)));
                }
              }
              return;
            }
          }
        }
      }
    }
    if (!ev.getPlayer().getInventory().getItemInMainHand().isEmpty()) {
      if (ev.getItem().getType().equals(Material.NAME_TAG)) {
        ItemStack nametag = ev.getItem();
        TextComponent textcom = (TextComponent) nametag.getItemMeta().displayName();
        String nametag_name = textcom.content(); // .toString();
        // System.out.println(nametag_name);
        // if (ev.getClickedBlock().equals(null)) {

        // }
        if (ev.getClickedBlock() == null) {
          return;
        }
        if (ev.getClickedBlock().getType().equals(Material.LODESTONE)) {
          Location location = ev.getClickedBlock().getLocation();
          GlobalWarps warp = isThisAWarpLocation(ev.getClickedBlock().getLocation());
          if (warp == null) {
            addWarp(ev.getClickedBlock().getLocation(), ev.getPlayer().getName(), nametag_name, audience);
            Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
                Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 1f);
            audience.sendActionBar(
                () -> Component.text("Warp created"));
          } else {
            if (warp.creator.equals(ev.getPlayer().getName())) {
              boolean name_in_use = false;
              for (GlobalWarps w : global_warps) {
                if (nametag_name.equals(w.name)) {
                  Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
                      Sound.BLOCK_CONDUIT_DEACTIVATE, 1f, 1f);
                  audience.sendActionBar(
                      () -> Component.text(ChatColor.LIGHT_PURPLE + "Choose another name"));
                  name_in_use = true;
                  break;
                }
              }
              if (!name_in_use) {
                Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
                    Sound.BLOCK_SMITHING_TABLE_USE, 1f, 1f);
              }
            } else {
              audience.sendActionBar(
                  () -> Component.text(String.format("Cannot rename. %s's Warp.", warp.creator)));
            }
          }
        }
        return;
      } else {
        // if
        // (!item.getItemMeta().displayName().equals(Item_Manager.magic_mirror_book.getItemMeta().displayName()))
        // {
        // return;
        // }
        NamespacedKey key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
        ItemMeta meta = ev.getItem().getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer cur_value = container.get(key, PersistentDataType.INTEGER);
        if (cur_value == null) {
          return;
        }
        if (cur_value != null) {
          ev.setCancelled(true); // prevent other iteractions

          // if (magic_mirror_no_data.equals(hand_item)) {
          // if (action.equals(Action.RIGHT_CLICK_BLOCK) ||
          // action.equals(Action.RIGHT_CLICK_AIR)) {
          if (ev.getClickedBlock() != null) {
            if (ev.getClickedBlock().getType().equals(Material.LODESTONE)) {
              // System.out.println("yes this is the lodestone");
              // addWarp(ev.getClickedBlock().getLocation(), ev.getPlayer().getName());
              GlobalWarps warp = isThisAWarpLocation(ev.getClickedBlock().getLocation());
              if (warp != null) {
                Location location = player.getLocation();
                for (PlayerWarps pw : player_warps) {
                  if (pw.player_name.equals(player.getName())) {
                    // System.out.println("yes this is the same player");
                    for (GlobalWarps pWarp : pw.known_warps) {
                      // System.out.println("yes we have warps");
                      if (pWarp.name.equals(warp.name)) {
                        // System.out.println("yes same name");
                        if (pWarp.dimension_name.equals(warp.dimension_name)) {
                          // System.out.println("yes same dimension");
                          pw.known_warps.remove(pWarp);
                          Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
                              Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1f, 1.5f);
                          audience.sendActionBar(
                              () -> Component.text(String.format("removed [%s] from known locations", pWarp.name)));
                          return;
                        }
                      }
                    }
                    break;
                  }
                }
                Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
                    Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1f, 1.5f);
                // TODO: extent this message to also get the warp's name
                audience.sendActionBar(
                    () -> Component.text(String.format("added [%s] to known locations", warp.name)));
                addPlayerWarp(ev.getClickedBlock().getLocation(), ev.getPlayer().getName());
                return;
              }
            }
          }
        }
      }
    } else {
      return;
    }
    // }

    // player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL,
    // 1f, 1f);
    ev.setCancelled(true);
    // PlayerDialMenu dm = dialMenu.getPlayerDialMenu(player);
    // Integer index = dialMenu.getPlayer(player);
    if (dm != null) {
      dm.playerChooseSelection(ev);
      switch (dm.dial_id) {
        case "MM:main":
          switch (dm.selection_answer) {
            case "BED":
              if (isBookOutOfPages(ev)) {
                return;
              }
              if (player.getBedSpawnLocation() == null) {
                audience.sendActionBar(() -> Component.text("Where is my bed?").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
              } else {
                teleportEffectSound(player, player.getLocation());
                player.teleportAsync(player.getBedSpawnLocation());
                dialMenu.closeMenu(player);
                // System.out.println("\n\n\tYES BOOK IS BEING USED\n\n");
                useBook(player);
                // teleportEffectSound(player, player.getBedSpawnLocation());
              }
              dialMenu.closeMenu(player);
              // dialMenu.sendDialMenu(2, List.of(confirm_prompt, cancel_prompt), player,
              // Item_Manager.magic_mirror_book);
              return;
            case "SPAWN":
              if (isBookOutOfPages(ev)) {
                return;
              }
              teleportEffectSound(player, player.getLocation());
              player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
              dialMenu.closeMenu(player);
              useBook(player);
              // teleportEffectSound(player, Bukkit.getWorld("world").getSpawnLocation());
              dialMenu.closeMenu(player);
              return;
            case "WARPS":
              dialMenu.openDialMenu("MM:warps",
                  List.of("LOCATIONS", "OTHER USERS", go_back_prompt, cancel_prompt), player,
                  Item_Manager.magic_mirror_book);
              return;
            case "LAST DEATH":
              if (isBookOutOfPages(ev)) {
                return;
              }
              int death_index = -1;
              for (player_deaths d : deaths) {
                if (d.name == player) {
                  death_index = deaths.indexOf(d);
                }
              }
              if (death_index == -1) {
                // NOTE: this message does not show up because i am clearing the title when
                // closing the menu
                dialMenu.closeMenu(player);
                audience.sendActionBar(() -> Component.text("Long Live").color(NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
              } else {
                teleportEffectSound(player, player.getLocation());
                player.teleportAsync(deaths.get(death_index).loc);
                dialMenu.closeMenu(player);
                useBook(player);
                // teleportEffectSound(player, deaths.get(death_index).loc);
              }
              return;
            case "INFO":
              dialMenu.closeMenu(player);
              // BookMeta bookMeta = new BookMeta();
              // ItemStack book_item = new ItemStack(Material.WRITTEN_BOOK);
              // ItemMeta meta = book_item.getItemMeta();
              // BookMeta book_Meta = (BookMeta) meta;
              ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

              // Get the book's meta data
              BookMeta bookMeta = (BookMeta) book.getItemMeta();

              // Set the book's title and author
              bookMeta.setTitle("-----");
              bookMeta.setAuthor("OLRAK");

              // Add some pages to the book
              bookMeta.addPages(
                  Component.text("Table of Contents:\n\n").decorate(TextDecoration.BOLD)
                      .append(Component.text(
                          """
                              [pg. 3] Strange book
                              [pg. 4] Infused-paper
                              [pg. 5] Refilling the book
                              [pg. 6] use cauldron
                              [pg. 7] Warping to other book users
                              [pg. 9] Custom warp locations
                              """).decorate(TextDecoration.ITALIC).decoration(TextDecoration.BOLD, false)),
                  Component.text("")
                      .append(Component.text("Stange\n\n").decorate(TextDecoration.BOLD))
                      .append(Component.text("Use this book to warp around.\n\n"))
                      .append(Component.text("Warping with this book\n"))
                      .append(Component.text("will consume a page..\n")),
                  Component.text("")
                      .append(Component.text("infused-paper:\n\n").decorate(TextDecoration.BOLD))
                      .append(Component.text("ingredients tossed into a cauldron\n\n").decorate(TextDecoration.ITALIC))
                      .append(Component.text("- 1x ender pearl\n"))
                      .append(Component.text("- 1x spider eye\n"))
                      .append(Component.text("- 1x dandelion\n\n"))
                      .append(Component.text("lastly toss in:\n\n"))
                      .append(Component.text("- 6x paper\n")),
                  Component.text("")
                      .append(Component.text("refilling the magic mirror:\n\n").decorate(TextDecoration.BOLD))
                      .append(Component.text("in crafting table\n\n").decorate(TextDecoration.ITALIC))
                      .append(Component.text("(I|I|I) top   \n"))
                      .append(Component.text("(I|I|I) center\n"))
                      .append(Component.text("(_|M|_) bottom\n\n"))
                      .append(Component.text("- (I) Infused paper\n"))
                      .append(Component.text("- (M) Magic Mirror\n")),
                  Component.text("")
                      .append(Component.text("How to use cauldron:\n").decorate(TextDecoration.BOLD))
                      .append(Component.text(
                          """
                              Simply place a `cauldron` on
                               top of a lit `campfire`
                              and toss in the ingredients.
                              """).decoration(TextDecoration.BOLD, false)),
                  Component.text("Warping to other book users\n\n").decorate(TextDecoration.BOLD)
                      .append(Component.text(
                          """
                              To warp to other users you'll have to ask them select `wait_for` within the book's menu.
                              """).decoration(TextDecoration.BOLD, false)),
                  Component.text("Custom warp locations\n\n").decorate(TextDecoration.BOLD)
                      .append(Component.text("Creating\n\n").decorate(TextDecoration.BOLD))
                      .append(Component.text(
                          """
                              Placing down a lodestone and using a name_tag on it will create a warp at that spot for you to warp to whenever.
                              """)
                          .decoration(TextDecoration.BOLD, false)),
                  Component.text("Saving location\n\n").decorate(TextDecoration.BOLD)
                      .append(Component.text(
                          """
                              If you happen to find an active lodestone and would like to save it for yourself you can use your book on it, saving its location.
                              """)
                          .decoration(TextDecoration.BOLD, false)),
                  Component.text("Using\n\n").decorate(TextDecoration.BOLD)
                      .append(Component.text(
                          """
                              Stepping on any active lodestone will open a warp menu showing you all other locations that you have saved..
                              Warping from lodestone to lodestone will cost you nothing.
                              """)
                          .decoration(TextDecoration.BOLD, false)));
              // .append(Component.text(""))
              // Set the book's meta data
              book.setItemMeta(bookMeta);

              // Open the book for the player
              player.openBook(book);

              return;
            case cancel_prompt:
              dialMenu.closeMenu(player);
              return;
          }
        case "MM:warps":
          switch (dm.selection_answer) {
            case go_back_prompt: // go back to last menu
              dialMenu.openDialMenu("MM:main",
                  List.of(
                      "BED",
                      "SPAWN",
                      "WARPS",
                      "LAST DEATH",
                      "INFO",
                      cancel_prompt),
                  player, Item_Manager.magic_mirror_book);
              return;
            case "LOCATIONS":
              if (isBookOutOfPages(ev)) {
                return;
              }
              // for each player's warp compare to global warps, remove if needed, rename if
              // needed.
              List<String> prompt_list = new ArrayList<>();
              List<GlobalWarps> to_remove = new ArrayList<>();
              for (PlayerWarps pw : player_warps) {
                // System.out.printf("%s's warp size: %s\n", pw.player_name,
                // pw.known_warps.size());
                if (pw.player_name.equals(player.getName())) {
                  // System.out.printf("%s has: %s warp locations", pw.player_name,
                  // pw.known_warps.size());
                  for (GlobalWarps w : pw.known_warps) {
                    if (findPlayerWarpInGlobalWarps(w) == null) {
                      to_remove.add(w);
                      // System.out.println("seems like this warp spot getting removed from the
                      // player's list.");
                      // pw.known_warps.remove(w);
                    } else {
                      if (w.name != null) {
                        prompt_list.add(w.name);
                      } else {
                        prompt_list.add(w.location.toString());

                      }
                    }
                  }
                  for (GlobalWarps w : to_remove) {
                    pw.known_warps.remove(w);
                  }
                  break; // found the player
                }
              }
              Collections.sort(prompt_list, String.CASE_INSENSITIVE_ORDER);
              prompt_list.add(cancel_prompt);
              dialMenu.openDialMenu("MM:locations", prompt_list, player, Item_Manager.magic_mirror_book);
              saveGlobalWarpsToFile(global_warps_file);
              savePlayerWarpsToFile(player_warps_file);
              return;
            case "OTHER USERS":
              dialMenu.openDialMenu("MM:OTHER_USERS",
                  List.of("WARP TO", "WAIT FOR", go_back_prompt, cancel_prompt), player,
                  Item_Manager.magic_mirror_book);
              return;
            case cancel_prompt:
              dialMenu.closeMenu(player);
              return;

          }
        case "MM:OTHER_USERS":
          switch (dm.selection_answer) {
            case go_back_prompt:
              dialMenu.openDialMenu("MM:warps",
                  List.of("LOCATIONS", "OTHER USERS", go_back_prompt, cancel_prompt), player,
                  Item_Manager.magic_mirror_book);
              return;
            case cancel_prompt:
              dialMenu.closeMenu(player);
              return;
            case "WARP TO":
              // TODO: list all players in the wait list
              if (isBookOutOfPages(ev)) {
                return;
              }
              List<String> the_players = new ArrayList<>();
              for (Player waiting_players : dialMenu.wait_list) {
                the_players.add(waiting_players.getName().toUpperCase());
              }
              the_players.add(cancel_prompt);
              dialMenu.openDialMenu("mm:warp_to", the_players, player, Item_Manager.magic_mirror_book);
              return;
            case "WAIT FOR":
              // TODO: add player to some wait list
              dialMenu.wait_list.add(player);
              dialMenu.openDialMenu("MM:wait_for", List.of(cancel_prompt), player, Item_Manager.magic_mirror_book);
              return;
          }
        case "MM:warp_to":
          switch (dm.selection_answer) {
            case cancel_prompt:
              dialMenu.closeMenu(player);
              return;
            default:
              for (Player wait_player : dialMenu.wait_list) {
                // System.out.printf("Player chose [%s]\n",
                // p_menu.getAll_context().answer.name.toUpperCase());
                if (dm.selection_answer.toUpperCase().equals(wait_player.getName().toUpperCase())) {
                  useBook(player);
                  teleportEffectSound(player, player.getLocation());
                  player.teleport(wait_player);
                  dialMenu.closeMenu(player);
                  dialMenu.closeMenu(wait_player);
                  // teleportEffectSound(player, wait_player.getLocation());
                  return;
                }
              }
          }
        case "MM:wait_for":
          switch (dm.selection_answer) {
            case cancel_prompt:
              dialMenu.closeMenu(player);
              return;
          }
      }
    } else {
      dialMenu.openDialMenu("MM:main",
          List.of(
              "BED",
              "SPAWN",
              "WARPS",
              "LAST DEATH",
              "INFO",
              cancel_prompt),
          player, Item_Manager.magic_mirror_book);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    // Player player = event.getPlayer();
    Entity entity = event.getRightClicked();
    if (entity.getType().equals(EntityType.VILLAGER)) {
      // System.out.print("this do be a villager\n");
      Villager villager = (Villager) entity;
      List<MerchantRecipe> villager_recipes = villager.getRecipes();
      List<MerchantRecipe> new_villager_recipes = new ArrayList<>();
      ItemStack xp_bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
      Profession villager_profession = villager.getProfession();
      if (villager_profession == Profession.CLERIC) {
        for (MerchantRecipe r : villager_recipes) {
          // System.out.print(String.format("%s\n",r.getResult()));
          if (r.getResult().asOne().equals(xp_bottle)) {
            // System.out.print("We'be got the bottle, we gottem.\n");
            continue;
          }
          new_villager_recipes.add(r);
        }
        villager.setRecipes(new_villager_recipes);
      }
    }
    // prevent the player from breaking shit
    if (dialMenu.getPlayer(event.getPlayer()) > -1) {
      event.setCancelled(true);
    }
  }

  // @EventHandler
  // public void onPlayerElytraBoost(PlayerElytraBoostEvent event) {
  // Player player = event.getPlayer();
  // World curr_dim = player.getLocation().getWorld();
  // if (Bukkit.getWorld("world_the_end") == player.getWorld()) {

  // } else {
  // player.sendActionBar(() -> Component.text("End dimension
  // only").color(NamedTextColor.RED));
  // event.setCancelled(true);
  // }
  // }
  // @EventHandler
  // public void onPlayerAttack(PrePlayerAttackEntityEvent event) {
  // // prevent the player from breaking shit
  // for (UsedWarp usedWarp : just_used_lode_warp) {
  // if (usedWarp.player.getName() == event.getPlayer().getName()) {
  // event.setCancelled(true);
  // }
  // }
  // }

  // NOTE: this should not be part of this plugin
  // Breaks the player's elytra if hit by the dragon
  // @EventHandler
  // public void onPlayerHit(EntityDamageByEntityEvent event) {
  // Entity entity = event.getEntity();
  // Entity damager = event.getDamager();
  // if (damager.getType() == EntityType.ENDER_DRAGON) {
  // if (entity instanceof Player) {
  // Player player = (Player) event.getEntity();
  // ItemStack chest = player.getInventory().getChestplate();
  // if (chest == null) {
  // return;
  // }
  // if (chest.getType() == Material.ELYTRA) {
  // Damageable damageable = (Damageable) chest.getItemMeta();
  // damageable.setDamage(chest.getType().getMaxDurability());
  // chest.setItemMeta(damageable);
  // player.updateInventory();
  // }
  // }
  // }
  // }

  // @EventHandler
  // public void onPlayerJoin(PlayerJoinEvent event) {
  // NamespacedKey key = new NamespacedKey(magic.getPlugin(),
  // "aquired_spawn_book");
  // Player player = event.getPlayer();
  // PersistentDataContainer player_container =
  // player.getPersistentDataContainer();
  // Set<NamespacedKey> continer_data = player_container.getKeys();
  // for (NamespacedKey k : continer_data) {
  // if (k.equals(key)) {
  // // System.out.print("found the key, no book for you\n");
  // // magic.getPlugin().getComponentLogger().debug("found the key, no book for
  // // you\n");
  // magic.getPlugin().getComponentLogger().info(Component.text("found the key, no
  // book for you"));
  // return;
  // }
  // }
  // player_container.set(key, PersistentDataType.INTEGER, 1);
  // event.getPlayer().getInventory().addItem(Item_Manager.spawn_book);

  // }

  // NOTE: hitting enderman to gain magic mirror has been removed
  // @EventHandler
  // public void onPlayerDamageEntity(EntityDamageByEntityEvent ev) {
  // if (ev.getEntity() instanceof Enderman && ev.getDamager() instanceof Player)
  // {
  // Player player = (Player) ev.getDamager();
  // // player.sendMessage("Something is working ..but..");
  // // Enderman enderman = (Enderman) ev.getEntity();
  // if
  // (player.getInventory().getItemInMainHand().getType().equals(Material.BOOK)) {
  // // player.sendMessage("This should be working then..");
  // player.getInventory().addItem(Item_Manager.magic_mirror_book);
  // player.playSound(ev.getEntity().getLocation(),
  // Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.BLOCKS, 1, 1);

  // ItemStack old_stack = player.getInventory().getItemInMainHand();
  // old_stack.setAmount(old_stack.getAmount() - 1);
  // player.getInventory().setItemInMainHand(old_stack);
  // }
  // // do something when player attacks enderman
  // }
  // }

  ArrayList<String> hasItemInHand = new ArrayList<String>();

  @EventHandler
  public void onItemSwitch(PlayerItemHeldEvent ev) {
    Player player = ev.getPlayer();
    int newSlot = ev.getNewSlot();
    var isEmpty = player.getInventory().getItem(newSlot);
    boolean notHoldingItem = false;

    if (isEmpty != null) {
      Material itemType = isEmpty.getType();
      if (itemType.equals(Item_Manager.magic_mirror_book.getType())) {
        if (!hasItemInHand.contains(player.getName())) {
          hasItemInHand.add(player.getName());
        }
      } else {
        notHoldingItem = true;
      }
    } else {
      notHoldingItem = true;
    }
    if (notHoldingItem == true) {
      if (hasItemInHand.size() > 0) {
        if (hasItemInHand.contains(player.getName())) {
          hasItemInHand.remove(player.getName());
          dialMenu.closeMenu(player);
        }
      }
    }

    // ItemStack item = player.getInventory().getItemInMainHand();
    // ItemStack xp_bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
    // int min = 1;
    // int max = 5;
    // int random_int = new Random().nextInt(max - min + 1) + min;
    // if (random_int == 3) {
    // if (item.getType().equals(Material.GLASS_BOTTLE)) {
    // player.sendMessage("Seems like i may be able to scrape some xp off and place
    // it in this bottle.");
    // return;
    // }
    // if (item.equals(xp_bottle)) {
    // player.sendMessage(
    // "If i crouch and punch myself with this bottle i can get all the xp out,
    // without wasting a drop.");
    // return;
    // }
    // }
  }

  @EventHandler
  public void onPlayerDeath(EntityDeathEvent ev) {
    EntityType entity = ev.getEntityType();
    // check if its a player that died
    if (entity.equals(EntityType.PLAYER)) {
      Entity player = ev.getEntity();
      player_deaths this_death = new player_deaths();
      this_death.name = (Player) player;
      this_death.loc = player.getLocation();
      int index = -1;
      if (deaths.size() > 0) {
        for (player_deaths d : deaths) {
          if (d.name == player) {
            index = deaths.indexOf(d);
          }
        }

        if (index > -1) {
          deaths.get(index).loc = player.getLocation();
        }
      }
      if (index == -1) {
        deaths.add(this_death);
      }
    }
  }

  @EventHandler
  public void onPrepareItemCraft(PrepareItemCraftEvent event) {
    // Get the crafting matrix
    ItemStack[] matrix = event.getInventory().getMatrix();

    // Check each item in the matrix
    for (ItemStack item : matrix) {
      if (item != null) {
        ItemStack one_item = item.clone();
        one_item.setAmount(1);
        if (one_item.equals(Item_Manager.coin)) {
          // If the item with the certain data value is found, prevent crafting
          event.getInventory().setResult(new ItemStack(Material.AIR));
          break;
        }
      }

    }
  }
}
