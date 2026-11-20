package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 虫蚀草在原生战利品为空时按内容表补自身掉落。 */
final class ChongshicaoBlock extends ContentFlower {
    private final int selfDropCount;

    ChongshicaoBlock(BlockTraits traits) {
        super(traits);
        selfDropCount = traits.row().behavior().fallbackDrop()
                .filter(drop -> drop.item().equals("self")).orElseThrow().count();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this, selfDropCount)) : drops;
    }
}
