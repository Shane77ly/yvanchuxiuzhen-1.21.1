package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 分别在来源战利品表和开发专用空表中检查九种玉矿。 */
final class JadeOreDropsCheck extends SystemCheck {
    static final String EMPTY = "jadeoreempty";

    static void emptyJadeTables(LootTableLoadEvent event) {
        if (event.getName().getNamespace().equals(ModId.VALUE) && event.getName().getPath().matches("blocks/cailiao_16[1-9]01")) {
            event.setCanceled(true);
        }
    }
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
            if (number == 168) {
                expectTrue("168 干态无流体", state.getFluidState().isEmpty());
                expectTrue("168 含水态保留水源", state.setValue(BlockStateProperties.WATERLOGGED, true).getFluidState().is(Fluids.WATER));
            }
            LootParams.Builder params = new LootParams.Builder(player.serverLevel())
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
            boolean empty = EMPTY.equals(System.getProperty("yvanchuxiuzhen.smoke.only"));
            String expected = empty ? prefix : prefix + "02";
            expectTrue(prefix + (empty ? " 空表回退一件成玉" : " 正常表掉落一件璞玉"),
                    one(state.getDrops(params), BuiltInRegistries.ITEM.get(id(expected))));
        }
    }

    private static boolean one(List<ItemStack> drops, Item item) {
        return drops.size() == 1 && drops.getFirst().is(item) && drops.getFirst().getCount() == 1;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
