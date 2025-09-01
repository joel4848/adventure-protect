package com.joel4848.adventureprotect;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AdventureProtectCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, net.minecraft.command.CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("adventureprotect")
                .then(CommandManager.literal("exceptionator")
                        .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
                        .executes(AdventureProtectCommands::giveExceptionator))
                .then(CommandManager.literal("protect")
                        .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
                        .then(CommandManager.literal("trapdoors")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "trapdoors", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("flowerpots")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "flowerpots", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("chests")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "chests", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("barrels")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "barrels", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("item_frames")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "item_frames", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("jop_easels")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "jop_easels", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("jop_canvases")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "jop_canvases", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("camerapture_photographs")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "camerapture_photographs", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("paintings")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "paintings", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("brewing_stands")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "brewing_stands", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("note_blocks")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "note_blocks", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("juke_boxes")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "juke_boxes", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("decorated_pots")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "decorated_pots", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("armour_stands_remove")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "armour_stands_remove", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("armour_stands_place")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "armour_stands_place", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("armour_stands_replace")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "armour_stands_replace", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("shulker_boxes")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "shulker_boxes", BoolArgumentType.getBool(ctx, "enabled")))))
                        .then(CommandManager.literal("music_mod_blocks")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProtection(ctx, "music_mod_blocks", BoolArgumentType.getBool(ctx, "enabled")))))));
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

    private static int setProtection(CommandContext<ServerCommandSource> context, String protectionType, boolean enabled) {
        ServerCommandSource source = context.getSource();

        try {
            // Update the config based on protection type
            boolean success = updateConfigProtection(protectionType, enabled);

            if (success) {
                // Save the config to disk
                AdventureProtectConfig.save();

                String enabledText = enabled ? "§aenabled" : "§cdisabled";
                String friendlyName = getFriendlyProtectionName(protectionType);

                source.sendFeedback(() -> Text.literal("§7Protection for §f" + friendlyName + " §7has been " + enabledText), true);
                return 1;
            } else {
                source.sendError(Text.literal("Unknown protection type: " + protectionType));
                return 0;
            }

        } catch (Exception e) {
            source.sendError(Text.literal("Error updating protection: " + e.getMessage()));
            return 0;
        }
    }

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
            case "item_frames":
                AdventureProtectConfig.INSTANCE.DisableItemFrameInteraction = enabled;
                return true;
            case "jop_easels":
                AdventureProtectConfig.INSTANCE.DisableEaselInteraction = enabled;
                return true;
            case "jop_canvases":
                AdventureProtectConfig.INSTANCE.DisablePlacedCanvasInteraction = enabled;
                return true;
            case "camerapture_photographs":
                AdventureProtectConfig.INSTANCE.DisablePlacedPhotographInteraction = enabled;
                return true;
            case "paintings":
                AdventureProtectConfig.INSTANCE.DisablePaintingInteraction = enabled;
                return true;
            case "brewing_stands":
                AdventureProtectConfig.INSTANCE.DisableBrewingStandInteraction = enabled;
                return true;
            case "note_blocks":
                AdventureProtectConfig.INSTANCE.DisableNoteBlockInteraction = enabled;
                return true;
            case "juke_boxes":
                AdventureProtectConfig.INSTANCE.DisableJukeboxInteraction = enabled;
                return true;
            case "decorated_pots":
                AdventureProtectConfig.INSTANCE.DisableDecoratedPotInteraction = enabled;
                return true;
            case "armour_stands_remove":
                AdventureProtectConfig.INSTANCE.DisableArmourStandRemoveItems = enabled;
                return true;
            case "armour_stands_place":
                AdventureProtectConfig.INSTANCE.DisableArmourStandPlaceItems = enabled;
                return true;
            case "armour_stands_replace":
                AdventureProtectConfig.INSTANCE.DisableArmourStandReplaceItems = enabled;
                return true;
            case "shulker_boxes":
                AdventureProtectConfig.INSTANCE.DisableShulkerBoxInteraction = enabled;
                return true;
            case "music_mod_blocks":
                AdventureProtectConfig.INSTANCE.DisableXercaMusicInteraction = enabled;
                return true;
            default:
                return false;
        }
    }

    private static String getFriendlyProtectionName(String protectionType) {
        switch (protectionType) {
            case "trapdoors": return "Trapdoors";
            case "flowerpots": return "Flower Pots";
            case "chests": return "Chests";
            case "barrels": return "Barrels";
            case "item_frames": return "Item Frames";
            case "jop_easels": return "JOP Easels";
            case "jop_canvases": return "JOP Canvases";
            case "camerapture_photographs": return "Camerapture Photographs";
            case "paintings": return "Paintings";
            case "brewing_stands": return "Brewing Stands";
            case "note_blocks": return "Note Blocks";
            case "juke_boxes": return "Juke Boxes";
            case "decorated_pots": return "Decorated Pots";
            case "armour_stands_remove": return "Armour Stands (Remove Items)";
            case "armour_stands_place": return "Armour Stands (Place Items)";
            case "armour_stands_replace": return "Armour Stands (Replace Items)";
            case "shulker_boxes": return "Shulker Boxes";
            case "music_mod_blocks": return "Music Mod Blocks";
            default: return protectionType;
        }
    }
}