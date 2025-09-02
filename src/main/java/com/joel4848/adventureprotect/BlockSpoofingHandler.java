package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.registry.Registries;

import static com.joel4848.adventureprotect.Adventureprotect.LOGGER;

public class BlockSpoofingHandler {

    // Tunables: adjust as needed for your server
    private static final int SECOND_CLOSE_TICK = 1; // send an extra close at tick 1
    private static final int THIRD_CLOSE_TICK  = 3; // send an extra close at tick 3
    private static final int RESTORE_TICKS     = 5; // restore block at tick 5

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

            if (isXercaMusicInteractiveBlock(blockIdString)) {
                LOGGER.info("[AdventureProtect] Detected click on XercaMusic block: {} by player: {}",
                        blockIdString, serverPlayer.getName().getString());

                // Inform player
                serverPlayer.sendMessage(Text.literal("§cMusical instruments are disabled in adventure mode!"), true);

                // Run the spoof/close/restore sequence on the server thread
                world.getServer().execute(() -> {
                    try {
                        // Step 1: Immediately spoof to air
                        serverPlayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, Blocks.AIR.getDefaultState()));
                        LOGGER.info("[AdventureProtect] Sent fake AIR block update to {} at {}", serverPlayer.getName().getString(), pos);

                        // Step 2: Immediately send a CloseScreen packet (belt-and-suspenders)
                        int syncId = serverPlayer.currentScreenHandler != null ? serverPlayer.currentScreenHandler.syncId : 0;
                        serverPlayer.networkHandler.sendPacket(new CloseScreenS2CPacket(syncId));
                        LOGGER.info("[AdventureProtect] Sent immediate CloseScreen (syncId={}) to {}", syncId, serverPlayer.getName().getString());

                        // Step 3: Schedule staged follow-ups (second/third close + restore)
                        scheduleStagedPackets(serverPlayer, pos, blockState, world,
                                SECOND_CLOSE_TICK, THIRD_CLOSE_TICK, RESTORE_TICKS);

                    } catch (Exception e) {
                        LOGGER.error("[AdventureProtect] Error in spoof sequence for {} at {}: {}", serverPlayer.getName().getString(), pos, e.getMessage());
                    }
                });

                // Tell Fabric that the use was blocked server-side
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });

        LOGGER.info("[AdventureProtect] Block spoofing handler registered");
    }

    /**
     * Schedules staged follow-up packets on the server thread:
     * - sends CloseScreen at tick secondCloseTick
     * - sends CloseScreen at tick thirdCloseTick
     * - restores block and sends final CloseScreen at tick restoreTicks
     */
    private static void scheduleStagedPackets(ServerPlayerEntity player,
                                              BlockPos pos,
                                              net.minecraft.block.BlockState originalState,
                                              net.minecraft.world.World world,
                                              int secondCloseTick,
                                              int thirdCloseTick,
                                              int restoreTicks) {
        final int[] tick = {0};

        Runnable task = new Runnable() {
            @Override
            public void run() {
                // stop if player disconnected / unreachable
                if (player.networkHandler == null || player.getServer() == null) {
                    LOGGER.info("[AdventureProtect] Player disconnected or not reachable during spoof restore: {}", player.getName().getString());
                    return;
                }

                tick[0]++;

                try {
                    if (tick[0] == secondCloseTick) {
                        int syncId = player.currentScreenHandler != null ? player.currentScreenHandler.syncId : 0;
                        player.networkHandler.sendPacket(new CloseScreenS2CPacket(syncId));
                        LOGGER.info("[AdventureProtect] Sent staged CloseScreen (tick {}) to {} (syncId={})", secondCloseTick, player.getName().getString(), syncId);
                    }

                    if (tick[0] == thirdCloseTick) {
                        int syncId = player.currentScreenHandler != null ? player.currentScreenHandler.syncId : 0;
                        player.networkHandler.sendPacket(new CloseScreenS2CPacket(syncId));
                        LOGGER.info("[AdventureProtect] Sent staged CloseScreen (tick {}) to {} (syncId={})", thirdCloseTick, player.getName().getString(), syncId);
                    }

                    if (tick[0] >= restoreTicks) {
                        // Restore the real block state and send final CloseScreen
                        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, originalState));
                        int syncId = player.currentScreenHandler != null ? player.currentScreenHandler.syncId : 0;
                        player.networkHandler.sendPacket(new CloseScreenS2CPacket(syncId));
                        LOGGER.info("[AdventureProtect] Restored real block and sent final CloseScreen to {} at {} (syncId={})",
                                player.getName().getString(), pos, syncId);
                        // done, don't re-enqueue
                        return;
                    }
                } catch (Exception e) {
                    LOGGER.error("[AdventureProtect] Exception during staged spoof/restore for {} at {}: {}", player.getName().getString(), pos, e.getMessage());
                    // continue scheduling to attempt restoration
                }

                // re-schedule for next tick
                world.getServer().execute(this);
            }
        };

        // start on next tick
        world.getServer().execute(task);
    }

    private static boolean isXercaMusicInteractiveBlock(String blockIdString) {
        return blockIdString.equals("xercamusic:piano") ||
                blockIdString.equals("xercamusic:drum_kit") ||
                blockIdString.equals("xercamusic:music_box") ||
                blockIdString.equals("xercamusic:metronome");
    }
}
