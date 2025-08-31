package com.joel4848.adventureprotect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AdventureProtectConfig {
    public static final AdventureProtectConfig INSTANCE = new AdventureProtectConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("adventureprotect.json");

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
            } catch (IOException e) {
                System.err.println("Failed to load AdventureProtect config: " + e.getMessage());
                // Use defaults and save
                save();
            }
        } else {
            // Create default config file
            save();
        }
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
}