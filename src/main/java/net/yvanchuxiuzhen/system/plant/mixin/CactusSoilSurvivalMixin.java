package net.yvanchuxiuzhen.system.plant.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.system.plant.api.PlantSoilSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 土壤支持不能越过仙人掌的侧边拒绝和上方非液体条件。 */
@Mixin(CactusBlock.class)
public abstract class CactusSoilSurvivalMixin {
    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private void yvanchuxiuzhen$sourceSoilTopFluid(BlockState state, LevelReader level, BlockPos pos,
                                                  CallbackInfoReturnable<Boolean> callback) {
        if (PlantSoilSupport.supports(level.getBlockState(pos.below()), state)) {
            callback.setReturnValue(callback.getReturnValueZ() && !level.getBlockState(pos.above()).liquid());
        }
    }
}
