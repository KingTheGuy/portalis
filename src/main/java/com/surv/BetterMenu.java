package com.surv;

import java.io.Serializable;
import java.time.Duration;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.surv.items.Item_Manager;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatColor;

//TODO: getPlayer should expect and integer, should change all uses to check if returning a -1

public class BetterMenu {

	ArrayList<PlayerWithMenu> player_with_menu = new ArrayList<>();
	List<Player> wait_list = new ArrayList<>();

	public Integer hasMenuOpen(Player player) {
		for (PlayerWithMenu p : player_with_menu) {
			if (p.player.equals(player)) {
				return player_with_menu.indexOf(p);
			}
		}
		return -1;
	}

	public void closeMenu(Player player) {
		Integer index = hasMenuOpen(player);
		if (index != -1) {
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			Audience audience = Audience.audience(player);
			// audience.showTitle(Title.title(Component.text(""),Component.text("")));
			// title(audience, "");
			audience.sendActionBar(
					() -> Component.text(""));
			player_with_menu.get(index).all_context = null;
			player_with_menu.remove(player_with_menu.get(index));
			wait_list.remove(player);
			Location location = player.getLocation();
			Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
					Sound.BLOCK_CHISELED_BOOKSHELF_PICKUP_ENCHANTED, 1f, 0.6f);
		}
	}

	public class PlayerContext {
		int id;
		List<String> context;
		Answer answer = new Answer();
	}

	public class Answer {
		String name;
		Object obj;
	}

	public class PlayerWithMenu {
		Player player;
		String selection;
		String selection_last;
		Integer page;
		float initial_yaw;
		int last_yaw_value;
		int last_selection_value = -1;
		int tick = 0;
		int tick_offset = 3;
		ArrayList<PlayerContext> all_context = new ArrayList<>();
		ItemStack using_item;

		// private List<String> menu_options = new ArrayList<>();

		public PlayerContext getAll_context() {
			return all_context.get(all_context.size() - 1);
		}

		// public List<String> getMenuOptions() {
		// return menu_options;
		// }

		public void createPlayer(Player the_player) {
			this.player = the_player;
			// this.all_context = new ArrayList<PlayerToggleSneakEvent>();
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
			Location location = player.getLocation();
			int yaw = (int) player.getLocation().getYaw();

			// yaw = yaw & 360;
			if (yaw < 0) {
				yaw += 360;
			}
			// player.sendMessage(String.format("YAW: %s", yaw));

			// System.out.printf("last Yaw: %s\n", yaw);
			// 360/9
			// System.out.printf("last Yaw: %s and switch\n", last_yaw_value);
			// TODO: go back to between range system.

			// prevents the selection from jumping back when going from 3 digits to 1
			if (String.valueOf(last_yaw_value).length() == 3) {
				if (String.valueOf(yaw).length() == 1) {
					last_yaw_value = yaw;
				}
			}
			// prevents the selection from jumping back when going from 3 digits to 1
			if (String.valueOf(last_yaw_value).length() == 1) {
				if (String.valueOf(yaw).length() == 3) {
					last_yaw_value = yaw;
				}
			}
			int selected = last_selection_value;
			if (selected > getAll_context().context.size() - 1) {
				selected = 0;
			}
			if (yaw % 50 >= 8) {
				// if (yaw % 40 >= 20) {
				if (yaw > last_yaw_value) {
					if (selected < getAll_context().context.size() - 1) {
						Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
								Sound.ITEM_SPYGLASS_USE, 0.5f, 1f);
						tick++;
					}
				} else if (yaw < last_yaw_value) {
					if (selected > 0) {
						Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
								Sound.ITEM_SPYGLASS_USE, 0.5f, 1f);
						tick--;
					}
				}
			}
			if (tick >= (tick_offset * 2)) {
				tick = 1;
				if (selected < getAll_context().context.size() - 1) {
					selected++;
				}
			} else if (tick <= 0) {
				tick = tick_offset*2-1;
				if (selected > 0) {
					selected--;
				}
			}
			if (last_yaw_value != yaw) {
				last_yaw_value = yaw;
			}
			// System.out.println(String.format("menu_tick at %s", tick));
			selection = getAll_context().context.get(selected);
			// System.out.printf("selection is: %s\n", selection);
			// NOTE: this next line should not be hard coded here
			if (selection != selection_last) {
				selection_last = getAll_context().context.get(selected);
			}
			if (selected != last_selection_value) {
				// System.out.println("the page flip sound should be playing");
				Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
						Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
			}
			last_selection_value = selected;
		}

		public void playerChoose(PlayerInteractEvent ev) {
			if (hasMenuOpen(player) > -1) {
				ev.setCancelled(true); // since the menu us open lets prevent breaking shit
			} else {
				if (ev.getPlayer().getInventory().getItemInMainHand() == null) {
					return;
				}
			}

			// if (ev.getPlayer().getInventory().getItemInMainHand() == null) {
			// return;
			// }
			// if (!ev.getHand().equals(EquipmentSlot.HAND)) {
			// return;
			// }

			Player player = ev.getPlayer();

			// if (player.getInventory().getItemInMainHand().isEmpty()) {
			// return;
			// }
			// if
			// (!player.getInventory().getItemInMainHand().getType().equals(Item_Manager.magic_mirror_book.getType()))
			// {
			// return;
			// }

			for (PlayerWithMenu p : player_with_menu) {
				if (!p.player.equals(player)) {
					continue;
				}
				if (p.selection == "NEXT PAGE") {
					p.page++;
					return; // this should not trigger a new page just update it so it should return here
				}
				p.selection = selection;
				p.getAll_context().answer.name = selection;
			}
		}
	}

	public void sendPrompt(int id, List<String> prompt_list, Player player, ItemStack item) {
		Integer index = hasMenuOpen(player);
		if (index == -1) {
			PlayerWithMenu new_player = new PlayerWithMenu();
			new_player.createPlayer(player);
			new_player.using_item = item;
			player_with_menu.add(new_player);
			// new_player.selection = prompt_list.get(0);
		}
		PlayerWithMenu has_menu_open = player_with_menu.get(hasMenuOpen(player));
		List<String> new_list = new ArrayList<>();
		for (int x = 0; x < prompt_list.size(); x++) {
			new_list.add(prompt_list.get(x));
		}

		PlayerContext new_context = new PlayerContext();
		new_context.id = id;
		new_context.context = new_list;
		has_menu_open.all_context.add(new_context);

		player.addPotionEffect(
				new PotionEffect(PotionEffectType.BLINDNESS, 1200, 1).withAmbient(false).withParticles(false));
		Audience audience = Audience.audience(player);
		audience.sendActionBar(
				() -> Component.text(centerTextBar(has_menu_open)));
		// title(audience, has_menu_open);
		// audience.showTitle(Title.title(Component.text(""),Component.text(centerTextBar(has_menu_open))));
	}

	public void title(Audience audience,PlayerWithMenu msg) {	
		audience.showTitle(Title.title(Component.text(""),Component.text(centerTextBar(msg)),Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(1), Duration.ofSeconds(1))));
	}

	public void playerSelection(PlayerMoveEvent ev) {
		Player player = ev.getPlayer();

		Integer index = hasMenuOpen(ev.getPlayer());
		if (index == -1) {
			return;
		}
		PlayerWithMenu has_menu_open = player_with_menu.get(index);

		has_menu_open.prompt_selections();

		Audience audience = Audience.audience(player);

		audience.sendActionBar(
				() -> Component.text(centerTextBar(has_menu_open)));
		// audience.showTitle(Title.title(Component.text(""),Component.text(centerTextBar(has_menu_open)),Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(0), Duration.ofSeconds(0))));
		// title(audience, has_menu_open);

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

	public String centerTextBar(PlayerWithMenu has_menu_open) {
		int bar_length = 60;
		if (has_menu_open.last_selection_value == -1) {
			int size = has_menu_open.getAll_context().context.size();
			has_menu_open.last_selection_value = Math.round(size / 2);
			// System.out.printf("last selection value: %s\n",
			// has_menu_open.last_selection_value);
		}
		String bar_text = formatListToString(has_menu_open,has_menu_open.getAll_context().context,
				has_menu_open.selection);
		String word = has_menu_open.selection;
		String blank_space = new String();
		for (int x = 0; x < (bar_length / 2); x++) {
			blank_space += " ";
		}
		// FIXME: there are times when the array will have multiples of the same
		// string.. and not propperly center.
		// one fix would be to prefix each option with a number..
		int word_index = bar_text.indexOf(word);
		bar_text = String.format("%s%s%s", blank_space, bar_text, blank_space);
		String final_text = bar_text.substring(word_index + (word.length() / 2),
				word_index + (word.length() / 2) + bar_length);

		String text_bar = String.format(ChatColor.DARK_GRAY + "< %s >", final_text);
		return text_bar;
	}

	public String formatListToString(PlayerWithMenu pWithMenu,List<String> list, String selection) {
		String action_list = new String();
		for (int x = 0; x < list.size(); x++) {
			if (list.get(x) == selection) {
				//NOTE: or maybe just have the dots change color
				// action_list += String.format(ChatColor.AQUA + "[ %s ]" + ChatColor.DARK_GRAY, selection);
				if (pWithMenu.tick == 1) {
					action_list += String.format(ChatColor.AQUA + "(([ %s ]...." + ChatColor.DARK_GRAY, selection);
				} else if (pWithMenu.tick == 2) {
					action_list += String.format(ChatColor.AQUA + "..([ %s ]...." + ChatColor.DARK_GRAY, selection);
				} else if (pWithMenu.tick == 3) {
					action_list += String.format(ChatColor.AQUA + "....[ %s ]...." + ChatColor.DARK_GRAY, selection);
				} else if (pWithMenu.tick == 4) {
					action_list += String.format(ChatColor.AQUA + "....[ %s ]).." + ChatColor.DARK_GRAY, selection);
				} else if (pWithMenu.tick == 5) {
					action_list += String.format(ChatColor.AQUA + "....[ %s ]))" + ChatColor.DARK_GRAY, selection);
				} else {
					action_list += String.format(ChatColor.AQUA + "[ %s ]" + ChatColor.DARK_GRAY, selection);
				}

				

				continue;
			}
			if (x == 0) {
				action_list += String.format("%s ", list.get(x));

			} else if (x == list.size()) {
				action_list += String.format(" %s", list.get(x));

			} else {
				action_list += String.format(" %s ", list.get(x));
			}
		}
		// return String.format(ChatColor.GRAY + action_list);
		return action_list;
	}
}
