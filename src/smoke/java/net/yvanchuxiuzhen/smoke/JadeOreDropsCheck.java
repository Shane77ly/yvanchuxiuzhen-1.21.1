package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.yvanchuxiuzhen.platform.ModId;

/** 比较九种实际注册玉矿的常规掉落与爆炸衰减后的回退物。 */
final class JadeOreDropsCheck extends SystemCheck {
    JadeOreDropsCheck(List<String> report, String levelId) {
        super("玉矿采收", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(this::checkDrops);
        finish();
        return true;
    }

    private void checkDrops(ServerPlayer player) {
        for (int number = 161; number <= 169; number++) {
            String prefix = "cailiao_" + number;
            BlockState state = BuiltInRegistries.BLOCK.get(id(prefix + "01")).defaultBlockState();
            LootParams.Builder params = new LootParams.Builder(player.serverLevel())
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
            expectTrue(prefix + " 正常采收掉落一件璞玉", one(state.getDrops(params), BuiltInRegistries.ITEM.get(id(prefix + "02"))));
            // 爆炸半径只用于战利品上下文，观察实际衰减为空的样本。
            params.withParameter(LootContextParams.EXPLOSION_RADIUS, 1024f);
            boolean sawFallback = false;
            for (int sample = 0; sample < 8 && !sawFallback; sample++) {
                List<ItemStack> drops = state.getDrops(params);
                sawFallback = one(drops, BuiltInRegistries.ITEM.get(id(prefix)));
                if (!sawFallback && !one(drops, BuiltInRegistries.ITEM.get(id(prefix + "02")))) break;
            }
            expectTrue(prefix + " 空结果回退一件成玉", sawFallback);
        }
    }

    private static boolean one(List<ItemStack> drops, Item item) {
        return drops.size() == 1 && drops.getFirst().is(item) && drops.getFirst().getCount() == 1;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
