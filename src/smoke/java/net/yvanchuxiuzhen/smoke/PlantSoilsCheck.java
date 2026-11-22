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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 五种来源土壤的原生消费者、显式ground优先级和有限结瓜样本。 */
final class PlantSoilsCheck extends SystemCheck {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final List<String> SOILS = List.of("scorchedsoil", "loess", "youbosoil", "rottingsoil", "grassblock");
    private static final List<GroundSample> GROUNDS = List.of(
            new GroundSample("scorchedgrass", List.of("scorchedsoil")),
            new GroundSample("thundergrass", List.of("scorchedsoil")),
            new GroundSample("iceboundrock", List.of()),
            new GroundSample("icecrystal", List.of()),
            new GroundSample("icicleflower", List.of()),
            new GroundSample("coldleaf", List.of()),
            new GroundSample("bluegrass", List.of()),
            new GroundSample("iceboundrock_02", List.of()),
            new GroundSample("frostflowers", List.of()),
            new GroundSample("nautilus", List.of()),
            new GroundSample("thistlesandthorns", List.of("loess")),
            new GroundSample("ironbamboo", List.of("loess")),
            new GroundSample("ironbambootail", List.of("loess")),
            new GroundSample("tripterygiumwilfordii", List.of("loess")),
            new GroundSample("soulgrass", List.of("loess", "youbosoil")),
            new GroundSample("corpsegrass", List.of("rottingsoil")),
            new GroundSample("bladegrass", List.of()),
            new GroundSample("tailbladegrass", List.of()));
    private static final List<Block> NATIVE_REJECTIONS = List.of(
            Blocks.BAMBOO_SAPLING, Blocks.BAMBOO, Blocks.CHORUS_FLOWER, Blocks.CHORUS_PLANT,
            Blocks.SMALL_DRIPLEAF, Blocks.BIG_DRIPLEAF, Blocks.MANGROVE_PROPAGULE, Blocks.CHERRY_SAPLING,
            Blocks.TORCHFLOWER_CROP, Blocks.TORCHFLOWER, Blocks.PITCHER_CROP, Blocks.PITCHER_PLANT);
    private final List<SavedBlock> saved = new ArrayList<>();
    private ItemStack held;
    private GameType mode;
    private BlockPos open;
    private BlockPos bright;
    private BlockPos dark;

    PlantSoilsCheck(List<String> report, String levelId) {
        super("五种来源种植土壤", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::prepare);
            return true;
        }
        if (local < 40) {
            return false;
        }
        onServer(this::check);
        finish();
        return true;
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        open = player.blockPosition().above(8);
        bright = open.east(10);
        dark = open.east(20);
        held = player.getMainHandItem().copy();
        mode = player.gameMode.getGameModeForPlayer();
        for (BlockPos center : List.of(open, bright, dark)) {
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
                saved.add(SavedBlock.capture(player, pos));
            }
        }
        for (BlockPos pos : BlockPos.betweenClosed(open.offset(-2, -2, -2), open.offset(2, 2, 2))) {
            level.setBlock(pos, (pos.getY() < open.getY() ? Blocks.STONE : Blocks.AIR).defaultBlockState(), FLAGS);
        }
        chamber(level, bright, true);
        chamber(level, dark, false);
        player.setGameMode(GameType.SURVIVAL);
    }

    private static void chamber(ServerLevel level, BlockPos center, boolean lit) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            boolean shell = Math.abs(pos.getX() - center.getX()) == 2 || Math.abs(pos.getY() - center.getY()) == 2
                    || Math.abs(pos.getZ() - center.getZ()) == 2;
            level.setBlock(pos, (shell ? Blocks.STONE : Blocks.AIR).defaultBlockState(), FLAGS);
        }
        if (lit) {
            level.setBlock(center.above(), Blocks.SEA_LANTERN.defaultBlockState(), FLAGS);
        }
    }

    private void check(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        try {
            expectTrue("明室原生亮度至少13且不可见天空", level.getRawBrightness(bright, 0) >= 13 && !level.canSeeSky(bright));
            expectTrue("暗室原生亮度小于8且不可见天空", level.getRawBrightness(dark, 0) < 8 && !level.canSeeSky(dark));
            groundMatrix(level);
            ordinaryPlants(player);
            nativeBoundaries(level);
            lightBoundaries(level);
            cactusAndCane(level);
            growthContributions(level);
            fruitSamples(level);
        } finally {
            for (SavedBlock block : saved) {
                block.restore(player);
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            player.setGameMode(mode);
            player.inventoryMenu.broadcastChanges();
        }
    }

    private void groundMatrix(ServerLevel level) {
        for (GroundSample sample : GROUNDS) {
            Block plant = block(sample.plant());
            BlockState lower = plant.defaultBlockState();
            for (String soil : SOILS) {
                ground(level, open, block(soil));
                expect(sample.plant() + "在" + soil + "保持来源ground判定", sample.soils().contains(soil), lower.canSurvive(level, open));
            }
            if (plant instanceof DoublePlantBlock) {
                BlockState upper = lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                ground(level, open, Blocks.STONE);
                level.setBlock(open, lower, FLAGS);
                expectTrue(sample.plant() + "上半株只认自身下半株并不重查下层土壤", upper.canSurvive(level, open.above()));
                level.setBlock(open, upper, FLAGS);
                expectTrue(sample.plant() + "上半株拒绝自身上半株", !upper.canSurvive(level, open.above()));
                Block other = block(sample.plant().equals("iceboundrock") ? "iceboundrock_02" : "iceboundrock");
                level.setBlock(open, other.defaultBlockState(), FLAGS);
                expectTrue(sample.plant() + "上半株拒绝其他双株下半株", !upper.canSurvive(level, open.above()));
                level.setBlock(open, block("loess").defaultBlockState(), FLAGS);
                expectTrue(sample.plant() + "上半株拒绝直接种在土壤上", !upper.canSurvive(level, open.above()));
            }
        }
    }

    private void ordinaryPlants(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (String soilId : SOILS) {
            Block soil = block(soilId);
            ground(level, open, soil);
            for (Block plant : List.of(Blocks.DANDELION, Blocks.OAK_SAPLING, block("annualfleabane"))) {
                expectTrue(soilId + "支持普通植物" + BuiltInRegistries.BLOCK.getKey(plant), plant.defaultBlockState().canSurvive(level, open));
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Blocks.DANDELION, 2));
            BlockPos soilPos = open.below();
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(soilPos), Direction.UP, soilPos, false);
            player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            expectTrue(soilId + "经原生物品入口放置普通花", level.getBlockState(open).is(Blocks.DANDELION));
            expect(soilId + "生存放置扣一", 1, player.getMainHandItem().getCount());
            level.setBlock(soilPos, Blocks.AIR.defaultBlockState(), FLAGS);
            expectTrue(soilId + "普通花失土时原生邻居更新移除", Blocks.DANDELION.defaultBlockState().updateShape(
                    Direction.DOWN, Blocks.AIR.defaultBlockState(), level, open, soilPos).isAir());
            List<ItemStack> drops = Block.getDrops(soil.defaultBlockState(), level, soilPos, null, player, ItemStack.EMPTY);
            Item expected = soilId.equals("grassblock") ? Items.DIRT : soil.asItem();
            expectTrue(soilId + "保留空战利品表的来源掉落", drops.size() == 1 && drops.getFirst().is(expected)
                    && drops.getFirst().getCount() == 1);
        }
    }

    private void nativeBoundaries(ServerLevel level) {
        for (String soilId : SOILS) {
            Block soil = block(soilId);
            for (Block plant : NATIVE_REJECTIONS) {
                ground(level, open, soil);
                BlockState state = plant.defaultBlockState();
                expectTrue(soilId + "不扩张原生独立约束" + BuiltInRegistries.BLOCK.getKey(plant),
                        soil.defaultBlockState().canSustainPlant(level, open.below(), Direction.UP, state).isDefault()
                                && !state.canSurvive(level, open));
            }
            ground(level, open, soil);
            BlockState cocoa = Blocks.COCOA.defaultBlockState();
            Direction facing = cocoa.getValue(HorizontalDirectionalBlock.FACING);
            BlockPos backing = open.relative(facing);
            level.setBlock(backing, soil.defaultBlockState(), FLAGS);
            expectTrue(soilId + "不替代可可的丛林木侧向条件", !cocoa.canSurvive(level, open));
            level.setBlock(backing, Blocks.AIR.defaultBlockState(), FLAGS);
            BlockState hanging = Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(MangrovePropaguleBlock.HANGING, true);
            level.setBlock(open.above(), soil.defaultBlockState(), FLAGS);
            expectTrue(soilId + "不替代悬挂红树苗的红树叶条件", !hanging.canSurvive(level, open));
        }
    }

    private void lightBoundaries(ServerLevel level) {
        for (String soilId : SOILS) {
            Block soil = block(soilId);
            level.setBlock(bright.below(), soil.defaultBlockState(), FLAGS);
            level.setBlock(dark.below(), soil.defaultBlockState(), FLAGS);
            expectTrue(soilId + "作物在明室存活", Blocks.WHEAT.defaultBlockState().canSurvive(level, bright));
            expectTrue(soilId + "作物在不可见天的暗室拒绝存活", !Blocks.WHEAT.defaultBlockState().canSurvive(level, dark));
            for (Block mushroom : List.of(Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM)) {
                expectTrue(soilId + "蘑菇明室拒绝存活" + BuiltInRegistries.BLOCK.getKey(mushroom), !mushroom.defaultBlockState().canSurvive(level, bright));
                expectTrue(soilId + "蘑菇暗室存活" + BuiltInRegistries.BLOCK.getKey(mushroom), mushroom.defaultBlockState().canSurvive(level, dark));
            }
        }
        level.setBlock(bright.below(), Blocks.MYCELIUM.defaultBlockState(), FLAGS);
        expectTrue("原版菌丝仍绕过蘑菇高亮度限制", Blocks.BROWN_MUSHROOM.defaultBlockState().canSurvive(level, bright));
        level.setBlock(bright.below(), Blocks.FARMLAND.defaultBlockState(), FLAGS);
        level.setBlock(dark.below(), Blocks.FARMLAND.defaultBlockState(), FLAGS);
        expectTrue("原版耕地作物保留明室存活", Blocks.WHEAT.defaultBlockState().canSurvive(level, bright));
        expectTrue("原版耕地作物保留暗室拒绝", !Blocks.WHEAT.defaultBlockState().canSurvive(level, dark));
    }

    private void cactusAndCane(ServerLevel level) {
        for (String soilId : SOILS) {
            ground(level, open, block(soilId));
            BlockState cactus = Blocks.CACTUS.defaultBlockState();
            expectTrue(soilId + "支持无侧障碍的仙人掌", cactus.canSurvive(level, open));
            for (Block liquid : List.of(Blocks.WATER, Blocks.LAVA)) {
                level.setBlock(open.above(), liquid.defaultBlockState(), FLAGS);
                expectTrue(soilId + "仙人掌拒绝上方液体" + BuiltInRegistries.BLOCK.getKey(liquid), !cactus.canSurvive(level, open));
            }
            level.setBlock(open.above(), Blocks.AIR.defaultBlockState(), FLAGS);
            for (Block obstacle : List.of(Blocks.STONE, Blocks.LAVA)) {
                level.setBlock(open.north(), obstacle.defaultBlockState(), FLAGS);
                expectTrue(soilId + "仙人掌拒绝侧边" + BuiltInRegistries.BLOCK.getKey(obstacle), !cactus.canSurvive(level, open));
            }
            level.setBlock(open.north(), Blocks.WATER.defaultBlockState(), FLAGS);
            expectTrue(soilId + "仙人掌保留来源侧边水允许", cactus.canSurvive(level, open));
            level.setBlock(open.north(), Blocks.AIR.defaultBlockState(), FLAGS);
            expectTrue(soilId + "甘蔗保留来源无邻水支持", Blocks.SUGAR_CANE.defaultBlockState().canSurvive(level, open));
        }
    }

    private void growthContributions(ServerLevel level) {
        for (String soilId : SOILS) {
            Block soil = block(soilId);
            for (Block plant : List.of(Blocks.WHEAT, Blocks.PUMPKIN_STEM)) {
                BlockState state = plant.defaultBlockState();
                soilPlane(level, dark, soil);
                expect(soilId + "暗室九格普通生长贡献" + BuiltInRegistries.BLOCK.getKey(plant), 4, GrowthProbe.speed(state, level, dark));
                soilPlane(level, dark, Blocks.STONE);
                level.setBlock(dark.below(), soil.defaultBlockState(), FLAGS);
                expect(soilId + "暗室仅中心普通生长贡献" + BuiltInRegistries.BLOCK.getKey(plant), 2, GrowthProbe.speed(state, level, dark));
                soilPlane(level, dark, Blocks.STONE);
                expect(soilId + "暗室石头无生长贡献" + BuiltInRegistries.BLOCK.getKey(plant), 1, GrowthProbe.speed(state, level, dark));
            }
            level.setBlock(dark.below(), soil.defaultBlockState(), FLAGS);
            expectTrue(soilId + "保持非肥沃土壤", !soil.defaultBlockState().isFertile(level, dark.below()));
        }
    }

    private void fruitSamples(ServerLevel level) {
        for (String soilId : SOILS) {
            for (FruitSample sample : List.of(new FruitSample(Blocks.PUMPKIN_STEM, Blocks.PUMPKIN, Blocks.ATTACHED_PUMPKIN_STEM),
                    new FruitSample(Blocks.MELON_STEM, Blocks.MELON, Blocks.ATTACHED_MELON_STEM))) {
                soilPlane(level, bright, block(soilId));
                BlockState mature = sample.stem().defaultBlockState().setValue(StemBlock.AGE, 7);
                level.setBlock(bright, mature, FLAGS);
                RandomSource random = RandomSource.create(0);
                BlockPos fruit = null;
                int attempts = 0;
                while (attempts < 256 && fruit == null) {
                    mature.randomTick(level, bright, random);
                    attempts++;
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        BlockPos candidate = bright.relative(direction);
                        if (level.getBlockState(candidate).is(sample.fruit())) {
                            fruit = candidate;
                            break;
                        }
                    }
                }
                BlockState attached = level.getBlockState(bright);
                expectTrue(soilId + "成熟瓜藤原生randomTick有限样本结瓜并连接" + BuiltInRegistries.BLOCK.getKey(sample.stem())
                                + " 次数" + attempts + "/256",
                        fruit != null && attached.is(sample.attached())
                                && bright.relative(attached.getValue(HorizontalDirectionalBlock.FACING)).equals(fruit));
            }
        }
        soilPlane(level, bright, Blocks.STONE);
        BlockState mature = Blocks.PUMPKIN_STEM.defaultBlockState().setValue(StemBlock.AGE, 7);
        level.setBlock(bright, mature, FLAGS);
        RandomSource random = RandomSource.create(0);
        for (int attempt = 0; attempt < 256; attempt++) {
            mature.randomTick(level, bright, random);
        }
        boolean fruit = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            fruit |= level.getBlockState(bright.relative(direction)).is(Blocks.PUMPKIN);
        }
        expectTrue("成熟瓜藤未向普通石头扩张果实落点的有限256次对照", !fruit);
    }

    private static void soilPlane(ServerLevel level, BlockPos center, Block soil) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(center.offset(x, 0, z), Blocks.AIR.defaultBlockState(), FLAGS);
                level.setBlock(center.offset(x, -1, z), soil.defaultBlockState(), FLAGS);
            }
        }
    }

    private static void ground(ServerLevel level, BlockPos pos, Block soil) {
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), FLAGS);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
        level.setBlock(pos.below(), soil.defaultBlockState(), FLAGS);
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }

    private record GroundSample(String plant, List<String> soils) {
    }

    private record FruitSample(Block stem, Block fruit, Block attached) {
    }

    /** 仅暴露原生静态公式，场景不复制公式或替换服务端世界查询。 */
    private abstract static class GrowthProbe extends CropBlock {
        private GrowthProbe() {
            super(BlockBehaviour.Properties.of());
        }

        static float speed(BlockState state, BlockGetter level, BlockPos pos) {
            return getGrowthSpeed(state, level, pos);
        }
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
            level.setBlock(pos, state, FLAGS);
            if (data != null) {
                level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
            }
        }
    }
}
