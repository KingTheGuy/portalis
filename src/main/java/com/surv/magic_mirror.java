package com.surv;

import java.awt.image.BufferedImageOp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import com.surv.items.Item_Manager;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent.DamageState;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
// import com.surv.menu;
import com.surv.BetterMenu.PlayerContext;
import com.surv.BetterMenu.PlayerWithMenu;

import net.kyori.adventure.Adventure;
// import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

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
  String global_warps_file = "global_warps.txt";
  String player_warps_file = "player_warps.txt";
  public List<player_deaths> deaths = new ArrayList<>();

  public List<GlobalWarps> global_warps = new ArrayList<>();
  public List<PlayerWarps> player_warps = new ArrayList<>();

  public void addWarp(Location location) {
    GlobalWarps warp_to_add = new GlobalWarps();
    warp_to_add.location = new Vector3();
    warp_to_add.location.X = location.getBlockX();
    warp_to_add.location.Y = location.getBlockY();
    warp_to_add.location.Z = location.getBlockZ();
    warp_to_add.dimension_name = location.getWorld().getName();
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
    global_warps.add(warp_to_add);
    saveGlobalWarpsToFile(global_warps_file);
    // System.out.println("new location saved");
  }

  public void addPlayerWarp(Location location, Player player) {
    // TODO(done): check if player is in the list
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
      if (pw.player_name.equals(player.getName())) {
        System.out.println("found the player");
        found_player = pw.player_name;
      } else {
        System.out.println("did not find the player..");
      }
    }
    for (GlobalWarps w : global_warps) {
      if (w.location.toString().equals(warp_to_add.location.toString())) {
        if (w.dimension_name.equals(warp_to_add.dimension_name)) {
          System.out.println("found this location");
          // yes we have a warp spot here
          if (found_player == null) {
            System.out.println("did not find the player, creating new list..");
            PlayerWarps new_player_warp = new PlayerWarps();
            found_player = player.getName();
            new_player_warp.player_name = player.getName();
            player_warps.add(new_player_warp);
          }
          for (PlayerWarps pw : player_warps) {
            if (pw.player_name.equals(found_player)) {
              System.out.println("player is in fact found");
              for (GlobalWarps pww : pw.known_warps) {
                if (w.location.toString().equals(pww.location.toString())) {
                  if (w.dimension_name.equals(pww.dimension_name)) {
                    System.out.println("the player is already aware of this warp.");
                    return;
                  }
                }
              }
              pw.known_warps.add(w);
              // player_warps.add(pw);
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
    Vector3 location = new Vector3();
    String dimension_name;
    String name;

    @Override
    public String toString() {
      return String.format("[warp] <%s>, <%s>, <%s>\n", location, dimension_name, name);

    }
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
  BetterMenu betterMenu = new BetterMenu();

  @EventHandler
  public void onLeave(PlayerQuitEvent ev) {
    Player player = ev.getPlayer();
    betterMenu.closeMenu(player);
    // new_prompt.closeMenu(player);
  }

  public void saveGlobalWarpsToFile(String file) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      for (GlobalWarps w : global_warps) {
        writer.write("[warp]\n");
        writer.write(String.format("\t-location:%s\n", w.location.toString()));
        writer.write(String.format("\t-dimension:%s\n", w.dimension_name));
        writer.write(String.format("\t-name:%s\n", w.name));
        writer.write("\n");
      }
      writer.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void savePlayerWarpsToFile(String file) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      for (PlayerWarps pw : player_warps) {
        writer.write(String.format("PLAYER:%s\n", pw.player_name));
        for (GlobalWarps w : pw.known_warps) {
          writer.write("\t[warp]\n");
          writer.write(String.format("\t\t-location:%s\n", w.location.toString()));
          writer.write(String.format("\t\t-dimension:%s\n", w.dimension_name));
          writer.write(String.format("\t\t-name:%s\n", w.name));
          writer.write("\n");
        }
        writer.write("\n");
      }
      writer.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void loadGlobalWarpsFromFile(String file) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line;
      GlobalWarps warp_from_file = new GlobalWarps();
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.startsWith("-location")) {
          String[] split = trimmed.split(":", -1);
          String[] xyz = split[1].split(",", -1);
          warp_from_file.location = new Vector3();
          warp_from_file.location.X = Integer.parseInt(xyz[0]);
          warp_from_file.location.Y = Integer.parseInt(xyz[1]);
          warp_from_file.location.Z = Integer.parseInt(xyz[2]);
        }
        if (trimmed.startsWith("-dimension")) {
          String[] split = trimmed.split(":", -1);
          warp_from_file.dimension_name = split[1];
        }
        if (trimmed.startsWith("-name")) {
          String[] split = trimmed.split(":", -1);
          if (split[1] == "null") {
            warp_from_file.name = null;
          } else {
            warp_from_file.name = split[1];
          }
        }
        if (trimmed.isBlank()) {
          if (warp_from_file != null) {
            global_warps.add(warp_from_file);
            warp_from_file = new GlobalWarps();
          }
        }
      }
      reader.close();
    } catch (IOException e) {
      System.out.println(e);
    }

  }

  // TODO: implement this
  public void loadPlayerWarpsFromFile(String file) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line;
      PlayerWarps player_warp_from_file = new PlayerWarps();
      GlobalWarps warp = new GlobalWarps();
      boolean last_line_empty = false;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.startsWith("PLAYER:")) {
          String[] split = trimmed.split(":", -1);
          player_warp_from_file.player_name = split[1];
          System.out.println("loaded the name");
          System.out.println(player_warp_from_file.player_name);
        }
        if (trimmed.startsWith("-location")) {
          String[] split = trimmed.split(":", -1);
          String[] xyz = split[1].split(",", -1);
          warp.location = new Vector3();
          warp.location.X = Integer.parseInt(xyz[0]);
          warp.location.Y = Integer.parseInt(xyz[1]);
          warp.location.Z = Integer.parseInt(xyz[2]);
        }
        if (trimmed.startsWith("-dimension")) {
          String[] split = trimmed.split(":", -1);
          warp.dimension_name = split[1];
        }
        if (trimmed.startsWith("-name")) {
          String[] split = trimmed.split(":", -1);
          if (split[1] == "null") {
            warp.name = null;
          } else {
            warp.name = split[1];
          }
        }
        if (trimmed.isBlank()) {
          if (last_line_empty == true) {
            if (player_warp_from_file.player_name != null) {
              player_warps.add(player_warp_from_file);
              player_warp_from_file = new PlayerWarps();
            }
            last_line_empty = false;
          }
          last_line_empty = true;
          if (warp != null) {
            System.out.printf("found a warp spot: %s\n", warp);
            player_warp_from_file.known_warps.add(warp);
            warp = new GlobalWarps();
          }
        }
      }
      reader.close();
      System.out.printf("player warp spots: %s\n", player_warps.toString());
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
    betterMenu.playerSelection(ev);
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
    NamespacedKey key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
    ItemMeta meta = item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();
    Integer cur_value = container.get(key, PersistentDataType.INTEGER);
    Integer max_uses = 6;
    Integer new_value = cur_value - 1;
    container.set(key, PersistentDataType.INTEGER, new_value);
    List<Component> lore = new ArrayList<>();
    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, new_value);
    item.setItemMeta(meta);
    lore.add(Component.text(String.format("%s/%s uses", new_value, max_uses)));
    item.lore().clear();
    item.lore(lore);
  }

  public void teleportEffect(Player player) {
    Location location = player.getLocation();
    Bukkit.getWorld(location.getWorld().getUID()).playSound(location, Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    // Bukkit.getWorld(location.getWorld().getUID());
    // audience.playSound(Sound.ENTITY_SHULKER_TELEPORT,
    // location.getX(),location.getY(),location.getZ());
    // player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f,
    // 1f);
  }

  public boolean isEmpty(PlayerInteractEvent ev) {
    Player player = ev.getPlayer();
    Audience audience = Audience.audience(player);
    NamespacedKey key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
    ItemMeta meta = ev.getItem().getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();
    Integer cur_value = container.get(key, PersistentDataType.INTEGER);
    if (cur_value == 0) {
      betterMenu.closeMenu(player);
      audience.sendActionBar(() -> Component.text("out of pages").color(NamedTextColor.RED));
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
      return true;
    }
    return false;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent ev) {
    if (ev.getPlayer().getInventory().getItemInMainHand() == null) {
      return;
    }
    if (!ev.getHand().equals(EquipmentSlot.HAND)) {
      return;
    }
    Player player = ev.getPlayer();
    ItemStack item = player.getInventory().getItemInMainHand();
    Material itemType = item.getType();
    Action action = ev.getAction();
    Audience audience = Audience.audience(player);

    // BEGINING OF THIS
    // Player player = ev.getPlayer();
    // boolean isSneaking = ev.isSneaking();
    // System.out.printf("latest: %s\n", p.context.get(p.context.size() -
    // 1).context);
    // TODO: check if the latest list mateches this new list, something like that
    // List<String> list = List.of("WARP");
    // p.showPrompt(list);
    // PlayerWithMenu has_menu_open = betterMenu.player_with_menu.get(index);
    // System.out.print(String.format("Current menu
    // index:[%s]\n",has_menu_open.context.size()));
    // Integer context_size = p.context.size();

    // if (context_size == 2) {
    // if (p.context.get(0).answer == "LAST DEATH") {
    // // System.out.print(String.format("%s wants to chose
    // // %s\n",ev.getPlayer().getName(),has_menu_open.context.get(0).answer));
    // List<String> prompt_options = List.of("WARP");
    // betterMenu.openMenu(prompt_options, player);
    // return;
    // // List<String> prompt_options = List.of("LAST DEATH", "BED" , "INFO");
    // // betterMenu.openMenu(prompt_options, player);
    // }
    // if (p.context.get(0).answer == "BED") {
    // // System.out.print(String.format("%s wants to chose
    // // %s\n",ev.getPlayer().getName(),has_menu_open.context.get(0).answer));
    // List<String> prompt_options = List.of("WARP");
    // betterMenu.openMenu(prompt_options, player);
    // // List<String> prompt_options = List.of("LAST DEATH", "BED" , "INFO");
    // // betterMenu.openMenu(prompt_options, player);
    // return;
    // }

    // if (p.context.get(0).answer == "SPAWN") {
    // // System.out.print(String.format("%s wants to chose
    // // %s\n",ev.getPlayer().getName(),has_menu_open.context.get(0).answer));
    // List<String> prompt_options = List.of("WARP");
    // betterMenu.openMenu(prompt_options, player);
    // // List<String> prompt_options = List.of("LAST DEATH", "BED" , "INFO");
    // // betterMenu.openMenu(prompt_options, player);
    // return;
    // }
    // }

    // // Audience audience = Audience.audience(player);
    // if (context_size == 3) {
    // // System.out.print("So this is working right?\n");
    // if (p.context.get(1).prompt == "LAST DEATH") {
    // if (p.context.get(1).answer == "WARP") {
    // int death_index = -1;
    // for (player_deaths d : deaths) {
    // if (d.name == player) {
    // death_index = deaths.indexOf(d);
    // }
    // }
    // if (death_index == -1) {
    // // NOTE: this message does not show up because i am clearing the title when
    // // closing the menu
    // audience.sendActionBar(() -> Component.text("Long
    // Live").color(NamedTextColor.RED));
    // player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f,
    // 1f);
    // } else {
    // teleportEffect(player);
    // player.teleportAsync(deaths.get(death_index).loc);
    // betterMenu.closeMenu(player);
    // useBook(player);
    // teleportEffect(player);
    // }
    // }
    // betterMenu.closeMenu(player);
    // }
    // if (p.context.get(1).prompt == "BED") {
    // if (p.context.get(1).answer == "WARP") {
    // if (player.getBedSpawnLocation() == null) {
    // audience.sendActionBar(() -> Component.text("Where is my
    // bed?").color(NamedTextColor.RED));
    // player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f,
    // 1f);
    // } else {
    // teleportEffect(player);
    // player.teleportAsync(player.getBedSpawnLocation());
    // betterMenu.closeMenu(player);
    // useBook(player);
    // teleportEffect(player);
    // }
    // }
    // betterMenu.closeMenu(player);
    // }
    // if (p.context.get(1).prompt == "SPAWN") {
    // if (p.context.get(1).answer == "WARP") {
    // teleportEffect(player);
    // player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
    // betterMenu.closeMenu(player);
    // useBook(player);
    // teleportEffect(player);
    // }
    // betterMenu.closeMenu(player);
    // }
    // }
    // for (PlayerContext c : p.context) {
    // if (p.prompt == "CANCEL") {
    // betterMenu.closeMenu(player);
    // }
    // }
    // if (has_menu_open.context.get(1).answer == "WARP") {

    // }
    // END OF THSIS
    // return;

    // if (isSneaking == false) {
    // return;
    // }

    // player.sendMessage(ChatColor.AQUA + "______");
    // player.sendMessage(ChatColor.AQUA + "ItemType: " + itemType);

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
            player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE));
            player.playSound(player.getLocation(), Sound.BLOCK_BEEHIVE_DRIP, 1f, 1f);
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
          player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
          player.getInventory().removeItem(new ItemStack(Material.EXPERIENCE_BOTTLE));
          player.playSound(player.getLocation(), Sound.AMBIENT_UNDERWATER_EXIT, 0.5f, 1f);
          player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1f);
          ev.setCancelled(true);

        }
      }
    }
    // on use Magic Mirror
    if (ev.getItem() != null) {

      // TODO: This is for naming the lodestone
      if (ev.getItem().getType().equals(Material.NAME_TAG)) {
        ItemStack nametag = ev.getItem();
        String nametag_name = nametag.displayName().toString();
        // NamespacedKey tag_key = new NamespacedKey(magic.getPlugin(),
        // "lodestone_name");
        // PersistentDataContainer lodestone_data =
        // ev.getClickedBlock().getWorld().getPersistentDataContainer();
        // lodestone_data.set(tag_key, PersistentDataType.LIST, );
      }

      // FIXME: create a copy of the book and remove the persistanet data and then
      // check to see if the items
      // are equal without that data.
      ItemStack magic_mirror_no_data = new ItemStack(Item_Manager.magic_mirror_book);
      ItemMeta magic_meta = magic_mirror_no_data.getItemMeta();

      ItemStack hand_item = new ItemStack(ev.getItem());
      hand_item.setItemMeta(magic_meta);

      if (magic_mirror_no_data.equals(hand_item)) {
        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
          if (ev.getClickedBlock() != null) {
            if (ev.getClickedBlock().getType().equals(Material.LODESTONE)) {
              // System.out.println("yes this is the lodestone");
              addWarp(ev.getClickedBlock().getLocation());
              addPlayerWarp(ev.getClickedBlock().getLocation(), ev.getPlayer());
            }
            return;
          }

          // TODO: move this, the book should not use a use until the player teleports

          final String confirm_prompt = "RIP OUT PAGE";
          final String cancel_prompt = "CLOSE";
          // player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL,
          // 1f, 1f);
          Integer index = betterMenu.findPlayer(player);
          if (index < 0) {
            List<String> THIS_LIST = List.of("BED", "SPAWN", "WARPS", "LAST DEATH", cancel_prompt);
            // List<String> THIS_LIST = List.of("JUST", "SOME", "NEW", "LIST");
            betterMenu.sendPrompt(1, THIS_LIST, player);
            index = betterMenu.findPlayer(player);
          }
          PlayerWithMenu p_menu = betterMenu.player_with_menu.get(index);
          p_menu.playerChoose(ev);
          if (p_menu.all_context.size() <= 1) {
            switch (p_menu.getAll_context().answer) {
              case "BED":
                if (isEmpty(ev)) {
                  return;
                }
                betterMenu.sendPrompt(2, List.of(confirm_prompt, cancel_prompt), player);
                return;
              case "SPAWN":
                if (isEmpty(ev)) {
                  return;
                }
                betterMenu.sendPrompt(2, List.of(confirm_prompt, cancel_prompt), player);
                return;
              case "WARPS":
                betterMenu.sendPrompt(3, List.of("WARP TO", "WAIT FOR", cancel_prompt), player);
                return;
              case "LAST DEATH":
                if (isEmpty(ev)) {
                  return;
                }
                betterMenu.sendPrompt(2, List.of(confirm_prompt, cancel_prompt), player);
                return;
              case cancel_prompt:
                betterMenu.closeMenu(player);
                return;
            }
          }
          if (p_menu.getAll_context().id == 2) {
            switch (p_menu.getAll_context().answer) {
              case confirm_prompt:
                switch (p_menu.all_context.get(0).answer) {
                  case "BED":
                    if (player.getBedSpawnLocation() == null) {
                      audience.sendActionBar(() -> Component.text("Where is my bed?").color(NamedTextColor.RED));
                      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
                    } else {
                      teleportEffect(player);
                      player.teleportAsync(player.getBedSpawnLocation());
                      betterMenu.closeMenu(player);
                      useBook(player);
                      teleportEffect(player);
                    }
                    betterMenu.closeMenu(player);
                    return;
                  case "SPAWN":
                    teleportEffect(player);
                    player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
                    betterMenu.closeMenu(player);
                    useBook(player);
                    teleportEffect(player);
                    betterMenu.closeMenu(player);
                    return;
                  case "LAST DEATH":
                    int death_index = -1;
                    for (player_deaths d : deaths) {
                      if (d.name == player) {
                        death_index = deaths.indexOf(d);
                      }
                    }
                    if (death_index == -1) {
                      // NOTE: this message does not show up because i am clearing the title when
                      // closing the menu
                      betterMenu.closeMenu(player);
                      audience.sendActionBar(() -> Component.text("Long Live").color(NamedTextColor.RED));
                      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
                    } else {
                      teleportEffect(player);
                      player.teleportAsync(deaths.get(death_index).loc);
                      betterMenu.closeMenu(player);
                      useBook(player);
                      teleportEffect(player);
                    }
                    return;
                  case "WARPS":
                    betterMenu.closeMenu(player);
                    return;
                }
              case cancel_prompt:
                betterMenu.closeMenu(player);
                return;
            }
          }
          if (p_menu.getAll_context().id == 3) {
            switch (p_menu.getAll_context().answer) {
              case "WARP TO":
                // TODO: list all players in the wait list
                if (isEmpty(ev)) {
                  return;
                }
                List<String> the_players = new ArrayList<>();
                for (Player waiting_players : betterMenu.wait_list) {
                  the_players.add(waiting_players.getName().toUpperCase());
                }
                the_players.add(cancel_prompt);
                betterMenu.sendPrompt(4, the_players, player);
                return;
              case "WAIT FOR":
                // TODO: add player to some wait list
                betterMenu.wait_list.add(player);
                betterMenu.sendPrompt(5, List.of(cancel_prompt), player);
                return;
              case cancel_prompt:
                betterMenu.closeMenu(player);
                return;
            }

          }
          if (p_menu.getAll_context().id == 4) {
            // FIXME: why is this not working?
            switch (p_menu.getAll_context().answer) {
              case cancel_prompt:
                betterMenu.closeMenu(player);
                return;
              default:
                for (Player wait_player : betterMenu.wait_list) {
                  System.out.printf("Player chose [%s]\n", p_menu.getAll_context().answer.toUpperCase());
                  if (p_menu.getAll_context().answer.toUpperCase().equals(wait_player.getName().toUpperCase())) {
                    useBook(player);
                    player.teleport(wait_player);
                    betterMenu.closeMenu(player);
                    betterMenu.closeMenu(wait_player);
                    return;
                  }
                }
            }
          }
          if (p_menu.getAll_context().id == 5) {
            switch (p_menu.getAll_context().answer) {
              case cancel_prompt:
                betterMenu.closeMenu(player);
                return;
            }
          }
        }
      }
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
  }

  @EventHandler
  public void onPlayerElytraBoost(PlayerElytraBoostEvent event) {
    Player player = event.getPlayer();
    World curr_dim = player.getLocation().getWorld();
    // player.getWorld().getEnvironment();
    // for (World w : Bukkit.getWorlds()) {
    // magic.getPlugin().getComponentLogger().info(Component.text(String.format("dim:
    // %s",w.getName())));
    // }
    if (Bukkit.getWorld("world_the_end") == player.getWorld()) {

    } else {
      player.sendActionBar(() -> Component.text("End dimension only").color(NamedTextColor.RED));
      event.setCancelled(true);
    }
  }

  // Breaks the player's elytra if hit by the dragon
  @EventHandler
  public void onPlayerHit(EntityDamageByEntityEvent event) {
    Entity entity = event.getEntity();
    Entity damager = event.getDamager();
    if (damager.getType() == EntityType.ENDER_DRAGON) {
      if (entity instanceof Player) {
        Player player = (Player) event.getEntity();
        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null) {
          return;
        }
        if (chest.getType() == Material.ELYTRA) {
          Damageable damageable = (Damageable) chest.getItemMeta();
          damageable.setDamage(chest.getType().getMaxDurability());
          chest.setItemMeta(damageable);
          player.updateInventory();
        }
      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    NamespacedKey key = new NamespacedKey(magic.getPlugin(), "aquired_spawn_book");
    Player player = event.getPlayer();
    PersistentDataContainer player_container = player.getPersistentDataContainer();
    Set<NamespacedKey> continer_data = player_container.getKeys();
    for (NamespacedKey k : continer_data) {
      if (k.equals(key)) {
        // System.out.print("found the key, no book for you\n");
        // magic.getPlugin().getComponentLogger().debug("found the key, no book for
        // you\n");
        magic.getPlugin().getComponentLogger().info(Component.text("found the key, no book for you"));
        return;
      }
    }
    player_container.set(key, PersistentDataType.INTEGER, 1);
    event.getPlayer().getInventory().addItem(Item_Manager.spawn_book);

  }

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
          betterMenu.closeMenu(player);
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

}
