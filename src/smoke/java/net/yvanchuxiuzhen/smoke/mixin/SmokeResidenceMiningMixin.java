package net.yvanchuxiuzhen.smoke.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.yvanchuxiuzhen.smoke.SmokeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 居所重放在隐藏窗口中显式推进原生挖掘，保留其他情况下的原版鼠标松开取消流程。 */
@Mixin(Minecraft.class)
abstract class SmokeResidenceMiningMixin {
    @Redirect(method = "continueAttack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"))
    private void allowResidenceMining(MultiPlayerGameMode gameMode) {
        if (!SmokeMod.isResidenceDigging()) gameMode.stopDestroyBlock();
    }
}
