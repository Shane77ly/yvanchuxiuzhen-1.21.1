package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.collection.festival.Caishenye;
import net.yvanchuxiuzhen.collection.festival.FestivalSetup;
import net.yvanchuxiuzhen.system.economy.CommerceTrading;
import net.yvanchuxiuzhen.system.economy.EconomyLayout;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.client.CommerceScreen;
import net.yvanchuxiuzhen.system.economy.content.CommerceMenu;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.client.DialogueScreen;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 受控生成财神后的真实右键、中文对话、红包购买及保存重进检查；不代表财神自然生成入口。 */
final class CaishenyeCheck extends SystemCheck {
    private static final ResourceLocation CAISHEN_ID = ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "caishenye");
    private static final ResourceLocation RED_ENVELOPE_ID = ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "fuqi_06");

    private final Screenshots screenshots;
    private ListTag inventory;
    private ListTag purchasedInventory;
    private CompoundTag dialogue;
    private CompoundTag commerce;
    private UUID caishenyeId;
    private int networkId;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private GameType mode;
    private int selected;
    private boolean shiftKey;
    private boolean prepared;
    private boolean cleaned;

    CaishenyeCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("财神购买红包", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 7) return reopen(local);
        if (step == 8) return reentered(local);
        if (local < 10) return false;
        switch (step) {
            case 0 -> {
                shiftKey = minecraft.options.keyShift.isDown();
                minecraft.options.keyShift.setDown(false);
                onServer(this::prepare);
                prepared = true;
            }
            case 1 -> {
                if (!clientEntity(local)) return false;
                screenshots.take("caishenye_entity.png");
                Entity entity = minecraft.level.getEntity(networkId);
                minecraft.gameMode.interact(minecraft.player, entity, InteractionHand.MAIN_HAND);
            }
            case 2 -> {
                if (!dialogue(local, false, "财神：恭喜发财呀帅哥美女！")) return false;
                screenshots.take("caishenye_dialogue_first.png");
                return pressButton("继续对话");
            }
            case 3 -> {
                if (!dialogue(local, true, "来看看新年新获吗？")) return false;
                onServer(player -> {
                    CompoundTag state = DialogueActions.snapshot(player);
                    expect("财神首段选择提示中文", "来看看新年新获吗？", state.getString("TXTxianshi"));
                    expect("财神逛逛商店按钮中文", "逛逛商店", state.getString("daantxt1"));
                    expect("财神逛逛商店目标步骤", 3, state.getInt("daan1"));
                    expectTrue("财神对话投影同步", state.equals(DialogueActions.snapshot(minecraft.player)));
                });
                screenshots.take("caishenye_dialogue_choices.png");
                return pressButton("逛逛商店");
            }
            case 4 -> {
                if (!shop(local)) return false;
                screenshots.take("caishenye_shop.png");
                return pressCommerceSelection(0);
            }
            case 5 -> {
                if (!purchase(local)) return false;
                screenshots.take("caishenye_shop_purchased.png");
                minecraft.player.closeContainer();
            }
            case 6 -> {
                if (!closed(local)) return false;
                onServer(player -> purchasedInventory = player.getInventory().save(new ListTag()));
                leaveWorld();
            }
            case 9 -> {
                onServer(player -> {
                    expect("保存重进仍持有大红包", 1, count(player, "fuqi_06"));
                    expect("保存重进一文钱币仍已扣除", 0, count(player, "money_01"));
                    expectTrue("保存重进保留购买后库存", purchasedInventory.equals(player.getInventory().save(new ListTag())));
                    cleanup(player);
                });
                cleaned = true;
                minecraft.options.keyShift.setDown(shiftKey);
                finish();
            }
            default -> throw new IllegalStateException("未知财神检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        inventory = player.getInventory().save(new ListTag());
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        dialogue = DialogueActions.snapshot(player);
        commerce = EconomyMenus.snapshot(player);
        player.closeContainer();
        player.getInventory().clearContent();
        player.getInventory().setItem(0, item("money_01").copyWithCount(20));
        player.getInventory().selected = 0;
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);

        EntityType<Caishenye> type = FestivalSetup.caishenyeType().get();
        Caishenye entity = type.spawn(player.serverLevel(), player.blockPosition().east(2), MobSpawnType.COMMAND);
        if (entity == null) throw new IllegalStateException("受控生成财神失败");
        caishenyeId = entity.getUUID();
        networkId = entity.getId();
        player.connection.teleport(entity.getX() - 2.0, entity.getY(), entity.getZ(), 0, 0);
        player.inventoryMenu.broadcastChanges();
        expect("受控财神实体注册ID", CAISHEN_ID.toString(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    private boolean clientEntity(int local) {
        boolean[] serverReady = {false};
        onServer(player -> {
            Entity entity = player.serverLevel().getEntity(caishenyeId);
            if (entity instanceof Caishenye) {
                serverReady[0] = true;
                networkId = entity.getId();
            }
        });
        if (serverReady[0] && minecraft.level != null && minecraft.level.getEntity(networkId) instanceof Caishenye) return true;
        if (local > 300) abort("受控财神未同步到客户端");
        return false;
    }

    private boolean dialogue(int local, boolean choices, String text) {
        if (minecraft.screen instanceof DialogueScreen
                && minecraft.player != null
                && minecraft.player.containerMenu instanceof DialogueMenu menu
                && menu.choices == choices
                && DialogueActions.snapshot(minecraft.player).getString("TXTxianshi").equals(text)) return true;
        if (local > 200) abort("未进入预期财神中文对话：" + text);
        return false;
    }

    private boolean shop(int local) {
        if (!(minecraft.screen instanceof CommerceScreen)
                || !(minecraft.player.containerMenu instanceof CommerceMenu menu)
                || menu.kind() != CommerceMenu.Kind.SHOP) {
            if (local > 200) abort("未进入财神中文商店");
            return false;
        }
        expect("财神商店中文标题", "通宝商店", minecraft.screen.getTitle().getString());
        expect("首件商品客户端中文名称", "大红包", minecraft.player.containerMenu.getSlot(0).getItem().getHoverName().getString());
        onServer(player -> {
            CompoundTag state = EconomyMenus.snapshot(player);
            ItemStack first = player.containerMenu.getSlot(0).getItem();
            expect("财神宝库中文店名", "财神宝库", state.getString("storename"));
            expect("财神中文店主", "财神", state.getString("storeowner"));
            expect("首件商品注册ID", RED_ENVELOPE_ID.toString(), BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
            expectTrue("首件商品标签保留来源价格", state.getString("shopnametxt00").endsWith("(20)"));
            expect("首件商品中文价格", "2贯", state.getString("shopjiagetxt00"));
            expectTrue("首件商品实际非空", !first.isEmpty());
        });
        return true;
    }

    private boolean purchase(int local) {
        boolean ready = minecraft.screen instanceof CommerceScreen
                && minecraft.player.containerMenu instanceof CommerceMenu menu
                && menu.kind() == CommerceMenu.Kind.SHOP;
        if (!ready) {
            if (local > 200) abort("购买红包前商店界面已关闭");
            return false;
        }
        boolean[] purchased = {false};
        onServer(player -> purchased[0] = count(player, "fuqi_06") == 1 && count(player, "money_01") == 0
                && coinValue(player) == 0);
        if (purchased[0]) return true;
        if (local > 200) abort("购买后未同时获得大红包并扣除20个money_01");
        return false;
    }

    private boolean closed(int local) {
        if (minecraft.player != null && minecraft.screen == null && minecraft.player.containerMenu == minecraft.player.inventoryMenu) return true;
        if (local > 200) abort("关闭财神商店超时");
        return false;
    }

    private boolean reopen(int local) {
        if (minecraft.screen instanceof TitleScreen && local >= 20) return reopened(local);
        if (local > 600) abort("保存后未回到标题界面并开始重进");
        return false;
    }

    private boolean reentered(int local) {
        if (minecraft.player != null && minecraft.getSingleplayerServer() != null && local >= 100) return true;
        if (local > 1100) abort("保存重进财神场景超时");
        return inWorld(local);
    }

    private boolean pressButton(String message) {
        if (minecraft.screen != null) {
            for (Button button : buttons()) {
                if (button.getMessage().getString().equals(message)) {
                    button.onPress();
                    return true;
                }
            }
        }
        abort("未找到中文按钮：" + message);
        return false;
    }

    private boolean pressCommerceSelection(int operation) {
        if (!(minecraft.player.containerMenu instanceof CommerceMenu menu)) {
            abort("财神商店容器缺少商品选择");
            return false;
        }
        List<EconomyLayout.ImageButton> definitions = EconomyLayout.page(menu.sourcePage()).buttons();
        List<Button> buttons = buttons();
        for (int index = 0; index < definitions.size(); index++) {
            if (definitions.get(index).operation() == operation) {
                if (index >= buttons.size()) {
                    abort("财神商店首件商品按钮未渲染");
                    return false;
                }
                buttons.get(index).onPress();
                return true;
            }
        }
        abort("财神商店缺少首件商品操作");
        return false;
    }

    private List<Button> buttons() {
        if (minecraft.screen == null) return List.of();
        return minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
    }

    private void abort(String reason) {
        fail(reason);
        if (prepared && !cleaned && minecraft.getSingleplayerServer() != null && minecraft.player != null) {
            onServer(this::cleanup);
            cleaned = true;
        }
        if (prepared) minecraft.options.keyShift.setDown(shiftKey);
        finish();
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        Entity entity = player.serverLevel().getEntity(caishenyeId);
        if (entity != null) entity.discard();
        player.getInventory().clearContent();
        player.getInventory().load(inventory);
        player.getInventory().selected = selected;
        player.setData(QuestData.SESSION, dialogue.copy());
        player.setData(CommerceData.SESSION, commerce.copy());
        player.setGameMode(mode);
        player.setShiftKeyDown(false);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }

    private static int count(ServerPlayer player, String name) {
        return player.getInventory().countItem(item(name).getItem());
    }

    private static double coinValue(ServerPlayer player) {
        double value = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            value += CommerceTrading.coinUnit(stack) * stack.getCount();
        }
        return value;
    }

    private static ItemStack item(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", name)));
    }
}
