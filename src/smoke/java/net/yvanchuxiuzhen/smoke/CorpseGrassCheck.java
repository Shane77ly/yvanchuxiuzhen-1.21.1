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

/** 尸草上下文替换位置、主副手扣件与自身物品拒绝回归。 */
final class CorpseGrassCheck extends SystemCheck {
    private static final int FIXTURE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    CorpseGrassCheck(List<String> report, String levelId) {
        super("尸草原位替换", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(this::check);
        finish();
        return true;
    }

    private void check(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Block plant = block("corpsegrass");
        BlockPos origin = player.blockPosition().above(8);
        ItemStack main = player.getMainHandItem().copy();
        ItemStack off = player.getOffhandItem().copy();
        GameType mode = player.gameMode.getGameModeForPlayer();
        List<SavedBlock> saved = new ArrayList<>();
        for (int index = 0; index < 3; index++) {
            BlockPos pos = origin.east(index * 2);
            for (BlockPos target : List.of(pos.below(), pos, pos.above())) {
                saved.add(SavedBlock.capture(player, target));
            }
            level.setBlock(pos.below(), block("rottingsoil").defaultBlockState(), FIXTURE_FLAGS);
            level.setBlock(pos, plant.defaultBlockState(), FIXTURE_FLAGS);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), FIXTURE_FLAGS);
        }
        player.setGameMode(GameType.SURVIVAL);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE, 2));
            use(player, origin, InteractionHand.MAIN_HAND);
            expectTrue("石头替换尸草原格且上格保持空气", level.getBlockState(origin).is(Blocks.STONE)
                    && level.getBlockState(origin.above()).isAir());
            expect("主手原位放置扣一", 1, player.getMainHandItem().getCount());

            BlockPos secondary = origin.east(2);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(plant, 2));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STONE, 2));
            use(player, secondary, InteractionHand.OFF_HAND);
            expectTrue("副手石头按副手上下文原位替换尸草", level.getBlockState(secondary).is(Blocks.STONE)
                    && level.getBlockState(secondary.above()).isAir());
            expect("副手原位替换扣一", 1, player.getOffhandItem().getCount());
            expect("副手替换保留主手尸草", 2, player.getMainHandItem().getCount());

            BlockPos contextPos = origin.east(4);
            BlockPlaceContext same = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, new ItemStack(plant), hit(contextPos));
            expectTrue("自身物品上下文拒绝原位替换", !level.getBlockState(contextPos).canBeReplaced(same));
            BlockPlaceContext empty = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, hit(contextPos));
            expectTrue("空手上下文允许原位替换", level.getBlockState(contextPos).canBeReplaced(empty));
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

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
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
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), FIXTURE_FLAGS);
            level.setBlock(pos, state, FIXTURE_FLAGS);
            if (data != null) level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
        }
    }
}
