package com.drtdrc.tws.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.tick.TickManager;

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
	@Unique private boolean warping = false;
	@Unique float sleepGameTickRate = 200;

	@Shadow public abstract TickManager getTickManager();
	@Shadow @Final private List<ServerPlayerEntity> players;

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/SleepManager;canSkipNight(I)Z"), method = "tick")
	private boolean onIsEnoughPlayersSleeping(SleepManager instance, int percentage) {
		boolean isEnoughResters = instance.canSkipNight(percentage);
		boolean isEnoughSleepers = instance.canResetTime(percentage, players);

		if (isEnoughResters && isEnoughSleepers) {
			if (!warping) {
				warping = true;
				this.getTickManager().setTickRate(sleepGameTickRate);
			}
		} else {
			if (warping) {
				warping = false;
				this.getTickManager().setTickRate(20f);
			}
		}
		return false;
	}
}