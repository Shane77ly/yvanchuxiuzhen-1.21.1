package net.yvanchuxiuzhen.system.fulu.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 背包内生命危急时自动触发的符箓（22、23 号），来源为物品 {@code inventoryTick}。 */
public final class ThresholdTalismanItem extends ContentItem {
    private final int number;

    public ThresholdTalismanItem(Properties properties, List<Component> tooltip, int number) {
        super(properties, tooltip);
        this.number = number;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide() || !(entity instanceof LivingEntity living)
                || living.getHealth() > living.getMaxHealth() * 0.1) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        serverLevel.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + 1, entity.getZ()),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulushifang")),
                SoundSource.NEUTRAL, 1, 1);
        if (number == 22) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + 1, entity.getZ(),
                    10, 0, 0, 0, 1);
            double grade = grade(stack);
            living.setHealth((float) (living.getHealth() + living.getMaxHealth() * 0.1 * grade));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, (int) (grade * 2 + 3)));
        } else if (number == 23) {
            serverLevel.sendParticles(Particles.type("fulutexiao_11"), entity.getX(), entity.getY() + 1, entity.getZ(),
                    10, 0.5, 0, 0.5, 1);
            living.setHealth((float) (living.getMaxHealth() * 0.15));
            double grade = grade(stack);
            int scaledAmplifier = (int) (grade * 2 - 1);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, scaledAmplifier));
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, scaledAmplifier));
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, (int) (grade - 1)));
            living.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, scaledAmplifier));
        }
        stack.shrink(1);
    }

    private static double grade(ItemStack stack) {
        double grade = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("fululevel");
        return grade < 1 ? 1 : grade;
    }
}
