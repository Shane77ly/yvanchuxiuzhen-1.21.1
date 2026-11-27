package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.economy.EconomyLayout;
import net.yvanchuxiuzhen.system.economy.ShopTrading;
import net.yvanchuxiuzhen.system.economy.api.BankAccount;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.BankCards;
import net.yvanchuxiuzhen.system.economy.api.BankDisplay;
import net.yvanchuxiuzhen.system.economy.api.EconomyCatalogs;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;
import net.yvanchuxiuzhen.system.economy.client.BankLoginScreen;
import net.yvanchuxiuzhen.system.economy.client.CommerceScreen;
import net.yvanchuxiuzhen.system.economy.content.CommerceMenu;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntity;
import net.yvanchuxiuzhen.system.economy.data.BankData;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.treasure.api.Caskets;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplates;
import net.yvanchuxiuzhen.system.quest.client.DialogueScreen;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 天地银行四选项自然入口、商品扣款、普通玉购买和基础出售的必要消费检查。 */
final class CommerceCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ItemStack> savedInventory = new ArrayList<>();
    private BankAccount account;
    private BankDisplay display;
    private CompoundTag dialogue;
    private CompoundTag commerce;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private GameType mode;
    private int selected;
    private BlockPos bank;
    private BlockPos stand;
    private BlockState oldBank;
    private BlockState oldFloor;
    private BlockState oldStand;

    CommerceCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("银行自然商贸", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 39) return reopened(local);
        if (step == 40) return inWorld(local);
        if (local < 10) return false;
        switch (step) {
            case 0 -> onServer(player -> {
                for (ShopCatalogDefinition shop : EconomyCatalogs.allList()) {
                    var encoded = ShopCatalogDefinition.CODEC.encodeStart(NbtOps.INSTANCE, shop).getOrThrow();
                    var decoded = ShopCatalogDefinition.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
                    expect("商店目录 Codec 往返：" + shop.id(), shop, decoded);
                }
                for (QuestTemplateDefinition template : QuestTemplates.allList()) {
                    var encoded = QuestTemplateDefinition.CODEC.encodeStart(NbtOps.INSTANCE, template).getOrThrow();
                    var decoded = QuestTemplateDefinition.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
                    expect("对话模板 Codec 往返：" + template.id(), template, decoded);
                }
                prepare(player);
            });
            case 1, 6, 10, 17, 24 -> useBank();
            case 2, 7, 11, 18, 25 -> {
                if (!waitDialogue(local, false, "欢迎使用通宝商盟")) return false;
                dialogueButton(0);
            }
            case 3 -> {
                if (!waitDialogue(local, true, "办理什么业务")) return false;
                onServer(player -> {
                    CompoundTag state = DialogueActions.snapshot(player);
                    expect("第一选项中文", "逛逛商店", state.getString("daantxt1"));
                    expect("第二选项中文", "打开银行", state.getString("daantxt2"));
                    expect("第三选项中文", "购买新通宝玉", state.getString("daantxt3"));
                    expect("第四选项中文", "出售", state.getString("daantxt4"));
                    expect("本人对话投影同步", state, DialogueActions.snapshot(minecraft.player));
                });
                screenshots.take("commerce_bank_choices.png");
                dialogueButton(0);
            }
            case 4 -> {
                if (!waitCommerce(local, CommerceMenu.Kind.SHOP)) return false;
                onServer(player -> {
                    expectTrue("四选项商店实际显示十二件来源商品", player.containerMenu.slots.subList(0, 12).stream()
                            .allMatch(slot -> !slot.getItem().isEmpty()));
                    ItemStack first = player.containerMenu.getSlot(0).getItem();
                    expectTrue("首件商品是带功法组件的天衍神功", first.is(item("book_022").getItem())
                            && Techniques.book(first).map(book -> book.definition().name().equals("天衍神功")).orElse(false));
                    expect("自然商店标题为中文", "通宝商店", minecraft.screen.getTitle().getString());
                });
                screenshots.take("commerce_bank_shop.png");
                commerceButton(0);
            }
            case 5 -> {
                onServer(player -> {
                    expect("首件功法自然购买只发一件", 1, count(player, "book_022"));
                    expect("5242880价格从81枚九级灵石扣80枚", 1, BankAccounts.balance(player, 9));
                    for (int tier = 1; tier < 9; tier++) expect("整额购买不增加低级余额" + tier, 0, BankAccounts.balance(player, tier));
                    expectTrue("购买后商品预览不被取走", !player.containerMenu.getSlot(0).getItem().isEmpty());
                });
                minecraft.player.closeContainer();
            }
            case 8 -> {
                if (!waitDialogue(local, true, "办理什么业务")) return false;
                dialogueButton(1);
            }
            case 9 -> {
                if (!waitScreen(local, minecraft.screen instanceof BankLoginScreen, "银行登录")) return false;
                onServer(player -> expect("自然银行选项打开登录页", "银行登录", minecraft.screen.getTitle().getString()));
                minecraft.player.closeContainer();
                onServer(player -> stageStones(player, 9));
            }
            case 12, 19 -> {
                if (!waitDialogue(local, true, "办理什么业务")) return false;
                dialogueButton(2);
            }
            case 13, 20 -> {
                if (!waitDialogue(local, false, "购买一个新通宝玉需要10枚")) return false;
                dialogueButton(0);
            }
            case 14, 21 -> {
                if (!waitDialogue(local, true, "确认需要购买")) return false;
                dialogueButton(0);
            }
            case 15, 22 -> {
                if (!waitDialogue(local, false, "购买通宝玉需要10枚")) return false;
                dialogueButton(0);
            }
            case 16 -> {
                if (!waitDialogue(local, false, "不足10枚")) return false;
                onServer(player -> {
                    expect("不足十枚时不扣已有灵石", 9, count(player, "cailiao_191"));
                    expect("不足十枚时不发普通玉", 0, count(player, "ling_12"));
                });
                screenshots.take("commerce_card_insufficient.png");
                dialogueButton(0);
                onServer(player -> stageStones(player, 12));
            }
            case 23 -> {
                if (!waitDialogue(local, false, "恭喜获得新通宝玉")) return false;
                onServer(player -> {
                    expect("购买普通玉实际扣十枚随身一级灵石", 2, count(player, "cailiao_191"));
                    expect("成功购买只发一枚普通玉", 1, count(player, "ling_12"));
                    ItemStack card = player.getInventory().items.stream().filter(stack -> stack.is(item("ling_12").getItem())).findFirst().orElseThrow();
                    expect("新玉尚未认主", "", BankCards.read(card).masterUUID());
                    expect("购买普通玉不扣账户九级余额", 1, BankAccounts.balance(player, 9));
                });
                screenshots.take("commerce_card_purchased.png");
                dialogueButton(0);
            }
            case 26 -> {
                if (!waitDialogue(local, true, "办理什么业务")) return false;
                dialogueButton(3);
            }
            case 27 -> {
                if (!waitCommerce(local, CommerceMenu.Kind.STONE_SALE)) return false;
                onServer(player -> {
                    expect("银行出售页为两槽加玩家库存", 38, player.containerMenu.slots.size());
                    ItemStack card = item("ling_12");
                    BankCards.bind(player, card);
                    player.containerMenu.getSlot(0).set(card);
                    player.containerMenu.getSlot(1).set(item("cailiao_191").copyWithCount(3));
                    player.containerMenu.broadcastChanges();
                });
                commerceButton(3);
            }
            case 28 -> {
                onServer(player -> expectTrue("三枚灵石估价", EconomyMenus.snapshot(player).getString("bankceshi2").contains("一级灵石3枚")));
                commerceButton(2);
            }
            case 29 -> {
                onServer(player -> {
                    expect("出售所得存入本人通宝玉账户", 3, BankAccounts.balance(player, 1));
                    expectTrue("售出后货物槽已清空", player.containerMenu.getSlot(1).getItem().isEmpty());
                    expect("灵石收购不发钱币", 0, count(player, "money_01"));
                });
                minecraft.player.closeContainer();
            }
            case 30 -> onServer(player -> EconomyMenus.open(player, player.blockPosition().above(), "凡物出售"));
            case 31 -> {
                if (!waitCommerce(local, CommerceMenu.Kind.BASIC_SALE)) return false;
                onServer(player -> {
                    expect("凡物出售页为一槽加玩家库存", 37, player.containerMenu.slots.size());
                    player.getInventory().setItem(9, new ItemStack(Items.STONE, 37));
                    player.containerMenu.broadcastChanges();
                });
            }
            case 32 -> inventoryClick(1, ClickType.QUICK_MOVE);
            case 33 -> {
                onServer(player -> expect("真实快速移动将37石头放入出售槽", 37, player.containerMenu.getSlot(0).getItem().getCount()));
                commerceButton(1);
            }
            case 34 -> {
                onServer(player -> expectTrue("基础收购中文估价按来源石头单价一文", EconomyMenus.snapshot(player).getString("bankceshi1").contains("单价1文，总价值为37文")));
                screenshots.take("commerce_basic_sale.png");
                commerceButton(0);
            }
            case 35 -> {
                onServer(player -> {
                    expectTrue("基础出售清空实际输入", player.containerMenu.getSlot(0).getItem().isEmpty());
                    expect("37文拆成三枚十文钱币", 3, count(player, "money_02"));
                    expect("37文保留七枚一文钱币", 7, count(player, "money_01"));
                    expect("基础出售不改灵石账户", 1, BankAccounts.balance(player, 9));
                    player.getInventory().setItem(9, new ItemStack(Items.GLASS, 3));
                    player.containerMenu.broadcastChanges();
                });
            }
            case 36 -> inventoryClick(1, ClickType.QUICK_MOVE);
            case 37 -> minecraft.player.closeContainer();
            case 38 -> {
                onServer(player -> {
                    expect("关闭出售页归还三块未售玻璃", 3, player.getInventory().countItem(Items.GLASS));
                    for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) savedInventory.add(player.getInventory().getItem(slot).copy());
                });
                leaveWorld();
            }
            case 41 -> {
                onServer(player -> {
                    expect("正常保存重进保留灵石出售入账", 3, BankAccounts.balance(player, 1));
                    expect("正常保存重进保留扣款后账户", 1, BankAccounts.balance(player, 9));
                    for (int slot = 0; slot < savedInventory.size(); slot++) {
                        expectTrue("正常重进保留购买/找零/余料库存槽" + slot,
                                ItemStack.matches(savedInventory.get(slot), player.getInventory().getItem(slot)));
                    }
                    expectTrue("登录清除普通对话会话", DialogueActions.snapshot(player).isEmpty());
                    EconomyMenus.setShopCatalog(player, EconomyCatalogs.catalog("preset_n51_64_n320"));
                    BankAccounts.setBalance(player, 1, 1170);
                    EconomyMenus.open(player, player.blockPosition(), CommerceMenu.Kind.SHOP);
                });
            }
            case 42 -> {
                if (!waitCommerce(local, CommerceMenu.Kind.SHOP)) return false;
                commerceButton(10);
            }
            case 43 -> {
                onServer(player -> expect("罩甲护腿扣除270灵石", 900, BankAccounts.balance(player, 1)));
                commerceButton(2);
            }
            case 44 -> {
                onServer(player -> {
                    expect("铁胄甲护腿扣除900灵石", 0, BankAccounts.balance(player, 1));
                    List<ItemStack> bought = player.getInventory().items.stream().filter(stack -> !Caskets.command(stack).isEmpty())
                            .map(Caskets::command).filter(command -> command.startsWith("lianqi "))
                            .map(command -> Equipment.createProduct(player.registryAccess(), List.of(command.substring(7).split(" ")))).toList();
                    for (int tier = 1; tier <= 2; tier++) {
                        var leggings = item("armor_0" + tier + "_leggings").getItem();
                        expect("实购宝匣可产出护腿" + tier, 1L, bought.stream().filter(stack -> stack.is(leggings) && Equipment.product(stack).isPresent()).count());
                    }
                    player.closeContainer();
                    player.level().setBlock(bank, BuiltInRegistries.BLOCK.get(id("shopblock_01")).defaultBlockState(), 3);
                    EconomyBlockEntity shop = (EconomyBlockEntity) player.level().getBlockEntity(bank);
                    ShopTrading.placed(player, shop);
                    shop.state().putInt("cunchulingshi", 25);
                    int beforeClaim = totalBalance(player);
                    ShopTrading.interact(player, shop);
                    expect("Q71 首次领取商铺灵石总价值入账", beforeClaim + 25, totalBalance(player));
                    expect("Q71 领取后清零历史暂存", 0, shop.state().getInt("cunchulingshi"));
                    player.closeContainer();
                    ShopTrading.interact(player, shop);
                    expect("Q71 再次打开商铺不重复入账", beforeClaim + 25, totalBalance(player));
                    cleanup(player);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知商贸检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        account = BankAccounts.snapshot(player);
        display = BankAccounts.display(player);
        dialogue = DialogueActions.snapshot(player);
        commerce = EconomyMenus.snapshot(player);
        bank = player.blockPosition().above(4);
        stand = bank.west(2);
        oldBank = player.level().getBlockState(bank);
        oldFloor = player.level().getBlockState(bank.below());
        oldStand = player.level().getBlockState(stand.below());
        player.level().setBlock(bank.below(), Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(stand.below(), Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(bank, BuiltInRegistries.BLOCK.get(id("tiandiyinhang")).defaultBlockState(), 3);
        player.getInventory().clearContent();
        player.getInventory().selected = 8;
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        minecraft.options.keyShift.setDown(false);
        player.connection.teleport(stand.getX() + .5, stand.getY(), stand.getZ() + .5, -90, 0);
        for (int tier = 1; tier <= 9; tier++) BankAccounts.setBalance(player, tier, tier == 9 ? 81 : 0);
        BankAccounts.refresh(player);
        player.inventoryMenu.broadcastChanges();
    }

    private void stageStones(ServerPlayer player, int amount) {
        player.getInventory().setItem(9, item("cailiao_191").copyWithCount(amount));
        player.inventoryMenu.broadcastChanges();
    }

    private void useBank() {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(bank), Direction.UP, bank, false));
    }

    private boolean waitDialogue(int local, boolean choices, String text) {
        if (!waitScreen(local, minecraft.screen instanceof DialogueScreen && minecraft.player.containerMenu instanceof DialogueMenu menu
                && menu.choices == choices, "对话：" + text)) return false;
        String actual = DialogueActions.snapshot(minecraft.player).getString("TXTxianshi");
        if (actual.contains(text)) return true;
        if (local > 200) {
            fail("对话文本不符，期望含“" + text + "”，实际“" + actual + "”");
            finish();
        }
        return false;
    }

    private boolean waitCommerce(int local, CommerceMenu.Kind kind) {
        return waitScreen(local, minecraft.screen instanceof CommerceScreen && minecraft.player.containerMenu instanceof CommerceMenu menu
                && menu.kind() == kind, "经济页面 " + kind);
    }

    private boolean waitScreen(int local, boolean ready, String expected) {
        if (ready) return true;
        if (local > 200) {
            fail("未进入" + expected + "，当前界面" + (minecraft.screen == null ? "无" : minecraft.screen.getClass().getSimpleName()));
            finish();
        }
        return false;
    }

    private void dialogueButton(int index) {
        buttons().get(index).onPress();
    }

    private void commerceButton(int operation) {
        CommerceMenu menu = (CommerceMenu) minecraft.player.containerMenu;
        List<EconomyLayout.ImageButton> definitions = EconomyLayout.page(menu.sourcePage()).buttons();
        for (int index = 0; index < definitions.size(); index++) {
            if (definitions.get(index).operation() == operation) {
                buttons().get(index).onPress();
                return;
            }
        }
        throw new IllegalStateException("来源页面缺少操作 " + operation);
    }

    private List<Button> buttons() {
        return minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
    }

    private void inventoryClick(int slot, ClickType type) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, type, minecraft.player);
    }

    private static int totalBalance(ServerPlayer player) {
        BankAccount snapshot = BankAccounts.snapshot(player);
        int sum = 0;
        for (int tier = 1; tier <= 9; tier++) sum += snapshot.balance(tier) * BankAccount.weight(tier);
        return sum;
    }

    private static int count(ServerPlayer player, String name) {
        return player.getInventory().countItem(item(name).getItem());
    }

    private static ItemStack item(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(name)));
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", name);
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setData(BankData.ACCOUNT, account);
        player.setData(BankData.DISPLAY, display);
        player.setData(QuestData.SESSION, dialogue);
        player.setData(CommerceData.SESSION, commerce);
        player.level().setBlock(bank, oldBank, 3);
        player.level().setBlock(bank.below(), oldFloor, 3);
        player.level().setBlock(stand.below(), oldStand, 3);
        player.setGameMode(mode);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }
}
