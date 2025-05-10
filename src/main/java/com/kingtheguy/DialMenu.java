package com.kingtheguy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatColor;

public class DialMenu implements Listener {

	ArrayList<PlayerDialMenu> player_with_menu = new ArrayList<>();
	List<Player> wait_list = new ArrayList<>();
	public static ChatColor selection_color = ChatColor.AQUA;

	public class DialContext {
		int id;
		List<String> selection_options;
		String answer = new String();
	}

	public class PlayerDialMenu {
		Player player;
		String hover;
		String hover_last;
		Integer page;
		float initial_yaw;
		int last_yaw_value;
		int last_selection_value = -1;

		int tick = 0;
		int tick_offset = 3;

		/**
		 * Deprecated should be using a simple list instead
		 */
		ArrayList<DialContext> all_context = new ArrayList<>();
		ItemStack using_item;
		List<String> dial_options;
		String dial_id;
		String selection_answer;
		String custom_answer = ""; // FIXME: something like this could also be used as a way to pass data around

		public DialContext getListOfContext() {
			return all_context.get(all_context.size() - 1);
		}

		public void createPlayer(Player the_player) {
			this.player = the_player;
			this.hover = "";
			this.hover_last = "";
			this.initial_yaw = the_player.getLocation().getYaw();
		}

		public void moveDialMenuSelection() {
			Location location = player.getLocation();
			int yaw = (int) player.getLocation().getYaw();

			if (yaw <= -1) {
				yaw += 361;
			}
			// System.out.println(String.format("player's yaw is: [ %s ]",yaw));
			// player.sendMessage(String.format("YAW: %s", yaw)); // prevents the selection
			// from jumping back when going from 3 digits to 1
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
			if (selected > dial_options.size() - 1) {
				selected = 0;
			}
			// FIXME: how do we make this feel smoother?
			// player's ping could play a roll with this.
			// ideally if it was possible run this client side.. it would be way smoother
			// if (yaw % 50 >= 8) {
			// if (yaw % 30 >= 8) {
			// if (yaw % 60 >= 10) {
			// if (yaw % 30 >= 15) { //NOTE: this one is pretty fine
			boolean sneaking = false;
			if (player.isSneaking()) {
				tick = tick_offset; //re-center
				sneaking = true;
			}
			if (yaw > last_yaw_value) {
				if (selected < dial_options.size() - 1) {
					Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
							Sound.ITEM_SPYGLASS_USE, 0.5f, 1f);
					if (sneaking == true) {
						selected++;
					} else {
						tick++;
					}
				}
			} else if (yaw < last_yaw_value) {
				if (selected > 0) {
					Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
							Sound.ITEM_SPYGLASS_USE, 0.5f, 1f);
					if (sneaking == true) {
						selected--;
					} else {
						tick--;
					}
				}
			}
			// }
			if (sneaking == false) {
				if (tick >= (tick_offset * 2)) {
					tick = 1;
					if (selected < dial_options.size() - 1) {
						selected++;
					}
				} else if (tick <= 0) {
					tick = tick_offset * 2 - 1;
					if (selected > 0) {
						selected--;
					}
				}
			}

			if (last_yaw_value != yaw) {
				last_yaw_value = yaw;
			}
			// System.out.println(String.format("menu_tick at %s", tick));
			if (selected <= -1) {
				// selection =
				// getListOfContext().selection_options.get(getListOfContext().selection_options.size()/2);
				// return;
			} else {
				hover = dial_options.get(selected);
			}
			// System.out.printf("selection is: %s\n", selection);
			// NOTE: this next line should not be hard coded here
			if (hover != hover_last) {
				if (selected <= -1) {
					hover_last = dial_options.get(dial_options.size() / 2);
				} else {
					hover_last = dial_options.get(selected);
				}
			}
			if (selected != last_selection_value) {
				Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
						Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
				// Bukkit.getWorld(location.getWorld().getUID()).playSound(location,
				// 		Sound.ITEM_INK_SAC_USE, 1f, 1f);
			}
			last_selection_value = selected;
		}

		// NOTE: will this be used?
		/**
		 * for when extra data is needed, this could also be done outside of the menu
		 * stuff
		 * example plugin_name:menu_name:data_as_string
		 * 
		 * @return
		 */
		public String getDataFromId() {
			String[] split = dial_id.split(":", -1);
			System.out.println(String.format("the id length is: %s", split.length));
			if (split.length <= 2) {
				return split[1];
			}
			return split[2];
		}

		// NOTE(to self): this is not an actualy event
		public void playerChooseSelection(PlayerInteractEvent ev) {
			if (getPlayer(player) > -1) {
				ev.setCancelled(true); // since the menu is open lets prevent breaking shit
			} else {
				if (ev.getPlayer().getInventory().getItemInMainHand() == null) {
					return;
				}
			}

			Player player = ev.getPlayer();

			for (PlayerDialMenu p : player_with_menu) {
				if (!p.player.equals(player)) {
					continue;
				}
				if (p.hover == "NEXT PAGE") {
					p.page++;
					return; // this should not trigger a new page just update it so it should return here
				}
				p.hover = hover;
				p.selection_answer = hover;
				// NOTE(NO): i should close this here right?
				// closeMenu(ev.getPlayer());
				// if (p.using_item != null) {
				// if (!ev.getPlayer().getInventory().getItemInMainHand().equals(p.using_item))
				// {
				// closeMenu(ev.getPlayer());
				// }
				// }
				// System.out.println("should be showing some text now.");
			}
			// sendPrompt(0, List.of("scroll left and right"), player,
			// Item_Manager.magic_mirror_book);
		}
	}

	public Integer getPlayer(Player player) {
		for (PlayerDialMenu p : player_with_menu) {
			if (p.player.equals(player)) {
				return player_with_menu.indexOf(p);
			}
		}
		return -1;
	}

	public PlayerDialMenu getPlayerDialMenu(Player player) {
		Integer index = getPlayer(player);
		if (index > -1) {
			return player_with_menu.get(index);
		}
		return null;
	}

	public void closeMenu(Player player) {
		Integer index = getPlayer(player);
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

	// FIXME: remove tha all_context part of this
	public void removeLastDialMenu(Player player) {
		Integer index = getPlayer(player);
		if (index <= -1) {
			return;
		}
		PlayerDialMenu has_menu_open = player_with_menu.get(getPlayer(player));
		has_menu_open.all_context.remove(has_menu_open.all_context.size() - 1);
		sendDialActionBar(player, has_menu_open, false);
		refreshDialMenu(player);
	}

	/**
	 * @param id     :exmple server_name:menu_title or mod_name:menu_title
	 * @param so     :list of options
	 * @param player :the plyer
	 * @param uit    :item to check for or null
	 */
	public void openDialMenu(String id, List<String> so, Player player, ItemStack uit) {
		Integer index = getPlayer(player);
		if (index == -1) {
			PlayerDialMenu new_player = new PlayerDialMenu();
			new_player.createPlayer(player);
			player_with_menu.add(new_player);
		}
		PlayerDialMenu has_menu_open = player_with_menu.get(getPlayer(player));
		List<String> new_list = new ArrayList<>();
		for (int x = 0; x < so.size(); x++) {
			new_list.add(so.get(x));
		}

		has_menu_open.dial_id = id;
		has_menu_open.dial_options = so;
		has_menu_open.using_item = uit;

		player.addPotionEffect(
				new PotionEffect(PotionEffectType.BLINDNESS, 1200, 1).withAmbient(false).withParticles(false));
		// FIXME: new to change these to other methods to work with the simpler list
		has_menu_open.moveDialMenuSelection();
		refreshDialMenu(player);
	}

	public void refreshDialMenu(Player player) {
		Integer index = getPlayer(player);
		if (index == -1) {
			return;
		}
		PlayerDialMenu has_menu_open = player_with_menu.get(index);

		has_menu_open.moveDialMenuSelection();
		sendDialActionBar(player, has_menu_open, false);

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

	public void sendDialActionBar(Player player, PlayerDialMenu p_with_m, boolean as_title) {
		Audience audience = Audience.audience(player);
		if (as_title) {
			audience.showTitle(Title.title(Component.text(""), Component.text(centerTextBar(p_with_m)),
					Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(1), Duration.ofSeconds(1))));
			return;
		}
		audience.sendActionBar(
				() -> Component.text(centerTextBar(p_with_m)));
	}

	public String centerTextBar(PlayerDialMenu has_menu_open) {
		int bar_size = 55;
		// int bar_size = 25;
		String word = has_menu_open.hover;
		if (word == "") {
			//FIXME: something is wrong here
			word = has_menu_open.dial_options.get(has_menu_open.dial_options.size() / 2);
		}
		int word_size = word.length();

		String blank_space = new String();
		for (int x = 0; x < bar_size; x++) {
			blank_space += " ";
		}

		// String text_bar = formatListToString(has_menu_open,
		// has_menu_open.getAll_context().context,
		// has_menu_open.selection);
		// String text_bar =
		// String.format("%s",has_menu_open.getAll_context().context.toString());
		String text_bar = String.join("  ", has_menu_open.dial_options);
		// String context = has_menu_open.getAll_context().context.get(cnt.size()-1);
		String context = "MAIN";
		int cnt_size = has_menu_open.all_context.size();
		if (cnt_size > 1) {
			context = has_menu_open.all_context.get(cnt_size - 2).answer;
		}
		// if (has_menu_open.all_context.get(has_menu_open.all_context.size() - 2) !=
		// null) {
		// }
		if (has_menu_open.last_selection_value == -1) {
			int size = has_menu_open.dial_options.size();
			has_menu_open.last_selection_value = Math.round(size / 2);
			// System.out.printf("last selection value: %s\n",
			// has_menu_open.last_selection_value);
		}

		text_bar = String.format("%s%s%s", blank_space, text_bar, blank_space);
		// if (text_bar.length() <= 0) {
		// return String.format(ChatColor.GRAY + "< %s >",
		// colorText(has_menu_open.tick, "Scroll LEFT & RIGHT"));
		// }
		int word_index = text_bar.indexOf(word);
		// if (word_index <= 0) {
		// return String.format(ChatColor.GRAY + "< %s >",
		// colorText(has_menu_open.tick, "Scroll LEFT & RIGHT"));
		// }

		// String final_text = text_bar.substring(word_index + (word.length() / 2),
		// word_index + (word.length() / 2) + bar_length);

		String first = new String();
		String last = new String();

		String[] bar_split = text_bar.split(word);

		int start_index = bar_split[0].length() - bar_size / 2 - 1 + (word_size / 2);
		for (int x = 0; x <= bar_size / 2 - (word_size / 2); x++) {
			first = String.format("%s%s", first, text_bar.charAt(start_index));
			start_index++;
		}
		int add_this = 0;
		if (word_size % 2 == 0) {
			add_this++;
		}
		start_index = word_index + word_size;
		for (int x = 0; x <= bar_size / 2 - (word_size / 2) + add_this; x++) {
			last = String.format("%s%s", last, text_bar.charAt(start_index));
			start_index++;
		}

		if (has_menu_open.tick > has_menu_open.tick_offset) {
			String extra = new String();
			if (has_menu_open.tick == 4) {
				extra = String.format("%s%s", extra, " ");
				first = first.substring(1, first.length());
			} else {
				extra = String.format("%s%s", extra, "  ");
				first = first.substring(2, first.length());
			}
			last = String.format("%s%s", last, extra);
		}
		if (has_menu_open.tick < has_menu_open.tick_offset) {
			String extra = new String();
			if (has_menu_open.tick == 2) {
				extra = String.format("%s%s", extra, " ");
				last = last.substring(0, last.length() - 1);
			} else {
				extra = String.format("%s%s", extra, "  ");
				last = last.substring(0, last.length() - 2);
			}
			first = String.format("%s%s", extra, first);
		}

		if (has_menu_open.custom_answer != "") {
			return String.format(ChatColor.GRAY + "%s : < %s%s%s >", has_menu_open.custom_answer, first,
					colorText(has_menu_open.tick, word),
					last);
		}
		return String.format(ChatColor.GRAY + "< %s%s%s >", first, colorText(has_menu_open.tick, word),
				last);
	}

	public String colorText(int tick, String text) {
		// if (tick == 1) {
		// return String.format(selection_color + "(([ %s ]. ." +
		// ChatColor.GRAY, text);
		// } else if (tick == 2) {
		// return String.format(selection_color + ". .[ %s ]. ." +
		// ChatColor.GRAY, text);
		// } else if (tick == 3) {
		// return String.format(selection_color + ". .[ %s ]))" +
		// ChatColor.GRAY, text);
		// }
		boolean skip_around = false;
		if (skip_around) {
			return String.format(selection_color + ". .[ %s ]. ." +
					ChatColor.GRAY, text);
		} else if (tick == 1) {
			return String.format(selection_color + "(([ %s ]. ." +
					ChatColor.GRAY, text);
		} else if (tick == 2) {
			return String.format(selection_color + ". ([ %s ]. ." +
					ChatColor.GRAY, text);
		} else if (tick == 3) {
			return String.format(selection_color + ". .[ %s ]. ." +
					ChatColor.GRAY, text);
		} else if (tick == 4) {
			return String.format(selection_color + ". .[ %s ]) ." +
					ChatColor.GRAY, text);
		} else if (tick == 5) {
			return String.format(selection_color + ". .[ %s ]))" +
					ChatColor.GRAY, text);
		}

		return String.format(selection_color + "%s" + ChatColor.GRAY, text);
	}

	public List<String> alphabet() {
		// FIXME: tihs just requires way too much scrolling, not good.
		return List.of(
				"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
				"w", "x", "y", "z",
				"-", "'", "#", "_",
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
				"=DEL=",
				"=DONE=");
	}
}
