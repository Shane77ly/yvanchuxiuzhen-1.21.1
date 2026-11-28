package net.yvanchuxiuzhen.system.town;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.town.content.TownBlockEntity;
import net.yvanchuxiuzhen.system.town.content.TownBuildMenu;
import net.yvanchuxiuzhen.system.town.content.TownMenu;
import net.yvanchuxiuzhen.system.town.data.TownBuildPlan;
import net.yvanchuxiuzhen.system.town.data.TownData;

/** 城镇规划与材料建设只写本系统方块状态。 */
public final class TownRuntime {
    private static final TagKey<Item> STONE = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "stone"));
    private static final TagKey<Item> WOOL = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "wool"));

    private TownRuntime() {
    }

    public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("obtainatowncenterscroll").executes(context -> {
            Vec3 pos = context.getSource().getPosition();
            ItemEntity item = new ItemEntity(context.getSource().getLevel(), pos.x + .5, pos.y + .5, pos.z + .5, centerScroll());
            item.setPickUpDelay(10);
            context.getSource().getLevel().addFreshEntity(item);
            return 0;
        }));
    }

    public static ItemStack centerScroll() {
        List<BlockPos> shifts = List.of(
                new BlockPos(18, 2, 3),
                new BlockPos(29, 2, 44),
                new BlockPos(44, 2, 18),
                new BlockPos(3, 2, 29));
        BlockPos standardLength = new BlockPos(48, 17, 48);
        TownBuildPlan build = new TownBuildPlan(
                "市镇中心案台", "市镇中心", "",
                shifts, List.of(standardLength, standardLength, standardLength, standardLength),
                60, 60, 3, 0, 0, 0);
        ItemStack stack = scroll(build);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("txt.yvanchuxiuzhen.build01"));
        return stack;
    }

    private static ItemStack scroll(TownBuildPlan build) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chengzhen_buildblock_item")));
        stack.set(TownData.BUILD_PLAN.get(), build);
        return stack;
    }

    public static void usePlan(ServerPlayer player, TownBlockEntity town) {
        if (!player.isShiftKeyDown()) {
            TownBuildMenu.open(player, town.getBlockPos());
        } else if (!town.completed()) {
            ItemHandlerHelper.giveItemToPlayer(player, scroll(town.buildPlan()));
            player.level().setBlock(town.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        } else if (BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "hammer"))) {
            player.level().setBlock(town.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        } else player.displayClientMessage(Component.literal("需要用建筑锤才可拆除此建筑！"), false);
    }

    public static void useCenter(ServerPlayer player, TownBlockEntity town) {
        if (town.masterUuid().isEmpty()) {
            if (town.townName().isEmpty()) town.setTownName("无名村落");
            if (town.townUuid().isEmpty()) {
                StringBuilder id = new StringBuilder();
                for (int index = 0; index < 8; index++) id.append((char) ('a' + player.getRandom().nextInt(26)));
                town.setTownUuid(id.toString());
            }
            if (town.townLevelNumber() < 1) {
                town.setTownLevelNumber(1);
                town.setTownLevelName("村落");
            }
            town.setMasterUuid(player.getStringUUID());
            town.sync();
        }
        TownMenu.open(player, town.getBlockPos(), town.masterUuid().equals(player.getStringUUID()) ? 2 : 1);
    }

    private static int material(ItemStack stack) {
        if (stack.is(ItemTags.LOGS)) return 0;
        if (stack.is(STONE)) return 1;
        if (stack.is(WOOL)) return 2;
        return -1;
    }

    public static void countMaterials(TownBlockEntity town) {
        int[] counts = new int[3];
        for (int slot = 0; slot < 18; slot++) {
            ItemStack stack = town.getItem(slot);
            int type = material(stack);
            if (type >= 0) counts[type] += stack.getCount();
        }
        town.setBuildPlan(town.buildPlan().withOwned(counts[0], counts[1], counts[2]));
        town.sync();
    }

    public static void build(ServerPlayer player, TownBlockEntity plan) {
        countMaterials(plan);
        TownBuildPlan build = plan.buildPlan();
        int[] required = new int[3];
        for (int type = 0; type < 3; type++) {
            required[type] = build.need(type);
            if (build.owned(type) < required[type]) {
                player.displayClientMessage(Component.literal("§4材料不足！"), false);
                return;
            }
        }
        List<ItemStack> remaining = new ArrayList<>(18);
        for (int slot = 0; slot < 18; slot++) remaining.add(plan.getItem(slot).copy());
        for (ItemStack stack : remaining) {
            if (required[0] == 0 && required[1] == 0 && required[2] == 0) break;
            int type = material(stack);
            if (type < 0 || required[type] < 1) continue;
            int taken = Math.min(required[type], stack.getCount());
            stack.shrink(taken);
            required[type] -= taken;
        }
        TownBuildPlan buildSnapshot = plan.buildPlan();
        int sx = plan.shiftingX(), sy = plan.shiftingY(), sz = plan.shiftingZ();
        int lx = plan.lengthOfX(), ly = plan.lengthOfY(), lz = plan.lengthOfZ();
        String team = plan.team();
        // 建筑模板可以把十八槽规划核心替换为一槽中心案台，清空旧库存后再放置，避免替换掉落重复结算。
        plan.clearContent();
        placeStructure(player.serverLevel(), plan.getBlockPos(), buildSnapshot, sx, sy, sz);
        TownBlockEntity current = (TownBlockEntity) player.level().getBlockEntity(plan.getBlockPos());
        current.setShifting(sx, sy, sz);
        current.setLengthOf(lx, ly, lz);
        current.setTeam(team);
        current.setBuildPlan(buildSnapshot);
        current.setCompleted(true);
        if (current.getContainerSize() == 18) {
            for (int slot = 0; slot < 18; slot++) current.setItem(slot, remaining.get(slot));
        } else {
            for (ItemStack stack : remaining) {
                if (!stack.isEmpty()) Containers.dropItemStack(player.level(), plan.getBlockPos().getX() + .5,
                        plan.getBlockPos().getY() + .5, plan.getBlockPos().getZ() + .5, stack);
            }
        }
        current.sync();
        if (current != plan) player.closeContainer();
    }

    private static void placeStructure(ServerLevel level, BlockPos pos, TownBuildPlan build, int sx, int sy, int sz) {
        String prefix = switch (build.buildType()) {
            case "市镇中心" -> "build01";
            case "工筑堂" -> "build02";
            case "灰瓦民居" -> "build03";
            default -> "";
        };
        String suffix = switch (build.buildDirection()) {
            case "坐北朝南" -> "nouth";
            case "坐南朝北" -> "south";
            case "坐东朝西" -> "east";
            case "坐西朝东" -> "west";
            default -> "";
        };
        if (prefix.isEmpty() || suffix.isEmpty()) return;
        BlockPos origin = pos.offset(-sx, -sy, -sz);
        level.getStructureManager().getOrCreate(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, prefix + suffix))
                .placeInWorld(level, origin, origin, new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false), level.random, 3);
    }
}
