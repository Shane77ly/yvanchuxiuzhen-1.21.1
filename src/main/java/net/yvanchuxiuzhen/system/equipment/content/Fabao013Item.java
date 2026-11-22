package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentSword;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 穿界古镜：主世界与人间道之间双向传送，分别保存两个方向的落点坐标；沉默时不生效，每次使用 12000 刻冷却。 */
public final class Fabao013Item extends ContentSword {
    private static final String X = "chuansongX";
    private static final String Y = "chuansongY";
    private static final String Z = "chuansongZ";
    private static final String TARGET_X = "mubiaoX";
    private static final String TARGET_Y = "mubiaoY";
    private static final String TARGET_Z = "mubiaoZ";
    private static final ResourceKey<Level> RENJIAN = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimensionrenjian"));
    private static final int COOLDOWN = 12000;

    public Fabao013Item(LegacyTier tier, Properties properties, List<Component> tooltip) {
        super(tier, properties, tooltip, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)
                || StatusEffects.has(serverPlayer, "effect_002")) {
            return result;
        }
        ItemStack stack = result.getObject();
        if (serverLevel.dimension() == Level.OVERWORLD) {
            store(stack, X, Y, Z, player.getX(), player.getY(), player.getZ());
            double[] target = readOr(stack, TARGET_X, TARGET_Y, TARGET_Z, 0, 120, 0);
            travel(serverPlayer, RENJIAN, target);
            cultistMark(serverPlayer);
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN);
        } else if (serverLevel.dimension() == RENJIAN) {
            store(stack, TARGET_X, TARGET_Y, TARGET_Z, player.getX(), player.getY(), player.getZ());
            double[] target = readOr(stack, X, Y, Z, 0, 32, 0);
            travel(serverPlayer, Level.OVERWORLD, target);
            cultistMark(serverPlayer);
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN);
        }
        return result;
    }

    private static void travel(ServerPlayer player, ResourceKey<Level> destination, double[] target) {
        ServerLevel next = player.server.getLevel(destination);
        if (next != null) {
            player.teleportTo(next, target[0], target[1], target[2], player.getYRot(), player.getXRot());
        }
    }

    /** 来源写入体质物品的 {@code kaqiangpanding}：玩家刻在身体位置被占用时把玩家向上推出。 */
    private static void cultistMark(ServerPlayer player) {
        Cultivation.setTizhiBoolean(player, "kaqiangpanding", true);
    }

    private static void store(ItemStack stack, String x, String y, String z, double px, double py, double pz) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putDouble(x, px);
        data.putDouble(y, py);
        data.putDouble(z, pz);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    private static double[] readOr(ItemStack stack, String x, String y, String z, double defaultX, double defaultY, double defaultZ) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.getDouble(x) != 0 || data.getDouble(y) != 0 || data.getDouble(z) != 0) {
            return new double[]{data.getDouble(x), data.getDouble(y), data.getDouble(z)};
        }
        return new double[]{defaultX, defaultY, defaultZ};
    }
}
