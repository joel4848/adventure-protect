package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.text.Text;

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

                // If no exception name, check the game rule
                if (!world.getGameRules().getBoolean(Adventureprotect.CHEST)) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a barrel
            if (block instanceof BarrelBlock) {
                if (!world.getGameRules().getBoolean(Adventureprotect.BARREL)) {
                    return ActionResult.FAIL;
                }
            }

            // Check if it's a flower pot
            if (block instanceof FlowerPotBlock) {
                if (!world.getGameRules().getBoolean(Adventureprotect.FLOWERPOT)) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
    }
}