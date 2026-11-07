package net.yvanchuxiuzhen.system.skill;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;

/** 十六种技能弹体物品：开始使用后，松手按各物品的静态 shoot 参数发射，不经过技能书施放条件。 */
public final class SkillProjectileItem extends ContentItem {
    public SkillProjectileItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTicks) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        String id = BuiltInRegistries.ITEM.getKey(this).getPath();
        float power = id.equals("skill_light_02") || id.equals("skill_water_05") ? 2 : 1;
        double damage = id.equals("skill_water_0101") ? 1 : 0.1;
        int knockback = switch (id) {
            case "skill_water_03", "skill_ice_02", "skill_ice_03", "skill_rock_01" -> 1;
            default -> 0;
        };
        SkillProjectile projectile = SkillSetup.<SkillProjectile>entityType("projectile_" + id).get().create(player.serverLevel()).launchedWith(knockback, 0);
        projectile.setPos(player.getX(), player.getEyeY() - (double) 0.1F, player.getZ());
        projectile.setOwner(player);
        Vec3 direction = player.getViewVector(1);
        projectile.shoot(direction.x, direction.y, direction.z, power * 2, 0);
        projectile.setCritArrow(id.equals("skill_rock_01"));
        projectile.setBaseDamage(damage);
        level.addFreshEntity(projectile);
        float pitch = 1 / (level.random.nextFloat() * 0.5F + 1) + power / 2;
        // 其余来源物品的声音 ID 为空，原加载器不广播声音。
        if (id.equals("skill_water_0101")) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 1, pitch);
        }
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        projectile.pickup = AbstractArrow.Pickup.DISALLOWED;
    }
}
