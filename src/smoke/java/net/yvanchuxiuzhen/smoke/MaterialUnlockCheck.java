package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.material.MaterialLibrary;
import net.yvanchuxiuzhen.system.material.api.MaterialDefinition;
import net.yvanchuxiuzhen.system.material.api.MaterialRevealedEntry;
import net.yvanchuxiuzhen.system.material.client.MaterialAnalysisScreen;
import net.yvanchuxiuzhen.system.material.content.MaterialAnalysisMenu;
import net.yvanchuxiuzhen.system.material.data.MaterialAnalysis;
import net.yvanchuxiuzhen.system.material.data.MaterialData;
import net.yvanchuxiuzhen.system.material.network.MaterialAnalysisButtonPayload;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse;
import net.yvanchuxiuzhen.system.storage.data.StorageData;

/** 材料管理解锁、刷新缓存及普通库存和输入槽扣料检查；不覆盖多人逐玩家提示。 */
final class MaterialUnlockCheck extends SystemCheck {
    private static final String ITEM = "yvanchuxiuzhen:cailiao_001";
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private MaterialAnalysis original;
    private MaterialWarehouse warehouse;
    private MaterialAnalysis beforeInsufficient;

    MaterialUnlockCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("材料管理解锁", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step > 0 && local < 10) return false;
        switch (step) {
            case 0 -> onServer(player -> {
                verifyDataSemantics();
                player.closeContainer();
                original = MaterialLibrary.analysis(player);
                warehouse = player.getData(StorageData.WAREHOUSE);
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    inventory.add(player.getInventory().getItem(slot).copy());
                }
                player.setData(StorageData.WAREHOUSE, MaterialWarehouse.EMPTY);
                player.setData(MaterialData.ANALYSIS, MaterialAnalysis.EMPTY);
                BlockPos pos = player.blockPosition().above(32);
                player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new MaterialAnalysisMenu(id, inventory, pos),
                        Component.literal("千材录")), pos);
                MaterialAnalysisMenu menu = (MaterialAnalysisMenu) player.containerMenu;
                menu.getSlot(0).set(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ITEM))));
                menu.clickMenuButton(player, 0);
            });
            case 1 -> {
                if (!opened()) return true;
                MaterialAnalysisMenu menu = (MaterialAnalysisMenu) minecraft.player.containerMenu;
                expect("命令前客户端显示进度为零", 0, menu.selectedProgress());
                expectTrue("命令前没有已显示效果", menu.selectedEntries().isEmpty());
                screenshots.take("material-unlock-before.png");
                onServer(player -> player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(1),
                        "yvanchuxitong " + player.getDisplayName().getString() + " unlock cailiao true"));
            }
            case 2 -> {
                if (!opened()) return true;
                onServer(player -> {
                    MaterialAnalysis analysis = MaterialLibrary.analysis(player);
                    expect("来源完整材料目录", 225, MaterialLibrary.all().size());
                    expectTrue("完整目录逐行写满进度与上限", MaterialLibrary.all().stream().allMatch(definition ->
                            definition.entryCount() == analysis.progress(definition.itemId()) && definition.entryCount() == analysis.maximum(definition.itemId())));
                    int count = MaterialLibrary.all().stream().mapToInt(definition -> analysis.revealed(definition.itemId()).size()).sum();
                    expect("来源完整有序效果总数", 1451, count);
                    expectTrue("管理解锁没有设置异火初始化或全局特殊标记", !analysis.yihuoInitialized() && analysis.specialTraits().isEmpty());
                    expect("服务端菜单仍保留命令前的缓存", 0, ((MaterialAnalysisMenu) player.containerMenu).selectedProgress());
                    expect("管理解锁不扣输入材料", 1, player.containerMenu.getSlot(0).getItem().getCount());
                });
                MaterialAnalysisMenu menu = (MaterialAnalysisMenu) minecraft.player.containerMenu;
                boolean unlocked = MaterialLibrary.analysis(minecraft.player).progress(ITEM) > 0;
                expectTrue("永久辨识同步给客户端", unlocked);
                if (!unlocked) {
                    onServer(this::restore);
                    finish();
                    return true;
                }
                expect("永久附件更新后当前菜单进度仍为零", 0, menu.selectedProgress());
                expectTrue("永久附件更新后当前菜单效果仍为空", menu.selectedEntries().isEmpty());
                PacketDistributor.sendToServer(new MaterialAnalysisButtonPayload(menu.containerId, 0));
            }
            case 3 -> {
                if (!opened()) return true;
                MaterialAnalysisMenu menu = (MaterialAnalysisMenu) minecraft.player.containerMenu;
                MaterialAnalysis analysis = MaterialLibrary.analysis(minecraft.player);
                expect("原生刷新消息更新显示进度", analysis.progress(ITEM), menu.selectedProgress());
                expect("原生刷新消息更新显示上限", analysis.maximum(ITEM), menu.selectedMaximum());
                expect("原生刷新消息更新已揭示效果", analysis.revealed(ITEM), menu.selectedEntries());
                expectTrue("管理解锁介绍同步并随刷新保留", !menu.selectedEntries().isEmpty() && menu.selectedEntries().getFirst().description().startsWith("增加"));
                screenshots.take("material-unlock-after.png");
                onServer(player -> {
                    MaterialDefinition definition = MaterialLibrary.definition(ITEM);
                    MaterialAnalysis seeded = MaterialAnalysis.EMPTY.withMaximum(ITEM, definition.entryCount())
                            .reveal(ITEM, definition.entries().getFirst());
                    player.setData(MaterialData.ANALYSIS, seeded);
                    player.syncData(MaterialData.ANALYSIS);
                    player.getInventory().clearContent();
                    player.getInventory().setItem(0, material(3));
                    MaterialAnalysisMenu serverMenu = (MaterialAnalysisMenu) player.containerMenu;
                    serverMenu.getSlot(0).set(material(4));
                    serverMenu.clickMenuButton(player, 0);
                    serverMenu.broadcastChanges();
                });
            }
            case 4 -> {
                if (!opened()) return true;
                expectDebit("普通库存扣料前", 1, 3, 4);
                identify();
            }
            case 5 -> {
                if (!opened()) return true;
                expectDebit("成本2先扣普通库存且输入不动", 2, 1, 4);
                identify();
            }
            case 6 -> {
                if (!opened()) return true;
                expectDebit("成本4联合扣普通库存1与输入3", 3, 0, 1);
                onServer(player -> beforeInsufficient = MaterialLibrary.analysis(player));
                identify();
            }
            case 7 -> {
                if (!opened()) return true;
                expectDebit("成本6不足时库存与进度不变", 3, 0, 1);
                onServer(player -> {
                    expect("不足拒绝不改永久辨识记录", beforeInsufficient, MaterialLibrary.analysis(player));
                    player.containerMenu.getSlot(0).set(material(7));
                    player.containerMenu.broadcastChanges();
                });
                identify();
            }
            case 8 -> {
                if (!opened()) return true;
                expectDebit("成本6仅扣输入槽并保留样本1", 4, 0, 1);
                onServer(this::restore);
                finish();
            }
            default -> throw new IllegalStateException("未知材料管理解锁检查步骤 " + step);
        }
        return true;
    }

    private void identify() {
        PacketDistributor.sendToServer(new MaterialAnalysisButtonPayload(minecraft.player.containerMenu.containerId, 1));
    }

    private void verifyDataSemantics() {
        MaterialAnalysis unlocked = MaterialAnalysis.EMPTY.unlock(definition(List.of(
                new MaterialDefinition.Entry("attribute", "healthnum", "4"),
                new MaterialDefinition.Entry("teshu", "gold", "1"),
                new MaterialDefinition.Entry("minecraft", "unbreaking", "2"))));
        expect("管理解锁按来源分支写入介绍与附魔标记", List.of(
                new MaterialRevealedEntry("", "healthnum", false, "增加4点"),
                new MaterialRevealedEntry("teshu", "gold", true, "附魔增加1点"),
                new MaterialRevealedEntry("minecraft", "unbreaking", true, "附魔增加2点")), unlocked.revealed(ITEM));
        expect("永久介绍与标记经过NBT序列化保留", unlocked, MaterialAnalysis.CODEC.parse(NbtOps.INSTANCE,
                MaterialAnalysis.CODEC.encodeStart(NbtOps.INSTANCE, unlocked).getOrThrow()).getOrThrow());
        MaterialRevealedEntry originalEntry = new MaterialRevealedEntry("prior", "old", true, "原介绍");
        MaterialRevealedEntry trailing = new MaterialRevealedEntry("minecraft", "mending", true, "尾项");
        MaterialAnalysis originalData = new MaterialAnalysis(Map.of(ITEM, 2), Map.of(ITEM, 2),
                Map.of(ITEM, List.of(originalEntry, trailing)), List.of("existing"), true);
        MaterialAnalysis replaced = originalData.unlock(definition(List.of(new MaterialDefinition.Entry("attribute", "healthnum", "4"))));
        expect("覆盖定义同时保留来源残留前缀附魔与尾项", List.of(
                new MaterialRevealedEntry("prior", "healthnum", true, "增加4点"), trailing), replaced.revealed().get(ITEM));
        expectTrue("覆盖定义重写进度且保留已有全局标记", replaced.progress(ITEM) == 1 && replaced.maximum(ITEM) == 1
                && replaced.specialTraits().equals(List.of("existing")) && replaced.yihuoInitialized());
        expect("解锁生成新值且不修改原记录", originalEntry, originalData.revealed(ITEM).getFirst());
        MaterialAnalysis natural = MaterialAnalysis.EMPTY.reveal(ITEM, new MaterialDefinition.Entry("teshu", "gold", "1"));
        expect("自然特殊效果辨识不采用管理解锁介绍", new MaterialRevealedEntry("teshu", "gold", false, ""), natural.revealed(ITEM).getFirst());
        expect("自然辨识写入全局特殊效果", List.of("gold"), natural.specialTraits());
    }

    private static MaterialDefinition definition(List<MaterialDefinition.Entry> entries) {
        return new MaterialDefinition(ITEM, Optional.empty(), Optional.empty(), List.of(), List.of(), List.of(), entries);
    }

    private void expectDebit(String label, int progress, int ordinary, int input) {
        onServer(player -> {
            expect(label + "：服务端进度", progress, MaterialLibrary.analysis(player).progress(ITEM));
            expect(label + "：服务端普通库存", ordinary, inventoryCount(player.getInventory()));
            expect(label + "：服务端输入槽", input, player.containerMenu.getSlot(0).getItem().getCount());
            expect(label + "：材料仓关闭且未消费", MaterialWarehouse.EMPTY, player.getData(StorageData.WAREHOUSE));
        });
        MaterialAnalysisMenu menu = (MaterialAnalysisMenu) minecraft.player.containerMenu;
        MaterialAnalysis analysis = MaterialLibrary.analysis(minecraft.player);
        expect(label + "：本人同步进度", progress, analysis.progress(ITEM));
        expect(label + "：客户端普通库存", ordinary, inventoryCount(minecraft.player.getInventory()));
        expect(label + "：客户端输入槽", input, menu.getSlot(0).getItem().getCount());
        expect(label + "：刷新后的显示进度", progress, menu.selectedProgress());
        expect(label + "：刷新后的显示效果", analysis.revealed(ITEM), menu.selectedEntries());
    }

    private static ItemStack material(int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ITEM)), count);
    }

    private static int inventoryCount(Inventory inventory) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ITEM)))) count += stack.getCount();
        }
        return count;
    }

    private boolean opened() {
        boolean opened = minecraft.screen instanceof MaterialAnalysisScreen;
        expectTrue("千材录客户端菜单已打开", opened);
        if (!opened) {
            onServer(this::restore);
            finish();
        }
        return opened;
    }

    private void restore(ServerPlayer player) {
        if (player.containerMenu instanceof MaterialAnalysisMenu menu) menu.getSlot(0).set(ItemStack.EMPTY);
        player.closeContainer();
        player.setData(MaterialData.ANALYSIS, original);
        player.syncData(MaterialData.ANALYSIS);
        player.setData(StorageData.WAREHOUSE, warehouse);
        player.syncData(StorageData.WAREHOUSE);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.inventoryMenu.broadcastChanges();
    }
}
