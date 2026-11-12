package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;
import net.yvanchuxiuzhen.system.xiulian.tribulation.RollingThunder;
import net.yvanchuxiuzhen.system.xiulian.tribulation.ThunderDragon;
import net.yvanchuxiuzhen.system.xiulian.tribulation.ThunderPillar;
import net.yvanchuxiuzhen.system.xiulian.tribulation.TribulationCloud;
import net.yvanchuxiuzhen.system.xiulian.tribulation.TribulationEntity;
import net.yvanchuxiuzhen.system.xiulian.tribulation.Tribulations;

/** 完整雷劫的精简主路径：自然开始、实际落雷、序列成功、三类实体及劫云被杀失败。 */
final class TribulationCheck extends SystemCheck {
    private final Consumer<String> shot;
    private CultivationData original;
    private TribulationCloud cloud;
    private Pig target;
    private boolean griefing;

    TribulationCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("完整雷劫", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(player -> {
                original = player.getData(XiulianAttachments.CULTIVATION);
                player.setData(XiulianAttachments.CULTIVATION, new CultivationData());
                Arena.level(player);
                griefing = player.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                player.level().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false, player.server);
                player.setGameMode(GameType.SURVIVAL);
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 20);
                Cultivation.set(player, CultivationValue.EXPERIENCE_MAX, 1000);
                Cultivation.set(player, CultivationValue.EXPERIENCE, 1000);
                Cultivation.set(player, CultivationValue.THUNDER, 2);
                Cultivation.set(player, CultivationValue.MERITS, 100);
                Cultivation.setTizhiDouble(player, "ThunderSufferingAttack", -28);
            });
            return true;
        }
        if (step == 1) {
            if (local < 30) return false;
            onServer(player -> {
                cloud = cloud(player);
                expectTrue("满修为自然生成劫云，功德动态抵消后为十四组并清旧雷数", Cultivation.thunderOpen(player)
                        && Cultivation.get(player, CultivationValue.THUNDER_MAX) == 14
                        && Cultivation.get(player, CultivationValue.THUNDER) == 0
                        && Cultivation.get(player, CultivationValue.MERITS) == 50
                        && cloud.getPersistentData().getString("thunder14").startsWith("雷"));
                player.setGameMode(GameType.CREATIVE);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, -75);
            });
            return true;
        }
        if (step == 2) {
            if (local < 12) return false;
            shot.accept("tribulation-cloud.png");
            onServer(player -> {
                target = pig(player, player.position().add(0.5, 0, 0));
                for (int i = 2; i <= 14; i++) cloud.getPersistentData().remove("thunder" + i);
                cloud.getPersistentData().putString("thunder1", "雷1");
                cloud.getPersistentData().putDouble("number", 1);
                cloud.getPersistentData().putDouble("qianyao", 61);
                cloud.getPersistentData().putDouble("leipitick", 14);
                Cultivation.setTizhiDouble(player, "jishiqi", player.level().getDayTime());
            });
            return true;
        }
        if (step == 3) {
            if (local < 5) return false;
            onServer(player -> {
                expectTrue("真实云 tick 的落雷经战斗监听造成伤害", target.getHealth() < target.getMaxHealth());
                expectTrue("最后一组结束后云移除，设置进阶并关闭雷劫", cloud.isRemoved()
                        && (Cultivation.tizhiBoolean(player, "jinji") || Cultivation.get(player, CultivationValue.LEVEL_NUMBER) == 21)
                        && !Cultivation.thunderOpen(player)
                        && Cultivation.get(player, CultivationValue.THUNDER_MAX) == 0);
                Cultivation.setTizhiBoolean(player, "jinji", false);
                target.discard();
                target = pig(player, player.position().add(0.5, 0, 0));
                Cultivation.setThunderOpen(player, true);
                Tribulations.begin(player);
                cloud = cloud(player);
                cloud.getPersistentData().putString("thunder1", "雷3");
                cloud.getPersistentData().putDouble("number", 1);
                cloud.getPersistentData().putDouble("qianyao", 101);
                cloud.getPersistentData().putDouble("leipitick", 9);
                cloud.baseTick();
                RollingThunder rolling = player.serverLevel().getEntitiesOfClass(RollingThunder.class, player.getBoundingBox().inflate(30)).getFirst();
                rolling.setPos(target.position());
                rolling.getPersistentData().putBoolean("keyi", true);
                rolling.getPersistentData().putDouble("tick", 1);
                rolling.baseTick();
                float afterRolling = target.getHealth();
                target.invulnerableTime = 0;
                cloud.getPersistentData().putDouble("thundernum", 1);
                cloud.getPersistentData().putString("thunder1", "雷5");
                cloud.getPersistentData().putDouble("number", 0);
                cloud.getPersistentData().putDouble("qianyao", 201);
                cloud.baseTick();
                ThunderPillar pillar = player.serverLevel().getEntitiesOfClass(ThunderPillar.class, player.getBoundingBox().inflate(10)).getFirst();
                pillar.getPersistentData().putDouble("tick2", 319);
                pillar.baseTick();
                cloud.getPersistentData().putDouble("thundernum", 1);
                cloud.getPersistentData().putString("thunder1", "雷7");
                cloud.getPersistentData().putDouble("qianyao", 199);
                cloud.baseTick();
                ThunderDragon dragon = player.serverLevel().getEntitiesOfClass(ThunderDragon.class, player.getBoundingBox().inflate(40)).getFirst();
                dragon.setPos(target.position());
                dragon.getPersistentData().putString("jinengjinxing", "结束");
                dragon.baseTick();
                expectTrue("劫云生成三类天雷实体，地滚雷与雷龙伤害生效，雷柱按寿命消散", rolling.isRemoved()
                        && afterRolling < target.getMaxHealth() && target.getHealth() < afterRolling && pillar.isRemoved() && dragon.isRemoved());
                cloud.kill();
                expectTrue("劫云死亡关闭雷劫、清组数并累积一次失败", !Cultivation.thunderOpen(player)
                        && Cultivation.get(player, CultivationValue.THUNDER_MAX) == 0 && Cultivation.get(player, CultivationValue.THUNDER) == 1);
                target.discard();
                player.serverLevel().getEntitiesOfClass(TribulationEntity.class, player.getBoundingBox().inflate(150)).forEach(TribulationEntity::discard);
                player.setData(XiulianAttachments.CULTIVATION, original);
                player.level().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(griefing, player.server);
            });
            finish();
            return true;
        }
        throw new IllegalStateException("雷劫检查步骤 " + step);
    }

    private static TribulationCloud cloud(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(TribulationCloud.class, player.getBoundingBox().inflate(150)).getFirst();
    }

    private static Pig pig(ServerPlayer player, Vec3 at) {
        Pig pig = EntityType.PIG.create(player.serverLevel());
        pig.setNoAi(true);
        pig.setPos(at);
        pig.getRandom().setSeed(1);
        player.serverLevel().addFreshEntity(pig);
        return pig;
    }
}
