package net.yvanchuxiuzhen.smoke;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
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

/** 固定自然矿位采集、凝锭、合金合成与铁砧消费，操作在一次性副本中执行。 */
final class NaturalAlloyRepairCheck extends SystemCheck {
    static final String SELECTOR = "naturalalloyrepair";
    static final String SAVE = "Renjian-Alloy-268-275";
    private static final List<BlockPos> ORES = List.of(new BlockPos(4297, -3, 4407),
            new BlockPos(4300, 8, 4400), new BlockPos(4288, 20, 4412), new BlockPos(4292, 48, 4405));
    private static final BlockPos HOME = new BlockPos(4298, 66, 4414);
    private static final BlockPos PLATFORM = new BlockPos(4298, 110, 4414);
    private static final BlockPos FURNACE = PLATFORM.north(), TABLE = PLATFORM.west(), ANVIL = PLATFORM.east();
    private final Block ore = BuiltInRegistries.BLOCK.getOptional(id("cailiao_20501")).orElseThrow();
    private final Item raw = item("cailiao_205raw"), melt = item("cailiao_205melt"), ingot = item("cailiao_205");
    private final Item alloyMelt = item("cailiao_206melt"), alloy = item("cailiao_206"), axe = item("gongjv_016");
    private final List<String> report;
    private final Screenshots screenshots;
    private enum Stage { START, BEGIN_DIG, DIG, PICKUP, FIRE, SMELT, CAST, ALLOY, CAST_ALLOY, REPAIR, SYNC }
    private Stage stage = Stage.START;
    private long began, duration;
    private int harvested, sequence;
    private boolean stopped;
    private ItemStack repaired;

    NaturalAlloyRepairCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("天然寒潭精铁至黑曜合金修理", report, levelId);
        this.report = report;
        this.screenshots = screenshots;
    }

    static boolean ready(Path dir) {
        Path saves = dir.resolve("saves"), copy = saves.resolve(SAVE), data = copy.resolve("level.dat");
        try {
            return dir.toAbsolutePath().normalize().getParent().endsWith(Path.of("run", "natural-alloy"))
                    && copy.toRealPath().equals(saves.toRealPath().resolve(SAVE))
                    && !Files.exists(copy.resolve("session.lock")) && !Files.isSymbolicLink(data)
                    && fingerprint(data, "c59c4a5cf3fe440341f8508f8c9290cb5358765d06720a997412ddaeab6fbd98")
                    && fingerprint(copy.resolve("dimensions/yvanchuxiuzhen/dimensionrenjian/region/r.8.8.mca"),
                            "55f77d5aaa587be87ab8830d396334eee5f9673da1ce0ee9896d66b007bf1882")
                    && fingerprint(copy.resolve("playerdata/380df991-f603-344c-a090-369bad2a924a.dat"),
                            "b555ebcb27d44d8c6c5700f501f22d3e6314d2e8518f75dc3259027f7f2e9c23");
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
        try {
            if (step == 0) return inWorld(local);
            if (step == 2) {
                if (minecraft.screen instanceof TitleScreen && local >= 20) return reopened(local);
                if (local > 900) throw new IllegalStateException("退出后未到标题界面");
                return false;
            }
            if (step == 3) return inWorld(local);
            if (step == 4) {
                onServer(p -> need(p.getInventory().items.stream().anyMatch(s -> ItemStack.matches(repaired, s)),
                        "重进后修复斧头的数量或组件不符"));
                info("真实重进后修复斧头及组件保留");
                screenshots.take("natural-alloy-after-reentry.png");
                finish();
                return true;
            }
            need(local < 2400 && minecraft.player != null && minecraft.getSingleplayerServer() != null,
                    "世界退出或超过2400客户端 tick");
            if (stage == Stage.SYNC) {
                if (minecraft.player.getInventory().items.stream().anyMatch(s -> ItemStack.matches(repaired, s))
                        && minecraft.player.containerMenu == minecraft.player.inventoryMenu) {
                    info("客户端背包同步修复斧头及组件");
                    screenshots.take("natural-alloy-repaired.png");
                    leaveWorld();
                    return true;
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
        BlockPos target = ORES.get(Math.min(harvested, ORES.size() - 1));
        switch (stage) {
            case START -> {
                need(p.server.getWorldData().worldGenOptions().seed() == -5586163949850800998L
                        && p.position().distanceToSqr(Vec3.atBottomCenterOf(HOME)) < 1
                        && p.containerMenu == p.inventoryMenu && p.getInventory().isEmpty()
                        && p.inventoryMenu.getCarried().isEmpty(), "非原存档准备态");
                for (BlockPos pos : ORES) at(p, pos, ore);
                need(mined(p) == 0 && picked(p) == 0, "已有寒潭矿采掘或拾取统计");
                for (Item fixture : List.of(Items.DIAMOND_PICKAXE, Items.BLAST_FURNACE, Items.COAL,
                        Items.CRAFTING_TABLE, Items.OBSIDIAN, Items.ANVIL)) {
                    need(p.getInventory().add(new ItemStack(fixture, fixture == Items.OBSIDIAN ? 4 : 1)), "配发夹具失败 " + fixture);
                }
                ItemStack damaged = new ItemStack(axe);
                damaged.setDamageValue(1000);
                need(p.getInventory().add(damaged), "配发受损斧失败");
                p.giveExperienceLevels(10);
                info("受控配发钻石镐、高炉、煤、工作台、黑曜石4、铁砧、受损黑曜合金斧及10级经验；不配发链内材料");
                stage = Stage.BEGIN_DIG;
            }
            case BEGIN_DIG -> {
                at(p, target, ore);
                BlockPos stand = target.south();
                p.serverLevel().setBlockAndUpdate(stand.below(), Blocks.STONE.defaultBlockState());
                p.serverLevel().setBlockAndUpdate(stand, Blocks.AIR.defaultBlockState());
                p.serverLevel().setBlockAndUpdate(stand.above(), Blocks.AIR.defaultBlockState());
                stand(p, stand);
                select(p, Items.DIAMOND_PICKAXE);
                var state = p.level().getBlockState(target);
                need(p.canInteractWithBlock(target, 0) && state.canHarvestBlock(p.level(), target, p), "矿位不可交互或不接受钻石镐 " + target);
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
                    p.serverLevel().setBlockAndUpdate(target.below(), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(target.above(), Blocks.AIR.defaultBlockState());
                    stand(p, target);
                    began = now;
                    stage = Stage.PICKUP;
                } else {
                    at(p, target, ore);
                    need(now - began < duration + 80 && p.getMainHandItem().is(Items.DIAMOND_PICKAXE), "开采超时或工具改变");
                    if (!stopped && now - began >= duration) {
                        dig(p, target, Action.STOP_DESTROY_BLOCK);
                        stopped = true;
                    }
                }
            }
            case PICKUP -> {
                need(now - began < 120, "本格粗矿没有碰撞入包");
                if (count(p, raw) > harvested) {
                    harvested++;
                    need(count(p, raw) == harvested && picked(p) == harvested, "粗矿数量与拾取统计不符");
                    info("天然矿位 " + target.toShortString() + "，累计采掘/拾取 " + mined(p) + "/" + picked(p));
                    stage = harvested == ORES.size() ? Stage.FIRE : Stage.BEGIN_DIG;
                }
            }
            case FIRE -> {
                for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
                    BlockPos floor = PLATFORM.offset(x, -1, z);
                    p.serverLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(floor.above(2), Blocks.AIR.defaultBlockState());
                }
                stand(p, PLATFORM);
                place(p, FURNACE, Blocks.BLAST_FURNACE);
                place(p, TABLE, Blocks.CRAFTING_TABLE);
                place(p, ANVIL, Blocks.ANVIL);
                select(p, Items.AIR);
                use(p, FURNACE);
                need(p.containerMenu instanceof BlastFurnaceMenu, "未打开高炉");
                for (int i = 0; i < 3; i++) need(!p.containerMenu.getSlot(i).hasItem(), "高炉非空");
                for (int i = 0; i < 4; i++) putOne(p, raw, 0);
                putOne(p, Items.COAL, 1);
                began = now;
                stage = Stage.SMELT;
            }
            case SMELT -> {
                AbstractContainerMenu menu = p.containerMenu;
                need(menu instanceof BlastFurnaceMenu && menu.stillValid(p) && now - began < 700, "高炉菜单失效或熔炼超时");
                ItemStack output = menu.getSlot(2).getItem();
                if (!output.isEmpty()) need(output.is(melt) && output.getCount() <= 4, "高炉产物不符 " + output);
                if (output.getCount() == 4) {
                    need(!menu.getSlot(0).hasItem() && count(p, raw) == 0, "粗矿未耗清");
                    take(p, 2, melt, 4);
                    p.closeContainer();
                    info("高炉用时 " + (now - began) + " tick，四份粗矿产四份熔液");
                    stage = Stage.CAST;
                }
            }
            case CAST -> {
                select(p, melt);
                p.gameMode.useItem(p, p.level(), p.getMainHandItem(), InteractionHand.MAIN_HAND);
                need(count(p, melt) == 0 && count(p, ingot) == 4, "寒潭熔液凝锭数量不符");
                for (ItemStack stack : p.getInventory().items) if (stack.is(ingot)) {
                    var data = Materials.instance(stack);
                    need(data.level() == 23 && data.quality() >= 0 && data.quality() <= 4, "寒潭锭材料实例不符 " + data);
                    info("寒潭锭等级 " + data.level() + " 品阶 " + data.quality() + " 数量 " + stack.getCount());
                }
                stage = Stage.ALLOY;
            }
            case ALLOY -> {
                select(p, Items.AIR);
                use(p, TABLE);
                need(p.containerMenu instanceof CraftingMenu, "未打开工作台");
                for (int slot : new int[]{1, 3, 7, 9}) putOne(p, ingot, slot);
                for (int slot : new int[]{2, 4, 6, 8}) putOne(p, Items.OBSIDIAN, slot);
                take(p, 0, alloyMelt, 1);
                for (int i = 1; i <= 9; i++) need(!p.containerMenu.getSlot(i).hasItem(), "合金工作台残料 " + i);
                p.closeContainer();
                need(count(p, ingot) == 0 && count(p, Items.OBSIDIAN) == 0, "合金原料未耗清");
                info("工作台消耗四寒潭锭和四黑曜石，取出黑曜合金熔液一份");
                stage = Stage.CAST_ALLOY;
            }
            case CAST_ALLOY -> {
                select(p, alloyMelt);
                p.gameMode.useItem(p, p.level(), p.getMainHandItem(), InteractionHand.MAIN_HAND);
                ItemStack result = p.getInventory().items.stream().filter(s -> s.is(alloy)).findFirst().orElseThrow();
                var data = Materials.instance(result);
                need(count(p, alloyMelt) == 0 && count(p, alloy) == 1 && data.level() == 28
                        && data.quality() >= 0 && data.quality() <= 4, "合金凝锭数量或实例不符 " + data);
                info("黑曜合金锭等级 " + data.level() + " 品阶 " + data.quality());
                stage = Stage.REPAIR;
            }
            case REPAIR -> {
                select(p, Items.AIR);
                use(p, ANVIL);
                need(p.containerMenu instanceof AnvilMenu && p.containerMenu.stillValid(p), "未打开铁砧");
                putOne(p, axe, 0);
                putOne(p, alloy, 1);
                ItemStack output = p.containerMenu.getSlot(2).getItem();
                need(output.is(axe) && output.getDamageValue() == 0, "铁砧未产修复斧 " + output);
                take(p, 2, axe, 1);
                need(!p.containerMenu.getSlot(0).hasItem() && !p.containerMenu.getSlot(1).hasItem()
                        && count(p, alloy) == 0, "铁砧未消费斧和合金锭");
                p.closeContainer();
                repaired = p.getInventory().items.stream().filter(s -> s.is(axe)).findFirst().orElseThrow().copy();
                info("原生铁砧消耗一枚来源合金锭，将受损1000的黑曜合金斧修复至0");
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
    }

    private void dig(ServerPlayer p, BlockPos pos, Action action) {
        p.gameMode.handleBlockBreakAction(pos, action, Direction.SOUTH, p.level().getMaxBuildHeight(), ++sequence);
    }

    private static void place(ServerPlayer p, BlockPos pos, Block block) {
        at(p, pos, Blocks.AIR);
        select(p, block.asItem());
        use(p, pos.below());
        at(p, pos, block);
    }

    private static void use(ServerPlayer p, BlockPos pos) {
        need(p.canInteractWithBlock(pos, 0), "越过交互距离 " + pos);
        var hit = new BlockHitResult(Vec3.atBottomCenterOf(pos).add(0, 1, 0), Direction.UP, pos, false);
        need(p.gameMode.useItemOn(p, p.serverLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND, hit).consumesAction(),
                "原生方块右键未接受 " + pos);
    }

    private static void select(ServerPlayer p, Item item) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = p.getInventory().getItem(slot);
            if (item == Items.AIR ? stack.isEmpty() : stack.is(item)) {
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
        ItemStack result = menu.getSlot(output).getItem();
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
    private static int count(ServerPlayer p, Item item) { return p.getInventory().countItem(item); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
    private static Item item(String path) { return BuiltInRegistries.ITEM.getOptional(id(path)).orElseThrow(); }
    private static void at(ServerPlayer p, BlockPos pos, Block b) { need(p.level().getBlockState(pos).is(b), "原位方块不符 " + pos); }
    private static void need(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    private void info(String message) { report.add("INFO 天然合金修理 " + message); }
}
