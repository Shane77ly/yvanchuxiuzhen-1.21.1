package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 只用已取样自然桃叶掉出的林桃，串接 Material 食用及 Plant 普通桃树。 */
final class NaturalPeachChain {
    static final int MAX_LEAVES = 12;
    private static final int MAX_GROWTH_ATTEMPTS = 512;
    private final SystemCheck check;
    private final List<String> report;
    private final ResourceKey<Level> dimension;
    private final List<BlockPos> leaves = new ArrayList<>(MAX_LEAVES);
    private PlayerState original;
    private BlockPos harvest;
    private BlockPos root;
    private String grownCanopy;
    private int originalRandomTicks;

    NaturalPeachChain(SystemCheck check, List<String> report, ResourceKey<Level> dimension) {
        this.check = check;
        this.report = report;
        this.dimension = dimension;
    }

    int collect(LevelChunk chunk, List<BlockPos> roots) {
        Block leaf = block("peachleaves");
        // 原生桃林树干高 7–9，树冠锚在干顶，半径至多 2，最低相对根部 +4。
        for (BlockPos rootPos : roots) {
            for (BlockPos point : BlockPos.betweenClosed(rootPos.offset(-2, 4, -2), rootPos.offset(2, 9, 2))) {
                if (leaves.size() == MAX_LEAVES) return leaves.size();
                if (!new ChunkPos(point).equals(chunk.getPos()) || !chunk.getBlockState(point).is(leaf) || leaves.contains(point)) continue;
                leaves.add(point.immutable());
                info("PEACH_SAMPLE leaf=" + point.toShortString() + " treeRoot=" + rootPos.toShortString());
            }
        }
        return leaves.size();
    }

    boolean hasSample() {
        return !leaves.isEmpty();
    }

    boolean harvestAndEat(ServerPlayer player) {
        if (leaves.isEmpty()) {
            info("未覆盖：已取样 FULL 没有自然桃叶");
            return false;
        }
        Item fruit = item("peachpeach");
        Item kernel = item("peachkernel");
        if (player.getInventory().countItem(fruit) != 0 || player.getInventory().countItem(kernel) != 0) {
            check.fail("自然林桃需要初始库存无林桃及桃核");
            return false;
        }
        ServerLevel level = player.server.getLevel(dimension);
        var food = player.getFoodData();
        var regeneration = level.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION);
        original = new PlayerState(player.serverLevel(), player.position(), player.getYRot(), player.getXRot(),
                player.gameMode.getGameModeForPlayer(), player.getMainHandItem(), food.getFoodLevel(),
                food.getSaturationLevel(), regeneration.get());
        boolean started = false;
        try {
            player.setGameMode(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            Block leaf = block("peachleaves");
            int broken = 0;
            for (BlockPos point : leaves) {
                if (!level.getBlockState(point).is(leaf)) continue;
                player.teleportTo(level, point.getX() + 0.5, point.getY() + 1, point.getZ() + 0.5, 0, 0);
                AABB area = new AABB(point).inflate(2);
                Set<UUID> before = new HashSet<>();
                fruitDrops(level, area, fruit).forEach(drop -> before.add(drop.getUUID()));
                if (!player.gameMode.destroyBlock(point) || level.getBlockState(point).is(leaf)) {
                    check.fail("未从生存入口破坏实得桃叶 " + point.toShortString());
                    return false;
                }
                broken++;
                for (ItemEntity drop : fruitDrops(level, area, fruit)) {
                    if (before.contains(drop.getUUID())) continue;
                    drop.setNoPickUpDelay();
                    drop.playerTouch(player);
                    check.expectTrue("自然林桃掉落经原生实体拾取", drop.isRemoved());
                    if (drop.isRemoved()) harvest = point;
                }
                if (harvest != null) break;
            }
            if (harvest == null) {
                info("未覆盖：" + broken + " 次自然桃叶生存采收没有拾得林桃");
                return false;
            }
            var inventory = player.getInventory();
            int slot = inventory.findSlotMatchingItem(new ItemStack(fruit));
            if (slot < 0 || inventory.countItem(fruit) != 1) {
                check.fail("实得林桃没有恰好入主库存一枚");
                return false;
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, inventory.removeItem(slot, 1));
            ChunkPos plotChunk = new ChunkPos(harvest);
            root = new BlockPos(plotChunk.getMinBlockX() + 8, level.getMaxBuildHeight() - 20, plotChunk.getMinBlockZ() + 8);
            for (BlockPos soil : BlockPos.betweenClosed(root.offset(-1, -1, -1), root.offset(1, -1, 1))) {
                level.setBlock(soil, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            }
            player.teleportTo(level, root.getX() + 1.5, root.getY(), root.getZ() + 0.5, 0, 0);
            regeneration.set(false, player.server);
            food.setFoodLevel(10);
            food.setSaturation(0);
            started = player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND)
                    .consumesAction() && player.isUsingItem();
            info("PEACH_USE fruitClass=" + fruit.getClass().getName()
                    + " kernelClass=" + kernel.getClass().getName());
            check.expectTrue("实得林桃进入原生食用计时", started);
            return started;
        } finally {
            if (!started) restore(player, level);
        }
    }

    boolean plantAfterMeal(ServerPlayer player, boolean expired) {
        if (player.isUsingItem() && !expired) return false;
        ServerLevel level = player.server.getLevel(dimension);
        try {
            ItemStack kernel = player.getMainHandItem();
            if (player.isUsingItem()) {
                check.fail("自然林桃食用计时超限，剩余=" + player.getUseItemRemainingTicks());
                return true;
            }
            if (!kernel.is(item("peachkernel")) || kernel.getCount() != 1) {
                check.fail("自然林桃未实际得到一枚手持桃核：主手=" + kernel
                        + "，饥饿=" + player.getFoodData().getFoodLevel());
                return true;
            }
            check.expect("自然林桃食用增加四点饥饿", 14, player.getFoodData().getFoodLevel());
            BlockPos soil = root.below();
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(soil), Direction.UP, soil, false);
            Block sapling = block("peachsapling");
            boolean planted = player.gameMode.useItemOn(player, level, kernel, InteractionHand.MAIN_HAND, hit).consumesAction()
                    && kernel.isEmpty() && level.getBlockState(root).is(sapling);
            check.expectTrue("该枚实得桃核经原生顶面播种入口扣一并生成普通桃苗", planted);
            if (planted) {
                // 玩家移至树冠上方后再触发原生随机刻回调。
                player.teleportTo(level, root.getX() + 0.5, root.getY() + 10, root.getZ() + 0.5, 0, 90);
                for (int attempt = 0; attempt < MAX_GROWTH_ATTEMPTS && level.getBlockState(root).is(sapling); attempt++) {
                    level.getBlockState(root).randomTick(level, root, level.random);
                }
                grownCanopy = canopy(level);
                check.expectTrue("自然实得桃核长成来源普通桃树", grownCanopy != null);
                if (grownCanopy != null) {
                    var randomTicks = level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING);
                    originalRandomTicks = randomTicks.get();
                    randomTicks.set(0, player.server);
                    info("PEACH_CHAIN leaf=" + harvest.toShortString() + " tree=" + root.toShortString() + " " + grownCanopy);
                }
            }
            return true;
        } finally {
            restore(player, level);
        }
    }

    boolean grown() {
        return grownCanopy != null;
    }

    void checkReloaded(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(dimension);
        try {
            check.expectTrue("同档重进：采收桃叶未复原", !level.getBlockState(harvest).is(block("peachleaves")));
            check.expect("同档重进：保留实得桃核成长后的树冠", grownCanopy, canopy(level));
        } finally {
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(originalRandomTicks, player.server);
        }
    }

    private String canopy(ServerLevel level) {
        Block log = block("peachlog");
        for (int y = 0; y < 4; y++) if (!level.getBlockState(root.above(y)).is(log)) return null;
        Block leaf = block("peachleaves");
        Block blossom = block("peachflower");
        int leafCount = 0;
        int blossomCount = 0;
        for (BlockPos pos : BlockPos.betweenClosed(root.offset(-2, 4, -2), root.offset(2, 8, 2))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(leaf)) leafCount++;
            if (state.is(blossom)) blossomCount++;
        }
        if (leafCount == 74 && blossomCount == 0) return "桃叶74";
        if (leafCount == 0 && blossomCount == 74) return "桃花74";
        return null;
    }

    private void restore(ServerPlayer player, ServerLevel level) {
        if (player.isUsingItem()) player.stopUsingItem();
        player.setItemInHand(InteractionHand.MAIN_HAND, original.hand());
        player.getFoodData().setFoodLevel(original.food());
        player.getFoodData().setSaturation(original.saturation());
        level.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(original.regeneration(), player.server);
        player.setGameMode(original.mode());
        Vec3 pos = original.position();
        player.teleportTo(original.level(), pos.x, pos.y, pos.z, original.yaw(), original.pitch());
        original = null;
    }

    private static List<ItemEntity> fruitDrops(ServerLevel level, AABB area, Item fruit) {
        return level.getEntitiesOfClass(ItemEntity.class, area, drop -> drop.getItem().is(fruit));
    }

    private void info(String text) {
        report.add("INFO 自然林桃 " + text);
    }

    private static Block block(String name) {
        return BuiltInRegistries.BLOCK.getOptional(id(name)).orElseThrow();
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.getOptional(id(name)).orElseThrow();
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name);
    }

    private record PlayerState(ServerLevel level, Vec3 position, float yaw, float pitch, GameType mode, ItemStack hand,
            int food, float saturation, boolean regeneration) {}
}
