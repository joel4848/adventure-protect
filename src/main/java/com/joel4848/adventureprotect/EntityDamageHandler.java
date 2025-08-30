package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.registry.Registries;

public class EntityDamageHandler {

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            if (serverPlayer.interactionManager.getGameMode() != GameMode.ADVENTURE) {
                return ActionResult.PASS;
            }

            // Get the entity type identifier
            Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
            String entityIdString = entityId.toString();

            // Check for XercaPaint easel entity
            if (entityIdString.contains("xercapaint") && entityIdString.contains("easel")) {
                // Check if easel has a canvas by looking for Item NBT data
                boolean hasCanvas = false;
                try {
                    var nbt = entity.writeNbt(new NbtCompound());
                    hasCanvas = nbt.contains("Item");
                } catch (Exception e) {
                    // If we can't read NBT, default to preventing break
                    hasCanvas = false;
                }

                // If easel has canvas, allow punch (to remove canvas)
                if (hasCanvas) {
                    return ActionResult.PASS; // Allow canvas removal
                } else {
                    // Empty easel - check gamerule to see if breaking is allowed
                    if (!world.getGameRules().getBoolean(Adventureprotect.XERCAPAINT_EASEL)) {
                        return ActionResult.FAIL; // Prevent breaking empty easel
                    }
                }
            }

            // Check for XercaPaint canvas entity
            if (entityIdString.contains("xercapaint") && entityIdString.contains("canvas")) {
                if (!world.getGameRules().getBoolean(Adventureprotect.XERCAPAINT_CANVAS)) {
                    return ActionResult.FAIL;
                }
            }

            // Check for CameraCapture picture frame entity
            if (entityIdString.contains("camerapture") && (entityIdString.contains("picture") || entityIdString.contains("frame"))) {
                if (!world.getGameRules().getBoolean(Adventureprotect.CAMERAPTURE_PICTURE_FRAME)) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
}