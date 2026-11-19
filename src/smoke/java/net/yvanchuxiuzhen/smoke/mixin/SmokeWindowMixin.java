package net.yvanchuxiuzhen.smoke.mixin;

import com.mojang.blaze3d.platform.Window;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.neoforged.fml.loading.ImmediateWindowHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
abstract class SmokeWindowMixin {
    @Shadow private boolean fullscreen;
    @Shadow private boolean actuallyFullscreen;

    /** 创建隐藏的窗口化 OpenGL 上下文，供原生渲染和帧缓冲截图使用。 */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/loading/ImmediateWindowHandler;setupMinecraftWindow(Ljava/util/function/IntSupplier;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)J"))
    private long createHiddenWindow(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {
        fullscreen = false;
        actuallyFullscreen = false;
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
        long handle = ImmediateWindowHandler.setupMinecraftWindow(width, height, title, monitor);
        if (handle == 0L || GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_VISIBLE) != GLFW.GLFW_FALSE
                || GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) != GLFW.GLFW_FALSE
                || GLFW.glfwGetInputMode(handle, GLFW.GLFW_CURSOR) != GLFW.GLFW_CURSOR_NORMAL) {
            throw new IllegalStateException("冒烟客户端未创建无焦点、光标正常的隐藏窗口");
        }
        return handle;
    }

    @Inject(method = "toggleFullScreen", at = @At("HEAD"), cancellable = true)
    private void keepWindowed(CallbackInfo callback) {
        callback.cancel();
    }
}
