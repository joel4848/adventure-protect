package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
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

            // Check for item frames and glow item frames
            if (entity instanceof ItemFrameEntity || entity instanceof GlowItemFrameEntity) {
                if (AdventureProtectConfig.INSTANCE.DisableItemFrameInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Get the entity type identifier
            Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
            String entityIdString = entityId.toString();

            // Check for XercaPaint easel entity
            if (entityIdString.contains("xercapaint") && entityIdString.contains("easel")) {
                if (AdventureProtectConfig.INSTANCE.DisableEaselInteraction) {
                    return ActionResult.PASS; // Always allow interactions with easels when protection is on
                }
            }

            // Check for XercaPaint canvas entity
            if (entityIdString.contains("xercapaint") && entityIdString.contains("canvas")) {
                if (AdventureProtectConfig.INSTANCE.DisablePlacedCanvasInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check for CameraCapture picture frame entity
            if (entityIdString.contains("camerapture") && (entityIdString.contains("picture") || entityIdString.contains("frame"))) {
                if (AdventureProtectConfig.INSTANCE.DisablePlacedPhotographInteraction) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
}