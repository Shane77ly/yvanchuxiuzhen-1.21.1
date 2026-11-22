package net.yvanchuxiuzhen.system.creature;

import java.util.List;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.fulu.api.TalismanTamingEvent;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 神傀符的妖兽驯服消费者：限制、原始概率叠加、阵营替换与成功状态清理。 */
final class TalismanTaming {
    private static final List<Holder<MobEffect>> VANILLA = List.of(MobEffects.DIG_SLOWDOWN, MobEffects.CONFUSION,
            MobEffects.BLINDNESS, MobEffects.HUNGER, MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.WITHER, MobEffects.UNLUCK);
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private TalismanTaming() { }

    static void register() {
        NeoForge.EVENT_BUS.addListener(TalismanTaming::hit);
    }

    private static void hit(TalismanTamingEvent event) {
        ServerLevel level = event.level();
        Entity target = event.target();
        Entity shooter = event.shooter();
        if (target.getPersistentData().getBoolean("fubenentity")) {
            message(shooter, "dungeon");
            return;
        }
        if (target.getPersistentData().getBoolean("gaoshipaientity")) {
            message(shooter, "bounty");
            return;
        }
        Objective objective = level.getScoreboard().getObjective("zhaohuanwu");
        if (objective != null && level.getScoreboard().getOrCreatePlayerScore(target, objective).get() == 1) {
            message(shooter, "summoned");
            return;
        }
        if (target instanceof TamableAnimal animal && animal.isTame()) {
            return;
        }
        Chance chance = chance(target, shooter, event.grade());
        level.sendParticles(Particles.type(chance.delta() >= 0 ? "stargreen" : "starcrimson"), target.getX(),
                target.getY() + target.getBbHeight() / 2, target.getZ(), 16, 0, 0, 0, 1);
        if (new Random().nextInt(1, 101) <= chance.value()) {
            Relations.replaceTamingFriends(target);
            if (target instanceof TamableAnimal animal && shooter instanceof Player player) {
                animal.tame(player);
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + target.getBbHeight() / 2,
                    target.getZ(), 8, target.getBbWidth(), target.getBbHeight(), target.getBbWidth(), 1);
            Relations.setTamingTeam(shooter, target);
            message(shooter, "success", target.getDisplayName().getString());
            if (target instanceof LivingEntity living) {
                living.removeAllEffects();
                living.addEffect(new MobEffectInstance(effect("effect_002"), 100, 0));
                living.addEffect(new MobEffectInstance(effect("effect_033"), 100, 0));
            }
            target.getPersistentData().putString("gongjizhe", "");
            target.getPersistentData().putString("linshigongjizhe", "");
        } else {
            int threshold = chance.value() >= 90 ? 90 : chance.value() >= 80 ? 80 : chance.value() >= 65 ? 65
                    : chance.value() >= 50 ? 50 : chance.value() >= 35 ? 35 : chance.value() >= 20 ? 20
                    : chance.value() >= 10 ? 10 : chance.value() >= 5 ? 5 : chance.value() > 0 ? 1 : 0;
            message(shooter, "failure_" + threshold);
            if (chance.show()) {
                message(shooter, "chance", chance.value());
                if (chance.delta() < 0) {
                    message(shooter, "low_grade");
                }
            }
        }
    }

    /** 概率保留来源顺序与未截断值；023、024存在时仍读取032等级，缺少032按零级计。 */
    static Chance chance(Entity target, Entity shooter, double grade) {
        float health = target instanceof LivingEntity living ? living.getHealth() : -1;
        float maxHealth = target instanceof LivingEntity living ? living.getMaxHealth() : -1;
        double value = (1 - health / maxHealth) * 20;
        double delta = grade * 10 - target.getPersistentData().getDouble("LEVELnumber");
        value += delta >= 0 ? delta : -delta * delta;
        if (target instanceof LivingEntity living) {
            MobEffectInstance slow = living.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (slow != null) {
                int amplifier = slow.getAmplifier();
                value = amplifier <= 9 ? value + (amplifier + 1) * 2 : value + 20 + amplifier - 9;
            }
        }
        value += contribution(shooter, MobEffects.LUCK);
        value -= contribution(shooter, MobEffects.UNLUCK);
        for (int index = 1; index <= 19; index++) {
            value += contribution(target, effect("duyao_%02d".formatted(index)));
        }
        for (int index = 1; index <= 7; index++) {
            value += contribution(target, effect("effect_%03d".formatted(index)));
        }
        value += contribution(target, effect("effect_032"));
        if (target instanceof LivingEntity living) {
            for (String id : List.of("effect_023", "effect_024")) {
                if (living.hasEffect(effect(id))) {
                    MobEffectInstance wrong = living.getEffect(effect("effect_032"));
                    value += (wrong == null ? 0 : wrong.getAmplifier()) + 1;
                }
            }
        }
        for (Holder<MobEffect> effect : VANILLA) {
            value += contribution(target, effect);
        }
        boolean show = false;
        if (shooter instanceof LivingEntity living) {
            for (EquipmentSlot slot : ARMOR) {
                int charm = Enchants.slot(living, slot, Enchants.key("charm"));
                if (charm != 0) {
                    value += charm;
                    show = true;
                }
            }
        }
        return new Chance(value, delta, show);
    }

    private static int contribution(Entity entity, Holder<MobEffect> effect) {
        MobEffectInstance instance = entity instanceof LivingEntity living ? living.getEffect(effect) : null;
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    private static Holder<MobEffect> effect(String id) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)).orElseThrow();
    }

    private static void message(Entity shooter, String key, Object... arguments) {
        if (shooter instanceof Player player) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.taming." + key, arguments), false);
        }
    }

    record Chance(double value, double delta, boolean show) { }
}
