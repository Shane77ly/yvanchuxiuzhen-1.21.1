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
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
    /** 俯视位置：x 坐标与方块行号。 */
    private static final int[][] VIEWS = {{14, 0}, {50, 0}, {100, 0}, {30, 40}, {70, 80}, {30, 120}, {90, 160}};
    private final Path output;
    private final List<String> report = new ArrayList<>();
    private final List<Closeup> closeups = new ArrayList<>();
    private final Minecraft minecraft = Minecraft.getInstance();
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

    /** 近景截图：相机所在玩家的位置与朝向，以及在服务端线程上的布置动作。 */
    private record Closeup(String name, CameraType camera, double x, double y, double z, float yRot, float xRot,
                           Consumer<ServerPlayer> setup) {
    }

    SmokeRun(String output) {
        this.output = Path.of(output);
    }

    void tick() {
        ticks++;
        try {
            if (finishRequested) {
                waitForSettledExit();
                return;
            }
            switch (phase) {
                case 0 -> waitForTitle();
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
            report.add("FAIL harness " + exception);
            if (finishRequested) {
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

    private void waitForTitle() {
        if (minecraft.getOverlay() == null && minecraft.screen instanceof AccessibilityOnboardingScreen onboarding) {
            report.add("INFO 首次启动完成原生辅助功能引导，继续进入标题页");
            onboarding.onClose();
            return;
        }
        if (minecraft.screen instanceof TitleScreen && minecraft.getOverlay() == null) {
            // 关闭首次进入的出身界面，免得内容检查期间弹出；修炼检查中再开启。
            GameRules rules = new GameRules();
            rules.getRule(XiulianRules.BIRTH_SELECTION).set(false, null);
            LevelSettings settings = new LevelSettings("smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                    rules, WorldDataConfiguration.DEFAULT);
            levelId = "smoke" + System.currentTimeMillis();
            minecraft.createWorldOpenFlows().createFreshLevel(levelId, settings,
                    "renjianrandom".equals(System.getProperty("yvanchuxiuzhen.smoke.only"))
                            ? new WorldOptions(RenjianRandomCheck.SEED, true, false) : WorldOptions.defaultWithRandomSeed(),
                    WorldPresets::createNormalWorldDimensions, minecraft.screen);
            next();
        }
    }

    private void waitForWorld() {
        if (minecraft.player != null && minecraft.level != null && minecraft.getSingleplayerServer() != null
                && ticks - phaseStart > 100) {
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
            minecraft.setScreen(null);
            String only = System.getProperty("yvanchuxiuzhen.smoke.only", "");
            List<String> names = only.isEmpty() ? List.of("relation", "attribute", "xiulian", "gongfa", "combat", "skill") : List.of(only.split(","));
            systemChecks = new ArrayList<>();
            for (String name : names) {
                systemChecks.addAll(switch (name) {
                    case "relation" -> List.of(new RelationCheck(report, levelId, this::shot));
                    case "attribute" -> List.of(new AttributeCheck(report, levelId));
                    case "xiulian" -> List.of(new XiulianCheck(report, levelId, this::shot), new XiulianProgressCheck(report, levelId, this::shot),
                            new DantianCheck(report, levelId, this::shot), new BloodPathCheck(report, levelId, this::shot),
                            new XiulianOperationsCheck(report, levelId));
                    case "gongfa" -> List.of(new GongfaCheck(report, levelId), new ComprehensionCheck(report, levelId, this::shot));
                    case "combat" -> List.of(new CombatCheck(report, levelId, this::shot));
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
                    case "equipmentconsumers" -> List.of(new EquipmentConsumersCheck(report, levelId, this::shot));
                    case "benmingcultivation" -> List.of(new BenmingCultivationCheck(report, levelId));
                    case "accessories" -> List.of(new AccessoryCheck(report, levelId, this::shot));
                    case "storage" -> List.of(new StorageCheck(report, levelId, this::shot));
                    case "bank" -> List.of(new BankCheck(report, levelId, this::shot));
                    case "commerce" -> List.of(new CommerceCheck(report, levelId, this::shot));
                    case "spiritstone" -> List.of(new SpiritStoneCheck(report, levelId), new SpiritStoneBundleCheck(report, levelId));
                    case "spiritstonebundle" -> List.of(new SpiritStoneBundleCheck(report, levelId));
                    case "construction" -> List.of(new ConstructionCheck(report, levelId, this::shot));
                    case "towntemplates" -> List.of(new TownTemplatePlacementCheck(report, levelId, this::shot));
                    case "buildinghammer" -> List.of(new BuildingHammerCheck(report, levelId, this::shot));
                    case "dungeon" -> List.of(new DungeonCheck(report, levelId, this::shot));
                    case "structurerelease" -> List.of(new StructureReleaseCheck(report, levelId));
                    case "temporaryteam" -> List.of(new TemporaryTeamCheck(report, levelId, this::shot));
                    case "npcresidents" -> List.of(new NpcResidentsCheck(report, levelId, this::shot));
                    case "npcmanagement" -> List.of(new NpcManagementCheck(report, levelId, this::shot));
                    case "npcmerchanteggs" -> List.of(new NpcMerchantSpawnEggCheck(report, levelId));
                    case "questjournal" -> List.of(new QuestJournalCheck(report, levelId, this::shot));
                    case "questeditor" -> List.of(new QuestTriggerEditorCheck(report, levelId, this::shot));
                    case "guconstitutionharvest" -> List.of(new GuConstitutionHarvestCheck(report, levelId, this::shot));
                    case "festivalremaining" -> List.of(new FestivalRemainingCheck(report, levelId, this::shot));
                    case "doublebang" -> List.of(new DoubleBangFireworkCheck(report, levelId, this::shot));
                    case "qiankun" -> List.of(new QiankunCheck(report, levelId, this::shot));
                    case "npc08" -> List.of(new NpcFlyingShuttleCheck(report, levelId, this::shot));
                    case "npc_underworld" -> List.of(new NpcUnderworldCheck(report, levelId, this::shot));
                    case "wishlantern" -> List.of(new WishLanternCheck(report, levelId, this::shot));
                    case "rabbitcage" -> List.of(new RabbitCageCheck(report, levelId, this::shot));
                    case "furniturestorage" -> List.of(new FurnitureStorageCheck(report, levelId, this::shot));
                    case "inscribeddecor" -> List.of(new InscribedDecorCheck(report, levelId, this::shot));
                    case "decor_remainder" -> List.of(new DecorRemainderCheck(report, levelId, this::shot));
                    case "animateddecor" -> List.of(new AnimatedDecorCheck(report, levelId, this::shot));
                    case "worldexploration" -> List.of(new WorldExplorationCheck(report, levelId, this::shot));
                    case "dimensionportal" -> List.of(new DimensionPortalCheck(report, levelId, this::shot));
                    case "renjianrandom" -> List.of(new RenjianRandomCheck(report, levelId));
                    case "tianqve" -> List.of(new TianqveWorldgenCheck(report, levelId, this::shot));
                    case "wugui" -> List.of(new WuguiWorldgenCheck(report, levelId, this::shot));
                    case "wuguiportal" -> List.of(new DimensionPortalCheck(report, levelId, this::shot, DimensionPortalSpec.WUGUI));
                    case "renjianportal" -> List.of(new DimensionPortalCheck(report, levelId, this::shot, DimensionPortalSpec.RENJIAN));
                    case "xiuluoportal" -> List.of(new DimensionPortalCheck(report, levelId, this::shot, DimensionPortalSpec.XIULUO));
                    case "questblockrefresh" -> List.of(new QuestBlockRefreshCheck(report, levelId));
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
            case "yaoshouinteraction" -> List.of(new YaoshouInteractionCheck(report, levelId, this::shot));
            case "yaoshou017form" -> List.of(new Yaoshou017FormBagCheck(report, levelId));
            case "boss02axe" -> List.of(new Boss02AxeCheck(report, levelId));
            case "boss02axehit" -> List.of(new Boss02AxeHitCheck(report, levelId));
            case "yaoshouprojectileitems" -> List.of(new YaoshouProjectileItemCheck(report, levelId));
            case "coffinghost" -> List.of(new CoffinGhostCheck(report, levelId, this::shot));
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
                    case "plantsupply" -> List.of(new PlantSupplyCheck(report, levelId));
                    case "plantsoils" -> List.of(new PlantSoilsCheck(report, levelId));
                    case "hotoil" -> List.of(new HotOilCheck(report, levelId, this::shot));
                    case "willowdrooping" -> List.of(new WillowDroopingCheck(report, levelId));
                    case "corpsegrass" -> List.of(new CorpseGrassCheck(report, levelId));
                    case "fulucooking" -> List.of(new FuluCookingCheck(report, levelId, this::shot));
                    case "naturalsilkchain" -> List.of(new NaturalSilkChainCheck(report, levelId));
                    case "fuluripening" -> List.of(new FuluRipeningCheck(report, levelId));
                    case "fuluspecial" -> List.of(new FuluSpecialCheck(report, levelId, this::shot));
                    case "fulutaming" -> List.of(new FuluTamingCheck(report, levelId, this::shot));
                    case "fulucorpse" -> List.of(new FuluCorpseCheck(report, levelId));
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
