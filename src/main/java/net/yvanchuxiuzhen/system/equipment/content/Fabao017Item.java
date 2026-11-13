package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;

/** 摄虫网的普通捕获与潜行困虫入口，保持来源品阶条件和两档冷却。 */
public final class Fabao017Item extends ContentItem {
    public Fabao017Item(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!(level instanceof ServerLevel server) || StatusEffects.has(player, "effect_002")) return result;
        ItemStack stack = result.getObject();
        boolean trapping = player.isShiftKeyDown();
        if (trapping && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("item_level_num") < 1) {
            player.displayClientMessage(Component.literal("§4需要法宝等级在黄阶一级以上，才可释放此技能"), false);
            return result;
        }
        BlockPos pos = level.clip(new ClipContext(player.getEyePosition(1), player.getEyePosition(1).add(player.getViewVector(1).scale(2)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();
        Fabao01701Entity net = EquipmentSetup.fabao017Type().get().create(server);
        net.moveTo(pos.getX(), pos.getY(), pos.getZ(), level.random.nextFloat() * 360, 0);
        Vec3 look = player.getLookAngle();
        net.setDeltaMovement(look.x, look.y + .5, look.z);
        net.configure(player, trapping);
        net.finalizeSpawn(server, server.getCurrentDifficultyAt(net.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(net);
        player.getCooldowns().addCooldown(stack.getItem(), trapping ? 400 : 50);
        return result;
    }
}
