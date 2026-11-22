package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 炮竹（pao_01）与窜天猴（pao_08）共用的按住右键/松手发射入口。 */
public final class SimpleFireworkProjectileItem extends ContentItem {
    public enum Kind {
        PAO_01(UseAnim.NONE),
        PAO_08(UseAnim.BOW);

        private final UseAnim animation;

        Kind(UseAnim animation) {
            this.animation = animation;
        }

        public UseAnim animation() {
            return animation;
        }
    }

    private final Kind kind;

    public SimpleFireworkProjectileItem(Properties properties, List<Component> tooltip, Kind kind) {
        super(properties, tooltip);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return kind.animation();
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack held, Level level, LivingEntity entity, int timeLeft) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof ServerPlayer player)) return;
        ItemStack ammunition = ProjectileWeaponItem.getHeldProjectile(player, stack -> stack.is(this));
        if (ammunition.isEmpty()) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(this)) {
                    ammunition = stack;
                    break;
                }
            }
        }
        if (!player.getAbilities().instabuild && ammunition.isEmpty()) return;
        SimpleFireworkProjectileEntity projectile = SimpleFireworkProjectileEntity.shoot(server, player, kind);
        if (player.getAbilities().instabuild) {
            projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else {
            ammunition.shrink(1);
        }
    }
}
