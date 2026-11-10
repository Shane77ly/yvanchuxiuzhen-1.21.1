package net.yvanchuxiuzhen.system.cooking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.WineVesselBlockEntity;

/** 木甑按四个输入槽的物品身份匹配配方，持续受下方 lit 状态控制。 */
public final class WoodenStillRuntime {
    private static final List<Map<String, Integer>> RECIPES = List.of(
            Map.of("yvanchuxiuzhen:shicai_002", 3, "yvanchuxiuzhen:ginseng", 1),
            Map.of("yvanchuxiuzhen:shicai_002", 4),
            Map.of("minecraft:wheat", 2, "yvanchuxiuzhen:shicai_005", 2),
            Map.of("yvanchuxiuzhen:jiuniang_001", 1, "yvanchuxiuzhen:fragranceflower", 3),
            Map.of("yvanchuxiuzhen:shicai_032", 4),
            Map.of("yvanchuxiuzhen:peachpeach", 4),
            Map.of("yvanchuxiuzhen:jiuniang_001", 1, "yvanchuxiuzhen:peachflower", 3),
            Map.of("yvanchuxiuzhen:jiuniang_001", 1, "yvanchuxiuzhen:xvejing", 3));

    private WoodenStillRuntime() {
    }

    public static void button(ServerPlayer player, WineVesselBlockEntity still, int button) {
        if (button == 1) {
            for (String key : new String[] {"message.yvanchuxiuzhen.cooking.wine.recipes.title",
                    "message.yvanchuxiuzhen.cooking.wine.recipes.001", "message.yvanchuxiuzhen.cooking.wine.recipes.002",
                    "message.yvanchuxiuzhen.cooking.wine.recipes.003", "message.yvanchuxiuzhen.cooking.wine.recipes.005"}) {
                player.displayClientMessage(Component.translatable(key), false);
            }
        } else if (button == 0) {
            start(player, still);
        }
    }

    private static void start(ServerPlayer player, WineVesselBlockEntity still) {
        if (!heated(still.getLevel().getBlockState(still.getBlockPos().below()))) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cooking.wine.needs_heat"), false);
            return;
        }
        CompoundTag data = still.getPersistentData();
        if (data.getBoolean("begin")) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cooking.wine.busy"), false);
            return;
        }
        Map<String, Integer> occupied = new HashMap<>();
        for (int slot = 0; slot < 4; slot++) {
            occupied.merge(BuiltInRegistries.ITEM.getKey(still.getItem(slot).getItem()).toString(), 1, Integer::sum);
        }
        for (int recipe = 0; recipe < RECIPES.size(); recipe++) {
            String result = "jiuniang_%03d".formatted(recipe + 1);
            ItemStack output = still.getItem(4);
            if (!occupied.equals(RECIPES.get(recipe)) || !output.isEmpty() && !output.is(WineRuntime.item(result))) continue;
            data.putString("jiuniangid", ModId.VALUE + ":" + result);
            data.putBoolean("begin", true);
            still.changed();
            for (int slot = 0; slot < 4; slot++) still.getItem(slot).shrink(1);
            still.setChanged();
            return;
        }
    }

    public static void tick(ServerLevel level, BlockPos pos) {
        WineVesselBlockEntity still = (WineVesselBlockEntity) level.getBlockEntity(pos);
        CompoundTag data = still.getPersistentData();
        if (!data.getBoolean("begin") || !heated(level.getBlockState(pos.below()))) return;
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                4, 0.5, 0.5, 0.5, 0);
        data.putDouble("jindu", data.getDouble("jindu") + 1);
        still.changed();
        if (data.getDouble("jindu") >= 100) {
            data.putDouble("jindu", 0);
            still.changed();
            ItemStack output = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(data.getString("jiuniangid"))));
            output.setCount(still.getItem(4).getCount() + 1);
            still.setItem(4, output);
            data.putBoolean("begin", false);
            still.changed();
        }
    }

    private static boolean heated(BlockState state) {
        return state.getBlock().getStateDefinition().getProperty("lit") instanceof BooleanProperty lit && state.getValue(lit);
    }
}
