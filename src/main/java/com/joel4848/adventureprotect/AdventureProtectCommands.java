package com.joel4848.adventureprotect;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.joel4848.adventureprotect.Adventureprotect.LOGGER;

public class AdventureProtectCommands {

    /**
     * Register method used by Fabric's CommandRegistrationCallback.
     * Matches (dispatcher, registryAccess, environment).
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {

        LOGGER.info("[AdventureProtect] Registering commands; environment = {}", environment);

        // Main root: /adventureprotect
        var mainCommand = CommandManager.literal("adventureprotect")
                .requires(source -> source.hasPermissionLevel(2)); // OP level 2 required

        // /adventureprotect exceptionator
        mainCommand.then(
                CommandManager.literal("exceptionator")
                        .executes(ctx -> {
                            LOGGER.info("[AdventureProtect] Executing exceptionator command by {}",
                                    safeGetPlayerName(ctx));
                            return giveExceptionator(ctx);
                        })
        );

        // Build /adventureprotect protect <type> <true|false>
        var protectCommand = CommandManager.literal("protect");

        // Map: literal used in command -> internal protection key used by updateConfigProtection
        // Keep insertion order for predictable tab completion ordering.
        Map<String, String> protectMap = new LinkedHashMap<>();
        protectMap.put("trapdoors", "trapdoors");
        protectMap.put("flowerpots", "flowerpots");
        protectMap.put("chests", "chests");
        protectMap.put("barrels", "barrels");
        protectMap.put("itemFrames", "itemFrames");
        protectMap.put("jopEasels", "jopEasels");
        protectMap.put("jopCanvases", "jopCanvases");
        protectMap.put("camerapturePhotographs", "camerapturePhotographs");
        protectMap.put("paintings", "paintings");
        protectMap.put("brewingStands", "brewingStands");
        protectMap.put("noteBlocks", "noteBlocks");
        protectMap.put("jukeBoxes", "jukeBoxes");
        protectMap.put("decoratedPots", "decoratedPots");
        protectMap.put("armourStandsRemove", "armourStandsRemove");
        protectMap.put("armourStandsPlace", "armourStandsPlace");
        protectMap.put("armourStandsReplace", "armourStandsReplace");
        protectMap.put("shulkerBoxes", "shulkerBoxes");
        // corrected: musicModBlocks (no underscore)
        protectMap.put("musicModBlocks", "musicModBlocks");

        // Build each protection literal under /adventureprotect protect <literal> [enabled]
        for (Map.Entry<String, String> entry : protectMap.entrySet()) {
            final String literal = entry.getKey();
            final String protectionKey = entry.getValue();

            // Create node for literal
            var literalNode = CommandManager.literal(literal)
                    // When user supplies the boolean argument: set the value
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes(ctx -> {
                                boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                LOGGER.info("[AdventureProtect] Command /adventureprotect protect {} executed by {} with enabled={}",
                                        protectionKey, safeGetPlayerName(ctx), enabled);
                                return setProtection(ctx, protectionKey);
                            })
                    )
                    // When user omits the boolean: show the current value
                    .executes(ctx -> {
                        boolean current = getProtectionValue(protectionKey);
                        String enabledText = current ? "§aenabled" : "§cdisabled";
                        String friendly = getFriendlyProtectionName(protectionKey);

                        // Send feedback only to command source (don't broadcast to ops)
                        ctx.getSource().sendFeedback(() ->
                                Text.literal("§7Protection for §f" + friendly + " §7is " + enabledText), false);

                        LOGGER.info("[AdventureProtect] Queried protection {} -> {} by {}",
                                protectionKey, current, safeGetPlayerName(ctx));
                        return 1;
                    });

            protectCommand.then(literalNode);
            LOGGER.debug("[AdventureProtect] Added protect subcommand: {}", literal);
        }

        // attach protect branch to main command
        mainCommand.then(protectCommand);

        // Finally register the root command into dispatcher
        dispatcher.register(mainCommand);
        LOGGER.info("[AdventureProtect] Registered /adventureprotect root (including protect subtree).");
    }

    // Helper to safely get the player's name for logging (does not throw if command run from console)
    private static String safeGetPlayerName(CommandContext<ServerCommandSource> ctx) {
        try {
            // Use getString() on Text to get the plain player name string
            return ctx.getSource().getPlayerOrThrow().getName().getString();
        } catch (Exception e) {
            return "CONSOLE";
        }
    }

    private static int giveExceptionator(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();

            // Create a stick with special NBT to identify it as the exceptionator
            ItemStack exceptionator = new ItemStack(Items.STICK);
            exceptionator.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§6The Exceptionator"));
            exceptionator.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

            // Create the lore component properly
            LoreComponent lore = new LoreComponent(java.util.List.of(
                    Text.literal("§7Adventure Protection Tool"),
                    Text.literal("§7Right-click chests to toggle exceptions")
            ));
            exceptionator.set(DataComponentTypes.LORE, lore);

            // Give it to the player
            boolean success = player.getInventory().insertStack(exceptionator);

            if (success) {
                player.sendMessage(Text.literal("§aYou have been given The Exceptionator!"), false);
                player.sendMessage(Text.literal("§7Right-click on chests to toggle adventure protection exceptions"), false);
                return 1;
            } else {
                player.sendMessage(Text.literal("§cYour inventory is full!"), false);
                return 0;
            }

        } catch (Exception e) {
            source.sendError(Text.literal("This command can only be used by players"));
            return 0;
        }
    }

    private static int setProtection(CommandContext<ServerCommandSource> context, String protectionType) {
        ServerCommandSource source = context.getSource();
        boolean enabled = BoolArgumentType.getBool(context, "enabled");

        try {
            // Update the config based on protection type
            boolean success = updateConfigProtection(protectionType, enabled);

            if (success) {
                // Save the config to disk
                AdventureProtectConfig.save();

                String enabledText = enabled ? "§aenabled" : "§cdisabled";
                String friendlyName = getFriendlyProtectionName(protectionType);

                // Mirror your original behaviour: feedback to command source and log
                source.sendFeedback(() -> Text.literal("§7Protection for §f" + friendlyName + " §7has been " + enabledText), true);
                LOGGER.info("[AdventureProtect] {} protection set to {} by {}",
                        protectionType, enabled, safeGetPlayerName(context));
                return 1;
            } else {
                source.sendError(Text.literal("Unknown protection type: " + protectionType));
                LOGGER.warn("[AdventureProtect] Attempted to set unknown protection type: {} by {}", protectionType, safeGetPlayerName(context));
                return 0;
            }

        } catch (Exception e) {
            source.sendError(Text.literal("Error updating protection: " + e.getMessage()));
            LOGGER.error("[AdventureProtect] Error updating protection {}: {}", protectionType, e.getMessage(), e);
            return 0;
        }
    }

    // Map the camelCase protectionType keys to your config fields (fields unchanged)
    private static boolean updateConfigProtection(String protectionType, boolean enabled) {
        switch (protectionType) {
            case "trapdoors":
                AdventureProtectConfig.INSTANCE.DisableTrapdoorInteraction = enabled;
                return true;
            case "flowerpots":
                AdventureProtectConfig.INSTANCE.DisableFlowerpotInteraction = enabled;
                return true;
            case "chests":
                AdventureProtectConfig.INSTANCE.DisableChestInteraction = enabled;
                return true;
            case "barrels":
                AdventureProtectConfig.INSTANCE.DisableBarrelInteraction = enabled;
                return true;
            case "itemFrames":
                AdventureProtectConfig.INSTANCE.DisableItemFrameInteraction = enabled;
                return true;
            case "jopEasels":
                AdventureProtectConfig.INSTANCE.DisableEaselInteraction = enabled;
                return true;
            case "jopCanvases":
                AdventureProtectConfig.INSTANCE.DisablePlacedCanvasInteraction = enabled;
                return true;
            case "camerapturePhotographs":
                AdventureProtectConfig.INSTANCE.DisablePlacedPhotographInteraction = enabled;
                return true;
            case "paintings":
                AdventureProtectConfig.INSTANCE.DisablePaintingInteraction = enabled;
                return true;
            case "brewingStands":
                AdventureProtectConfig.INSTANCE.DisableBrewingStandInteraction = enabled;
                return true;
            case "noteBlocks":
                AdventureProtectConfig.INSTANCE.DisableNoteBlockInteraction = enabled;
                return true;
            case "jukeBoxes":
                AdventureProtectConfig.INSTANCE.DisableJukeboxInteraction = enabled;
                return true;
            case "decoratedPots":
                AdventureProtectConfig.INSTANCE.DisableDecoratedPotInteraction = enabled;
                return true;
            case "armourStandsRemove":
                AdventureProtectConfig.INSTANCE.DisableArmourStandRemoveItems = enabled;
                return true;
            case "armourStandsPlace":
                AdventureProtectConfig.INSTANCE.DisableArmourStandPlaceItems = enabled;
                return true;
            case "armourStandsReplace":
                AdventureProtectConfig.INSTANCE.DisableArmourStandReplaceItems = enabled;
                return true;
            case "shulkerBoxes":
                AdventureProtectConfig.INSTANCE.DisableShulkerBoxInteraction = enabled;
                return true;
            case "musicModBlocks":
                AdventureProtectConfig.INSTANCE.DisableXercaMusicInteraction = enabled;
                return true;
            default:
                return false;
        }
    }

    // New helper: return current boolean value for a protectionType
    private static boolean getProtectionValue(String protectionType) {
        switch (protectionType) {
            case "trapdoors": return AdventureProtectConfig.INSTANCE.DisableTrapdoorInteraction;
            case "flowerpots": return AdventureProtectConfig.INSTANCE.DisableFlowerpotInteraction;
            case "chests": return AdventureProtectConfig.INSTANCE.DisableChestInteraction;
            case "barrels": return AdventureProtectConfig.INSTANCE.DisableBarrelInteraction;
            case "itemFrames": return AdventureProtectConfig.INSTANCE.DisableItemFrameInteraction;
            case "jopEasels": return AdventureProtectConfig.INSTANCE.DisableEaselInteraction;
            case "jopCanvases": return AdventureProtectConfig.INSTANCE.DisablePlacedCanvasInteraction;
            case "camerapturePhotographs": return AdventureProtectConfig.INSTANCE.DisablePlacedPhotographInteraction;
            case "paintings": return AdventureProtectConfig.INSTANCE.DisablePaintingInteraction;
            case "brewingStands": return AdventureProtectConfig.INSTANCE.DisableBrewingStandInteraction;
            case "noteBlocks": return AdventureProtectConfig.INSTANCE.DisableNoteBlockInteraction;
            case "jukeBoxes": return AdventureProtectConfig.INSTANCE.DisableJukeboxInteraction;
            case "decoratedPots": return AdventureProtectConfig.INSTANCE.DisableDecoratedPotInteraction;
            case "armourStandsRemove": return AdventureProtectConfig.INSTANCE.DisableArmourStandRemoveItems;
            case "armourStandsPlace": return AdventureProtectConfig.INSTANCE.DisableArmourStandPlaceItems;
            case "armourStandsReplace": return AdventureProtectConfig.INSTANCE.DisableArmourStandReplaceItems;
            case "shulkerBoxes": return AdventureProtectConfig.INSTANCE.DisableShulkerBoxInteraction;
            case "musicModBlocks": return AdventureProtectConfig.INSTANCE.DisableXercaMusicInteraction;
            default: return false;
        }
    }

    private static String getFriendlyProtectionName(String protectionType) {
        switch (protectionType) {
            case "trapdoors": return "Trapdoors";
            case "flowerpots": return "Flower Pots";
            case "chests": return "Chests";
            case "barrels": return "Barrels";
            case "itemFrames": return "Item Frames";
            case "jopEasels": return "JOP Easels";
            case "jopCanvases": return "JOP Canvases";
            case "camerapturePhotographs": return "Camerapture Photographs";
            case "paintings": return "Paintings";
            case "brewingStands": return "Brewing Stands";
            case "noteBlocks": return "Note Blocks";
            case "jukeBoxes": return "Juke Boxes";
            case "decoratedPots": return "Decorated Pots";
            case "armourStandsRemove": return "Armour Stands (Remove Items)";
            case "armourStandsPlace": return "Armour Stands (Place Items)";
            case "armourStandsReplace": return "Armour Stands (Replace Items)";
            case "shulkerBoxes": return "Shulker Boxes";
            case "musicModBlocks": return "Music Mod Blocks";
            default: return protectionType;
        }
    }
}
