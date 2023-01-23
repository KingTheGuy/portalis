package com.surv;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

//TODO: this may be the chance to add particle markers showing where to aim for menu option
//FIXME: timer may not be needed.. just using the built in effect for a timer
//TODO: figure out how to do sub menus.. by by just recalling a menu with new options

public class menu {
	// may not be able to peep who have menu open here.
	public List<player_selection> has_menu = new ArrayList<>();

	class player_selection {
		Player player;
		String selected;
	}

	boolean within(int v, int w1, int w2) {
		// why so many if statements? i have no idea, they are the same.. but yet they
		// were needed
		if (v > 0) {
			if (v >= w1 && v <= w2) {
				return true;
			}
		}
		if (v < 0) {
			if (v >= w1 && v <= w2) {
				return true;
			}
		}
		return false;
	}

	// this will show the player a list of options
	// NOTE: i need this to loop.. looping would need to end once the player
	// selects.. so it can't be done here
	// NOTE: ok i am looping throught this using player move
	public void menu_options(List<String> options, Player player) {
		int pitch = (int) player.getLocation().getPitch();
		// requires player move
		String selected = " ";
		if (within(pitch, -90, -60)) {
			if (options.get(0) != null) {
				selected = options.get(0);
			}
		}
		if (within(pitch, -61, -30)) {
			if (options.get(1) != null) {
				selected = options.get(1);
			}
		}
		if (within(pitch, -29, 29)) {
			if (options.get(2) != null) {
				selected = options.get(2);
			}
		}
		if (within(pitch, 30, 61)) {
			if (options.get(3) != null) {
				selected = options.get(3);
			}
		}
		if (within(pitch, 60, 90)) {
			if (options.get(4) != null) {
				selected = options.get(4);
			}
		}
		int index = get_player(player);
		if (index != -1) {
			has_menu.get(index).selected = selected;
			// player.sendMessage(String.format("selected %s: ", selected));
		}
		// now we have the player's selection
	}

	public int get_player(Player player) {
		if (has_menu.size() > 0) {
			int index = has_menu.indexOf(has_menu.stream().filter(p -> p.player == player).findFirst().get());
			if (index != -1) {
				return index;
			}
		}
		return -1;
	}

	public void close_menu(Player player) {
		int index = get_player(player);
		if (index != -1) {
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.clearTitle();
			has_menu.remove(index);
		}
	}

	// effect and auto close timer
	public void open_menu(Player player) {
		player_selection new_player = new player_selection();
		new_player.player = player;
		new_player.selected = " ";
		has_menu.add(new_player);
		player.addPotionEffect(
				new PotionEffect(PotionEffectType.BLINDNESS, 600, 1).withAmbient(false).withParticles(false));
	}

}