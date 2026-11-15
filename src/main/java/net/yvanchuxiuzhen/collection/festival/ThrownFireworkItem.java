package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 三类抛出烟花的自然右键：按视线速度生成对应实体，生存模式消耗一件。 */
public final class ThrownFireworkItem extends ContentItem {
    private final ThrownFirework.Kind kind;

    ThrownFireworkItem(Properties properties, List<Component> tooltip, ThrownFirework.Kind kind) {
        super(properties, tooltip);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (level instanceof ServerLevel server) {
            ThrownFirework firework = FestivalSetup.thrownFireworkType(kind).get().create(server);
            firework.moveTo(player.getX(), player.getY() + 1.4, player.getZ(), level.random.nextFloat() * 360, 0);
            firework.setDeltaMovement(player.getLookAngle());
            EventHooks.finalizeMobSpawn(firework, server, server.getCurrentDifficultyAt(firework.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            server.addFreshEntity(firework);
            if (!player.getAbilities().instabuild) result.getObject().shrink(1);
        }
        return result;
    }
}
