package com.kingtheguy.data;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.kingtheguy.Warps;
import com.kingtheguy.Warps.GlobalWarps;

public class Data {
  public static void saveGlobalWarpsToFile(String file) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      Gson gson = new Gson();
      String json = gson.toJson(Warps.global_warps);
      writer.write(json);
      writer.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public static void savePlayerWarpsToFile(String file) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      Gson gson = new Gson();
      String json = gson.toJson(Warps.player_warps);
      writer.write(json);
      writer.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  public void loadGlobalWarpsFromFile(String file) {
    try {
      Gson gson = new Gson();
      JsonReader reader = new JsonReader(new FileReader(file));
      // BufferedReader reader = new BufferedReader(new FileReader(file));
      // String line;
      Type listOfMyClassObject = new TypeToken<List<GlobalWarps>>() {
      }.getType();

      List<GlobalWarps> warps = gson.fromJson(reader, listOfMyClassObject);
      for (GlobalWarps w : warps) {
        Warps.global_warps.add(w);
      }
      reader.close();
    } catch (IOException e) {
      System.out.println(e);
    }

  }

  public void loadPlayerWarpsFromFile(String file) {
    try {
      Gson gson = new Gson();
      JsonReader reader = new JsonReader(new FileReader(file));
      // player_warps = gson.fromJson(reader, PlayerWarps.class);
      Type listOfMyClassObject = new TypeToken<List<Warps.PlayerWarps>>() {
      }.getType();
      List<Warps.PlayerWarps> warps = gson.fromJson(reader, listOfMyClassObject);
      for (Warps.PlayerWarps w : warps) {
        Warps.player_warps.add(w);
      }
      reader.close();
      // savePlayerWarpsToFile(player_warps_file); // TODO: remove this line
      // System.out.printf("player warp spots: %s\n", player_warps.toString());
    } catch (IOException e) {
      System.out.println(e);
    }

  }

}
