package com.surv;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.surv.items.Item_Manager;

// import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

//TODO: add SD tp

//TODO: clean/ do re-something everything
//TODO: change the way the player recives a magic mirror 
//maybe some in world recipe
//TODO: figure out how to "add" my own item. so that the magic mirror is stricktly made
//TODO: implement save/ reload data.

public class magic_mirror implements Listener {
  ///// Config////
  // String TheItemName = "Magic Mirror";
  // Material TheItemType = Material.BOOK;
  //// END////

  // TODO: add a delay, on item use.. will cause some problems otherwise.
  // may need to change the confirmation on select to be crouch.

  class PlayerMenuOption {
    String playerName;
    String selection = "-";
  }

  ArrayList<PlayerMenuOption> playersWithMenuOpen = new ArrayList<PlayerMenuOption>();

  public String menuSelecting(float pitch, Player player) {
    String selected = "-";
    if (pitch >= -90 && pitch <= -60) {
      selected = "BED";
    }
    if (pitch >= -61 && pitch <= -30) {
      selected = "SPAWN";
    }
    if (pitch >= -29 && pitch <= 29) {
      selected = "LAST DEATH";
    }
    if (pitch >= 30 && pitch <= 61) {
      selected = "SHOPS";
    }
    if (pitch >= 60 && pitch <= 90) {
      selected = "INFO";
    }
    // play sound as the player changes their selection
    // index index = playersWithMenuOpen
    // .indexOf(playersWithMenuOpen.stream().filter(o -> o.playerName ==
    // player.getName()).findFirst().orElse(-1));
    boolean there = playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().isPresent();
    int index = -1;
    if (there == true) {
      index = playersWithMenuOpen
          .indexOf(playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().get());
    }
    // index being if the player has a spot in the array
    if (index > -1) {
      String oldSelection = playersWithMenuOpen.get(index).selection;
      if (oldSelection != selected) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1f, 1f);
      }
      playersWithMenuOpen.get(index).selection = selected;
    }
    return selected;
  }

  @EventHandler
  public void onLeave(PlayerQuitEvent ev) {
    Player player = ev.getPlayer();
    boolean there = playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().isPresent();
    int index = -1;
    if (there == true) {
      index = playersWithMenuOpen
          .indexOf(playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().get());
      playersWithMenuOpen.remove(index);
    }
  }
  // @EventHandler
  // public void onChat(AsyncChatEvent ev) {
  // Player player = ev.getPlayer();
  // Audience audience = Audience.audience(player);
  // String msgContent =
  // PlainTextComponentSerializer.plainText().serialize(ev.originalMessage());
  // if (msgContent.startsWith("~")) {
  // if (msgContent.contains("head")) {

  // }
  // } else {

  // }
  // String msg = "[slimshady]<" + player.getName() + "> " + msgContent;
  // audience.sendMessage(() -> Component.text(ChatColor.WHITE + ":" + msg));
  // ev.setCancelled(true);

  // }

  // FIXME: Issue when player clicks blocks with the item.
  // NOTE: fixed this by changing click to crouch. to confirm selection
  @EventHandler
  public void onPlayerSneak(PlayerToggleSneakEvent ev) {
    Player player = ev.getPlayer();
    boolean isSneaking = ev.isSneaking();

    boolean there = playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().isPresent();
    int index = -1;
    if (there == true) {
      index = playersWithMenuOpen
          .indexOf(playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().get());
    }
    if (isSneaking == true && index > -1) {
      String playerSelected = playersWithMenuOpen.get(index).selection;
      boolean success = false;
      int xp = player.getLevel();
      if (playerSelected == "BED") {
        if (xp >= 6) {
          var hasBed = player.getBedSpawnLocation();
          if (hasBed == null) {
            player.sendMessage(ChatColor.RED + "you don't have a bed.");
          } else {
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.setLevel(xp - 6);
            player.teleportAsync(player.getBedSpawnLocation());
            success = true;
          }
        }
      }
      if (playerSelected == "SPAWN") {
        // 5xp
        if (xp >= 4) {
          // tp
          player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
          player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
          player.setLevel(xp - 4);
          player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
          success = true;

        }

      }
      if (playerSelected == "SHOPS") {
        // 5xp
        if (xp >= 4) {
          // tp
          player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
          player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
          player.setLevel(xp - 4);
          Location new_location = new Location(Bukkit.getWorld("world"), -1806, 73, 1502);
          // player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
          player.teleportAsync(new_location);
          success = true;

        }

      }
      if (playerSelected == "LAST DEATH") {
        // 3xp
        if (xp >= 3) {
          boolean dead = playerDeathData.stream().filter(o -> o.playerName == player.getName()).findFirst().isPresent();
          if (dead == true) {
            int hasPlayerDied = playerDeathData
                .indexOf(playerDeathData.stream().filter(o -> o.playerName == player.getName()).findFirst().get());
            // NOTE: this looks like its had been fixed, this line was using a random index,
            // but no more.
            PlayerDeathLoc deathData = playerDeathData.get(hasPlayerDied);
            player.setLevel(xp - 3);
            player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            Location loc = new Location(Bukkit.getWorld(deathData.dim), deathData.x, deathData.y, deathData.z);
            player.teleportAsync(loc);
            success = true;
          } else {
            player.sendMessage(ChatColor.RED + "seems like you havent died yet");

          }
        }

      }
      if (playerSelected == "INFO") {
        // show the player info on how to use item
        player.sendMessage(
            ChatColor.AQUA + "What is there to tell? you crouch to carry on with your selection and it costs xp.");
        player.sendMessage(ChatColor.AQUA
            + "Oh btw if you have an empty bottle in hand you can crouch use it and collect you xp in it. Great for later use.");
        success = true;

      }
      if (playerSelected == "CLOSE") {
        success = true;

      }
      if (success == true) {
        // FIXME: there is an issue here, right clicking a block acts incorrectly
        playersWithMenuOpen.remove(index);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
      } else {
        if (playerSelected != "-") {
          Audience audience = Audience.audience(player);
          audience.sendActionBar(() -> Component.text(ChatColor.RED + "not Enough xp"));
          player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
          playersWithMenuOpen.remove(index);
          player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
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
    // if (itemType.equals(TheItemType)) {
    if (ev.getItem() != null) {
      if (ev.getItem().equals(Item_Manager.mm)) {
        // String itemName = item.displayName().toString();
        // if (itemName.contains(TheItemName)) {
        // item.setLore();
        boolean there = playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst()
            .isPresent();
        int index = -1;
        if (there == true) {
          index = playersWithMenuOpen
              .indexOf(playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().get());
        }
        if (playersWithMenuOpen.size() > 0) {
          index = playersWithMenuOpen
              .indexOf(playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().get());
        }

        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {
          // add player to array if not already in it

          if (index <= -1) {
            PlayerMenuOption playerSelection = new PlayerMenuOption();
            playerSelection.playerName = player.getName();
            playersWithMenuOpen.add(playerSelection);
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.BLINDNESS, 600, 1).withAmbient(false).withParticles(false));
          }
        } else {
          // close the menu
          if (index > -1) {
            playersWithMenuOpen.remove(index);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
          }
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
        player.getInventory().addItem(Item_Manager.mm);
        player.playSound(ev.getEntity().getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.BLOCKS, 1, 1);

        ItemStack old_stack = player.getInventory().getItemInMainHand();
        old_stack.setAmount(old_stack.getAmount() - 1);
        player.getInventory().setItemInMainHand(old_stack);
      }
      // do something when player attacks enderman
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent ev) {
    var player = ev.getPlayer();
    var block = ev.getTo().clone().subtract(0.0, 0.1, 0.0).getBlock();
    // player.sendMessage("this is it:" + block.getType());
    // player.getVehicle().getType();

    if (block.getType() == Material.DIRT_PATH) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1).withAmbient(false).withParticles(true));
      if (player.isInsideVehicle()) {
        EntityType entType = player.getVehicle().getType();
        if (entType == EntityType.HORSE || entType == EntityType.MULE || entType == EntityType.DONKEY
            || entType == EntityType.PIG) {
          if (player.getVehicle() instanceof LivingEntity livingEntity) {
            livingEntity.addPotionEffect(
                new PotionEffect(PotionEffectType.SPEED, 40, 4).withAmbient(false).withParticles(true));
          }
        }
      }
    }
    if (playersWithMenuOpen.size() > 0) {
      boolean there = playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst()
          .isPresent();
      if (there == true) {
        int index = playersWithMenuOpen
            .indexOf(playersWithMenuOpen.stream().filter(o -> o.playerName == player.getName()).findFirst().get());
        String selection = menuSelecting(player.getLocation().getPitch(), player);
        Audience audience = Audience.audience(player);
        if (selection == "BED") {
          audience.sendActionBar(() -> Component.text("BED" + ChatColor.GRAY + " 6xp"));

        } else if (selection == "SPAWN") {
          audience.sendActionBar(() -> Component.text("SPAWN" + ChatColor.GRAY + " 4xp"));

        } else if (selection == "LAST DEATH") {
          audience.sendActionBar(() -> Component.text("LAST DEATH" + ChatColor.GRAY + " 3xp"));

        } else {
          audience.sendActionBar(() -> Component.text(selection));
        }
        boolean hasBlindness = false;
        for (PotionEffect effect : player.getActivePotionEffects()) {
          if (effect.getType().equals(PotionEffectType.BLINDNESS)) {
            hasBlindness = true;
          }
        }
        if (hasBlindness == false) {
          playersWithMenuOpen.remove(index);
        }

      }

    }
  }

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
      if (itemType.equals(Item_Manager.mm.getType())) {
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
          player.removePotionEffect(PotionEffectType.BLINDNESS);
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

  ArrayList<PlayerDeathLoc> playerDeathData = new ArrayList<PlayerDeathLoc>();

  @EventHandler
  public void onPlayerDeath(EntityDeathEvent ev) {
    EntityType entity = ev.getEntityType();
    // check if its a player that died
    if (entity.equals(EntityType.PLAYER)) {
      Entity player = ev.getEntity();
      var location = player.getLocation();
      // check if the array contains the player or not
      boolean there = playerDeathData.stream().filter(o -> o.playerName == player.getName()).findFirst()
          .isPresent();
      if (there == true) {
        int index = playerDeathData
            .indexOf(playerDeathData.stream().filter(o -> o.playerName == player.getName()).findAny().get());
        playerDeathData.get(index).dim = player.getWorld().getName();
        playerDeathData.get(index).x = (int) location.getX();
        playerDeathData.get(index).y = (int) location.getY();
        playerDeathData.get(index).z = (int) location.getZ();
      } else {
        PlayerDeathLoc playerDeath = new PlayerDeathLoc();
        playerDeath.playerName = player.getName();
        playerDeath.dim = player.getWorld().getName();
        playerDeath.x = (int) location.getX();
        playerDeath.y = (int) location.getY();
        playerDeath.z = (int) location.getZ();
        playerDeathData.add(playerDeath);

      }
    }
  }

  // @EventHandler
  // public void onPlayerDeath(PlayerDeathEvent ev) {
  // // Player player = ev.getPlayer();
  // List<ItemStack> dropped_items = ev.getDrops();
  // // Location death_location = player.getLocation();
  // if (dropped_items.contains(Item_Manager.mm)) {
  // }
  // }
}
