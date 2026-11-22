package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;

/** 用原生堆肥桶计划刻、自动化容器和熔炉燃烧核验植物物品数据映射的实际消费。 */
final class PlantItemExtensionsCheck extends SystemCheck {
    private BlockPos composterPos;
    private BlockPos furnacePos;
    private BlockState composterBefore;
    private BlockState furnaceBefore;
    private long started;
    private boolean ready;

    PlantItemExtensionsCheck(List<String> report, String levelId) {
        super("植物堆肥与燃料", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (!ready) {
                    finish();
                    return true;
                }
                boolean[] elapsed = {false};
                onServer(player -> elapsed[0] = player.serverLevel().getGameTime() - started >= 340);
                if (!elapsed[0] && local < 1000) return false;
                expectTrue("原生机器经过至少340服务端刻", elapsed[0]);
                onServer(this::verifyAndRestore);
                finish();
            }
            default -> throw new IllegalStateException("未知植物扩展检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        composterPos = player.blockPosition().above(20).immutable();
        furnacePos = composterPos.east(2);
        if (!level.isEmptyBlock(composterPos) || !level.isEmptyBlock(furnacePos)) {
            fail("机器夹具位置不是空气，停止本项以保留原有方块");
            return;
        }
        composterBefore = level.getBlockState(composterPos);
        furnaceBefore = level.getBlockState(furnacePos);
        ready = true;
        for (Map.Entry<String, Integer> entry : Map.of("ancienttimeswood", 20000, "scorchedwooden", 4800,
                "peachlog", 1600, "mapleresinitem", 300).entrySet()) {
            ItemStack fuel = stack(entry.getKey(), 1);
            expect(entry.getKey() + "熔炼配方燃料刻", entry.getValue().doubleValue(), fuel.getBurnTime(RecipeType.SMELTING));
            expect(entry.getKey() + "未指定配方燃料刻", entry.getValue().doubleValue(), fuel.getBurnTime(null));
        }
        for (Map.Entry<String, Double> entry : Map.of("peachpeach", 0.65, "laurel_04", 0.6,
                "peachleaves", 0.3, "ancienttimesleaves", 1.0, "ginkgofruit", 1.0).entrySet()) {
            expect(entry.getKey() + "原生堆肥概率", entry.getValue().doubleValue(), ComposterBlock.getValue(stack(entry.getKey(), 1)));
        }
        BlockState state = Blocks.COMPOSTER.defaultBlockState();
        level.setBlock(composterPos, state, Block.UPDATE_ALL);
        WorldlyContainer input = ((ComposterBlock) Blocks.COMPOSTER).getContainer(state, level, composterPos);
        expectTrue("顶部自动化接受林桃", input.canPlaceItemThroughFace(0, stack("peachpeach", 1), Direction.UP));
        expectTrue("侧面不接受堆肥输入", !input.canPlaceItemThroughFace(0, stack("peachpeach", 1), Direction.EAST));
        ItemStack fruit = stack("peachpeach", 2);
        state = ComposterBlock.insertItem(player, state, level, fruit, composterPos);
        expect("林桃填入首层", 1, state.getValue(ComposterBlock.LEVEL).doubleValue());
        expect("首层消耗一个林桃", 1, fruit.getCount());
        ItemStack leaves = stack("ancienttimesleaves", 7);
        for (int index = 0; index < 6; index++) {
            state = ComposterBlock.insertItem(player, state, level, leaves, composterPos);
        }
        expect("百分百材料填满七层", 7, state.getValue(ComposterBlock.LEVEL).doubleValue());
        expect("六层消耗六份材料", 1, leaves.getCount());
        level.setBlock(furnacePos, Blocks.FURNACE.defaultBlockState(), Block.UPDATE_ALL);
        FurnaceBlockEntity furnace = (FurnaceBlockEntity) level.getBlockEntity(furnacePos);
        furnace.setItem(0, new ItemStack(Items.COBBLESTONE, 2));
        furnace.setItem(1, stack("ancienttimesleaves", 1));
        furnace.setChanged();
        started = level.getGameTime();
    }

    private void verifyAndRestore(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(composterPos);
        expect("计划刻将七层转为可取肥料", 8, state.getValue(ComposterBlock.LEVEL).doubleValue());
        WorldlyContainer output = ((ComposterBlock) Blocks.COMPOSTER).getContainer(state, level, composterPos);
        expectTrue("原生输出槽为骨粉", output.getItem(0).is(Items.BONE_MEAL));
        expectTrue("底部自动化可取骨粉", output.canTakeItemThroughFace(0, output.getItem(0), Direction.DOWN));
        output.removeItem(0, 1);
        expect("取出骨粉后回到零层", 0, level.getBlockState(composterPos).getValue(ComposterBlock.LEVEL).doubleValue());
        FurnaceBlockEntity furnace = (FurnaceBlockEntity) level.getBlockEntity(furnacePos);
        expectTrue("原生熔炉产出石头", furnace.getItem(2).is(Items.STONE));
        expect("300刻燃料只完成一份200刻熔炼", 1, furnace.getItem(2).getCount());
        expect("第二份圆石仍待熔炼", 1, furnace.getItem(0).getCount());
        expectTrue("燃料已被熔炉消耗", furnace.getItem(1).isEmpty());
        furnace.clearContent();
        level.setBlock(furnacePos, furnaceBefore, Block.UPDATE_ALL);
        level.setBlock(composterPos, composterBefore, Block.UPDATE_ALL);
    }

    private static ItemStack stack(String path, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)), count);
    }
}
