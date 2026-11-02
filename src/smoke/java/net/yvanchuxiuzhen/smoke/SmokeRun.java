package net.yvanchuxiuzhen.smoke;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * 客户端冒烟检查：建一个创造模式世界，在高空平台放置本 Mod 的全部方块状态，核对每个方块与物品有模型和名称，
 * 打开创造模式物品栏并逐个切换创造栏，截图后写出报告并退出。进程中途崩溃时不会写出 DONE 行。
 */
final class SmokeRun {
    private static final String MOD = "yvanchuxiuzhen";
    private final Path output;
    private final List<String> report = new ArrayList<>();
    private final Minecraft minecraft = Minecraft.getInstance();
    private int ticks;
    private int phase;
    private int phaseStart;
    private int tabIndex;
    private List<CreativeModeTab> tabs;
    private Method selectTab;

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
                case 3 -> waitThen(80, this::checkModelsAndNames);
                case 4 -> waitThen(2, this::openCreative);
                case 5 -> cycleTabs();
                case 6 -> waitThen(20, this::finish);
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
            LevelSettings settings = new LevelSettings("smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                    new GameRules(), WorldDataConfiguration.DEFAULT);
            minecraft.createWorldOpenFlows().createFreshLevel("smoke" + System.currentTimeMillis(), settings,
                    WorldOptions.defaultWithRandomSeed(), WorldPresets::createNormalWorldDimensions, minecraft.screen);
            next();
        }
    }

    private void waitForWorld() {
        if (minecraft.player != null && minecraft.level != null && minecraft.getSingleplayerServer() != null
                && ticks - phaseStart > 100) {
            next();
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
        report.add("INFO 已核对方块 " + blocks + " 物品 " + items);
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
        if (tabIndex >= tabs.size()) {
            next();
            return;
        }
        CreativeModeTab tab = tabs.get(tabIndex++);
        selectTab.invoke(minecraft.screen, tab);
        String title = tab.getDisplayName().getString();
        report.add("INFO 创造栏 " + title + " 物品 " + tab.getDisplayItems().size());
        if (title.startsWith("itemGroup.")) {
            report.add("FAIL 创造栏未翻译 " + title);
        }
        if (MOD.equals(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).getNamespace())) {
            Screenshot.grab(minecraft.gameDirectory, "smoke_tab_" + tabIndex + ".png", minecraft.getMainRenderTarget(), message -> {
            });
        }
    }

    private void finish() {
        phase = 99;
        try {
            Files.createDirectories(output);
            report.add("DONE");
            Files.write(output.resolve("report.txt"), report, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        minecraft.stop();
    }
}
