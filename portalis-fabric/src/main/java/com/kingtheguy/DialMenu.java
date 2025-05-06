package com.kingtheguy;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class DialMenu {

  public static List<PlayerDial> players_with_dial = new ArrayList<>();

  public static class PlayerDial {
    PlayerEntity player;
    int selected = -1;
    int prev_selected;
    String hover;
    String last_hover;
    float inicial_yaw;
    float prev_yaw_value;

    // for a bit "buffering" between selections
    int dial_tick = 0;
    int dial_tick_offset = 3;

    String dial_id; // example [dial_name:answer]
    List<String> dial_options;
    String selection_answer;
    String custom_answer;

    public String getDialId() {
      return dial_id;
    }

    public void moveDialMenuSelection() {
      int yaw = (int) player.headYaw;
      boolean sneaking = player.isInSneakingPose();

      if (yaw <= -1) {
        yaw += 360;
      }
      inicial_yaw = yaw; // what am i even using init_yaw for?

      { // this should prevent the back & forth? jumping
        if (yaw >= 0 && yaw <= 9) {
          if (prev_yaw_value >= 100 && prev_yaw_value <= 360) {
            prev_yaw_value = yaw;
            return;
          }
        }
        if (yaw >= 100 && yaw <= 360) {
          if (prev_yaw_value >= 0 && prev_yaw_value <= 9) {
            prev_yaw_value = yaw;
            return;
          }
        }
      }

      //smoothens the scrollig
      if (yaw % 4 == 1) {
      return;
      }

      if (yaw > prev_yaw_value) {
        if (selected < dial_options.size() - 1) {
          if (!player.getWorld().isClient()) {
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_SPYGLASS_USE,
                SoundCategory.NEUTRAL, 1f, 1f);
          }
          if (sneaking == true) {
            selected++;
          } else {
            dial_tick++;
          }
        }
      } else if (yaw < prev_yaw_value) {
        if (selected > 0) {
          if (!player.getWorld().isClient()) {
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_SPYGLASS_USE,
                SoundCategory.NEUTRAL, 1f, 1f);
          }
          if (sneaking == true) {
            selected--;
          } else {
            dial_tick--;
          }
        }
      }
      if (sneaking == false) {
        if (dial_tick >= (dial_tick_offset * 2)) {
          dial_tick = 1;
          if (selected < dial_options.size() - 1) {
            selected++;
          }
        } else if (dial_tick <= 0) {
          dial_tick = dial_tick_offset * 2 - 1;
          if (selected > 0) {
            selected--;
          }
        }
      } else {
        dial_tick = dial_tick_offset;
      }
      if (prev_yaw_value != yaw) {
        prev_yaw_value = yaw;
      }
      // Play sound when new selection is made (this is not really the selection)
      if (selected != prev_selected) {
        prev_selected = selected;
        if (!player.getWorld().isClient()) {
          player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN,
              SoundCategory.NEUTRAL, 1f, 1f);
        }
      }
      // System.out.println(String.format("selecton: #%s [ %s ]", selected, dial_options.get(selected)));
    }

    public void displayDialMenu() {
      player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 500, 1));
      int bar_size = 55;
      if (selected <= -1) {
        if (dial_options != null) {
          selected = (dial_options.size() / 2) - 1;
        }
      }
      String word = dial_options.get(selected);
      if (dial_options.size() <= -1) {
        word = dial_options.get(dial_options.size() / 2);
      }
      String blank_space = new String();
      for (int x = 0; x < bar_size; x++) {
        blank_space += " ";
      }

      String text_bar = String.join(" ", dial_options);

      String prefix = dial_id; // kinda like a prefix to the dial_menu

      text_bar = String.format("%s%s%s", blank_space, text_bar, blank_space);
      int word_index = text_bar.indexOf(word);

      String first_half = new String();
      String second_half = new String();

      String[] bar_split = text_bar.split(word);

      String obscured_word = "";
      for (int x = 0; x <= word.length(); x++) {
        obscured_word = String.format("%s%s", obscured_word, "0");
      }
      int obscured_word_size = obscured_word.length();

      int start_index = bar_split[0].length() - bar_size / 2 - 1 + (obscured_word_size / 2);
      for (int x = 0; x <= bar_size / 2 - (obscured_word_size / 2); x++) {
        first_half = String.format("%s%s", first_half, text_bar.charAt(start_index));
        start_index++;
      }
      int add_this = 0;
      if (obscured_word_size % 2 == 0) {
        add_this++;
      }
      start_index = word_index + obscured_word_size;

      for (int x = 0; x <= bar_size / 2 - (obscured_word_size / 2) + add_this; x++) {
        second_half = String.format("%s%s", second_half, text_bar.charAt(start_index));
        start_index++;
      }

      if (dial_tick > dial_tick_offset) {
        String extra = new String();
        if (dial_tick == 4) {
          extra = String.format("%s%s", extra, " ");
          first_half = first_half.substring(1, first_half.length());
        } else {
          extra = String.format("%s%s", extra, "  ");
          first_half = first_half.substring(2, first_half.length());
        }
        second_half = String.format("%s%s", second_half, extra);
      }
      if (dial_tick < dial_tick_offset) {
        String extra = new String();
        if (dial_tick == 2) {
          extra = String.format("%s%s", extra, " ");
          second_half = second_half.substring(0, second_half.length() - 1);
        } else {
          extra = String.format("%s%s", extra, "  ");
          second_half = second_half.substring(0, second_half.length() - 2);
        }
        first_half = String.format("%s%s", extra, first_half);
      }

      String bracket_first = "";
      String bracket_second = "";
      if (dial_tick == 1) {
        bracket_first = "§3(([ ";
        bracket_second = " ]. .§7";
        word = String.format("§3(([ %s ]. .§7", word);
      } else if (dial_tick == 2) {
        bracket_first = "§3. ([ ";
        bracket_second = " ]. .§7";
        word = String.format("§3. ([ %s ]. .§7", word);
      } else if (dial_tick == 3) {
        bracket_first = "§3. .[ ";
        bracket_second = " ]. .§7";
        word = String.format("§3. .[ %s ]. .§7", word);
      } else if (dial_tick == 4) {
        bracket_first = "§3. .[ ";
        bracket_second = " ]) .§7";
        word = String.format("§3. .[ %s ]) .§7", word);
      } else if (dial_tick == 5) {
        bracket_first = "§3. .[ ";
        bracket_second = " ]))§7";
        word = String.format("§3. .[ %s ]))§7", word);
      }

      player.sendMessage(Text.of(String.format("§7< %s %s %s >", first_half, word, second_half)), true);
    }

    public void makeSelection() {
      selection_answer = dial_options.get(selected);
    }
    public String getAnswer() {
      return selection_answer;
    }
  }

  public static PlayerDial getPlayer(PlayerEntity player) {
    for (PlayerDial pd : players_with_dial) {
      if (pd.player.equals(player)) {
        return pd;
      }
    }
    return null;
  }

  public static void openDialMenu(PlayerEntity player, String dial_id, List<String> dial_options) {
    PlayerDial p_dial = getPlayer(player);
    if (p_dial == null) {
      p_dial = new PlayerDial();
      p_dial.player = player;
    }
    p_dial.dial_id = dial_id;
    p_dial.dial_options = dial_options;
    players_with_dial.add(p_dial);
    // player.sendMessage(Text.of("SUCK MY ASS"), true);
    DialMenu.refreshDialMenu(player);
  }

  public static void closeDialMenu(PlayerEntity player) {
    PlayerDial p_dial = getPlayer(player);
    if (p_dial == null) {
      return;
    }
    player.removeStatusEffect(StatusEffects.BLINDNESS);
    players_with_dial.remove(p_dial);
    player.sendMessage(Text.of(""), true);
  }

  public static void refreshDialMenu(PlayerEntity player) {
    PlayerDial p_dial = getPlayer(player);
    if (p_dial == null) {
      return;
    }
    p_dial.moveDialMenuSelection();
    p_dial.displayDialMenu();
  }

  public List<String> alphabet() {
    // FIXME: tihs just requires way too much scrolling, not goo
    return List.of(
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
        "w", "x", "y", "z",
        "-", "'", "#", "_",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
        "=DEL=",
        "=DONE=");
  }
}
