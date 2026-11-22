package net.yvanchuxiuzhen.smoke.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.yvanchuxiuzhen.smoke.PlantDecorationTrace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlacedFeature.class)
public abstract class PlantPlacedFeatureMixin {
    @Inject(method = "placeWithBiomeCheck", at = @At("RETURN"))
    private void yvanchuxiuzhen$recordPlantPlacement(WorldGenLevel level, ChunkGenerator generator,
            RandomSource random, BlockPos origin, CallbackInfoReturnable<Boolean> callback) {
        PlantDecorationTrace.placed(level, (PlacedFeature) (Object) this, callback.getReturnValueZ());
    }
}
