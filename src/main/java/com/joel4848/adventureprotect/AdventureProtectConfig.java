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

public class AdventureProtectConfig {
    public static final AdventureProtectConfig INSTANCE = new AdventureProtectConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("adventureprotect.json");
    private static WatchService watchService;
    private static CompletableFuture<Void> watchTask;

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

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                AdventureProtectConfig loaded = GSON.fromJson(json, AdventureProtectConfig.class);
                copyValues(loaded);
                System.out.println("[AdventureProtect] Config loaded successfully");
            } catch (IOException e) {
                System.err.println("Failed to load AdventureProtect config: " + e.getMessage());
                // Use defaults and save
                save();
            }
        } else {
            // Create default config file
            save();
        }

        // Start watching for config file changes
        startConfigWatcher();
    }

    public static void save() {
        try {
            String json = GSON.toJson(INSTANCE);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Failed to save AdventureProtect config: " + e.getMessage());
        }
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

    private static void startConfigWatcher() {
        try {
            watchService = CONFIG_PATH.getFileSystem().newWatchService();
            CONFIG_PATH.getParent().register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);

            watchTask = CompletableFuture.runAsync(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();

                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed.toString().equals("adventureprotect.json")) {
                                System.out.println("[AdventureProtect] Config file changed, reloading...");
                                // Small delay to ensure file write is complete
                                Thread.sleep(100);
                                load();
                            }
                        }

                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("[AdventureProtect] Error watching config file: " + e.getMessage());
                }
            });

            System.out.println("[AdventureProtect] Config file watcher started");

        } catch (IOException e) {
            System.err.println("[AdventureProtect] Failed to start config file watcher: " + e.getMessage());
        }
    }

    public static void stopConfigWatcher() {
        if (watchTask != null) {
            watchTask.cancel(true);
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                System.err.println("[AdventureProtect] Error closing config watcher: " + e.getMessage());
            }
        }
    }
}