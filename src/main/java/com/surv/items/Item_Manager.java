package com.surv.items;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

	}
}
