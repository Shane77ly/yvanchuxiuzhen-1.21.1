package net.yvanchuxiuzhen.system.plant.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.system.plant.api.PlantSoilSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 五种来源土壤支持作物时，仍保留1.18.2的亮度或可见天空前置条件。 */
@Mixin(CropBlock.class)
public abstract class CropSoilSurvivalMixin {
    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private void yvanchuxiuzhen$sourceSoilLight(BlockState state, LevelReader level, BlockPos pos,
                                               CallbackInfoReturnable<Boolean> callback) {
        if (PlantSoilSupport.supports(level.getBlockState(pos.below()), state)) {
            callback.setReturnValue(level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos));
        }
    }
}
