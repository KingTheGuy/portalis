package com.kingtheguy.items;

import java.util.EnumSet;
import java.util.List;

import com.kingtheguy.DialMenu;
import com.kingtheguy.DialMenu.PlayerDial;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PortalisItem extends Item {
  public PortalisItem(Settings settings) {
    super(settings);
  }

  @Override
  public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
  	tooltip.add(Text.translatable(String.format("%s/8 uses",3)).formatted(Formatting.GOLD));
  }

  //this should be part of some utils.. as lodestones will be using the same effect
  public void teleportEffect(PlayerEntity player) {
    if (!player.getWorld().isClient()) {
      player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_SHULKER_TELEPORT,
          SoundCategory.NEUTRAL, 1f, 1f);
      player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_EYE_DEATH,
          SoundCategory.NEUTRAL, 1f, 1f);
    }
  }

  @Override
  public ActionResult use(World world, PlayerEntity player, Hand hand) {
    // ItemStack stack = user.getStackInHand(hand);
    if (world.isClient) {
      return ActionResult.PASS;
    }
    // TODO: at this point just need DialMenu.getPlayer(player)d to then do about the

    // same as the plugin

    // System.out.println(String.format("how many 'players' are in the menu? [ %s
    // ]", DialMenu.players_with_dial.size()));

    if (!player.getWorld().isClient()) {
      player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER,
          SoundCategory.NEUTRAL, 1f, 1f);
    }
    bookUsage(player,false);

    // return ActionResult.PASS;
    return ActionResult.SUCCESS;
  }

  @Override
  public ActionResult useOnBlock(ItemUsageContext context) {
    BlockPos blockPos = context.getBlockPos();
    // Get the block state
    BlockState blockState = context.getWorld().getBlockState(blockPos);
    // Do something with the block state
    System.out.println("Item used on block: " + blockState.getBlock().getName());
    if (context.getWorld().isClient()) {
        return ActionResult.SUCCESS;
    }

    bookUsage(context.getPlayer(), context.getPlayer().isInSneakingPose());

    return ActionResult.SUCCESS;
  }

  public void bookUsage(PlayerEntity player, boolean on_block) {
    PlayerDial p_dial = DialMenu.getPlayer(player);
    if (p_dial != null) {
      p_dial.makeSelection();
      if (p_dial.getAnswer() == "CLOSE-BOOK") {
        DialMenu.closeDialMenu(player);
        return;
      }
      ServerWorld server_world = player.getServer().getWorld(World.OVERWORLD);
      BlockPos server_spawn = server_world.getSpawnPos();
      switch (p_dial.getDialId()) {
        case "PORTALIS:main":
          switch (p_dial.getAnswer()) {
            case "SPAWN":
              // FIXME: im pretty sure i need take into acount the world.. not just the pos
              player.teleport(server_world, server_spawn.getX(), server_spawn.getY(), server_spawn.getZ(),
                  EnumSet.noneOf(PositionFlag.class), player.getYaw(), player.getPitch(), false);
              teleportEffect(player);
              DialMenu.closeDialMenu(player);
            case "BED":
              // FIXME: im pretty i need take into acount the world.. not just the pos
              player.teleport(server_world, server_spawn.getX(), server_spawn.getY(), server_spawn.getZ(),
                  EnumSet.noneOf(PositionFlag.class), player.getYaw(), player.getPitch(), false);
              teleportEffect(player);
              DialMenu.closeDialMenu(player);
          }
      }
    } else {
      if (!on_block) {       
        DialMenu.openDialMenu(player, "PORTALIS:main",
            List.of("BED", "SPAWN", "WARPS", "LAST-DEATH", "INFO", "CLOSE-BOOK"));
      } else {
        DialMenu.openDialMenu(player, "PORTALIS:main",
            List.of("create-warp","CLOSE-BOOK"));
      }
    }
  }

}
