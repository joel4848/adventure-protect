package com.joel4848.adventureprotect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.nio.file.WatchKey;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.joel4848.adventureprotect.Adventureprotect.LOGGER;

public class AdventureProtectConfig {
    public static final AdventureProtectConfig INSTANCE = new AdventureProtectConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("adventureprotect.json");

    // Watcher & executor
    private static WatchService watchService;
    private static ScheduledExecutorService executor;
    // A pending scheduled reload (for debounce); cancelled/rescheduled on new events
    private static ScheduledFuture<?> pendingReload;

    // Config fields (unchanged - keeps JSON stable)
    public boolean DisableTrapdoorInteraction = true;
    public boolean DisableFlowerpotInteraction = true;
    public boolean DisableChestInteraction = true;
    public boolean DisableBarrelInteraction = true;
    public boolean DisableItemFrameInteraction = true;
    public boolean DisableEaselInteraction = true;
    public boolean DisablePlacedCanvasInteraction = true;
    public boolean DisablePlacedPhotographInteraction = true;
    public boolean DisablePaintingInteraction = true;
    public boolean DisableBrewingStandInteraction = true;
    public boolean DisableNoteBlockInteraction = true;
    public boolean DisableJukeboxInteraction = true;
    public boolean DisableDecoratedPotInteraction = true;
    public boolean DisableArmourStandRemoveItems = true;
    public boolean DisableArmourStandPlaceItems = true;
    public boolean DisableArmourStandReplaceItems = true;
    public boolean DisableShulkerBoxInteraction = true;
    public boolean DisableXercaMusicInteraction = true;

    /**
     * Public load entry used at mod init.
     * This reads the config file (if present) and starts the watcher (once).
     */
    public static void load() {
        // Read config once (safe read)
        performLoad();

        // Start watching for changes, but only once
        if (watchService == null) {
            startConfigWatcher();
        }
    }

    /**
     * Save current in-memory config to disk.
     */
    public static void save() {
        try {
            String json = GSON.toJson(INSTANCE);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("[AdventureProtect] Config saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("[AdventureProtect] Failed to save AdventureProtect config: {}", e.getMessage(), e);
        }
    }

    /**
     * Read the config file and copy values into the singleton instance.
     * This is used both at startup and by the watcher (via the scheduled reload).
     */
    private static void performLoad() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                AdventureProtectConfig loaded = GSON.fromJson(json, AdventureProtectConfig.class);
                copyValues(loaded);
                LOGGER.info("[AdventureProtect] Config loaded successfully from {}", CONFIG_PATH);
                return;
            } catch (IOException e) {
                LOGGER.error("[AdventureProtect] Failed to load config (will save defaults): {}", e.getMessage(), e);
                // fall through to save defaults
            } catch (Exception e) {
                LOGGER.error("[AdventureProtect] Error parsing config (will save defaults): {}", e.getMessage(), e);
                // fall through to save defaults
            }
        }

        // If file didn't exist or load failed, write defaults to disk
        save();
    }

    private static void copyValues(AdventureProtectConfig from) {
        if (from != null) {
            INSTANCE.DisableTrapdoorInteraction = from.DisableTrapdoorInteraction;
            INSTANCE.DisableFlowerpotInteraction = from.DisableFlowerpotInteraction;
            INSTANCE.DisableChestInteraction = from.DisableChestInteraction;
            INSTANCE.DisableBarrelInteraction = from.DisableBarrelInteraction;
            INSTANCE.DisableItemFrameInteraction = from.DisableItemFrameInteraction;
            INSTANCE.DisableEaselInteraction = from.DisableEaselInteraction;
            INSTANCE.DisablePlacedCanvasInteraction = from.DisablePlacedCanvasInteraction;
            INSTANCE.DisablePlacedPhotographInteraction = from.DisablePlacedPhotographInteraction;
            INSTANCE.DisablePaintingInteraction = from.DisablePaintingInteraction;
            INSTANCE.DisableBrewingStandInteraction = from.DisableBrewingStandInteraction;
            INSTANCE.DisableNoteBlockInteraction = from.DisableNoteBlockInteraction;
            INSTANCE.DisableJukeboxInteraction = from.DisableJukeboxInteraction;
            INSTANCE.DisableDecoratedPotInteraction = from.DisableDecoratedPotInteraction;
            INSTANCE.DisableArmourStandRemoveItems = from.DisableArmourStandRemoveItems;
            INSTANCE.DisableArmourStandPlaceItems = from.DisableArmourStandPlaceItems;
            INSTANCE.DisableArmourStandReplaceItems = from.DisableArmourStandReplaceItems;
            INSTANCE.DisableShulkerBoxInteraction = from.DisableShulkerBoxInteraction;
            INSTANCE.DisableXercaMusicInteraction = from.DisableXercaMusicInteraction;
        }
    }

    /**
     * Start a single WatchService and a single-thread executor to debounce reloads.
     * This method is safe to call only once; load() ensures it is only invoked when watchService == null.
     */
    private static void startConfigWatcher() {
        try {
            watchService = CONFIG_PATH.getFileSystem().newWatchService();
            CONFIG_PATH.getParent().register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);

            // Single-thread executor for debounce tasks and for the watch loop
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "AdventureProtect-ConfigWatcher");
                t.setDaemon(true);
                return t;
            });

            // Run the blocking watcher loop on the executor
            executor.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        WatchKey key;
                        try {
                            key = watchService.take(); // blocks
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed != null && changed.toString().equals(CONFIG_PATH.getFileName().toString())) {
                                LOGGER.info("[AdventureProtect] Detected config change event: {} - scheduling reload...", event.kind());

                                // Debounce: cancel any pending reload and schedule a new one 100 ms later
                                if (pendingReload != null && !pendingReload.isDone()) {
                                    pendingReload.cancel(false);
                                }

                                // Schedule the reload after short delay to allow the file write to finish
                                pendingReload = executor.schedule(() -> {
                                    try {
                                        performLoad();
                                    } catch (Exception e) {
                                        LOGGER.error("[AdventureProtect] Error reloading config: {}", e.getMessage(), e);
                                    }
                                }, 100, TimeUnit.MILLISECONDS);
                            }
                        }

                        key.reset();
                    }
                } catch (Exception e) {
                    // Catch any unexpected exceptions to keep watcher thread alive behavior predictable
                    LOGGER.error("[AdventureProtect] Config watcher terminated due to unexpected error: {}", e.getMessage(), e);
                }
            });

            LOGGER.info("[AdventureProtect] Config file watcher started for {}", CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("[AdventureProtect] Failed to start config file watcher: {}", e.getMessage(), e);
            // Clean up partial start
            stopConfigWatcher();
        }
    }

    /**
     * Stop the watcher and executor cleanly. Called by shutdown hook.
     */
    public static void stopConfigWatcher() {
        // Cancel pending reload
        if (pendingReload != null) {
            try {
                pendingReload.cancel(false);
            } catch (Exception ignored) { }
            pendingReload = null;
        }

        // Shutdown executor
        if (executor != null) {
            try {
                executor.shutdownNow();
            } catch (Exception e) {
                LOGGER.error("[AdventureProtect] Error shutting down config watcher executor: {}", e.getMessage(), e);
            } finally {
                executor = null;
            }
        }

        // Close watch service
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                LOGGER.error("[AdventureProtect] Error closing config watcher: {}", e.getMessage(), e);
            } finally {
                watchService = null;
            }
        }

        LOGGER.info("[AdventureProtect] Config watcher stopped");
    }
}
