package net.yvanchuxiuzhen.smoke;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.BankUpdates;
import net.yvanchuxiuzhen.system.economy.api.BankAccount;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.BankCard;
import net.yvanchuxiuzhen.system.economy.api.BankCards;
import net.yvanchuxiuzhen.system.economy.api.BankDisplay;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;
import net.yvanchuxiuzhen.system.economy.client.BankLoginScreen;
import net.yvanchuxiuzhen.system.economy.client.BankScreen;
import net.yvanchuxiuzhen.system.economy.content.BankLoginMenu;
import net.yvanchuxiuzhen.system.economy.content.BankMenu;
import net.yvanchuxiuzhen.system.economy.data.BankData;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.quest.api.BankDialogueActions;
import net.yvanchuxiuzhen.system.quest.api.BankDialogueSession;
import net.yvanchuxiuzhen.system.quest.data.BankDialogueData;

/** 银行客户端闭环：认主、入口、真实菜单操作、账户数值和玩家生命周期。 */
final class BankCheck extends SystemCheck {
    private static final String DROP_PREFIX = "r13_bank_";
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private final List<String> clientMessages = new ArrayList<>();
    private final List<UUID> testDrops = new ArrayList<>();
    private final Screenshots screenshots;
    private final Path smokeOutput = Path.of(System.getProperty("yvanchuxiuzhen.smoke"));
    private final String worldId;
    private BankAccount originalAccount;
    private BankMenu originalBankMenu;
    private BankMenu remappedBankMenu;
    private BankDisplay originalDisplay;
    private BankDialogueSession originalSession;
    private GameType originalMode;
    private boolean originalKeepInventory;
    private int originalSelected;
    private String playerId;
    private UUID fullInventoryDrop;
    private BankAccount disconnectAccount;
    private BankDialogueSession disconnectSession;

    BankCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("银行", report, levelId);
        this.screenshots = screenshots;
        worldId = levelId;
        NeoForge.EVENT_BUS.addListener(this::captureChat);
        NeoForge.EVENT_BUS.addListener(this::freezeFixtureDrop);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            if (local < 8) return false;
            onServer(this::prepare);
            return true;
        }
        if (step == 1) {
            if (local < 8) return false;
            onServer(player -> {
                use(player, true);
                String owner = BankCards.read(player.getMainHandItem()).masterUUID();
                expect("普通玉潜行绑定本人", player.getStringUUID(), owner);
                use(player, true);
                expect("重复绑定不覆盖原主人", player.getStringUUID(), BankCards.read(player.getMainHandItem()).masterUUID());
            });
            return true;
        }
        if (step == 2) {
            if (local < 8) return false;
            onServer(player -> {
                ItemStack regular = player.getInventory().getItem(0);
                regular.set(BankData.CARD.get(), new BankCard(player.getStringUUID(), 2.75, 0, 0, 0, 0, 0, 0, 0, 12.2));
                BankAccount before = BankAccounts.snapshot(player);
                use(player, false);
                expect("普通玉查询不改玩家账户", before, BankAccounts.snapshot(player));
            });
            return true;
        }
        if (step == 3) {
            if (local < 12) return false;
            String tierOne = Component.translatable("item.yvanchuxiuzhen.cailiao_191").getString();
            String tierNine = Component.translatable("item.yvanchuxiuzhen.cailiao_199").getString();
            expectTrue("普通玉按卡内double四舍五入并显示一级3枚", clientMessages.stream().anyMatch(message -> message.contains(tierOne + "3枚")));
            expectTrue("普通玉按卡内double四舍五入并显示九级12枚", clientMessages.stream().anyMatch(message -> message.contains(tierNine + "12枚")));
            return true;
        }
        if (step == 4) {
            if (local < 8) return false;
            onServer(player -> {
                ItemStack advanced = item("ling_13");
                advanced.set(BankData.CARD.get(), new BankCard(player.getStringUUID(), 0, 0, 0, 0, 0, 0, 0, 0, 0));
                player.getInventory().setItem(1, advanced);
                player.getInventory().selected = 1;
                use(player, false);
                expect("高级玉真实右键建立来源对话会话", new BankDialogueSession(BankDialogueSession.SCRIPT, 0, true), BankDialogueActions.snapshot(player));
                player.getInventory().selected = 0;
                expect("切换主手后仍保留登录请求", "ling_12", BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath());
            });
            return true;
        }
        if (step == 5) {
            if (local < 12) return false;
            if (!(minecraft.screen instanceof BankLoginScreen)) {
                if (local > 160) {
                    fail("高级玉请求未在END刻打开登录页：" + screenName());
                    finish();
                }
                return false;
            }
            onServer(player -> {
                expect("END刻清理银行对话会话", BankDialogueSession.EMPTY, BankDialogueActions.snapshot(player));
                expectTrue("登录菜单和界面均为银行原生实现", player.containerMenu instanceof BankLoginMenu
                        && minecraft.player.containerMenu instanceof BankLoginMenu);
                expect("登录页中文标题", "银行登录", minecraft.screen.getTitle().getString());
            });
            screenshots.take("bank_login.png");
            return true;
        }
        if (step == 6) {
            if (local < 8) return false;
            onServer(player -> {
                ItemStack foreign = item("ling_12");
                foreign.set(BankData.CARD.get(), new BankCard(UUID.randomUUID().toString(), 0, 0, 0, 0, 0, 0, 0, 0, 0));
                player.getInventory().setItem(9, foreign);
                player.inventoryMenu.broadcastChanges();
            });
            inventoryClick(1, ClickType.QUICK_MOVE);
            return true;
        }
        if (step == 7) {
            if (local < 8) return false;
            clickButton(0);
            return true;
        }
        if (step == 8) {
            if (local < 8) return false;
            onServer(player -> expectTrue("外人普通玉被拒且仍停留在登录页", player.containerMenu instanceof BankLoginMenu
                    && minecraft.screen instanceof BankLoginScreen));
            onServer(player -> {
                player.containerMenu.getSlot(0).set(ItemStack.EMPTY);
                player.getInventory().setItem(10, player.getInventory().getItem(0).copy());
                player.inventoryMenu.broadcastChanges();
            });
            inventoryClick(2, ClickType.QUICK_MOVE);
            return true;
        }
        if (step == 9) {
            if (local < 8) return false;
            clickButton(0);
            return true;
        }
        if (step == 10) {
            if (local < 12) return false;
            onServer(player -> {
                expectTrue("本人普通玉登录后打开11槽银行", player.containerMenu instanceof BankMenu menu && menu.slots.size() == 47
                        && minecraft.player.containerMenu instanceof BankMenu && minecraft.screen instanceof BankScreen);
                expect("登录转移卡数量为一", 1, player.containerMenu.getSlot(0).getItem().getCount());
                for (int tier = 1; tier <= 9; tier++) {
                    expectTrue("银行九档展示槽" + tier, !player.containerMenu.getSlot(tier).getItem().isEmpty());
                }
                expect("本人显示附件反映账户快照", BankAccounts.snapshot(player), BankAccounts.display(player).amounts());
                expectTrue("显示投影同步到本人客户端", BankAccounts.display(minecraft.player).ready());
            });
            screenshots.take("bank_account.png");
            return true;
        }
        if (step == 11) {
            if (local < 8) return false;
            inventoryClick(1, ClickType.PICKUP);
            inventoryClick(1, ClickType.QUICK_MOVE);
            return true;
        }
        if (step == 12) {
            if (local < 8) return false;
            onServer(player -> expectTrue("展示槽真实点击与快速移动均只读", player.containerMenu instanceof BankMenu
                    && player.containerMenu.getSlot(1).getItem().is(item("cailiao_191").getItem())
                    && player.containerMenu.getCarried().isEmpty()
                    && player.getInventory().items.stream().noneMatch(stack -> stack.is(item("cailiao_191").getItem()) && stack.getCount() > 1)));
            onServer(player -> {
                BankAccounts.setBalance(player, 1, 0);
                BankAccounts.setBalance(player, 2, -1);
                BankAccounts.refresh(player);
            });
            clickButton(0);
            clickButton(1);
            return true;
        }
        if (step >= 13 && step < 49) {
            return bankTier(step, local);
        }
        return switch (step) {
            case 49 -> action(local, 8, player -> {
                expectTrue("延迟打开回调现读当前Supplier槽映射", player.containerMenu == remappedBankMenu
                        && remappedBankMenu != null
                        && java.util.stream.IntStream.rangeClosed(1, 9).allMatch(tier -> remappedBankMenu.getSlot(tier).getItem()
                        .is(item("cailiao_19" + tier).getItem()))
                        && java.util.stream.IntStream.rangeClosed(1, 9).allMatch(tier -> originalBankMenu.getSlot(tier).getItem().isEmpty()));
                player.containerMenu = originalBankMenu;
                BankUpdates.opened(player);
            });
            case 50 -> action(local, 8, player -> expectTrue("切回原菜单后新延迟回调填充当前映射", player.containerMenu == originalBankMenu
                    && java.util.stream.IntStream.rangeClosed(1, 9).allMatch(tier -> originalBankMenu.getSlot(tier).getItem()
                    .is(item("cailiao_19" + tier).getItem()))));
            case 51 -> action(local, 8, player -> {
                player.getInventory().clearContent();
                for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
                    player.getInventory().setItem(index, new ItemStack(Items.STONE, 64));
                }
                player.containerMenu.broadcastChanges();
            });
            case 52 -> {
                if (local < 8) yield false;
                clickButton(7);
                yield true;
            }
            case 53 -> action(local, 10, player -> {
                List<ItemEntity> drops = player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(12),
                        entity -> entity.getItem().is(item("cailiao_198").getItem()));
                expectTrue("满背包取款实际生成一枚八级灵石掉落实体", drops.stream().anyMatch(entity -> entity.getItem().getCount() == 1));
                if (!drops.isEmpty()) fullInventoryDrop = drops.get(0).getUUID();
                expect("满背包取款仍扣除账户一枚", 16, BankAccounts.balance(player, 8));
                player.getInventory().clearContent();
                player.containerMenu.getSlot(10).set(named(item("cailiao_194"), 7, DROP_PREFIX + "normal_input"));
                player.containerMenu.broadcastChanges();
            });
            case 54 -> {
                if (local < 8) yield false;
                clickButton(10);
                yield true;
            }
            case 55 -> action(local, 8, player -> {
                expectTrue("正常关闭只返还卡和未存输入", player.containerMenu == player.inventoryMenu
                        && player.getInventory().items.stream().anyMatch(stack -> stack.getItem() == item("ling_12").getItem() && BankCards.ownedBy(stack, player))
                        && player.getInventory().items.stream().anyMatch(stack -> stack.getItem() == item("cailiao_194").getItem() && stack.getCount() == 7));
                expectTrue("正常关闭不把展示币返给玩家", player.getInventory().items.stream()
                        .noneMatch(stack -> stack.getItem() == item("cailiao_191").getItem()));
            });
            case 56 -> action(local, 8, player -> {
                player.getInventory().clearContent();
                player.getInventory().setItem(0, advancedCard(player));
                player.getInventory().selected = 0;
                BankAccounts.setBalance(player, 4, 44);
                BankAccounts.refresh(player);
                player.setData(BankDialogueData.SESSION, new BankDialogueSession(BankDialogueSession.SCRIPT, 1, false));
                player.serverLevel().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, player.server);
                BankMenus.openBank(player, player.blockPosition());
            });
            case 57 -> {
                if (local < 12) yield false;
                onServer(player -> {
                    player.containerMenu.getSlot(0).set(named(ownedCard(player), 1, DROP_PREFIX + "death_card"));
                    player.containerMenu.getSlot(10).set(named(item("cailiao_192"), 5, DROP_PREFIX + "death_input"));
                    player.containerMenu.broadcastChanges();
                    expect("死亡前银行会话为非启动状态", new BankDialogueSession(BankDialogueSession.SCRIPT, 1, false), BankDialogueActions.snapshot(player));
                });
                kill();
                yield true;
            }
            case 58 -> { yield respawned(local); }
            case 59 -> action(local, 80, player -> {
                expect("死亡克隆保留银行账户", 44, BankAccounts.balance(player, 4));
                expect("死亡克隆保留非启动银行会话", new BankDialogueSession(BankDialogueSession.SCRIPT, 1, false), BankDialogueActions.snapshot(player));
                expect("非持久显示投影在死亡克隆后重置", BankDisplay.EMPTY, BankAccounts.display(player));
                expectTrue("keepInventory保留高级通宝玉物品组件", player.getInventory().getItem(0).getItem() == item("ling_13").getItem()
                        && BankCards.ownedBy(player.getInventory().getItem(0), player));
                expectTrue("死亡关闭银行时普通卡与输入币掉落", hasNamedDrop(player.serverLevel(), DROP_PREFIX + "death_card")
                        && hasNamedDrop(player.serverLevel(), DROP_PREFIX + "death_input"));
                player.getInventory().clearContent();
                player.getInventory().setItem(0, advancedCard(player));
                player.getInventory().selected = 0;
                disconnectAccount = new BankAccount(11, 12, 13, 14, 15, 16, 17, 18, 19);
                disconnectSession = new BankDialogueSession(BankDialogueSession.SCRIPT, 1, false);
                player.setData(BankData.ACCOUNT, disconnectAccount);
                player.setData(BankDialogueData.SESSION, disconnectSession);
                BankAccounts.refresh(player);
                BankMenus.openBank(player, player.blockPosition());
            });
            case 60 -> {
                if (local < 12) yield false;
                onServer(player -> {
                    player.containerMenu.getSlot(0).set(named(ownedCard(player), 1, DROP_PREFIX + "logout_card"));
                    player.containerMenu.getSlot(10).set(named(item("cailiao_193"), 5, DROP_PREFIX + "logout_input"));
                    player.containerMenu.broadcastChanges();
                });
                leaveWorld();
                yield true;
            }
            case 61 -> {
                if (!(minecraft.screen instanceof TitleScreen) || local < 20) yield false;
                readSavedPlayerData();
                yield reopened(local);
            }
            case 62 -> {
                if (!inWorld(local)) yield false;
                onServer(player -> {
                    expect("實際退出重進保留銀行帳戶", disconnectAccount, BankAccounts.snapshot(player));
                    expect("登录钩子清空持久对话会话", BankDialogueSession.EMPTY, BankDialogueActions.snapshot(player));
                    expectTrue("玩家原清单保留高级玉和卡组件", player.getInventory().getItem(0).getItem() == item("ling_13").getItem()
                            && BankCards.ownedBy(player.getInventory().getItem(0), player));
                    expectTrue("断线关闭菜单将临时普通卡和输入物掉落", hasNamedDrop(player.serverLevel(), DROP_PREFIX + "logout_card")
                            && hasNamedDrop(player.serverLevel(), DROP_PREFIX + "logout_input"));
                    cleanup(player);
                });
                finish();
                yield true;
            }
            default -> throw new IllegalStateException("未知银行检查步骤 " + step);
        };
    }

    private boolean bankTier(int step, int local) {
        int tier = (step - 13) / 4 + 1;
        int phase = (step - 13) % 4;
        if (local < 8) return false;
        switch (phase) {
            case 0 -> {
                onServer(player -> {
                    if (tier == 1) {
                        expect("零余额取款不扣款", 0, BankAccounts.balance(player, 1));
                        expect("负余额取款不扣款", -1, BankAccounts.balance(player, 2));
                        BankAccounts.setBalance(player, 1, 1);
                        BankAccounts.setBalance(player, 2, 2);
                        BankAccounts.refresh(player);
                    }
                    player.getInventory().setItem(9, item("cailiao_19" + tier));
                    player.getInventory().getItem(9).setCount(tier + 2);
                    player.containerMenu.broadcastChanges();
                });
                inventoryClick(11, ClickType.QUICK_MOVE);
            }
            case 1 -> {
                onServer(player -> expectTrue("" + tier + "级输入只接收当前真实币栈", player.containerMenu.getSlot(10).getItem().is(item("cailiao_19" + tier).getItem())
                        && player.containerMenu.getSlot(10).getItem().getCount() == tier + 2 && player.getInventory().getItem(9).isEmpty()));
                clickButton(9);
            }
            case 2 -> {
                onServer(player -> {
                    expect("存入整栈后账户余额", 2 * tier + 2, BankAccounts.balance(player, tier));
                    expect("存入不会改卡片自身余额", 2.75, BankCards.read(player.containerMenu.getSlot(0).getItem()).balance(1));
                });
                clickButton(tier - 1);
            }
            case 3 -> onServer(player -> {
                int outputIndex = -1;
                ItemStack output = ItemStack.EMPTY;
                for (int index = 0; index < 36; index++) {
                    ItemStack candidate = player.getInventory().getItem(index);
                    if (candidate.is(item("cailiao_19" + tier).getItem())) {
                        outputIndex = index;
                        output = candidate;
                        break;
                    }
                }
                expectTrue(tier + "级取款进入真实玩家背包", outputIndex >= 0);
                if (outputIndex >= 0) {
                    MaterialInstance material = Materials.instance(output);
                    expect(tier + "级取款为单枚", 1, output.getCount());
                    expect(tier + "级取款材料等级", tier * 5, material.level());
                    expect(tier + "级取款材料品质", 4, material.quality());
                    player.getInventory().setItem(outputIndex, ItemStack.EMPTY);
                }
                expect("取款后账户余量", 2 * tier + 1, BankAccounts.balance(player, tier));
                if (tier == 9) {
                    originalBankMenu = (BankMenu) player.containerMenu;
                    for (int displayTier = 1; displayTier <= 9; displayTier++) originalBankMenu.getSlot(displayTier).set(ItemStack.EMPTY);
                    originalBankMenu.broadcastChanges();
                    remappedBankMenu = new BankMenu(originalBankMenu.containerId, player.getInventory(), player.blockPosition());
                    player.containerMenu = remappedBankMenu;
                }
            });
            default -> throw new IllegalStateException("银行级别阶段 " + phase);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        playerId = player.getStringUUID();
        originalSelected = player.getInventory().selected;
        originalMode = player.gameMode.getGameModeForPlayer();
        originalKeepInventory = player.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        originalAccount = BankAccounts.snapshot(player);
        originalDisplay = BankAccounts.display(player);
        originalSession = BankDialogueActions.snapshot(player);
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            originalInventory.add(player.getInventory().getItem(index).copy());
        }
        player.setGameMode(GameType.SURVIVAL);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false, player.server);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, item("ling_12"));
        ItemStack stranger = item("ling_12");
        stranger.set(BankData.CARD.get(), new BankCard(UUID.randomUUID().toString(), 0, 0, 0, 0, 0, 0, 0, 0, 0));
        player.getInventory().setItem(9, stranger);
        player.getInventory().setItem(1, advancedCard(player));
        for (int tier = 1; tier <= 9; tier++) BankAccounts.setBalance(player, tier, tier);
        BankAccounts.refresh(player);
        player.inventoryMenu.broadcastChanges();
        BankAccount initial = new BankAccount(5, 2, 0, 0, 0, 0, 0, 0, 0);
        expect("double价值拆分保留旧余额", new BankAccount(8, 3, 1, 1, 1, 1, 1, 1, 1), initial.creditValue(87383.75));
        expect("不足一枚价值不入账", initial, initial.creditValue(0.75));
        expect("负价值不入账", initial, initial.creditValue(-4));
        BankAccounts.setBalance(player, 1, Integer.MAX_VALUE - 1);
        BankAccounts.depositCount(player, 1, 3);
        expect("存入数量double转int饱和截断", Integer.MAX_VALUE, BankAccounts.balance(player, 1));
        for (int tier = 1; tier <= 9; tier++) BankAccounts.setBalance(player, tier, tier);
        expect("购买力保留九级int乘法溢出", -2147483648.0, new BankAccount(0, 0, 0, 0, 0, 0, 0, 0, 32768).purchasingPower());
        expect("购买力忽略负余额并保留正档溢出", -2147483640.0, new BankAccount(-3, 2, 0, 0, 0, 0, 0, 0, 32768).purchasingPower());
        expect("购买力逐档int乘法后累加double", 2147483655.0, new BankAccount(Integer.MAX_VALUE, 2, 0, 0, 0, 0, 0, 0, 0).purchasingPower());
    }

    private void readSavedPlayerData() {
        try {
            Path levelFile = minecraft.gameDirectory.toPath().resolve("saves").resolve(worldId).resolve("playerdata").resolve(playerId + ".dat");
            if (!Files.isRegularFile(levelFile)) {
                fail("断线后玩家数据文件不存在：" + levelFile);
                return;
            }
            CompoundTag savedPlayerData = NbtIo.readCompressed(levelFile, NbtAccounter.unlimitedHeap());
            CompoundTag attachments = savedPlayerData.getCompound("neoforge:attachments");
            BankAccount account = BankAccount.CODEC.parse(NbtOps.INSTANCE, attachments.get("yvanchuxiuzhen:bank_account")).getOrThrow();
            BankDialogueSession session = BankDialogueSession.CODEC.parse(NbtOps.INSTANCE, attachments.get("yvanchuxiuzhen:quest_bank_dialogue")).getOrThrow();
            expect("断线关闭前已保存账户", disconnectAccount, account);
            expect("断线关闭前已保存非空对话会话", disconnectSession, session);
            ListTag inventory = savedPlayerData.getList("Inventory", Tag.TAG_COMPOUND);
            boolean hasTemporaryCard = false;
            boolean hasTemporaryInput = false;
            for (int index = 0; index < inventory.size(); index++) {
                String id = inventory.getCompound(index).getString("id");
                hasTemporaryCard |= id.equals("yvanchuxiuzhen:ling_12");
                hasTemporaryInput |= id.equals("yvanchuxiuzhen:cailiao_193");
            }
            expectTrue("存盘清单不含仍在菜单的临时卡与输入币", !hasTemporaryCard && !hasTemporaryInput);
            Files.createDirectories(smokeOutput);
            Files.copy(levelFile, smokeOutput.resolve("bank-playerdata-before-disconnect.dat"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("读取断线保存玩家数据失败", exception);
        }
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.getInventory().clearContent();
        for (int index = 0; index < originalInventory.size(); index++) player.getInventory().setItem(index, originalInventory.get(index));
        player.getInventory().selected = originalSelected;
        player.setData(BankData.ACCOUNT, originalAccount);
        player.setData(BankData.DISPLAY, originalDisplay);
        player.setData(BankDialogueData.SESSION, originalSession);
        player.setGameMode(originalMode);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(originalKeepInventory, player.server);
        if (fullInventoryDrop != null && player.serverLevel().getEntity(fullInventoryDrop) != null) player.serverLevel().getEntity(fullInventoryDrop).discard();
        for (UUID uuid : testDrops) {
            if (player.serverLevel().getEntity(uuid) != null) player.serverLevel().getEntity(uuid).discard();
        }
        player.inventoryMenu.broadcastChanges();
    }

    private void captureChat(ClientChatReceivedEvent.System event) {
        clientMessages.add(event.getMessage().getString());
    }

    private void freezeFixtureDrop(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;
        String name = itemEntity.getItem().getHoverName().getString();
        if (!name.startsWith(DROP_PREFIX)) return;
        itemEntity.setNoGravity(true);
        itemEntity.setDeltaMovement(0, 0, 0);
        itemEntity.setPickUpDelay(1200);
        testDrops.add(itemEntity.getUUID());
    }

    private static boolean hasNamedDrop(ServerLevel level, String name) {
        return !level.getEntitiesOfClass(ItemEntity.class, net.minecraft.world.phys.AABB.ofSize(
                        net.minecraft.world.phys.Vec3.atCenterOf(level.getSharedSpawnPos()), 512, 512, 512),
                entity -> entity.getItem().getHoverName().getString().equals(name)).isEmpty();
    }

    private void inventoryClick(int slot, ClickType type) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, type, minecraft.player);
    }

    private void clickButton(int index) {
        if (minecraft.screen == null) throw new IllegalStateException("银行界面已关闭，无法点击按钮 " + index);
        List<Button> buttons = minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
        buttons.get(index).onPress();
    }

    private boolean action(int local, int wait, java.util.function.Consumer<ServerPlayer> action) {
        if (local < wait) return false;
        onServer(action);
        return true;
    }

    private static void use(ServerPlayer player, boolean sneaking) {
        player.setShiftKeyDown(sneaking);
        try {
            player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        } finally {
            player.setShiftKeyDown(false);
        }
    }

    private static ItemStack ownedCard(ServerPlayer player) {
        ItemStack card = item("ling_12");
        card.set(BankData.CARD.get(), new BankCard(player.getStringUUID(), 0, 0, 0, 0, 0, 0, 0, 0, 0));
        return card;
    }

    private static ItemStack advancedCard(ServerPlayer player) {
        ItemStack card = item("ling_13");
        card.set(BankData.CARD.get(), new BankCard(player.getStringUUID(), 0, 0, 0, 0, 0, 0, 0, 0, 0));
        return card;
    }

    private static ItemStack named(ItemStack stack, int count, String name) {
        stack.setCount(count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }

    private String screenName() {
        return minecraft.screen == null ? "无" : minecraft.screen.getClass().getSimpleName();
    }
}
