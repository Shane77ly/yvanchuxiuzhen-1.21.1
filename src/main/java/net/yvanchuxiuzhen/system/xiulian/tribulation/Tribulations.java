package net.yvanchuxiuzhen.system.xiulian.tribulation;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.XiulianSetup;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.TribulationDamageEvent;

/** 雷劫的内容装配与开始过程（来源 ThunderBegin）；修炼拥有状态，范围伤害通过具体事件交给战斗系统。 */
public final class Tribulations {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private Tribulations() { }

    public static Map<String, EntityModelRow> models() {
        Map<String, EntityModelRow> result = new LinkedHashMap<>();
        for (EntityModelRow row : ContentFiles.read("content/xiulian/entity_models.json", EntityModelRow.CODEC)) {
            result.put(row.id(), row);
        }
        return result;
    }

    public static void register(ContentTables tables) {
        Map<String, EntityModelRow> models = models();
        EntityModelRow cloud = models.get("jieyvn");
        tables.<TribulationCloud>livingEntity(cloud.id(), (type, level) -> new TribulationCloud(type, level, cloud),
                () -> cloud.attributes().add(Attributes.FLYING_SPEED, 0.3).add(NeoForgeMod.SWIM_SPEED, 0.3));
        EntityModelRow rolling = models.get("tianjie_03");
        tables.<RollingThunder>livingEntity(rolling.id(), (type, level) -> new RollingThunder(type, level, rolling), rolling::attributes);
        EntityModelRow pillar = models.get("tianjie_05");
        tables.<ThunderPillar>livingEntity(pillar.id(), (type, level) -> new ThunderPillar(type, level, pillar),
                () -> pillar.attributes().add(NeoForgeMod.SWIM_SPEED, 0.3));
        EntityModelRow dragon = models.get("tianjie_07");
        tables.<ThunderDragon>livingEntity(dragon.id(), (type, level) -> new ThunderDragon(type, level, dragon), dragon::attributes);
    }

    /** 在自然突破已打开雷劫标记后生成云；组数、功德消耗、攻击修正及随机序列保留来源顺序。 */
    public static void begin(ServerPlayer player) {
        double realm = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        double attack = 0;
        if (realm == 20) {
            Cultivation.set(player, CultivationValue.THUNDER_MAX, 13);
            attack = 30;
        } else if (realm == 30) {
            Cultivation.set(player, CultivationValue.THUNDER_MAX, 25);
            attack = 60;
        } else if (realm == 40) {
            Cultivation.set(player, CultivationValue.THUNDER_MAX, 37);
            attack = 90;
        } else if (realm >= 41) {
            Cultivation.set(player, CultivationValue.THUNDER_MAX, 37);
            attack = 120;
        }
        Cultivation.add(player, CultivationValue.THUNDER_MAX, Cultivation.tizhiDouble(player, "ThunderSufferingNumber"));
        attack = (attack + Cultivation.tizhiDouble(player, "ThunderSufferingAttack"))
                * (1 + Cultivation.tizhiDouble(player, "ThunderSufferingAttackPercentage"));
        double merits = Cultivation.get(player, CultivationValue.MERITS);
        if (merits <= -50) {
            Cultivation.add(player, CultivationValue.MERITS, Math.floor(Math.abs(merits) / 50) * 50);
        } else {
            // 来源循环上界每次读取正在减少的 thunder，不能改为固定次数。
            for (int i = 0; i < (int) Cultivation.get(player, CultivationValue.THUNDER); i++) {
                if (Cultivation.get(player, CultivationValue.MERITS) < 50) break;
                Cultivation.add(player, CultivationValue.MERITS, -50);
                Cultivation.add(player, CultivationValue.THUNDER, -1);
            }
        }
        Cultivation.add(player, CultivationValue.THUNDER_MAX, Cultivation.get(player, CultivationValue.THUNDER));
        ServerLevel level = player.serverLevel();
        TribulationCloud cloud = XiulianSetup.<TribulationCloud>entityType("jieyvn").get().create(level);
        cloud.moveTo(player.getX(), player.getY() + 64, player.getZ(), level.getRandom().nextFloat() * 360, 0);
        Relations.setTeam(cloud, "tianjiejieyvn");
        Relations.addFriendlyTeam(cloud, "tianjiejieyvn");
        cloud.getPersistentData().putString("playerUUID", player.getStringUUID());
        cloud.getPersistentData().putDouble("thundernum", 1);
        MobEffectInstance resistance = player.getEffect(BuiltInRegistries.MOB_EFFECT.getHolder(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "effect_022")).orElseThrow());
        cloud.getPersistentData().putDouble("thunder_Att", attack + Cultivation.get(player, CultivationValue.THUNDER_ATT)
                - (resistance == null ? 0 : resistance.getAmplifier() + 1));
        for (int i = 1; i <= (int) Cultivation.get(player, CultivationValue.THUNDER_MAX); i++) {
            cloud.getPersistentData().putString("thunder" + i, "雷" + (level.getRandom().nextInt(7) + 1));
        }
        Cultivation.set(player, CultivationValue.THUNDER, 0);
        cloud.finalizeSpawn(level, level.getCurrentDifficultyAt(cloud.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(cloud);
    }

    static ServerPlayer player(TribulationEntity self) {
        String id = self.getPersistentData().getString("playerUUID");
        return ((ServerLevel) self.level()).players().stream().filter(player -> player.getStringUUID().equals(id)).findFirst().orElse(null);
    }

    static void particles(Entity self, String id, Vec3 at, int count, double dx, double dy, double dz, double speed) {
        ((ServerLevel) self.level()).sendParticles(Particles.type(id), at.x, at.y, at.z, count, dx, dy, dz, speed);
    }

    /** 来源按原始坐标的 AABB 和距离顺序遍历，非生物仍可接收命中特效。 */
    static void damage(TribulationEntity self, double diameter, String multiplier, boolean hitParticles) {
        Vec3 center = self.position();
        ServerLevel level = (ServerLevel) self.level();
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(diameter / 2), entity -> true)
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
            if (Relations.isFriendly(self, target)) continue;
            if (hitParticles) {
                particles(self, "smallexplosionyellow", target.position().add(0, target.getBbHeight() / 2, 0), 1, 0, 0, 0, 0);
            }
            if (target instanceof LivingEntity living) {
                NeoForge.EVENT_BUS.post(new TribulationDamageEvent(level, living, "shanghai 雷 " + self.getStringUUID() + " false "
                        + WHOLE.format(self.getPersistentData().getDouble("thunder_Att")) + " " + multiplier));
            }
        }
    }
}
