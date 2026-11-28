package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentSword;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.equipment.api.SpatialPocketCoords;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 穿界古镜：主世界与人间道之间双向传送，分别保存两个方向的落点坐标；沉默时不生效，每次使用 12000 刻冷却。 */
public final class Fabao013Item extends ContentSword {
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
        SpatialPocketCoords coords = stack.getOrDefault(EquipmentData.SPATIAL_COORDS.get(), SpatialPocketCoords.EMPTY);
        if (serverLevel.dimension() == Level.OVERWORLD) {
            stack.set(EquipmentData.SPATIAL_COORDS.get(), new SpatialPocketCoords(
                    player.getX(), player.getY(), player.getZ(), coords.targetX(), coords.targetY(), coords.targetZ()));
            double[] target = orDefault(coords.targetX(), coords.targetY(), coords.targetZ(), 0, 120, 0);
            travel(serverPlayer, RENJIAN, target);
            cultistMark(serverPlayer);
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN);
        } else if (serverLevel.dimension() == RENJIAN) {
            stack.set(EquipmentData.SPATIAL_COORDS.get(), new SpatialPocketCoords(
                    coords.x(), coords.y(), coords.z(), player.getX(), player.getY(), player.getZ()));
            double[] target = orDefault(coords.x(), coords.y(), coords.z(), 0, 32, 0);
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

    private static double[] orDefault(double x, double y, double z, double defaultX, double defaultY, double defaultZ) {
        return (x != 0 || y != 0 || z != 0) ? new double[]{x, y, z} : new double[]{defaultX, defaultY, defaultZ};
    }
}
