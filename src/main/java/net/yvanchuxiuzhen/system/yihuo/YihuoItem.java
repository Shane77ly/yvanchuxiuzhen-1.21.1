package net.yvanchuxiuzhen.system.yihuo;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 十八种异火材料共用的使用入口；首个使用刻扣灵力并发射对应异火弹体。 */
public final class YihuoItem extends ContentItem {
    private final int material;
    private final int grade;
    private final double requiredPower;
    private final double cost;

    public YihuoItem(int material, Properties properties, List<Component> tooltip) {
        super(properties.attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 1 + grade(material) * 3,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build()), tooltip);
        this.material = material;
        grade = grade(material);
        cost = 1 << grade;
        requiredPower = material == 234 ? 8 : cost;
    }

    private static int grade(int material) {
        return material % 10 - 1;
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
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(livingEntity instanceof ServerPlayer player)) {
            return;
        }
        if (!canRelease(player)) {
            return;
        }
        ServerLevel server = player.serverLevel();
        YihuoProjectile projectile = YihuoProjectiles.type(material).get().create(server);
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        projectile.shoot(player.getViewVector(1).x, player.getViewVector(1).y, player.getViewVector(1).z, 2, 0);
        projectile.setBaseDamage(0.1);
        projectile.setSilent(true);
        server.addFreshEntity(projectile);
        server.playSound(null, player.getX(), player.getY(), player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fireshoot")),
                SoundSource.PLAYERS, 1, 1 / (level.random.nextFloat() * 0.5F + 1) + 0.5F);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        player.stopUsingItem();
    }

    private boolean canRelease(ServerPlayer player) {
        if (StatusEffects.has(player, "effect_002")) {
            return false;
        }
        if (player.isCreative()) {
            Cultivation.setTizhiDouble(player, "yihuoshifangshu", 0);
            Cultivation.setTizhiDouble(player, "yihuojishi", 0);
            return true;
        }
        if (Cultivation.get(player, CultivationValue.POWER) < requiredPower) {
            return false;
        }
        Cultivation.add(player, CultivationValue.POWER, -cost);
        double now = player.level().dayTime();
        double last = Cultivation.tizhiDouble(player, "yihuojishi");
        double count = Cultivation.tizhiDouble(player, "yihuoshifangshu");
        if (last == 0 || now < last || now >= last + 100) {
            count = 1;
        } else {
            count += 1;
            if (count >= 5) {
                player.getCooldowns().addCooldown(this, 100);
            }
        }
        Cultivation.setTizhiDouble(player, "yihuoshifangshu", count);
        Cultivation.setTizhiDouble(player, "yihuojishi", now);
        return true;
    }
}
