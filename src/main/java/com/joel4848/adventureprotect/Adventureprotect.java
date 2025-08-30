package com.joel4848.adventureprotect;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class Adventureprotect implements ModInitializer {
    public static GameRules.Key<GameRules.BooleanRule> TRAPDOOR;
    public static GameRules.Key<GameRules.BooleanRule> FENCEGATE;
    public static GameRules.Key<GameRules.BooleanRule> FLOWERPOT;
    public static GameRules.Key<GameRules.BooleanRule> DOOR;
    public static GameRules.Key<GameRules.BooleanRule> CHEST;
    public static GameRules.Key<GameRules.BooleanRule> BARREL;
    public static GameRules.Key<GameRules.BooleanRule> XERCAPAINT_EASEL;
    public static GameRules.Key<GameRules.BooleanRule> XERCAPAINT_CANVAS;
    public static GameRules.Key<GameRules.BooleanRule> CAMERAPTURE_PICTURE_FRAME;

    public void onInitialize() {
        DOOR = GameRuleRegistry.register("AdventureBlock_Door", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        TRAPDOOR = GameRuleRegistry.register("AdventureBlock_Trapdoor", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        FENCEGATE = GameRuleRegistry.register("AdventureBlock_FenceGate", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        FLOWERPOT = GameRuleRegistry.register("AdventureBlock_FlowerPot", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        CHEST = GameRuleRegistry.register("AdventureBlock_Chest", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        BARREL = GameRuleRegistry.register("AdventureBlock_Barrel", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        XERCAPAINT_EASEL = GameRuleRegistry.register("AdventureBlock_XercaPaint_Easel", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        XERCAPAINT_CANVAS = GameRuleRegistry.register("AdventureBlock_XercaPaint_Canvas", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        CAMERAPTURE_PICTURE_FRAME = GameRuleRegistry.register("AdventureBlock_CameraCapture_PictureFrame", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

        BlockInteractionHandler.register();
        EntityInteractionHandler.register();
        EntityDamageHandler.register();
        StickInteractionHandler.register();

        // Register the Exceptionator command
        CommandRegistrationCallback.EVENT.register(ExceptionatorCommand::register);
    }
}