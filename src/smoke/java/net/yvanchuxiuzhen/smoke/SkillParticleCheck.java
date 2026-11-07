package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 弹体粒子广播、第02箭施放标记，以及共用弹体保存与命中箭数的针对性检查。 */
final class SkillParticleCheck extends SystemCheck {
    private final Consumer<String> shot;
    private SkillProjectile spirit;
    private SkillProjectile gold;
    private SkillProjectile fire;
    private IronGolem target;
    private long launchedAt;
    private int before;

    SkillParticleCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("技能弹体粒子", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            before = libraryParticles();
            onServer(player -> {
                Arena.level(player);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, 0);
                Cultivation.setTizhiBoolean(player, "arrowskill", true);
                Cultivation.setTizhiBoolean(player, "manxvli", true);
                SkillProjectile arrow = SkillSetup.<SkillProjectile>entityType("projectile_skill_arrow_02").get().create(player.serverLevel());
                arrow.setOwner(player);
                arrow.setBaseDamage(8);
                arrow.getPersistentData().putBoolean("arrowAlt", true);
                arrow.setPos(player.position().add(1.5, 2, 5));
                player.serverLevel().addFreshEntity(arrow);
                spirit = arrow;
                SkillProjectile bolt = SkillSetup.<SkillProjectile>entityType("projectile_skill_gold_01").get().create(player.serverLevel());
                bolt.setOwner(player);
                bolt.setPos(player.position().add(-1.5, 2, 5));
                player.serverLevel().addFreshEntity(bolt);
                gold = bolt;
                persistence(player);
            });
            return true;
        }
        if (local < 8) {
            return false;
        }
        long[] serverTime = {0};
        onServer(player -> serverTime[0] = player.serverLevel().getGameTime());
        if (serverTime[0] - launchedAt < 8) {
            return false;
        }
        expectTrue("弹体的自定义粒子从服务端广播到真实客户端提供器", libraryParticles() > before);
        onServer(player -> {
            expectTrue("第02箭不套用增伤模式且保留玩家施放标记", spirit.getBaseDamage() == 8
                    && spirit.getPersistentData().getString("jinengName").isEmpty()
                    && Cultivation.tizhiBoolean(player, "arrowskill") && Cultivation.tizhiBoolean(player, "manxvli"));
            expectTrue("共用火球弹体真实命中后不留下生物身上的箭", target.getHealth() < 100 && target.getArrowCount() == 0);
        });
        shot.accept("skill-projectile-particles.png");
        onServer(player -> {
            spirit.discard();
            gold.discard();
            fire.discard();
            target.discard();
        });
        finish();
        return true;
    }

    private void persistence(ServerPlayer player) {
        target = EntityType.IRON_GOLEM.create(player.serverLevel());
        target.setNoAi(true);
        target.setPos(player.position().add(8, 0, 0));
        target.getPersistentData().putDouble("LEVELnumber", 17);
        player.serverLevel().addFreshEntity(target);

        SkillProjectile flower = SkillSetup.<SkillProjectile>entityType("projectile_skill_ice_03").get().create(player.serverLevel());
        flower.setOwner(player);
        flower.setPos(player.position().add(0, 10, 0));
        flower.launchedWith(3, 2);
        flower.begin();
        flower.snapshot(target, 1, 1, 1);
        flower.setSkillName("飞羽冰花");
        flower.setAngle(45);
        flower.setReversed(true);
        flower.markSaved();
        for (int i = 0; i < 3; i++) flower.advance();
        CompoundTag saved = flower.saveWithoutId(new CompoundTag());
        SkillProjectile restored = (SkillProjectile) flower.getType().create(player.serverLevel());
        restored.load(saved);
        expectTrue("弹体保存重读保留施放数值、计时、轨迹、初始化标记和原生穿透",
                restored.mana() == 17 && restored.multiplier() == 1 && restored.skillName().equals("飞羽冰花")
                        && restored.ticks() == 3 && restored.angle() == 45 && restored.reversed() && restored.saved()
                        && restored.getPierceLevel() == 2 && restored.isNoGravity());
        restored.tick();
        expectTrue("重读后飞羽冰花接续第三刻和左旋轨迹，保留伤害快照",
                restored.ticks() == 5 && restored.angle() == 25 && restored.mana() == 17 && restored.multiplier() == 1);

        fire = SkillSetup.<SkillProjectile>entityType("projectile_skill_fire_01").get().create(player.serverLevel());
        fire.setOwner(player);
        fire.setPos(player.position().add(3, 1, 0));
        fire.setBaseDamage(1);
        fire.begin();
        fire.snapshot(target, 1, 1, 1);
        fire.setSkillName("火球术");
        fire.shoot(1, 0, 0, 1, 0);
        player.serverLevel().addFreshEntity(fire);
        launchedAt = player.serverLevel().getGameTime();
    }

    private int libraryParticles() {
        int[] count = {0};
        minecraft.particleEngine.iterateParticles(particle -> {
            if (particle.getClass().getName().equals("net.yvanchuxiuzhen.system.particle.client.LibraryParticle")) {
                count[0]++;
            }
        });
        return count[0];
    }
}
