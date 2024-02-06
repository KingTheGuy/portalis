package com.surv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.surv.items.Item_Manager;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

//TODO(not really possible atm): make it so potion brewing can be done without a player.. so it can be automated.

//TODO(scrapped no need for this): on server shutdown if a cauldron has any items but no uses
//drop the items, and empty.
//OR just figure out saving and loading data

//HOW(tossed in by order): toss item, log item:once logged dont log again. log next item and then last item. if they are equal to a mix: check again if the items are still there if so complete the mix | else overflow?
//TODO: add a can be bottled option
//MAYBE: i could also switch the ingrideinets to be an array and account for that. this can make it so that the recipes can take any amount of ingridenets.

//DO NOW!!!
//TODO: remove all print messages

public class CauldronBrewing implements Listener {
	// items need to be tossed in; in order
	static List<Recipe> mixrecipeList = new ArrayList<>();
	static double cauldron_radius = 0.3;

	public void recipe(ItemStack first, ItemStack second, ItemStack third,
			ItemStack last, ItemStack result, boolean bottled, int uses) {
		Recipe new_recipe = new Recipe();
		new_recipe.first_ingridient = first;
		new_recipe.second_ingridient = second;
		new_recipe.third_ingridient = third;
		new_recipe.last_item = last;
		new_recipe.resulting_item = result;
		new_recipe.can_be_bottled = bottled;
		new_recipe.result_uses = uses;
		mixrecipeList.add(new_recipe);
	}

	public void init_recipes() {
		recipe(new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.SPIDER_EYE),
				new ItemStack(Material.DANDELION), new ItemStack(Material.PAPER), Item_Manager.infused_paper, true, 8);
		recipe(new ItemStack(Material.FEATHER), new ItemStack(Material.PHANTOM_MEMBRANE),
				new ItemStack(Material.POTATO), new ItemStack(Material.COPPER_BLOCK), new ItemStack(Material.OXIDIZED_COPPER),
				false, 8);
		recipe(new ItemStack(Material.BONE_MEAL), new ItemStack(Material.NETHER_WART),
				new ItemStack(Material.LILAC), new ItemStack(Material.NETHERRACK), new ItemStack(Material.COBBLESTONE),
				false, 8);
		// System.out.printf("Are we big? [%s]\n", mixrecipeList.size());
	}

	private class Recipe {
		ItemStack first_ingridient;
		ItemStack second_ingridient;
		ItemStack third_ingridient;
		ItemStack last_item;
		ItemStack resulting_item;
		boolean can_be_bottled;
		int result_uses;
	}

	private List<Cauldron> all_cauldrons = new ArrayList<>();

	public static int max_uses = 24;

	public class Cauldron {
		Location location;
		Boolean mixed;
		// List<Entity> items;
		// int uses;
		Recipe locked_recipe;

		public void CauldronOverflow() {
			Location top = this.location.clone();
			top.setY(top.getY() + 1);
			if (top.getBlock().getType().equals(Material.AIR)) {
				top.getBlock().setType(Material.WATER);
				Levelled level = (Levelled) top.getBlock().getBlockData();
				level.setLevel(3);
				top.getBlock().setBlockData(level);
			}
			// RemoveCauldron();
		}

		public void CauldronTossIngridient(Location location) {
			Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.EXPLOSION_NORMAL, location.getX(),
					location.getY() + 0.2, location.getZ(), 0);
			location.getWorld().playSound(location, org.bukkit.Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, (float) 1.0,
					(float) 0.8);
		}

		private void RemoveCauldron() {
			// all_cauldrons.remove(this);
			// if (remove_list.contains(this)) {
			// return;
			// }
			remove_list.add(this);
		}

		private void CheckCauldron() {
			Block block = this.location.getBlock();
			Block block_below = Bukkit.getWorld(this.location.getWorld().getUID()).getBlockAt(this.location.getBlockX(),
					this.location.getBlockY() - 1,
					this.location.getBlockZ());
			if (block.getType().equals(Material.AIR)) {
				this.RemoveCauldron();
				return;
			}
			if (!block.getType().equals(Material.WATER_CAULDRON)) {
				// System.out.printf("removed cauldron at: [%s,%s,%s]\n", this.location.getX(),
				// this.location.getY(),
				// this.location.getZ());
				this.RemoveCauldron();
				return;
			}
			// FIXME: just make sure this is right != or just ==
			if (block_below.getType().equals(Material.CAMPFIRE) || block_below.getType().equals(Material.SOUL_CAMPFIRE)) {
			} else {
				this.RemoveCauldron();
			}
		}

		public void CauldronIdleParticles() {
			int min = 2;
			int max = 8;
			float x = (new Random().nextInt(max - min + 1) + min);
			float z = (new Random().nextInt(max - min + 1) + min);
			// magic.getPlugin().getComponentLogger().info(Component.text(String.format("x:[%s],
			// z:[%s]",x,z)));
			// location.getWorld().playSound(location,org.bukkit.Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP,
			// 1,1);
			this.location.getWorld().playSound(this.location, org.bukkit.Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1,
					(float) 0.5);
			// Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.BUBBLE_POP,
			// location.getBlockX() + (x / 10),
			// location.getBlockY() + 1, location.getBlockZ() + (z / 10), 0);
			Bukkit.getWorld(this.location.getWorld().getUID()).spawnParticle(Particle.WATER_SPLASH,
					this.location.getBlockX() + (x / 10),
					this.location.getBlockY() + 1, this.location.getBlockZ() + (z / 10), 0);
		}

		public void CauldronMixedParticle() {
			int min = 2;
			int max = 8;
			float x = (new Random().nextInt(max - min + 1) + min);
			float z = (new Random().nextInt(max - min + 1) + min);
			float y = (new Random().nextInt(max - min + 1) + min);
			Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.GLOW, location.getBlockX() + (x / 10),
					location.getBlockY() + (1 + y / 10), location.getBlockZ() + (z / 10), 0);
		}

		public void CauldronMixComplete() {
			Bukkit.getWorld(this.location.getWorld().getUID()).spawnParticle(Particle.EXPLOSION_NORMAL,
					this.location.getBlockX() + 0.5,
					this.location.getBlockY() + 1, this.location.getBlockZ() + 0.5, 0);
			this.location.getWorld().playSound(this.location, org.bukkit.Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE,
					(float) 1.0,
					(float) 0.8);
			// Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.EXPLOSION_NORMAL,
			// location.getBlockX() + 0.5,
			// location.getBlockY() + 1, location.getBlockZ() + 0.5, 0);
			// int index = findCauldron(location);
			// Cauldron cauldron = all_cauldrons.get(index);
			this.mixed = true;
		}

		public void CauldronMixUse(Recipe recipe, ItemStack item) {
			// int index = findCauldron(this.location);
			// location.getWorld().playSound(location, org.bukkit.Sound.ITEM_BOTTLE_FILL, 1,
			// 1);
			// if (index == -1) {
			// return;
			// }
			Bukkit.getWorld(this.location.getWorld().getUID()).spawnParticle(Particle.END_ROD,
					this.location.getBlockX() + 0.5,
					this.location.getBlockY() + 1.5, this.location.getBlockZ() + 0.5, 0);
			if (item.isSimilar(this.locked_recipe.last_item)) {
				// System.out.printf("should be gettnig %s\n",
				// this.locked_recipe.last_item);
				Location new_location = new Location(this.location.getWorld(), this.location.getBlockX(),
						this.location.getBlockY() + 1, this.location.getBlockZ());
				// new_location.setY(new_location.getY() + 1);
				Bukkit.getServer().getWorld(new_location.getWorld().getUID()).dropItem(new_location,
						recipe.resulting_item);
				this.locked_recipe.result_uses -= 1;
			}
			if (item.getType().equals(Material.GLASS_BOTTLE)) {
				this.locked_recipe.result_uses -= 8; // bottle has 8 uses?
			}
			if (this.locked_recipe.result_uses < 1) {
				// this.CauldronMixComplete();
				this.RemoveCauldron();
			}
			for (Recipe mix : mixrecipeList) {
				// System.out.printf("mix: (%s) has [%s] uese.\n", mix.last_item,
				// mix.result_uses);
			}
		}
	}

	private int findCauldron(Location loc) {
		if (all_cauldrons.size() <= 0) {
			return -1;
		}
		for (Cauldron c : all_cauldrons) {
			if (c.location.equals(loc)) {
				return all_cauldrons.indexOf(c);
			}
		}
		return -1;
	}

	private void createCauldron(Location loc) {
		int index = findCauldron(loc);
		// magic.getPlugin().getComponentLogger().info(Component.text(String.format("index
		// is: [%s]", index)));
		if (index == -1) {
			Cauldron c = new Cauldron();
			c.location = loc;
			c.mixed = false;
			c.locked_recipe = new Recipe();
			all_cauldrons.add(c);
		}
		return;
	}

	List<Cauldron> remove_list = new ArrayList<>();

	private void ToBeRemoved() {
		for (Cauldron c : remove_list) {
			// System.out.printf("removed cauldron at: [%s,%s,%s]\n", c.location.getX(),
			// c.location.getY(),
			// c.location.getZ());
			if (c.location.getBlock().getType().equals(Material.WATER_CAULDRON)) {
				c.location.getBlock().setType(Material.CAULDRON);
			}
			all_cauldrons.remove(c);
		}
		remove_list.clear();
	}

	// NOTE: this should also be looking for the Y axis
	public int getDistance(int x1, int z1, int x2, int z2) {
		int z = x2 - x1;
		int x = z2 - z1;
		return (int) Math.sqrt(x * x + z * z);
	}

	@EventHandler
	public void onServerStart(ServerLoadEvent event) {
		init_recipes();
	}

	@EventHandler
	public void tick(ServerTickStartEvent event) {
		if (all_cauldrons.size() > 0) {
			ToBeRemoved();
			if (event.getTickNumber() % 6 == 1) {
				for (Cauldron c : all_cauldrons) {
					c.CheckCauldron();
				}
				for (Cauldron c : all_cauldrons) {
					c.CauldronIdleParticles();
					CauldronCheckMixRecipe(c.location);
					if (c.mixed == true) {
						c.CauldronMixedParticle();
						CauldronCheckResult(c.location);
					}
				}
			}
		}
	}

	// on player consume ender_oil teleport them
	@EventHandler
	public void onPlayerConsume(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		if (item.equals(Item_Manager.ender_oil)) {
			Block block = player.getTargetBlockExact(30);
			// Block block = player.getTargetBlockFace(30).getDirection();
			if (block == null) {
				Audience audience = Audience.audience(player);
				audience.sendActionBar(() -> Component.text("Too Far").color(NamedTextColor.RED));
				event.setCancelled(true);
				return;
			}
			player.damage(10);
			player.teleportAsync(block.getLocation());
			player.getLocation().getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_SHULKER_TELEPORT, 1f, 1f);
			// player.damage();
		}
	}

	// should only work if recipe can be bottled.
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block clicked_block = event.getClickedBlock();
		//
		// magic.getPlugin().getComponentLogger().info(Component.text(String.format("what
		// click? [%s]", event.getAction())));
		// if (event.getAction().isLeftClick()) {
		// return;
		// }
		if (clicked_block != null) {
			Location block_location = clicked_block.getLocation();
			// if (clicked_block.getType() == Material.WATER_CAULDRON
			// ||
			if (player.getInventory().getItemInMainHand().getType().equals(Material.WATER_BUCKET)) {
				if (clicked_block.getType() != Material.CAULDRON) {
					return;
				}
				Block block_below = Bukkit.getWorld(block_location.getWorld().getUID()).getBlockAt(block_location.getBlockX(),
						block_location.getBlockY() - 1, block_location.getBlockZ());
				if (block_below.getType() == Material.CAMPFIRE || block_below.getType() == Material.SOUL_CAMPFIRE) {
					createCauldron(block_location);
					// System.out.printf("created cauldron at [%s,%s,%s]\n",
					// clicked_block.getLocation().blockX(),
					// clicked_block.getLocation().blockY(), clicked_block.getLocation().blockZ());

					// ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE);
					// if
					// (player.getInventory().getItemInMainHand().getType().equals(Material.GLASS_BOTTLE))
					// {
					// event.setCancelled(true);// cancel the bottle from turing into water
					// PlayerInventory player_inv = player.getInventory();
					// player_inv.removeItem(bottle);
					// player_inv.addItem(Item_Manager.ender_oil);
					// player.updateInventory();
					// CauldronMixUse(block_location, bottle);
					// // TODO: i need to check if the player's inv is full
					// // player_inv.getContents();
					// }
				}
			}
		}
	}

	public void CauldronCheckResult(Location location) {
		int index = findCauldron(location);
		Cauldron cauldron = all_cauldrons.get(index);
		Collection<Entity> entities = location.getNearbyEntities(location.getBlockX(), location.getBlockY(),
				location.getBlockZ());

		Recipe recipe = cauldron.locked_recipe;
		entities.forEach(entity -> {
			if (entity instanceof Item) {
				Item entity_item = (Item) entity;
				ItemStack item = entity_item.getItemStack();
				// if (cauldron.mixed == true) {
				// return;
				// }
				if (getDistance(location.blockX(), location.blockZ(), entity.getLocation().blockX(),
						entity.getLocation().blockZ()) > cauldron_radius) {
					return;
				}
				if (recipe.last_item.isSimilar(item)) {
					// System.out.printf("entity size: %s\n", item);
					cauldron.CauldronTossIngridient(location);
					for (int x = 0; x <= item.getAmount(); x++) {
						// System.out.printf("uses left: %s\n", cauldron.locked_recipe.result_uses);
						cauldron.CauldronMixUse(recipe, item);
						item.setAmount(item.getAmount() - 1);
						if (cauldron.locked_recipe.result_uses < 1) {
							break;
						}
					}
					if (item.getAmount() < 1) {
						entity.remove();
					}
					// if (entity_item.getItemStack().getAmount() == recipe.result_uses) {
					// // ItemStack new_item = new ItemStack(recipe.resulting_item.getType(),
					// // recipe.result_uses);
					// Location new_location = cauldron.location;
					// new_location.setY(new_location.getY() + 1);
					// for (int x = 0; x < recipe.result_uses; x++) {
					// Bukkit.getServer().getWorld(new_location.getWorld().getUID()).dropItem(new_location,
					// recipe.resulting_item);
					// }
					// cauldron.CauldronMixComplete();
					// cauldron.RemoveCauldron();
					// }

				}
			}
		});

	}

	public boolean recipeContainsIngridient(ItemStack item) {
		for (Recipe recipe : mixrecipeList) {
			if (recipe.first_ingridient.isSimilar(item)) {
				return true;
			}
			if (recipe.second_ingridient.isSimilar(item)) {
				return true;
			}
			if (recipe.third_ingridient.isSimilar(item)) {
				return true;
			}
			if (recipe.last_item.isSimilar(item)) {
				return true;
			}

		}
		return false;
	}

	// TODO: at function to check right items were tossed in
	public void CauldronCheckMixRecipe(Location location) {
		int index = findCauldron(location);
		Cauldron cauldron = all_cauldrons.get(index);
		Collection<Entity> entities = location.getNearbyEntities(location.getBlockX(), location.getBlockY(),
				location.getBlockZ());

		Recipe recipe = cauldron.locked_recipe;
		entities.forEach(entity -> {
			if (entity instanceof Item) {
				Item entity_item = (Item) entity;
				ItemStack item = entity_item.getItemStack();
				if (cauldron.mixed == true) {
					return;
				}
				if (getDistance(location.blockX(), location.blockZ(), entity.getLocation().blockX(),
						entity.getLocation().blockZ()) > cauldron_radius) {
					return;
				}
				if (!recipeContainsIngridient(item)) {
					cauldron.CauldronTossIngridient(entity.getLocation());
					cauldron.CauldronOverflow();
					cauldron.RemoveCauldron();
					return;
				}
				if (recipe.first_ingridient == null || recipe.first_ingridient.equals(item)) {
					recipe.first_ingridient = item;
					cauldron.CauldronTossIngridient(entity.getLocation());
					entity.remove();
					return;
				}
				if (recipe.second_ingridient == null || recipe.second_ingridient.equals(item)) {
					recipe.second_ingridient = item;
					cauldron.CauldronTossIngridient(entity.getLocation());
					entity.remove();
					return;
				}
				if (recipe.third_ingridient == null || recipe.third_ingridient.equals(item)) {
					recipe.third_ingridient = item;
					cauldron.CauldronTossIngridient(entity.getLocation());
					entity.remove();
					return;
				}
			}
		});
		// FIXME: THIS IS FUCKED!!!!!!

		if (cauldron.mixed == false) {
			for (int x = 0; x < mixrecipeList.size(); x++) {
				// System.out.printf("%s: mix=[%s,%s,%s],current=[%s,%s,%s]\n",
				// mixrecipeList.indexOf(mix), mix.first_ingridient,
				// mix.second_ingridient, mix.third_ingridient, recipe.first_ingridient,
				// recipe.second_ingridient,
				// recipe.third_ingridient);
				Recipe mix = mixrecipeList.get(x);
				if (!mix.first_ingridient.equals(recipe.first_ingridient)) {
					continue;
				}
				if (!mix.second_ingridient.equals(recipe.second_ingridient)) {
					continue;
				}
				if (!mix.third_ingridient.equals(recipe.third_ingridient)) {
					continue;
				}
				// FIXME: shit i need to also implement the deletion of the items/entities
				cauldron.locked_recipe = new Recipe();
				cauldron.locked_recipe.first_ingridient = mix.first_ingridient;
				cauldron.locked_recipe.second_ingridient = mix.second_ingridient;
				cauldron.locked_recipe.third_ingridient = mix.third_ingridient;
				cauldron.locked_recipe.last_item = mix.last_item;
				cauldron.locked_recipe.resulting_item = mix.resulting_item;
				cauldron.locked_recipe.can_be_bottled = mix.can_be_bottled;
				cauldron.locked_recipe.result_uses = mix.result_uses;

				cauldron.CauldronMixComplete();
			}
			// for (Recipe mix : mixrecipeList) {

			// }
		}
		if (recipe.first_ingridient != null && recipe.second_ingridient != null && recipe.third_ingridient != null) {
			if (cauldron.mixed == false) {
				cauldron.CauldronOverflow();
				cauldron.RemoveCauldron();
			}
		}
		// System.out.printf("Items [%s][%s][%s]\n",
		// cauldron.locked_recipe.first_ingridient,
		// cauldron.locked_recipe.second_ingridient,
		// cauldron.locked_recipe.third_ingridient);
	}

}
