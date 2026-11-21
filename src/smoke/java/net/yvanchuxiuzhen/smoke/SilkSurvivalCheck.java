package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.craft.content.SilkwormBoxBlockEntity;

/** 仅操作已准备的人间道生存副本；原位采集和原生菜单消费均在集成服务端执行。 */
final class SilkSurvivalCheck extends SystemCheck {
    private static final BlockPos TABLE = new BlockPos(4298, 66, 4413);
    private static final BlockPos BOX = TABLE.west();
    private static final BlockPos LOG_BASE = new BlockPos(4299, 66, 4414);
    private static final List<BlockPos> LEAVES = leaves();
    private final Block leaf = block("mulberryleaves"), log = block("camphorlog"), box = block("canhe");
    private final Item board = block("camphorplanks").asItem();
    private final Item berry = item("mulberrymulberry"), sapling = item("mulberrysapling");
    private final List<String> report;
    private int leafIndex, logs, minedBefore, inventoryBefore, sequence;
    private boolean haveLeaf, stopSent;
    private boolean approached;
    private Set<Integer> existingDrops;
    private long started, miningTicks;
    private BlockPos target;
    private Block expected;
    private Phase phase = Phase.START;

    private enum Phase { START, SELECT, MINING, PICKUP, CRAFT, BOX }

    SilkSurvivalCheck(List<String> report, String levelId) {
        super("自然桑叶制盒", report, levelId);
        this.report = report;
    }

    private static List<BlockPos> leaves() {
        List<BlockPos> result = new ArrayList<>();
        for (int y = 68; y <= 69; y++) {
            for (int x = 4294; x <= 4296; x++) result.add(new BlockPos(x, y, 4414));
            for (int x = 4293; x <= 4297; x++) result.add(new BlockPos(x, y, 4415));
        }
        for (int x = 4294; x <= 4296; x++) result.add(new BlockPos(x, 70, 4415));
        return List.copyOf(result);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) return inWorld(local);
        if (minecraft.getSingleplayerServer() == null || minecraft.player == null || local > 3600) {
            fail("世界已退出或阶段超时：" + phase);
            finish();
            return false;
        }
        onServer(p -> {
            try {
                require(p.isAlive() && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                        && p.level().dimension().location().toString().equals("yvanchuxiuzhen:dimensionrenjian"),
                        "玩家不在人间生存状态");
                advance(p);
            } catch (RuntimeException e) {
                fail(phase + " @ " + target + "：" + e);
                finish();
            }
        });
        return false;
    }

    private void advance(ServerPlayer p) {
        long now = p.level().getGameTime();
        switch (phase) {
            case START -> {
                require(p.server.getWorldData().worldGenOptions().seed() == -5586163949850800998L
                        && p.position().distanceToSqr(4298.5, 66, 4414.5) < 1
                        && p.containerMenu == p.inventoryMenu && p.getInventory().isEmpty()
                        && p.inventoryMenu.getCarried().isEmpty(), "起点、种子、菜单或背包不是准备态");
                for (BlockPos pos : LEAVES) at(p, pos, leaf);
                for (int k = 0; k < 3; k++) at(p, LOG_BASE.above(k), log);
                for (BlockPos pos : List.of(TABLE, BOX)) {
                    at(p, pos, Blocks.SHORT_GRASS);
                    at(p, pos.below(), Blocks.GRASS_BLOCK);
                }
                info("起点确认：19格原叶、3段原木；仅调整服务端站位，等待原生碰撞拾取");
                phase = Phase.SELECT;
            }
            case SELECT -> {
                if (!haveLeaf) {
                    require(leafIndex < LEAVES.size(), "已逐格用尽19格原叶，未拾得桑叶");
                    target = LEAVES.get(leafIndex++);
                    expected = leaf;
                    stand(p, target.getX() + 0.5, target.getZ() + 0.5);
                } else {
                    target = LOG_BASE.above(logs);
                    expected = log;
                    stand(p, 4298.69, 4414.5);
                }
                at(p, target, expected);
                select(p, Items.AIR);
                require(p.canInteractWithBlock(target, 0), "原位不在生存交互距离内");
                minedBefore = mined(p, expected);
                inventoryBefore = gathered(p);
                existingDrops = nearbyDrops(p).stream().map(ItemEntity::getId).collect(Collectors.toSet());
                approached = false;
                float progress = p.level().getBlockState(target).getDestroyProgress(p, p.level(), target);
                require(progress > 0, "该方块不能徒手采掘");
                miningTicks = (long) Math.ceil(1.0 / progress);
                started = now;
                stopSent = false;
                dig(p, Action.START_DESTROY_BLOCK);
                phase = Phase.MINING;
            }
            case MINING -> {
                if (p.level().getBlockState(target).isAir()) {
                    require(mined(p, expected) == minedBefore + 1, "原位已空但无玩家采掘统计");
                    started = now;
                    phase = Phase.PICKUP;
                } else {
                    at(p, target, expected);
                    require(now - started < miningTicks + 80, "原生采掘没有完成");
                    if (!stopSent && now - started >= miningTicks) {
                        require(p.getMainHandItem().isEmpty(), "采掘途中主手改变");
                        dig(p, Action.STOP_DESTROY_BLOCK);
                        stopSent = true;
                    }
                }
            }
            case PICKUP -> {
                if (gathered(p) > inventoryBefore) {
                    if (expected == log) require(count(p, log.asItem()) == ++logs, "原木未逐段拾齐");
                    else haveLeaf = count(p, leaf.asItem()) == 1;
                    info("原位采掘并拾取 " + target.toShortString() + "；桑叶=" + count(p, leaf.asItem())
                            + " 桑葚=" + count(p, berry) + " 桑苗=" + count(p, sapling) + " 樟木=" + logs);
                    phase = logs == 3 ? Phase.CRAFT : Phase.SELECT;
                } else {
                    require(now - started < 100, "未观察到本格实际掉落被玩家拾取");
                    if (now - started >= 20 && !approached) approachDrop(p);
                }
            }
            case CRAFT -> {
                stand(p, 4298.5, 4414.5);
                require(p.containerMenu == p.inventoryMenu && count(p, leaf.asItem()) == 1, "背包制板前提不符");
                for (int k = 0; k < 3; k++) craft(p, log.asItem(), new int[]{1}, board, 4);
                require(count(p, board) == 12 && count(p, log.asItem()) == 0, "三次单格制板未得12板");
                craft(p, board, new int[]{1, 2, 3, 4}, Items.CRAFTING_TABLE, 1);
                place(p, TABLE, Blocks.CRAFTING_TABLE);
                select(p, Items.AIR);
                use(p, TABLE);
                require(p.containerMenu instanceof CraftingMenu, "未打开原位工作台菜单");
                info("背包三次制板及工作台合成、放置通过");
                phase = Phase.BOX;
            }
            case BOX -> {
                require(p.containerMenu instanceof CraftingMenu && p.containerMenu.stillValid(p), "工作台已关闭或越距");
                distribute(p, board, new int[]{1, 3, 4, 5, 6});
                craft(p, leaf.asItem(), new int[]{2}, box.asItem(), 1);
                p.closeContainer();
                place(p, BOX, box);
                select(p, Items.AIR);
                at(p, TABLE, Blocks.CRAFTING_TABLE);
                require(p.level().getBlockEntity(BOX) instanceof SilkwormBoxBlockEntity, "缺少原位蚕盒方块实体");
                require(count(p, board) == 3 && count(p, leaf.asItem()) == 0 && count(p, log.asItem()) == 0
                        && count(p, Items.CRAFTING_TABLE) == 0 && count(p, box.asItem()) == 0, "最终材料余量不符");
                info("首次进场完成：工作台 " + TABLE.toShortString() + "，蚕盒及BE " + BOX.toShortString()
                        + "，余3板；保存及重启不在此检查中");
                finish();
            }
        }
    }

    private void dig(ServerPlayer p, Action action) {
        p.gameMode.handleBlockBreakAction(target, action, expected == leaf ? Direction.DOWN : Direction.WEST,
                p.level().getMaxBuildHeight(), ++sequence);
    }

    private void stand(ServerPlayer p, double x, double z) {
        p.connection.teleport(x, 66, z, p.getYRot(), p.getXRot());
        info("受控服务端站位 " + x + ",66," + z);
    }

    private int gathered(ServerPlayer p) {
        return expected == log ? count(p, log.asItem()) : count(p, leaf.asItem()) + count(p, berry) + count(p, sapling);
    }

    private List<ItemEntity> nearbyDrops(ServerPlayer p) {
        return p.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(3, 6, 3), entity -> {
            ItemStack s = entity.getItem();
            return expected == log ? s.is(log.asItem()) : s.is(leaf.asItem()) || s.is(berry) || s.is(sapling);
        });
    }

    /** 弹离采掘站位的掉落物仅作为站位目标，拾取仍须发生玩家与实体碰撞。 */
    private void approachDrop(ServerPlayer p) {
        List<ItemEntity> fresh = nearbyDrops(p).stream().filter(e -> !existingDrops.contains(e.getId())).toList();
        require(fresh.size() <= 1, "本格附近有多个新掉落实体，无法确定来源");
        if (fresh.isEmpty()) return;
        ItemEntity e = fresh.get(0);
        if (!e.onGround()) return;
        Vec3 dest = e.position();
        if (!p.level().noCollision(p, p.getBoundingBox().move(dest.subtract(p.position())))) return;
        p.connection.teleport(dest.x, dest.y, dest.z, p.getYRot(), p.getXRot());
        approached = true;
        info("受控服务端拾取站位 " + dest + "，本次新掉落实体 " + e.getId());
    }

    private static int mined(ServerPlayer p, Block b) {
        return p.getStats().getValue(Stats.BLOCK_MINED.get(b));
    }

    private static int count(ServerPlayer p, Item item) {
        return p.getInventory().countItem(item);
    }

    private static void select(ServerPlayer p, Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (item == Items.AIR ? stack.isEmpty() : stack.is(item)) {
                p.getInventory().selected = i;
                return;
            }
        }
        throw new IllegalStateException("快捷栏无所需空手或物品：" + item);
    }

    private static void use(ServerPlayer p, BlockPos clicked) {
        require(p.canInteractWithBlock(clicked, 0), "右键目标越距");
        BlockHitResult hit = new BlockHitResult(Vec3.atBottomCenterOf(clicked).add(0, 1, 0), Direction.UP, clicked, false);
        require(p.gameMode.useItemOn(p, p.serverLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND, hit)
                .consumesAction(), "原生方块交互未接受：" + clicked);
    }

    private static void place(ServerPlayer p, BlockPos pos, Block b) {
        at(p, pos, Blocks.SHORT_GRASS);
        select(p, b.asItem());
        use(p, pos.below());
        at(p, pos, b);
    }

    /** 从真实库存槽拾起一叠，逐格右键投入一件；剩料放回原槽。 */
    private static void distribute(ServerPlayer p, Item item, int[] slots) {
        AbstractContainerMenu menu = p.containerMenu;
        require(menu.getCarried().isEmpty(), "投料前光标不空");
        Slot from = menu.slots.stream().filter(s -> s.container == p.getInventory() && s.getItem().is(item)
                && s.getItem().getCount() >= slots.length).findFirst().orElseThrow();
        menu.clicked(from.index, 0, ClickType.PICKUP, p);
        for (int slot : slots) {
            require(!menu.getSlot(slot).hasItem(), "配方格已占用：" + slot);
            menu.clicked(slot, 1, ClickType.PICKUP, p);
        }
        if (!menu.getCarried().isEmpty()) menu.clicked(from.index, 0, ClickType.PICKUP, p);
        require(menu.getCarried().isEmpty(), "投料未完成");
    }

    private static void craft(ServerPlayer p, Item input, int[] slots, Item result, int amount) {
        distribute(p, input, slots);
        AbstractContainerMenu menu = p.containerMenu;
        ItemStack output = menu.getSlot(0).getItem();
        require(output.is(result) && output.getCount() == amount, "原生配方实际产出 " + output + "，预期 " + result);
        Slot dest = menu.slots.stream().filter(s -> s.container == p.getInventory() && s.getContainerSlot() < 9
                && s.getItem().is(result)).findFirst().orElseGet(() -> menu.slots.stream().filter(s ->
                        s.container == p.getInventory() && s.getContainerSlot() < 9 && !s.hasItem()).findFirst().orElseThrow());
        menu.clicked(0, 0, ClickType.PICKUP, p);
        menu.clicked(dest.index, 0, ClickType.PICKUP, p);
        require(menu.getCarried().isEmpty() && !menu.getSlot(0).hasItem(), "取出产物后光标或结果格未清空");
        menu.broadcastChanges();
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path)).orElseThrow();
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path)).orElseThrow();
    }

    private static void at(ServerPlayer p, BlockPos pos, Block b) {
        require(p.level().getBlockState(pos).is(b), pos.toShortString() + " 不是 " + b);
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }

    private void info(String message) {
        report.add("INFO 自然桑叶制盒 " + message);
    }
}
