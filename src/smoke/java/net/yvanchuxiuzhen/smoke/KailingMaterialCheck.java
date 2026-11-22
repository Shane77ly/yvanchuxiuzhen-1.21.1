package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.equipment.content.ToolMachineEntity;

/** 受控推进四种开灵工具机，检查来源材质选择、五刻消费、主人限制及植物和叶片边界。 */
final class KailingMaterialCheck extends ActionCheck {
    private record Sample(Block block, boolean accepted) {
    }

    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<Entity> created = new ArrayList<>();
    private Set<UUID> previousDrops;
    private BlockPos site;
    private AABB area;

    KailingMaterialCheck(List<String> report, String levelId) {
        super("开灵工具机来源材质", report, levelId);
        script(new Action(1, this::check));
    }

    private void check(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        site = player.blockPosition().offset(4, 6, 0);
        area = new AABB(site).inflate(16);
        previousDrops = player.serverLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                .map(Entity::getUUID).collect(Collectors.toSet());
        prepareMatrix(player, ToolMachineEntity.Kind.PICKAXE, List.of(
                new Sample(Blocks.STONE, true), new Sample(Blocks.ICE, true), new Sample(Blocks.FROSTED_ICE, true),
                new Sample(Blocks.CLAY, true), new Sample(Blocks.HONEY_BLOCK, true), new Sample(Blocks.SLIME_BLOCK, true),
                new Sample(Blocks.INFESTED_STONE, true), new Sample(block("taopi"), true),
                new Sample(Blocks.COPPER_BLOCK, false), new Sample(Blocks.PACKED_ICE, false),
                new Sample(Blocks.AMETHYST_BLOCK, false), new Sample(Blocks.BEDROCK, false)));
        prepareMatrix(player, ToolMachineEntity.Kind.HOE, List.of(
                new Sample(Blocks.DIRT, true), new Sample(Blocks.FARMLAND, true), new Sample(Blocks.DIRT_PATH, true),
                new Sample(Blocks.SOUL_SOIL, true), new Sample(Blocks.GRAVEL, true), new Sample(Blocks.SOUL_SAND, true),
                new Sample(Blocks.RED_CONCRETE_POWDER, true), new Sample(Blocks.HAY_BLOCK, true),
                new Sample(Blocks.NETHER_WART_BLOCK, true), new Sample(Blocks.SNOW, true), new Sample(block("snowdrift"), true),
                new Sample(block("loess"), true), new Sample(Blocks.SNOW_BLOCK, false),
                new Sample(Blocks.MOSS_BLOCK, false), new Sample(Blocks.CRIMSON_NYLIUM, false)));
        prepareMatrix(player, ToolMachineEntity.Kind.SHOVEL, List.of(
                new Sample(Blocks.DIRT, true), new Sample(Blocks.COARSE_DIRT, true), new Sample(Blocks.GRASS_BLOCK, true),
                new Sample(Blocks.FARMLAND, false), new Sample(Blocks.SOUL_SOIL, false), new Sample(block("loess"), false)));
        consume(player, ToolMachineEntity.Kind.PICKAXE, Blocks.HONEY_BLOCK);
        consume(player, ToolMachineEntity.Kind.HOE, Blocks.DIRT);
        till(player);
        leaves(player);
    }

    private void prepareMatrix(ServerPlayer player, ToolMachineEntity.Kind kind, List<Sample> samples) {
        ToolMachineEntity machine = machine(player, kind);
        for (Sample sample : samples) {
            setBlock(player, site, sample.block().defaultBlockState());
            phase(machine, "prepare", player.getStringUUID());
            machine.tick();
            String actual = machine.getPersistentData().getString("jinxing");
            expectTrue(kind.id() + "来源材质准备判定" + BuiltInRegistries.BLOCK.getKey(sample.block()),
                    actual.equals(sample.accepted() ? "mine" : "finish")
                            && player.serverLevel().getBlockState(site).is(sample.block()));
        }
        machine.discard();
    }

    private void consume(ServerPlayer player, ToolMachineEntity.Kind kind, Block block) {
        ToolMachineEntity machine = machine(player, kind);
        setBlock(player, site, block.defaultBlockState());
        int ownerDrops = drops(player, block, player.getBoundingBox().inflate(3));
        int targetDrops = drops(player, block, new AABB(site).inflate(2));
        phase(machine, "mine", UUID.randomUUID().toString());
        machine.tick();
        for (int tick = 0; tick < 5; tick++) machine.tick();
        expectTrue(kind.id() + "主人不在本维度时完成等待但保留方块",
                player.serverLevel().getBlockState(site).is(block)
                        && machine.getPersistentData().getString("jinxing").equals("finish")
                        && drops(player, block, player.getBoundingBox().inflate(3)) == ownerDrops
                        && drops(player, block, new AABB(site).inflate(2)) == targetDrops);
        phase(machine, "mine", player.getStringUUID());
        machine.tick();
        for (int tick = 0; tick < 4; tick++) machine.tick();
        expectTrue(kind.id() + "前四采掘刻保留方块", player.serverLevel().getBlockState(site).is(block));
        machine.tick();
        expectTrue(kind.id() + "第五采掘刻消费来源允许方块", player.serverLevel().getBlockState(site).isAir()
                && machine.getPersistentData().getString("jinxing").equals("finish")
                && drops(player, block, player.getBoundingBox().inflate(3)) == ownerDrops + 1
                && drops(player, block, new AABB(site).inflate(2)) == targetDrops);
        setBlock(player, site, block.defaultBlockState());
        phase(machine, "mining", "");
        machine.getPersistentData().putDouble("xiaoshitick2", 4);
        machine.tick();
        expectTrue(kind.id() + "空主人在目标位置交付一件掉落", player.serverLevel().getBlockState(site).isAir()
                && drops(player, block, new AABB(site).inflate(2)) == targetDrops + 1);
        machine.discard();
    }

    private int drops(ServerPlayer player, Block block, AABB bounds) {
        return player.serverLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                entity -> entity.getItem().is(block.asItem())).stream()
                .mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private void till(ServerPlayer player) {
        ToolMachineEntity machine = machine(player, ToolMachineEntity.Kind.SHOVEL);
        BlockPos control = site.offset(2, 0, 0);
        List<Sample> samples = List.of(new Sample(Blocks.DANDELION, true), new Sample(Blocks.OAK_SAPLING, true), new Sample(Blocks.GLOW_LICHEN, true),
                new Sample(Blocks.MOSS_CARPET, true), new Sample(block("ironbamboo"), true),
                new Sample(Blocks.SNOW, false), new Sample(Blocks.FIRE, false),
                new Sample(Blocks.WATER, false), new Sample(Blocks.OAK_LEAVES, false), new Sample(Blocks.CHERRY_SAPLING, false));
        for (Sample sample : samples) {
            for (BlockPos pos : List.of(site, control)) {
                setBlock(player, pos.below(), Blocks.STONE.defaultBlockState());
                setBlock(player, pos, Blocks.DIRT.defaultBlockState());
                setBlock(player, pos.above(), sample.block().defaultBlockState());
            }
            if (sample.accepted()) player.serverLevel().setBlock(control.above(), Blocks.AIR.defaultBlockState(), 3);
            player.serverLevel().setBlock(control, Blocks.FARMLAND.defaultBlockState(), 3);
            player.serverLevel().setBlock(control, player.serverLevel().getBlockState(control)
                    .setValue(BlockStateProperties.MOISTURE, 7), 3);
            phase(machine, "mining", UUID.randomUUID().toString());
            machine.getPersistentData().putDouble("xiaoshitick2", 4);
            machine.tick();
            BlockState actual = player.serverLevel().getBlockState(site);
            expectTrue("开灵锄上方来源植物规则" + BuiltInRegistries.BLOCK.getKey(sample.block()),
                    actual.is(Blocks.FARMLAND) && actual.getValue(BlockStateProperties.MOISTURE) == 7
                            && player.serverLevel().getBlockState(site.above()).equals(player.serverLevel().getBlockState(control.above())));
        }
        machine.discard();
    }

    private void leaves(ServerPlayer player) {
        ToolMachineEntity machine = machine(player, ToolMachineEntity.Kind.AXE);
        for (BlockPos pos : BlockPos.betweenClosed(site.offset(-1, -1, -1), site.offset(1, 1, 1))) {
            setBlock(player, pos.immutable(), Blocks.AIR.defaultBlockState());
        }
        BlockPos leaf = site.offset(1, 0, 0);
        for (Sample sample : List.of(new Sample(Blocks.OAK_LEAVES, true), new Sample(block("jadelogleaves"), true),
                new Sample(Blocks.CHERRY_LEAVES, false), new Sample(Blocks.MOSS_BLOCK, false))) {
            setBlock(player, leaf, sample.block().defaultBlockState());
            machine.moveTo(site.getX() + .5, site.getY(), site.getZ() + .5, 0, 0);
            machine.setDeltaMovement(Vec3.ZERO);
            phase(machine, "path", player.getStringUUID());
            machine.getPersistentData().putDouble("mubiaoX", site.getX() + 4);
            machine.tick();
            expectTrue("开灵斧附近叶片检测" + BuiltInRegistries.BLOCK.getKey(sample.block()),
                    machine.getPersistentData().getString("jinxing").equals(sample.accepted() ? "clear_leaves" : "path"));
            machine.moveTo(site.getX() + .5, site.getY(), site.getZ() + .5, 0, 0);
            machine.setDeltaMovement(Vec3.ZERO);
            phase(machine, "clear_leaves", player.getStringUUID());
            machine.tick();
            expectTrue("开灵斧三格叶片清理" + BuiltInRegistries.BLOCK.getKey(sample.block()),
                    sample.accepted() ? player.serverLevel().getBlockState(leaf).isAir()
                            : player.serverLevel().getBlockState(leaf).is(sample.block()));
        }
        machine.discard();
    }

    private ToolMachineEntity machine(ServerPlayer player, ToolMachineEntity.Kind kind) {
        ToolMachineEntity machine = EquipmentSetup.toolMachineType(kind).get().create(player.serverLevel());
        created.add(machine);
        machine.setNoAi(true);
        machine.moveTo(site.getX() + .5, site.getY() + 2, site.getZ() + .5, 0, 0);
        machine.getPersistentData().putDouble("fumodengji", 1);
        machine.getPersistentData().putDouble("xiaoshitickMax", 10000);
        return machine;
    }

    private void phase(ToolMachineEntity machine, String phase, String owner) {
        CompoundTag data = machine.getPersistentData();
        data.putString("jinxing", phase);
        data.putString("zhurenuuid", owner);
        data.putDouble("xiaoshitick2", 0);
        data.putDouble("mubiaoX", site.getX());
        data.putDouble("mubiaoY", site.getY());
        data.putDouble("mubiaoZ", site.getZ());
    }

    private void setBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        blocks.putIfAbsent(pos, player.serverLevel().getBlockState(pos));
        player.serverLevel().setBlock(pos, state, 2);
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        created.forEach(Entity::discard);
        blocks.forEach((pos, state) -> player.serverLevel().setBlock(pos, state, 2));
        player.serverLevel().getEntitiesOfClass(ItemEntity.class, area,
                entity -> !previousDrops.contains(entity.getUUID())).forEach(Entity::discard);
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
    }
}
