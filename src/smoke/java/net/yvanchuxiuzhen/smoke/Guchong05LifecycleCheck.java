package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.api.GuchongTracking;
import net.yvanchuxiuzhen.system.guchong.api.GuchongTrackingData;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong05Entity;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 05物品真实释放、追踪实体保存重读、百刻返还与回收字段边界。 */
final class Guchong05LifecycleCheck extends SystemCheck {
    private final Map<BlockPos, BlockState> originalBlocks = new LinkedHashMap<>();
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private Difficulty originalDifficulty;
    private GameType originalMode;
    private BlockPos site;
    private BlockPos destination;
    private Guchong05Entity ordinary;
    private Guchong05Entity trackedBeforeSave;
    private UUID trackedUuid;
    private String unrelatedOwner;
    private double saveTick;
    private boolean originalFrozen;
    private boolean resumedProgress;
    private int reloadWaitTicks;
    private boolean reopening;
    private int executed = -1;

    Guchong05LifecycleCheck(List<String> report, String levelId) {
        super("荧光蛊生命周期", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> action(step, local, 8, this::prepare);
            case 1 -> action(step, local, 5, this::releaseOrdinary);
            case 2 -> action(step, local, 5, this::recoverOrdinary);
            case 3 -> action(step, local, 5, this::releaseTracked);
            case 4 -> waitForSavePoint(local);
            case 5 -> { leaveWorld(); yield true; }
            case 6 -> reopened(local);
            case 7 -> resumed(local);
            case 8 -> returned(local);
            case 9 -> action(step, local, 5, player -> { cleanup(player); finish(); });
            default -> throw new IllegalStateException("未知荧光蛊检查步骤 " + step);
        };
    }

    private boolean action(int step, int local, int delay, Consumer<ServerPlayer> run) {
        if (executed != step) {
            executed = step;
            onServer(run);
        }
        return local >= delay;
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalDifficulty = player.level().getDifficulty();
        originalFrozen = player.server.tickRateManager().isFrozen();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            originalInventory.add(player.getInventory().getItem(slot).copy());
        }
        player.closeContainer();
        player.getInventory().clearContent();
        player.setGameMode(GameType.SURVIVAL);
        player.server.setDifficulty(Difficulty.NORMAL, true);
        site = player.blockPosition().above(16);
        destination = site.offset(0, 1, 10);
        for (int x = -2; x <= 2; x++) {
            for (int z = -1; z <= 5; z++) {
                setBlock(player, site.offset(x, -1, z), Blocks.STONE.defaultBlockState());
            }
        }
        for (int z = 0; z <= 4; z++) {
            setBlock(player, site.offset(0, 1, z), Blocks.AIR.defaultBlockState());
        }
        player.connection.teleport(site.getX() + .5, site.getY(), site.getZ() + .5, 0, 0);
        expectTrue("05粒子类型可由公共接口读取", Particles.type("guchong_05lizi") != null);
    }

    private void releaseOrdinary(ServerPlayer player) {
        ItemStack stack = item("guchong_05item");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        use(player);
        ordinary = nearby(player).stream().findFirst().orElseThrow(() -> new IllegalStateException("普通荧光蛊右键未释放实体"));
        expectTrue("普通荧光蛊原生右键释放并消耗一只，且不自动获得追踪目标",
                player.getMainHandItem().isEmpty() && !ordinary.getPersistentData().getBoolean("zhuizhong")
                        && ordinary.getPersistentData().getString("belongToMasterUUID").isEmpty());
    }

    private void recoverOrdinary(ServerPlayer player) {
        unrelatedOwner = UUID.randomUUID().toString();
        if (unrelatedOwner.equals(player.getStringUUID())) unrelatedOwner = UUID.randomUUID().toString();
        ordinary.getPersistentData().putString("belongToMasterUUID", unrelatedOwner);
        ordinary.mobInteract(player, InteractionHand.MAIN_HAND);
        ItemStack recovered = findItem(player);
        expectTrue("实体主人UUID与当前玩家不同时仍可右键回收", ordinary.isRemoved() && !recovered.isEmpty()
                && !GuchongTracking.get(recovered).tracking()
                && GuchongTracking.get(recovered).ownerUuid().equals(unrelatedOwner));
        player.getInventory().clearContent();
    }

    private void releaseTracked(ServerPlayer player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, GuchongTracking.createBound(player, destination));
        use(player);
        trackedBeforeSave = nearby(player).stream().filter(entity -> entity.getPersistentData().getBoolean("zhuizhong"))
                .findFirst().orElseThrow(() -> new IllegalStateException("悬赏荧光蛊右键未释放追踪实体"));
        trackedUuid = trackedBeforeSave.getUUID();
        CompoundTag data = trackedBeforeSave.getPersistentData();
        expectTrue("悬赏荧光蛊真实物品入口保留主人与三维目标", player.getMainHandItem().isEmpty()
                && data.getBoolean("zhuizhong") && data.getString("belongToMasterUUID").equals(player.getStringUUID())
                && data.getDouble("zuobiaoX") == destination.getX() && data.getDouble("zuobiaoY") == destination.getY()
                && data.getDouble("zuobiaoZ") == destination.getZ());
    }

    private boolean waitForSavePoint(int local) {
        double[] tick = {-1};
        onServer(player -> {
            Entity entity = player.serverLevel().getEntity(trackedUuid);
            if (entity instanceof Guchong05Entity insect) {
                tick[0] = insect.getPersistentData().getDouble("entitytick");
                if (tick[0] >= 12 && tick[0] < 100) player.server.tickRateManager().setFrozen(true);
            }
        });
        if (tick[0] >= 12) {
            saveTick = tick[0];
            expectTrue("追踪计时已自然推进后再保存中途实体", saveTick >= 12 && saveTick < 100);
            return true;
        }
        if (local > 100) throw new IllegalStateException("荧光蛊追踪计时未推进到保存点");
        return false;
    }

    private boolean resumed(int local) {
        if (reopening) return false;
        if (minecraft.screen instanceof BackupConfirmScreen screen
                && screen.getTitle().equals(Component.translatable("selectWorld.backupQuestion.experimental"))) {
            reopening = true;
            try {
                screen.children().stream().filter(child -> child instanceof Button button
                                && button.getMessage().equals(Component.translatable("selectWorld.backupJoinSkipButton")))
                        .map(child -> (Button) child).findFirst().orElseThrow().onPress();
            } finally {
                reopening = false;
            }
            return false;
        }
        if (minecraft.player == null || minecraft.getSingleplayerServer() == null || local < 2) {
            if (local > 1200) throw new IllegalStateException("荧光蛊存档重进超时");
            return false;
        }
        boolean[] checked = {false};
        reloadWaitTicks++;
        onServer(player -> {
            player.server.tickRateManager().setFrozen(true);
            Entity entity = player.serverLevel().getEntity(trackedUuid);
            if (!(entity instanceof Guchong05Entity loaded) || loaded.isRemoved()) {
                if (reloadWaitTicks > 40) throw new IllegalStateException("存档重进未恢复百刻前的追踪荧光蛊");
                return;
            }
            CompoundTag data = loaded.getPersistentData();
            expectTrue("真实退出重进恢复同一追踪实体和来源字段", loaded != trackedBeforeSave
                    && data.getBoolean("zhuizhong") && data.getString("belongToMasterUUID").equals(player.getStringUUID())
                    && data.getDouble("zuobiaoX") == destination.getX() && data.getDouble("zuobiaoY") == destination.getY()
                    && data.getDouble("zuobiaoZ") == destination.getZ());
            expectTrue("真实退出重进保留实体追踪计时", data.getDouble("entitytick") >= saveTick
                    && data.getDouble("entitytick") < 100
                    && data.getDouble("entitytick2") == data.getDouble("entitytick") % 5);
            player.server.tickRateManager().setFrozen(false);
            checked[0] = true;
        });
        return checked[0];
    }

    private boolean returned(int local) {
        boolean[] done = {false};
        onServer(player -> {
            Entity entity = player.serverLevel().getEntity(trackedUuid);
            if (entity != null && !entity.isRemoved()) {
                if (!resumedProgress && entity.getPersistentData().getDouble("entitytick") > saveTick) {
                    expectTrue("重进后自然继续推进追踪计时", entity.getPersistentData().getDouble("entitytick") > saveTick);
                    resumedProgress = true;
                }
                return;
            }
            ItemStack returned = findItem(player);
            if (returned.isEmpty()) {
                throw new IllegalStateException("追踪荧光蛊百刻移除后未返还主人物品");
            }
            GuchongTrackingData data = GuchongTracking.get(returned);
            expectTrue("百刻返还发生在重进后计时继续推进之后", resumedProgress);
            expectTrue("追踪荧光蛊百刻返还唯一物品并保留来源身份字段", returned.getCount() == 1
                    && data.tracking() && data.ownerUuid().equals(player.getStringUUID())
                    && data.targetX() == destination.getX() && data.targetY() == destination.getY()
                    && data.targetZ() == destination.getZ() && !data.tamed()
                    && !data.cutPrice() && data.homeX() == 0
                    && data.homeY() == 0 && data.homeZ() == 0);
            expectTrue("返还物品不复制两个实体计时器", !tag(returned).contains("entitytick") && !tag(returned).contains("entitytick2"));
            done[0] = true;
        });
        if (!done[0] && local > 180) throw new IllegalStateException("荧光蛊百刻返还超时");
        return done[0];
    }

    private List<Guchong05Entity> nearby(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Guchong05Entity.class, new AABB(site).inflate(32), Entity::isAlive);
    }

    private static void use(ServerPlayer player) {
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    private static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }

    private ItemStack findItem(ServerPlayer player) {
        return player.getInventory().items.stream().filter(stack -> stack.is(item("guchong_05item").getItem()))
                .findFirst().orElse(ItemStack.EMPTY);
    }

    private CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private void setBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        originalBlocks.putIfAbsent(pos, player.level().getBlockState(pos));
        player.level().setBlock(pos, state, 3);
    }

    private void cleanup(ServerPlayer player) {
        if (ordinary != null && !ordinary.isRemoved()) ordinary.discard();
        Entity tracked = trackedUuid == null ? null : player.serverLevel().getEntity(trackedUuid);
        if (tracked != null) tracked.discard();
        originalBlocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        player.getInventory().clearContent();
        for (int slot = 0; slot < originalInventory.size(); slot++) {
            player.getInventory().setItem(slot, originalInventory.get(slot));
        }
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.setGameMode(originalMode);
        player.server.setDifficulty(originalDifficulty, true);
        player.server.tickRateManager().setFrozen(originalFrozen);
    }
}
