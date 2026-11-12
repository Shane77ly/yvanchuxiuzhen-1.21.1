package net.yvanchuxiuzhen.smoke;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.yvanchuxiuzhen.system.particle.ParticleRow;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 仅验证新增延迟光爆在本体移除后仍送达，以及血雾轻功调用真实客户端提供器。 */
final class SkillVisualCheck extends SystemCheck {
    private final Consumer<String> shot;
    private final Set<Particle> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    private SkillEntity flash;
    private SkillState original;
    private GameType gameMode;
    private double power;
    private ParticleStatus particleStatus;
    private int bigFlashes;
    private int smallFlashes;
    private int bloodMist;
    private boolean captured;

    SkillVisualCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("技能其余粒子", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        observeParticles();
        if (step == 0) {
            particleStatus = minecraft.options.particles().get();
            minecraft.options.particles().set(ParticleStatus.ALL);
            onServer(player -> {
                Arena.level(player);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, 0);
                flash = SkillEntities.summon(player.serverLevel(), player, "skill_light_03", "光", 1, 1, "万丈光芒");
                flash.setPos(player.position().add(0, 0, 4));
                SkillEntities.spawn(player.serverLevel(), flash, false);
            });
            return true;
        }
        if (step == 1) {
            if (!captured && local >= 7) {
                shot.accept("skill-light-particles.png");
                captured = true;
            }
            if (local < 16) return false;
            onServer(player -> expectTrue("万丈光芒移除后仍有后续光爆送达", flash.isRemoved() && bigFlashes > 2 && smallFlashes > 4));
            onServer(player -> {
                original = Skills.state(player);
                gameMode = player.gameMode.getGameModeForPlayer();
                power = Cultivation.get(player, CultivationValue.POWER);
                ItemStack book = Skills.createBook(SkillKind.FLIGHT, List.of("yvanchuxiuzhen:book_012", "血雾", "1", "1", "无", "轻功。", "4", "1.5", "血雾"));
                book.set(SkillData.SKILL.get(), Skills.book(book).orElseThrow().withOwner(player.getUUID(), player.getName().getString()));
                Skills.setFlight(player, book);
                player.setGameMode(GameType.SURVIVAL);
                Cultivation.set(player, CultivationValue.POWER, 200);
                Skills.toggleFlight(player);
            });
            return true;
        }
        if (local < 8) return false;
        expectTrue("血雾轻功的起飞与持续粒子实际送达客户端", bloodMist > 30);
        shot.accept("skill-flight-particles.png");
        onServer(player -> {
            Skills.toggleFlight(player);
            player.setData(SkillData.STATE, original);
            player.setGameMode(gameMode);
            Cultivation.set(player, CultivationValue.POWER, power);
        });
        minecraft.options.particles().set(particleStatus);
        finish();
        return true;
    }

    private void observeParticles() {
        minecraft.particleEngine.iterateParticles(particle -> {
            if (!particle.getClass().getName().equals("net.yvanchuxiuzhen.system.particle.client.LibraryParticle") || !seen.add(particle)) return;
            try {
                Field field = particle.getClass().getDeclaredField("row");
                field.setAccessible(true);
                String id = ((ParticleRow) field.get(particle)).id();
                if (id.equals("bigbanglightyellow")) bigFlashes++;
                if (id.equals("smallexplosionlightyellow")) smallFlashes++;
                if (id.equals("flyskill_06")) bloodMist++;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        });
    }
}
