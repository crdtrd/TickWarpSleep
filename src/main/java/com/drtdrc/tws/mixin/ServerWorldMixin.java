package com.drtdrc.tws.mixin;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SleepManager;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.tick.TickManager;

import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World
{
	@Unique private boolean warping = false;
	@Unique float sleepGameTickRate = 200;

	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
	}

	@Shadow public abstract TickManager getTickManager();
	@Shadow @Final List<ServerPlayerEntity> players;

	@Shadow protected abstract void wakeSleepingPlayers();

	// This effectively replaces vanilla sleep logic
	// Sleeping no longer skips weather events. You can still sleep during thunderstorms.
	// Speeds up time in all dimensions
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/SleepManager;canSkipNight(I)Z"), method = "tick")
	private boolean onIsEnoughPlayersSleeping(SleepManager instance, int percentage) {

		// enough players in bed && enough players with sleep counter at 100, 5+ seconds in bed
		boolean playerReq = instance.canSkipNight(percentage) && instance.canResetTime(percentage, players);
		boolean night = this.isNight(); // ambient darkness >= 4

		if (playerReq && night && !warping) {
			warping = true;
			this.getTickManager().setTickRate(sleepGameTickRate);
		}
		if (playerReq && !night && warping) {
			warping = false;
			this.wakeSleepingPlayers();
			this.getTickManager().setTickRate(20f);
		}

		if (!playerReq && !night && warping) {
			warping = false;
			this.wakeSleepingPlayers();
			this.getTickManager().setTickRate(20f);
		}
		if (!playerReq && night && warping) {
			warping = false;
			this.getTickManager().setTickRate(20f);
		}

		// returning false ensures vanilla logic is not executed
		return false;
	}
}