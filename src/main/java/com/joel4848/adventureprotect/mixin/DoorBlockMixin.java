package com.joel4848.adventureprotect.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.block.DoorBlock;
import com.joel4848.adventureprotect.Adventureprotect;

@Mixin(DoorBlock.class)
public class DoorBlockMixin {
    @Inject(
        method = "onUse",
        at = @At("HEAD"),
        cancellable = true
    )
    private void checkGamerule(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (serverPlayer.interactionManager.getGameMode() == GameMode.ADVENTURE &&
                    !world.getGameRules().getBoolean(Adventureprotect.DOOR)) {
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }
}
