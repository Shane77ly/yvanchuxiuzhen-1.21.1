package net.yvanchuxiuzhen.system.storage.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.material.api.MaterialRevealedEntry;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.storage.api.MaterialStorage;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse;
import net.yvanchuxiuzhen.system.storage.content.MaterialBagMenu;

/** 材料袋沿用来源500像素界面、贴图及坐标，复选状态通过当前菜单发送服务端。 */
public final class MaterialBagScreen extends AbstractContainerScreen<MaterialBagMenu> {
    private static final List<String> FILTERS = List.of("cailiaohealth", "cailiaopower", "cailiaojingli", "cailiaoattack", "cailiaoattdef",
            "cailiaomanaatt", "cailiaomanadef", "cailiaosoulatt", "cailiaosouldef");
    private static final List<String> SORTS = List.of("同类相靠", "从大到小", "从小到大");
    private static final List<String> QUALITIES = List.of("次品", "下品", "中品", "上品", "极品");
    private final Checkbox[] options = new Checkbox[11];
    private boolean syncingOptions;
    private int detailRevision;
    private ItemStack detailSample = ItemStack.EMPTY;
    private MaterialWarehouse.Group detailGroup;
    private List<MaterialRevealedEntry> detailEntries = List.of();

    public MaterialBagScreen(MaterialBagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 500;
        imageHeight = 500;
    }

    @Override
    protected void init() {
        super.init();
        button(0, 52, 292, 26, 13, "07");
        button(1, 86, 292, 26, 13, "09");
        button(2, 58, 320, 69, 18, "03");
        for (int index = 0; index < 5; index++) button(3 + index, 231, 235 + index * 14, 100, 14, index == 0 ? "05" : "05" + index);
        for (int index = 0; index < 45; index++) button(8 + index, 116 + index % 5 * 22, 112 + index / 5 * 22, 22, 22,
                index == 0 ? "01" : "01" + index);
        button(53, 3, 119, 40, 18, "11");
        for (int index = 0; index < 9; index++) checkbox(index, 53, 117 + index * 19, FILTERS.get(index));
        checkbox(9, 58, 342, "zidongfangru");
        checkbox(10, 45, 405, "cailiaokouchu");
    }

    private void button(int id, int x, int y, int width, int height, String suffix) {
        addRenderableWidget(new TextureButton(leftPos + x, topPos + y, width, height,
                texture("atlas/imagebutton_cailiaodaianniu" + suffix), width, height * 2, ignored -> send(id)));
    }

    private void checkbox(int id, int x, int y, String key) {
        options[id] = addRenderableWidget(Checkbox.builder(Component.translatable("gui.yvanchuxiuzhen.cailiaodai_gui." + key), font)
                .pos(leftPos + x, topPos + y).selected(menu.option(id)).onValueChange((checkbox, selected) -> {
                    if (!syncingOptions) send(100 + id * 2 + (selected ? 1 : 0));
                }).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        syncingOptions = true;
        for (int index = 0; index < options.length; index++) {
            if (options[index].selected() != menu.option(index)) options[index].onPress();
        }
        syncingOptions = false;
        if (detailRevision != menu.detailRevision()) {
            detailRevision = menu.detailRevision();
            detailSample = menu.selected() == 0 ? ItemStack.EMPTY : menu.slots.get(menu.selected()).getItem().copy();
            String id = BuiltInRegistries.ITEM.getKey(detailSample.getItem()).toString();
            detailGroup = MaterialStorage.snapshot(minecraft.player).group(id);
            detailEntries = Materials.revealedEntries(minecraft.player, id);
        }
    }

    private void send(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + path + ".png");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(texture("cailiaodaigui"), leftPos + 46, topPos + 110, 0, 0, 293, 291, 293, 291);
        graphics.blit(texture("wupinlan"), leftPos + 163, topPos + 312, 0, 0, 176, 94, 176, 94);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MaterialWarehouse warehouse = MaterialStorage.snapshot(minecraft.player);
        graphics.drawString(font, "品种总数:" + warehouse.groups().size(), 58, 363, -1);
        graphics.drawString(font, "类别总数:" + warehouse.styles(), 58, 374, -1);
        graphics.drawString(font, "材料总数:" + warehouse.total(), 58, 386, -1);
        graphics.drawString(font, SORTS.get(warehouse.preferences().sort()), 5, 124, -1);
        graphics.drawString(font, warehouse.preferences().page() + "/" + warehouse.pageMax(), 74, 295, -1);
        ItemStack selected = detailSample;
        String name = "";
        MaterialWarehouse.Group group = null;
        List<MaterialRevealedEntry> entries = List.of();
        if (detailRevision > 0) {
            name = "空";
            if (!selected.isEmpty()) {
                group = detailGroup;
                name = selected.getDisplayName().getString().replace("[", "").replace("]", "") + "(" + selected.getCount() + "级材料)";
                entries = detailEntries;
            }
        }
        graphics.drawString(font, name, 231, 119, -13261);
        for (int index = 0; index < 5; index++) {
            int quality = 4 - index;
            int count = group == null ? 0 : group.count(selected.getCount(), quality);
            String label = count > 0 ? QUALITIES.get(quality) + "材料数量:" + count : "无" + QUALITIES.get(quality) + "材料";
            graphics.drawString(font, detailRevision == 0 ? "" : label, 234, 238 + index * 14, -1);
        }
        for (int index = 0; index < 7; index++) {
            Component label = Component.literal(selected.isEmpty() ? " " : "§4暂未鉴定");
            if (index < entries.size()) {
                MaterialRevealedEntry entry = entries.get(index);
                label = Component.translatable(entry.open() ? "enchantment." + entry.namespace() + "." + entry.name()
                        : "attribute.yvanchuxiuzhen." + entry.name());
            }
            graphics.drawString(font, label, 231, 132 + index * 12, -6697729);
        }
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.cailiaodai_gui.label_gou_xuan_hou_lian_qi_he_jian_ding_hui_you_xian_kou_chu_cai_liao_dai_nei_de_cai_liao"), 45, 429, -3355444);
        graphics.drawString(font, Component.translatable("gui.yvanchuxiuzhen.cailiaodai_gui.label_jian_ding_shi_hui_you_xian_kou_chu_di_deng_ji_qie_di_pin_jie_de_cai_liao_lian_qi_ding_ze_hui_zhi_jie_xuan_ze_gao_deng_ji_qie"), 45, 441, -3355444);
    }
}
