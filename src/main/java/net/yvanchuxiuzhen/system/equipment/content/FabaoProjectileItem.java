package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.equipment.data.FabaoTempData;
import net.yvanchuxiuzhen.system.equipment.projectile.FabaoProjectile;

/** 1–8 号法宝的共同发射入口；来源以自身物品作为弹药，并按法宝类别设置冷却与发射参数。 */
public final class FabaoProjectileItem extends ContentItem {
    private final FabaoProjectile.Kind kind;

    public FabaoProjectileItem(FabaoProjectile.Kind kind, Properties properties, List<Component> tooltip) {
        super(withAttributes(kind, properties), tooltip);
        this.kind = kind;
    }

    private static Properties withAttributes(FabaoProjectile.Kind kind, Properties properties) {
        if (kind == FabaoProjectile.Kind.FABAO_001 || kind == FabaoProjectile.Kind.FABAO_002) {
            return properties.attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 3,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build());
        }
        if (kind == FabaoProjectile.Kind.FABAO_008) {
            return properties.attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 2,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build());
        }
        return properties;
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
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (kind.autoRelease() && entity instanceof ServerPlayer player && prepare(stack, player)) {
            shoot(stack, player);
            player.releaseUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!kind.autoRelease() && entity instanceof ServerPlayer player && prepare(stack, player)) {
            shoot(stack, player);
        }
    }

    private boolean prepare(ItemStack stack, ServerPlayer player) {
        if (kind == FabaoProjectile.Kind.FABAO_004) {
            return true;
        }
        if (player.getMainHandItem().getItem() != stack.getItem()
                && player.getOffhandItem().getItem() != stack.getItem()) {
            return false;
        }
        player.setData(EquipmentData.FABAO_TEMP.get(), new FabaoTempData(stack, false));
        player.getCooldowns().addCooldown(stack.getItem(), kind.cooldown());
        return true;
    }

    private void shoot(ItemStack stack, ServerPlayer player) {
        ItemStack ammunition = findAmmunition(player);
        if (!player.getAbilities().instabuild && ammunition.isEmpty()) {
            return;
        }
        FabaoProjectile projectile = FabaoProjectile.shoot(player.serverLevel(), player, kind);
        SoundEvent sound = switch (kind) {
            case FABAO_001, FABAO_002, FABAO_003, FABAO_004 -> SoundEvents.SNOWBALL_THROW;
            case FABAO_005, FABAO_006, FABAO_007, FABAO_008 -> SoundEvents.ARROW_SHOOT;
        };
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), sound,
                SoundSource.PLAYERS, 1, 1f / (player.serverLevel().random.nextFloat() * .5f + 1) + kind.speed() / 4);
        EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);
        if (player.getAbilities().instabuild) {
            projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else {
            if (kind != FabaoProjectile.Kind.FABAO_004 && ammunition == stack && ammunition.getCount() == 1) {
                player.setData(EquipmentData.FABAO_TEMP.get(), new FabaoTempData(stack.copyWithCount(1), true));
            }
            ammunition.shrink(1);
            if (ammunition.isEmpty()) {
                player.getInventory().removeItem(ammunition);
            }
        }
    }

    private ItemStack findAmmunition(ServerPlayer player) {
        ItemStack held = ProjectileWeaponItem.getHeldProjectile(player, stack -> stack.is(this));
        if (!held.isEmpty()) {
            return held;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(this)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
