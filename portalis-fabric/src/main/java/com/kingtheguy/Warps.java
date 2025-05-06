package com.kingtheguy;

import java.util.ArrayList;
import java.util.List;

import com.kingtheguy.data.Data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class Warps {
  String global_warps_file = String.format("mods/%s/global_warps.json", Portalisfabric.MOD_ID);
  String player_warps_file = String.format("mods/%s/player_warps.json", Portalisfabric.MOD_ID);

  public static List<GlobalWarps> global_warps = new ArrayList<>();
  public static List<PlayerWarps> player_warps = new ArrayList<>();

	public class GlobalWarps {
		String creator;
		Vector3 location = new Vector3();
		String dimension_name;
		String name;

		@Override
		public String toString() {
			return String.format("[warp] <%s>, <%s>, <%s>\n", location, dimension_name, name);
		}
	}

	public class PlayerWarps {
		String player_name;
		List<GlobalWarps> known_warps = new ArrayList<>();
	}

	class Vector3 {
		int X;
		int Y;
		int Z;

		@Override
		public String toString() {
			return String.format("%s,%s,%s", X, Y, Z);
		}
	}


public void addPlayerWarp(BlockPos pos,PlayerEntity player) {
		//NOTE: i will have to update the older data to this new format
		//FIXME: i am pretty sure this is broken to hell
    GlobalWarps warp_to_add = new GlobalWarps();
    warp_to_add.location = new Vector3();
    warp_to_add.location.X = pos.getX();
    warp_to_add.location.Y = pos.getY();
    warp_to_add.location.Z = pos.getZ();
		warp_to_add.dimension_name =  player.getWorld().toString();
    // check if the warp has been created
    String found_player = null; // check if player had a list
    for (PlayerWarps pw : player_warps) {
      if (pw.player_name.equals(found_player)) {
        found_player = pw.player_name;
      } else {
      }
    }
    for (GlobalWarps w : global_warps) {
      if (w.location.toString().equals(warp_to_add.location.toString())) {
        if (w.dimension_name.equals(warp_to_add.dimension_name)) {

          // check if player is in warp add them
          if (found_player == null) {
            // System.out.println("did not find the player, creating new list..");
            PlayerWarps new_player_warp = new PlayerWarps();
            found_player = player.getName().toString();
            new_player_warp.player_name = found_player;
            player_warps.add(new_player_warp);
          }
          for (PlayerWarps pw : player_warps) {
            if (pw.player_name.equals(found_player)) {
              // System.out.println("player is in fact found");
              for (GlobalWarps pww : pw.known_warps) {
                if (w.location.toString().equals(pww.location.toString())) {
                  if (w.dimension_name.equals(pww.dimension_name)) {
                    // System.out.println("the player is already aware of this warp.");
                    return;
                  }
                }
              }
              pw.known_warps.add(w);
              Data.savePlayerWarpsToFile(player_warps_file);
              break;
            }
          }
          return;
        }
      }
    }
    // new_player_warp.known_warps

    // global_warps.add(warp_to_add);
    // saveGlobalWarpsToFile(global_warps_file);
    // System.out.println("new location saved");
  }	
}
