package net.yvanchuxiuzhen.smoke;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
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

/** 在独立存档副本中采收现存文玉木，合成并放置白砖后同档重进。 */
final class JadeWoodSupplyCheck extends SystemCheck {
    static final String SELECTOR = "jadewoodsupply";
    static final String SAVE = "Renjian-Jadewood-268-275";
    private static final String LEVEL_SHA = "9e035d96af6f3bb00f802892b540efd378ff1f05955ae3f642d8c8774411bf40";
    private static final String REGION_SHA = "a06070ef170c240d6305bbb8dafef84472927d099f665851e0da091fa60f7e47";
    private static final BlockPos BASE = new BlockPos(4240, 64, 4368);
    private static final BlockPos PLACED = new BlockPos(4241, 64, 4370);
    private final Block log = block("jadeloglog"), leaves = block("jadelogleaves"), brick = block("brickwallwhite");
    private final List<String> report;
    private int minedBefore, pickedBefore, pickupStatBefore, sequence, harvested;
    private long start, duration;
    private boolean stopSent, approached;
    private Set<Integer> oldDrops;
    private BlockPos target;
    private Stage stage = Stage.START;
    private enum Stage { START, BEGIN, DIG, PICKUP, CRAFT, DONE }

    JadeWoodSupplyCheck(List<String> report, String levelId) {
        super("文玉木供给", report, levelId);
        this.report = report;
    }

    static boolean ready(Path dir) {
        Path saves = dir.resolve("saves"), copy = saves.resolve(SAVE);
        try {
            return dir.toAbsolutePath().normalize().endsWith(Path.of("run", "smoke"))
                    && copy.toRealPath().equals(saves.toRealPath().resolve(SAVE))
                    && !Files.exists(copy.resolve("session.lock"))
                    && fingerprint(copy.resolve("level.dat"), LEVEL_SHA)
                    && fingerprint(copy.resolve("dimensions/yvanchuxiuzhen/dimensionrenjian/region/r.8.8.mca"), REGION_SHA);
        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean fingerprint(Path path, String expected) throws IOException, NoSuchAlgorithmException {
        return !Files.isSymbolicLink(path) && HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path))).equals(expected);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) return inWorld(local);
        if (step == 2) return reopened(local);
        if (step == 3 && !inWorld(local)) return false;
        if (step == 1 || step == 3) {
            if (local > 2400 || minecraft.player == null || minecraft.getSingleplayerServer() == null) {
                fail("世界退出或阶段超时 " + stage);
                finish();
                return false;
            }
            boolean[] ok = {true};
            onServer(p -> {
                try {
                    if (step == 1) advance(p);
                    else reentry(p);
                } catch (RuntimeException e) {
                    fail(stage + "：" + e);
                    ok[0] = false;
                }
            });
            if (!ok[0] || step == 3) { finish(); return false; }
            if (stage == Stage.DONE) { leaveWorld(); return true; }
            return false;
        }
        throw new IllegalStateException("无效步骤 " + step);
    }

    private void advance(ServerPlayer p) {
        need(p.isAlive() && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                && p.level().dimension().location().equals(id("dimensionrenjian")), "玩家不在人间生存模式");
        long now = p.level().getGameTime();
        switch (stage) {
            case START -> {
                need(p.server.getWorldData().worldGenOptions().seed() == -5586163949850800998L
                        && p.containerMenu == p.inventoryMenu && p.inventoryMenu.getCarried().isEmpty(), "存档或菜单不符");
                need(count(p, log.asItem()) == 0 && count(p, Items.CLAY) == 0 && count(p, brick.asItem()) == 0, "背包已有本链物品");
                at(p, BASE, log); at(p, BASE.above(), log); at(p, new BlockPos(4241, 69, 4368), leaves);
                need(p.level().getBlockState(PLACED).isAir() && p.level().getBlockState(PLACED.below()).is(Blocks.STONE), "放砖位置不符");
                need(p.getInventory().add(new ItemStack(Items.CLAY, 2)), "无法放入夹具黏土");
                stand(p, 4241.5, 64, 4369.5);
                info("存档现存树体：两段文玉木及树冠已核对；两块黏土由夹具提供，不宣称树体生成已被追踪。");
                stage = Stage.BEGIN;
            }
            case BEGIN -> {
                target = BASE.above(harvested);
                at(p, target, log);
                select(p, Items.AIR);
                need(p.canInteractWithBlock(target, 0), "树干越距 " + target);
                minedBefore = p.getStats().getValue(Stats.BLOCK_MINED.get(log));
                pickedBefore = count(p, log.asItem());
                oldDrops = drops(p).stream().map(ItemEntity::getId).collect(Collectors.toSet());
                need(oldDrops.isEmpty(), "采收前附近已有文玉木掉落实体");
                pickupStatBefore = p.getStats().getValue(Stats.ITEM_PICKED_UP.get(log.asItem()));
                float progress = p.level().getBlockState(target).getDestroyProgress(p, p.level(), target);
                need(progress > 0, "无法徒手采掘");
                duration = (long) Math.ceil(1.0 / progress);
                start = now; stopSent = false; approached = false;
                dig(p, Action.START_DESTROY_BLOCK);
                stage = Stage.DIG;
            }
            case DIG -> {
                if (p.level().getBlockState(target).isAir()) {
                    need(p.getStats().getValue(Stats.BLOCK_MINED.get(log)) == minedBefore + 1, "未记录玩家采掘");
                    start = now;
                    stage = Stage.PICKUP;
                } else {
                    at(p, target, log);
                    need(now - start < duration + 80, "采掘超时");
                    if (!stopSent && now - start >= duration) { dig(p, Action.STOP_DESTROY_BLOCK); stopSent = true; }
                }
            }
            case PICKUP -> {
                if (count(p, log.asItem()) > pickedBefore) {
                    need(count(p, log.asItem()) == pickedBefore + 1
                            && p.getStats().getValue(Stats.ITEM_PICKED_UP.get(log.asItem())) == pickupStatBefore + 1, "拾取数量或统计不符");
                    info("原位采掘并碰撞拾取文玉木 " + target.toShortString());
                    harvested++;
                    stage = harvested == 2 ? Stage.CRAFT : Stage.BEGIN;
                } else {
                    need(now - start < 100, "未拾取本次树干掉落");
                    if (now - start >= 20 && !approached) approachDrop(p);
                }
            }
            case CRAFT -> {
                need(p.containerMenu == p.inventoryMenu && count(p, log.asItem()) == 2 && count(p, Items.CLAY) == 2, "合成材料不符");
                AbstractContainerMenu menu = p.inventoryMenu;
                distribute(p, Items.CLAY, new int[]{1, 4});
                distribute(p, log.asItem(), new int[]{2, 3});
                need(menu.getSlot(0).getItem().is(brick.asItem()) && menu.getSlot(0).getItem().getCount() == 8, "白砖配方结果不符");
                Slot dest = menu.slots.stream().filter(s -> s.container == p.getInventory() && s.getContainerSlot() < 9 && !s.hasItem()).findFirst().orElseThrow();
                menu.clicked(0, 0, ClickType.PICKUP, p);
                menu.clicked(dest.index, 0, ClickType.PICKUP, p);
                need(menu.getCarried().isEmpty() && !menu.getSlot(0).hasItem() && count(p, brick.asItem()) == 8
                        && count(p, log.asItem()) == 0 && count(p, Items.CLAY) == 0, "白砖取出或扣料不符");
                stand(p, 4242.5, 64, 4369.5);
                select(p, brick.asItem());
                BlockPos ground = PLACED.below();
                need(p.canInteractWithBlock(ground, 0), "放置越距");
                need(p.gameMode.useItemOn(p, p.serverLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atBottomCenterOf(ground).add(0, 1, 0), Direction.UP, ground, false)).consumesAction(), "放砖失败");
                at(p, PLACED, brick);
                need(count(p, brick.asItem()) == 7, "放砖未扣一块");
                info("背包2×2配方消费两段实采文玉木和两块夹具黏土，制8砖，原位放置1砖、余7砖。");
                stage = Stage.DONE;
            }
            case DONE -> throw new IllegalStateException("场景已经结束");
        }
    }

    private void reentry(ServerPlayer p) {
        need(p.level().dimension().location().equals(id("dimensionrenjian")), "重进维度不符");
        need(p.level().getBlockState(BASE).isAir() && p.level().getBlockState(BASE.above()).isAir(), "采收缺口未保留");
        at(p, PLACED, brick);
        need(count(p, brick.asItem()) == 7 && count(p, log.asItem()) == 0 && count(p, Items.CLAY) == 0, "重进库存不符");
        info("同档重进：原采收缺口、已放置白砖和背包7砖保留。");
    }

    private void dig(ServerPlayer p, Action action) {
        p.gameMode.handleBlockBreakAction(target, action, Direction.EAST, p.level().getMaxBuildHeight(), ++sequence);
    }
    private List<ItemEntity> drops(ServerPlayer p) {
        return p.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(3), e -> e.getItem().is(log.asItem()));
    }
    private void approachDrop(ServerPlayer p) {
        List<ItemEntity> fresh = drops(p).stream().filter(e -> !oldDrops.contains(e.getId())).toList();
        need(fresh.size() <= 1, "新掉落实体不唯一");
        if (fresh.isEmpty() || !fresh.getFirst().onGround()) return;
        Vec3 dest = fresh.getFirst().position();
        if (!p.level().noCollision(p, p.getBoundingBox().move(dest.subtract(p.position())))) return;
        stand(p, dest.x, dest.y, dest.z);
        approached = true;
    }
    private static void distribute(ServerPlayer p, Item item, int[] slots) {
        AbstractContainerMenu menu = p.inventoryMenu;
        Slot from = menu.slots.stream().filter(s -> s.container == p.getInventory() && s.getItem().is(item)
                && s.getItem().getCount() >= slots.length).findFirst().orElseThrow();
        menu.clicked(from.index, 0, ClickType.PICKUP, p);
        for (int slot : slots) menu.clicked(slot, 1, ClickType.PICKUP, p);
        if (!menu.getCarried().isEmpty()) menu.clicked(from.index, 0, ClickType.PICKUP, p);
        need(menu.getCarried().isEmpty(), "投料后光标非空");
    }
    private static void select(ServerPlayer p, Item item) {
        for (int i = 0; i < 9; i++) if (item == Items.AIR ? p.getInventory().getItem(i).isEmpty() : p.getInventory().getItem(i).is(item)) {
            p.getInventory().selected = i; return;
        }
        throw new IllegalStateException("快捷栏无所需物品 " + item);
    }
    private static void stand(ServerPlayer p, double x, double y, double z) {
        Vec3 dest = new Vec3(x, y, z);
        need(p.level().noCollision(p, p.getBoundingBox().move(dest.subtract(p.position()))), "站位碰撞 " + dest);
        p.connection.teleport(x, y, z, p.getYRot(), p.getXRot());
    }
    private static int count(ServerPlayer p, Item item) { return p.getInventory().countItem(item); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
    private static Block block(String path) { return BuiltInRegistries.BLOCK.getOptional(id(path)).orElseThrow(); }
    private static void at(ServerPlayer p, BlockPos pos, Block block) { need(p.level().getBlockState(pos).is(block), "方块不符 " + pos); }
    private static void need(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    private void info(String message) { report.add("INFO 文玉木供给 " + message); }
}
