package com.surv;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

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
			player.clearTitle();
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
			// addContext(new_player);
			index = getPlayer(player);
			// return;
		}
		PlayerWithMenu has_menu_open = player_with_menu.get(index);
        if (has_menu_open.menu_options == promp_list) {
            return;         
        }
        has_menu_open.selection = promp_list.get(0);
		Audience audience = Audience.audience(player);
		audience.sendActionBar(() -> Component.text(has_menu_open.selection));
        has_menu_open.menu_options = promp_list;
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
	/**
	 * show the player a prompt with a list of options to choose from
	 * 
	 * @param options a list of strings for the player to choose from
	 * @param player  the player
	 */
	private void prompt_selections(Player player) {
		// NOTE: this should not need to have player passed in
		Integer index = getPlayer(player);
		PlayerWithMenu has_menu_open = player_with_menu.get(index);
        if (index == -1) {
            return;
        }

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
		if (has_menu_open.getMenuOptions().size() > 5) {
			// for a dynamic list
			var chunk = has_menu_open.page * 4;
			if (selected == 4) {
                has_menu_open.selection = "NEXT PAGE";
				return;
			}
			selected = (chunk - 4) + selected;
		}
		if (selected > has_menu_open.getMenuOptions().size() - 1) {
            has_menu_open.selection = "CANCEL";
			return;
		}
        has_menu_open.selection = has_menu_open.getMenuOptions().get(selected);

		//NOTE: this next line should not be hard coded here
		if (has_menu_open.selection != has_menu_open.selection_last) {
			has_menu_open.selection_last = has_menu_open.getMenuOptions().get(selected);
			System.out.print("the page flip sound should be playing");

      Location location = player.getLocation();
      Bukkit.getWorld(location.getWorld().getUID()).playSound(location,Sound.ITEM_BOOK_PAGE_TURN,1f,1f);
      // player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
		}
	}

	public void playerSelection(PlayerMoveEvent ev) {
		Player player = ev.getPlayer();

        Integer index = getPlayer(ev.getPlayer());
        if (index == -1) {
            return;
        }
		prompt_selections(player);
		PlayerWithMenu has_menu_open = player_with_menu.get(index);

		// using ACTIONBAR
		Audience audience = Audience.audience(player);
		audience.sendActionBar(() -> Component.text(has_menu_open.selection));

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

	public void playerChoose(PlayerToggleSneakEvent ev) {
		// System.out.println("sneaking>> YESSSSS");
		if (ev.isSneaking() == false) {
			return;
		}
		Player player = ev.getPlayer();
        Integer index = getPlayer(player);
        if (index == -1) {
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

        if (has_menu_open.selection == "CANCEL") {
			closeMenu(player);
			return; // same reason as above
        }
	}
	public void addContext(PlayerWithMenu player) {
        PlayerContext new_context = new PlayerContext();
        new_context.prompt = player.selection;
        if (player.context.size() >= 1) {
            player.context.get(player.context.size()-1).answer = player.selection;           
			if (player.context.get(player.context.size()-1).prompt == new_context.prompt) {
				//we do not want copies
				return;
			}
        }
        player.context.add(new_context);
	}
}
