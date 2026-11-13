package net.yvanchuxiuzhen.system.fulu;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.plant.api.PlantRipening;

/** 30号荫翠符：付款后，以六格视线落点为中心按来源Y、X、Z顺序催熟13×5×13范围。 */
final class FuluRipening {
    private FuluRipening() {
    }

    static void use(ServerPlayer player, ItemStack stack) {
        if (!FuluUse.pay(player, stack)) {
            return;
        }
        Vec3 eye = player.getEyePosition();
        BlockPos center = player.level().clip(new ClipContext(eye, eye.add(player.getViewVector(1).scale(6)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();
        for (int y = -2; y <= 2; y++) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    PlantRipening.ripen(player.serverLevel(), center.offset(x, y, z));
                }
            }
        }
        stack.shrink(1);
    }
}
