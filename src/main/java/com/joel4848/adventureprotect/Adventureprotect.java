package com.joel4848.adventureprotect;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Adventureprotect implements ModInitializer {
    public static final String MOD_ID = "adventureprotect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Load config with file watching for live reloading
        AdventureProtectConfig.load();

        // Register all interaction handlers
        BlockInteractionHandler.register();
        EntityInteractionHandler.register();
        EntityDamageHandler.register();
        StickInteractionHandler.register();

        // Register block spoofing handler for XercaMusic client-side GUI prevention
        BlockSpoofingHandler.register();

        // Register commands
        CommandRegistrationCallback.EVENT.register(AdventureProtectCommands::register);

        LOGGER.info("AdventureProtect initialized with block spoofing handler and commands registered.");

        // Add shutdown hook to clean up file watcher
        Runtime.getRuntime().addShutdownHook(new Thread(AdventureProtectConfig::stopConfigWatcher));
    }
}