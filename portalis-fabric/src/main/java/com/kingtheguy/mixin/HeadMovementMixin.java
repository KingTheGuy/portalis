package com.kingtheguy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kingtheguy.DialMenu;

// import com.nimbusds.openid.connect.sdk.federation.entities.EntityType;
// import net.minecraft.entity.LivingEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

@Mixin(PlayerEntity.class)
public class HeadMovementMixin {
  @Inject(method = "tick", at = @At("HEAD"))
  private void onTick(CallbackInfo ci) {
    PlayerEntity player = (PlayerEntity) (Object) this;
    // Get the player's head yaw and pitch
    float headYaw = player.getHeadYaw();
    //TODO: somewhere here i'll be checking at all lodestones for if a player is above it.. maybe its better to check if the player has a lodestone under them. it may be a lot more performant

    // float headPitch = ((LivingEntity) (Object) this).getHeadPitch();
    
    // Check if the head yaw or pitch has changed
    // if (((LivingEntity) (Object) this).prevHeadYaw != headYaw) {
    if (player.prevHeadYaw != headYaw) {
      // Run your code here
      // System.out.println("Player moved their head!");
      // System.out.println(String.format("yaw is: [ %s ]", headYaw));
      // player.sendMessage(Text.of(String.format("§3head yaw: %s",headYaw)), true);
      DialMenu.refreshDialMenu(player);
    }

  }
}
