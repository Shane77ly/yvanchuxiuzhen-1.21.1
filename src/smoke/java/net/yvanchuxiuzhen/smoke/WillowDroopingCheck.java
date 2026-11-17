package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 柳树垂枝原生放置的替换位置、上下文手与扣件回归；不代替自然成树或画面验收。 */
final class WillowDroopingCheck extends SystemCheck {
    WillowDroopingCheck(List<String> report, String levelId) {
        super("柳树垂枝原位替换", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(this::check);
        finish();
        return true;
    }

    private void check(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Block willow = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "willowdrooping"));
        BlockPos origin = player.blockPosition().above(8);
        ItemStack main = player.getMainHandItem().copy();
        ItemStack off = player.getOffhandItem().copy();
        GameType mode = player.gameMode.getGameModeForPlayer();
        List<SavedBlock> saved = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            BlockPos pos = origin.east(index * 2);
            for (BlockPos target : List.of(pos, pos.above())) {
                saved.add(SavedBlock.capture(player, target));
                level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            level.setBlock(pos, willow.defaultBlockState(), Block.UPDATE_ALL);
        }
        player.setGameMode(GameType.SURVIVAL);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE, 2));
            use(player, origin, InteractionHand.MAIN_HAND);
            expectTrue("其他方块在垂枝原格放置且相邻格保持空气", level.getBlockState(origin).is(Blocks.STONE)
                    && level.getBlockState(origin.above()).isAir());
            expect("原位替换消耗一件石头", 1, player.getMainHandItem().getCount());

            BlockPos same = origin.east(2);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(willow, 2));
            use(player, same, InteractionHand.MAIN_HAND);
            expectTrue("手持自身物品保留原垂枝并在相邻格放置", level.getBlockState(same).is(willow)
                    && level.getBlockState(same.above()).is(willow));
            expect("同类相邻放置消耗一株", 1, player.getMainHandItem().getCount());

            BlockPos secondary = origin.east(4);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(willow, 2));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STONE, 2));
            use(player, secondary, InteractionHand.OFF_HAND);
            expectTrue("副手替换使用副手上下文", level.getBlockState(secondary).is(Blocks.STONE)
                    && level.getBlockState(secondary.above()).isAir());
            expect("副手替换只消耗副手一件", 1, player.getOffhandItem().getCount());
            expect("副手替换保留主手两株", 2, player.getMainHandItem().getCount());

            BlockPlaceContext emptyHand = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, hit(same));
            expectTrue("空手上下文保留来源可替换判定", level.getBlockState(same).canBeReplaced(emptyHand));
        } finally {
            for (SavedBlock block : saved) block.restore(player);
            player.setItemInHand(InteractionHand.MAIN_HAND, main);
            player.setItemInHand(InteractionHand.OFF_HAND, off);
            player.setGameMode(mode);
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static void use(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        player.gameMode.useItemOn(player, player.level(), player.getItemInHand(hand), hand, hit(pos));
    }

    private static BlockHitResult hit(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
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
