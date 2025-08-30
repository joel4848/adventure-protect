package com.joel4848.adventureprotect.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import com.joel4848.adventureprotect.Adventureprotect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceGateBlock.class)
public class FenceGateBlockMixin {
    @Inject(
        method = "onUse",
        at = @At("HEAD"),
        cancellable = true
    )
    private void checkGamerule(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (serverPlayer.interactionManager.getGameMode() == GameMode.ADVENTURE &&
                !player.getWorld().getGameRules().getBoolean(Adventureprotect.FENCEGATE)) {
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }
}
