package net.yvanchuxiuzhen.system.guchong.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
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
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.api.GuchongTracking;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong05Entity;

/** 荧光蛊在三格视线落点释放，普通物品不会自动获得追踪坐标。 */
public final class GuchongTrackingItem extends ContentItem {
    public GuchongTrackingItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Vec3 eye = player.getEyePosition(1);
            BlockPos pos = level.clip(new ClipContext(eye, eye.add(player.getViewVector(1).scale(3)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();
            Guchong05Entity insect = GuchongSetup.<Guchong05Entity>entityType("guchong_05").get().create(serverLevel);
            insect.moveTo(pos.getX(), pos.getY(), pos.getZ(), level.random.nextFloat() * 360, 0);
            CompoundTag saved = GuchongTracking.fromState(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())
                    .get(DataComponents.CUSTOM_DATA).copyTag();
            insect.getPersistentData().merge(saved);
            EventHooks.finalizeMobSpawn(insect, serverLevel, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
            serverLevel.addFreshEntity(insect);
            stack.shrink(1);
        }
        return InteractionResultHolder.pass(stack);
    }
}
