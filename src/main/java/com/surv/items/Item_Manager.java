package com.surv.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BrewingStand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.sun.source.tree.LambdaExpressionTree.BodyKind;
import com.surv.magic;

import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.hover.content.Item;

public class Item_Manager {

	public static ItemStack magic_mirror_book;
	public static ItemStack ender_oil;
	public static ItemStack infused_paper;

	public static void init() {
		createMagicMirror();
		createEnderOil();
		createInfusedPaper();
		createMagicMirrorRecipe();
		refillMagicMirrorRecipe();
	}
	private static ItemStack getMundanePotion() {
	    ItemStack mundane_potion = new ItemStack(Material.POTION);
	    PotionMeta mundane_potion_meta = (PotionMeta) mundane_potion.getItemMeta();
	    mundane_potion_meta.setBasePotionData(new PotionData(PotionType.MUNDANE));
	    mundane_potion.setItemMeta(mundane_potion_meta);

    	return mundane_potion;
  	}

	private static void createMagicMirror() {
		//Magic Mirror
		ItemStack item = new ItemStack(Material.BOOK, 1);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(String.format("Magic Mirror")));
        Integer max_uses = 6;
		NamespacedKey key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
		meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Warps it's users"));
		lore.add(Component.text(String.format("%s/%s uses",0,max_uses)));
		meta.lore(lore);
		meta.addEnchant(Enchantment.LUCK, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		magic_mirror_book = item;
		
	}

	private static void createMagicMirrorRecipe() {
		//leather
		{
			NamespacedKey key = new NamespacedKey(magic.getPlugin(), "custom_magic_mirror_recipe_LEATHER");
			ShapedRecipe recipe = new ShapedRecipe(key, magic_mirror_book);
			recipe.shape("XXX","ASD","XXX");
			recipe.setIngredient('X', Material.LEATHER);
			recipe.setIngredient('A', Material.GOLD_INGOT);
			recipe.setIngredient('S', Material.ENDER_EYE);
			recipe.setIngredient('D', infused_paper);
			Bukkit.addRecipe(recipe);
		}

		//rabbit hide
		{		
			NamespacedKey key = new NamespacedKey(magic.getPlugin(), "custom_magic_mirror_recipe_HIDE");
			ShapedRecipe recipe = new ShapedRecipe(key, magic_mirror_book);
			recipe.shape("XXX","ASD","XXX");
			recipe.setIngredient('X', Material.RABBIT_HIDE);
			recipe.setIngredient('A', Material.GOLD_INGOT);
			recipe.setIngredient('S', Material.ENDER_EYE);
			recipe.setIngredient('D', infused_paper);
			Bukkit.addRecipe(recipe);
		}
	}

	private static void refillMagicMirrorRecipe() {
		ItemStack repaired_book = new ItemStack(magic_mirror_book);
		ItemMeta meta = repaired_book.getItemMeta();
		NamespacedKey m_key = new NamespacedKey(magic.getPlugin(), "magic_mirror_use_data");
		meta.getPersistentDataContainer().set(m_key, PersistentDataType.INTEGER, 6);
        Integer max_uses = 6;
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Warps it's users"));
		lore.add(Component.text(String.format("%s/%s uses",max_uses,max_uses)));
		meta.lore(lore);
		repaired_book.setItemMeta(meta);
		NamespacedKey key = new NamespacedKey(magic.getPlugin(), "custom_magic_mirror_recipe_REFILL");
		ShapelessRecipe recipe = new ShapelessRecipe(key, repaired_book);
		recipe.addIngredient(1, magic_mirror_book);
		recipe.addIngredient(6, infused_paper);
		Bukkit.addRecipe(recipe);
	}

	private static void createEnderOil() {
		//Ender Oil
		ItemStack potion = new ItemStack(Material.POTION, 1);
		PotionMeta potion_meta = (PotionMeta) potion.getItemMeta();
		PotionData potion_data = new PotionData(PotionType.MUNDANE, false, false);
		potion_meta.displayName(Component.text(String.format("Ender Oil")));
		potion_meta.addEnchant(Enchantment.LUCK, 1, false);
		potion_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		potion_meta.setBasePotionData(potion_data);
		potion.setItemMeta(potion_meta);
		ender_oil = potion;

		//Ender Oil recipe
		NamespacedKey key = new NamespacedKey(magic.getPlugin(), "custom_potion_mix");
		ItemStack result = potion; //result
		RecipeChoice input = new RecipeChoice.ExactChoice(getMundanePotion()); //bottom three slots
		RecipeChoice ingredient = new RecipeChoice.MaterialChoice(Material.ENDER_PEARL); //top slot
		PotionMix custom_potion_mix = new PotionMix(key,result,input,ingredient);
		PotionBrewer brewer = Bukkit.getServer().getPotionBrewer();
		brewer.addPotionMix(custom_potion_mix);

	}

	private static void createInfusedPaper() {
		//Infused paper
		ItemStack item = new ItemStack(Material.PAPER,1);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(String.format("Infused Paper")));
		meta.addEnchant(Enchantment.LUCK, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		infused_paper = item;

		//Infused paper recipe
		NamespacedKey key = new NamespacedKey(magic.getPlugin(), "custom_infused_paper_recipe");
		ItemStack result = new ItemStack(infused_paper);
		result.setAmount(8);
		ShapedRecipe recipe = new ShapedRecipe(key, result);
		recipe.shape("XXX","XYX","XXX");
		recipe.setIngredient('X', Material.PAPER);
		recipe.setIngredient('Y', ender_oil);
		Bukkit.addRecipe(recipe);



	}
}
