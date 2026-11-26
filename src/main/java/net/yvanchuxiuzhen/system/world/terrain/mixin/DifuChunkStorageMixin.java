package net.yvanchuxiuzhen.system.world.terrain.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.yvanchuxiuzhen.system.world.DifuWriteAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkStorage.class)
public abstract class DifuChunkStorageMixin implements DifuWriteAccess {
    @Unique private Consumer<CompletableFuture<Void>> yvanchuxiuzhen$receiver;

    @Override
    public void yvanchuxiuzhen$difuWrites(Consumer<CompletableFuture<Void>> receiver) {
        yvanchuxiuzhen$receiver = receiver;
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void yvanchuxiuzhen$trackWrite(ChunkPos pos, CompoundTag data, CallbackInfoReturnable<CompletableFuture<Void>> callback) {
        if (yvanchuxiuzhen$receiver != null) yvanchuxiuzhen$receiver.accept(callback.getReturnValue());
    }
}
