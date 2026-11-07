package net.yvanchuxiuzhen.system.skill;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.api.FlightEnchantmentEvent;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 轻功的开关、掠空、每秒耗灵力与禁空；玩法状态由服务端写入，原生能力和速度通知本人。 */
public final class SkillFlight {
    private SkillFlight() {
    }

    public static void toggle(ServerPlayer player) {
        SkillBook book = Skills.book(Skills.flight(player)).orElse(null);
        boolean advanced = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + Cultivation.tizhiDouble(player, "LevelNumberCorrect") >= 31;
        if (!player.isCreative() && (book == null || book.name().isEmpty()) && !advanced) {
            message(player, "§4还没有装备飞行技能！");
        } else if (!player.isCreative() && (book == null || !book.ownedBy(player.getUUID())) && !advanced) {
            message(player, book == null || book.owner().isEmpty() ? "§4还没有习得此飞行技能！" : "§4此飞行技能非您习得的，无法使用！");
        } else if (!player.isCreative() && player.level().dimension().location().equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fabao_009weidu"))) {
            message(player, "§4此维度禁止飞行！");
        } else {
            boolean starting = !Skills.flying(player);
            if (starting) {
                sound(player, "flybegin", 1.2f);
                beginVisuals(player, book);
                SkillMovement.move(player, new Vec3(0, .5, 0));
                abilities(player, true);
            } else if (!player.isCreative()) {
                abilities(player, false);
            }
            Skills.setFlying(player, starting);
            resetGliding(player);
        }
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }

    private static void abilities(ServerPlayer player, boolean flying) {
        player.getAbilities().mayfly = flying;
        player.getAbilities().flying = flying;
        player.onUpdateAbilities();
    }

    private static void resetGliding(ServerPlayer player) {
        Cultivation.setTizhiDouble(player, "lvekongspeed", 0);
        Cultivation.setTizhiDouble(player, "superjet", 0);
        Cultivation.setTizhiBoolean(player, "lvekong", false);
    }

    static void accelerate(ServerPlayer player) {
        if (!Cultivation.tizhiBoolean(player, "lvekong")) {
            sound(player, "flyspeeedbegin", 1.3f);
            Cultivation.setTizhiBoolean(player, "lvekong", true);
            int jet = Enchants.armorSum(player, Enchants.key("jet"));
            if (jet != 0) {
                Cultivation.setTizhiDouble(player, "superjet", 1 + jet * .2);
                player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 30, 0, 0, 0, 1);
                player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 30, 1, 0, 1, 0);
            }
        } else if (Cultivation.tizhiDouble(player, "lvekongspeed") < 4) {
            Cultivation.setTizhiDouble(player, "lvekongspeed", Cultivation.tizhiDouble(player, "lvekongspeed") + 1);
        }
    }

    static void decelerate(ServerPlayer player) {
        double speed = Cultivation.tizhiDouble(player, "lvekongspeed");
        if (speed > 0) {
            Cultivation.setTizhiDouble(player, "lvekongspeed", Math.max(0, speed - 1));
        } else if (Cultivation.tizhiBoolean(player, "lvekong")) {
            Cultivation.setTizhiBoolean(player, "lvekong", false);
            abilities(player, true);
        }
    }

    public static void tick(ServerPlayer player) {
        SkillBook book = Skills.book(Skills.flight(player)).orElse(null);
        boolean own = book != null && book.ownedBy(player.getUUID());
        if (Skills.flying(player)) {
            if (!player.isCreative()) {
                double cost = own ? book.number("flyskillxiaohao") : 12;
                if (StatusEffects.has(player, "skillwind_04")) cost = Math.max(1, cost - StatusEffects.amplifier(player, "skillwind_04") - 1);
                if (Cultivation.get(player, CultivationValue.POWER) > cost) {
                    player.fallDistance = 0;
                    double ticks = Cultivation.tizhiDouble(player, "flyxiaohaotick") + 1;
                    Cultivation.setTizhiDouble(player, "flyxiaohaotick", ticks >= 20 ? 0 : ticks);
                    if (ticks >= 20) {
                        FlightEnchantmentEvent ornaments = NeoForge.EVENT_BUS.post(new FlightEnchantmentEvent(player));
                        int levels = Enchants.armorSum(player, Enchants.key("flying")) + ornaments.levels();
                        double discount = 1 - levels * .05;
                        if (discount > 0) cost *= discount;
                        if (Cultivation.tizhiBoolean(player, "lvekong")) cost *= 2;
                        double speed = Cultivation.tizhiDouble(player, "lvekongspeed");
                        if (speed > 0) cost += speed * speed * 2;
                        Cultivation.add(player, CultivationValue.POWER, -cost);
                    }
                    flightVisuals(player, book);
                } else {
                    Skills.setFlying(player, false);
                    abilities(player, false);
                    Cultivation.setTizhiBoolean(player, "lvekong", false);
                }
            }
            if (Cultivation.tizhiBoolean(player, "lvekong")) {
                double speed = own ? book.number("flyskillspeed") : player.isCreative() ? 2 : 1;
                double throttle = Cultivation.tizhiDouble(player, "lvekongspeed");
                double jet = Cultivation.tizhiDouble(player, "superjet");
                double extra = 0;
                if (throttle > 0 || jet > 0) {
                    extra = throttle / 4;
                    if (jet > 0) {
                        extra += jet;
                        Cultivation.setTizhiDouble(player, "superjet", 0);
                        // 来源先清零再读取扣费值，因此喷射的额外灵力扣费为零。
                    }
                }
                SkillMovement.move(player, player.getLookAngle().scale(speed + extra));
            }
        }
        if (player.getPersistentData().getBoolean("jinkong")) {
            Skills.setFlying(player, false);
            if (!player.isCreative()) abilities(player, false);
            Cultivation.setTizhiBoolean(player, "takong", false);
            player.getPersistentData().putBoolean("jinkong", false);
        }
    }

    /** 禁空的每刻过程保留来源的互斥顺序：鞘翅下坠、关闭轻功、恢复重力。 */
    public static void forbid(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            if (!player.isCreative()) {
                if (player.isFallFlying()) {
                    SkillMovement.move(player, new Vec3(0, -1, 0));
                } else if (Skills.flying(player)) {
                    abilities(player, false);
                    resetGliding(player);
                    Skills.setFlying(player, false);
                } else if (player.isNoGravity()) {
                    player.setNoGravity(false);
                }
            }
        } else if (entity instanceof Mob && entity.isNoGravity()) {
            entity.setNoGravity(false);
        }
    }

    private static void sound(ServerPlayer player, String id, float volume) {
        player.serverLevel().playSound(null, player.blockPosition(), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), SoundSource.NEUTRAL, volume, 1);
    }

    /** 来源的 addParticle 圆环只在本人的客户端可见，逐个发送给本人，保留原式把角度数值直接用作弧度。 */
    static void cloudRing(ServerPlayer player, int count, int step, double radius, double y, double velocity) {
        for (int i = 1; i <= count; i++) {
            double x = Math.cos(i * step) * radius;
            double z = Math.sin(i * step) * radius;
            player.serverLevel().sendParticles(player, ParticleTypes.CLOUD, false, player.getX() + x, player.getY() + y, player.getZ() + z, 0,
                    x * velocity, 0, z * velocity, 1);
        }
    }

    private static void beginVisuals(ServerPlayer player, SkillBook book) {
        String effect = book == null ? "" : book.text("flyskilltexiao");
        if (effect.equals("云雾") || effect.equals("两行云雾") || effect.equals("拔空术")) {
            cloudRing(player, 72, 5, 1, 1, .2);
        } else if (effect.equals("火花")) {
            player.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), 20, 0, 0, 0, .5);
        }
    }

    private static void flightVisuals(ServerPlayer player, SkillBook book) {
        String effect = book == null ? "" : book.text("flyskilltexiao");
        switch (effect) {
            case "云雾" -> player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() - .2, player.getZ(), 10, .4, .2, .4, .05);
            case "两行云雾" -> {
                for (int offset : new int[]{90, -90}) {
                    player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX() + Math.cos(player.getYRot() + offset), player.getY() + 1,
                            player.getZ() + Math.sin(player.getYRot() + offset), 1, 0, 0, 0, 0);
                }
            }
            case "黑雾", "大黑雾" -> {
                player.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() - .2, player.getZ(), effect.equals("黑雾") ? 5 : 2, .5, .2, .5, 0);
                if (effect.equals("大黑雾")) player.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() - .5, player.getZ(), 20, 3, .5, 3, 0);
            }
            case "火花" -> player.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 5, 1, 1, 1, 0);
            default -> { }
        }
    }
}
