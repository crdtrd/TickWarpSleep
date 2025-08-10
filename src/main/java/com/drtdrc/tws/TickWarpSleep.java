package com.drtdrc.tws;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TickWarpSleep implements ModInitializer {
	public static final String MOD_ID = "tickwarpsleep";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("TWS initialized");
	}
}