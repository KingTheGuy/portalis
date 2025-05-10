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
    // if (sender instanceof Player) {

    // } else {

    // }
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
          System.out.println("this command can only be executed by a player");
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
          System.out.println("this command can only be executed by a player");
        }
        break;
      case "portalis:setselectioncolor":
        ChatColor color = DialMenu.selection_color;
        if (args.length > 0) {
          switch (args[0]) {
            case "aqua":
              color = ChatColor.AQUA;
              break;
            case "blue":
              color = ChatColor.BLUE;
              break;
            case "red":
              color = ChatColor.RED;
              break;
            case "gold":
              color = ChatColor.GOLD;
              break;
            case "purple":
              color = ChatColor.LIGHT_PURPLE;
              break;

            default:
              color = ChatColor.AQUA;
              break;
          }
        }
        DialMenu.selection_color = color;
        break;
    }
    System.out.println(String.format("is this working?%s %s %s %s", sender, command, label, args));
    return true;
  }

}
