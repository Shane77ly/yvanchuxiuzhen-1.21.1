package net.yvanchuxiuzhen.smoke;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 在已记录的两个相邻矿位验证正常采收、熔炼、凝锭和工作台消费。 */
final class NaturalMetalChainCheck extends SystemCheck {
    static final String SELECTOR = "naturalmetalchain";
    static final String SAVE = "Renjian-Metal-268-275";
    private static final BlockPos FIRST = new BlockPos(4298, -30, 4409);
    private static final BlockPos HOME = new BlockPos(4298, 66, 4414);
    private static final BlockPos FURNACE = HOME.north(), TABLE = FURNACE.west();
    private final Block ore = BuiltInRegistries.BLOCK.getOptional(id("cailiao_20101")).orElseThrow();
    private final Item raw = item("cailiao_201raw"), melt = item("cailiao_201melt");
    private final Item ingot = item("cailiao_201"), arrow = item("skill_arrow_04");
    private final List<String> report;
    private enum Stage { START, BEGIN_DIG, DIG, PICKUP, FIRE, SMELT, CAST, CRAFT, SYNC }
    private Stage stage = Stage.START;
    private long began, duration;
    private int harvested, sequence;
    private boolean stopped;

    NaturalMetalChainCheck(List<String> report, String levelId) {
        super("天然精铁制箭", report, levelId);
        this.report = report;
    }

    static boolean ready(Path dir) {
        Path saves = dir.resolve("saves"), copy = saves.resolve(SAVE), data = copy.resolve("level.dat");
        try {
            return dir.toAbsolutePath().normalize().endsWith(Path.of("run", "smoke"))
                    && copy.toRealPath().equals(saves.toRealPath().resolve(SAVE))
                    && !Files.exists(copy.resolve("session.lock")) && !Files.isSymbolicLink(data)
                    && fingerprint(data, "c59c4a5cf3fe440341f8508f8c9290cb5358765d06720a997412ddaeab6fbd98")
                    && fingerprint(copy.resolve("dimensions/yvanchuxiuzhen/dimensionrenjian/region/r.8.8.mca"),
                            "55f77d5aaa587be87ab8830d396334eee5f9673da1ce0ee9896d66b007bf1882");
        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean fingerprint(Path path, String expected) throws IOException, NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))).equals(expected);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) return inWorld(local);
        try {
            need(local < 1800 && minecraft.player != null && minecraft.getSingleplayerServer() != null,
                    "世界退出或超过1800客户端 tick");
            if (stage == Stage.SYNC) {
                var inv = minecraft.player.getInventory();
                if (inv.countItem(arrow) == 6 && inv.countItem(raw) == 0 && inv.countItem(melt) == 0
                        && inv.countItem(ingot) == 0 && inv.countItem(Items.STICK) == 0
                        && minecraft.player.containerMenu == minecraft.player.inventoryMenu) {
                    info("客户端背包已同步六支成箭，粗矿、熔液、精铁锭、木棍均为零");
                    finish();
                }
            } else onServer(this::advance);
        } catch (RuntimeException e) {
            fail(stage + "：" + (e.getCause() == null ? e : e.getCause()));
            finish();
        }
        return false;
    }

    private void advance(ServerPlayer p) {
        need(p.isAlive() && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                && p.level().dimension().location().equals(id("dimensionrenjian")), "不是人间道生存玩家");
        long now = p.level().getGameTime();
        BlockPos target = harvested == 0 ? FIRST : FIRST.west();
        switch (stage) {
            case START -> {
                need(p.server.getWorldData().worldGenOptions().seed() == -5586163949850800998L
                        && p.position().distanceToSqr(Vec3.atBottomCenterOf(HOME)) < 1
                        && p.containerMenu == p.inventoryMenu && p.getInventory().isEmpty()
                        && p.inventoryMenu.getCarried().isEmpty(), "非原存档准备态");
                for (BlockPos pos : List.of(FIRST, FIRST.west())) at(p, pos, ore);
                for (BlockPos pos : List.of(TABLE, FURNACE)) {
                    at(p, pos, Blocks.SHORT_GRASS);
                    at(p, pos.below(), Blocks.GRASS_BLOCK);
                }
                need(mined(p) == 0 && picked(p) == 0 && crafted(p) == 0, "已有本链统计");
                for (Item fixture : List.of(Items.STONE_PICKAXE, Items.FURNACE, Items.COAL, Items.CRAFTING_TABLE, Items.STICK))
                    need(p.getInventory().add(new ItemStack(fixture)), "未能配发夹具 " + fixture);
                info("夹具各一：无附魔石镐、普通熔炉、煤、工作台、木棍；空背包进入，无链内材料");
                stand(p, FIRST.south());
                stage = Stage.BEGIN_DIG;
            }
            case BEGIN_DIG -> {
                at(p, target, ore);
                select(p, Items.STONE_PICKAXE);
                var state = p.level().getBlockState(target);
                need(p.canInteractWithBlock(target, 0) && p.hasCorrectToolForDrops(state), "石镐阶位或矿位距离不符");
                float progress = state.getDestroyProgress(p, p.level(), target);
                need(progress > 0, "不可开采");
                duration = (long) Math.ceil(1 / progress);
                began = now;
                stopped = false;
                dig(p, target, Action.START_DESTROY_BLOCK);
                stage = Stage.DIG;
            }
            case DIG -> {
                if (p.level().getBlockState(target).isAir()) {
                    need(mined(p) == harvested + 1, "缺本格采掘统计");
                    stand(p, target);
                    began = now;
                    stage = Stage.PICKUP;
                } else {
                    at(p, target, ore);
                    need(now - began < duration + 80 && p.getMainHandItem().is(Items.STONE_PICKAXE), "开采超时或工具改变");
                    if (!stopped && now - began >= duration) {
                        dig(p, target, Action.STOP_DESTROY_BLOCK);
                        stopped = true;
                    }
                }
            }
            case PICKUP -> {
                need(now - began < 100, "本格粗矿没有碰撞入包");
                if (count(p, raw) > harvested) {
                    harvested++;
                    need(count(p, raw) == harvested && picked(p) == harvested, "粗矿数量与拾取统计不符");
                    info("原位 " + target.toShortString() + "，累计开采/拾取/粗矿 " + mined(p) + "/" + picked(p) + "/" + count(p, raw));
                    stage = harvested == 2 ? Stage.FIRE : Stage.BEGIN_DIG;
                }
            }
            case FIRE -> {
                stand(p, HOME);
                place(p, FURNACE, Blocks.FURNACE);
                place(p, TABLE, Blocks.CRAFTING_TABLE);
                select(p, Items.AIR);
                use(p, FURNACE);
                need(p.containerMenu instanceof FurnaceMenu, "未打开普通熔炉");
                var menu = p.containerMenu;
                for (int slot = 0; slot <= 2; slot++) need(!menu.getSlot(slot).hasItem(), "熔炉并非空炉");
                putOne(p, raw, 0);
                putOne(p, raw, 0);
                putOne(p, Items.COAL, 1);
                need(menu.getSlot(0).getItem().is(raw) && menu.getSlot(0).getItem().getCount() == 2
                        && menu.getSlot(1).getItem().is(Items.COAL) && count(p, raw) == 0, "炉口投料不符");
                began = now;
                stage = Stage.SMELT;
            }
            case SMELT -> {
                var menu = p.containerMenu;
                need(menu instanceof FurnaceMenu && menu.stillValid(p) && now - began < 600, "熔炉菜单失效或熔炼超时");
                ItemStack output = menu.getSlot(2).getItem();
                if (!output.isEmpty()) need(output.is(melt) && output.getCount() <= 2, "实际炉产物 " + output);
                if (output.getCount() == 2) {
                    need(!menu.getSlot(0).hasItem() && !menu.getSlot(1).hasItem(), "原料或燃料未扣清");
                    take(p, 2, melt, 2);
                    p.closeContainer();
                    info("普通熔炉原生用时 " + (now - began) + " tick，两份粗矿烧成两份熔液并取出");
                    stage = Stage.CAST;
                }
            }
            case CAST -> {
                select(p, melt);
                need(p.getMainHandItem().getCount() == 2 && count(p, ingot) == 0, "凝锭前物料不符");
                p.gameMode.useItem(p, p.level(), p.getMainHandItem(), InteractionHand.MAIN_HAND);
                need(count(p, melt) == 0 && count(p, ingot) == 2, "一次右键未净耗两份熔液并产两锭");
                for (ItemStack s : p.getInventory().items) if (s.is(ingot)) {
                    var data = Materials.instance(s);
                    need(data.level() == 3 && data.quality() >= 0 && data.quality() <= 4, "不符的精铁锭属性 " + data);
                    info("凝锭：等级 " + data.level() + " 品阶 " + data.quality() + " × " + s.getCount());
                }
                stage = Stage.CRAFT;
            }
            case CRAFT -> {
                select(p, Items.AIR);
                use(p, TABLE);
                var menu = p.containerMenu;
                need(menu instanceof CraftingMenu && menu.stillValid(p), "未打开原位工作台");
                putOne(p, ingot, 1);
                putOne(p, ingot, 4);
                putOne(p, Items.STICK, 7);
                take(p, 0, arrow, 6);
                for (int i = 0; i <= 9; i++) need(!menu.getSlot(i).hasItem(), "工作台残料 " + i);
                p.closeContainer();
                need(crafted(p) == 6 && count(p, arrow) == 6 && count(p, ingot) == 0 && count(p, Items.STICK) == 0,
                        "制箭统计、成品或余料不符");
                info("工作台正常取出 skill_arrow_04 × 6；两锭一棍全部消费，制作统计=6");
                stage = Stage.SYNC;
            }
            default -> throw new IllegalStateException(stage.name());
        }
    }

    private void stand(ServerPlayer p, BlockPos pos) {
        Vec3 feet = Vec3.atBottomCenterOf(pos);
        need(p.level().getBlockState(pos.below()).isFaceSturdy(p.level(), pos.below(), Direction.UP)
                && p.level().noCollision(p, p.getBoundingBox().move(feet.subtract(p.position()))), "站位受阻 " + pos);
        p.connection.teleport(feet.x, feet.y, feet.z, p.getYRot(), p.getXRot());
        info("受控服务端站位 " + feet);
    }

    private void dig(ServerPlayer p, BlockPos pos, Action action) {
        p.gameMode.handleBlockBreakAction(pos, action, harvested == 0 ? Direction.SOUTH : Direction.EAST,
                p.level().getMaxBuildHeight(), ++sequence);
    }

    private static void place(ServerPlayer p, BlockPos pos, Block b) {
        at(p, pos, Blocks.SHORT_GRASS);
        select(p, b.asItem());
        use(p, pos.below());
        at(p, pos, b);
    }

    private static void use(ServerPlayer p, BlockPos pos) {
        need(p.canInteractWithBlock(pos, 0), "越过交互距离 " + pos);
        var hit = new BlockHitResult(Vec3.atBottomCenterOf(pos).add(0, 1, 0), Direction.UP, pos, false);
        need(p.gameMode.useItemOn(p, p.serverLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND, hit).consumesAction(),
                "原生方块右键未接受 " + pos);
    }

    private static void select(ServerPlayer p, Item item) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack s = p.getInventory().getItem(slot);
            if (item == Items.AIR ? s.isEmpty() : s.is(item)) {
                p.getInventory().selected = slot;
                return;
            }
        }
        throw new IllegalStateException("快捷栏无 " + item);
    }

    private static void putOne(ServerPlayer p, Item item, int target) {
        AbstractContainerMenu menu = p.containerMenu;
        Slot from = menu.slots.stream().filter(s -> s.container == p.getInventory() && s.getItem().is(item)).findFirst().orElseThrow();
        int before = menu.getSlot(target).getItem().getCount();
        need(menu.getCarried().isEmpty(), "投料前光标不空");
        menu.clicked(from.index, 0, ClickType.PICKUP, p);
        menu.clicked(target, 1, ClickType.PICKUP, p);
        if (!menu.getCarried().isEmpty()) menu.clicked(from.index, 0, ClickType.PICKUP, p);
        need(menu.getCarried().isEmpty() && menu.getSlot(target).getItem().is(item)
                && menu.getSlot(target).getItem().getCount() == before + 1, "未实际投入 " + item);
    }

    private static void take(ServerPlayer p, int output, Item item, int n) {
        AbstractContainerMenu menu = p.containerMenu;
        var result = menu.getSlot(output).getItem();
        need(menu.getCarried().isEmpty() && result.is(item) && result.getCount() == n, "原生结果槽不符：" + result);
        Slot dest = menu.slots.stream().filter(s -> s.container == p.getInventory() && s.getContainerSlot() < 9
                && !s.hasItem()).findFirst().orElseThrow();
        menu.clicked(output, 0, ClickType.PICKUP, p);
        menu.clicked(dest.index, 0, ClickType.PICKUP, p);
        need(!menu.getSlot(output).hasItem() && menu.getCarried().isEmpty() && count(p, item) == n, "未净取出 " + item);
        menu.broadcastChanges();
    }

    private int mined(ServerPlayer p) { return p.getStats().getValue(Stats.BLOCK_MINED.get(ore)); }
    private int picked(ServerPlayer p) { return p.getStats().getValue(Stats.ITEM_PICKED_UP.get(raw)); }
    private int crafted(ServerPlayer p) { return p.getStats().getValue(Stats.ITEM_CRAFTED.get(arrow)); }
    private static int count(ServerPlayer p, Item item) { return p.getInventory().countItem(item); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
    private static Item item(String path) { return BuiltInRegistries.ITEM.getOptional(id(path)).orElseThrow(); }
    private static void at(ServerPlayer p, BlockPos pos, Block b) { need(p.level().getBlockState(pos).is(b), "原位方块不符 " + pos); }
    private static void need(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    private void info(String message) { report.add("INFO 天然精铁制箭 " + message); }
}
