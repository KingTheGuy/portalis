package com.kingtheguy;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

// GET BOOK/ GIVE BOOK.. hit enderman with a book.

//TODO: this may be the chance to add particle markers showing where to aim for menu option
//FIXME: timer may not be needed.. just using the built in effect for a timer
//TODO: figure out how to do sub menus.. by by just recalling a menu with new options

public class menu {
	// may not be able to peep who have menu open here.
	public ArrayList<player_selection> has_menu_open = new ArrayList<>();

	public int getPlayer(Player player) {
		if (has_menu_open.size() > 0) {
			int index = has_menu_open.indexOf(has_menu_open.stream().filter(p -> p.the_player == player).findFirst().get());
			if (index != -1) {
				return index;
			}
		}
		return -1;
	}

	public class player_selection {
		private Player the_player;
		private String player_selected_text;
		private String previeus_selected = null;
		private List<String> prompt_options;
		private int sub_menu_page = 1;

		public String getSelectedText() {
			return this.player_selected_text;
		}

		public void setSelectedText(String text) {
			this.player_selected_text = text;
		}

		public String getPrevieusSelected() {
			return this.previeus_selected;
		}

		public void setPrevieusSelected(String text) {
			this.previeus_selected = text;
		}
	}

	// needs to be called
	public void playerSelection(PlayerMoveEvent ev) {
		Player player = ev.getPlayer();

		int index = getPlayer(player);
		if (index == -1) {
			return;
		}
		System.out.println(String.format("should be on page: %s", has_menu_open.get(index).sub_menu_page));

		promptPlayer(has_menu_open.get(index).prompt_options, player);
		Audience audience = Audience.audience(player);
		audience.sendActionBar(
				() -> Component.text(String.format("%s", has_menu_open.get(index).player_selected_text) + ChatColor.GRAY));

		boolean hasBlindness = false;
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (effect.getType().equals(PotionEffectType.BLINDNESS)) {
				hasBlindness = true;
			}
		}
		if (hasBlindness == false) {
			closeMenu(player);
		}

	}

	private boolean within(int v, int w1, int w2) {
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

	// this needs to run everytime the player moves
	private void promptPlayer(List<String> options, Player player) {
		int pitch = (int) player.getLocation().getPitch();
		// requires player move
		int selected = 0;
		if (within(pitch, -90, -60)) {
			selected = 0;
		}
		if (within(pitch, -61, -30)) {
			selected = 1;
		}
		if (within(pitch, -29, 29)) {
			selected = 2;
		}
		if (within(pitch, 30, 61)) {
			selected = 3;
		}
		if (within(pitch, 60, 90)) {
			selected = 4;
		}
		int index = getPlayer(player);
		if (index == -1) {
			return;
		}
		if (options.size() > 5) {
			// for a dynamic list
			var chunk = has_menu_open.get(index).sub_menu_page * 4;
			if (selected == 4) {
				has_menu_open.get(index).player_selected_text = "NEXT PAGE";
				return;
			}
			selected = (chunk - 4) + selected;
		}
		if (selected > options.size() - 1) {
			has_menu_open.get(index).player_selected_text = "-";
			return;
		}
		has_menu_open.get(index).player_selected_text = options.get(selected);

		// player.sendMessage(String.format("selected %s: ", selected));
		// now we have the player's selection
	}

	public void nextPage(Player player) {
		int index = getPlayer(player);
		if (index == -1) {
			return;
		}
		has_menu_open.get(index).sub_menu_page++;
	}

	public void closeMenu(Player player) {
		int index = getPlayer(player);
		if (index != -1) {
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.clearTitle();
			has_menu_open.get(index).sub_menu_page = 1;
			has_menu_open.remove(index);
		}
	}

	// effect and auto close timer
	public void openMenu(List<String> promp_list, Player player) {
		player_selection new_player = new player_selection();
		new_player.the_player = player;
		new_player.player_selected_text = " ";

		new_player.prompt_options = promp_list;

		has_menu_open.add(new_player);
		player.addPotionEffect(
				new PotionEffect(PotionEffectType.BLINDNESS, 600, 1).withAmbient(false).withParticles(false));
	}
}
