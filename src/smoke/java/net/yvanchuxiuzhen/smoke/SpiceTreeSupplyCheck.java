package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** 新档受控模板采收、树苗生长、配方消费和同档重进；不代表自然生成。 */
final class SpiceTreeSupplyCheck extends SystemCheck {
    private static final BlockPos ORIGIN = new BlockPos(0, 180, 0);
    private static final BlockPos FIRST = new BlockPos(12, 180, 0);
    private static final BlockPos SECOND = new BlockPos(22, 180, 0);
    private static final BlockPos TABLE = new BlockPos(17, 180, 5);
    private final List<String> report;

    SpiceTreeSupplyCheck(List<String> report, String levelId) {
        super("调味树受控供给", report, levelId);
        this.report = report;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0 && !inWorld(local)) return false;
        if (step == 2) return reopened(local);
        if (step == 3 && !inWorld(local)) return false;
        if (step == 0 || step == 3) {
            boolean[] ok = {true};
            onServer(player -> {
                try {
                    if (step == 0) exercise(player);
                    else verifyReload(player);
                } catch (RuntimeException e) {
                    fail(e.toString());
                    ok[0] = false;
                }
            });
            if (!ok[0] || step == 3) finish();
            else leaveWorld();
            return true;
        }
        if (step == 1) return true;
        throw new IllegalStateException("无效步骤 " + step);
    }

    private void exercise(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        for (int x = -2; x <= 27; x++) for (int z = -3; z <= 8; z++) {
            level.setBlock(new BlockPos(x, 179, z), Blocks.DIRT.defaultBlockState(), 2);
            for (int y = 180; y <= 194; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        }
        player.connection.teleport(17.5, 180, 4.5, 0, 0);
        info("新档主世界人工预置来源自然树模板；采叶走服务端破坏，掉落实体由夹具转入背包，未验证客户端挖掘和碰撞拾取。");
        harvest(player, "shicai00907", "shicai_00903", "shicai_00904", "shicai_009");
        harvest(player, "shicai01004", "shicai_01002", "shicai_01003", "shicai_010");
        grow(player, FIRST, "shicai_00904", "shicai_00902", "shicai_00903", 1);
        grow(player, SECOND, "shicai_01003", "shicai_01001", "shicai_01002", 3);
        craft(player);
        info("准备退出并同档重进；花椒、八角与树苗来自受控采叶，骨粉及其他三种配料由夹具提供。");
    }

    private void harvest(ServerPlayer p, String templateId, String leafId, String saplingId, String fruitId) {
        ServerLevel level = p.serverLevel();
        Block leaf = block(leafId);
        Item sapling = item(saplingId), fruit = item(fruitId);
        StructureTemplate template = level.getStructureManager().getOrCreate(id(templateId));
        Vec3i size = template.getSize();
        require(size.getX() > 0 && size.getY() > 0 && size.getZ() > 0, "模板为空 " + templateId);
        int harvested = 0, placements = 0;
        for (; placements < 12 && (count(p, sapling) == 0 || count(p, fruit) == 0); placements++) {
            p.connection.teleport(17.5, 180, 4.5, 0, 0);
            require(template.placeInWorld(level, ORIGIN, ORIGIN, new StructurePlaceSettings(), level.random, 2), "模板放置失败 " + templateId);
            int leaves = 0;
            for (BlockPos pos : BlockPos.betweenClosed(ORIGIN, ORIGIN.offset(size.getX()-1, size.getY()-1, size.getZ()-1))) {
                if (!level.getBlockState(pos).is(leaf)) continue;
                leaves++;
                if (!approach(p, pos)) continue;
                AABB area = new AABB(pos).inflate(2);
                require(level.getEntitiesOfClass(ItemEntity.class, area).isEmpty(), "采收区域有未归属掉落 " + pos);
                p.getInventory().selected = emptyHotbar(p);
                require(p.canInteractWithBlock(pos, 0), "采叶越距 " + pos);
                require(p.gameMode.destroyBlock(pos), "采叶失败 " + pos);
                info("采叶位置=" + pos.toShortString() + "，受控站位=" + p.position());
                for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area)) {
                    ItemStack drop = entity.getItem().copy();
                    info("采叶 " + pos.toShortString() + " 掉落 " + drop);
                    require(p.getInventory().add(drop) && drop.isEmpty(), "背包无法接收采叶掉落");
                    entity.discard();
                }
                harvested++;
                if (count(p, sapling) > 0 && count(p, fruit) > 0) break;
            }
            require(leaves > 0, "模板内未发现目标树叶 " + templateId);
        }
        require(count(p, sapling) > 0 && count(p, fruit) > 0, "有限采样未获得食材和树苗 " + templateId);
        info(templateId + " 人工放置次数=" + placements + "，原位采叶=" + harvested
                + "，树苗=" + count(p, sapling) + "，食材=" + count(p, fruit));
    }

    /** 只调整服务端站位到无碰撞的近处，采集本身仍在树叶原坐标。 */
    private static boolean approach(ServerPlayer p, BlockPos pos) {
        for (int dy = -2; dy <= 2; dy++) for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            Vec3 dest = new Vec3(pos.getX() + dx + 0.5, pos.getY() + dy, pos.getZ() + dz + 0.5);
            if (!p.level().noCollision(p, p.getBoundingBox().move(dest.subtract(p.position())))) continue;
            p.connection.teleport(dest.x, dest.y, dest.z, 0, 0);
            if (p.canInteractWithBlock(pos, 0)) return true;
        }
        return false;
    }

    private void grow(ServerPlayer p, BlockPos pos, String saplingId, String logId, String leafId, int height) {
        p.connection.teleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 2.5, 0, 0);
        Item sapling = item(saplingId);
        int before = count(p, sapling);
        select(p, sapling);
        use(p, pos.below());
        require(p.level().getBlockState(pos).is(block(saplingId)) && count(p, sapling) == before - 1, "树苗放置或消耗失败 " + pos);
        int attempts = 0;
        while (p.level().getBlockState(pos).is(block(saplingId)) && attempts < 512) {
            int empty = emptyHotbar(p);
            p.getInventory().selected = empty;
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_MEAL, 1));
            use(p, pos);
            require(p.getMainHandItem().isEmpty(), "骨粉未消耗 " + pos);
            attempts++;
        }
        checkTree(p, pos, logId, leafId, height);
        info(saplingId + " 来源苗种植并长成，骨粉次数=" + attempts + "，位置=" + pos.toShortString());
    }

    private void craft(ServerPlayer p) {
        p.connection.teleport(17.5, 180, 4.5, 0, 0);
        p.serverLevel().setBlock(TABLE, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        p.getInventory().selected = emptyHotbar(p);
        use(p, TABLE);
        require(p.containerMenu instanceof CraftingMenu, "工作台菜单未打开");
        Item[] inputs = {item("cassia_01"), item("shicai_010"), item("shicai_01901"), item("shicai_009"), item("dingxiangflowerdry")};
        for (int i : new int[]{0, 2, 4}) require(p.getInventory().add(new ItemStack(inputs[i])), "夹具配料无法放入背包");
        int[] before = new int[inputs.length];
        AbstractContainerMenu menu = p.containerMenu;
        for (int i = 0; i < inputs.length; i++) {
            before[i] = count(p, inputs[i]);
            Item ingredient = inputs[i];
            Slot from = menu.slots.stream().filter(s -> s.container == p.getInventory() && s.getItem().is(ingredient)).findFirst().orElseThrow();
            menu.clicked(from.index, 0, ClickType.PICKUP, p);
            menu.clicked(i + 1, 1, ClickType.PICKUP, p);
            if (!menu.getCarried().isEmpty()) menu.clicked(from.index, 0, ClickType.PICKUP, p);
        }
        Item result = item("tiaowei_07");
        require(menu.getSlot(0).getItem().is(result) && menu.getSlot(0).getItem().getCount() == 3, "调味料结果错误");
        Slot dest = menu.slots.stream().filter(s -> s.container == p.getInventory() && !s.hasItem()).findFirst().orElseThrow();
        menu.clicked(0, 0, ClickType.PICKUP, p);
        menu.clicked(dest.index, 0, ClickType.PICKUP, p);
        require(menu.getCarried().isEmpty() && !menu.getSlot(0).hasItem() && count(p, result) == 3, "取出结果失败");
        for (int i = 0; i < inputs.length; i++) require(count(p, inputs[i]) == before[i] - 1, "配方消耗错误 " + inputs[i]);
        p.closeContainer();
        info("工作台五种配料各消耗1，产出调味料3；两种采收食材确实参与配方。");
    }

    private void verifyReload(ServerPlayer p) {
        checkTree(p, FIRST, "shicai_00902", "shicai_00903", 1);
        checkTree(p, SECOND, "shicai_01001", "shicai_01002", 3);
        require(count(p, item("tiaowei_07")) == 3, "重进后调味料数量错误");
        info("同档重进后两树干、树冠和3个调味料均保留。");
    }

    private static void checkTree(ServerPlayer p, BlockPos pos, String log, String leaf, int height) {
        for (int y = 0; y < height; y++) require(p.level().getBlockState(pos.above(y)).is(block(log)), "树干缺失 " + pos.above(y));
        boolean found = false;
        for (BlockPos at : BlockPos.betweenClosed(pos.offset(-4, 1, -4), pos.offset(4, 12, 4))) {
            if (p.level().getBlockState(at).is(block(leaf))) { found = true; break; }
        }
        require(found, "未找到对应树冠 " + pos);
    }

    private static void select(ServerPlayer p, Item item) {
        for (int i = 0; i < 9; i++) if (p.getInventory().getItem(i).is(item)) { p.getInventory().selected = i; return; }
        throw new IllegalStateException("快捷栏没有 " + item);
    }
    private static int emptyHotbar(ServerPlayer p) {
        for (int i = 0; i < 9; i++) if (p.getInventory().getItem(i).isEmpty()) return i;
        throw new IllegalStateException("快捷栏没有空位");
    }
    private static void use(ServerPlayer p, BlockPos pos) {
        require(p.canInteractWithBlock(pos, 0), "交互越距 " + pos);
        require(p.gameMode.useItemOn(p, p.serverLevel(), p.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atBottomCenterOf(pos).add(0, 1, 0), Direction.UP, pos, false)).consumesAction(), "交互未接受 " + pos);
    }
    private static int count(ServerPlayer p, Item item) { return p.getInventory().countItem(item); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
    private static Item item(String path) { return BuiltInRegistries.ITEM.getOptional(id(path)).orElseThrow(); }
    private static Block block(String path) { return BuiltInRegistries.BLOCK.getOptional(id(path)).orElseThrow(); }
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    private void info(String message) { report.add("INFO 调味树受控供给 " + message); }
}
