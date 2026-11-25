package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss0201Entity;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss02Entity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 单飞斧原生飞行与爆炸分别核对敌友、伤害请求及实际扣血。 */
final class Boss02AxeHitCheck extends SystemCheck {
    private static final String OWNER_TEAM = "smoke_axe_owner";
    private static final String ALLY_TEAM = "smoke_axe_ally";
    private static final float HEALTH = 40;
    private final Consumer<EntityJoinLevelEvent> joinListener = this::onJoin;
    private final Consumer<EntityTickEvent.Pre> tickListener = this::beforeTick;
    private final Consumer<LivingIncomingDamageEvent> incoming = this::onIncoming;
    private final Consumer<LivingDamageEvent.Post> settled = this::onSettled;
    private final List<Sample> samples = new ArrayList<>();
    private final List<Boss0201Entity> axes = new ArrayList<>();
    private ServerLevel level;
    private Difficulty difficulty;
    private Boss02Entity boss;
    private Pig target;
    private boolean listening;

    Boss02AxeHitCheck(List<String> report, String levelId) {
        super("首领02飞斧敌友伤害", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            if (step == 0) {
                onServer(this::prepare);
                return true;
            }
            boolean[] done = {false};
            onServer(player -> {
                if (axes.isEmpty() || !axes.getFirst().isRemoved()) return;
                checkHits();
                cleanup(player);
                done[0] = true;
            });
            if (done[0]) finish();
            else if (local > 200) throw new IllegalStateException("首领飞斧未在规定时间完成飞行");
            return done[0];
        } catch (RuntimeException error) {
            fail(error.getMessage());
            onServer(this::cleanup);
            finish();
            return false;
        }
    }

    private void prepare(ServerPlayer player) {
        level = player.serverLevel();
        difficulty = level.getDifficulty();
        player.server.setDifficulty(Difficulty.NORMAL, true);
        Vec3 origin = player.position().add(3, 12, 0);
        boss = CreatureSetup.<Boss02Entity>entityType("ycxzboss_02").get().create(level);
        if (boss == null) throw new IllegalStateException("无法创建02号首领");
        anchor(boss, origin);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        Relations.setTeam(boss, OWNER_TEAM);
        Relations.addFriendlyTeam(boss, OWNER_TEAM);
        Relations.addFriendlyTeam(boss, ALLY_TEAM);
        target = pig(origin.add(7, 0, 0), ALLY_TEAM);
        for (int tick : List.of(1, 20)) {
            for (boolean friend : List.of(false, true)) {
                Pig pig = pig(origin.add(4, 0, friend ? 7 : -7), friend ? ALLY_TEAM : "smoke_axe_enemy");
                samples.add(new Sample(pig, tick, friend));
            }
        }
        EntityAttributes.setStat(boss, Stat.ATTACK, 10);
        CreatureData.state(boss).setSkill("飞斧");
        NeoForge.EVENT_BUS.addListener(joinListener);
        NeoForge.EVENT_BUS.addListener(tickListener);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, incoming);
        NeoForge.EVENT_BUS.addListener(settled);
        listening = true;
        if (!level.addFreshEntity(boss)) throw new IllegalStateException("02号首领未入场");
        boss.setTarget(target);
    }

    private Pig pig(Vec3 position, String team) {
        Pig pig = EntityType.PIG.create(level);
        if (pig == null) throw new IllegalStateException("无法创建飞斧伤害样本");
        anchor(pig, position);
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(HEALTH);
        pig.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        pig.setHealth(HEALTH);
        Relations.setTeam(pig, team);
        if (!level.addFreshEntity(pig)) throw new IllegalStateException("飞斧伤害样本未入场");
        return pig;
    }

    private static void anchor(Mob mob, Vec3 position) {
        mob.moveTo(position);
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.setPersistenceRequired();
    }

    private void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() == level && event.getEntity() instanceof Boss0201Entity axe
                && axe.attackUuid().equals(boss.getStringUUID())) axes.add(axe);
    }

    private void beforeTick(EntityTickEvent.Pre event) {
        if (axes.isEmpty() || event.getEntity() != axes.getFirst()) return;
        Boss0201Entity axe = axes.getFirst();
        int tick = (int) axe.skillTick() + 1;
        Vec3 center = axe.position();
        for (Sample sample : samples) {
            if (tick == sample.tick) {
                double offset = sample.tick == 1 ? .65 : 1.65;
                sample.pig.moveTo(center.add(0, 0, sample.friend ? offset : -offset));
                AABB bounds = sample.pig.getBoundingBox();
                sample.placed = bounds.intersects(new AABB(center, center).inflate(2))
                        && bounds.intersects(new AABB(center, center).inflate(1)) == (sample.tick == 1);
                // 友方仅由飞斧单向认定，避免 Combat 的反向友方免伤掩盖外层门槛。
                sample.relation = Relations.isFriendly(axe, sample.pig) == sample.friend
                        && !Relations.isFriendly(sample.pig, boss);
            } else if (tick == sample.tick + 1) {
                sample.pig.moveTo(boss.position().add(0, 0, sample.friend ? 7 : -7));
            }
        }
    }

    private void onIncoming(LivingIncomingDamageEvent event) {
        for (Sample sample : samples) {
            if (event.getEntity() == sample.pig) sample.incoming.add(hit(event.getSource(), event.getOriginalAmount()));
        }
    }

    private void onSettled(LivingDamageEvent.Post event) {
        for (Sample sample : samples) {
            if (event.getEntity() == sample.pig) sample.settled.add(hit(event.getSource(), event.getNewDamage()));
        }
    }

    private Hit hit(DamageSource source, float amount) {
        int elapsed = axes.isEmpty() ? -1 : (int) axes.getFirst().skillTick();
        return new Hit(elapsed, amount, source.is(DamageTypes.PLAYER_ATTACK)
                && source.getEntity() == boss && source.getDirectEntity() == boss);
    }

    private void checkHits() {
        expect("受测飞斧数量", 1, axes.size());
        for (Sample sample : samples) {
            String label = (sample.tick == 1 ? "飞行一格" : "第20刻独有爆炸范围") + (sample.friend ? "友方" : "敌方");
            expectTrue(label + "样本范围及单向关系成立", sample.placed && sample.relation);
            int eventTick = sample.tick == 1 ? 0 : 20;
            List<Hit> expected = sample.friend ? List.of() : List.of(new Hit(eventTick, 12, true));
            expect(label + "原始伤害回调", expected, sample.incoming);
            expect(label + "实际伤害回调", expected, sample.settled);
            expect(label + "实际生命", sample.friend ? HEALTH : HEALTH - 12, sample.pig.getHealth());
        }
    }

    private void cleanup(ServerPlayer player) {
        if (listening) {
            NeoForge.EVENT_BUS.unregister(joinListener);
            NeoForge.EVENT_BUS.unregister(tickListener);
            NeoForge.EVENT_BUS.unregister(incoming);
            NeoForge.EVENT_BUS.unregister(settled);
            listening = false;
        }
        axes.forEach(Entity::discard);
        samples.forEach(sample -> sample.pig.discard());
        if (target != null) target.discard();
        if (boss != null) boss.discard();
        if (difficulty != null) player.server.setDifficulty(difficulty, true);
    }

    private static final class Sample {
        private final Pig pig;
        private final int tick;
        private final boolean friend;
        private final List<Hit> incoming = new ArrayList<>();
        private final List<Hit> settled = new ArrayList<>();
        private boolean placed;
        private boolean relation;

        private Sample(Pig pig, int tick, boolean friend) {
            this.pig = pig;
            this.tick = tick;
            this.friend = friend;
        }
    }

    private record Hit(int elapsed, float amount, boolean ownerPlayerAttack) { }
}
