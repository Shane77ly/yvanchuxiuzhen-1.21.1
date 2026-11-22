package net.yvanchuxiuzhen.system.plant.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.system.plant.api.PlantSoilSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 五种来源土壤没有蘑菇专用基底标签时，保留小于13的来源亮度限制。 */
@Mixin(MushroomBlock.class)
public abstract class MushroomSoilSurvivalMixin {
    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private void yvanchuxiuzhen$sourceSoilLight(BlockState state, LevelReader level, BlockPos pos,
                                               CallbackInfoReturnable<Boolean> callback) {
        BlockState soil = level.getBlockState(pos.below());
        if (PlantSoilSupport.supports(soil, state)) {
            callback.setReturnValue(soil.is(BlockTags.MUSHROOM_GROW_BLOCK) || level.getRawBrightness(pos, 0) < 13);
        }
    }
}
