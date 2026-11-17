package net.yvanchuxiuzhen.smoke.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.smoke.WorldStopFutureDiagnostics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 冒烟专用拦截只包装原卸载动作，保存注册Future和原Executor的对应关系。 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapUnloadFutureDiagnosticsMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Redirect(method = "scheduleUnload", at = @At(value = "INVOKE", target =
            "Ljava/util/concurrent/CompletableFuture;thenRunAsync(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
            remap = false), require = 1, expect = 1, allow = 1)
    private CompletableFuture<Void> yvanchuxiuzhen$observeRegisteredFuture(CompletableFuture<?> parent,
            Runnable action, Executor executor, long chunkPos, ChunkHolder holder) {
        Runnable observed = WorldStopFutureDiagnostics.wrap((ChunkMap) (Object) this, level, holder, chunkPos, parent, action);
        return parent.thenRunAsync(observed, executor);
    }
}
