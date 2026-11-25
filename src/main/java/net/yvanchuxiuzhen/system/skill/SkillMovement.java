package net.yvanchuxiuzhen.system.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.skill.api.MountToggleFlightEvent;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillMovementState;
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
            NeoForge.EVENT_BUS.post(new MountToggleFlightEvent(player.getVehicle(), player));
            return;
        }
        if (Skills.flying(player) && (input == Input.W || input == Input.S)) {
            if (input == Input.W && Cultivation.thunderOpen(player)) return;
            if (repeated(player, input.name())) {
                if (input == Input.W) SkillFlight.accelerate(player);
                else SkillFlight.decelerate(player);
            }
            return;
        }
        SkillBook book = Skills.book(Skills.movement(player)).orElse(null);
        if (book == null || !book.ownedBy(player.getUUID())) return;
        SkillMovementState state = player.getData(SkillData.MOVEMENT);
        if (input == Input.JUMP) {
            if (book.number("shenfajumptick") > 0 && repeated(player, "JUMP")
                    && state.cooldownTicks() <= book.number("shenfajumptickMax")) {
                move(player, new Vec3(0, .6, 0));
                state = player.getData(SkillData.MOVEMENT);
                player.setData(SkillData.MOVEMENT, state.withCooldownTicks(state.cooldownTicks() + book.number("shenfajumptick")));
                SkillFlight.cloudRing(player, 36, 10, .3, 0, .1);
            }
            return;
        }
        double distance = book.number("shenfa" + input + "jump");
        if (input != Input.W && distance <= 0) return;
        if (!repeated(player, input.name())) return;
        state = player.getData(SkillData.MOVEMENT);
        if (distance > 0 && state.cooldownTicks() <= book.number("shenfatickMax")) {
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
            player.setData(SkillData.MOVEMENT, state.withCooldownTicks(state.cooldownTicks() + book.number("shenfatick")));
        }
        if (input == Input.W && book.number("shenfaWspeed") > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, (int) book.number("shenfaWspeed") - 1, false, false));
        }
    }

    private static boolean repeated(ServerPlayer player, String key) {
        SkillMovementState state = player.getData(SkillData.MOVEMENT);
        if (state.window(key) <= 0) {
            player.setData(SkillData.MOVEMENT, state.withWindow(key, 10));
            return false;
        }
        return true;
    }

    static void move(ServerPlayer player, Vec3 motion) {
        player.setDeltaMovement(motion);
        player.hurtMarked = true;
    }

    public static void tick(ServerPlayer player) {
        SkillMovementState state = player.getData(SkillData.MOVEMENT);
        double w = state.wWindow() > 0 ? state.wWindow() - 1 : state.wWindow();
        double s = state.sWindow() > 0 ? state.sWindow() - 1 : state.sWindow();
        double a = state.aWindow() > 0 ? state.aWindow() - 1 : state.aWindow();
        double d = state.dWindow() > 0 ? state.dWindow() - 1 : state.dWindow();
        double j = state.jumpWindow() > 0 ? state.jumpWindow() - 1 : state.jumpWindow();
        double cd = state.cooldownTicks();
        if (cd > 0) {
            int levels = Enchants.armorSum(player, Enchants.key("flyingskill"));
            cd = Math.max(0, cd - (1 + levels * .03));
        }
        if (w != state.wWindow() || s != state.sWindow() || a != state.aWindow()
                || d != state.dWindow() || j != state.jumpWindow() || cd != state.cooldownTicks()) {
            player.setData(SkillData.MOVEMENT, new SkillMovementState(w, s, a, d, j, cd));
        }
    }
}
