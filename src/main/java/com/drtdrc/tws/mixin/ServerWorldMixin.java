package com.drtdrc.tws.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import net.minecraft.world.MutableWorldProperties;
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
	@Shadow @Final List<ServerPlayerEntity> players;

	@Shadow protected abstract void wakeSleepingPlayers();
	@Shadow abstract public void setTimeOfDay(long timeOfDay);

	// @Shadow abstract public boolean isDay();

	// This effectively replaces vanilla sleep logic
	// Sleeping no longer skips weather events. You can still sleep during thunderstorms.
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/SleepManager;canSkipNight(I)Z"), method = "tick")
	private boolean onIsEnoughPlayersSleeping(SleepManager instance, int percentage) {
		boolean isEnoughResters = instance.canSkipNight(percentage);
		boolean isEnoughSleepers = instance.canResetTime(percentage, players);
		// TODO add condition for time to wake players
		if (isEnoughResters && isEnoughSleepers) {
			if (!warping) {
				warping = true;
				this.getTickManager().setTickRate(sleepGameTickRate);
			}
		} else {
			if (warping) { // && isDay()
				warping = false;
//				long l = this.properties.getTimeOfDay() + 24000L;
//				this.setTimeOfDay(l - l % 24000L);
				this.wakeSleepingPlayers();
				this.getTickManager().setTickRate(20f);
			}
		}
		return false;
	}
}