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
    private static final String CONFIG_HEADER = """
            // AdventureProtect Configuration
            """;

    public boolean DisableTrapdoorInteraction = true;
    public boolean DisableFlowerpotInteraction = true;
    public boolean DisableChestInteraction = true;
    public boolean DisableBarrelInteraction = true;
    public boolean DisableItemFrameInteraction = true;
    public boolean DisableEaselInteraction = true;
    public boolean DisablePlacedCanvasInteraction = true;
    public boolean DisablePlacedPhotographInteraction = true;

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                AdventureProtectConfig loaded = GSON.fromJson(json, AdventureProtectConfig.class);
                copyValues(loaded, INSTANCE);
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

    private static void copyValues(AdventureProtectConfig from, AdventureProtectConfig to) {
        if (from != null) {
            to.DisableTrapdoorInteraction = from.DisableTrapdoorInteraction;
            to.DisableFlowerpotInteraction = from.DisableFlowerpotInteraction;
            to.DisableChestInteraction = from.DisableChestInteraction;
            to.DisableBarrelInteraction = from.DisableBarrelInteraction;
            to.DisableItemFrameInteraction = from.DisableItemFrameInteraction;
            to.DisableEaselInteraction = from.DisableEaselInteraction;
            to.DisablePlacedCanvasInteraction = from.DisablePlacedCanvasInteraction;
            to.DisablePlacedPhotographInteraction = from.DisablePlacedPhotographInteraction;
        }
    }
}