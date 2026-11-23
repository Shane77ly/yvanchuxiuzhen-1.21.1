package net.yvanchuxiuzhen.smoke;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelSummary;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import net.yvanchuxiuzhen.system.xiulian.XiulianRules;
import net.yvanchuxiuzhen.system.world.diagnostic.WorldStopRequeueDiagnostics;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;

/**
 * 客户端冒烟检查：建一个创造模式世界，在高空平台放置本 Mod 的全部方块状态，核对每个方块与物品有模型和名称，
 * 加载全部结构模板（旧数据版本在加载时升级）并摆放方块实体最多的几个，记录调色板中的方块名；
 * 另对花、双格植物与每套盔甲的穿戴效果各拍一张近景，再打开创造模式物品栏并逐个切换创造栏并截图；
 * 最后依次做各系统的实机检查（{@link RelationCheck}、{@link AttributeCheck}、{@link XiulianCheck}，各含死亡重生与退出重进），写出报告并退出。
 * 系统属性 yvanchuxiuzhen.smoke.only 为逗号分隔的系统标识（如 relation,attribute,xiulian）时，进入世界后只做这些系统的检查。
 * 场景结果先写入报告，集成服务端正常断开后才写出 DONE 行；JVM 的最终退出仍需核对构建结果。
 */
final class SmokeRun {
    private static final String MOD = "yvanchuxiuzhen";
    /** 固定首入 run-01 成功离档重进的 level.dat 种子；首技夹具仍用原点附近的既有平台。 */
    private static final long FIRST_SPELL_SEED = 228722870480601105L;
    private static final long FIRST_SPELL_EXIT_TIMEOUT = TimeUnit.SECONDS.toNanos(60);
    /** 俯视位置：x 坐标与方块行号。 */
    private static final int[][] VIEWS = {{14, 0}, {50, 0}, {100, 0}, {30, 40}, {70, 80}, {30, 120}, {90, 160}};
    private final Path output;
    private final List<String> report = new ArrayList<>();
    private final List<Closeup> closeups = new ArrayList<>();
    private final Minecraft minecraft = Minecraft.getInstance();
    private final boolean guardedFirstSpell = "renjianfirstspell".equals(System.getProperty("yvanchuxiuzhen.smoke.only"));
    private final boolean renjianCombatLifecycle = "renjiancombatlifecycle".equals(System.getProperty("yvanchuxiuzhen.smoke.only"));
    private final boolean renjianBirthSupply = "renjianbirthsupply".equals(System.getProperty("yvanchuxiuzhen.smoke.only"));
    private final boolean renjianResidence = RenjianResidenceCheck.SELECTOR.equals(System.getProperty("yvanchuxiuzhen.smoke.only"));
    private final boolean q235SpellShield = SpellShieldCheck.SELECTOR.equals(System.getProperty("yvanchuxiuzhen.smoke.only"));
    private final boolean renjianRewardSpell = RenjianRewardSpellCheck.SELECTOR.equals(System.getProperty("yvanchuxiuzhen.smoke.only"));
    private final boolean renjianRewardLeafImpact = RenjianRewardLeafImpactCheck.SELECTOR.equals(System.getProperty("yvanchuxiuzhen.smoke.only"));
    private final boolean rewardReplay = renjianRewardSpell || renjianRewardLeafImpact;
    private final boolean guardedWorldExit = guardedFirstSpell || renjianCombatLifecycle || renjianBirthSupply || renjianResidence
            || rewardReplay || q235SpellShield;
    private final String replayLevelId = switch (System.getProperty("yvanchuxiuzhen.smoke.only", "")) {
        case "renjianvisual" -> "renjian-visual";
        case "silksurvival", "silksurvivalreentry" -> SilkSurvivalReentryCheck.SAVE;
        case NaturalMetalChainCheck.SELECTOR -> NaturalMetalChainCheck.SAVE;
        case NaturalAlloyRepairCheck.SELECTOR -> NaturalAlloyRepairCheck.SAVE;
        case JadeWoodSupplyCheck.SELECTOR -> JadeWoodSupplyCheck.SAVE;
        case JadeBambooSupplyCheck.SELECTOR -> JadeBambooSupplyCheck.SAVE;
        case "q119" -> QiankunDialogueCheck.SAVE_ID;
        case RenjianLocustNaturalCheck.SELECTOR -> RenjianLocustNaturalCheck.SAVE;
        case RenjianCapturedJarCheck.SELECTOR -> RenjianCapturedJarCheck.SAVE;
        case RenjianResidenceCheck.SELECTOR -> RenjianResidenceCheck.SAVE_ID;
        case RenjianRewardSpellCheck.SELECTOR -> RenjianRewardSpellCheck.SAVE_ID;
        case RenjianRewardLeafImpactCheck.SELECTOR -> RenjianRewardLeafImpactCheck.SAVE_ID;
        default -> "";
    };
    private int ticks;
    private int phase;
    private int phaseStart;
    private int tabIndex;
    private List<CreativeModeTab> tabs;
    private Method selectTab;
    private String pendingShot;
    private String levelId;
    private List<SystemCheck> systemChecks;
    private boolean stopDiagnosticsArmed;
    private boolean finishRequested;
    private int settledExitTicks;
    private boolean firstSpellExitBlocked;
    private boolean firstSpellCompletingExit;
    private long firstSpellExitStarted;
    private int firstSpellCleanSamples;
    private int firstSpellLastServerTick = -1;
    private ExitSample firstSpellLastSample;
    private CompletableFuture<ExitSample> firstSpellPendingSample;

    private record ExitSample(int serverTick, int pending, WorldStopRequeueDiagnostics.Readiness readiness) {
        boolean settled() {
            return pending == 0 && readiness.active() && readiness.settled();
        }
    }

    /** 近景截图：相机所在玩家的位置与朝向，以及在服务端线程上的布置动作。 */
    private record Closeup(String name, CameraType camera, double x, double y, double z, float yRot, float xRot,
                           Consumer<ServerPlayer> setup) {
    }

    SmokeRun(String output) {
        this.output = Path.of(output);
    }

    void tick() {
        if (firstSpellExitBlocked || firstSpellCompletingExit) return;
        ticks++;
        try {
            if (finishRequested) {
                waitForSettledExit();
                return;
            }
            switch (phase) {
                case 0 -> waitForTitle();
                case 10 -> selectResidenceWorld();
                case 1 -> waitForWorld();
                case 2 -> placeBlocks();
                case 3 -> worldShots();
                case 4 -> swingShots();
                case 5 -> waitThen(2, this::checkModelsAndNames);
                case 6 -> waitThen(2, this::openCreative);
                case 7 -> cycleTabs();
                case 8 -> checkSystems();
                case 9 -> {
                    if (ticks - phaseStart >= 20) {
                        finish();
                    }
                }
                default -> {
                }
            }
        } catch (Exception exception) {
            if (renjianResidence) {
                SmokeMod.residenceDigging = false;
                minecraft.options.keyUp.setDown(false);
                minecraft.options.keyJump.setDown(false);
                minecraft.options.keyShift.setDown(false);
            }
            report.add("FAIL harness " + exception);
            if (renjianResidence && minecraft.getSingleplayerServer() == null && !finishRequested) {
                finish();
            } else if (guardedWorldExit) {
                blockFirstSpellExit("夹具异常：" + exception);
            } else if (finishRequested) {
                completeExit();
            } else {
                finish();
            }
        }
    }

    private void next() {
        phase++;
        phaseStart = ticks;
    }

    private void waitThen(int delay, Runnable action) {
        if (ticks - phaseStart >= delay) {
            action.run();
            next();
        }
    }

    private void waitForTitle() throws IOException {
        if (minecraft.getOverlay() == null && minecraft.screen instanceof AccessibilityOnboardingScreen onboarding) {
            report.add("INFO 首次启动完成原生辅助功能引导，继续进入标题页");
            onboarding.onClose();
            return;
        }
        if (minecraft.screen instanceof TitleScreen && minecraft.getOverlay() == null) {
            String only = System.getProperty("yvanchuxiuzhen.smoke.only", "");
            if ("jadepickupsave".equals(only) || NaturalAlloyRepairCheck.SELECTOR.equals(only)) {
                int view = minecraft.options.renderDistance().get();
                int simulation = minecraft.options.simulationDistance().get();
                float volume = minecraft.options.getSoundSourceVolume(SoundSource.MASTER);
                String language = minecraft.options.languageCode;
                String settings = "视距=" + view + " 模拟距离=" + simulation + " 主音量=" + volume + " 语言=" + language;
                if (view != 2 || simulation != 5 || volume != 0.0f || !"zh_cn".equals(language)) {
                    report.add("FAIL " + only + " 启动配置不符：" + settings);
                    finish();
                    return;
                }
                report.add("INFO " + only + " 启动配置 " + settings);
            }
            List<String> selectors = List.of(only.split(",", -1));
            if (selectors.stream().map(String::strip).anyMatch(NaturalAlloyRepairCheck.SELECTOR::equals)
                    && (!NaturalAlloyRepairCheck.SAVE.equals(replayLevelId)
                    || !NaturalAlloyRepairCheck.ready(minecraft.gameDirectory.toPath()))) {
                report.add("FAIL 天然合金修理须独立选择，并使用核对指纹的一次性存档副本");
                finish();
                return;
            }
            if (selectors.stream().map(String::strip).anyMatch(NaturalMetalChainCheck.SELECTOR::equals)
                    && (!NaturalMetalChainCheck.SAVE.equals(replayLevelId)
                    || !NaturalMetalChainCheck.ready(minecraft.gameDirectory.toPath()))) {
                report.add("FAIL 精铁制箭须独立选择，并使用已核对指纹的一次性开发客户端副本");
                finish();
                return;
            }
            if (selectors.stream().map(String::strip).anyMatch(JadeBambooSupplyCheck.SELECTOR::equals)
                    && (selectors.size() != 1 || !JadeBambooSupplyCheck.SELECTOR.equals(only)
                    || !JadeBambooSupplyCheck.SAVE.equals(replayLevelId)
                    || !JadeBambooSupplyCheck.ready(minecraft.gameDirectory.toPath())
                    || minecraft.options.renderDistance().get() != 2 || minecraft.options.simulationDistance().get() != 5)) {
                report.add("FAIL 玉竹供给须独立选择，使用核对指纹的一次性副本和视距2/模拟距离5");
                finish();
                return;
            }
            if (selectors.stream().map(String::strip).anyMatch(JadeWoodSupplyCheck.SELECTOR::equals)
                    && (!JadeWoodSupplyCheck.SAVE.equals(replayLevelId)
                    || !JadeWoodSupplyCheck.ready(minecraft.gameDirectory.toPath())
                    || minecraft.options.renderDistance().get() != 2 || minecraft.options.simulationDistance().get() != 5)) {
                report.add("FAIL 文玉木供给须独立选择，使用核对指纹的一次性副本和视距2/模拟距离5");
                finish();
                return;
            }
            if (selectors.stream().map(String::strip).anyMatch(id -> id.equals("silksurvival") || id.equals("silksurvivalreentry"))) {
                Path gameDir = minecraft.gameDirectory.toPath().toAbsolutePath().normalize();
                if (!SilkSurvivalReentryCheck.SAVE.equals(replayLevelId) || !gameDir.endsWith(Path.of("run", "smoke"))
                        || !Files.isRegularFile(gameDir.resolve("saves").resolve(replayLevelId).resolve("level.dat"))) {
                    report.add("FAIL 蚕盒生存专项必须独立运行，且使用已准备的开发客户端副本");
                    finish();
                    return;
                }
            }
            if (!q235SpellShield && selectors.stream().map(String::strip).anyMatch(SpellShieldCheck.SELECTOR::equals)) {
                report.add("FAIL Q235 需要单独选择 " + SpellShieldCheck.SELECTOR);
                finish();
                return;
            }
            if (selectors.stream().map(String::strip).anyMatch(RenjianRandomCheck.Remainder::isSelector)
                    && (!RenjianRandomCheck.Remainder.isSelector(only) || minecraft.options.renderDistance().get() != 2
                        || minecraft.options.simulationDistance().get() != 5)) {
                report.add("FAIL 剩余随机结构需单独选择 renjianrandom02、renjianrandom05 或 renjianrandom06，并使用视距 2 / 模拟距离 5");
                finish();
                return;
            }
            if (selectors.size() > 1 && (selectors.contains(RenjianLocustNaturalCheck.SELECTOR)
                    || selectors.contains(RenjianCapturedJarCheck.SELECTOR))) {
                report.add("FAIL 自然飞蝗及后续装罐各需单独选择运行");
                finish();
                return;
            }
            if (RenjianLocustNaturalCheck.SAVE.equals(replayLevelId) || RenjianCapturedJarCheck.SAVE.equals(replayLevelId)) {
                Path gameDir = minecraft.gameDirectory.toPath().toAbsolutePath().normalize();
                Path save = gameDir.resolve("saves").resolve(replayLevelId);
                int view = minecraft.options.renderDistance().get();
                int simulation = minecraft.options.simulationDistance().get();
                if (!gameDir.endsWith(Path.of("run", "smoke")) || view != 2 || simulation != 5
                        || !Files.isRegularFile(save.resolve("level.dat")) || Files.exists(save.resolve("session.lock"))) {
                    report.add("FAIL " + only + " 需要未启动的固定副本和视距 2/5：" + save + "，实际=" + view + "/" + simulation);
                    finish();
                    return;
                }
                report.add("INFO REPLAY_LOCUST_OPEN selector=" + only + " save=" + save + " clientView=" + view + " clientSimulation=" + simulation);
            }
            if (!renjianResidence && selectors.contains(RenjianResidenceCheck.SELECTOR)) {
                report.add("FAIL 居所归档重放必须单独选择");
                finish();
                return;
            }
            if (renjianResidence) {
                RenjianResidenceCheck.replaySave(minecraft);
                levelId = replayLevelId;
                phase = 10;
                phaseStart = ticks;
                pressMenuButton(minecraft.screen, Component.translatable("menu.singleplayer"));
                return;
            }
            if (!renjianRewardSpell && selectors.contains(RenjianRewardSpellCheck.SELECTOR)) {
                blockFirstSpellExit(RenjianRewardSpellCheck.SELECTOR + " 需单独运行；未创建世界");
                return;
            }
            if (!renjianRewardLeafImpact && List.of(only.split(",")).contains(RenjianRewardLeafImpactCheck.SELECTOR)) {
                blockFirstSpellExit(RenjianRewardLeafImpactCheck.SELECTOR + " 需单独运行；未创建世界");
                return;
            }
            if (rewardReplay && !rewardReplayReady()) return;
            if (!replayLevelId.isEmpty()) {
                levelId = replayLevelId;
                next();
                minecraft.createWorldOpenFlows().openWorld(levelId, () -> {
                    if (rewardReplay) {
                        blockFirstSpellExit("无法打开人间奖励技能专用副本 " + levelId);
                        return;
                    }
                    report.add("FAIL 无法打开人间道专用存档副本 " + levelId);
                    finish();
                });
                return;
            }
            if (guardedWorldExit && (minecraft.options.renderDistance().get() != 2 || minecraft.options.simulationDistance().get() != 5)) {
                blockFirstSpellExit("启动配置需视距 2 / 模拟距离 5；实际 " + minecraft.options.renderDistance().get()
                        + " / " + minecraft.options.simulationDistance().get());
                return;
            }
            // 出身专项保留正常首登界面；其他检查在需要时自行开启。
            GameRules rules = new GameRules();
            rules.getRule(XiulianRules.BIRTH_SELECTION).set(renjianBirthSupply, null);
            if (q235SpellShield) {
                rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
                rules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, null);
            }
            LevelSettings settings = new LevelSettings("smoke", renjianBirthSupply || q235SpellShield ? GameType.SURVIVAL : GameType.CREATIVE,
                    false, q235SpellShield ? Difficulty.NORMAL : Difficulty.PEACEFUL, !renjianBirthSupply && !q235SpellShield,
                    rules, WorldDataConfiguration.DEFAULT);
            levelId = "smoke" + System.currentTimeMillis();
            if (q235SpellShield) report.add("INFO Q235 存档=" + levelId + "，超平坦生存，普通难度，种子=" + FIRST_SPELL_SEED);
            WorldOptions options = switch (System.getProperty("yvanchuxiuzhen.smoke.only", "")) {
                case "renjianfirstspell", "renjiancombatlifecycle", "renjianbirthsupply" -> new WorldOptions(FIRST_SPELL_SEED, true, false);
                case "renjianrandom", "renjianrandom02", "renjianrandom05", "renjianrandom06" ->
                        new WorldOptions(RenjianRandomCheck.SEED, true, false);
                case "plantsupply" -> new WorldOptions(PlantSupplyCheck.REPLAY_SEED, true, false);
                case SpellShieldCheck.SELECTOR -> new WorldOptions(FIRST_SPELL_SEED, false, false);
                default -> WorldOptions.defaultWithRandomSeed();
            };
            minecraft.createWorldOpenFlows().createFreshLevel(levelId, settings, options, q235SpellShield
                    ? registry -> registry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions()
                    : WorldPresets::createNormalWorldDimensions, minecraft.screen);
            next();
        }
    }

    /** 在原生选档列表中用存档目录标识选择副本，再按下原生进入按钮。 */
    private void selectResidenceWorld() throws IOException, ReflectiveOperationException {
        if (ticks - phaseStart > 400) throw new IllegalStateException("原生选档列表未出现居所专用副本 " + levelId);
        if (!(minecraft.screen instanceof SelectWorldScreen screen) || minecraft.getOverlay() != null) return;
        WorldSelectionList list = screen.children().stream().filter(WorldSelectionList.class::isInstance)
                .map(WorldSelectionList.class::cast).findFirst().orElseThrow();
        Field summaryField = WorldSelectionList.WorldListEntry.class.getDeclaredField("summary");
        summaryField.setAccessible(true);
        for (WorldSelectionList.Entry row : list.children()) {
            if (!(row instanceof WorldSelectionList.WorldListEntry entry)) continue;
            LevelSummary summary = (LevelSummary) summaryField.get(entry);
            if (!levelId.equals(summary.getLevelId())) continue;
            if (!entry.canJoin()) throw new IllegalStateException("原生菜单禁止进入 " + levelId);
            if (list.getSelected() != entry) {
                list.setSelected(entry);
                return;
            }
            Path save = RenjianResidenceCheck.replaySave(minecraft);
            Files.createFile(save.resolve(RenjianResidenceCheck.REPLAY_MARKER));
            report.add("INFO RESIDENCE_OPEN 原生单人游戏菜单已选 " + save + "，视距 2/5，中文，无教程");
            writeReport();
            phase = 1;
            phaseStart = ticks;
            pressMenuButton(screen, LevelSummary.PLAY_WORLD);
            return;
        }
    }

    private static void pressMenuButton(Screen screen, Component message) {
        screen.children().stream().filter(child -> child instanceof Button button && button.active
                        && button.getMessage().equals(message))
                .map(Button.class::cast).findFirst().orElseThrow().onPress();
    }

    private boolean rewardReplayReady() {
        int render = minecraft.options.renderDistance().get();
        int simulation = minecraft.options.simulationDistance().get();
        if (render != 2 || simulation != 5) {
            blockFirstSpellExit("回放启动配置需视距 2 / 模拟距离 5；实际 " + render + " / " + simulation);
            return false;
        }
        Path saves = minecraft.gameDirectory.toPath().resolve("saves").toAbsolutePath().normalize();
        Path copy = saves.resolve(replayLevelId);
        Path level = copy.resolve("level.dat");
        try {
            if (Files.isRegularFile(level) && copy.toRealPath().equals(saves.toRealPath().resolve(replayLevelId))
                    && level.toRealPath().equals(copy.toRealPath().resolve("level.dat"))) {
                if (!renjianRewardLeafImpact || RenjianRewardLeafImpactCheck.matchesArchive(copy)) return true;
                blockFirstSpellExit("Q40 副本的 level.dat 或本人玩家文件与施放后原归档不符：" + copy);
                return false;
            }
        } catch (IOException exception) {
            blockFirstSpellExit("无法核对回放专用副本 " + copy + "：" + exception);
            return false;
        }
        blockFirstSpellExit("缺少回放专用独立副本 " + copy + "；不自动复制或创建世界");
        return false;
    }

    private void waitForWorld() {
        if (!replayLevelId.isEmpty() && minecraft.screen instanceof BackupConfirmScreen screen
                && screen.getTitle().equals(Component.translatable("selectWorld.backupQuestion.experimental"))) {
            screen.children().stream().filter(child -> child instanceof Button button
                            && button.getMessage().equals(Component.translatable("selectWorld.backupJoinSkipButton")))
                    .map(child -> (Button) child).findFirst().orElseThrow().onPress();
        }
        boolean worldReady = minecraft.player != null && minecraft.level != null && minecraft.getSingleplayerServer() != null;
        if (renjianResidence) worldReady &= minecraft.screen == null && minecraft.getOverlay() == null;
        if (rewardReplay && (!worldReady || minecraft.screen instanceof ReceivingLevelScreen)) {
            if (ticks - phaseStart > 1200) {
                blockFirstSpellExit("人间奖励技能存档未就绪：" + (minecraft.screen == null ? "无界面" : minecraft.screen.getTitle().getString()));
            }
            return;
        }
        if (!replayLevelId.isEmpty() && (renjianResidence ? !worldReady : minecraft.player == null)
                && (ticks - phaseStart > (renjianResidence ? 2400 : 1200)
                || renjianResidence && minecraft.screen instanceof SelectWorldScreen)) {
            report.add("FAIL 人间道专用存档副本 " + levelId + " 未就绪："
                    + (minecraft.screen == null ? "无界面" : minecraft.screen.getTitle().getString()));
            finish();
            return;
        }
        if (worldReady && ticks - phaseStart > 100) {
            if (!stopDiagnosticsArmed) {
                WorldStopDiagnostics.arm(minecraft.getSingleplayerServer());
                stopDiagnosticsArmed = true;
            }
            next();
            if (!System.getProperty("yvanchuxiuzhen.smoke.only", "").isEmpty()) {
                phase = 8;
            }
        }
    }

    private void checkSystems() {
        if (systemChecks == null) {
            if (!renjianBirthSupply && !renjianResidence && !rewardReplay) minecraft.setScreen(null);
            String only = System.getProperty("yvanchuxiuzhen.smoke.only", "");
            List<String> names = only.isEmpty() ? List.of("relation", "attribute", "xiulian", "gongfa", "combat", "skill") : List.of(only.split(","));
            systemChecks = new ArrayList<>();
            for (String name : names) {
                systemChecks.addAll(switch (name) {
                    case RenjianRewardSpellCheck.SELECTOR -> List.of(new RenjianRewardSpellCheck(report, levelId, this::shot,
                            () -> pollFirstSpellExit("足额施放后同档重进")));
                    case RenjianRewardLeafImpactCheck.SELECTOR -> List.of(new RenjianRewardLeafImpactCheck(report, levelId, this::shot));
                    case "renjianbirthsupply" -> {
                        if (!renjianBirthSupply) throw new IllegalArgumentException("renjianbirthsupply 需单独运行");
                        yield List.of(new RenjianBirthSupplyCheck(report, levelId, this::shot, () -> pollFirstSpellExit("闭关后同档重进")));
                    }
                    case "birthitemdialogue" -> List.of(new BirthItemDialogueCheck(report, levelId));
                    case "reincarnation" -> List.of(new ReincarnationCheck(report, levelId));
                    case "relation" -> List.of(new RelationCheck(report, levelId, this::shot));
                    case "attribute" -> List.of(new AttributeCheck(report, levelId));
                    case "xiulian" -> List.of(new XiulianCheck(report, levelId, this::shot), new XiulianProgressCheck(report, levelId, this::shot),
                            new DantianCheck(report, levelId, this::shot), new BloodPathCheck(report, levelId, this::shot),
                            new XiulianOperationsCheck(report, levelId));
                    case "gongfa" -> List.of(new GongfaCheck(report, levelId), new ComprehensionCheck(report, levelId, this::shot));
                    case "combat" -> List.of(new CombatCheck(report, levelId, this::shot));
                    case SpellShieldCheck.SELECTOR -> List.of(new SpellShieldCheck(report, levelId));
                    case "skill" -> List.of(new SkillCheck(report, levelId), new SkillProjectileCheck(report, levelId), new SkillEntityCheck(report, levelId), new SwordArtCheck(report, levelId), new LingwenCheck(report, levelId), new FireBirdCheck(report, levelId), new CorpseCheck(report, levelId), new VoidRiftCheck(report, levelId), new DamageAreaCheck(report, levelId), new YuanyingCheck(report, levelId), new SkillArrowCheck(report, levelId), new SkillMovementCheck(report, levelId), new SkillUiCheck(report, levelId, this::shot), new SkillRemainderCheck(report, levelId));
                    case "firebird" -> List.of(new FireBirdCheck(report, levelId));
                    case "corpse" -> List.of(new CorpseCheck(report, levelId));
                    case "windbladetarget" -> List.of(new WindBladeTargetCheck(report, levelId));
                    case "voidrift" -> List.of(new VoidRiftCheck(report, levelId));
                    case "damagearea" -> List.of(new DamageAreaCheck(report, levelId));
                    case "yuanying" -> List.of(new YuanyingCheck(report, levelId));
                    case "skillarrow" -> List.of(new SkillArrowCheck(report, levelId));
                    case "skillmovement" -> List.of(new SkillMovementCheck(report, levelId));
                    case "skillui" -> List.of(new SkillUiCheck(report, levelId, this::shot));
                    case "skillloadoutcontainer" -> List.of(new SkillLoadoutContainerCheck(report, levelId));
                    case "skillremainder" -> List.of(new SkillRemainderCheck(report, levelId));
                    case "particle" -> List.of(new ParticleLibraryCheck(report, levelId, this::shot));
                    case "skillparticle" -> List.of(new SkillParticleCheck(report, levelId, this::shot));
                    case "skillvisual" -> List.of(new SkillVisualCheck(report, levelId, this::shot));
                    case "lightning" -> List.of(new LightningCheck(report, levelId, this::shot));
                    case "tribulation" -> List.of(new TribulationCheck(report, levelId, this::shot));
                    case "dantianrealmchain" -> List.of(new DantianTribulationLifecycleCheck(report, levelId));
                    case "equipmentweapon" -> List.of(new EquipmentWeaponCheck(report, levelId));
                    case "kailingmaterial" -> List.of(new KailingMaterialCheck(report, levelId));
                    case "reactiveplants" -> List.of(new ReactiveWildPlantsCheck(report, levelId));
                    case "plantitemextensions" -> List.of(new PlantItemExtensionsCheck(report, levelId));
                    case "fabao013" -> List.of(new Fabao013Check(report, levelId));
                    case "fabao017" -> List.of(new Fabao017Check(report, levelId, this::shot));
                    case "bookreading" -> List.of(new BookReadingCheck(report, levelId, this::shot));
                    case "bookbinding" -> List.of(new BookBindingCheck(report, levelId, this::shot));
                    case "bookquiz" -> List.of(new BookQuizCheck(report, levelId, this::shot));
                    case "treasuremedicine" -> List.of(new TreasureMedicineRewardCheck(report, levelId));
                    case "bookquizowner" -> List.of(new BookQuizOwnershipCheck(report, levelId, this::shot));
                    case "yihuomaterial" -> List.of(new YihuoMaterialCheck(report, levelId, this::shot));
                    case "demon" -> List.of(new DemonTransformationCheck(report, levelId, this::shot));
                    case "equipmentproduct" -> List.of(new EquipmentProductCheck(report, levelId, this::shot));
                    case "flyinginstrumentacquisition" -> List.of(new FlyingInstrumentAcquisitionCheck(report, levelId));
                    case "equipmentconsumers" -> List.of(new EquipmentConsumersCheck(report, levelId, this::shot));
                    case "flyinginstrumentdeath" -> List.of(new FlyingInstrumentDeathCheck(report, levelId));
                    case "flyinginstrumentreentry" -> List.of(new FlyingInstrumentReentryCheck(report, levelId));
                    case "benmingcultivation" -> List.of(new BenmingCultivationCheck(report, levelId));
                    case "accessories" -> List.of(new AccessoryCheck(report, levelId, this::shot));
                    case "storage" -> List.of(new StorageCheck(report, levelId, this::shot));
                    case "bank" -> List.of(new BankCheck(report, levelId, this::shot));
                    case "commerce" -> List.of(new CommerceCheck(report, levelId, this::shot));
                    case "spiritstone" -> List.of(new SpiritStoneCheck(report, levelId), new SpiritStoneBundleCheck(report, levelId));
                    case "spiritstonebundle" -> List.of(new SpiritStoneBundleCheck(report, levelId));
                    case "materialcrafting" -> List.of(new MaterialCraftingCheck(report, levelId));
                    case "construction" -> List.of(new ConstructionCheck(report, levelId, this::shot));
                    case "towntemplates" -> List.of(new TownTemplatePlacementCheck(report, levelId, this::shot));
                    case "buildinghammer" -> List.of(new BuildingHammerCheck(report, levelId, this::shot));
                    case "dungeon" -> List.of(new DungeonCheck(report, levelId, this::shot));
                    case "structurerelease" -> List.of(new StructureReleaseCheck(report, levelId));
                    case "temporaryteam" -> List.of(new TemporaryTeamCheck(report, levelId, this::shot));
                    case "npcresidents" -> List.of(new NpcResidentsCheck(report, levelId, this::shot));
                    case "npcmanagement" -> List.of(new NpcManagementCheck(report, levelId, this::shot));
                    case "npcmerchanteggs" -> List.of(new NpcMerchantSpawnEggCheck(report, levelId));
                    case "xiuzhespawn" -> List.of(new XiuzheSpawnCheck(report, levelId));
                    case "questjournal" -> List.of(new QuestJournalCheck(report, levelId, this::shot));
                    case "questeditor" -> List.of(new QuestTriggerEditorCheck(report, levelId, this::shot));
                    case "guconstitutionharvest" -> List.of(new GuConstitutionHarvestCheck(report, levelId, this::shot));
                    case "festivalremaining" -> List.of(new FestivalRemainingCheck(report, levelId, this::shot));
                    case "doublebang" -> List.of(new DoubleBangFireworkCheck(report, levelId, this::shot));
                    case "qiankun" -> List.of(new QiankunCheck(report, levelId, this::shot));
                    case "q119" -> {
                        if (!QiankunDialogueCheck.SAVE_ID.equals(levelId)) throw new IllegalArgumentException("q119 需单独运行");
                        yield List.of(new QiankunDialogueCheck(report, levelId, this::shot));
                    }
                    case "npc08" -> List.of(new NpcFlyingShuttleCheck(report, levelId, this::shot));
                    case "npc_underworld" -> List.of(new NpcUnderworldCheck(report, levelId, this::shot));
                    case "wishlantern" -> List.of(new WishLanternCheck(report, levelId, this::shot));
                    case "rabbitcage" -> List.of(new RabbitCageCheck(report, levelId, this::shot));
                    case "furniturestorage" -> List.of(new FurnitureStorageCheck(report, levelId, this::shot));
                    case "inscribeddecor" -> List.of(new InscribedDecorCheck(report, levelId, this::shot));
                    case "decor_remainder" -> List.of(new DecorRemainderCheck(report, levelId, this::shot));
                    case "animateddecor" -> List.of(new AnimatedDecorCheck(report, levelId, this::shot));
                    case "worldexploration" -> List.of(new WorldExplorationCheck(report, levelId, this::shot));
                    case "globalbiomefeatures" -> List.of(new GlobalBiomeFeaturesCheck(report, levelId));
                    case "dimensionportal" -> List.of(new DimensionPortalCheck(report, levelId, this::shot));
                    case "renjianrandom" -> List.of(new RenjianRandomCheck(report, levelId));
                    case "renjianrandom02" -> List.of(new RenjianRandomCheck(report, levelId, RenjianRandomCheck.Remainder.SURFACE));
                    case "renjianrandom05" -> List.of(new RenjianRandomCheck(report, levelId, RenjianRandomCheck.Remainder.CAVERN));
                    case "renjianrandom06" -> List.of(new RenjianRandomCheck(report, levelId, RenjianRandomCheck.Remainder.RUIN));
                    case RenjianLocustNaturalCheck.SELECTOR -> List.of(new RenjianLocustNaturalCheck(report, levelId));
                    case RenjianCapturedJarCheck.SELECTOR -> List.of(new RenjianCapturedJarCheck(report, levelId, this::shot));
                    case "renjianvisual" -> List.of(new RenjianVisualCheck(report, levelId, this::shot));
                    case RenjianResidenceCheck.SELECTOR -> List.of(new RenjianResidenceCheck(report, levelId, this::shot, this::writeReport));
                    case "tianqve" -> List.of(new TianqveWorldgenCheck(report, levelId, this::shot));
                    case "wugui" -> List.of(new WuguiWorldgenCheck(report, levelId, this::shot));
                    case "wuguiportal" -> List.of(new DimensionPortalCheck(report, levelId, this::shot, DimensionPortalSpec.WUGUI));
                    case "renjianportal" -> List.of(new DimensionPortalCheck(report, levelId, this::shot, DimensionPortalSpec.RENJIAN));
                    case "renjiandeath" -> List.of(new RenjianDeathCheck(report, levelId, this::shot));
                    case "renjiancombatlifecycle" -> {
                        if (!renjianCombatLifecycle) throw new IllegalArgumentException("renjiancombatlifecycle 需单独运行");
                        yield List.of(new RenjianCombatLifecycleCheck(report, levelId,
                                () -> pollFirstSpellExit("战斗中同档重进"), this::writeReport));
                    }
                    case "renjiancorpseharvest" -> List.of(new RenjianCorpseHarvestCheck(report, levelId));
                    case "aquaticcorpse" -> List.of(new AquaticCorpseCheck(report, levelId));
                    case "renjianpacks" -> List.of(new RenjianPackCheck(report, levelId));
                    case "temporarybeasts" -> List.of(new TemporaryBeastCheck(report, levelId));
                    case "ghostfirechain" -> List.of(new GhostFireChainCheck(report, levelId));
                    case "renjiancorpseclient" -> List.of(new RenjianCorpseClientCheck(report, levelId));
                    case "renjianentry" -> List.of(new DimensionPortalCheck(report, levelId, this::shot,
                            DimensionPortalSpec.RENJIAN, new RenjianEntryProbe()));
                    case "xiuluoportal" -> List.of(new DimensionPortalCheck(report, levelId, this::shot, DimensionPortalSpec.XIULUO));
                    case "questblockrefresh" -> List.of(new QuestBlockRefreshCheck(report, levelId));
                    case "renjianstarter" -> List.of(new RenjianStarterCheck(report, levelId, this::shot, true));
                    case "overworldstarter" -> List.of(new RenjianStarterCheck(report, levelId, this::shot, false));
                    case "renjianfirstspell" -> {
                        if (!guardedFirstSpell) throw new IllegalArgumentException("renjianfirstspell 需单独运行");
                        yield List.of(new RenjianStarterCheck(report, levelId, this::shot, this::writeBreakthroughEvidence,
                                () -> pollFirstSpellExit("首破后同档重进")));
                    }
                    case "overworldfirstspell" -> List.of(new RenjianStarterCheck(report, levelId, this::shot, false, true));
                    case "handheldfirework" -> List.of(new HandheldFireworkCheck(report, levelId, this::shot));
                    case "festivalfirework" -> List.of(new FestivalFireworkCheck(report, levelId));
                    case "boss" -> List.of(new BossCheck(report, levelId, this::shot));
                    case "treasure" -> List.of(new TreasureCheck(report, levelId, this::shot));
                    case "treasuretool" -> List.of(new TreasureToolCheck(report, levelId, this::shot));
                    case "treasurecapability" -> List.of(new TreasureChestCapabilityCheck(report, levelId));
                    case "casket" -> List.of(new TreasureCasketCheck(report, levelId));
                    case "guchong" -> List.of(new GuchongCheck(report, levelId, this::shot));
                    case "guchonglifecycle" -> List.of(new GuchongLifecycleCheck(report, levelId));
                    case "guchonggroundsave" -> List.of(new GuchongGroundSaveCheck(report, levelId, this::shot));
                    case "spiritbeastbag" -> List.of(new SpiritBeastBagCheck(report, levelId, this::shot));
            case "yaoshourecovery" -> List.of(new YaoshouRecoveryCheck(report, levelId));
            case "yaoshouinteraction" -> List.of(new YaoshouInteractionCheck(report, levelId, this::shot));
            case "yaoshou017form" -> List.of(new Yaoshou017FormBagCheck(report, levelId));
            case "boss02axe" -> List.of(new Boss02AxeCheck(report, levelId));
            case "boss02axehit" -> List.of(new Boss02AxeHitCheck(report, levelId));
            case "yaoshouprojectileitems" -> List.of(new YaoshouProjectileItemCheck(report, levelId));
            case "coffinghost" -> List.of(new CoffinGhostCheck(report, levelId, this::shot));
            case "coffinghostmarker" -> List.of(new CoffinGhostMarkerCheck(report, levelId));
            case "moshou002" -> List.of(new Moshou002Check(report, levelId, this::shot));
            case "moshou001drops" -> List.of(new Moshou001DropsCheck(report, levelId));
                    case "guchong05" -> List.of(new Guchong05LifecycleCheck(report, levelId));
                    case "guchongflute" -> List.of(new GuchongFluteCheck(report, levelId, this::shot));
                    case "guchong01" -> List.of(new Guchong01Check(report, levelId, this::shot));
                    case "guchongconstitution" -> List.of(new GuConstitutionHarvestCheck(report, levelId, this::shot));
                    case "guchongconstitutionuse" -> List.of(new GuConstitutionHarvestCheck(report, levelId, this::shot, GuConstitutionHarvestCheck.Scope.USE_ONLY));
                    case "guchongconstitutionupper" -> List.of(new GuConstitutionHarvestCheck(report, levelId, this::shot, true));
                    case "formationelement" -> List.of(new FormationElementCheck(report, levelId, this::shot));
                    case "formation" -> List.of(new FormationCheck(report, levelId), new FormationElementCheck(report, levelId, this::shot));
                    case "formationspace" -> List.of(new FormationCheck(report, levelId, true));
                    case "formationyang" -> List.of(new FormationCheck(report, levelId, false, true));
                    case "professioncommand" -> List.of(new ProfessionCommandCheck(report, levelId));
                    case "panelpages" -> List.of(new PanelPagesCheck(report, levelId, this::shot));
                    case "materialunlock" -> List.of(new MaterialUnlockCheck(report, levelId, this::shot));
                    case "cultivationcommand" -> List.of(new CultivationCommandCheck(report, levelId));
                    case "tiandao_display" -> List.of(new TiandaoDisplayCheck(report, levelId, this::shot));
                     case "nbteditor" -> List.of(new NbtEditorCheck(report, levelId));
                     case "birthinitialrewards" -> List.of(new BirthInitialRewardsCheck(report, levelId));
                     case "ling11" -> List.of(new Ling11GuideCheck(report, levelId, this::shot));
                     case "tiandao_multi_structure" -> List.of(new TiandaoMultiStructureCheck(report, levelId, this::shot));
                    case "economycommand" -> List.of(new EconomyCommandCheck(report, levelId));
                    case "biggrass" -> List.of(new BigGrassCheck(report, levelId));
                    case "plantground" -> List.of(new PlantGroundCheck(report, levelId));
                    case "thundergrassdrops" -> List.of(new ThundergrassDropsCheck(report, levelId));
                    case "jadeoredrops", JadeOreDropsCheck.EMPTY -> List.of(new JadeOreDropsCheck(report, levelId));
                    case NaturalAlloyRepairCheck.SELECTOR -> List.of(new NaturalAlloyRepairCheck(report, levelId, this::shot));
                    case "jadecarving" -> List.of(new JadeCarvingCheck(report, levelId, this::shot));
                    case "jadepickupsave" -> List.of(new JadePickupPersistenceCheck(report, levelId, this::shot));
                    case "peachleaves" -> List.of(new PeachLeavesCheck(report, levelId));
                    case "peachflower" -> List.of(new PeachLeavesCheck(report, levelId, "peachflower", "桃花"));
                    case "particleleaves" -> List.of(
                            new PeachLeavesCheck(report, levelId, "ginkgoleaves", "银杏叶"),
                            new PeachLeavesCheck(report, levelId, "poplarleaves", "白杨叶"),
                            new PeachLeavesCheck(report, levelId, "poplarleavesyellow", "黄白杨叶"));
                    case "plantsupply" -> List.of(new PlantSupplyCheck(report, levelId));
                    case "spicetreesupply" -> List.of(new SpiceTreeSupplyCheck(report, levelId));
                    case "plantsoils" -> List.of(new PlantSoilsCheck(report, levelId));
                    case "hotoil" -> List.of(new HotOilCheck(report, levelId, this::shot));
                    case "willowdrooping" -> List.of(new WillowDroopingCheck(report, levelId));
                    case "corpsegrass" -> List.of(new CorpseGrassCheck(report, levelId));
                    case "cerealharvest" -> List.of(new FuluCookingCheck(report, levelId, FuluCookingCheck.Harvest.CEREAL));
                    case "vegetableharvest" -> List.of(new FuluCookingCheck(report, levelId, FuluCookingCheck.Harvest.VEGETABLE));
                    case "fulucooking" -> List.of(new FuluCookingCheck(report, levelId, this::shot));
                    case "naturalsilkchain" -> List.of(new NaturalSilkChainCheck(report, levelId));
                    case NaturalMetalChainCheck.SELECTOR -> List.of(new NaturalMetalChainCheck(report, levelId));
                    case JadeWoodSupplyCheck.SELECTOR -> List.of(new JadeWoodSupplyCheck(report, levelId));
                    case JadeBambooSupplyCheck.SELECTOR -> List.of(new JadeBambooSupplyCheck(report, levelId));
                    case "metalrepair" -> List.of(new MetalRepairCheck(report, levelId));
                    case "silksurvival" -> List.of(new SilkSurvivalCheck(report, levelId));
                    case "silksurvivalreentry" -> List.of(new SilkSurvivalReentryCheck(report, levelId));
                    case "fuluripening" -> List.of(new FuluRipeningCheck(report, levelId));
                    case "fuluspecial" -> List.of(new FuluSpecialCheck(report, levelId, this::shot));
                    case "fulutaming" -> List.of(new FuluTamingCheck(report, levelId, this::shot));
                    case "fulucorpse" -> List.of(new FuluCorpseCheck(report, levelId));
                    case "caishenye" -> List.of(new CaishenyeCheck(report, levelId, this::shot));
                    default -> throw new IllegalArgumentException("没有该系统的检查：" + name);
                });
            }
        }
        if (systemChecks.isEmpty()) {
            next();
        } else if (systemChecks.get(0).tick(ticks)) {
            systemChecks.remove(0);
        }
    }

    /** 在平台上按行排列全部状态；平台下方实心，方块放置不触发邻接更新，以免双格方块被立即清除。 */
    private void placeBlocks() {
        List<BlockState> states = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(MOD)) {
                states.addAll(block.getStateDefinition().getPossibleStates());
            }
        }
        int columns = 64;
        int base = 200;
        plantCloseups(states, columns, base);
        armorCloseups(base);
        // 世界由服务端线程持有，放置在服务端线程执行并等待完成。
        minecraft.getSingleplayerServer().submit(() -> {
            ServerLevel level = minecraft.getSingleplayerServer().overworld();
            ServerPlayer player = minecraft.getSingleplayerServer().getPlayerList().getPlayers().get(0);
            for (int i = 0; i < states.size(); i++) {
                BlockPos pos = new BlockPos((i % columns) * 2, base + 1, (i / columns) * 2);
                level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 2);
                level.setBlock(pos, states.get(i), 2);
            }
            player.teleportTo(level, 40, base + 6, -4, 0.0F, 35.0F);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }).join();
        report.add("INFO 放置方块状态 " + states.size());
        next();
    }

    /** 第一个花、第一个双格植物与两个着色方块的近景；双格植物在下半格上方补放上半格。 */
    private void plantCloseups(List<BlockState> states, int columns, int base) {
        boolean flower = false;
        boolean doublePlant = false;
        for (int i = 0; i < states.size(); i++) {
            BlockState state = states.get(i);
            BlockPos pos = new BlockPos((i % columns) * 2, base + 1, (i / columns) * 2);
            String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            if (!flower && state.getBlock() instanceof FlowerBlock) {
                flower = true;
                closeups.add(new Closeup("plant_" + path + ".png", CameraType.FIRST_PERSON, pos.getX() + 0.5, base + 4, pos.getZ() - 3,
                        0.0F, 54.0F, player -> {
                }));
            }
            if ((path.equals("grassblock") || path.equals("cloudblock")) && closeups.stream().noneMatch(c -> c.name().equals("tint_" + path + ".png"))) {
                closeups.add(new Closeup("tint_" + path + ".png", CameraType.FIRST_PERSON, pos.getX() + 0.5, base + 4, pos.getZ() - 3,
                        0.0F, 54.0F, player -> {
                }));
            }
            if (!doublePlant && state.getBlock() instanceof DoublePlantBlock && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                doublePlant = true;
                closeups.add(new Closeup("plant_" + path + ".png", CameraType.FIRST_PERSON, pos.getX() + 0.5, base + 4, pos.getZ() - 3,
                        0.0F, 50.0F, player -> player.serverLevel().setBlock(pos.above(),
                        state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 2)));
            }
        }
    }

    /** 每套盔甲各让玩家穿上全部部件，以三人称正面视角拍照，贴图缺失时显示为紫黑方格。 */
    private void armorCloseups(int base) {
        Map<Holder<ArmorMaterial>, List<ItemStack>> sets = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ArmorItem armor && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MOD)) {
                sets.computeIfAbsent(armor.getMaterial(), material -> new ArrayList<>()).add(new ItemStack(item));
            }
        }
        for (List<ItemStack> set : sets.values()) {
            String name = "armor_" + BuiltInRegistries.ITEM.getKey(set.get(0).getItem()).getPath() + ".png";
            closeups.add(new Closeup(name, CameraType.THIRD_PERSON_FRONT, 40, base + 6, 20, 0.0F, 0.0F, player -> {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.isArmor()) {
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
                for (ItemStack piece : set) {
                    player.setItemSlot(((ArmorItem) piece.getItem()).getEquipmentSlot(), piece);
                }
            }));
        }
    }

    /** 从平台上方俯视几处方块行，再依次拍近景，隐藏界面以便看清方块。 */
    private void worldShots() {
        int local = ticks - phaseStart;
        int index = local / 70;
        int step = local % 70;
        if (index >= VIEWS.length + closeups.size()) {
            minecraft.options.hideGui = false;
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            next();
            return;
        }
        Closeup closeup = index < VIEWS.length ? null : closeups.get(index - VIEWS.length);
        if (step == 0) {
            minecraft.options.hideGui = true;
            minecraft.options.setCameraType(closeup == null ? CameraType.FIRST_PERSON : closeup.camera());
            minecraft.getSingleplayerServer().submit(() -> {
                ServerPlayer player = minecraft.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                ServerLevel level = minecraft.getSingleplayerServer().overworld();
                if (closeup == null) {
                    player.teleportTo(level, VIEWS[index][0], 208, VIEWS[index][1] * 2 - 6, 0.0F, 55.0F);
                } else {
                    closeup.setup().accept(player);
                    player.teleportTo(level, closeup.x(), closeup.y(), closeup.z(), closeup.yRot(), closeup.xRot());
                }
            }).join();
        }
        if (step == 60) {
            shot(closeup == null ? "world_" + index + ".png" : closeup.name());
        }
    }

    /** 先穿 clothes_04、再穿原版铁盔甲，各自挥动手臂，静止时与挥臂过程中逐 tick 截图，用来对比盔甲与手臂重叠处的显示。 */
    private void swingShots() {
        int local = ticks - phaseStart;
        swingSequence(local, "swing_", List.of(equipByPath("clothes_04_")));
        swingSequence(local - 100, "swing_iron_", List.of(new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE),
                new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS)));
        if (local >= 200) {
            minecraft.options.hideGui = false;
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            next();
        }
    }

    private static ItemStack[] equipByPath(String prefix) {
        List<ItemStack> pieces = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ArmorItem && BuiltInRegistries.ITEM.getKey(item).getPath().startsWith(prefix)) {
                pieces.add(new ItemStack(item));
            }
        }
        return pieces.toArray(new ItemStack[0]);
    }

    private void swingSequence(int local, String prefix, List<ItemStack> pieces) {
        if (local == 1) {
            minecraft.options.hideGui = true;
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            minecraft.getSingleplayerServer().submit(() -> {
                ServerPlayer player = minecraft.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.isArmor()) {
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
                for (ItemStack piece : pieces) {
                    player.setItemSlot(((ArmorItem) piece.getItem()).getEquipmentSlot(), piece);
                }
                player.teleportTo(minecraft.getSingleplayerServer().overworld(), 40, 206, 20, 0.0F, 0.0F);
            }).join();
        }
        if (local >= 50 && local < 54) {
            shot(prefix + "rest_" + (local - 50) + ".png");
        }
        if (local == 60) {
            minecraft.player.swing(InteractionHand.MAIN_HAND);
        }
        if (local >= 60 && local < 72) {
            shot(prefix + (local - 60) + ".png");
        }
    }

    private void shot(String name) {
        try (NativeImage image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
            Path directory = output.resolve("screenshots");
            Files.createDirectories(directory);
            image.writeToFile(directory.resolve(name));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void checkModelsAndNames() {
        BakedModel missing = minecraft.getModelManager().getMissingModel();
        int blocks = 0;
        int items = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(MOD)) {
                continue;
            }
            blocks++;
            if (!I18n.exists(block.getDescriptionId())) {
                report.add("FAIL 无名称 方块 " + BuiltInRegistries.BLOCK.getKey(block));
            }
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                if (minecraft.getBlockRenderer().getBlockModelShaper().getBlockModel(state) == missing) {
                    report.add("FAIL 缺失模型 方块状态 " + state);
                    break;
                }
            }
        }
        Set<String> seenKeys = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MOD)) {
                continue;
            }
            items++;
            if (!I18n.exists(item.getDescriptionId())) {
                report.add("FAIL 无名称 物品 " + BuiltInRegistries.ITEM.getKey(item));
            }
            if (minecraft.getItemRenderer().getItemModelShaper().getItemModel(item) == missing) {
                report.add("FAIL 缺失模型 物品 " + BuiltInRegistries.ITEM.getKey(item));
            }
            List<Component> lines = new ItemStack(item).getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player,
                    TooltipFlag.Default.NORMAL);
            for (Component line : lines) {
                if (line.getString().startsWith("tooltip." + MOD) && seenKeys.add(line.getString())) {
                    report.add("FAIL 提示文本未翻译 " + line.getString());
                }
            }
        }
        int sounds = 0;
        for (SoundEvent sound : BuiltInRegistries.SOUND_EVENT) {
            if (!sound.getLocation().getNamespace().equals(MOD)) {
                continue;
            }
            sounds++;
            WeighedSoundEvents definition = minecraft.getSoundManager().getSoundEvent(sound.getLocation());
            if (definition == null) {
                report.add("FAIL 声音事件没有 sounds.json 定义 " + sound.getLocation());
            } else if (definition.getSubtitle() != null && definition.getSubtitle().getString().startsWith("subtitles.")) {
                report.add("FAIL 声音字幕未翻译 " + definition.getSubtitle().getString());
            }
        }
        report.add("INFO 已核对方块 " + blocks + " 物品 " + items + " 声音事件 " + sounds);
        checkStructures();
    }

    /** 在服务端线程加载本 Mod 的全部结构模板，摆放方块实体最多的几个，并把每个模板的调色板方块名写入 structure_palette.tsv。 */
    private void checkStructures() {
        boolean heavy = Boolean.getBoolean("yvanchuxiuzhen.smoke.heavy");
        MinecraftServer server = minecraft.getSingleplayerServer();
        List<String> palette = new ArrayList<>();
        server.submit(() -> {
            StructureTemplateManager manager = server.getStructureManager();
            Map<ResourceLocation, Resource> found = server.getResourceManager().listResources("structure",
                    id -> id.getNamespace().equals(MOD) && id.getPath().endsWith(".nbt"));
            List<ResourceLocation> ids = found.keySet().stream()
                    .map(id -> ResourceLocation.fromNamespaceAndPath(MOD, id.getPath().substring("structure/".length(), id.getPath().length() - ".nbt".length())))
                    .sorted().toList();
            record Loaded(ResourceLocation id, StructureTemplate template, CompoundTag saved, int blockEntities) {
            }
            List<Loaded> loaded = new ArrayList<>();
            long slowest = 0;
            String slowestId = "";
            for (ResourceLocation id : ids) {
                if (!heavy && id.getPath().equals("difujiegou")) {
                    continue;
                }
                long start = System.nanoTime();
                Optional<StructureTemplate> template = manager.get(id);
                long millis = (System.nanoTime() - start) / 1_000_000;
                if (millis > slowest) {
                    slowest = millis;
                    slowestId = id.getPath();
                }
                if (template.isEmpty()) {
                    report.add("FAIL 结构无法加载 " + id);
                    continue;
                }
                CompoundTag saved = template.get().save(new CompoundTag());
                int blockEntities = 0;
                for (Tag block : saved.getList("blocks", Tag.TAG_COMPOUND)) {
                    if (((CompoundTag) block).contains("nbt")) {
                        blockEntities++;
                    }
                }
                ListTag names = saved.contains("palettes") ? saved.getList("palettes", Tag.TAG_LIST).getList(0) : saved.getList("palette", Tag.TAG_COMPOUND);
                StringBuilder line = new StringBuilder(id.getPath()).append('	').append(millis).append('	')
                        .append(saved.getList("blocks", Tag.TAG_COMPOUND).size()).append('	');
                for (Tag entry : names) {
                    line.append(((CompoundTag) entry).getString("Name")).append(' ');
                }
                palette.add(line.toString());
                loaded.add(new Loaded(id, template.get(), saved, blockEntities));
            }
            report.add("INFO 结构 加载 " + loaded.size() + "/" + ids.size() + (heavy ? "" : "（未含 difujiegou）") + "，最慢 " + slowestId + " " + slowest + " ms");
            List<Loaded> placed = loaded.stream().filter(entry -> entry.saved().getList("blocks", Tag.TAG_COMPOUND).size() <= 60_000)
                    .sorted((a, b) -> b.blockEntities() - a.blockEntities()).limit(8).toList();
            ServerLevel level = server.overworld();
            Map<String, Integer> withoutEntity = new TreeMap<>();
            for (int i = 0; i < placed.size(); i++) {
                Loaded entry = placed.get(i);
                BlockPos origin = new BlockPos((i % 4) * 100, 230, 300 + (i / 4) * 100);
                boolean ok = entry.template().placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), 2);
                int expected = 0;
                int present = 0;
                for (Tag block : entry.saved().getList("blocks", Tag.TAG_COMPOUND)) {
                    CompoundTag tag = (CompoundTag) block;
                    ListTag offset = tag.getList("pos", Tag.TAG_INT);
                    BlockPos pos = origin.offset(offset.getInt(0), offset.getInt(1), offset.getInt(2));
                    if (tag.contains("nbt") && !level.getBlockState(pos).isAir()) {
                        expected++;
                        if (level.getBlockEntity(pos) != null) {
                            present++;
                        } else {
                            withoutEntity.merge(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString(), 1, Integer::sum);
                        }
                    }
                }
                report.add("INFO 结构 摆放 " + entry.id().getPath() + (ok ? "" : "（失败）") + " 方块实体 " + present + "/" + expected);
            }
            report.add("INFO 结构 带数据但没有方块实体的方块 " + withoutEntity);
        }).join();
        try {
            Files.createDirectories(output);
            Files.write(output.resolve("structure_palette.tsv"), palette, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void openCreative() throws IllegalStateException {
        try {
            minecraft.setScreen(new CreativeModeInventoryScreen(minecraft.player, minecraft.player.connection.enabledFeatures(), true));
            tabs = new ArrayList<>(CreativeModeTabs.allTabs());
            selectTab = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
            selectTab.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void cycleTabs() throws ReflectiveOperationException {
        if ((ticks - phaseStart) % 4 != 0) {
            return;
        }
        if (pendingShot != null) {
            shot(pendingShot);
            pendingShot = null;
        }
        if (tabIndex >= tabs.size()) {
            next();
            return;
        }
        CreativeModeTab tab = tabs.get(tabIndex++);
        showPageOf(tab);
        selectTab.invoke(minecraft.screen, tab);
        String title = tab.getDisplayName().getString();
        report.add("INFO 创造栏 " + title + " 物品 " + tab.getDisplayItems().size());
        if (title.startsWith("itemGroup.")) {
            report.add("FAIL 创造栏未翻译 " + title);
        }
        if (MOD.equals(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).getNamespace())) {
            pendingShot = "tab_" + BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).getPath() + ".png";
        }
    }

    /** 切到包含该栏的标签页，否则第二页的栏在截图里不可见。 */
    private void showPageOf(CreativeModeTab tab) throws ReflectiveOperationException {
        Field pagesField = CreativeModeInventoryScreen.class.getDeclaredField("pages");
        Field currentField = CreativeModeInventoryScreen.class.getDeclaredField("currentPage");
        pagesField.setAccessible(true);
        currentField.setAccessible(true);
        for (Object page : (List<?>) pagesField.get(minecraft.screen)) {
            if (((CreativeTabsScreenPage) page).getVisibleTabs().contains(tab)) {
                currentField.set(minecraft.screen, page);
            }
        }
    }

    private void finish() {
        if (finishRequested) return;
        finishRequested = true;
        phase = 99;
        report.add((minecraft.mouseHandler.isMouseGrabbed() ? "FAIL" : "INFO") + " 检查期间不捕获鼠标");
        report.add("WAITING_FOR_WORLD_EXIT");
        writeReport();
    }

    private void waitForSettledExit() {
        if (renjianResidence && minecraft.getSingleplayerServer() == null && minecraft.level == null) {
            completeExit();
            return;
        }
        if (guardedWorldExit) {
            if (pollFirstSpellExit("最终退出")) {
                firstSpellCompletingExit = true;
                completeExit();
            }
            return;
        }
        var server = minecraft.getSingleplayerServer();
        if (minecraft.level != null && server != null) {
            int[] pending = new int[1];
            WorldStopRequeueDiagnostics.Readiness[] readiness = new WorldStopRequeueDiagnostics.Readiness[1];
            server.submit(() -> {
                for (ServerLevel level : server.getAllLevels()) {
                    pending[0] += level.getChunkSource().getPendingTasksCount();
                }
                readiness[0] = WorldStopRequeueDiagnostics.readiness(server);
            }).join();
            settledExitTicks = pending[0] == 0 && readiness[0].settled() ? settledExitTicks + 1 : 0;
            if (settledExitTicks < 40) return;
            report.add("INFO Smoke停服前区块状态连续40 tick收敛");
        }

        completeExit();
    }

    /** 闸门沿用公共跨维判据。只留一个未完成的采样；服务端忙时不在渲染线程 join。 */
    private boolean pollFirstSpellExit(String stage) {
        long now = System.nanoTime();
        String scenario = q235SpellShield ? "Q235 原生盾牌" : renjianRewardLeafImpact ? "人间奖励拈花飞叶 Q40" : renjianRewardSpell ? "人间奖励首技回放"
                : renjianCombatLifecycle ? "人间普通战斗" : renjianBirthSupply ? "人间出身供给"
                : renjianResidence ? "人间居所" : "人间首技";
        if (firstSpellExitStarted == 0) {
            firstSpellExitStarted = now;
            report.add("INFO " + scenario + " " + stage + " 等待区块收敛，存档=" + levelId);
            writeReport();
        }
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (server == null || minecraft.level == null || now - firstSpellExitStarted >= FIRST_SPELL_EXIT_TIMEOUT) {
            blockFirstSpellExit(stage + " 未达到离档条件：最近采样=" + firstSpellLastSample
                    + "，连续样本=" + firstSpellCleanSamples + "，采样已返回="
                    + (firstSpellPendingSample != null && firstSpellPendingSample.isDone()));
            return false;
        }
        if (firstSpellPendingSample != null) {
            if (!firstSpellPendingSample.isDone()) return false;
            ExitSample sample = firstSpellPendingSample.join();
            firstSpellLastSample = sample;
            if (!sample.settled()) {
                firstSpellCleanSamples = 0;
            } else if (sample.serverTick() != firstSpellLastServerTick) {
                firstSpellCleanSamples++;
            }
            firstSpellLastServerTick = sample.serverTick();
            if (firstSpellCleanSamples >= 40) {
                report.add("INFO " + scenario + " " + stage + " 连续 40 个不同服务端刻的采样收敛；即将原生断开");
                writeReport();
                firstSpellExitStarted = 0;
                firstSpellCleanSamples = 0;
                firstSpellLastServerTick = -1;
                firstSpellLastSample = null;
                firstSpellPendingSample = null;
                return true;
            }
        }
        firstSpellPendingSample = server.submit(() -> {
            int pending = 0;
            for (ServerLevel level : server.getAllLevels()) pending += level.getChunkSource().getPendingTasksCount();
            return new ExitSample(server.getTickCount(), pending, WorldStopRequeueDiagnostics.readiness(server));
        });
        return false;
    }

    private void blockFirstSpellExit(String reason) {
        firstSpellExitBlocked = true;
        String code = q235SpellShield ? "Q235_SPELL_SHIELD_BLOCKED"
                : renjianRewardLeafImpact ? "REWARD_LEAF_IMPACT_BLOCKED" : renjianRewardSpell ? "REWARD_SPELL_REPLAY_BLOCKED"
                : renjianCombatLifecycle ? "COMBAT_LIFECYCLE_EXIT_BLOCKED"
                : renjianBirthSupply ? "BIRTH_SUPPLY_EXIT_BLOCKED"
                : renjianResidence ? "RESIDENCE_EXIT_BLOCKED" : "FIRST_SPELL_EXIT_BLOCKED";
        report.add("FAIL " + code + " " + reason + "；未请求新的断开或重开");
        writeReport();
    }

    private void writeBreakthroughEvidence() {
        List<String> evidence = new ArrayList<>(report);
        evidence.add("CHECKPOINT FIRST_BREAKTHROUGH_BEFORE_DISCONNECT levelId=" + levelId + " seed=" + FIRST_SPELL_SEED);
        try {
            Files.createDirectories(output);
            Files.write(output.resolve("renjianfirstspell-breakthrough.txt"), evidence, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void completeExit() {
        if (minecraft.level != null) {
            minecraft.level.disconnect();
            minecraft.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")));
            minecraft.setScreen(new TitleScreen());
        }
        report.remove("WAITING_FOR_WORLD_EXIT");
        report.add("DONE");
        writeReport();
        minecraft.stop();
    }

    private void writeReport() {
        try {
            Files.createDirectories(output);
            Files.write(output.resolve("report.txt"), report, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
