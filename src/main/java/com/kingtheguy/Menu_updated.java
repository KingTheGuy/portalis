package com.kingtheguy;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.destroystokyo.paper.exception.ServerInternalException;
import com.destroystokyo.paper.profile.PlayerProfile;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatColor;

// GET BOOK/ GIVE BOOK.. hit enderman with a book.

//[NEXT UPDATE](way better menu)
//the next update will switch away from using player head rotation for go through prompt options
//and instead will use entities(armor stands) with nametags of the prompt options 
//right in front of the player
//still use the playermove event and check if the player is looking at the options(this could just be used to update the color of the prompt so the player can see what they have chosen)..
//can replace having to crounch to choose and instead have the play punch or right click to select it

//[TRASH]
//TODO: this may be the chance to add particle markers showing where to aim for menu option
//FIXME: timer may not be needed.. just using the built in effect for a timer
//TODO(the TODOs below answer this): figure out how to do sub menus.. by by just recalling a menu with new options

//[DOING]
//TODO: after selecting a prompt reposition the players head/view back to eye level
//TODO(DONE?): get rid of player_selection and use PlayerSelections instead
//TODO(DONE?): also make all the changes needed for PlayerSelections to apply correctly

public class Menu_updated implements org.bukkit.event.Listener {
	// public ArrayList<player_selection> has_menu_open = new ArrayList<>();

	public ArrayList<PlayerSelections> using_menu = new ArrayList<>();

	/**
	 * gets the player index
	 * 
	 * @param player is the player
	 * @return the index in the array
	 * @see getPlayerFromIndex
	 */
	public int getPlayerIndex(Player player) {
		if (using_menu.size() > 0) {
			int index = using_menu.indexOf(using_menu.stream().filter(ps -> ps.getPlayer() == player).findFirst().get());
			if (index != -1) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * note: make sure getPlayerIndex() is not -1
	 * 
	 * @param int index from getPlayerIndex()
	 * @return {@link PlayerSelections}
	 */
	public PlayerSelections getPlayerFromIndex(int index) {
		return using_menu.get(index);
	}

	public class PlayerSelections {
		private Player player;
		private String selected_text;
		private int page = 1;
		private ArrayList<Trails> trailing_prompt;
		private ArrayList<Context> prompt_context;

		public class Context {
			String context;
			String opt_value = null;

			public void setContext(String string) {
				this.context = string;
			}

			// NOTE: this may need be for setting the last optional
			public void setContextOpt(String string) {
				this.opt_value = string;
			}

			public String getContext() {
				if (this.equals(null)) {
					return null;
				}
				if (this.context == null) {
					return null;
				}
				return this.context;
			}

			public String getContextOpt() {
				if (this.equals(null)) {
					return null;
				}
				if (this.opt_value == null) {
					return null;

				}
				return this.opt_value;
			}

		}

		public int getContextSize() {
			return prompt_context.size();
		}

		public Context getFirstContext() {
			return prompt_context.get(0);
		}

		/**
		 ** @param index returns the context_name at the index
		 **/
		// public Context getContext(int index) {
		// return prompt_context.get(index);
		// }

		public Context getLastContext() {
			if (prompt_context.size() > 1) {
				return prompt_context.get(prompt_context.size() - 1);
			}
			return null;
		}

		public int getLastIndex() {
			if (prompt_context.size() == 0) {
				return -1;
			}
			return prompt_context.indexOf(this.getLastContext());
		}

		public void addNewContext(String context_name) {
			if (context_name == null) {
				// lets prevent nulls
				return;
			}
			if (this.getContextSize() > 1) {
				System.out.println("we are greater than one so lets go ahead and do the next check");
				System.out.println(String.format("last: %s, new: %s", this.getLastContext().getContext(), context_name));
				if (this.getLastContext().getContext() == context_name) {
					System.out.println("look like the context is the same as last");
					// lets not repeat things
					return;
				}
			}
			Context newContext = new Context();
			// since this is the first prompt we have no context.. so lets set it to main.
			// if (this.prompt_context.size() < 1) {
			// newContext.setContext("MAIN");
			// this.prompt_context.add(newContext);
			// return;
			// }
			// lets set the new context
			newContext.setContext(context_name);
			// so now the previus context does not have the opt_context..
			// so lets set it the the player's currently selected_text
			if (this.getContextSize() > 1) {
				this.getLastContext().setContextOpt(this.selected_text);
			}
			// now lets make sure we add the the next context to the array
			this.prompt_context.add(newContext);
		}

		private List<String> prompt_options; // do i need this?

		public Player getPlayer() {
			return player;
		}

		public void nextPage() {
			page++;
		}

		public void prevPage() {
			page--;
		}

		public void setPromptOptions(List<String> options) {
			prompt_options = options;
		}

		public List<String> getPromptOptions() {
			return prompt_options;
		}

		public void createPlayer(Player the_player) {
			this.player = the_player;
			// this.trailing_prompt = new ArrayList<Trails>();
			this.prompt_context = new ArrayList<Context>();
			this.selected_text = "";

		}

		public int getPage() {
			return page;
		}

		public void setPage(int number) {
			page = number;
		}

		public void setSelectedText(String text) {
			selected_text = text;
		}

		public String getSelectedText() {
			return selected_text;
		}

		public class Trails {
			private String prompt_name;
			private String prompt_selection;

			public String getPromptName() {
				return prompt_name;
			}

			public String getPromptSelection() {
				return prompt_selection;
			}
		}

		// FIXME: this getLastTrail just is not going to work.. find something else to
		// do with this
		/**
		 * get the last prompt results
		 */
		public Trails getLastTrail() {
			if (this.trailing_prompt.size() < 1) {
				this.addTrail("MAIN", "MAIN");
			}
			return this.trailing_prompt.get(trailing_prompt.size() - 1);
		}

		public int getTrailSize() {
			return this.trailing_prompt.size();

		}

		/**
		 * gets any tail specified
		 * 
		 * @param index the index of the tail
		 */
		public Trails getTrail(int index) {
			return trailing_prompt.get(index);
		}

		/**
		 * adds the prompt name and selection
		 * 
		 * @param prompt_name      the name of the prompt
		 * @param prompt_selection the player's selection
		 */
		public void addTrail(String prompt_name, String prompt_selection) {
			Trails newTrail = new Trails();
			newTrail.prompt_name = prompt_name;
			newTrail.prompt_selection = prompt_selection;
			trailing_prompt.add(newTrail);
		}

	}

	// public class player_selection {
	// private Player the_player;
	// private String player_selected_text = null;
	// private String previeus_selected = null;
	// private String confirmed_text = null;
	// private List<String> prompt_options;
	// private int sub_menu_page = 1;

	// public String getConfirmedText() {
	// return this.confirmed_text;
	// }

	// public void createPlayer(Player player) {
	// this.the_player = player;
	// }

	// public String getSelectedText() {
	// return this.player_selected_text;
	// }

	// public void setSelectedText(String text) {
	// this.player_selected_text = text;
	// }

	// public String getPrevieusSelected() {
	// return this.previeus_selected;
	// }

	// public void setPrevieusSelected(String text) {
	// this.previeus_selected = text;
	// }

	// }

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
	/**
	 * show the player a prompt with a list of options to choose from
	 * 
	 * @param options a list of strings for the player to choose from
	 * @param player  the player
	 */
	private void prompt_selections(Player player) {
		// NOTE: this should not need to have player passed in
		PlayerSelections player_selection = getPlayerFromIndex(getPlayerIndex(player));

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
		if (player_selection.getPromptOptions().size() > 5) {
			// for a dynamic list
			var chunk = player_selection.getPage() * 4;
			if (selected == 4) {
				player_selection.setSelectedText("NEXT PAGE");
				return;
			}
			selected = (chunk - 4) + selected;
		}
		if (selected > player_selection.getPromptOptions().size() - 1) {
			player_selection.setSelectedText("CLOSE");
			return;
		}
		// if (selected == 3 && has_menu_open.get(index).getSelectedText() == "-") {
		// if (selected == 4) {
		// has_menu_open.get(index).setSelectedText("CLOSE");
		// closeMenu(player);
		// return;
		// }
		// }
		player_selection.setSelectedText(player_selection.getPromptOptions().get(selected));
	}

	public void closeMenu(Player player) {
		int index = getPlayerIndex(player);
		if (index != -1) {
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.clearTitle();
			using_menu.remove(index);
		}
	}

	// public int getPlayer(Player player) {
	// if (has_menu_open.size() > 0) {
	// int index = has_menu_open.indexOf(has_menu_open.stream().filter(p ->
	// p.the_player == player).findFirst().get());
	// if (index != -1) {
	// return index;
	// }
	// }
	// return -1;
	// }

	// effect and auto close timer
	public void openMenu(List<String> promp_list, Player player) {
		player.addPotionEffect(
				new PotionEffect(PotionEffectType.BLINDNESS, 600, 1).withAmbient(false).withParticles(false));
		int index = getPlayerIndex(player);
		if (index == -1) {
			PlayerSelections new_player = new PlayerSelections();
			new_player.createPlayer(player);
			using_menu.add(new_player);
			return;
		}
		if (getPlayerFromIndex(index).getPromptOptions() == promp_list) {
			// this prevents the same menu from opening
			return;
		}
		using_menu.get(index).setSelectedText(null);
		using_menu.get(index).setPromptOptions(promp_list);
	}

	@EventHandler
	public void playerSelection(PlayerMoveEvent ev) {
		// System.out.println("this should be working then");
		Player player = ev.getPlayer();

		int index = getPlayerIndex(player);
		if (index == -1) {
			return;
		}
		// System.out.println(String.format("should be on page: %s",
		// has_menu_open.get(index).sub_menu_page));
		prompt_selections(player);

		// using TITLE
		// Audience.audience(player)
		// .showTitle(Title.title(Component.text(""),
		// Component.text(using_menu.get(index).getSelectedText())));

		// using ACTIONBAR
		Audience audience = Audience.audience(player);
		audience.sendActionBar(
				() -> Component.text(using_menu.get(index).getSelectedText()));

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

	// NOTE: ok so this may not actually be needed.. but possibly i can somehow
	// tie all this tegether so i has les over head trying to remember to.. FUCK ME,
	// what am i talking about
	@EventHandler
	public void playerChoose(PlayerToggleSneakEvent ev) {
		// System.out.println("sneaking>> YESSSSS");
		if (ev.isSneaking() == false) {
			return;
		}
		Player player = ev.getPlayer();
		int index = getPlayerIndex(player);
		if (index == -1) {
			return;
		}
		PlayerSelections player_selection = getPlayerFromIndex(index);

		if (player_selection.getSelectedText() == "NEXT PAGE") {
			player_selection.nextPage();
			return; // this should not trigger a new page just update it so it should return here
		}

		player_selection.addNewContext(player_selection.getSelectedText());
		// player_selection.addTrail(last_prompt_selection, fix_null);
		if (player_selection.getSelectedText() == "CLOSE") {
			closeMenu(player);
			return; // same reason as above
		}
	}
}
