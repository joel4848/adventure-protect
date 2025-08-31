package com.joel4848.adventureprotect;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Adventureprotect implements ModInitializer {

    public void onInitialize() {
        // Load config
        AdventureProtectConfig.load();

        BlockInteractionHandler.register();
        EntityInteractionHandler.register();
        EntityDamageHandler.register();
        StickInteractionHandler.register();

        // Register the Exceptionator command
        CommandRegistrationCallback.EVENT.register(ExceptionatorCommand::register);
    }
}