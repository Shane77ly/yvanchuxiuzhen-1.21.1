package net.yvanchuxiuzhen.system.economy.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.client.TextureButton;
import net.yvanchuxiuzhen.system.economy.EconomyLayout;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.BankDisplay;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;
import net.yvanchuxiuzhen.system.economy.content.CommerceMenu;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntity;
import net.yvanchuxiuzhen.system.economy.network.CommerceButtonPayload;

/** 七页来源经济界面共用提取几何和只读状态；拍卖现场按钮保持来源空回调。 */
public final class CommerceScreen extends AbstractContainerScreen<CommerceMenu> {
    private final Player player;
    private final EconomyLayout.Page layout;
    public CommerceScreen(CommerceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        player = inventory.player;
        layout = EconomyLayout.page(menu.sourcePage());
        imageWidth = layout.width();
        imageHeight = layout.height();
    }

    @Override protected void init() {
        super.init();
        for (EconomyLayout.ImageButton source : layout.buttons()) {
            TextureButton button = new TextureButton(leftPos + source.x(), topPos + source.y(), source.width(), source.height(),
                    texture(source.texture()), source.texture_width(), source.texture_height(), pressed -> {
                        if (source.operation() >= 0) PacketDistributor.sendToServer(new CommerceButtonPayload(menu.containerId, source.operation()));
                    });
            button.setMessage(Component.translatable("gui.yvanchuxiuzhen.commerce.action"));
            addRenderableWidget(button);
        }
        if (layout.plain_buttons() != null) {
            for (EconomyLayout.PlainButton source : layout.plain_buttons()) {
                addRenderableWidget(Button.builder(Component.translatable(source.key()), pressed -> {
                    if (source.operation() >= 0) PacketDistributor.sendToServer(new CommerceButtonPayload(menu.containerId, source.operation()));
                }).bounds(leftPos + source.x(), topPos + source.y(), source.width(), source.height()).build());
            }
        }
        if (menu.kind() == CommerceMenu.Kind.AUCTION) {
            Component prompt = Component.translatable("gui.yvanchuxiuzhen.paimaihang.jiage");
            EditBox price = new EditBox(font, leftPos + 335, topPos + 344, 120, 20, prompt);
            price.setMaxLength(32767);
            price.setSuggestion(prompt.getString());
            price.setResponder(value -> price.setSuggestion(value.isEmpty() ? prompt.getString() : null));
            addRenderableWidget(price);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (EconomyLayout.Layer layer : layout.layers()) graphics.blit(texture(layer.texture()), leftPos + layer.x(), topPos + layer.y(), 0, 0,
                layer.width(), layer.height(), layer.texture_width(), layer.texture_height());
        RenderSystem.disableBlend();
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        CompoundTag session = EconomyMenus.snapshot(player);
        BlockPos pos = menu.kind() == CommerceMenu.Kind.AUCTION ? new BlockPos(session.getInt("paiblockX"), session.getInt("paiblockY"), session.getInt("paiblockZ")) : menu.position();
        CompoundTag block = player.level().getBlockEntity(pos) instanceof EconomyBlockEntity economy ? economy.state() : new CompoundTag();
        for (EconomyLayout.Label source : layout.labels()) {
            Component text = source.key().isEmpty() ? Component.literal(label(source.procedure(), session, block)) : Component.translatable(source.key());
            graphics.drawString(font, text, source.x(), source.y(), source.color(), false);
        }
    }

    private String label(String procedure, CompoundTag session, CompoundTag block) {
        if (procedure.startsWith("Shopnametxt") || procedure.startsWith("Shopjiagetxt")) {
            int index = Integer.parseInt(procedure.substring(procedure.length() - 2)) - 1;
            String suffix = String.format(Locale.ROOT, "%02d", index);
            String value = session.getString((procedure.startsWith("Shopnametxt") ? "shopnametxt" : "shopjiagetxt") + suffix);
            if (!procedure.startsWith("Shopnametxt") || menu.kind() != CommerceMenu.Kind.SHOP) return value;
            var item = menu.getSlot(index).getItem();
            if (item.isEmpty()) return "";
            var products = EconomyMenus.shopCatalog(player).products();
            int productIndex = session.getInt("shoppage") * 12 + index;
            if (productIndex >= 0 && productIndex < products.size()) {
                ShopCatalogDefinition.Product product = products.get(productIndex);
                return item.getHoverName().getString() + "(" + new DecimalFormat("0.##").format(product.price()) + ")";
            }
            return item.getHoverName().getString();
        }
        if (procedure.startsWith("Banktxtlingshi")) {
            BankDisplay display = BankAccounts.display(player);
            if (!display.ready()) return "";
            int tier = Integer.parseInt(procedure.substring(procedure.length() - 2));
            int amount = display.amounts().balance(tier);
            return amount >= 1 ? Component.translatable("item.yvanchuxiuzhen.cailiao_" + (190 + tier)).getString() + amount + "枚" : "无";
        }
        if (procedure.startsWith("Xianshiwenben")) return block.getString("xianshiwenben" + procedure.substring(13));
        return switch (procedure) {
            case "ShopPageTxt" -> Integer.toString(session.getInt("shoppage") + 1);
            case "Storenametxt" -> session.getString("storename");
            case "Banktgoumaili" -> BankAccounts.display(player).ready() ? "总购买力：" + new DecimalFormat("0").format(BankAccounts.display(player).amounts().purchasingPower()) : "";
            case "Shougoushuoming01" -> session.getString("bankceshi1");
            case "Shougoushuoming02" -> session.getString("bankceshi2");
            case "Shopblockguijiagexianshi" -> "灵石:" + block.getInt("lingshineed");
            case "Shopblockshengyv" -> "剩余商品:" + block.getInt("shengyvshangpin");
            case "Shopblockshuaxinshuaxin" -> block.getString("shuaxinshuaxin");
            case "Shopdanrenxiangouxianshi" -> "每个玩家限购数量:" + (block.getInt("danrenxiangou") >= 1 ? block.getInt("danrenxiangou") : "无限制");
            case "Shopshuaxintickxianshi" -> "商店刷新时间（生存玩家请忽略）:" + (block.getInt("shuaxintick") == 0 ? "不刷新" : block.getInt("shuaxintick"));
            case "Paimaihuiname" -> block.getString("paimaihuiname");
            case "PaimaiTuoguan" -> block.getBoolean("tuoguan") ? "全自动" : "自营";
            case "Paimaimoshi" -> block.getString("paimaimoshi");
            case "Paimaidengji", "Paimaidengjixianshi" -> block.getString("paimaidengji");
            case "Paimaihefaxing" -> block.getString("paimaihefaxing");
            case "Paimaiitemnumber" -> "拍卖数量：4<" + block.getInt("paimaiitemnumber") + "<21";
            case "Paimainpcnumberxianshi" -> block.isEmpty() ? "" : "人数：" + block.getInt("paimainpcnumber");
            case "Paimainame" -> block.isEmpty() ? "" : block.getString("paimaihuiname") + switch (block.getInt("paimaijindu")) { case 0 -> "(休会)"; case 1 -> "(准备)"; case 3 -> "(结束)"; default -> ""; };
            default -> throw new IllegalArgumentException("未实现经济标签：" + procedure);
        };
    }

    private static ResourceLocation texture(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/screens/" + path); }
}
