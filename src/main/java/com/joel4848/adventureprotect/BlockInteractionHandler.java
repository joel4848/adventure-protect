package com.joel4848.adventureprotect;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import static com.joel4848.adventureprotect.Adventureprotect.LOGGER;

public class BlockInteractionHandler {

    // Tunable timings (ticks). Default values are tighter to reduce GUI flash:
    // - SECOND_CLOSE_TICK: extra close immediately (0)
    // - THIRD_CLOSE_TICK: next tick (1)
    // - RESTORE_TICKS: restore after this many ticks (3) ~= 150ms
    private static final int SECOND_CLOSE_TICK = 0;
    private static final int THIRD_CLOSE_TICK  = 1;
    private static final int RESTORE_TICKS     = 3;

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

            // ---------- Existing protection checks (unchanged) ----------
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
            // ---------- End existing protections ----------

            // XercaMusic special handling:
            // Piano & drum_kit require spoofing because the GUI opens client-side immediately.
            Identifier blockId = Registries.BLOCK.getId(block);
            String blockIdString = blockId.toString();

            // If it's a placed piano or drum kit and the config flag is set, perform spoof+close+restore
            if (AdventureProtectConfig.INSTANCE.DisableXercaMusicInteraction &&
                    (blockIdString.equals("xercamusic:piano") || blockIdString.equals("xercamusic:drum_kit"))) {

                LOGGER.info("[AdventureProtect] Detected click on XercaMusic instrument: {} by player: {}",
                        blockIdString, serverPlayer.getName().getString());

                // Tell the player
                serverPlayer.sendMessage(Text.literal("§cMusical instruments are disabled in adventure mode!"), true);

                // Execute the spoof/close/restore sequence on server thread
                World finalWorld = world;
                BlockPos finalPos = pos;
                var originalState = blockState;

                finalWorld.getServer().execute(() -> {
                    try {
                        // Step 1: Immediate spoof to AIR
                        serverPlayer.networkHandler.sendPacket(new BlockUpdateS2CPacket(finalPos, Blocks.AIR.getDefaultState()));
                        LOGGER.info("[AdventureProtect] Sent fake AIR block update to {} at {}", serverPlayer.getName().getString(), finalPos);

                        // Step 2: Immediate CloseScreen (use syncId if available)
                        int syncId = serverPlayer.currentScreenHandler != null ? serverPlayer.currentScreenHandler.syncId : 0;
                        serverPlayer.networkHandler.sendPacket(new CloseScreenS2CPacket(syncId));
                        LOGGER.info("[AdventureProtect] Sent immediate CloseScreen (syncId={}) to {}", syncId, serverPlayer.getName().getString());

                        // Step 3: Schedule staged packets and restore with tighter timing
                        scheduleStagedPackets(serverPlayer, finalPos, originalState, finalWorld,
                                SECOND_CLOSE_TICK, THIRD_CLOSE_TICK, RESTORE_TICKS);

                    } catch (Exception e) {
                        LOGGER.error("[AdventureProtect] Error during instrument spoof sequence for {} at {}: {}", serverPlayer.getName().getString(), finalPos, e.getMessage());
                    }
                });

                // Tell Fabric the use was blocked server-side (still client may briefly open GUI)
                return ActionResult.FAIL;
            }

            // Check for XercaMusic blocks that are safe to block server-side normally (music_box, metronome)
            if (blockIdString.contains("xercamusic") &&
                    (blockIdString.contains("music_box") || blockIdString.contains("metronome"))) {
                if (AdventureProtectConfig.INSTANCE.DisableXercaMusicInteraction) {
                    return ActionResult.FAIL;
                }
            }

            return ActionResult.PASS;
        });
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
                                              World world,
                                              int secondCloseTick,
                                              int thirdCloseTick,
                                              int restoreTicks) {
        final int[] tick = {0};

        Runnable task = new Runnable() {
            @Override
            public void run() {
                // stop early if player unreachable
                if (player.networkHandler == null || player.getServer() == null) {
                    LOGGER.info("[AdventureProtect] Player unreachable during staged spoof/restore: {}", player.getName().getString());
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

        // start next tick
        world.getServer().execute(task);
    }

    private static boolean isXercaMusicInteractiveBlock(String blockIdString) {
        return blockIdString.equals("xercamusic:piano") ||
                blockIdString.equals("xercamusic:drum_kit") ||
                blockIdString.equals("xercamusic:music_box") ||
                blockIdString.equals("xercamusic:metronome");
    }
}
