package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.api.FabaoFireData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.equipment.projectile.Fabao010huoyan;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.yihuo.api.Yihuo;

/** 覆火葫芦的释放与潜行吸收；火块识别和消费由 yihuo 公开接口负责。 */
public final class Fabao010Item extends ContentItem {
    public Fabao010Item(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        ItemStack stack = result.getObject();
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer
                && !StatusEffects.has(serverPlayer, "effect_002")) {
            if (player.isShiftKeyDown()) {
                absorb(serverLevel, serverPlayer, stack);
            } else {
                release(serverPlayer, stack);
            }
        }
        return result;
    }

    private static void release(ServerPlayer player, ItemStack stack) {
        FabaoFireData fire = stack.getOrDefault(EquipmentData.FABAO_FIRE.get(), FabaoFireData.EMPTY);
        int selected = 0;
        for (int grade = 10; grade >= 1; grade--) {
            if (fire.get(grade) <= 0) continue;
            int cost = powerCost(grade);
            if (cost == 0 || Cultivation.get(player, CultivationValue.POWER) >= cost) {
                selected = grade;
                break;
            }
        }
        if (selected == 0) {
            player.displayClientMessage(Component.literal(fire.get(1) <= 0 ? "§4空" : "§4您的灵力不足！"), true);
            return;
        }
        int cost = powerCost(selected);
        if (cost > 0) {
            Cultivation.add(player, CultivationValue.POWER, -cost);
        }
        if (selected == 1) {
            Fabao010huoyan.shoot(player.serverLevel(), player, .6f, 10);
        } else {
            Yihuo.shootMaterial(player, 219 + selected, .6f, 10);
        }
        stack.set(EquipmentData.FABAO_FIRE.get(), fire.add(selected, -1));
    }

    private static int powerCost(int grade) {
        return grade <= 1 ? 0 : 1 << (grade - 2);
    }

    private static void absorb(ServerLevel level, ServerPlayer player, ItemStack stack) {
        BlockPos center = level.clip(new ClipContext(player.getEyePosition(1),
                player.getEyePosition(1).add(player.getViewVector(1).scale(5)), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, player)).getBlockPos();
        int levelNumber = Equipment.product(stack).map(EquipmentProduct::level)
                .orElse((int) Equipment.properties(stack).number("item_level_num"));
        int maximumGrade = Yihuo.fabaoAbsorptionGrade(levelNumber);
        FabaoFireData fire = stack.getOrDefault(EquipmentData.FABAO_FIRE.get(), FabaoFireData.EMPTY);
        for (int x = -4; x < 4; x++) {
            for (int y = -4; y < 4; y++) {
                for (int z = -4; z < 4; z++) {
                    int grade = Yihuo.absorbFire(level, center.offset(x, y, z), maximumGrade);
                    if (grade > 0) {
                        fire = fire.add(grade, 1);
                    }
                }
            }
        }
        stack.set(EquipmentData.FABAO_FIRE.get(), fire);
    }
}
