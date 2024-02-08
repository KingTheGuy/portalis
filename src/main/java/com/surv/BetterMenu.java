package com.surv;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.surv.items.Item_Manager;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

//TODO: getPlayer should expect and integer, should change all uses to check if returning a -1

public class BetterMenu {

	ArrayList<PlayerWithMenu> player_with_menu = new ArrayList<>();

	public Integer getPlayer(Player lp) {
		for (PlayerWithMenu p : player_with_menu) {
			if (p.player.equals(lp)) {
				return player_with_menu.indexOf(p);
			}
		}
		return -1;
	}

	public void closeMenu(Player player) {
		Integer index = getPlayer(player);
		if (index != -1) {
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			// player.clearTitle();
			Audience audience = Audience.audience(player);
			// audience.clearTitle();
			audience.sendActionBar(
					() -> Component.text(""));
			player_with_menu.get(index).menu_options = null;
			player_with_menu.remove(player_with_menu.get(index));
		}
	}

	public void openMenu(List<String> promp_list, Player player) {
		player.addPotionEffect(
				new PotionEffect(PotionEffectType.BLINDNESS, 600, 1).withAmbient(false).withParticles(false));
		Integer index = getPlayer(player);
		if (index == -1) {
			PlayerWithMenu new_player = new PlayerWithMenu();
			new_player.createPlayer(player);
			player_with_menu.add(new_player);
			new_player.selection = "MAIN";
			addContext(new_player);
			index = getPlayer(player);
		}
		// FIXME: something with the promp list stuff is broken.
		PlayerWithMenu has_menu_open = player_with_menu.get(index);
		List<String> new_list = new ArrayList<>();
		for (int x = 0; x < promp_list.size(); x++) {
			new_list.add(promp_list.get(x));
		}
		has_menu_open.selection = new_list.get(0);

		// if (promp_list.size() < 4) {
		// promp_list.add("CANCEL");
		// }
		// if (promp_list.size() < 2) {
		// }
		new_list.add("CANCEL");

		// if (has_menu_open.getMenuOptions().size() > 5) {
		// List<String> new_list = new ArrayList<>();
		// for (int x = 0; x < 4; x++) {
		// new_list.add(has_menu_open.getMenuOptions().get(x));
		// }
		// new_list.add("NEXT PAGE");
		// has_menu_open.menu_options = new_list;
		// }

		// if (has_menu_open.selected > getMenuOptions().size() - 1) {
		// selection = "CANCEL";
		// return;
		// }
		has_menu_open.menu_options = new_list;
		Audience audience = Audience.audience(player);
		audience.sendActionBar(
				() -> Component.text(formatListToString(has_menu_open.getMenuOptions(), has_menu_open.selection)));
	}

	public class PlayerContext {
		String prompt;
		String answer;

		public String getPrompt() {
			return prompt;
		}

		public String getAnswer() {
			return answer;
		}
	}

	public class PlayerWithMenu {
		Player player;
		String selection;
		String selection_last;
		Integer page;
		float initial_yaw;
		float last_yaw_value;
		int last_selection_value;
		ArrayList<PlayerContext> context = new ArrayList<>();

		private List<String> menu_options = new ArrayList<>();

		public List<String> getMenuOptions() {
			return menu_options;
		}

		public void createPlayer(Player the_player) {
			this.player = the_player;
			this.menu_options = new ArrayList<String>();
			this.selection = "";
			this.selection_last = "";
			this.initial_yaw = the_player.getLocation().getYaw();
		}

		// this needs to run everytime the player moves
		/**
		 * show the player a prompt with a list of options to choose from
		 * 
		 * @param options a list of strings for the player to choose from
		 * @param player  the player
		 */
		public void prompt_selections() {
			// NOTE: this should not need to have player passed in
			// Integer index = getPlayer(player);
			// PlayerWithMenu has_menu_open = player_with_menu.get(index);
			// if (index == -1) {
			// return;
			// }

			// int pitch = (int) player.getLocation().getPitch();
			float yaw = player.getLocation().getYaw();
			if (yaw < 0) {
				yaw += 180;
			}
			// System.out.printf("player yaw: %s", yaw);
			// requires player move
			int selected = last_selection_value;
			// System.out.println(yaw % 40);
			if (selected > getMenuOptions().size() - 1) {
				selected = 0;
			}
			if (18 < yaw % 40) {
				// System.out.println("yes");
				if (yaw > last_yaw_value) {
					if (selected < getMenuOptions().size() - 1) {
						selected++;
					}
				} else if (yaw < last_yaw_value) {
					if (selected > 0) {
						selected--;
					}
				}
			}
			if (last_yaw_value != yaw) {
				last_yaw_value = yaw;
			}
			last_selection_value = selected;
			selection = getMenuOptions().get(selected);
			// NOTE: this next line should not be hard coded here
			if (selection != selection_last) {
				selection_last = getMenuOptions().get(selected);
				System.out.print("the page flip sound should be playing");

				Location location = player.getLocation();
				Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
						Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
			}
		}
	}

	// private boolean within(float v, float w1, float w2) {
	// // 360/40 9
	// if (v > 0) {
	// if (v >= w1 && v <= w2) {
	// return true;
	// }
	// }
	// if (v < 0) {
	// if (v >= w1 && v <= w2) {
	// return true;
	// }
	// }
	// return false;
	// }

	public void playerSelection(PlayerMoveEvent ev) {
		Player player = ev.getPlayer();

		Integer index = getPlayer(ev.getPlayer());
		if (index == -1) {
			return;
		}
		PlayerWithMenu has_menu_open = player_with_menu.get(index);
		has_menu_open.prompt_selections();

		// using ACTIONBAR
		Audience audience = Audience.audience(player);
		// NOTE: this is the new format, need to apply it to everything else
		// String action_list = new String();
		// for (int x = 0; x < has_menu_open.getMenuOptions().size(); x++) {
		// if (has_menu_open.getMenuOptions().get(x) == has_menu_open.selection) {
		// action_list += String.format(ChatColor.GOLD + "[%s]" + ChatColor.WHITE,
		// has_menu_open.selection);
		// continue;
		// }
		// action_list += String.format(" %s ", has_menu_open.getMenuOptions().get(x));
		// }
		// String action_selections = action_list;
		audience.sendActionBar(
				() -> Component.text(formatListToString(has_menu_open.getMenuOptions(), has_menu_open.selection)));

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

	public void playerChoose(PlayerInteractEvent ev) {
		// System.out.println("sneaking>> YESSSSS");
		// if (ev.isSneaking() == false) {
		// return;
		// }
		if (ev.getPlayer().getInventory().getItemInMainHand() == null) {
			return;
		}
		if (!ev.getHand().equals(EquipmentSlot.HAND)) {
			return;
		}

		Player player = ev.getPlayer();
		Integer index = getPlayer(player);

		if (index == -1) {
			return;
		}
		if (player.getInventory().getItemInMainHand().isEmpty()) {
			return;
		}
		if (!player.getInventory().getItemInMainHand().getType().equals(Item_Manager.magic_mirror_book.getType())) {
			return;
		}

		PlayerWithMenu has_menu_open = player_with_menu.get(index);

		if (has_menu_open.selection == "NEXT PAGE") {
			has_menu_open.page++;
			return; // this should not trigger a new page just update it so it should return here
		}
		addContext(has_menu_open);

		// player_selection.addNewContext(player_selection.getSelectedText());
		// PlayerContext context = new PlayerContext();

		// if (has_menu_open.selection == "CANCEL") {
		// closeMenu(player);
		// return; // same reason as above
		// }
	}

	public void addContext(PlayerWithMenu player) {
		PlayerContext new_context = new PlayerContext();
		new_context.prompt = player.selection;
		if (player.context.size() >= 1) {
			player.context.get(player.context.size() - 1).answer = player.selection;
			if (player.context.get(player.context.size() - 1).prompt == new_context.prompt) {
				// we do not want copies
				return;
			}
		}
		player.context.add(new_context);
	}

	public String formatListToString(List<String> list, String selection) {
		String action_list = new String();
		for (int x = 0; x < list.size(); x++) {
			if (list.get(x) == selection) {
				action_list += String.format(ChatColor.GOLD + "[%s]" + ChatColor.WHITE, selection);
				continue;
			}
			action_list += String.format(" %s ", list.get(x));
		}
		return action_list;
	}
}
