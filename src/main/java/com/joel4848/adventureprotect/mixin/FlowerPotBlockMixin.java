package com.joel4848.adventureprotect.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import com.joel4848.adventureprotect.Adventureprotect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerPotBlock.class)
public class FlowerPotBlockMixin {

    @Inject(
        method = "onUse",
        at = @At("HEAD"),
        cancellable = true
    )
    private void checkGamerule(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (serverPlayer.interactionManager.getGameMode() == GameMode.ADVENTURE &&
                !player.getWorld().getGameRules().getBoolean(Adventureprotect.FLOWERPOT)) {
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }

    @Inject(
        method = "onUseWithItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void checkGameruleWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ItemActionResult> cir) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (serverPlayer.interactionManager.getGameMode() == GameMode.ADVENTURE &&
                !player.getWorld().getGameRules().getBoolean(Adventureprotect.FLOWERPOT)) {
                cir.setReturnValue(ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            }
        }
    }
}
