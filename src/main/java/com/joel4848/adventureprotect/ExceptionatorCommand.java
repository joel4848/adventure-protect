package com.joel4848.adventureprotect;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ExceptionatorCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, net.minecraft.command.CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("adventureprotect")
                .then(CommandManager.literal("exceptionator")
                        .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
                        .executes(ExceptionatorCommand::giveExceptionator)));
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
}