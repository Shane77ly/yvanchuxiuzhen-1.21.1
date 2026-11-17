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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 八种野生植物的来源土壤、原生放置与简单方块地物回归。 */
final class PlantGroundCheck extends SystemCheck {
    private static final int FIXTURE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final List<Sample> SAMPLES = List.of(
            new Sample("scorchedgrass", List.of("scorchedsoil"), List.of(Blocks.DIRT)),
            new Sample("thundergrass", List.of("scorchedsoil"), List.of(Blocks.DIRT)),
            new Sample("thistlesandthorns", List.of("loess"), List.of(Blocks.SAND)),
            new Sample("ironbamboo", List.of("loess"), List.of(Blocks.SAND)),
            new Sample("ironbambootail", List.of("loess"), List.of(Blocks.SAND)),
            new Sample("tripterygiumwilfordii", List.of("loess"), List.of(Blocks.DIRT, Blocks.SAND)),
            new Sample("soulgrass", List.of("loess", "youbosoil"), List.of(Blocks.NETHERRACK, Blocks.NETHER_WART_BLOCK)),
            new Sample("corpsegrass", List.of("rottingsoil"), List.of()));

    PlantGroundCheck(List<String> report, String levelId) {
        super("野生植物土壤命名空间", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(this::check);
        finish();
        return true;
    }

    private void check(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition().above(8);
        ItemStack held = player.getMainHandItem().copy();
        GameType mode = player.gameMode.getGameModeForPlayer();
        List<SavedBlock> saved = new ArrayList<>();
        for (BlockPos target : List.of(pos.below(), pos, pos.above())) {
            saved.add(SavedBlock.capture(player, target));
        }
        player.setGameMode(GameType.SURVIVAL);
        try {
            for (Sample sample : SAMPLES) {
                Block plant = block(sample.plant());
                BlockState state = plant.defaultBlockState();
                for (String soil : sample.soils()) {
                    prepare(level, pos, block(soil));
                    expectTrue(sample.plant() + "可在来源土壤" + soil + "存活", state.canSurvive(level, pos));
                    boolean generated = feature(level, pos, state) && level.getBlockState(pos).is(plant);
                    expectTrue(sample.plant() + "简单方块地物可在" + soil + "投放", generated);
                    if (generated && plant instanceof DoublePlantBlock) {
                        BlockState upper = level.getBlockState(pos.above());
                        expectTrue("铁竹尾地物保留上下半株并可存活", level.getBlockState(pos).getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
                                && upper.is(plant) && upper.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                                && upper.canSurvive(level, pos.above()));
                    }
                    level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), FIXTURE_FLAGS);
                    expectTrue(sample.plant() + "失去土壤后原生邻居更新移除", state.updateShape(Direction.DOWN,
                            Blocks.AIR.defaultBlockState(), level, pos, pos.below()).isAir());

                    prepare(level, pos, block(soil));
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(plant, 2));
                    BlockPos ground = pos.below();
                    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(ground), Direction.UP, ground, false);
                    player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
                    expectTrue(sample.plant() + "原生方块物品可在" + soil + "放置", level.getBlockState(pos).is(plant));
                    expect(sample.plant() + "生存放置扣一", 1, player.getMainHandItem().getCount());
                }
                for (Block vanilla : sample.vanilla()) {
                    prepare(level, pos, vanilla);
                    expectTrue(sample.plant() + "保留来源原版土壤" + BuiltInRegistries.BLOCK.getKey(vanilla), state.canSurvive(level, pos));
                }
                Block wrongSoil = block(sample.soils().contains("scorchedsoil") ? "loess" : "scorchedsoil");
                prepare(level, pos, wrongSoil);
                expectTrue(sample.plant() + "拒绝未列入来源的Mod土壤", !state.canSurvive(level, pos));
                prepare(level, pos, Blocks.AIR);
                expectTrue(sample.plant() + "拒绝空气地面", !state.canSurvive(level, pos));
                expectTrue(sample.plant() + "简单方块地物拒绝悬空投放", !feature(level, pos, state)
                        && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir());
            }
        } finally {
            prepare(level, pos, Blocks.AIR);
            for (SavedBlock block : saved) block.restore(player);
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            player.setGameMode(mode);
            player.inventoryMenu.broadcastChanges();
        }
    }

    /** 只调用来源斑块的SIMPLE_BLOCK内层，不绕过存活判断，也不覆盖维度或自然群系验收。 */
    private static boolean feature(ServerLevel level, BlockPos pos, BlockState state) {
        return Feature.SIMPLE_BLOCK.place(new SimpleBlockConfiguration(BlockStateProvider.simple(state)), level,
                level.getChunkSource().getGenerator(), RandomSource.create(0), pos);
    }

    private static void prepare(ServerLevel level, BlockPos pos, Block ground) {
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), FIXTURE_FLAGS);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), FIXTURE_FLAGS);
        level.setBlock(pos.below(), ground.defaultBlockState(), FIXTURE_FLAGS);
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }

    private record Sample(String plant, List<String> soils, List<Block> vanilla) {
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
            level.setBlock(pos, state, FIXTURE_FLAGS);
            if (data != null) level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
        }
    }
}
