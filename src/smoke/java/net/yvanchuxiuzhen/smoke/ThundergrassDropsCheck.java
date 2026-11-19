package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 引雷草空战利品时的来源回退。 */
final class ThundergrassDropsCheck extends SystemCheck {
    ThundergrassDropsCheck(List<String> report, String levelId) {
        super("引雷草掉落", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(this::checkDrops);
        finish();
        return true;
    }

    private void checkDrops(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Block grass = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "thundergrass"));
        BlockPos pos = player.blockPosition();
        List<ItemStack> drops = Block.getDrops(grass.defaultBlockState(), level, pos, null, player, ItemStack.EMPTY);
        expectTrue("空战利品回退一株引雷草", drops.size() == 1 && drops.getFirst().is(grass.asItem())
                && drops.getFirst().getCount() == 1);
    }
}
