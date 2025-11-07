package com.kingtheguy;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Config {

  public static Settings default_settings = new Settings(TextColor.fromHexString(NamedTextColor.AQUA.asHexString()), false);

  public static class Settings {
    TextColor text_color;
    boolean infinite_uses;

    public Settings() {
      this.text_color = NamedTextColor.AQUA;
      this.infinite_uses = false;
    }

    public Settings(TextColor text_color, Boolean infinite_uses) {
      this.text_color = text_color;
      this.infinite_uses = infinite_uses;
    }

  }

  public static void loadConfig() {
    try {
      Gson gson = new Gson();
      JsonReader reader = new JsonReader(new FileReader(String.format("plugins/%s/config.json", portalis.PLUGIN_NAME)));
      // Type listOfMyClassObject = new TypeToken<Settings>() {
      // }.getType();

      // gson.fromJson(reader, listOfMyClassObject);
      default_settings = gson.fromJson(reader, Settings.class);
      reader.close();
    } catch (IOException e) {
      System.out.println(e);
    }

  }

  public static void saveConfig() {
    System.out.println(String.format("here we have this stuff, uses: %s", default_settings.infinite_uses));
    System.out.println(String.format("here we have this stuff, color: %s", default_settings.text_color.toString()));
    try {
      BufferedWriter writer = new BufferedWriter(
          new FileWriter(String.format("plugins/%s/config.json", portalis.PLUGIN_NAME)));
      Gson gson = new Gson();
      String json = gson.toJson(default_settings, Settings.class);

      writer.write(json);
      writer.close();
    } catch (IOException e) {
      System.out.println(e);
    }
  }
}
