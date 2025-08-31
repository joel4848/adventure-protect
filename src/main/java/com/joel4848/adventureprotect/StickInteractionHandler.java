package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StickInteractionHandler {

    public static void register() {
        UseBlockCallback.EVENT.register(StickInteractionHandler::onUseBlock);
        UseEntityCallback.EVENT.register(StickInteractionHandler::onUseEntity);
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        // Only work on server side
        if (world.isClient()) {
            return ActionResult.PASS;
        }

        // Must be a server player
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        // Check if they're holding our special stick
        ItemStack heldItem = player.getStackInHand(hand);
        if (!isExceptionator(heldItem)) {
            return ActionResult.PASS;
        }

        // Require shift-right-click (sneaking)
        if (!player.isSneaking()) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        var blockState = world.getBlockState(pos);
        var block = blockState.getBlock();

        String blockType = null;
        String exceptionName = null;
        String currentExceptionName = null;

        // Check if the block is a chest
        if (block instanceof ChestBlock) {
            blockType = "chest";
            exceptionName = "Adventure Chest";
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ChestBlockEntity chestEntity) {
                Text customName = chestEntity.getCustomName();
                if (customName != null) {
                    currentExceptionName = customName.getString();
                }
            }
        }
        // Check if the block is a barrel
        else if (block instanceof BarrelBlock) {
            blockType = "barrel";
            exceptionName = "Adventure Barrel";
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BarrelBlockEntity barrelEntity) {
                Text customName = barrelEntity.getCustomName();
                if (customName != null) {
                    currentExceptionName = customName.getString();
                }
            }
        }
        // Check if the block is a shulker box
        else if (block instanceof ShulkerBoxBlock) {
            blockType = "shulker box";
            exceptionName = "Adventure Shulker Box";
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ShulkerBoxBlockEntity shulkerEntity) {
                Text customName = shulkerEntity.getCustomName();
                if (customName != null) {
                    currentExceptionName = customName.getString();
                }
            }
        }
        // Check if the block is a note block
        else if (block instanceof NoteBlock) {
            blockType = "note block";
            exceptionName = "Adventure Note Block";
            // Note blocks don't support custom names in standard Minecraft
            currentExceptionName = null;
        }
        // Check if the block is a jukebox
        else if (block instanceof JukeboxBlock) {
            blockType = "jukebox";
            exceptionName = "Adventure Jukebox";
            // Jukeboxes don't support custom names in standard Minecraft
            currentExceptionName = null;
        }
        // Check if the block is a brewing stand
        else if (block instanceof BrewingStandBlock) {
            blockType = "brewing stand";
            exceptionName = "Adventure Brewing Stand";
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BrewingStandBlockEntity brewingEntity) {
                Text customName = brewingEntity.getCustomName();
                if (customName != null) {
                    currentExceptionName = customName.getString();
                }
            }
        }

        if (blockType == null) {
            player.sendMessage(Text.literal("§cThe Exceptionator doesn't work on this block type!"), false);
            return ActionResult.FAIL;
        }

        return processException(serverPlayer, pos, blockType, exceptionName, currentExceptionName);
    }

    private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, net.minecraft.entity.Entity entity, net.minecraft.util.hit.EntityHitResult hitResult) {
        // Only work on server side
        if (world.isClient()) {
            return ActionResult.PASS;
        }

        // Must be a server player
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        // Check if they're holding our special stick
        ItemStack heldItem = player.getStackInHand(hand);
        if (!isExceptionator(heldItem)) {
            return ActionResult.PASS;
        }

        // Require shift-right-click (sneaking)
        if (!player.isSneaking()) {
            return ActionResult.PASS;
        }

        String entityType = null;
        String exceptionName = null;
        String currentExceptionName = null;

        // Check if the entity is an item frame or glow item frame
        if (entity instanceof ItemFrameEntity || entity instanceof GlowItemFrameEntity) {
            entityType = "item frame";
            exceptionName = "Adventure Item Frame";
            Text customName = entity.getCustomName();
            if (customName != null) {
                currentExceptionName = customName.getString();
            }
        }
        // Check if the entity is an armor stand
        else if (entity instanceof ArmorStandEntity) {
            entityType = "armour stand";
            exceptionName = "Adventure Armour Stand";
            Text customName = entity.getCustomName();
            if (customName != null) {
                currentExceptionName = customName.getString();
            }
        }

        if (entityType == null) {
            player.sendMessage(Text.literal("§cThe Exceptionator doesn't work on this entity type!"), false);
            return ActionResult.FAIL;
        }

        return processEntityException(serverPlayer, entity, entityType, exceptionName, currentExceptionName);
    }

    private static ActionResult processException(ServerPlayerEntity player, BlockPos pos, String blockType, String exceptionName, String currentExceptionName) {
        boolean hasException = exceptionName.equals(currentExceptionName);

        // Execute the data command to modify the block
        ServerCommandSource commandSource = player.getCommandSource();
        String command;

        if (hasException) {
            // Remove the exception
            command = String.format("data modify block %d %d %d CustomName set value null", pos.getX(), pos.getY(), pos.getZ());
            player.sendMessage(Text.literal("§aRemoved exception from " + blockType), false);
        } else {
            // Add the exception
            command = String.format("data modify block %d %d %d CustomName set value '\"%s\"'", pos.getX(), pos.getY(), pos.getZ(), exceptionName);
            player.sendMessage(Text.literal("§aAdded exception to " + blockType), false);
        }

        // Execute the command
        try {
            var server = player.getServer();
            if (server != null) {
                server.getCommandManager().executeWithPrefix(commandSource, command);
            } else {
                player.sendMessage(Text.literal("§cServer not available"), false);
                return ActionResult.FAIL;
            }
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cError modifying block data"), false);
            return ActionResult.FAIL;
        }

        return ActionResult.SUCCESS;
    }

    private static ActionResult processEntityException(ServerPlayerEntity player, net.minecraft.entity.Entity entity, String entityType, String exceptionName, String currentExceptionName) {
        boolean hasException = exceptionName.equals(currentExceptionName);

        if (hasException) {
            // Remove the exception
            entity.setCustomName(null);
            player.sendMessage(Text.literal("§aRemoved exception from " + entityType), false);
        } else {
            // Add the exception
            entity.setCustomName(Text.literal(exceptionName));
            player.sendMessage(Text.literal("§aAdded exception to " + entityType), false);
        }

        return ActionResult.SUCCESS;
    }

    // Helper method to check if an item is our special exceptionator stick
    private static boolean isExceptionator(ItemStack stack) {
        if (stack.getItem() != Items.STICK) {
            return false;
        }

        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) {
            return false;
        }

        return customName.getString().contains("The Exceptionator");
    }
}