package net.yvanchuxiuzhen.system.town;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.CustomData;
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

/** 城镇规划与材料建设只写本系统方块状态。 */
public final class TownRuntime {
    private static final String[] MATERIALS = {"log", "stone", "wool"};
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
        CompoundTag build = new CompoundTag();
        build.putString("build_name", "市镇中心案台");
        build.putString("build_type", "市镇中心");
        int[][] shifts = {{18,2,3},{29,2,44},{44,2,18},{3,2,29}};
        String[] axes = {"X", "Y", "Z"};
        for (int direction = 0; direction < 4; direction++) {
            for (int axis = 0; axis < 3; axis++) {
                build.putInt("shifting0" + (direction + 1) + axes[axis], shifts[direction][axis]);
                build.putInt("lengthof0" + (direction + 1) + axes[axis], axis == 1 ? 17 : 48);
            }
        }
        build.putInt("construction_need_log", 60);
        build.putInt("construction_need_stone", 60);
        build.putInt("construction_need_wool", 3);
        ItemStack stack = scroll(build);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("txt.yvanchuxiuzhen.build01"));
        return stack;
    }

    private static ItemStack scroll(CompoundTag build) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chengzhen_buildblock_item")));
        CompoundTag data = new CompoundTag();
        data.put("build_data", build.copy());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    public static void usePlan(ServerPlayer player, TownBlockEntity town) {
        if (!player.isShiftKeyDown()) {
            TownBuildMenu.open(player, town.getBlockPos());
        } else if (!town.getPersistentData().getBoolean("construction_completed")) {
            ItemHandlerHelper.giveItemToPlayer(player, scroll(town.getPersistentData().getCompound("build_data")));
            player.level().setBlock(town.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        } else if (BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "hammer"))) {
            player.level().setBlock(town.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        } else player.displayClientMessage(Component.literal("需要用建筑锤才可拆除此建筑！"), false);
    }

    public static void useCenter(ServerPlayer player, TownBlockEntity town) {
        CompoundTag data = town.getPersistentData();
        if (data.getString("belongToMasterUUID").isEmpty()) {
            if (data.getString("town_name").isEmpty()) data.putString("town_name", "无名村落");
            if (data.getString("town_uuid").isEmpty()) {
                StringBuilder id = new StringBuilder();
                for (int index = 0; index < 8; index++) id.append((char) ('a' + player.getRandom().nextInt(26)));
                data.putString("town_uuid", id.toString());
            }
            if (data.getInt("town_levelnumber") < 1) {
                data.putInt("town_levelnumber", 1);
                data.putString("town_levelname", "村落");
            }
            data.putString("belongToMasterUUID", player.getStringUUID());
            town.sync();
        }
        TownMenu.open(player, town.getBlockPos(), data.getString("belongToMasterUUID").equals(player.getStringUUID()) ? 2 : 1);
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
        CompoundTag build = town.getPersistentData().getCompound("build_data");
        for (int type = 0; type < 3; type++) build.putInt("construction_need_" + MATERIALS[type] + "_number", counts[type]);
        town.getPersistentData().put("build_data", build);
        town.sync();
    }

    public static void build(ServerPlayer player, TownBlockEntity plan) {
        countMaterials(plan);
        CompoundTag old = plan.getPersistentData().copy();
        CompoundTag build = old.getCompound("build_data");
        int[] required = new int[3];
        for (int type = 0; type < 3; type++) {
            required[type] = build.getInt("construction_need_" + MATERIALS[type]);
            if (build.getInt("construction_need_" + MATERIALS[type] + "_number") < required[type]) {
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
        // 建筑模板可以把十八槽规划核心替换为一槽中心案台，清空旧库存后再放置，避免替换掉落重复结算。
        plan.clearContent();
        placeStructure(player.serverLevel(), plan.getBlockPos(), old);
        TownBlockEntity current = (TownBlockEntity) player.level().getBlockEntity(plan.getBlockPos());
        for (String key : new String[]{"shiftingX", "shiftingY", "shiftingZ", "lengthofX", "lengthofY", "lengthofZ"}) current.getPersistentData().putInt(key, old.getInt(key));
        current.getPersistentData().putString("team", old.getString("team"));
        current.getPersistentData().put("build_data", old.getCompound("build_data"));
        current.getPersistentData().putBoolean("construction_completed", true);
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

    private static void placeStructure(ServerLevel level, BlockPos pos, CompoundTag data) {
        CompoundTag build = data.getCompound("build_data");
        String prefix = switch (build.getString("build_type")) {
            case "市镇中心" -> "build01";
            case "工筑堂" -> "build02";
            case "灰瓦民居" -> "build03";
            default -> "";
        };
        String suffix = switch (build.getString("build_direction")) {
            case "坐北朝南" -> "nouth";
            case "坐南朝北" -> "south";
            case "坐东朝西" -> "east";
            case "坐西朝东" -> "west";
            default -> "";
        };
        if (prefix.isEmpty() || suffix.isEmpty()) return;
        BlockPos origin = pos.offset(-data.getInt("shiftingX"), -data.getInt("shiftingY"), -data.getInt("shiftingZ"));
        level.getStructureManager().getOrCreate(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, prefix + suffix))
                .placeInWorld(level, origin, origin, new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE).setIgnoreEntities(false), level.random, 3);
    }
}
