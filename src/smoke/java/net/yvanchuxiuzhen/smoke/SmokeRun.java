package net.yvanchuxiuzhen.smoke;

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
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;
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
import net.minecraft.world.flag.FeatureFlags;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import net.yvanchuxiuzhen.system.xiulian.XiulianRules;

/**
 * 客户端冒烟检查：建一个创造模式世界，在高空平台放置本 Mod 的全部方块状态，核对每个方块与物品有模型和名称，
 * 加载全部结构模板（旧数据版本在加载时升级）并摆放方块实体最多的几个，记录调色板中的方块名；
 * 另对花、双格植物与每套盔甲的穿戴效果各拍一张近景，再打开创造模式物品栏并逐个切换创造栏并截图；
 * 最后依次做各系统的实机检查（{@link RelationCheck}、{@link AttributeCheck}、{@link XiulianCheck}，各含死亡重生与退出重进），写出报告并退出。
 * 系统属性 yvanchuxiuzhen.smoke.only 为逗号分隔的系统标识（如 relation,attribute,xiulian）时，进入世界后只做这些系统的检查。
 * 进程中途崩溃时不会写出 DONE 行。
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
                case 9 -> waitThen(20, this::finish);
                default -> {
                }
            }
        } catch (Exception exception) {
            report.add("FAIL harness " + exception);
            finish();
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
        if (minecraft.screen instanceof TitleScreen && minecraft.getOverlay() == null) {
            // 关闭首次进入的出身界面，免得内容检查期间弹出；修炼检查中再开启。
            GameRules rules = new GameRules();
            rules.getRule(XiulianRules.BIRTH_SELECTION).set(false, null);
            LevelSettings settings = new LevelSettings("smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                    rules, WorldDataConfiguration.DEFAULT);
            levelId = "smoke" + System.currentTimeMillis();
            minecraft.createWorldOpenFlows().createFreshLevel(levelId, settings,
                    WorldOptions.defaultWithRandomSeed(), WorldPresets::createNormalWorldDimensions, minecraft.screen);
            next();
        }
    }

    private void waitForWorld() {
        if (minecraft.player != null && minecraft.level != null && minecraft.getSingleplayerServer() != null
                && ticks - phaseStart > 100) {
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
            List<String> names = only.isEmpty() ? List.of("relation", "attribute", "xiulian") : List.of(only.split(","));
            systemChecks = new ArrayList<>();
            for (String name : names) {
                systemChecks.addAll(switch (name) {
                    case "relation" -> List.of(new RelationCheck(report, levelId, this::shot));
                    case "attribute" -> List.of(new AttributeCheck(report, levelId));
                    case "xiulian" -> List.of(new XiulianCheck(report, levelId, this::shot), new XiulianProgressCheck(report, levelId, this::shot),
                            new DantianCheck(report, levelId, this::shot));
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
        phase = 99;
        try {
            Files.createDirectories(output);
            report.add((minecraft.mouseHandler.isMouseGrabbed() ? "FAIL" : "INFO") + " 检查期间不捕获鼠标");
            report.add("DONE");
            Files.write(output.resolve("report.txt"), report, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        minecraft.stop();
    }
}
