package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.plant.FruitTreeLeavesBlock;

/** 桃叶原生放置、叶距更新与流体交互。 */
final class PeachLeavesCheck extends SystemCheck {
    private BlockPos logPos;
    private BlockPos leafPos;
    private BlockState previousLog;
    private BlockState previousLeaf;
    private ItemStack previousHeld;
    private GameType previousMode;

    PeachLeavesCheck(List<String> report, String levelId) {
        super("桃叶生命周期", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::place);
            return true;
        }
        if (local < 40) return false;
        if (step == 1) {
            onServer(this::checkAndRemoveLog);
            return true;
        }
        onServer(this::checkAndRestore);
        finish();
        return true;
    }

    private void place(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        logPos = player.blockPosition().above(8);
        leafPos = logPos.above();
        previousLog = level.getBlockState(logPos);
        previousLeaf = level.getBlockState(leafPos);
        previousHeld = player.getMainHandItem().copy();
        previousMode = player.gameMode.getGameModeForPlayer();
        Block log = block("peachlog");
        Block leaf = block("peachleaves");
        level.setBlock(leafPos, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(logPos, log.defaultBlockState(), 3);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(leaf, 2));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(logPos).add(0, 0.5, 0), Direction.UP, logPos, false);
        player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
        BlockState state = level.getBlockState(leafPos);
        expectTrue("原生放置桃叶且扣一", state.is(leaf) && player.getMainHandItem().getCount() == 1);
        if (!(state.getBlock() instanceof FruitTreeLeavesBlock leaves)) {
            fail("桃叶未使用来源树叶工厂");
            return;
        }
        expectTrue("玩家放置叶片持久且邻接桃木距离为一", state.getValue(LeavesBlock.PERSISTENT)
                && state.getValue(LeavesBlock.DISTANCE) == 1);
        expectTrue("桃叶拒绝注水和取水", !leaves.canPlaceLiquid(player, level, leafPos, state, Fluids.WATER)
                && !leaves.placeLiquid(level, leafPos, state, Fluids.WATER.defaultFluidState())
                && leaves.pickupBlock(player, level, leafPos, state).isEmpty() && state.getFluidState().isEmpty());
    }

    private void checkAndRemoveLog(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        expectTrue("邻接桃木时叶距保持一", level.getBlockState(leafPos).is(block("peachleaves"))
                && level.getBlockState(leafPos).getValue(LeavesBlock.DISTANCE) == 1);
        level.setBlock(logPos, Blocks.AIR.defaultBlockState(), 3);
    }

    private void checkAndRestore(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(leafPos);
        expectTrue("移除桃木后持久叶距更新为七", state.is(block("peachleaves"))
                && state.getValue(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.DISTANCE) == 7);
        level.setBlock(leafPos, previousLeaf, 3);
        level.setBlock(logPos, previousLog, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, previousHeld);
        player.setGameMode(previousMode);
        player.inventoryMenu.broadcastChanges();
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
