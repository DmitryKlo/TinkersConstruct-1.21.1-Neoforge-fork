package com.rainy.tconstrict;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.slf4j.Logger;

@Mod(TConstrict.MOD_ID)
public final class TConstrict {
    public static final String MOD_ID = "tconstrict";
    public static final String DISPLAY_NAME = "Tcostrict unofficial port 1.21.1 neoforge";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TConstrict(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerGameTests);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("{} initialized. Upstream TConstruct systems are being ported from the 1.20.1 codebase.", DISPLAY_NAME);
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(TConstrictGameTests.class);
    }
}
