package com.kingtheguy.items;

import java.util.List;
import java.util.function.Function;

import javax.swing.plaf.basic.BasicComboBoxUI.ItemHandler;

import com.kingtheguy.Portalisfabric;
import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;


public class Item_Manager {
  private Item_Manager() {
  }

  public static final Item PORTALIS_ITEM = register("portalis", PortalisItem::new, new Item.Settings());
  public static final Item INFUSED_PAPER_ITEM = register("infused_paper", InfusedPaperItem::new, new Item.Settings());
  // public static final Item PORTALIS_ITEM = new PotionItem(new Item.Settings());

 
  // public static final RegistryKey<ItemGroup> CUSTOM_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(Portalisfabric.MOD_ID, "item_group"));
  // public static final ItemGroup CUSTOM_ITEM_GROUP = FabricItemGroup.builder()
		// .icon(() -> new ItemStack(PORTALIS_ITEM))
		// .displayName(Text.translatable("itemGroup.portalis-fabric"))
		// .build();

  public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
    final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Portalisfabric.MOD_ID, path));
    return Items.register(registryKey, factory, settings);
  }

  public static void initialize() {
  // Get the event for modifying entries in the ingredients group.
  // And register an event handler that adds our suspicious item to the ingredients group.
  // Registry.register(Registries.ITEM, Identifier.of(Portalisfabric.MOD_ID, "portalis"), PORTALIS_ITEM);
  ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
  		.register((itemGroup) -> itemGroup.add(Item_Manager.PORTALIS_ITEM));
  ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
  		.register((itemGroup) -> itemGroup.add(Item_Manager.INFUSED_PAPER_ITEM));
    
  }

  //NOTE: can't test right now, assume this works for now
  public static final ComponentType<Integer> PORTALIS_USE_COMPONENT = Registry.register(
      Registries.DATA_COMPONENT_TYPE,
      Identifier.of(Portalisfabric.MOD_ID, "portalis_use"),
      ComponentType.<Integer>builder().codec(Codec.INT).build()
  );

  public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
    int count = stack.get(PORTALIS_USE_COMPONENT);
    tooltip.add(Text.translatable("item.fabric-docs-reference.counter.info", count).formatted(Formatting.GOLD));
  }
}
