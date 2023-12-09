package com.surv;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

//[]
//TODO: figure out a better way yo go about making cauldron recipes

//TODO: make it so potion brewing can be done without a player.. so it can be automated.
//IDEAS: 
// -just drop paper into the cauldron?
// -not sure what else.

//FIXME(fixed? i dont see an error mate): for some reason there is error when cauldron gets broken will full or emptied
//FIXME(done): need to make sure only the correct items and only of of each will trigger the mix from completing
//TODO(scrapped no need for this): on server shutdown if a cauldron has any items but no uses
//drop the items, and empty.
//OR just figure out saving and loading data
//TODO(done): adding items to brew will be as simple as just dropping them in

public class CauldronBrewing implements Listener {
	// items need to be tossed in; in order
	static List<Recipe> mixrecipeList = new ArrayList<>();

	public void create_recipe(String recipe_name, ItemStack first, ItemStack second, ItemStack third,
			ItemStack result) {
		Recipe new_recipe = new Recipe();
		new_recipe.first_item = first;
		new_recipe.second_item = second;
		new_recipe.third_item = third;
		new_recipe.resulting_item = result;
		new_recipe.recipe_name = recipe_name;
		mixrecipeList.add(new_recipe);
	}

	public void init_recipes() {
		create_recipe("ender oil", new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.SPIDER_EYE),
				new ItemStack(Material.DANDELION), Item_Manager.ender_oil);
	}

	private class Recipe {
		String recipe_name;
		ItemStack first_item;
		ItemStack second_item;
		ItemStack third_item;

		ItemStack resulting_item;
	}

	private List<Cauldron> all_cauldrons = new ArrayList<>();

	public static int max_uses = 24;

	private class Cauldron {
		Location loc;
		Boolean mixed;
		List<Entity> items;
		int uses;
	}

	private int findCauldron(Location loc) {
		if (all_cauldrons.size() <= 0) {
			return -1;
		}
		for (Cauldron c : all_cauldrons) {
			if (c.loc.equals(loc)) {
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
			c.loc = loc;
			c.mixed = false;
			c.items = new ArrayList<>();
			all_cauldrons.add(c);
		}
		return;
	}

	List<Cauldron> remove_list = new ArrayList<>();

	private void RemoveCauldron(Location loc) {
		int index = findCauldron(loc);
		// ToBeRemoved(all_cauldrons.get(index));
		remove_list.add(all_cauldrons.get(index));
		// all_cauldrons.remove(index);
	}

	private void ToBeRemoved() {
		for (Cauldron c : remove_list) {
			all_cauldrons.remove(c);
		}
		remove_list.clear();
	}

	private void CheckCauldron(Location loc) {
		// int index = findCauldron(loc);
		Block block = loc.getBlock();
		if (block.getType() != Material.WATER_CAULDRON) {
			RemoveCauldron(loc);
			return;
		}
		Block block_below = Bukkit.getWorld(loc.getWorld().getUID()).getBlockAt(loc.getBlockX(), loc.getBlockY() - 1,
				loc.getBlockZ());
		if (block_below.getType() == Material.CAMPFIRE || block_below.getType() == Material.SOUL_CAMPFIRE) {
		} else {
			RemoveCauldron(loc);
		}

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
			for (Cauldron c : all_cauldrons) {
				CheckCauldron(c.loc);
			}
			ToBeRemoved();
			if (event.getTickNumber() % 6 == 1) {
				for (Cauldron c : all_cauldrons) {
					CauldronParticles(c.loc);
					CauldronCheckMixRecipe(c.loc);
					if (c.mixed == true) {
						CauldronMixedParticle(c.loc);
					}
				}
			}
		}
	}

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

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block clicked_block = event.getClickedBlock();
		// magic.getPlugin().getComponentLogger().info(Component.text(String.format("what
		// click? [%s]", event.getAction())));
		// if (event.getAction().isLeftClick()) {
		// return;
		// }
		if (clicked_block != null) {
			Location block_location = clicked_block.getLocation();
			if (clicked_block.getType() == Material.WATER_CAULDRON
					|| player.getInventory().getItemInMainHand().getType().equals(Material.WATER_BUCKET)) {
				Block block_below = Bukkit.getWorld(block_location.getWorld().getUID()).getBlockAt(block_location.getBlockX(),
						block_location.getBlockY() - 1, block_location.getBlockZ());
				if (block_below.getType() == Material.CAMPFIRE || block_below.getType() == Material.SOUL_CAMPFIRE) {
					createCauldron(block_location);
					ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE);
					if (player.getInventory().getItemInMainHand().getType().equals(Material.GLASS_BOTTLE)) {
						event.setCancelled(true);// cancel the bottle from turing into water
						PlayerInventory player_inv = player.getInventory();
						player_inv.removeItem(bottle);
						player_inv.addItem(Item_Manager.ender_oil);
						player.updateInventory();
						CauldronMixUse(block_location, bottle);
						// TODO: i need to check if the player's inv is full
						// player_inv.getContents();
					}
				}
			}
		}
	}

	// TODO: at function to check right items were tossed in
	public void CauldronCheckMixRecipe(Location location) {
		int index = findCauldron(location);
		Cauldron cauldron = all_cauldrons.get(index);
		// int found_items = 0;
		double cauldron_radius = 1;
		List<Entity> entities_to_kill = new ArrayList<>();
		location.getNearbyEntities(location.getBlockX(), location.getBlockY(), location.getBlockZ()).forEach(entity -> {
			if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
				// if (cauldron.mixed == true) {
				// if (entity.getName().toUpperCase().replace(" ",
				// "_").equals(Material.PAPER.name())) {
				// if (getDistance(location.blockX(), location.blockZ(),
				// entity.getLocation().blockX(),
				// entity.getLocation().blockZ()) < cauldron_radius) {
				// if (!cauldron.items.contains(entity)) {
				// cauldron.items.add(entity);
				// entities_to_kill.add(entity);
				// for (Entity e : cauldron.items) {
				// e.remove();
				// }
				// cauldron.items.clear();
				// }
				// }
				// }
				// CauldronMixUse(location, new ItemStack(Material.PAPER));
				// return;
				// }

				// magic.getPlugin().getComponentLogger().info(Component.text(String.format("found
				// a: [%s] looking for: [%s]",entity.getName().toUpperCase().replace(" ",
				// "_"),Material.ENDER_PEARL.name())));

				if (entity.getName().toUpperCase().replace(" ", "_").equals(Material.ENDER_PEARL.name())) {
					if (getDistance(location.blockX(), location.blockZ(), entity.getLocation().blockX(),
							entity.getLocation().blockZ()) < cauldron_radius) {
						if (!cauldron.items.contains(entity)) {
							cauldron.items.add(entity);
							entities_to_kill.add(entity);
						}
						// magic.getPlugin().getComponentLogger().info(Component.text("found the ender
						// pearl"));
					}
				}
				if (entity.getName().toUpperCase().equals(Material.DANDELION.name())) {
					if (getDistance(location.blockX(), location.blockZ(), entity.getLocation().blockX(),
							entity.getLocation().blockZ()) < cauldron_radius) {
						if (!cauldron.items.contains(entity)) {
							cauldron.items.add(entity);
							entities_to_kill.add(entity);
						}
						// magic.getPlugin().getComponentLogger().info(Component.text("found the
						// dendelion"));
					}
				}
				if (entity.getName().toUpperCase().replace(" ", "_").equals(Material.SPIDER_EYE.name())) {
					if (getDistance(location.blockX(), location.blockZ(), entity.getLocation().blockX(),
							entity.getLocation().blockZ()) < cauldron_radius) {
						// location.getWorld().playSound(location,org.bukkit.Sound.ENTITY_FISHING_BOBBER_SPLASH,
						// 1,(float) cauldron_radius);
						// magic.getPlugin().getComponentLogger().info(Component.text("found the spider
						// eye"));
						if (!cauldron.items.contains(entity)) {
							cauldron.items.add(entity);
							entities_to_kill.add(entity);
						}
					}
				}
			}
		});

		// FIXME: why defuq does this not work?
		if (cauldron.mixed == true) { // make sure items dont keep getting ate
			// magic.getPlugin().getComponentLogger().info(Component.text(String.format("bro
			// why no work?")));
			return;
		}

		if (cauldron.items.size() == 3) {
			// magic.getPlugin().getComponentLogger()
			// .info(Component.text(String.format("have found [%s/2] items", found_items)));
			CauldronMixComplete(location);
			for (Entity e : cauldron.items) {
				e.remove();
			}
			cauldron.items.clear();
		} else {
			cauldron.items.clear();
		}
		// for (Entity e : entities_to_kill) {
		// found_items++;
		// }
		// if (found_items == 3) {
		// }
	}

	public void CauldronMixComplete(Location location) {
		Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.EXPLOSION_NORMAL, location.getBlockX() + 0.5,
				location.getBlockY() + 1, location.getBlockZ() + 0.5, 0);
		// Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.EXPLOSION_NORMAL,
		// location.getBlockX() + 0.5,
		// location.getBlockY() + 1, location.getBlockZ() + 0.5, 0);
		int index = findCauldron(location);
		Cauldron cauldron = all_cauldrons.get(index);
		location.getWorld().playSound(location, org.bukkit.Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, (float) 1.0,
				(float) 0.8);
		cauldron.mixed = true;
		cauldron.uses = max_uses;
	}

	public void CauldronMixUse(Location location, ItemStack item) {
		int index = findCauldron(location);
		location.getWorld().playSound(location, org.bukkit.Sound.ITEM_BOTTLE_FILL, 1, 1);

		if (index == -1) {
			return;
		}
		Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.END_ROD, location.getBlockX() + 0.5,
				location.getBlockY() + 1.5, location.getBlockZ() + 0.5, 0);
		Cauldron cauldron = all_cauldrons.get(index);
		if (item.getType().equals(Material.GLASS_BOTTLE)) {
			cauldron.uses = cauldron.uses - 8;
		} else if (item.getType().equals(Material.PAPER)) {

		}
		// Block block = location.getBlock();
		if (cauldron.uses == 0) {
			location.getBlock().setType(Material.CAULDRON);
			RemoveCauldron(location);
		}
	}

	public void CauldronParticles(Location location) {
		int min = 2;
		int max = 8;
		float x = (new Random().nextInt(max - min + 1) + min);
		float z = (new Random().nextInt(max - min + 1) + min);
		// magic.getPlugin().getComponentLogger().info(Component.text(String.format("x:[%s],
		// z:[%s]",x,z)));
		// location.getWorld().playSound(location,org.bukkit.Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP,
		// 1,1);
		location.getWorld().playSound(location, org.bukkit.Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1, (float) 0.5);
		// Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.BUBBLE_POP,
		// location.getBlockX() + (x / 10),
		// location.getBlockY() + 1, location.getBlockZ() + (z / 10), 0);
		Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.WATER_SPLASH, location.getBlockX() + (x / 10),
				location.getBlockY() + 1, location.getBlockZ() + (z / 10), 0);
	}

	public void CauldronMixedParticle(Location location) {
		int min = 2;
		int max = 8;
		float x = (new Random().nextInt(max - min + 1) + min);
		float z = (new Random().nextInt(max - min + 1) + min);
		float y = (new Random().nextInt(max - min + 1) + min);
		Bukkit.getWorld(location.getWorld().getUID()).spawnParticle(Particle.GLOW, location.getBlockX() + (x / 10),
				location.getBlockY() + (1 + y / 10), location.getBlockZ() + (z / 10), 0);
	}

}
