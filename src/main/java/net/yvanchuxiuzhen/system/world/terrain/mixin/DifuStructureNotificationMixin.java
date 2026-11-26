package net.yvanchuxiuzhen.system.world.terrain.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.yvanchuxiuzhen.system.world.DifuStructureNotifications;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class DifuStructureNotificationMixin implements DifuStructureNotifications {
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private StructureCheck structureCheck;
    @Unique private volatile boolean yvanchuxiuzhen$compactStarts;

    @Override
    public void yvanchuxiuzhen$compactDifuNotifications(boolean enabled) {
        yvanchuxiuzhen$compactStarts = enabled;
    }

    @Inject(method = "onStructureStartsAvailable", at = @At("HEAD"), cancellable = true)
    private void yvanchuxiuzhen$submitStarts(ChunkAccess chunk, CallbackInfo callback) {
        if (!yvanchuxiuzhen$compactStarts) return;
        var pos = chunk.getPos();
        // 起点表是原可变映射的只读视图。枚举和引用数读取仍在原生执行器回调中进行。
        var starts = chunk.getAllStarts();
        server.execute(() -> structureCheck.onStructureLoad(pos, starts));
        callback.cancel();
    }
}
