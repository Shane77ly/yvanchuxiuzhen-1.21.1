package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.world.terrain.LegacyDimensionChunkGenerator;
import net.yvanchuxiuzhen.system.xiulian.XiulianRules;

/** 从自然摄网的落盘库存开始，核对地上蛊罐存读和原生计划刻。 */
final class RenjianCapturedJarCheck extends SystemCheck {
    static final String SELECTOR = "renjianlocustjar";
    static final String SAVE = "renjian-locust-jar-20261119";
    private static final UUID PLAYER = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");
    private static final UUID CAPTURED = UUID.fromString("86eabaf0-87b1-4d97-a2cd-d48ee6095b35");
    private static final ResourceLocation RENJIAN = id("dimensionrenjian");
    private static final ResourceLocation FLY_ITEM = id("guchong_03item");
    private final List<String> report;
    private final Screenshots screenshots;
    private final RenjianWildJarWitness jar;
    private final RenjianCapturedJarDriver driver;
    private final RenjianControlledJarScene target;
    private boolean listening;
    private boolean targetPlaced;
    private int releaseSeenAt = -1;

    RenjianCapturedJarCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("人间道自然摄网飞蝗装罐", report, levelId);
        this.report = report;
        this.screenshots = screenshots;
        jar = new RenjianWildJarWitness(report);
        driver = new RenjianCapturedJarDriver(report, screenshots);
        target = new RenjianControlledJarScene(report);
    }

    @Override protected boolean step(int step, int local) {
        if (minecraft.mouseHandler.isMouseGrabbed()) minecraft.mouseHandler.releaseMouse();
        return switch (step) {
            case 0 -> { onServer(this::start); yield true; }
            case 1 -> beforeSave(local);
            case 2 -> {
                RenjianReplayPlayer.look(minecraft, Vec3.atCenterOf(jar.position()));
                if (local < 8) yield false;
                screenshots.take("renjian-locust-jar-02-owned.png");
                leaveWorld();
                yield true;
            }
            case 3 -> reopened(local);
            case 4 -> {
                boolean ready = inWorld(local);
                if (!ready && local > 1200) stop();
                yield ready;
            }
            case 5 -> {
                onServer(player -> {
                    if (!player.serverLevel().dimension().location().equals(RENJIAN)
                            || !PLAYER.equals(player.getUUID()) || flies(player) != 0 || !jar.rejoined(player)) {
                        abort("同档重开后未读到原位罐、原认主组件和待执行计划刻");
                    }
                });
                yield true;
            }
            case 6 -> { afterSave(local); yield false; }
            default -> throw new IllegalStateException("未知自然飞蝗装罐步骤 " + step);
        };
    }

    private void start(ServerPlayer player) {
        var level = player.serverLevel();
        var rules = level.getGameRules();
        var players = player.server.getPlayerList();
        var inventory = player.getInventory();
        ItemStack insect = inventory.getItem(5);
        var data = GuchongJarData.read(insect);
        ItemStack handJar = inventory.getItem(1);
        ItemStack blood = inventory.getItem(2);
        if (!PLAYER.equals(player.getUUID()) || !level.dimension().location().equals(RENJIAN)
                || level.getSeed() != -5586163949850800998L
                || !(level.getChunkSource().getGenerator() instanceof LegacyDimensionChunkGenerator generator)
                || generator.terrainSeed() != -3958683526324992456L
                || level.getDifficulty() != Difficulty.NORMAL || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL
                || players.getViewDistance() != 2 || players.getSimulationDistance() != 5
                || !rules.getBoolean(GameRules.RULE_DOMOBSPAWNING) || !rules.getBoolean(GameRules.RULE_DOMOBLOOT)
                || !rules.getBoolean(GameRules.RULE_DAYLIGHT) || rules.getBoolean(XiulianRules.BIRTH_SELECTION)
                || !item(insect).equals(FLY_ITEM) || insect.getCount() != 1 || flies(player) != 1
                || !data.getString("guchongid").equals("yvanchuxiuzhen:guchong_03") || data.getBoolean("guchongxvnfu")
                || data.getDouble("LEVELnumber") != 1 || data.getDouble("health_Num") != 121
                || data.getDouble("attack_Num") != 3 || data.getDouble("speed_number") != 0.20500000000000002
                || !item(handJar).equals(id("taoguan_01item")) || handJar.getCount() != 1
                || !GuchongJarData.read(handJar).getString("guchongid0").isEmpty()
                || !item(blood).equals(id("jingxve")) || blood.getCount() != 2
                || GuchongJarData.read(blood).getInt("playerlevelnum") < data.getInt("LEVELnumber")) {
            abort("不满足自然摄网副本前置：固定人间道生存玩家、种子、2/5 视距、原槽5飞蝗、空罐及精血");
            return;
        }
        jar.capture(CAPTURED, insect);
        jar.start();
        listening = true;
        report.add("INFO REPLAY_CAPTURED_STACK originSave=renjian-locust-natural-frozen-20261119 wild=" + CAPTURED
                + " slot=5 count=1 tool=original-net noReplacement=true components=" + data);
    }

    private boolean beforeSave(int local) {
        boolean[] ready = {false};
        onServer(player -> {
            if (!player.isAlive() || local > 2400) {
                abort("装罐/放置/认主超时或死亡：" + jar.stage());
                return;
            }
            jar.observe(player, flies(player));
            if (jar.fed()) {
                ready[0] = jar.prepareSave();
                if (!ready[0]) abort("认主后未能在未释放且待计划刻的状态保存原罐");
            } else driver.beforeSave(player, jar, local);
        });
        if (listening && !ready[0]) driver.input(minecraft, local);
        return ready[0];
    }

    private void afterSave(int local) {
        onServer(player -> {
            if (!player.isAlive() || local > 3600) {
                abort("重开后未收到放虫见证：受控目标=" + targetPlaced + "；" + jar.stage());
                return;
            }
            jar.observe(player, flies(player));
            if (!targetPlaced && jar.releaseAreaEmpty()) targetPlaced = target.provideTarget(player, jar);
        });
        if (!listening || !targetPlaced || !jar.complete()) return;
        if (releaseSeenAt < 0) releaseSeenAt = local;
        Entity fly = RenjianReplayPlayer.mirror(minecraft, jar.releasedTarget());
        if (fly != null && minecraft.screen == null) {
            RenjianReplayPlayer.look(minecraft, fly.getBoundingBox().getCenter().lerp(Vec3.atCenterOf(jar.position()), 0.35));
            if (local - releaseSeenAt >= 6) {
                screenshots.take("renjian-locust-jar-03-released.png");
                report.add("INFO REPLAY_CAPTURED_JAR_COMPLETE wild=" + CAPTURED + " controlledTarget=true");
                stop();
                finish();
                return;
            }
        }
        if (local - releaseSeenAt > 100) abort("服务端已放虫，未取得同一释放飞蝗的客户端镜像截图");
    }

    private void abort(String message) {
        fail(message);
        stop();
        finish();
    }

    private void stop() {
        if (listening) {
            jar.stop();
            listening = false;
        }
    }

    private static int flies(ServerPlayer player) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (item(stack).equals(FLY_ITEM)) count += stack.getCount();
        }
        return count;
    }

    private static ResourceLocation item(ItemStack stack) { return BuiltInRegistries.ITEM.getKey(stack.getItem()); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
}
