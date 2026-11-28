package net.yvanchuxiuzhen.smoke;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.npc.content.QiankundaorenBlockEntity;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession;
import net.yvanchuxiuzhen.system.quest.api.QuestJournalState;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.client.DialogueScreen;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 在新建烟测世界的人间道维度自举放置乾坤道人，检查初见任务的两个选项、断点恢复与客户端收到的原生传送。 */
final class QiankunDialogueCheck extends SystemCheck {
    private static final String QUEST_ID = "chujianqiankun";
    private static final ResourceKey<Level> RENJIAN = ResourceKey.create(Registries.DIMENSION, id("dimensionrenjian"));
    private static final BlockPos FLOOR = new BlockPos(4, 220, -24);
    private static final BlockPos DAO = FLOOR.offset(0, 1, 2);
    private static final Vec3 STAND = Vec3.atBottomCenterOf(FLOOR.above());
    private static final Vec3 LANDING = new Vec3(20, 65, 13);
    private static final String LATER = "一会儿再说";
    private static final String DEPART = "现在就出发";
    private static final String TELEPORT_PROBE = "q119_position_probe";
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final AtomicReference<Vec3> receivedLanding = new AtomicReference<>();
    private ChannelPipeline pipeline;
    private BlockState oldFloor;
    private BlockState oldDao;
    private QuestDialogueSession oldSession;
    private QuestJournalState oldJournal;
    private ResourceKey<Level> originalDimension;
    private GameType gameType;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private int selected;
    private int submittedMenuId = -1;
    private boolean prepared;

    QiankunDialogueCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("Q119初见乾坤道人", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (local < 12 || !clientAtDao()) return await(local, 400, "人间道受控道人未就绪");
                expect("客户端位于人间道", RENJIAN.location(), minecraft.level.dimension().location());
                useDao();
            }
            case 2 -> {
                if (local < 12) return false;
                boolean[] ready = {false};
                onServer(player -> {
                    String stage = daoStage(player);
                    QuestDialogueSession session = DialogueActions.snapshot(player);
                    expect("首次普通右键只写入准备态", "准备", stage);
                    boolean noDialogue = DialogueActions.activeTemplate(player).isEmpty() && !session.dialogueBegin();
                    expectTrue("首次右键未启动服务端对话会话", noDialogue);
                    ready[0] = "准备".equals(stage) && noDialogue;
                });
                if (!ready[0] || minecraft.screen != null) return abort("第一次右键未保持只准备、不打开对话");
                useDao();
            }
            case 3 -> {
                if (!(minecraft.screen instanceof DialogueScreen screen) || screen.getMenu().choices) {
                    return await(local, 200, "第二次普通右键未打开任务正文");
                }
                onServer(player -> expectTrue("第二次普通右键启动初见乾坤任务第1步",
                        isTaskAt(player, 1)));
            }
            case 4 -> {
                if (choicesVisible()) {
                    onServer(player -> expectTrue("两项选择出现时已保存43且恰好持有一枚寻灵盘",
                            isTaskAt(player, 43) && checkpoint(player, "未完成", 43) && compassCount(player) == 1));
                    return true;
                }
                continueText();
                return await(local, 900, "正文未推进至两个中文选项");
            }
            case 5 -> {
                if (!press(LATER)) return await(local, 200, "未能点击「一会儿再说」");
            }
            case 6 -> {
                continueText();
                if (minecraft.screen != null) return await(local, 200, "「一会儿再说」未结束会话");
                onServer(player -> expectTrue("稍后分支不传送，并保留未完成检查点43与一枚寻灵盘",
                        player.serverLevel().dimension().equals(RENJIAN) && player.position().distanceToSqr(STAND) < 0.01
                                && DialogueActions.activeTemplate(player).isEmpty()
                                && checkpoint(player, "未完成", 43) && compassCount(player) == 1));
                leaveWorld();
            }
            case 7 -> { return reopened(local); }
            case 8 -> {
                if (!inWorld(local)) return false;
                if (!clientAtDao()) return await(local, 400, "重进后未在人间道道人旁就绪");
                boolean[] persisted = {false};
                onServer(player -> persisted[0] = "准备".equals(daoStage(player))
                        && checkpoint(player, "未完成", 43) && compassCount(player) == 1
                        && DialogueActions.activeTemplate(player).isEmpty());
                if (!persisted[0]) return abort("原生保存重进后准备态、未完成检查点43、寻灵盘或清空的会话不符");
                expectTrue("原生保存重进保留准备态、未完成检查点43及一枚寻灵盘，临时会话清空", true);
                submittedMenuId = -1;
                useDao();
            }
            case 9 -> {
                if (minecraft.screen instanceof DialogueScreen screen && !screen.getMenu().choices) {
                    return abort("重进后右键从头打开文字页，没有恢复到选项");
                }
                if (!choicesVisible()) return await(local, 200, "重进后右键未恢复两个中文选项");
                onServer(player -> expectTrue("重进后真实右键直接恢复第43步，不重复发盘",
                        isTaskAt(player, 43) && checkpoint(player, "未完成", 43) && compassCount(player) == 1));
            }
            case 10 -> {
                // 等选项同步后的画面真正渲染，再按按钮，避免截到上一页。
                if (local < 6 || !choicesVisible()) return await(local, 200, "恢复后的中文选项未稳定显示");
                screenshots.take("q119-renjian-dialogue-choices.png");
                watchNativeTeleport();
                if (!press(DEPART)) return abort("未能点击「现在就出发」");
            }
            case 11 -> {
                continueText();
                if (minecraft.screen != null || receivedLanding.get() == null) {
                    return await(local, 300, "出发对白未结束或客户端未收到原生绝对位置包");
                }
                expect("客户端原生绝对传送坐标", LANDING, receivedLanding.get());
                expect("传送后客户端维度", RENJIAN.location(), minecraft.level.dimension().location());
                onServer(player -> {
                    expect("传送后服务端维度", RENJIAN.location(), player.serverLevel().dimension().location());
                    expectTrue("出发分支结束并把任务标为完成、存53", checkpoint(player, "完成", 53)
                            && DialogueActions.activeTemplate(player).isEmpty());
                    expect("完成任务后寻灵盘数量恰好一枚", 1, compassCount(player));
                });
                end();
            }
            default -> throw new IllegalStateException("未知Q119步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(RENJIAN);
        if (level == null) {
            fail("存档缺少人间道维度");
            finish();
            return;
        }
        oldFloor = level.getBlockState(FLOOR);
        oldDao = level.getBlockState(DAO);
        if (!oldFloor.isAir() || !oldDao.isAir() || !level.isEmptyBlock(FLOOR.above()) || !level.isEmptyBlock(FLOOR.above(2))) {
            fail("人间道受控站位不是空地，拒绝覆盖存档方块");
            finish();
            return;
        }
        originalDimension = player.serverLevel().dimension();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        gameType = player.gameMode.getGameModeForPlayer();
        selected = player.getInventory().selected;
        inventory.clear();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        oldSession = player.getData(QuestData.SESSION);
        oldJournal = player.getData(QuestData.JOURNAL);
        player.setData(QuestData.SESSION, QuestDialogueSession.EMPTY);
        player.setData(QuestData.JOURNAL, QuestJournalState.EMPTY);
        player.getInventory().clearContent();
        player.setGameMode(GameType.SURVIVAL);
        level.setBlockAndUpdate(FLOOR, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(DAO, BuiltInRegistries.BLOCK.get(id("qiankundaoren")).defaultBlockState());
        player.teleportTo(level, STAND.x, STAND.y, STAND.z, 0, 15);
        player.inventoryMenu.broadcastChanges();
        prepared = true;
    }

    private boolean clientAtDao() {
        return minecraft.screen == null && RENJIAN.equals(minecraft.level.dimension())
                && minecraft.player.position().distanceToSqr(STAND) < 0.01
                && minecraft.level.getBlockEntity(DAO) instanceof QiankundaorenBlockEntity;
    }

    private void useDao() {
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(DAO).add(0, 0.5, 0), Direction.UP, DAO, false));
    }

    private boolean choicesVisible() {
        return minecraft.screen instanceof DialogueScreen screen && screen.getMenu().choices
                && button(screen, LATER) != null && button(screen, DEPART) != null;
    }

    private void continueText() {
        if (minecraft.screen instanceof DialogueScreen screen && !screen.getMenu().choices) press("继续对话");
    }

    private boolean press(String text) {
        if (!(minecraft.screen instanceof DialogueScreen screen) || screen.getMenu().containerId == submittedMenuId) return false;
        Button button = button(screen, text);
        if (button == null) return false;
        submittedMenuId = screen.getMenu().containerId;
        button.onPress();
        return true;
    }

    private static Button button(DialogueScreen screen, String text) {
        return screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> button.getMessage().getString().equals(text)).findFirst().orElse(null);
    }

    private void watchNativeTeleport() {
        // 在原生入站包交给客户端之前读取绝对坐标，不受落地后位置变化影响。
        pipeline = minecraft.getConnection().getConnection().channel().pipeline();
        pipeline.addBefore("packet_handler", TELEPORT_PROBE, new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
                if (message instanceof ClientboundPlayerPositionPacket packet && packet.getRelativeArguments().isEmpty()) {
                    receivedLanding.compareAndSet(null, new Vec3(packet.getX(), packet.getY(), packet.getZ()));
                }
                super.channelRead(context, message);
            }
        });
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.setData(QuestData.SESSION, oldSession);
        player.setData(QuestData.JOURNAL, oldJournal);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(gameType);
        ServerLevel level = player.server.getLevel(RENJIAN);
        level.setBlockAndUpdate(DAO, oldDao);
        level.setBlockAndUpdate(FLOOR, oldFloor);
        player.teleportTo(player.server.getLevel(originalDimension), position.x, position.y, position.z, yaw, pitch);
        player.inventoryMenu.broadcastChanges();
    }

    private boolean await(int local, int limit, String reason) {
        return local > limit && abort(reason);
    }

    private boolean abort(String reason) {
        fail(reason);
        end();
        return true;
    }

    private void end() {
        if (pipeline != null) pipeline.remove(TELEPORT_PROBE);
        if (prepared) onServer(this::cleanup);
        finish();
    }

    private static String daoStage(ServerPlayer player) {
        if (RENJIAN.equals(player.serverLevel().dimension()) && player.level().getBlockEntity(DAO) instanceof QiankundaorenBlockEntity dao) {
            return dao.playerStage(player.getUUID());
        }
        return "未找到人间道道人";
    }

    private static boolean isTaskAt(ServerPlayer player, int cursor) {
        QuestDialogueSession session = DialogueActions.snapshot(player);
        return DialogueActions.activeTemplate(player)
                .map(QuestTemplateDefinition::questId)
                .filter(QUEST_ID::equals)
                .isPresent()
                && session.cursor() == cursor;
    }

    private static boolean checkpoint(ServerPlayer player, String state, int cursor) {
        QuestJournalState journal = DialogueActions.journal(player);
        QuestJournalState.QuestJournalEntry entry = journal.entry(QUEST_ID);
        return entry != null && QUEST_ID.equals(entry.uuid())
                && state.equals(entry.status()) && entry.progress() == cursor;
    }

    private static int compassCount(ServerPlayer player) {
        var item = BuiltInRegistries.ITEM.get(id("ling_14item"));
        return player.getInventory().items.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
