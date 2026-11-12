package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlockEntity;

/** 在主检查已保存的小场地中验证植物播种、采收、桃苗催化与食材加工的原生入口。 */
final class PlantFoodSupplyCheck {
    private final ServerPlayer player;
    private final ServerLevel level;
    private final BlockPos pos;
    private final BiConsumer<String, Boolean> report;

    private PlantFoodSupplyCheck(ServerPlayer player, BlockPos pos, BiConsumer<String, Boolean> report) {
        this.player = player;
        this.level = player.serverLevel();
        this.pos = pos;
        this.report = report;
    }

    static void run(ServerPlayer player, BlockPos pos, BiConsumer<String, Boolean> report) {
        PlantFoodSupplyCheck check = new PlantFoodSupplyCheck(player, pos, report);
        check.fungi();
        check.legumes();
        check.sesame();
        check.grinding();
        check.fibersAndSaplings();
        check.clearDrops();
    }

    private void fungi() {
        for (String number : List.of("040", "041")) {
            boolean placed = true;
            BlockPos support = pos.above();
            for (Direction direction : Direction.values()) {
                reset(Blocks.DIRT.defaultBlockState());
                level.setBlock(support, Blocks.OAK_LOG.defaultBlockState(), 3);
                hold("shicai_" + number, 2);
                use(support, direction);
                BlockState fungus = level.getBlockState(support.relative(direction));
                placed &= fungus.is(block("shicai_" + number + "_plant_01").getBlock())
                        && fungus.getValue(BlockStateProperties.FACING) == direction
                        && player.getMainHandItem().getCount() == 1;
            }
            report.accept(number + "食材经原生生存右键贴附原木六面并扣一件", placed);
        }
        for (String number : List.of("042", "043", "044", "045")) {
            reset(Blocks.MYCELIUM.defaultBlockState());
            hold("shicai_" + number, 2);
            use(pos.below(), Direction.UP);
            boolean planted = level.getBlockState(pos).is(block("shicai_" + number + "_plant_01").getBlock())
                    && player.getMainHandItem().getCount() == 1;
            reset(Blocks.DIRT.defaultBlockState());
            hold("shicai_" + number, 2);
            use(pos.below(), Direction.UP);
            report.accept(number + "菌丝顶面可种植，普通泥土拒绝且保留手持数量",
                    planted && level.isEmptyBlock(pos) && player.getMainHandItem().getCount() == 2);
        }
        reset(Blocks.MYCELIUM.defaultBlockState());
        BlockState attached = block("shicai_040_plant_01")
                .setValue(BlockStateProperties.FACING, Direction.UP)
                .setValue(BlockStateProperties.WATERLOGGED, true);
        level.setBlock(pos, attached, 2);
        ((BonemealableBlock) attached.getBlock()).performBonemeal(level, level.random, pos, attached);
        BlockState grown = level.getBlockState(pos);
        report.accept("040原生骨粉成功回调直接成熟并保留朝向与含水属性",
                grown.is(block("shicai_040_plant_04").getBlock())
                        && grown.getValue(BlockStateProperties.FACING) == Direction.UP
                        && grown.getValue(BlockStateProperties.WATERLOGGED));
        reset(Blocks.MYCELIUM.defaultBlockState());
        BlockState ground = block("shicai_042_plant_01");
        level.setBlock(pos, ground, 2);
        ((BonemealableBlock) ground.getBlock()).performBonemeal(level, level.random, pos, ground);
        report.accept("042原生骨粉成功回调只推进一个阶段",
                level.getBlockState(pos).is(block("shicai_042_plant_02").getBlock()));
        boolean drops = true;
        for (String[] entry : new String[][] {{"040", "04", "5"}, {"041", "04", "5"},
                {"042", "05", "5"}, {"043", "04", "3"}, {"044", "04", "3"}, {"045", "04", "3"}}) {
            List<ItemStack> loot = Block.getDrops(block("shicai_" + entry[0] + "_plant_" + entry[1]), level, pos, null);
            drops &= loot.size() == 1 && loot.getFirst().is(item("shicai_" + entry[0], 1).getItem())
                    && loot.getFirst().getCount() == Integer.parseInt(entry[2]);
        }
        report.accept("040–045成熟态原生掉落分别为5/5/5/3/3/3件本体", drops);
    }

    private void legumes() {
        for (int number = 46; number <= 56; number++) {
            String id = String.format("%03d", number);
            reset(Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
            hold("shicai_" + id, 2);
            use(pos.below(), Direction.UP);
            boolean support = number == 52 || number == 53;
            report.accept(id + (support ? "支架食材原生耕地右键只扣一件而不放置" : "食材原生耕地右键放首阶段并扣一件"),
                    player.getMainHandItem().getCount() == 1 && (support ? level.isEmptyBlock(pos)
                            : level.getBlockState(pos).is(block("shicai_" + id + "_plant_01").getBlock())));
        }
        for (String number : List.of("052", "053")) {
            reset(Blocks.DIRT.defaultBlockState());
            hold("shicai_" + number, 2);
            use(pos.below(), Direction.UP);
            boolean planted = level.getBlockState(pos).is(block("shicai_" + number + "_plant_01").getBlock())
                    && player.getMainHandItem().getCount() == 1;
            BlockState first = block("shicai_" + number + "_plant_01")
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                    .setValue(BlockStateProperties.WATERLOGGED, true);
            level.setBlock(pos, first, 2);
            ((BonemealableBlock) first.getBlock()).performBonemeal(level, level.random, pos, first);
            BlockState mature = level.getBlockState(pos);
            report.accept(number + "泥土可种支架，骨粉成功到05并保持西向与含水",
                    planted && mature.is(block("shicai_" + number + "_plant_05").getBlock())
                            && mature.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST
                            && mature.getValue(BlockStateProperties.WATERLOGGED));
            holdEmpty();
            clearDrops();
            use(pos, Direction.UP);
            List<ItemEntity> drops = drops();
            BlockState recovered = level.getBlockState(pos);
            report.accept(number + "原生空手收获交三个独立食材实体并恢复04默认状态",
                    drops.size() == 3 && drops.stream().allMatch(drop -> drop.getItem().is(item("shicai_" + number, 1).getItem())
                            && drop.getItem().getCount() == 1)
                            && recovered.equals(block("shicai_" + number + "_plant_04")));
        }
        reset(Blocks.DIRT.defaultBlockState());
        BlockState greenBean = block("shicai_048_plant_04");
        level.setBlock(pos, greenBean, 2);
        greenBean.randomTick(level, pos, level.random);
        report.accept("04804保留randomTicks声明但原生随机刻不执行成长或低湿度销毁",
                greenBean.isRandomlyTicking() && level.getBlockState(pos).equals(greenBean));
        holdEmpty();
        clearDrops();
        use(pos, Direction.UP);
        report.accept("04804仍可原生空手收获三个绿豆并恢复05",
                drops().size() == 3 && level.getBlockState(pos).is(block("shicai_048_plant_05").getBlock()));
        reset(Blocks.DIRT.defaultBlockState());
        List<ItemStack> empty = Block.getDrops(block("shicai_053_plant_04"), level, pos, null);
        report.accept("05304原生掉落为空且保留骨粉接口",
                empty.isEmpty() && block("shicai_053_plant_04").getBlock() instanceof BonemealableBlock);
        reset(Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
        BlockState young = block("shicai_046_plant_01");
        level.setBlock(pos, young, 2);
        clearDrops();
        young.randomTick(level, pos, level.random);
        report.accept("046幼苗原生随机刻在低湿度先掉食材再移除",
                level.isEmptyBlock(pos) && drops().size() == 1
                        && drops().getFirst().getItem().is(item("shicai_046", 1).getItem()));
    }

    private void sesame() {
        reset(Blocks.DIRT.defaultBlockState());
        hold("shicai_05701", 2);
        use(pos.below(), Direction.UP);
        BlockState lower = level.getBlockState(pos);
        BlockState upper = level.getBlockState(pos.above());
        report.accept("05701双株物品经原生生存使用放上下两格并扣一件",
                lower.is(block("shicai_05701").getBlock()) && upper.is(lower.getBlock())
                        && lower.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
                        && upper.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                        && player.getMainHandItem().getCount() == 1);
        List<ItemStack> loot = Block.getDrops(lower, level, pos, null, player, new ItemStack(Items.IRON_AXE));
        report.accept("05701原生掉落表产出黑或白芝麻，本次无附魔掉落在1至12件范围",
                !loot.isEmpty() && loot.stream().allMatch(stack -> stack.is(item("shicai_057", 1).getItem())
                        || stack.is(item("shicai_058", 1).getItem()))
                        && loot.stream().mapToInt(ItemStack::getCount).sum() >= 1
                        && loot.stream().mapToInt(ItemStack::getCount).sum() <= 12);
    }

    private void grinding() {
        reset(Blocks.STONE.defaultBlockState());
        level.setBlock(pos, block("stonemill"), 3);
        StoneMillBlockEntity mill = (StoneMillBlockEntity) level.getBlockEntity(pos);
        boolean shifted = player.isShiftKeyDown();
        player.setShiftKeyDown(false);
        holdEmpty();
        for (String[] recipe : new String[][] {{"046", "food_039", "foodwan"},
                {"048", "food_003", "foodwan"}, {"058", "tiaowei_08", ""}}) {
            mill.clearContent();
            mill.setItem(0, item("shicai_" + recipe[0], 16));
            if (!recipe[2].isEmpty()) mill.setItem(2, item(recipe[2], 4));
            use(pos, Direction.UP);
            report.accept(recipe[0] + "食材经原生石磨右键四轮消耗16件并产4件" + recipe[1],
                    mill.getItem(0).isEmpty() && mill.getItem(2).isEmpty()
                            && mill.getItem(1).is(item(recipe[1], 1).getItem()) && mill.getItem(1).getCount() == 4);
        }
        mill.clearContent();
        player.setShiftKeyDown(shifted);
    }

    private void fibersAndSaplings() {
        for (String[] entry : new String[][] {{"mianseed", "mian_plant_01"}, {"maseed", "ma_plant_01"}}) {
            reset(Blocks.FARMLAND.defaultBlockState());
            hold(entry[0], 2);
            use(pos.below(), Direction.UP);
            boolean planted = level.getBlockState(pos).is(block(entry[1]).getBlock())
                    && player.getMainHandItem().getCount() == 1;
            reset(Blocks.DIRT.defaultBlockState());
            hold(entry[0], 2);
            use(pos.below(), Direction.UP);
            report.accept(entry[0] + "原生耕地播种扣一件，普通泥土拒绝且不扣种子",
                    planted && level.isEmptyBlock(pos) && player.getMainHandItem().getCount() == 2);
        }

        reset(Blocks.DIRT.defaultBlockState());
        hold("nichanggrassseed", 2);
        use(pos.below(), Direction.UP);
        boolean grassPlanted = level.getBlockState(pos).is(block("nichanggrass_01").getBlock())
                && player.getMainHandItem().getCount() == 1;
        reset(Blocks.STONE.defaultBlockState());
        hold("nichanggrassseed", 2);
        use(pos.below(), Direction.UP);
        report.accept("霓裳草种子原生泥土播种扣一件，石头拒绝且不扣种子",
                grassPlanted && level.isEmptyBlock(pos) && player.getMainHandItem().getCount() == 2);

        reset(Blocks.DIRT.defaultBlockState());
        level.setBlock(pos, block("mian_plant_05"), 2);
        holdEmpty();
        use(pos, Direction.UP);
        List<ItemEntity> cotton = drops();
        report.accept("成熟棉花原生右键恢复04，固定交一份棉花并可额外交一份种子",
                level.getBlockState(pos).is(block("mian_plant_04").getBlock())
                        && cotton.stream().filter(drop -> drop.getItem().is(item("mianitem", 1).getItem()))
                                .mapToInt(drop -> drop.getItem().getCount()).sum() == 1
                        && cotton.stream().allMatch(drop -> drop.getItem().getCount() == 1
                                && (drop.getItem().is(item("mianitem", 1).getItem())
                                        || drop.getItem().is(item("mianseed", 1).getItem())))
                        && cotton.size() >= 1 && cotton.size() <= 2);

        reset(Blocks.DIRT.defaultBlockState());
        level.setBlock(pos, block("ma_plant_04"), 2);
        holdEmpty();
        boolean broken = player.gameMode.destroyBlock(pos);
        List<ItemEntity> hemp = drops();
        int fibers = hemp.stream().filter(drop -> drop.getItem().is(item("maitem", 1).getItem()))
                .mapToInt(drop -> drop.getItem().getCount()).sum();
        report.accept("成熟麻原生玩家破坏交一份种子和一至两份麻纤维",
                broken && level.isEmptyBlock(pos) && fibers >= 1 && fibers <= 2
                        && hemp.stream().filter(drop -> drop.getItem().is(item("maseed", 1).getItem()))
                                .mapToInt(drop -> drop.getItem().getCount()).sum() == 1
                        && hemp.stream().allMatch(drop -> drop.getItem().is(item("maitem", 1).getItem())
                                || drop.getItem().is(item("maseed", 1).getItem())));

        for (String catalyst : List.of("jingxve", "xvejing", "cailiao_200")) {
            reset(Blocks.DIRT.defaultBlockState());
            level.setBlock(pos, block("peachsapling"), 2);
            hold(catalyst, 2);
            use(pos, Direction.UP);
            report.accept(catalyst + "原生右键桃苗转血桃苗并扣一件",
                    level.getBlockState(pos).is(block("peachsaplingxve").getBlock())
                            && player.getMainHandItem().getCount() == 1);
        }
        reset(Blocks.DIRT.defaultBlockState());
        level.setBlock(pos, block("peachsapling"), 2);
        hold("mianseed", 2);
        use(pos, Direction.UP);
        report.accept("普通物品右键桃苗保持树苗与手持数量",
                level.getBlockState(pos).is(block("peachsapling").getBlock())
                        && player.getMainHandItem().getCount() == 2);
    }

    private void reset(BlockState soil) {
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 3, 1))) {
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 2);
        }
        level.setBlock(pos.below(), soil, 2);
        clearDrops();
    }

    private void hold(String id, int count) {
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, item(id, count));
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    private void holdEmpty() {
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    private void use(BlockPos at, Direction direction) {
        player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(at).relative(direction, 0.5), direction, at, false));
    }

    private List<ItemEntity> drops() {
        return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2));
    }

    private void clearDrops() {
        drops().forEach(Entity::discard);
    }

    private static BlockState block(String id) {
        return BuiltInRegistries.BLOCK.get(id(id)).defaultBlockState();
    }

    private static ItemStack item(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(id)), count);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
