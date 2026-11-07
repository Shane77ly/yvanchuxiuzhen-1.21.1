package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.function.Consumer;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 只检查新增的弹体粒子广播，以及来源第 02 箭不消费施放标记的差异。 */
final class SkillParticleCheck extends SystemCheck {
    private final Consumer<String> shot;
    private SkillProjectile spirit;
    private SkillProjectile gold;
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
            });
            return true;
        }
        if (local < 8) {
            return false;
        }
        expectTrue("弹体的自定义粒子从服务端广播到真实客户端提供器", libraryParticles() > before);
        onServer(player -> {
            expectTrue("第02箭不套用增伤模式且保留玩家施放标记", spirit.getBaseDamage() == 8
                    && spirit.getPersistentData().getString("jinengName").isEmpty()
                    && Cultivation.tizhiBoolean(player, "arrowskill") && Cultivation.tizhiBoolean(player, "manxvli"));
        });
        shot.accept("skill-projectile-particles.png");
        onServer(player -> {
            spirit.discard();
            gold.discard();
        });
        finish();
        return true;
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
