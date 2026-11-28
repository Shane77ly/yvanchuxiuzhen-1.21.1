package net.yvanchuxiuzhen.system.cooking;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.yvanchuxiuzhen.system.cooking.content.WineVesselBlockEntity;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.StillBrewingRule;
import net.yvanchuxiuzhen.system.cooking.data.CookingTableRecipes;

/** 木甑按四个输入槽的物品身份匹配 JSON 酿酒配方，持续受下方 lit 状态控制。 */
public final class WoodenStillRuntime {
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
        if (still.brewing()) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cooking.wine.busy"), false);
            return;
        }
        Map<String, Integer> occupied = new HashMap<>();
        for (int slot = 0; slot < 4; slot++) {
            occupied.merge(BuiltInRegistries.ITEM.getKey(still.getItem(slot).getItem()).toString(), 1, Integer::sum);
        }
        for (StillBrewingRule rule : CookingTableRecipes.stillBrewing()) {
            Item target = BuiltInRegistries.ITEM.get(ResourceLocation.parse(rule.outputId()));
            ItemStack output = still.getItem(4);
            if (!occupied.equals(rule.ingredients()) || !output.isEmpty() && !output.is(target)) continue;
            still.setWineId(rule.outputId());
            still.setBrewing(true);
            still.changed();
            for (int slot = 0; slot < 4; slot++) still.getItem(slot).shrink(1);
            still.setChanged();
            return;
        }
    }

    public static void tick(ServerLevel level, BlockPos pos) {
        WineVesselBlockEntity still = (WineVesselBlockEntity) level.getBlockEntity(pos);
        if (!still.brewing() || !heated(level.getBlockState(pos.below()))) return;
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                4, 0.5, 0.5, 0.5, 0);
        still.setProgress(still.progress() + 1);
        still.changed();
        if (still.progress() >= brewTicks(still.wineId())) {
            still.setProgress(0);
            still.changed();
            ItemStack output = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(still.wineId())));
            output.setCount(still.getItem(4).getCount() + 1);
            still.setItem(4, output);
            still.setBrewing(false);
            still.changed();
        }
    }

    private static int brewTicks(String wineId) {
        for (StillBrewingRule rule : CookingTableRecipes.stillBrewing()) {
            if (rule.outputId().equals(wineId)) return rule.brewTicks();
        }
        return 100;
    }

    private static boolean heated(BlockState state) {
        return state.getBlock().getStateDefinition().getProperty("lit") instanceof BooleanProperty lit && state.getValue(lit);
    }
}
