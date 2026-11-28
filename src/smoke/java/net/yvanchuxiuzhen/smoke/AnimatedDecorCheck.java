package net.yvanchuxiuzhen.smoke;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorBlock;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorBlockEntity;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorContent;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorVariant;
import net.yvanchuxiuzhen.collection.decor.FortuneDraws;
import software.bernie.geckolib.animation.AnimationController;

/** 梯子原生放置与库存保存、签筒双端延迟结果、实际动画及真实保存重进必要单项。 */
final class AnimatedDecorCheck extends SystemCheck {
    private static final Direction[] FACINGS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<SavedBlock> blocks = new ArrayList<>();
    private final List<Component> messages = new ArrayList<>();
    private final List<Integer> messageTicks = new ArrayList<>();
    private final List<Integer> clientGrades = new ArrayList<>();
    private final int[] requestTicks = new int[3];
    private final Screenshots screenshots;
    private final Consumer<ClientChatReceivedEvent.System> chat = event -> {
        if (event.getMessage().getContents() instanceof TranslatableContents text
                && text.getKey().startsWith("message.yvanchuxiuzhen.decor.fortune.")) {
            messages.add(event.getMessage());
            messageTicks.add(minecraft.getSingleplayerServer().getTickCount());
        }
    };
    private final Consumer<FortuneDraws.ClientResult> result = event -> minecraft.execute(() -> clientGrades.add(event.grade()));
    private BlockPos origin;
    private Vec3 position;
    private Vec3 velocity;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean sneaking;
    private boolean noGravity;
    private boolean shiftPressed;
    private boolean daylight;
    private long dayTime;
    private int placedLadders;
    private boolean prepared;
    private boolean animationObserved;
    private boolean activationObserved;

    AnimatedDecorCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("签筒与装饰梯子", report, levelId);
        this.screenshots = screenshots;
    }

    @Override protected boolean step(int step, int local) {
        if (prepared && minecraft.player != null && minecraft.level != null) observeClient();
        switch (step) {
            case 0 -> {
                NeoForge.EVENT_BUS.addListener(chat);
                NeoForge.EVENT_BUS.addListener(result);
                shiftPressed = minecraft.options.keyShift.isDown();
                minecraft.options.keyShift.setDown(false);
                onServer(this::prepare);
            }
            case 1 -> {
                if (!prepared) {
                    cleanupAndFinish();
                    return true;
                }
                if (local < 20) return false;
                draw(0, "空手真实右键被签筒消费");
            }
            case 2 -> {
                if (local < 10) return false;
                screenshots.take("animated_decor_shaking.png");
                onServer(player -> {
                    player.setItemInHand(InteractionHand.MAIN_HAND, held());
                    player.inventoryMenu.broadcastChanges();
                });
            }
            case 3 -> {
                if (local < 5) return false;
                expectTrue("第二次交互前持物已同步", ItemStack.matches(held(), minecraft.player.getMainHandItem()));
                draw(1, "前次未开奖时持物右键仍被签筒消费");
            }
            case 4 -> {
                if ((messages.size() < 4 || clientGrades.size() < 2 || !activationObserved) && local < 200) return false;
                verifyResults(2);
                expectTrue("实际渲染过程控制器播放签筒动画", animationObserved);
                expectTrue("集成服实际渲染器收到签条激活栈", activationObserved);
                screenshots.take("animated_decor_fortune.png");
            }
            case 5 -> {
                if (local < 80) return false;
                expect("动画结束后客户端恢复空闲状态", 0, minecraft.level.getBlockState(fortunePos()).getValue(AnimatedDecorBlock.FORTUNE_ANIMATION).intValue());
                onServer(player -> {
                    verifyNoItemReward(player);
                    player.connection.teleport(origin.getX() + 4.5, origin.getY() + 1, origin.getZ() - 5, 0, 10);
                    player.setDeltaMovement(Vec3.ZERO);
                });
            }
            case 6 -> {
                if (local < 15) return false;
                screenshots.take("animated_decor_ladders.png");
                leaveWorld();
            }
            case 7 -> { return reopened(local); }
            case 8 -> {
                if (!inWorld(local)) return false;
                onServer(player -> {
                    for (int index = 0; index < FACINGS.length; index++) {
                        BlockState state = player.level().getBlockState(ladderPos(index).above());
                        expectTrue("重进保留梯子上格朝向与含水" + FACINGS[index], state.is(AnimatedDecorContent.block(AnimatedDecorVariant.LADDER))
                                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == FACINGS[index] && state.getValue(BlockStateProperties.WATERLOGGED));
                    }
                    expectTrue("重进保留第九槽物品及组件", player.level().getBlockEntity(ladderPos(0).above()) instanceof AnimatedDecorBlockEntity ladder
                            && ItemStack.matches(witness(), ladder.getItem(8)));
                    expectTrue("重进保留签筒零槽方块实体", player.level().getBlockEntity(fortunePos()) instanceof AnimatedDecorBlockEntity fortune
                            && fortune.variant() == AnimatedDecorVariant.FORTUNE && fortune.getContainerSize() == 0);
                    verifyNoItemReward(player);
                    standByFortune(player);
                });
                animationObserved = false;
                activationObserved = false;
            }
            case 9 -> {
                if (local < 15) return false;
                draw(2, "真实重进后持物右键仍被签筒消费");
            }
            case 10 -> {
                if ((messages.size() < 6 || clientGrades.size() < 3 || !activationObserved) && local < 200) return false;
                verifyResults(3);
                expectTrue("重进后的签筒过程动画实际运行", animationObserved);
                expectTrue("重进后重新触发签条激活画面", activationObserved);
                screenshots.take("animated_decor_reloaded.png");
            }
            case 11 -> {
                if (local < 80) return false;
                expect("重进后的动画结束也恢复客户端空闲", 0, minecraft.level.getBlockState(fortunePos()).getValue(AnimatedDecorBlock.FORTUNE_ANIMATION).intValue());
                onServer(this::verifyNoItemReward);
                cleanupAndFinish();
            }
            default -> throw new IllegalStateException("未知动画装饰检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        position = player.position();
        velocity = player.getDeltaMovement();
        yaw = player.getYRot();
        pitch = player.getXRot();
        selected = player.getInventory().selected;
        mode = player.gameMode.getGameModeForPlayer();
        sneaking = player.isShiftKeyDown();
        noGravity = player.isNoGravity();
        dayTime = player.serverLevel().getDayTime();
        daylight = player.serverLevel().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        origin = player.blockPosition().above(12);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, player.server);
        player.serverLevel().setDayTime(6099);
        // 固定非零尾数使本夹具只观察可翻译消息，不改动生产抽签公式或Q214零档行为。
        for (int index = 0; index < 5; index++) {
            BlockPos pos = ladderPos(index);
            for (BlockPos fixture : List.of(pos.below(), pos, pos.above())) {
                blocks.add(SavedBlock.capture(player, fixture));
                player.level().setBlock(fixture, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            player.level().setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            if (index < FACINGS.length) {
                for (Direction side : FACINGS) {
                    BlockPos wall = pos.above().relative(side);
                    blocks.add(SavedBlock.capture(player, wall));
                    player.level().setBlock(wall, Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
                }
                player.level().setBlock(pos.above(), Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, FACINGS[index]).setValue(BlockStateProperties.WATERLOGGED, true), Block.UPDATE_ALL);
            }
            player.connection.teleport(pos.getX() + .5, pos.getY(), pos.getZ() - 2, 0, 15);
            player.setDeltaMovement(Vec3.ZERO);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(AnimatedDecorContent.block(index < 4 ? AnimatedDecorVariant.LADDER : AnimatedDecorVariant.FORTUNE)));
            player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false));
            expectTrue("生存原生放置消耗一个方块物品" + index, player.getMainHandItem().isEmpty());
            if (index < FACINGS.length) {
                BlockState relocated = player.level().getBlockState(pos.above());
                boolean placed = relocated.is(AnimatedDecorContent.block(AnimatedDecorVariant.LADDER));
                expectTrue("梯子移动到上格并清空原格" + FACINGS[index], placed && player.level().getBlockState(pos).isAir());
                if (!placed) return;
                placedLadders++;
                expectTrue("继承上格同名朝向和含水" + FACINGS[index], relocated.getValue(BlockStateProperties.HORIZONTAL_FACING) == FACINGS[index] && relocated.getValue(BlockStateProperties.WATERLOGGED));
                // 原格已断言为空；透明底板只围住夹具水源，避免等待和重进期间向世界溢水。
                player.level().setBlock(pos, Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
            } else {
                boolean placed = player.level().getBlockEntity(pos) instanceof AnimatedDecorBlockEntity;
                expectTrue("签筒原生放置建立方块实体", placed);
                if (!placed) return;
            }
        }
        for (Direction direction : Direction.values()) {
            IItemHandler ladder = player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, ladderPos(0).above(), direction);
            IItemHandler fortune = player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, fortunePos(), direction);
            expect("梯子六面能力九槽" + direction, 9, ladder.getSlots());
            expect("签筒六面能力保持零槽" + direction, 0, fortune.getSlots());
        }
        IItemHandler handler = player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, ladderPos(0).above(), null);
        expectTrue("第九槽经原生能力放入", handler.insertItem(8, witness(), false).isEmpty());
        expectTrue("六面能力可取出库存", ItemStack.matches(witness(), player.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, ladderPos(0).above(), Direction.DOWN).extractItem(8, 3, true)));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        for (BlockPos fixture : List.of(fortunePos().north(2).below(), fortunePos().north(2), fortunePos().north(2).above())) {
            blocks.add(SavedBlock.capture(player, fixture));
            player.level().setBlock(fixture, fixture.getY() < origin.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        standByFortune(player);
        player.inventoryMenu.broadcastChanges();
        prepared = true;
    }

    private void draw(int index, String label) {
        onServer(player -> requestTicks[index] = player.server.getTickCount());
        expectTrue(label, minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(fortunePos()), Direction.NORTH, fortunePos(), false)).consumesAction());
    }

    private void observeClient() {
        minecraft.player.getInventory().selected = 0;
        minecraft.player.setNoGravity(true);
        minecraft.player.setDeltaMovement(Vec3.ZERO);
        minecraft.player.setShiftKeyDown(false);
        if (minecraft.level.getBlockEntity(fortunePos()) instanceof AnimatedDecorBlockEntity block) {
            AnimationController<?> controller = block.getAnimatableInstanceCache().getManagerForId(0).getAnimationControllers().get("procedurecontroller");
            animationObserved |= controller != null && controller.getCurrentAnimation() != null
                    && controller.getAnimationState() == AnimationController.State.RUNNING;
        }
        try {
            Field itemField = GameRenderer.class.getDeclaredField("itemActivationItem");
            Field ticksField = GameRenderer.class.getDeclaredField("itemActivationTicks");
            itemField.setAccessible(true);
            ticksField.setAccessible(true);
            ItemStack stack = (ItemStack) itemField.get(minecraft.gameRenderer);
            activationObserved |= stack != null && ticksField.getInt(minecraft.gameRenderer) > 0
                    && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().startsWith("yvanchuxiuzhen:guaqian_0");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法读取目标GameRenderer的签条激活状态", exception);
        }
    }

    private void verifyResults(int requests) {
        expect("每次右键分别收到吉凶和诗句", requests * 2, messages.size());
        expect("每次右键分别发布集成服客户端结果", requests, clientGrades.size());
        for (int index = 0; index < requests; index++) {
            expectTrue("第" + (index + 1) + "次消息至少经过四十服务端刻", messageTicks.size() > index * 2
                    && messageTicks.get(index * 2) - requestTicks[index] >= 39);
        }
        for (Component message : messages) {
            expectTrue("消息使用合并汉化且不显示原始键", !message.getString().contains("message.yvanchuxiuzhen."));
            expectTrue("Q214 签筒不发空消息或裸颜色", !message.getString().isBlank() && !message.getString().equals("§f"));
        }
        for (int grade : clientGrades) expectTrue("Q214 签等在 1..7 范围内", grade >= 1 && grade <= 7);
    }

    private void verifyNoItemReward(ServerPlayer player) {
        expectTrue("持物抽签不扣手中物品", ItemStack.matches(held(), player.getMainHandItem()));
        expect("抽签只显示签条而不加入玩家库存", 7, player.getInventory().items.stream().mapToInt(ItemStack::getCount).sum());
    }

    private void standByFortune(ServerPlayer player) {
        player.setNoGravity(true);
        player.setShiftKeyDown(false);
        player.connection.teleport(fortunePos().getX() + .5, fortunePos().getY(), fortunePos().getZ() - 2, 0, 20);
        player.setDeltaMovement(Vec3.ZERO);
    }

    private void cleanupAndFinish() {
        prepared = false;
        NeoForge.EVENT_BUS.unregister(chat);
        NeoForge.EVENT_BUS.unregister(result);
        onServer(this::cleanup);
        minecraft.player.getInventory().selected = selected;
        minecraft.player.setNoGravity(noGravity);
        minecraft.player.setShiftKeyDown(sneaking);
        minecraft.options.keyShift.setDown(shiftPressed);
        finish();
    }

    private void cleanup(ServerPlayer player) {
        for (int index = 0; index < placedLadders; index++) player.level().setBlock(ladderPos(index).above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        for (SavedBlock block : blocks) block.restore(player);
        player.serverLevel().setDayTime(dayTime);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(daylight, player.server);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.setShiftKeyDown(sneaking);
        player.setNoGravity(noGravity);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.setDeltaMovement(velocity);
        player.inventoryMenu.broadcastChanges();
    }

    private BlockPos ladderPos(int index) { return origin.offset(index * 3, 0, 0); }
    private BlockPos fortunePos() { return ladderPos(4); }
    private static ItemStack held() { return new ItemStack(Items.STICK, 7); }
    private static ItemStack witness() {
        ItemStack stack = new ItemStack(Items.DIAMOND, 3);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("梯子第九槽验收物品"));
        return stack;
    }
    private record SavedBlock(BlockPos pos, BlockState state, CompoundTag data) {
        static SavedBlock capture(ServerPlayer player, BlockPos pos) {
            BlockEntity block = player.level().getBlockEntity(pos);
            return new SavedBlock(pos, player.level().getBlockState(pos), block == null ? null : block.saveWithFullMetadata(player.registryAccess()));
        }
        void restore(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            if (data != null) level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
        }
    }
}
