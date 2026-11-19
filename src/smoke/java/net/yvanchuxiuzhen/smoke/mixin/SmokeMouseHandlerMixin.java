package net.yvanchuxiuzhen.smoke.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class SmokeMouseHandlerMixin {
    /** 在 Minecraft 改变系统光标位置与捕获模式之前拦截。 */
    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void keepDesktopCursor(CallbackInfo callback) {
        callback.cancel();
    }
}
