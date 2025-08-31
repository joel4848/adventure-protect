package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public class EntityDamageHandler {

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            if (serverPlayer.interactionManager.getGameMode() != GameMode.ADVENTURE) {
                return ActionResult.PASS;
            }

            // Check for paintings
            if (entity instanceof PaintingEntity) {
                if (AdventureProtectConfig.INSTANCE.DisablePaintingInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check for item frames and glow item frames - NO exceptions for punching (breaking)
            if (entity instanceof ItemFrameEntity || entity instanceof GlowItemFrameEntity) {
                if (AdventureProtectConfig.INSTANCE.DisableItemFrameInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check for armor stands
            if (entity instanceof ArmorStandEntity) {
                // Check for exception name
                Text customName = entity.getCustomName();
                if (customName != null && customName.getString().equals("Adventure Armour Stand")) {
                    return ActionResult.PASS; // Allow breaking excepted armor stands
                }

                // No individual config check needed for breaking armor stands
                // They can be broken if any armor stand protection is disabled
                if (AdventureProtectConfig.INSTANCE.DisableArmourStandRemoveItems) {
                    return ActionResult.FAIL;
                }
            }

            // Handle XercaPaint and CameraCapture entities
            ActionResult modEntityResult = handleModEntities(entity);
            if (modEntityResult != ActionResult.PASS) {
                return modEntityResult;
            }

            return ActionResult.PASS;
        });
    }

    private static ActionResult handleModEntities(net.minecraft.entity.Entity entity) {
        // Get the entity type identifier
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        String entityIdString = entityId.toString();

        // Check for XercaPaint easel entity
        if (entityIdString.contains("xercapaint") && entityIdString.contains("easel")) {
            // Check if easel has a canvas by looking for Item NBT data
            boolean hasCanvas;
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
                // Empty easel - check config to see if breaking is allowed
                if (AdventureProtectConfig.INSTANCE.DisableEaselInteraction) {
                    return ActionResult.FAIL; // Prevent breaking empty easel
                }
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
    }
}