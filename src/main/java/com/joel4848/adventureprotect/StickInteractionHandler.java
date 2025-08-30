package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
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

        // Check if the block is a chest
        if (!(world.getBlockState(pos).getBlock() instanceof ChestBlock)) {
            player.sendMessage(Text.literal("§cThe Exceptionator only works on chests!"), false);
            return ActionResult.FAIL;
        }

        // Get the chest block entity
        var blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chestEntity)) {
            return ActionResult.FAIL;
        }

        // Check current custom name
        Text currentName = chestEntity.getCustomName();
        boolean hasException = currentName != null && currentName.getString().equals("Adventure Chest");

        // Execute the data command to modify the chest
        ServerCommandSource commandSource = serverPlayer.getCommandSource();
        String command;

        if (hasException) {
            // Remove the exception
            command = String.format("data modify block %d %d %d CustomName set value null", pos.getX(), pos.getY(), pos.getZ());
            player.sendMessage(Text.literal("§aRemoved adventure protection exception from chest"), false);
        } else {
            // Add the exception
            command = String.format("data modify block %d %d %d CustomName set value '\"Adventure Chest\"'", pos.getX(), pos.getY(), pos.getZ());
            player.sendMessage(Text.literal("§aAdded adventure protection exception to chest"), false);
        }

        // Execute the command
        try {
            var server = serverPlayer.getServer();
            if (server != null) {
                server.getCommandManager().executeWithPrefix(commandSource, command);
            } else {
                player.sendMessage(Text.literal("§cServer not available"), false);
                return ActionResult.FAIL;
            }
        } catch (Exception e) {
            player.sendMessage(Text.literal("§cError modifying chest data"), false);
            return ActionResult.FAIL;
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