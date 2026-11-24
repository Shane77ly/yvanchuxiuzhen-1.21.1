package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.system.town.TownRuntime;
import net.yvanchuxiuzhen.system.town.client.TownBuildScreen;
import net.yvanchuxiuzhen.system.town.content.TownBlockEntity;
import net.yvanchuxiuzhen.system.town.data.TownBuildPlan;
import net.yvanchuxiuzhen.system.town.data.TownData;

/** 三类城镇建筑十二个模板的真实建造、材料结算与保存重进检查。 */
final class TownTemplatePlacementCheck extends SystemCheck {
    private static final String[] PREFIXES = {"build01", "build02", "build03"};
    private static final String[] BUILD_TYPES = {"市镇中心", "工筑堂", "灰瓦民居"};
    private static final String[] DIRECTIONS = {"坐北朝南", "坐南朝北", "坐东朝西", "坐西朝东"};
    private static final String[] SUFFIXES = {"nouth", "south", "east", "west"};
    private static final int CASE_COUNT = PREFIXES.length * SUFFIXES.length;
    private static final int[] EXPECTED_REMAINDERS = {0, 2, 0, 1, 1, 2};
    private static final Item[] EXPECTED_ITEMS = {Items.OAK_LOG, Items.OAK_LOG, Items.COBBLESTONE,
            Items.COBBLESTONE, Items.WHITE_WOOL, Items.DIAMOND};
    private static final List<BlockPos> UNSUPPORTED_TORCHES = List.of(new BlockPos(7, 6, 28),
            new BlockPos(7, 6, 29), new BlockPos(40, 6, 28), new BlockPos(40, 6, 29));
    private static final List<BlockPos> CARPETS_ABOVE_SIGNS = List.of(new BlockPos(4, 3, 15),
            new BlockPos(21, 3, 10), new BlockPos(10, 3, 4), new BlockPos(15, 3, 21));

    private final BlockPos[] plans = new BlockPos[CASE_COUNT];
    private final BlockPos[] origins = new BlockPos[CASE_COUNT];
    private final BlockPos[] cores = new BlockPos[CASE_COUNT];
    private final String[] templateNames = new String[CASE_COUNT];
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Screenshots screenshots;
    private BlockPos firstOrigin;
    private Vec3 originalPosition;
    private int originalSelected;
    private boolean originalFlying;

    TownTemplatePlacementCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("城镇十二模板真实建造", report, levelId);
        this.screenshots = screenshots;
        NeoForge.EVENT_BUS.addListener(this::freezeFixtureDrops);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::prepare);
            return true;
        }
        if (step >= 1 && step <= CASE_COUNT * 4) {
            int index = (step - 1) / 4;
            switch ((step - 1) % 4) {
                case 0 -> onServer(player -> prepareTemplate(player, index));
                case 1 -> {
                    if (!waitForPlan(local, index)) return false;
                    use(plans[index]);
                }
                case 2 -> {
                    if (!waitForBuildScreen(local, index)) return false;
                    if (local < 10) return false;
                    screenshots.take("r13_town_" + templateNames[index] + ".png");
                    pressBuildButton();
                }
                case 3 -> {
                    if (local < 60) return false;
                    onServer(player -> verifyTemplate(player, index));
                    minecraft.player.closeContainer();
                }
                default -> throw new IllegalStateException("未知城镇模板阶段");
            }
            return true;
        }
        if (step == CASE_COUNT * 4 + 1) {
            leaveWorld();
            return true;
        }
        if (step == CASE_COUNT * 4 + 2) return reopened(local);
        if (step == CASE_COUNT * 4 + 3) return inWorld(local);

        int reopenStart = CASE_COUNT * 4 + 4;
        if (step >= reopenStart && step < reopenStart + CASE_COUNT * 2) {
            int index = (step - reopenStart) / 2;
            if ((step - reopenStart) % 2 == 0) {
                onServer(player -> player.connection.teleport(plans[index].getX() + .5, plans[index].getY(), plans[index].getZ() - 1.5, 0, 15));
            } else {
                if (local < 30) return false;
                onServer(player -> verifyTemplate(player, index));
                if (index == CASE_COUNT - 1) onServer(this::restorePlayer);
            }
            if (index == CASE_COUNT - 1 && (step - reopenStart) % 2 == 1) finish();
            return true;
        }
        throw new IllegalStateException("未知城镇模板检查步骤 " + step);
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalSelected = player.getInventory().selected;
        originalFlying = player.getAbilities().flying;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        player.getInventory().clearContent();
        player.getInventory().selected = 8;
        BlockPos start = player.blockPosition().offset(128, 0, 128);
        firstOrigin = new BlockPos(start.getX(), Math.max(96, player.blockPosition().getY() + 24), start.getZ());
    }

    private void prepareTemplate(ServerPlayer player, int index) {
        int type = index / SUFFIXES.length;
        int direction = index % SUFFIXES.length;
        String templateName = PREFIXES[type] + SUFFIXES[direction];
        ServerLevel level = player.serverLevel();
        StructureTemplate template = level.getStructureManager().getOrCreate(id(templateName));
        Block planningBlock = BuiltInRegistries.BLOCK.get(id("chengzhen_buildblock"));
        Block templateCoreBlock = BuiltInRegistries.BLOCK.get(id(type == 0 ? "chengzhen_antai" : "chengzhen_buildblock"));
        BlockPos core = template.filterBlocks(BlockPos.ZERO, placeSettings(), templateCoreBlock).getFirst().pos();
        BlockPos origin = firstOrigin.offset(index % 4 * 56, 0, index / 4 * 56);
        BlockPos plan = origin.offset(core);
        plans[index] = plan;
        origins[index] = origin;
        cores[index] = core;
        templateNames[index] = templateName;

        level.setBlock(plan.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(plan, planningBlock.defaultBlockState(), 3);
        TownBlockEntity town = (TownBlockEntity) level.getBlockEntity(plan);
        CompoundTag build = TownRuntime.centerScroll().getOrDefault(TownData.BUILD_PLAN.get(), TownBuildPlan.EMPTY).data();
        build.putString("build_type", BUILD_TYPES[type]);
        build.putString("build_name", BUILD_TYPES[type]);
        build.putString("build_direction", DIRECTIONS[direction]);
        build.putInt("construction_need_log", 5);
        build.putInt("construction_need_stone", 6);
        build.putInt("construction_need_wool", 2);
        town.getPersistentData().put("build_data", build);
        CompoundTag data = town.getPersistentData();
        data.putInt("shiftingX", core.getX());
        data.putInt("shiftingY", core.getY());
        data.putInt("shiftingZ", core.getZ());
        data.putInt("lengthofX", template.getSize().getX());
        data.putInt("lengthofY", template.getSize().getY());
        data.putInt("lengthofZ", template.getSize().getZ());
        data.putString("team", "");
        town.setItem(0, fixture(Items.OAK_LOG, 4, index, "log-0"));
        town.setItem(1, fixture(Items.OAK_LOG, 3, index, "log-1"));
        town.setItem(2, fixture(Items.COBBLESTONE, 2, index, "stone-0"));
        town.setItem(3, fixture(Items.COBBLESTONE, 5, index, "stone-1"));
        town.setItem(4, fixture(Items.WHITE_WOOL, 3, index, "wool"));
        town.setItem(5, fixture(Items.DIAMOND, 2, index, "extra"));
        TownRuntime.countMaterials(town);
        town.sync();
        level.setBlock(plan.north(2).below(), Blocks.STONE.defaultBlockState(), 3);
        player.connection.teleport(plan.getX() + .5, plan.getY(), plan.getZ() - 1.5, 0, 15);
        player.inventoryMenu.broadcastChanges();
    }

    private void verifyTemplate(ServerPlayer player, int index) {
        BlockPos plan = plans[index];
        BlockPos origin = origins[index];
        String templateName = templateNames[index];
        ServerLevel level = player.serverLevel();
        if (!(level.getBlockEntity(plan) instanceof TownBlockEntity town)) {
            expectTrue(templateName + " 核心保留城镇方块实体", false);
            return;
        }
        int type = index / SUFFIXES.length;
        int direction = index % SUFFIXES.length;
        CompoundTag data = town.getPersistentData();
        CompoundTag build = data.getCompound("build_data");
        Block expectedCore = BuiltInRegistries.BLOCK.get(id(type == 0 ? "chengzhen_antai" : "chengzhen_buildblock"));
        expect(templateName + " 实际核心方块", id(type == 0 ? "chengzhen_antai" : "chengzhen_buildblock"),
                BuiltInRegistries.BLOCK.getKey(level.getBlockState(plan).getBlock()));
        expect(templateName + " 核心容器槽数", type == 0 ? 1 : 18, town.getContainerSize());
        expect(templateName + " 完工状态", true, data.getBoolean("construction_completed"));
        expect(templateName + " 建筑类型元数据", BUILD_TYPES[type], build.getString("build_type"));
        expect(templateName + " 建筑方向元数据", DIRECTIONS[direction], build.getString("build_direction"));
        expect(templateName + " 保存团队字段", "", data.getString("team"));
        expect(templateName + " 核心偏移X", cores[index].getX(), data.getInt("shiftingX"));
        expect(templateName + " 核心偏移Y", cores[index].getY(), data.getInt("shiftingY"));
        expect(templateName + " 核心偏移Z", cores[index].getZ(), data.getInt("shiftingZ"));

        StructureTemplate template = level.getStructureManager().getOrCreate(id(templateName));
        expect(templateName + " 保存结构宽度", template.getSize().getX(), data.getInt("lengthofX"));
        expect(templateName + " 保存结构高度", template.getSize().getY(), data.getInt("lengthofY"));
        expect(templateName + " 保存结构深度", template.getSize().getZ(), data.getInt("lengthofZ"));
        expect(templateName + " 保存木材需求", 5, build.getInt("construction_need_log"));
        expect(templateName + " 保存石材需求", 6, build.getInt("construction_need_stone"));
        expect(templateName + " 保存羊毛需求", 2, build.getInt("construction_need_wool"));
        int blockMismatches = 0;
        int blockEntityMismatches = 0;
        List<String> mismatchDetails = new ArrayList<>();
        List<StructureTemplate.StructureBlockInfo> blocks = templateBlocks(template);
        for (StructureTemplate.StructureBlockInfo info : blocks) {
            BlockPos worldPos = origin.offset(info.pos());
            // 锁定来源的北向中心模板有四支下方为空气的立式火把，原生邻块更新后应移除。
            BlockState expected = templateName.equals("build01nouth") && UNSUPPORTED_TORCHES.contains(info.pos())
                    ? Blocks.AIR.defaultBlockState() : info.state();
            // 原生模板先放特殊形状块，最后放带NBT的墙牌；这四块牌顶地毯先失去支撑。
            if (type == 1 && expected.is(Blocks.BROWN_CARPET) && info.pos().equals(CARPETS_ABOVE_SIGNS.get(direction))) {
                expected = Blocks.AIR.defaultBlockState();
            }
            if (expected.is(Blocks.VINE)) expected = Block.updateFromNeighbourShapes(expected, level, worldPos);
            if (!level.getBlockState(worldPos).equals(expected)) {
                blockMismatches++;
                if (mismatchDetails.size() < 8) mismatchDetails.add(info.pos() + ": " + expected + " -> " + level.getBlockState(worldPos));
            }
            if (info.nbt() != null && !info.pos().equals(cores[index])) {
                BlockEntity actual = level.getBlockEntity(worldPos);
                String expectedId = info.nbt().getString("id");
                String actualId = actual == null ? "" : String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(actual.getType()));
                if (!expectedId.equals(actualId)) blockEntityMismatches++;
            }
        }
        expect(templateName + " 模板实际方块状态偏差 " + mismatchDetails, 0, blockMismatches);
        expect(templateName + " 模板方块实体类型偏差", 0, blockEntityMismatches);
        expectTrue(templateName + " 模板包含实际结构方块", !blocks.isEmpty());
        verifyMaterials(player, town, index);
        expect(templateName + " 模板核心注册方块定义", expectedCore, level.getBlockState(plan).getBlock());
    }

    private void verifyMaterials(ServerPlayer player, TownBlockEntity town, int index) {
        String templateName = templateNames[index];
        if (index / SUFFIXES.length == 0) {
            expect(templateName + " 中心单槽余料容器为空", true, town.getItem(0).isEmpty());
            expect(templateName + " 中心余料与无关物品完整掉落", 6, fixtureDropCount(player, index));
            return;
        }
        for (int slot = 0; slot < EXPECTED_REMAINDERS.length; slot++) {
            expect(templateName + " 原槽余料" + slot, EXPECTED_REMAINDERS[slot], town.getItem(slot).getCount());
            if (EXPECTED_REMAINDERS[slot] > 0) {
                expect(templateName + " 原槽物品" + slot, EXPECTED_ITEMS[slot], town.getItem(slot).getItem());
            }
        }
        for (int slot = EXPECTED_REMAINDERS.length; slot < town.getContainerSize(); slot++) {
            expect(templateName + " 其余空槽" + slot, true, town.getItem(slot).isEmpty());
        }
        expect(templateName + " 规划核心无重复掉落", 0, fixtureDropCount(player, index));
    }

    private List<StructureTemplate.StructureBlockInfo> templateBlocks(StructureTemplate template) {
        List<StructureTemplate.StructureBlockInfo> result = new ArrayList<>();
        StructurePlaceSettings settings = placeSettings();
        for (Block block : BuiltInRegistries.BLOCK) result.addAll(template.filterBlocks(BlockPos.ZERO, settings, block));
        return result;
    }

    private int fixtureDropCount(ServerPlayer player, int index) {
        String prefix = fixturePrefix(index);
        int count = 0;
        for (Entity entity : player.serverLevel().getAllEntities()) {
            if (entity instanceof ItemEntity drop && drop.getItem().getHoverName().getString().startsWith(prefix)) {
                count += drop.getItem().getCount();
            }
        }
        return count;
    }

    private void restorePlayer(ServerPlayer player) {
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot).copy());
        player.getInventory().selected = originalSelected;
        player.getAbilities().flying = originalFlying;
        player.onUpdateAbilities();
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, 0, 0);
        player.inventoryMenu.broadcastChanges();
    }

    private boolean waitForBuildScreen(int local, int index) {
        if (minecraft.screen instanceof TownBuildScreen) return true;
        if (local > 200) {
            fail(templateNames[index] + " 未通过真实右键打开建筑物资页");
            onServer(this::restorePlayer);
            finish();
        }
        return false;
    }

    private boolean waitForPlan(int local, int index) {
        BlockPos plan = plans[index];
        if (minecraft.player.distanceToSqr(Vec3.atCenterOf(plan)) < 16
                && minecraft.level.getBlockEntity(plan) instanceof TownBlockEntity town
                && town.getPersistentData().getCompound("build_data").getString("build_name")
                        .equals(BUILD_TYPES[index / SUFFIXES.length])) return true;
        if (local > 400) {
            fail(templateNames[index] + " 客户端尚未同步玩家传送、规划方块或受控建造数据");
            onServer(this::restorePlayer);
            finish();
        }
        return false;
    }

    private void use(BlockPos pos) {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private void pressBuildButton() {
        minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals("建造"))
                .map(Button.class::cast).findFirst().orElseThrow().onPress();
    }

    private ItemStack fixture(Item item, int count, int index, String suffix) {
        ItemStack stack = new ItemStack(item, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(fixturePrefix(index) + suffix));
        return stack;
    }

    private void freezeFixtureDrops(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity entity
                && entity.getItem().getHoverName().getString().startsWith("r13-town-template-")) {
            entity.setNoGravity(true);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setPickUpDelay(12000);
        }
    }

    private String fixturePrefix(int index) {
        return "r13-town-template-" + index + "-";
    }

    private static StructurePlaceSettings placeSettings() {
        return new StructurePlaceSettings().setIgnoreEntities(false);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", name);
    }
}

