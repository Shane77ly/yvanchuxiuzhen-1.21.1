package net.yvanchuxiuzhen.system.skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 身法的十刻双击窗口、共享冷却和跳跃窗口；飞行时 W、S 改为掠空与调速。 */
public final class SkillMovement {
    public enum Input {
        W, S, A, D, JUMP
    }

    private SkillMovement() {
    }

    public static void press(ServerPlayer player, Input input) {
        if (input == Input.W && player.isPassenger()) {
            CompoundTag mount = player.getVehicle().getPersistentData();
            mount.putBoolean("flying", !mount.getBoolean("flying"));
            return;
        }
        if (Skills.flying(player) && (input == Input.W || input == Input.S)) {
            if (input == Input.W && Cultivation.thunderOpen(player)) return;
            if (repeated(player, "shenfaanjian" + input)) {
                if (input == Input.W) SkillFlight.accelerate(player);
                else SkillFlight.decelerate(player);
            }
            return;
        }
        SkillBook book = Skills.book(Skills.movement(player)).orElse(null);
        if (book == null || !book.ownedBy(player.getUUID())) return;
        CompoundTag data = player.getPersistentData();
        if (input == Input.JUMP) {
            if (book.number("shenfajumptick") > 0 && repeated(player, "shenfatiaoyve")
                    && data.getDouble("shenfatick") <= book.number("shenfajumptickMax")) {
                move(player, new Vec3(0, .6, 0));
                data.putDouble("shenfatick", data.getDouble("shenfatick") + book.number("shenfajumptick"));
                SkillFlight.cloudRing(player, 36, 10, .3, 0, .1);
            }
            return;
        }
        double distance = book.number("shenfa" + input + "jump");
        if (input != Input.W && distance <= 0) return;
        if (!repeated(player, "shenfaanjian" + input)) return;
        if (distance > 0 && data.getDouble("shenfatick") <= book.number("shenfatickMax")) {
            double yaw = player.getYRot() + switch (input) {
                case W -> 0;
                case S -> 180;
                case A -> -90;
                case D -> 90;
                default -> throw new IllegalStateException("身法方向：" + input);
            };
            double angle = yaw * (input == Input.W ? 3.14 : Math.PI) / 180;
            move(player, new Vec3(-distance * Math.sin(angle), .4, distance * Math.cos(angle)));
            player.causeFoodExhaustion(.1f);
            data.putDouble("shenfatick", data.getDouble("shenfatick") + book.number("shenfatick"));
        }
        if (input == Input.W && book.number("shenfaWspeed") > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, (int) book.number("shenfaWspeed") - 1, false, false));
        }
    }

    private static boolean repeated(ServerPlayer player, String key) {
        CompoundTag data = player.getPersistentData();
        if (data.getDouble(key) <= 0) {
            data.putDouble(key, 10);
            return false;
        }
        return true;
    }

    static void move(ServerPlayer player, Vec3 motion) {
        player.setDeltaMovement(motion);
        player.hurtMarked = true;
    }

    public static void tick(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        for (String key : new String[]{"shenfaanjianW", "shenfaanjianS", "shenfaanjianA", "shenfaanjianD", "shenfatiaoyve"}) {
            if (data.getDouble(key) > 0) data.putDouble(key, data.getDouble(key) - 1);
        }
        if (data.getDouble("shenfatick") > 0) {
            int levels = Enchants.armorSum(player, Enchants.key("flyingskill"));
            data.putDouble("shenfatick", Math.max(0, data.getDouble("shenfatick") - (1 + levels * .03)));
        }
    }
}
