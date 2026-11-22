package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.system.plant.AnimatedBigGrassBlock;
import net.yvanchuxiuzhen.system.plant.AnimatedBigGrassBlockEntity;
import net.yvanchuxiuzhen.system.plant.BigGrassBlock;

/** 高草接触门槛、临时库存读回和三十服务端刻恢复的必要单项；不替代自然新区块获取。 */
final class BigGrassCheck extends SystemCheck {
    private final List<SavedBlock> blocks = new ArrayList<>();
    private BlockPos origin;
    private boolean sneaking;
    private int started;

    BigGrassCheck(List<String> report, String levelId) {
        super("高草生命周期", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                boolean[] elapsed = {false};
                onServer(player -> elapsed[0] = player.server.getTickCount() - started >= 35);
                if (!elapsed[0] && local < 300) return false;
                expectTrue("自然等待至少三十五服务端刻", elapsed[0]);
                onServer(this::restored);
            }
            case 2 -> {
                onServer(this::cleanup);
                finish();
            }
            default -> throw new IllegalStateException("未知高草检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        sneaking = player.isShiftKeyDown();
        origin = player.blockPosition().above(12);
        for (int index = 0; index < 2; index++) {
            BlockPos pos = origin.east(index * 3);
            for (BlockPos fixture : List.of(pos.below(), pos, pos.above())) {
                blocks.add(SavedBlock.capture(player, fixture));
                level.setBlock(fixture, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        }
        DoublePlantBlock.placeAt(level, BigGrassBlock.block().defaultBlockState(), origin, Block.UPDATE_ALL);
        player.setShiftKeyDown(false);
        level.getBlockState(origin.above()).entityInside(level, origin.above(), player);
        expectTrue("上半株接触不触发摇摆", whole(level, origin));
        player.setShiftKeyDown(true);
        level.getBlockState(origin).entityInside(level, origin, player);
        expectTrue("潜行接触不触发摇摆", whole(level, origin));
        player.setShiftKeyDown(false);
        Entity small = EntityType.CHICKEN.create(level);
        expectTrue("小体型样本宽高均低于一格", small.getBbWidth() < 1 && small.getBbHeight() < 1);
        level.getBlockState(origin).entityInside(level, origin, small);
        expectTrue("小体型接触不触发摇摆", whole(level, origin));
        small.discard();
        level.getBlockState(origin).entityInside(level, origin, player);
        BlockState animated = level.getBlockState(origin);
        expectTrue("正常玩家下半株接触切换数字动画一", animated.is(AnimatedBigGrassBlock.block())
                && animated.getValue(AnimatedBigGrassBlock.ANIMATION) == 1);
        expectTrue("切换动画时原生双格更新移除上半株", level.getBlockState(origin.above()).isAir());
        expectTrue("动画放置注册原生计划刻", level.getBlockTicks().hasScheduledTick(origin, AnimatedBigGrassBlock.block()));
        inventory(player, animated);
        BlockPos obstructed = origin.east(3);
        level.setBlock(obstructed, AnimatedBigGrassBlock.block().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(obstructed.above(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        started = player.server.getTickCount();
    }

    private void inventory(ServerPlayer player, BlockState animated) {
        ServerLevel level = player.serverLevel();
        if (!(level.getBlockEntity(origin) instanceof AnimatedBigGrassBlockEntity grass)) {
            expectTrue("动画方块按正式工厂创建九槽实体", false);
            return;
        }
        for (Direction direction : Direction.values()) {
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, origin, direction);
            expectTrue("六面能力保留九槽" + direction, handler != null && handler.getSlots() == 9);
        }
        expectTrue("无方向查询保持来源无能力", level.getCapability(Capabilities.ItemHandler.BLOCK, origin, null) == null);
        IItemHandler top = level.getCapability(Capabilities.ItemHandler.BLOCK, origin, Direction.UP);
        IItemHandler bottom = level.getCapability(Capabilities.ItemHandler.BLOCK, origin, Direction.DOWN);
        if (top != null && bottom != null) {
            expectTrue("顶面向第九槽放入带组件物品", top.insertItem(8, witness(), false).isEmpty());
            expectTrue("底面可取出第九槽带组件物品", ItemStack.matches(witness(), bottom.extractItem(8, 3, true)));
        }
        CompoundTag data = grass.saveWithFullMetadata(player.registryAccess());
        BlockEntity copy = BlockEntity.loadStatic(origin, animated, data, player.registryAccess());
        expectTrue("原生NBT读回保留第九槽物品及组件", copy instanceof AnimatedBigGrassBlockEntity loaded
                && ItemStack.matches(witness(), loaded.getItem(8)));
        AbstractContainerMenu menu = grass.createMenu(21, player.getInventory(), player);
        expectTrue("来源提供者仍返回独立三行箱子", menu != null && menu.slots.size() == 63
                && menu.getSlot(0).container != grass);
        ItemStack picked = AnimatedBigGrassBlock.block().getCloneItemStack(animated,
                new BlockHitResult(Vec3.atCenterOf(origin), Direction.UP, origin, false), level, origin, player);
        expectTrue("中键拾取动画草仍得到普通高草", picked.is(BigGrassBlock.block().asItem()) && picked.getCount() == 1);
        grass.setItem(0, new ItemStack(Items.COBBLESTONE, 65));
        expect("九槽库存沿用来源六十四堆叠上限", 64, grass.getItem(0).getCount());
    }

    private void restored(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        expectTrue("三十刻计划恢复完整上下半株", whole(level, origin));
        expectTrue("恢复替换移除临时动画方块实体", level.getBlockEntity(origin) == null);
        expectTrue("恢复按来源写序覆盖上方已有石块", whole(level, origin.east(3)));
        level.setBlock(origin.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        expectTrue("破坏上半株通过原生更新同时移除下半株", level.getBlockState(origin).isAir());
    }

    private void cleanup(ServerPlayer player) {
        for (SavedBlock block : blocks) block.restore(player);
        player.setShiftKeyDown(sneaking);
    }

    private static boolean whole(ServerLevel level, BlockPos pos) {
        BlockState lower = level.getBlockState(pos);
        BlockState upper = level.getBlockState(pos.above());
        return lower.is(BigGrassBlock.block()) && upper.is(BigGrassBlock.block())
                && lower.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
                && upper.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
    }

    private static ItemStack witness() {
        ItemStack stack = new ItemStack(Items.DIAMOND, 3);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("高草第九槽验收物品"));
        return stack;
    }

    private record SavedBlock(BlockPos pos, BlockState state, CompoundTag data) {
        static SavedBlock capture(ServerPlayer player, BlockPos pos) {
            BlockEntity block = player.level().getBlockEntity(pos);
            return new SavedBlock(pos.immutable(), player.level().getBlockState(pos),
                    block == null ? null : block.saveWithFullMetadata(player.registryAccess()));
        }

        void restore(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            if (data != null) level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
        }
    }
}
