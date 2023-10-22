package com.surv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.maven.model.MailingList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.surv.items.Item_Manager;
import com.comphenix.net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy.SelfInjection;
import com.comphenix.net.bytebuddy.build.Plugin.Factory.UsingReflection.Priority;
import com.comphenix.protocol.ProtocolManager;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent.DamageState;
import com.surv.menu;

// import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

//DOING//
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
  public List<player_deaths> deaths = new ArrayList<>();

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

  ArrayList<warp_options_cost> main_menu_selections = new ArrayList<>();

  public void loader() {
    main_menu_selections.add(new warp_options_cost("LAST DEATH", 6)); // 0
    main_menu_selections.add(new warp_options_cost("BED", 3)); // 1
    main_menu_selections.add(new warp_options_cost("PLAYERS", 0)); // 2
    main_menu_selections.add(new warp_options_cost("WARPS", 4)); // 3
    main_menu_selections.add(new warp_options_cost("INFO", 0)); // 4
  }

  // public void saveToFile() {
  // StringBuilder sb = new StringBuilder();
  // for (Camp camp : campfires) {
  // sb.append("- dimensionName: ").append(camp.dimensionName).append("\n")
  // .append(" posX: ").append(camp.posX).append("\n")
  // .append(" posY: ").append(camp.posY).append("\n")
  // .append(" posZ: ").append(camp.posZ).append("\n")
  // .append(" owner: ").append(camp.owner).append("\n")
  // .append(" pick_crops: ").append(camp.pick_crops).append("\n")
  // .append(" live_stock: ").append(camp.live_stock).append("\n\n");
  // }
  // String yamlString = sb.toString();
  // try (FileWriter writer = new FileWriter(PLAYER_WARPS)) {
  // writer.write(yamlString);
  // } catch (IOException o) {
  // }
  // }

  // public void loadFromFile() {
  // try {
  // File file = new File(PLAYER_WARPS);
  // Scanner scanner = new Scanner(file);
  // Camp new_claim = new Camp();
  // while (scanner.hasNextLine()) {
  // String line = scanner.nextLine();
  // String[] splits = line.split(": ", 0);
  // if (line.toString().startsWith("-")) {
  // System.out.println("::this is the START of a claim::");
  // }
  // if (splits[0].contains("dimensionName")) {
  // new_claim.dimensionName = splits[1];
  // System.out.println(String.format("size of name WORLD is: %s should be 5",
  // splits[1].length()));
  // } else if (splits[0].contains("posX")) {
  // new_claim.posX = Integer.parseInt(splits[1]);

  // } else if (splits[0].contains("posY")) {
  // new_claim.posY = Integer.parseInt(splits[1]);

  // } else if (splits[0].contains("posZ")) {
  // new_claim.posZ = Integer.parseInt(splits[1]);

  // } else if (splits[0].contains("owner")) {
  // new_claim.owner = splits[1];

  // } else if (splits[0].contains("pick_crops")) {
  // new_claim.pick_crops = Boolean.parseBoolean(splits[1]);

  // } else if (splits[0].contains("live_stock")) {
  // new_claim.live_stock = Boolean.parseBoolean(splits[1]);
  // } else if (line.length() == 0) {
  // campfires.add(new_claim);
  // System.out.println(new_claim.toString());
  // System.out.println("::this is the END of a claim::");
  // }
  // System.out.println(line);
  // }
  // scanner.close();
  // } catch (FileNotFoundException e) {
  // System.out.println("File not found");
  // }
  // }
  // HUH//
  menu prompt = new menu();

  @EventHandler
  public void onServerStart(ServerLoadEvent ev) {
    System.out.println(String.format("Does this not work %s", deaths.toString()));
    // loadFromFile();
    System.out.println(String.format("Does this not work %s", deaths.toString()));
    loader();
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent ev) {
    Player player = ev.getPlayer();
    prompt.closeMenu(player);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent ev) {
    prompt.playerSelection(ev);
  }

  // FIXME: Issue when player clicks blocks with the item.
  // NOTE: fixed this by changing click to crouch. to confirm selection
  @EventHandler
  public void onPlayerSneak(PlayerToggleSneakEvent ev) {
    Player player = ev.getPlayer();
    boolean isSneaking = ev.isSneaking();

    int player_index = prompt.getPlayer(player);
    // boolean there = playersWithMenuOpen.stream().filter(o -> o.playerName ==
    // player.getName()).findFirst().isPresent();
    // int index = -1;
    // if (there == true) {
    // index = playersWithMenuOpen
    // .indexOf(playersWithMenuOpen.stream().filter(o -> o.playerName ==
    // player.getName()).findFirst().get());
    // }

    if (isSneaking == true && player_index > -1) {
      // String playerSelected = playersWithMenuOpen.get(index).selection;
      String player_selected_text = prompt.has_menu_open.get(player_index).getSelectedText();
      boolean success = false;
      boolean sub_menu = false;
      int xp = player.getLevel();
      if (player_selected_text == "NEXT PAGE") {
        success = true;
        sub_menu = true;
        prompt.nextPage(player);
      }
      if (player_selected_text == main_menu_selections.get(0).getName()) {
        if (xp >= main_menu_selections.get(0).getXPCost()) {
          int dex = -1;
          if (deaths.size() > 0) {
            dex = deaths.indexOf(deaths.stream().filter(o -> o == player).findFirst().get());
            if (dex > -1) {
              // NOTE: this looks like its had been fixed, this line was using a random index,
              // but no more.
              player_deaths death_data = deaths.get(player_index);
              player.setLevel(xp - main_menu_selections.get(0).getXPCost());
              player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
              player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
              player.teleportAsync(death_data.loc);
              success = true;

            }
          } else {
            player.sendMessage(ChatColor.RED + "no deaths");
          }
          if (dex == -1) {
            player.sendMessage(ChatColor.RED + "seems like you havent died yet");

          }
        }
      }
      if (player_selected_text == main_menu_selections.get(1).getName()) {
        if (xp >= main_menu_selections.get(1).getXPCost()) {
          var has_bed = player.getBedSpawnLocation();
          if (has_bed == null) {
            player.sendMessage(ChatColor.RED + "you don't have a bed.");
            success = false;
          } else {
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.setLevel(xp - main_menu_selections.get(1).getXPCost());
            player.teleportAsync(player.getBedSpawnLocation());
            success = true;
          }
        }
      }
      // TODO: figure out how to make dynamic menus.. will be used for players to TP
      // to.
      // Also for dynamic lists.
      if (player_selected_text == main_menu_selections.get(2).getName()) {
        ArrayList<String> options = new ArrayList<>();
        options.add("steve");
        options.add("jim");
        // options.add("pam");
        // options.add("table");
        // options.add("slim");
        // options.add("shady");
        // options.add("stripes");
        // options.add("echo");
        // options.add("bob");
        // options.add("jimmy");
        // options.add("tommy");
        // options.add("not_me_you");
        // options.add("the_guy");
        for (Player p : Bukkit.getOnlinePlayers()) {
          if (p.equals(player)) {
            continue;
          }
          options.add(p.getName());
        }
        prompt.closeMenu(player);
        success = true;
        sub_menu = true;
        prompt.openMenu(options, player);
      }
      if (player_selected_text == main_menu_selections.get(3).getName()) {
        var options = List.of("SPAWN", "SHOPPING");
        prompt.closeMenu(player);
        success = true;
        sub_menu = true;
        prompt.openMenu(options, player);
      }
      if (player_selected_text == "SPAWN") {
        if (xp >= 3) {
          player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f,
              1f);
          player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
          player.setLevel(xp - 3);
          player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
          success = true;
        }
      }
      if (player_selected_text == "SHOPPING") {
        if (xp >= 3) {
          player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
          player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
          player.setLevel(xp - 3);
          Location new_location = new Location(Bukkit.getWorld("world"), -1806, 73, 1502);
          // player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
          player.teleportAsync(new_location);
          success = true;
        }
      }
      if (player_selected_text == main_menu_selections.get(4).getName()) {
        player.sendMessage(
            ChatColor.AQUA + "What is there to tell? you crouch to carry on with your selection and it costs xp.");
        player.sendMessage(ChatColor.AQUA
            + "Oh btw if you have an empty bottle in hand you can crouch use it and collect you xp in it. Great for later use.");
        success = true;
      }
      if (success == false) {
        // FIXME: there is an issue here, right clicking a block acts incorrectly
        // playersWithMenuOpen.remove(index);
        // player.removePotionEffect(PotionEffectType.BLINDNESS);
        Audience audience = Audience.audience(player);
        audience.sendActionBar(() -> Component.text(ChatColor.RED + "not Enough xp"));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
        // playersWithMenuOpen.remove(index);
        // player.removePotionEffect(PotionEffectType.BLINDNESS);
      }
      if (sub_menu != true) {
        prompt.closeMenu(player);
      }
      if (player_selected_text != "NEXT PAGE") {
        if (prompt.has_menu_open.get(player_index).getSelectedText() == main_menu_selections.get(2).getName()) {
          for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName() == player_selected_text) {
              player.teleportAsync(p.getLocation());
              break;
            }
          }
        }
        prompt.has_menu_open.get(player_index).setPrevieusSelected(player_selected_text);
      }
      player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent ev) {
    Player player = ev.getPlayer();
    ItemStack item = player.getInventory().getItemInMainHand();
    Material itemType = item.getType();
    Action action = ev.getAction();

    // player.sendMessage(ChatColor.AQUA + "______");
    // player.sendMessage(ChatColor.AQUA + "ItemType: " + itemType);

    // TODO: min level of xp needed to store is 10 levels.. should also give back 10
    // levels
    // Bottle you xp
    Audience audience = Audience.audience(player);
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
    // on use Magic Mirror
    if (ev.getItem() != null) {
      //FIXME: create a copy of the book and remove the persistanet data and then check to see if the items
      //are equal without that data.
      ItemStack magic_mirror_no_data = new ItemStack(Item_Manager.magic_mirror_book);
      ItemMeta magic_meta = magic_mirror_no_data.getItemMeta();

      ItemStack hand_item = new ItemStack(ev.getItem());
      hand_item.setItemMeta(magic_meta);

      if (magic_mirror_no_data.equals(hand_item)) {
        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
          {//Decrease BOOK USE
            NamespacedKey key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
            ItemMeta meta = ev.getItem().getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            Integer max_uses = 6;
            Integer cur_value = container.get(key, PersistentDataType.INTEGER);
            if (cur_value == 0) {
              return; // no uses left
            }
            Integer new_value = cur_value -1;
            container.set(key,PersistentDataType.INTEGER,new_value);
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, new_value);
            ev.getItem().setItemMeta(meta);
            lore.add(Component.text("Warps it's users") );
  	        lore.add(Component.text(String.format("%s/%s uses",new_value,max_uses)));
            ev.getItem().lore().clear();
            ev.getItem().lore(lore);
          }

          // FIXME: so the issue is that the "options" are just tossed right after and
          // menuPrompt does not see them
          // NOTE: i could be wrong        
          var prompt_options = List.of("LAST DEATH", "BED", "WARPS", "PLAYERS", "INFO");
          prompt.openMenu(prompt_options, player);

        } else {
          // close the menu
          prompt.closeMenu(player);
          player.sendMessage(
              ChatColor.GOLD + "HOW TO USE: look up/down to see all selections. to confirm your selection, crouch.");
          player.sendMessage(ChatColor.GRAY
              + "NOTE: you can crouch click with an empty bottle to store your xp for later use. crouch use an enchanted bottle to get the full xp back.");
          player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1f);
          //// player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);

        }
        // }
      }
    }
  }

  @EventHandler
  public void onPlayerDamageEntity(EntityDamageByEntityEvent ev) {
    if (ev.getEntity() instanceof Enderman && ev.getDamager() instanceof Player) {
      Player player = (Player) ev.getDamager();
      // player.sendMessage("Something is working ..but..");
      // Enderman enderman = (Enderman) ev.getEntity();
      if (player.getInventory().getItemInMainHand().getType().equals(Material.BOOK)) {
        // player.sendMessage("This should be working then..");
        player.getInventory().addItem(Item_Manager.magic_mirror_book);
        player.playSound(ev.getEntity().getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.BLOCKS, 1, 1);

        ItemStack old_stack = player.getInventory().getItemInMainHand();
        old_stack.setAmount(old_stack.getAmount() - 1);
        player.getInventory().setItemInMainHand(old_stack);
      }
      // do something when player attacks enderman
    }
  }

  // @EventHandler
  // public void onPlayerMove(PlayerMoveEvent ev) {
  // var player = ev.getPlayer();
  // var block = ev.getTo().clone().subtract(0.0, 0.1, 0.0).getBlock();
  // // does anyone have the menu?
  // // player.sendMessage(new_menu.has_menu.toString());

  // // Magic Mirror stuff
  // int index = prompt.get_player(player);
  // if (index != -1) {

  // // ArrayList<String> selection_options = new ArrayList<>();
  // // var main_selection_options = List.of("LAST DEATH", "BED", "WARPS",
  // "PLAYERS",
  // // "INFO");
  // var names_only = List.of(
  // main_menu_selections.get(0).name,
  // main_menu_selections.get(1).name,
  // main_menu_selections.get(2).name,
  // main_menu_selections.get(3).name,
  // main_menu_selections.get(4).name);
  // prompt.menu_prompt(names_only, player);

  // // TODO: this needs to be cleaned up. no need for all these strings
  // Audience audience = Audience.audience(player);
  // String selection = prompt.has_menu_open.get(index).player_selected_text;
  // if (selection == main_menu_selections.get(0).name) {
  // audience.sendActionBar(
  // () -> Component.text(String.format("%s",
  // main_menu_selections.get(0).getName()) + ChatColor.GRAY
  // + String.format("%s", main_menu_selections.get(0).getXPCost())));
  // } else if (selection == main_menu_selections.get(1).getName()) {
  // audience.sendActionBar(
  // () -> Component.text(String.format("%s",
  // main_menu_selections.get(1).getName()) + ChatColor.GRAY
  // + String.format("%s", main_menu_selections.get(1).getXPCost())));
  // } else if (selection == main_menu_selections.get(2).getName()) {
  // audience.sendActionBar(
  // () -> Component.text(String.format("%s",
  // main_menu_selections.get(2).getName()) + ChatColor.GRAY
  // + String.format("%s", main_menu_selections.get(2).getXPCost())));
  // } else if (selection == main_menu_selections.get(3).getName()) {
  // audience.sendActionBar(
  // () -> Component.text(String.format("%s",
  // main_menu_selections.get(3).getName()) + ChatColor.GRAY
  // + String.format("%s", main_menu_selections.get(3).getXPCost())));
  // } else {
  // audience.sendActionBar(
  // () -> Component.text(String.format("%s",
  // main_menu_selections.get(4).getName()) + ChatColor.GRAY
  // + String.format("%s", main_menu_selections.get(4).getXPCost())));
  // }
  // boolean hasBlindness = false;
  // for (PotionEffect effect : player.getActivePotionEffects()) {
  // if (effect.getType().equals(PotionEffectType.BLINDNESS)) {
  // hasBlindness = true;
  // }
  // }
  // if (hasBlindness == false) {
  // prompt.close_menu(player);
  // // playersWithMenuOpen.remove(index);
  // }

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
      // String itemInUseName =
      // player.getInventory().getItem(newSlot).displayName().toString();

      // if (ev.getItem().equals(Item_Manager.mm)) {
      if (itemType.equals(Item_Manager.magic_mirror_book.getType())) {
        // if (itemInUseName.contains(TheItemName)) {
        if (!hasItemInHand.contains(player.getName())) {
          hasItemInHand.add(player.getName());
        }
        // }
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
          prompt.closeMenu(player);
        }
      }
    }

  }

  class PlayerDeathLoc {
    String playerName;
    String dim;
    int x;
    int y;
    int z;

  }

  // ArrayList<PlayerDeathLoc> playerDeathData = new ArrayList<PlayerDeathLoc>();

  @EventHandler
  public void onPlayerDeath(EntityDeathEvent ev) {
    EntityType entity = ev.getEntityType();
    // check if its a player that died
    if (entity.equals(EntityType.PLAYER)) {
      Entity player = ev.getEntity();
      var location = player.getLocation();
      // check if the array contains the player or not
      // boolean there = playerDeathData.stream().filter(o -> o.playerName ==
      // player.getName()).findFirst()
      // .isPresent();
      // if (there == true) {
      // int index = playerDeathData
      // .indexOf(playerDeathData.stream().filter(o -> o.playerName ==
      // player.getName()).findAny().get());
      // playerDeathData.get(index).dim = player.getWorld().getName();
      // playerDeathData.get(index).x = (int) location.getX();
      // playerDeathData.get(index).y = (int) location.getY();
      // playerDeathData.get(index).z = (int) location.getZ();
      // } else {
      // PlayerDeathLoc playerDeath = new PlayerDeathLoc();
      // playerDeath.playerName = player.getName();
      // playerDeath.dim = player.getWorld().getName();
      // playerDeath.x = (int) location.getX();
      // playerDeath.y = (int) location.getY();
      // playerDeath.z = (int) location.getZ();
      // playerDeathData.add(playerDeath);

      // }
      player_deaths this_death = new player_deaths();
      this_death.name = (Player) player;
      this_death.loc = player.getLocation();
      int index = -1;
      if (deaths.size() > 0) {
        index = deaths.indexOf(deaths.stream().filter(p -> p == player).findFirst().get());
        if (index > -1) {
          deaths.get(index).loc = player.getLocation();
        }
      }
      if (index == -1) {
        deaths.add(this_death);
      }
      // saveToFile();
    }
  }

}
