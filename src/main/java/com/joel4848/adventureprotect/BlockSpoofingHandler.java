package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.registry.Registries;

import static com.joel4848.adventureprotect.Adventureprotect.LOGGER;

public class BlockSpoofingHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            if (serverPlayer.interactionManager.getGameMode() != GameMode.ADVENTURE) {
                return ActionResult.PASS;
            }

            if (!AdventureProtectConfig.INSTANCE.DisableXercaMusicInteraction) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            var blockState = world.getBlockState(pos);
            var block = blockState.getBlock();
            Identifier blockId = Registries.BLOCK.getId(block);
            String blockIdString = blockId.toString();

            // Check if this is a XercaMusic block we want to spoof
            if (isXercaMusicInteractiveBlock(blockIdString)) {
                LOGGER.info("[AdventureProtect] SPOOFING: Detected click on XercaMusic block: {} by player: {}",
                        blockIdString, serverPlayer.getName().getString());

                // Send feedback to player
                serverPlayer.sendMessage(Text.literal("§cMusical instruments are disabled in adventure mode!"), true);

                // Schedule the spoofing for a few ticks later to let any client-side processing happen first
                world.getServer().execute(() -> {
                    // Send fake block update to make client think the block is now air
                    var fakeAirState = Blocks.AIR.getDefaultState();
                    var blockUpdatePacket = new BlockUpdateS2CPacket(pos, fakeAirState);
                    serverPlayer.networkHandler.sendPacket(blockUpdatePacket);

                    LOGGER.debug("[AdventureProtect] Sent fake AIR block update to client for position: {}", pos);

                    // Schedule restoration of the real block after a very short delay
                    scheduleBlockRestoration(serverPlayer, pos, blockState, world, 3); // 3 ticks = ~150ms
                });

                // Don't prevent the interaction from the server perspective - let it happen
                // but spoof the client into thinking the block disappeared
                return ActionResult.PASS;
            }

            return ActionResult.PASS;
        });

        LOGGER.info("[AdventureProtect] Block spoofing handler registered");
    }

    private static void scheduleBlockRestoration(ServerPlayerEntity player, BlockPos pos,
                                                 net.minecraft.block.BlockState originalState,
                                                 net.minecraft.world.World world, int delayTicks) {

        // Create a simple counter-based delay system
        final int[] tickCounter = {0};

        // Create a recurring task that checks each tick
        Runnable restorationTask = new Runnable() {
            @Override
            public void run() {
                tickCounter[0]++;

                if (tickCounter[0] >= delayTicks) {
                    // Time to restore the block
                    try {
                        var realBlockUpdatePacket = new BlockUpdateS2CPacket(pos, originalState);
                        if (player.networkHandler != null) {
                            player.networkHandler.sendPacket(realBlockUpdatePacket);
                            LOGGER.debug("[AdventureProtect] Restored real block state to client for position: {}", pos);
                        }
                    } catch (Exception e) {
                        LOGGER.error("[AdventureProtect] Error during block restoration for position: {}: {}", pos, e.getMessage());
                    }
                } else {
                    // Schedule this task again for next tick
                    world.getServer().execute(this);
                }
            }
        };

        // Start the restoration timer
        world.getServer().execute(restorationTask);
    }

    private static boolean isXercaMusicInteractiveBlock(String blockIdString) {
        return blockIdString.equals("xercamusic:piano") ||
                blockIdString.equals("xercamusic:drum_kit") ||
                blockIdString.equals("xercamusic:music_box") ||
                blockIdString.equals("xercamusic:metronome");
    }
}