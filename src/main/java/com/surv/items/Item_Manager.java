package com.surv.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BrewingStand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionType;

import com.surv.magic;

import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.text.Component;

public class Item_Manager {

	public static ItemStack mm;

	public static void init() {
		create_book();

	}

	private static void create_book() {
		ItemStack item = new ItemStack(Material.BOOK, 1);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(String.format("Magic Mirror")));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("This is used to"));
		lore.add(Component.text("teleport from place"));
		lore.add(Component.text("to place"));
		meta.lore(lore);
		meta.addEnchant(Enchantment.LUCK, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		mm = item;

		NamespacedKey key = new NamespacedKey(magic.getPlugin(), "custom_potion_mix");
		Material result = Material.WATER;
		PotionType base_potion = PotionType.MUNDANE;
		Material ingredient = Material.ENDER_PEARL;
		PotionMix custom_potion_mix = new PotionMix(key,result,base_potion,ingredient);
	}
}
