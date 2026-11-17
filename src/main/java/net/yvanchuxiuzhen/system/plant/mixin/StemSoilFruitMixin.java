package net.yvanchuxiuzhen.system.plant.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.system.plant.api.PlantSoilSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 恢复成熟瓜藤在五种来源土壤上结瓜，不向通用泥土标签添加成员。 */
@Mixin(StemBlock.class)
public abstract class StemSoilFruitMixin {
    @Redirect(method = "randomTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean yvanchuxiuzhen$sourceFruitSoil(BlockState soil, TagKey<Block> tag, BlockState state,
                                                 ServerLevel level, BlockPos pos, RandomSource random) {
        return soil.is(tag) || PlantSoilSupport.supports(soil, state);
    }
}
