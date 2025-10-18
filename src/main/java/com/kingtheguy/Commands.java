package com.kingtheguy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.kingtheguy.items.Item_Manager;

import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.isOp()) {
      sender.sendMessage("unable to run command.. permission missing.");
      return false;
    }
    if (sender instanceof Player == false) {
      System.out.println("this command can only be executed by a player");
      return false;
    }
    switch (label) {
      case "portalis:givebook":
        if (sender instanceof Player) {
          Player player = (Player) sender;
          if (args.length > 0) {
            switch (args[0]) {
              case "full":
                player.getInventory().addItem(new ItemStack(Item_Manager.refillPortalis(8)));
                break;
              case "8":
                player.getInventory().addItem(new ItemStack(Item_Manager.refillPortalis(8)));
                break;
              default:
                player.getInventory().addItem(new ItemStack(Item_Manager.portalis_book));
                break;
            }
          } else {
            player.getInventory().addItem(new ItemStack(Item_Manager.portalis_book));
          }
        } else {
          // System.out.println("this command can only be executed by a player");
        }
        break;
      case "portalis:giveinfusedpaper":
        if (sender instanceof Player) {
          Player player = (Player) sender;
          if (args.length > 0) {
            player.getInventory().addItem(new ItemStack(Item_Manager.infused_paper).add(Integer.parseInt(args[0])));
          } else {
            player.getInventory().addItem(new ItemStack(Item_Manager.infused_paper));
          }
        } else {
          // System.out.println("this command can only be executed by a player");
        }
        break;
      case "portalis:setselectioncolor":
        ChatColor color = DialMenu.selection_color;
        color = stringToColor(args[0]);
        DialMenu.selection_color = color;
        break;
      case "portalis:giverippedpage":
        if (sender instanceof Player) {
          Player player = (Player) sender;
          if (args.length > 0) {
            // player.getInventory().addItem(new
            // ItemStack(Item_Manager.createRippedWarpPage(args[0])));
            player.getInventory().addItem(new ItemStack(Item_Manager.createRippedWarpPage(String.join(" ", args))));
          } else {
            player.getInventory().addItem(new ItemStack(Item_Manager.ripped_page));
            // System.out.println("this command can only be executed by a player");
          }
        }
        break;
    }
    return true;
  }

  public static ChatColor stringToColor(String text) {
      // This is the best i can do.. at least i did this using a macro.
      // the macro(helix btw): "aps<S-A><S-Q><S-U><S-A><ret>a<C-w><esc>hp,jxgs
      switch (text.toUpperCase()) {
        case "AQUA":
          return ChatColor.AQUA;
        case "BLACK":
          return ChatColor.BLACK;
        case "BLUE":
          return ChatColor.BLUE;
        case "DARK_AQUA":
          return ChatColor.DARK_AQUA;
        case "DARK_BLUE":
          return ChatColor.DARK_BLUE;
        case "DARK_GRAY":
          return ChatColor.DARK_GRAY;
        case "DARK_GREEN":
          return ChatColor.DARK_GREEN;
        case "DARK_PURPLE":
          return ChatColor.DARK_PURPLE;
        case "DARK_RED":
          return ChatColor.DARK_RED;
        case "GOLD":
          return ChatColor.GOLD;
        case "GRAY":
          return ChatColor.GRAY;
        case "GREEN":
          return ChatColor.GREEN;
        case "LIGHT_PURPLE":
          return ChatColor.LIGHT_PURPLE;
        case "RED":
          return ChatColor.RED;
        case "WHITE":
          return ChatColor.WHITE;
        case "YELLOW":
          return ChatColor.YELLOW;
        default:
          return ChatColor.AQUA;
      }
  }
}
