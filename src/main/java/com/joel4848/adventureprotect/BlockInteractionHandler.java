package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class BlockInteractionHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            if (serverPlayer.interactionManager.getGameMode() != GameMode.ADVENTURE) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            var blockState = world.getBlockState(pos);
            var block = blockState.getBlock();

            // Check if it's a chest
            if (block instanceof ChestBlock) {
                // Check if this chest has the custom name "Adventure Chest"
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ChestBlockEntity chestEntity) {
                    Text customName = chestEntity.getCustomName();
                    if (customName != null && customName.getString().equals("Adventure Chest")) {
                        return ActionResult.PASS; // Allow interaction with named chests
                    }
                }

                // If no exception name, check the config
                if (AdventureProtectConfig.INSTANCE.DisableChestInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a barrel
            if (block instanceof BarrelBlock) {
                // Check for exception name
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof BarrelBlockEntity barrelEntity) {
                    Text customName = barrelEntity.getCustomName();
                    if (customName != null && customName.getString().equals("Adventure Barrel")) {
                        return ActionResult.PASS;
                    }
                }

                if (AdventureProtectConfig.INSTANCE.DisableBarrelInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a flower pot
            if (block instanceof FlowerPotBlock) {
                if (AdventureProtectConfig.INSTANCE.DisableFlowerpotInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a trapdoor
            if (block instanceof TrapdoorBlock) {
                if (AdventureProtectConfig.INSTANCE.DisableTrapdoorInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a brewing stand
            if (block instanceof BrewingStandBlock) {
                // Check for exception name
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof BrewingStandBlockEntity brewingEntity) {
                    Text customName = brewingEntity.getCustomName();
                    if (customName != null && customName.getString().equals("Adventure Brewing Stand")) {
                        return ActionResult.PASS;
                    }
                }

                if (AdventureProtectConfig.INSTANCE.DisableBrewingStandInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a note block
            if (block instanceof NoteBlock) {
                // Note blocks don't have custom names, so no exception handling
                if (AdventureProtectConfig.INSTANCE.DisableNoteBlockInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a jukebox
            if (block instanceof JukeboxBlock) {
                // Jukeboxes don't have custom names in standard Minecraft, so no exception handling
                if (AdventureProtectConfig.INSTANCE.DisableJukeboxInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a decorated pot
            if (block instanceof DecoratedPotBlock) {
                if (AdventureProtectConfig.INSTANCE.DisableDecoratedPotInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a shulker box (any color)
            if (block instanceof ShulkerBoxBlock) {
                // Check for exception name
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ShulkerBoxBlockEntity shulkerEntity) {
                    Text customName = shulkerEntity.getCustomName();
                    if (customName != null && customName.getString().equals("Adventure Shulker Box")) {
                        return ActionResult.PASS;
                    }
                }

                if (AdventureProtectConfig.INSTANCE.DisableShulkerBoxInteraction) {
                    return ActionResult.FAIL;
                }
            }

            // Check for XercaMusic blocks that work with standard server-side blocking
            // Note: Piano and drum_kit are handled by BlockSpoofingHandler due to client-side GUI issues
            Identifier blockId = Registries.BLOCK.getId(block);
            String blockIdString = blockId.toString();
            if (blockIdString.contains("xercamusic") &&
                    (blockIdString.contains("music_box") || blockIdString.contains("metronome"))) {
                if (AdventureProtectConfig.INSTANCE.DisableXercaMusicInteraction) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
}