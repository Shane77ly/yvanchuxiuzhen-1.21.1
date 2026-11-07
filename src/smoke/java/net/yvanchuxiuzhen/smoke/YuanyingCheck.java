package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.entity.Yuanying;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 元婴出窍的门槛、神识消耗、实体、受伤同步及旁观状态下经真实按钮归窍。 */
final class YuanyingCheck extends ActionCheck {
    private ServerPlayer player;
    private double originalRank;
    private double originalCorrection;
    private double originalSoul;
    private double originalTimer;
    private double soulAfterCast;
    private boolean originalOut;
    private GameType originalMode;
    private float originalHealth;
    private double originalX;
    private double originalY;
    private double originalZ;

    YuanyingCheck(List<String> report, String levelId) {
        super("元婴出窍", report, levelId);
        script(
                new Action(8, p -> {
                    player = p;
                    originalRank = Cultivation.get(p, CultivationValue.LEVEL_NUMBER);
                    originalCorrection = Cultivation.tizhiDouble(p, "LevelNumberCorrect");
                    originalSoul = Cultivation.get(p, CultivationValue.SOUL);
                    originalTimer = Cultivation.tizhiDouble(p, "jishiqi");
                    originalOut = Cultivation.tizhiBoolean(p, "yvanyingchuqiao");
                    originalMode = p.gameMode.getGameModeForPlayer();
                    originalHealth = p.getHealth();
                    originalX = p.getX();
                    originalY = p.getY();
                    originalZ = p.getZ();
                    p.setGameMode(GameType.SURVIVAL);
                    Arena.level(p);
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 30);
                    Cultivation.setTizhiDouble(p, "LevelNumberCorrect", 0);
                    Cultivation.set(p, CultivationValue.SOUL, 10000);
                    Cultivation.setTizhiBoolean(p, "yvanyingchuqiao", false);
                    Skills.toggleYuanying(p);
                    expect("境界不足时不能出窍", 0, bodies(p).size());
                }),
                new Action(24, p -> {
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 31);
                    Cultivation.set(p, CultivationValue.SOUL, 10000);
                    Cultivation.setTizhiDouble(p, "jishiqi", p.level().getDayTime());
                    double before = Cultivation.get(p, CultivationValue.SOUL);
                    Skills.toggleYuanying(p);
                    soulAfterCast = Cultivation.get(p, CultivationValue.SOUL);
                    expectTrue("达到元婴期后扣神识并生成本体元婴",
                            Cultivation.tizhiBoolean(p, "yvanyingchuqiao") && p.gameMode.getGameModeForPlayer() == GameType.SPECTATOR
                                    && bodies(p).size() == 1 && Cultivation.get(p, CultivationValue.SOUL) < before - 61);
                }),
                new Action(8, p -> {
                    Yuanying body = bodies(p).get(0);
                    float bodyHealth = body.getHealth();
                    body.hurt(p.damageSources().generic(), 1);
                    expectTrue("出窍移动持续消耗神识，元婴受伤同步本体生命",
                            Cultivation.get(p, CultivationValue.SOUL) < soulAfterCast && p.getHealth() == bodyHealth && body.getHealth() < bodyHealth);
                    Cultivation.set(p, CultivationValue.SOUL, 10000);
                    Skills.openMenu(p);
                }),
                new Action(8, p -> minecraft.execute(() -> minecraft.screen.children().stream()
                        .filter(child -> child instanceof Button button && button.getMessage().getString().equals("元婴出窍"))
                        .map(child -> (Button) child).findFirst().orElseThrow().onPress())),
                new Action(8, p -> {
                    expectTrue("旁观状态点击归窍按钮后恢复原位置与生存模式",
                            !Cultivation.tizhiBoolean(p, "yvanyingchuqiao") && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                                    && bodies(p).isEmpty() && p.position().distanceToSqr(originalX, originalY, originalZ) < 0.01);
                }),
                new Action(8, p -> {
                    Cultivation.set(p, CultivationValue.SOUL, 0);
                    Skills.toggleYuanying(p);
                    expectTrue("神识不足时不能再次出窍", !Cultivation.tizhiBoolean(p, "yvanyingchuqiao")
                            && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL);
                })
        );
    }

    private static List<Yuanying> bodies(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Yuanying.class, new AABB(player.blockPosition()).inflate(3)).stream()
                .filter(body -> player.getUUID().equals(body.ownerId())).toList();
    }

    @Override
    protected void cleanup(ServerPlayer p) {
        if (Cultivation.tizhiBoolean(p, "yvanyingchuqiao")) {
            Skills.toggleYuanying(p);
        }
        for (Yuanying body : bodies(p)) {
            body.discard();
        }
        p.setGameMode(originalMode);
        p.connection.teleport(originalX, originalY, originalZ, p.getYRot(), p.getXRot());
        p.setHealth(originalHealth);
        Cultivation.set(p, CultivationValue.LEVEL_NUMBER, originalRank);
        Cultivation.setTizhiDouble(p, "LevelNumberCorrect", originalCorrection);
        Cultivation.set(p, CultivationValue.SOUL, originalSoul);
        Cultivation.setTizhiDouble(p, "jishiqi", originalTimer);
        Cultivation.setTizhiBoolean(p, "yvanyingchuqiao", originalOut);
    }
}
