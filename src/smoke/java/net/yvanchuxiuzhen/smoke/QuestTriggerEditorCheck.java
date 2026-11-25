package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.client.DialogueScreen;
import net.yvanchuxiuzhen.system.quest.client.QuestTriggerEditorScreen;
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlockEntity;
import net.yvanchuxiuzhen.system.quest.content.QuestTriggerEditorMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 通过创造潜行原生右键、真实编辑控件和保存重进检查任务方块配置。 */
final class QuestTriggerEditorCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private BlockPos pos;
    private BlockPos floor;
    private BlockState oldBlock;
    private BlockState oldFloor;
    private Vec3 position;
    private GameType mode;
    private int selected;
    private boolean shift;
    private boolean shiftKey;
    private CompoundTag session;
    private CompoundTag journal;
    private CompoundTag configured;
    private boolean prepared;

    QuestTriggerEditorCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("任务方块编辑", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            switch (step) {
                case 0 -> {
                    shiftKey = minecraft.options.keyShift.isDown();
                    minecraft.options.keyShift.setDown(true);
                    onServer(this::prepare);
                }
                case 1 -> {
                    if (local < 15) return false;
                    minecraft.player.setShiftKeyDown(true);
                    use();
                }
                case 2 -> {
                    if (!editor(local)) return false;
                    expect("中文原生菜单标题", "任务方块设置", minecraft.screen.getTitle().getString());
                    expect("三列十行文本框", 30, inputs().size());
                    expect("十个类型和粒子勾选", 11, checks().size());
                    expect("显示此物中文", "显示此物", I18n.get("gui.yvanchuxiuzhen.renwufangkuai_gui.label_xian_shi_ci_wu"));
                    expect("展示槽同步钻石", Items.DIAMOND, minecraft.player.containerMenu.getSlot(0).getItem().getItem());
                    expect("展示槽只容纳一件", 1, minecraft.player.containerMenu.getSlot(0).getMaxStackSize());
                    onServer(player -> expectTrue("服务端真实编辑菜单", player.containerMenu instanceof QuestTriggerEditorMenu));
                    screenshots.take("quest-trigger-editor-empty.png");
                    for (int row = 1; row <= 10; row++) {
                        inputs().get(row - 1).setValue(script(row));
                        inputs().get(9 + row).setValue("商店" + row);
                        inputs().get(19 + row).setValue("商品" + row);
                        if (row % 2 == 1) checks().get(row - 1).onPress();
                    }
                    checks().get(10).onPress();
                    save();
                }
                case 3 -> {
                    if (local < 15) return false;
                    onServer(player -> checkFirst(block(player)));
                    checkFirst(clientBlock());
                    expectTrue("设置按钮不关闭编辑页", minecraft.screen instanceof QuestTriggerEditorScreen);
                    screenshots.take("quest-trigger-editor-configured.png");
                    for (EditBox input : inputs()) input.setValue("");
                    inputs().get(0).setValue(ordinary());
                    checks().get(0).onPress();
                    inputs().get(11).setValue("商店二次设置");
                    checks().get(10).onPress();
                    save();
                }
                case 4 -> {
                    if (local < 15) return false;
                    onServer(player -> {
                        checkSecond(block(player));
                        configured = block(player).saveWithFullMetadata(player.registryAccess());
                    });
                    checkSecond(clientBlock());
                    minecraft.player.closeContainer();
                }
                case 5 -> {
                    minecraft.options.keyShift.setDown(true);
                    minecraft.player.setShiftKeyDown(true);
                    if (local < 10) return false;
                    use();
                }
                case 6 -> {
                    if (!editor(local)) return false;
                    expectTrue("重开不回填来源文本框", inputs().stream().allMatch(input -> input.getValue().isEmpty()));
                    expectTrue("旧高级脚本仍使第一勾选为真", checks().getFirst().selected());
                    expectTrue("粒子勾选每次初始false", !checks().get(10).selected());
                    minecraft.player.closeContainer();
                }
                case 7 -> leaveWorld();
                case 8 -> { return reopened(local); }
                case 9 -> {
                    if (!inWorld(local)) return false;
                    onServer(player -> {
                        CompoundTag data = block(player).saveWithFullMetadata(player.registryAccess());
                        for (String key : configured.getAllKeys()) {
                            if (key.startsWith("renwulist") || key.startsWith("tasklist") || key.startsWith("shopinformation")
                                    || key.startsWith("productlist") || key.equals("lizixianshi")) {
                                expectTrue("保存重进配置 " + key, configured.get(key).equals(data.get(key)));
                            }
                        }
                        expect("保存重进展示槽钻石一件", 1, block(player).getItem(0).getCount());
                        expect("保存重进展示物品", Items.DIAMOND, block(player).getItem(0).getItem());
                        player.setShiftKeyDown(false);
                        player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                        player.inventoryMenu.broadcastChanges();
                    });
                    minecraft.options.keyShift.setDown(false);
                    minecraft.player.setShiftKeyDown(false);
                }
                case 10 -> {
                    if (local < 10) return false;
                    use();
                }
                case 11 -> {
                    if (!(minecraft.screen instanceof DialogueScreen)
                            || !DialogueActions.snapshot(minecraft.player).getString("TXTxianshi").equals("配置高级1")) {
                        if (local > 200) throw new IllegalStateException("保存的高级脚本没有经普通方块右键启动");
                        return false;
                    }
                    expect("高级脚本优先普通旧值", "配置高级1", DialogueActions.snapshot(minecraft.player).getString("TXTxianshi"));
                    onServer(this::restore);
                    finish();
                }
                default -> throw new IllegalStateException("未知任务编辑步骤 " + step);
            }
        } catch (RuntimeException exception) {
            fail("检查异常：" + exception);
            if (prepared && minecraft.getSingleplayerServer() != null && minecraft.player != null) onServer(this::restore);
            finish();
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        position = player.position();
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        shift = player.isShiftKeyDown();
        session = DialogueActions.snapshot(player);
        journal = DialogueActions.journal(player);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        pos = player.blockPosition().above(6);
        floor = pos.north(2).below();
        oldBlock = player.level().getBlockState(pos);
        oldFloor = player.level().getBlockState(floor);
        if (player.level().getBlockEntity(pos) != null || player.level().getBlockEntity(floor) != null) throw new IllegalStateException("夹具位置有原方块实体");
        prepared = true;
        player.closeContainer();
        player.setData(QuestData.SESSION, new CompoundTag());
        player.getInventory().clearContent();
        player.getInventory().setItem(selected, new ItemStack(Items.STICK));
        player.setGameMode(GameType.CREATIVE);
        player.setShiftKeyDown(true);
        player.level().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
        player.level().setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "renwuweizhuangfangkuai")).defaultBlockState(), 3);
        block(player).setItem(0, new ItemStack(Items.DIAMOND));
        player.serverLevel().sendBlockUpdated(pos, block(player).getBlockState(), block(player).getBlockState(), 3);
        player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 1.5, 0, 0);
        player.inventoryMenu.broadcastChanges();
    }

    private void checkFirst(DisguisedQuestBlockEntity block) {
        for (int row = 1; row <= 10; row++) {
            expect("第" + row + "行来源任务类型", script(row), row % 2 == 1 ? block.renwulist(row) : block.tasklist(row));
            expect("第" + row + "行商店信息", "商店" + row, block.shopInformation(row));
            expect("第" + row + "行商品列表", "商品" + row, block.productList(row));
        }
        expectTrue("来源粒子配置开启", block.showParticles());
    }

    private void checkSecond(DisguisedQuestBlockEntity block) {
        expect("改普通保留另一类型高级旧值", script(1), block.renwulist(1));
        expect("普通任务写入独立字段", ordinary(), block.tasklist(1));
        expect("空输入保留原任务", script(10), block.tasklist(10));
        expect("空信息保留原商店", "商店1", block.shopInformation(1));
        expect("非空信息允许写空商品", "", block.productList(2));
        expect("二次商店信息已写入", "商店二次设置", block.shopInformation(2));
        expectTrue("取消勾选不关闭来源粒子", block.showParticles());
    }

    private static String script(int row) {
        return "编辑任务" + row + "/smoke_trigger_editor_" + row + "/说明/无/无/需求/奖励%文字/配置高级" + row + "%结束";
    }

    private static String ordinary() {
        return "普通编辑任务/smoke_trigger_editor_normal/说明/无/无/需求/奖励%文字/普通新脚本%结束";
    }

    private boolean editor(int local) {
        if (local < 10) return false;
        if (minecraft.screen instanceof QuestTriggerEditorScreen) return true;
        if (local > 200) throw new IllegalStateException("创造潜行原生右键没有打开编辑页");
        return false;
    }

    private void use() {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private List<EditBox> inputs() {
        return minecraft.screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).toList();
    }

    private List<Checkbox> checks() {
        return minecraft.screen.children().stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).toList();
    }

    private void save() {
        minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getMessage().getString().equals("设置任务")).findFirst().orElseThrow().onPress();
    }

    private DisguisedQuestBlockEntity block(ServerPlayer player) {
        return (DisguisedQuestBlockEntity) player.level().getBlockEntity(pos);
    }

    private DisguisedQuestBlockEntity clientBlock() {
        return (DisguisedQuestBlockEntity) minecraft.level.getBlockEntity(pos);
    }

    private void restore(ServerPlayer player) {
        player.closeContainer();
        if (player.level().getBlockEntity(pos) instanceof DisguisedQuestBlockEntity block) block.clearContent();
        player.level().setBlock(pos, oldBlock, 3);
        player.level().setBlock(floor, oldFloor, 3);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot).copy());
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(shift);
        player.setData(QuestData.SESSION, session.copy());
        player.setData(QuestData.JOURNAL, journal.copy());
        player.connection.teleport(position.x, position.y, position.z, 0, 0);
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> {
            minecraft.options.keyShift.setDown(shiftKey);
            minecraft.player.setShiftKeyDown(shift);
        });
        prepared = false;
    }
}
