package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanBlockEntity;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong03Entity;

/** 观察同一只放出飞蝗、同一只原版动物及原罐的服务端战斗和维护刻。 */
final class RenjianLocustCombatWitness {
    private static final int COMBAT_BUDGET = 400;
    private static final int AFTER_REMOVAL_TICKS = 40;
    private final List<String> report;
    private final Consumer<LivingDamageEvent.Pre> beforeDamage = this::onBeforeDamage;
    private final Consumer<LivingDamageEvent.Post> afterDamage = this::onAfterDamage;
    private final Consumer<LivingDeathEvent> dying = this::onDeath;
    private final Consumer<LevelTickEvent.Post> ticking = this::onTick;
    private ServerLevel level;
    private Guchong03Entity fly;
    private Animal prey;
    private TaoguanBlockEntity jar;
    private String jarId;
    private ItemStack savedSlot;
    private long releasedAt;
    private long removedAt = -1;
    private int lastFlyAge;
    private double lastMaintenanceTick;
    private int maintenanceCycles;
    private DamageSource pendingDamage;
    private float healthBeforeDamage;
    private int hits;
    private boolean acceptedTarget;
    private boolean killedByFly;
    private boolean done;

    RenjianLocustCombatWitness(List<String> report) { this.report = report; }

    void start() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, beforeDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, afterDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, dying);
        NeoForge.EVENT_BUS.addListener(ticking);
    }

    void stop() {
        NeoForge.EVENT_BUS.unregister(beforeDamage);
        NeoForge.EVENT_BUS.unregister(afterDamage);
        NeoForge.EVENT_BUS.unregister(dying);
        NeoForge.EVENT_BUS.unregister(ticking);
    }

    void track(ServerLevel level, Guchong03Entity fly, Animal prey, TaoguanBlockEntity jar, String jarId, ItemStack savedSlot) {
        this.level = level;
        this.fly = fly;
        this.prey = prey;
        this.jar = jar;
        this.jarId = jarId;
        this.savedSlot = savedSlot.copy();
        releasedAt = level.getGameTime();
        lastFlyAge = fly.tickCount;
        lastMaintenanceTick = fly.getPersistentData().getDouble("guchongtick");
        report.add("INFO REPLAY_LOCUST_COMBAT_TRACK released=" + fly.getUUID() + " target=" + prey.getUUID()
                + " jar=" + jarId + " slot=0 gameTime=" + releasedAt + " flyNoAI=" + fly.isNoAi());
    }

    boolean done() { return done; }

    void timeout() {
        if (!done) gap("client_tick_budget");
    }

    private void observeCommand() {
        if (!acceptedTarget && fly.getTarget() == prey && fly.getPersistentData().getString("gongjimubiao").isEmpty()) {
            acceptedTarget = true;
            report.add("INFO REPLAY_LOCUST_COMBAT_UUID_CONSUMED gameTime=" + level.getGameTime()
                    + " flyAge=" + fly.tickCount + " released=" + fly.getUUID() + " target=" + prey.getUUID());
        }
    }

    private boolean nativeHit(LivingEntity victim, DamageSource source) {
        return fly != null && !done && victim == prey && source.is(DamageTypes.MOB_ATTACK)
                && source.getDirectEntity() == fly && source.getEntity() == fly;
    }

    private void onBeforeDamage(LivingDamageEvent.Pre event) {
        if (!nativeHit(event.getEntity(), event.getSource())) return;
        pendingDamage = event.getSource();
        healthBeforeDamage = prey.getHealth();
    }

    private void onAfterDamage(LivingDamageEvent.Post event) {
        if (!nativeHit(event.getEntity(), event.getSource()) || pendingDamage != event.getSource()) return;
        pendingDamage = null;
        observeCommand();
        float healthAfter = prey.getHealth();
        if (!acceptedTarget || fly.getTarget() != prey || !(event.getNewDamage() > 0) || !(healthAfter < healthBeforeDamage)) return;
        hits++;
        report.add("INFO REPLAY_LOCUST_COMBAT_DAMAGE hit=" + hits + " gameTime=" + level.getGameTime()
                + " released=" + fly.getUUID() + " target=" + prey.getUUID() + " original=" + event.getOriginalDamage()
                + " final=" + event.getNewDamage() + " health=" + healthBeforeDamage + "->" + healthAfter);
    }

    private void onDeath(LivingDeathEvent event) {
        if (fly == null || done || event.getEntity() != prey) return;
        killedByFly = nativeHit(prey, event.getSource()) && hits > 0;
        report.add("INFO REPLAY_LOCUST_COMBAT_TARGET_DEATH gameTime=" + level.getGameTime() + " target=" + prey.getUUID()
                + " killedByFly=" + killedByFly + " " + state());
        if (!killedByFly) gap("target_death_without_witnessed_native_hit");
    }

    private void onTick(LevelTickEvent.Post event) {
        if (fly == null || done || event.getLevel() != level) return;
        observeCommand();
        long now = level.getGameTime();
        double maintenanceTick = fly.getPersistentData().getDouble("guchongtick");
        // 以实体自身的维护计数归零为准，区块未推进时不计入周期。
        if (removedAt >= 0 && fly.tickCount != lastFlyAge && lastMaintenanceTick > 0 && maintenanceTick == 0) {
            maintenanceCycles++;
            report.add("INFO REPLAY_LOCUST_COMBAT_MAINTENANCE cycle=" + maintenanceCycles + " gameTime=" + now + " " + state());
        }
        lastFlyAge = fly.tickCount;
        lastMaintenanceTick = maintenanceTick;
        if (removedAt < 0 && prey.isRemoved()) {
            if (!killedByFly || prey.getRemovalReason() != Entity.RemovalReason.KILLED) {
                gap("target_removed_without_witnessed_kill");
                return;
            }
            removedAt = now;
            report.add("INFO REPLAY_LOCUST_COMBAT_TARGET_REMOVED gameTime=" + now + " reason=" + prey.getRemovalReason()
                    + " " + state());
        }
        if (removedAt >= 0 && now - removedAt >= AFTER_REMOVAL_TICKS && (fly.isRemoved() || maintenanceCycles >= 2)) {
            observeOutcome(now);
        } else if (now - releasedAt >= COMBAT_BUDGET) {
            gap("server_tick_budget");
        }
    }

    private void observeOutcome(long now) {
        boolean matchingJar = level.getBlockEntity(jar.getBlockPos()) == jar && jarId.equals(jar.jarData().getString("guguanuuid"));
        boolean slotUnchanged = ItemStack.matches(savedSlot, jar.getItem(0));
        double active = jar.jarData().getDouble("shifangnum");
        boolean releasing = jar.jarData().getBoolean("shifangguchong");
        String result;
        if (matchingJar && slotUnchanged && fly.getTarget() == null && fly.getRemovalReason() == Entity.RemovalReason.DISCARDED
                && active == 0 && !releasing) {
            result = "returned";
        } else if (matchingJar && slotUnchanged && fly.isAlive() && level.getEntity(fly.getUUID()) == fly
                && fly.getTarget() == prey && active == 1 && releasing) {
            result = "retained_removed_target";
        } else {
            gap("unexpected_post_combat_state");
            return;
        }
        report.add("INFO REPLAY_LOCUST_COMBAT_OUTCOME result=" + result + " gameTime=" + now
                + " ticksAfterTargetRemoval=" + (now - removedAt) + " maintenanceCycles=" + maintenanceCycles + " hits=" + hits
                + " " + state());
        done = true;
    }

    private void gap(String reason) {
        report.add("INFO REPLAY_LOCUST_COMBAT_GAP reason=" + reason + " uuidConsumed=" + acceptedTarget + " hits=" + hits
                + " killedByFly=" + killedByFly + " maintenanceCycles=" + maintenanceCycles + (fly == null ? "" : " " + state()));
        done = true;
    }

    private String state() {
        LivingEntity target = fly.getTarget();
        return "released=" + fly.getUUID() + " flyAge=" + fly.tickCount + " flyRemoval=" + fly.getRemovalReason()
                + " target=" + (target == null ? "none" : target.getUUID()) + " targetAlive=" + (target != null && target.isAlive())
                + " targetRemoved=" + (target != null && target.isRemoved()) + " jar=" + jarId
                + " sameJar=" + (level.getBlockEntity(jar.getBlockPos()) == jar && jarId.equals(jar.jarData().getString("guguanuuid")))
                + " active=" + jar.jarData().getDouble("shifangnum") + " releasing=" + jar.jarData().getBoolean("shifangguchong")
                + " slotUnchanged=" + ItemStack.matches(savedSlot, jar.getItem(0));
    }
}
