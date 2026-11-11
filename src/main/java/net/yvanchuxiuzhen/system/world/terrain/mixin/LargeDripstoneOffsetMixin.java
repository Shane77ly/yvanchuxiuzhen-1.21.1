package net.yvanchuxiuzhen.system.world.terrain.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.world.SourceLargeDripstoneFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 仅在来源大型滴水石代理执行期间恢复坐标相加后取整的偏移语义。 */
@Mixin(targets = "net.minecraft.world.level.levelgen.feature.LargeDripstoneFeature$WindOffsetter")
public abstract class LargeDripstoneOffsetMixin {
    @Shadow @Final private int originY;
    @Shadow @Final private Vec3 windSpeed;

    @Redirect(method = "offset", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;"))
    private BlockPos yvanchuxiuzhen$restoreLegacyOffset(BlockPos pos, int x, int y, int z) {
        if (!SourceLargeDripstoneFeature.usesLegacyOffset()) {
            return pos.offset(x, y, z);
        }
        int verticalDistance = this.originY - pos.getY();
        Vec3 windDelta = this.windSpeed.scale((double) verticalDistance);
        return BlockPos.containing(pos.getX() + windDelta.x, pos.getY(), pos.getZ() + windDelta.z);
    }
}
