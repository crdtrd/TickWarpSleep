package com.drtdrc.tws.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;wakeUp(ZZ)V"), index = 1)
    boolean onWakeUpForDay(boolean skipSleepTimer) {
        return false;
    }

}
