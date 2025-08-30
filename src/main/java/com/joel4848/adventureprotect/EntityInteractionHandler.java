package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.registry.Registries;

public class EntityInteractionHandler {

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            if (serverPlayer.interactionManager.getGameMode() != GameMode.ADVENTURE) {
                return ActionResult.PASS;
            }

            // Get the entity type identifier
            Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
            String entityIdString = entityId.toString();

            // Debug: Send entity ID to player
/*            if (entityIdString.contains("xercapaint") || entityIdString.contains("camerapture")) {
                player.sendMessage(Text.literal("§eDebug: Entity ID is: " + entityIdString), false);
            }*/

            // Check for XercaPaint easel entity
            if (entityIdString.contains("xercapaint") && entityIdString.contains("easel")) {
/*
                player.sendMessage(Text.literal("§cXercaPaint Easel entity detected: " + entityIdString), false);
*/
                if (!world.getGameRules().getBoolean(Adventureprotect.XERCAPAINT_EASEL)) {
                    return ActionResult.PASS; // Always allow interactions with easels
                }
            }

            // Check for XercaPaint canvas entity
            if (entityIdString.contains("xercapaint") && entityIdString.contains("canvas")) {
/*
                player.sendMessage(Text.literal("§cXercaPaint Canvas entity detected: " + entityIdString), false);
*/
                if (!world.getGameRules().getBoolean(Adventureprotect.XERCAPAINT_CANVAS)) {
                    return ActionResult.FAIL;
                }
            }

            // Check for CameraCapture picture frame entity
            if (entityIdString.contains("camerapture") && (entityIdString.contains("picture") || entityIdString.contains("frame"))) {
/*
                player.sendMessage(Text.literal("§cCameraCapture Picture Frame entity detected: " + entityIdString), false);
*/
                if (!world.getGameRules().getBoolean(Adventureprotect.CAMERAPTURE_PICTURE_FRAME)) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
}