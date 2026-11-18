package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalBlock;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalRuntime;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;

/** 六工具自然点门、可用目标真实往返、缺失目标拒绝及原生保存重进检查。 */
final class DimensionPortalCheck extends SystemCheck {
    private static final List<Gate> GATES = List.of(
            new Gate(DimensionPortalSpec.RENJIAN, new BlockPos(8, 160, 8), Direction.Axis.X, "人间道"),
            new Gate(DimensionPortalSpec.YAOHUN, new BlockPos(8, 160, 20), Direction.Axis.Z, "妖魂道"),
            new Gate(DimensionPortalSpec.TIANQVE, new BlockPos(20, 160, 8), Direction.Axis.X, "天阙道"),
            new Gate(DimensionPortalSpec.XIULUO, new BlockPos(26, 160, 8), Direction.Axis.X, "修罗道"),
            new Gate(DimensionPortalSpec.WUGUI, new BlockPos(20, 160, 20), Direction.Axis.X, "无归道"),
            new Gate(DimensionPortalSpec.DILING, new BlockPos(26, 160, 20), Direction.Axis.X, "地灵道"));

    private final Screenshots screenshots;
    private final List<String> report;
    private final DimensionPortalSpec focusedGate;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final Map<DimensionPortalSpec, BlockPos> exits = new EnumMap<>(DimensionPortalSpec.class);
    private ResourceKey<Level> originalDimension;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private GameType originalMode;
    private CompoundTag originalAbilities;
    private int originalSelected;
    private int originalCooldown;
    private boolean originalNoGravity;
    private boolean prepared;
    private boolean additionalTargetExists;
    private int additionalPhase;
    private int additionalPhaseStart;

    DimensionPortalCheck(List<String> report, String levelId, Screenshots screenshots) {
        this(report, levelId, screenshots, null);
    }

    DimensionPortalCheck(List<String> report, String levelId, Screenshots screenshots, DimensionPortalSpec focusedGate) {
        super(focusedGate == null ? "天道门户" : gate(focusedGate).name() + "门户", report, levelId);
        this.report = report;
        this.screenshots = screenshots;
        this.focusedGate = focusedGate;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            abort("步骤" + step + "异常：" + exception);
            return false;
        }
    }

    private boolean advance(int step, int local) {
        if (focusedGate != null && (step >= 2 && step <= 9 || step >= 14 && step <= 17 || step >= 26 && step <= 29)) return true;
        if (step >= 2 && step <= 9) return checkAdditionalGate(step, local);
        switch (step) {
            case 0 -> {
                onServer(this::prepare);
                return true;
            }
            case 1 -> {
                if (!waitFor(local, "六组门户与工具同步", local >= 10 && minecraft.screen == null
                        && minecraft.level != null && minecraft.level.dimension().equals(Level.OVERWORLD)
                        && minecraft.level.getBlockState(GATES.getFirst().base()).is(portal(GATES.getFirst())), 200)) return false;
                for (int slot = 0; slot < GATES.size(); slot++) {
                    Gate gate = GATES.get(slot);
                    expect(gate.name() + "工具中文", gate.name(), minecraft.player.getInventory().getItem(slot).getHoverName().getString());
                }
                screenshots.take("dimensionportal_overworld_renjian.png");
                return true;
            }
            case 10, 14, 22, 26 -> {
                int index = step == 10 || step == 22 ? 0 : 1;
                Gate gate = primaryGate(index);
                onServer(player -> enter(player, gate.base(), gate.axis()));
                return true;
            }
            case 11, 15, 23, 27 -> {
                Gate gate = primaryGate(step == 11 || step == 23 ? 0 : 1);
                if (!arrived(local, gate.spec().dimension(), gate.name() + "真实碰撞入场")) return false;
                onServer(player -> {
                    BlockUtil.FoundRectangle rectangle = find(player.serverLevel(), player.blockPosition(), gate);
                    verifyPortal(player.serverLevel(), rectangle.minCorner, gate, "目标");
                    expectTrue(gate.name() + "落点处于本组目标门户内", player.position().distanceTo(center(rectangle.minCorner, gate.axis())) < 3);
                    expectTrue(gate.name() + "原生传送冷却已设置", player.isOnPortalCooldown());
                    if (exits.containsKey(gate.spec())) {
                        expect(gate.name() + "重进后复用原目标门户", exits.get(gate.spec()), rectangle.minCorner);
                    } else {
                        exits.put(gate.spec(), rectangle.minCorner.immutable());
                    }
                    park(player, rectangle.minCorner, gate.axis());
                });
                return true;
            }
            case 12, 16, 24, 28 -> {
                if (local < 10 || minecraft.screen != null) return false;
                Gate gate = primaryGate(step == 12 || step == 24 ? 0 : 1);
                if (step == 12 || step == 16) screenshots.take("dimensionportal_" + gate.spec().dimensionName() + "_exit.png");
                onServer(player -> enter(player, exits.get(gate.spec()), gate.axis()));
                return true;
            }
            case 13, 17, 25, 29 -> {
                Gate gate = primaryGate(step == 13 || step == 25 ? 0 : 1);
                if (!arrived(local, Level.OVERWORLD, gate.name() + "真实碰撞回程")) return false;
                onServer(player -> {
                    BlockUtil.FoundRectangle rectangle = find(player.serverLevel(), player.blockPosition(), gate);
                    expect(gate.name() + "回程复用原主世界门户", gate.base(), rectangle.minCorner);
                    verifyPortal(player.serverLevel(), rectangle.minCorner, gate, "主世界");
                    expectTrue(gate.name() + "回程落点在原入口内", player.position().distanceTo(center(gate.base(), gate.axis())) < 3);
                    park(player, gate.base(), gate.axis());
                });
                return true;
            }
            case 18 -> {
                if (local < 20) return false;
                onServer(player -> {
                    verifyTools(player, "保存前");
                    park(player, GATES.getFirst().base(), GATES.getFirst().axis());
                });
                leaveWorld();
                return true;
            }
            case 19 -> { return reopened(local); }
            case 20 -> { return inWorld(local); }
            case 21 -> {
                onServer(player -> {
                    expect("保存重进仍在主世界", Level.OVERWORLD, player.level().dimension());
                    player.setNoGravity(true);
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                    verifyTools(player, "真实保存重进");
                    for (Gate gate : GATES) verifyPortal(player.server.overworld(), gate.base(), gate, "重进主世界");
                    for (Gate gate : GATES) {
                        if (!exits.containsKey(gate.spec())) continue;
                        ServerLevel level = player.server.getLevel(gate.spec().dimension());
                        BlockPos exit = exits.get(gate.spec());
                        level.getChunkAt(exit);
                        verifyPortal(level, exit, gate, "重进目标");
                        expect(gate.name() + "目标POI保存重进仍可查找", exit, find(level, exit, gate).minCorner);
                    }
                });
                return true;
            }
            case 30 -> {
                onServer(this::cleanup);
                finish();
                return true;
            }
            default -> throw new IllegalStateException("未知门户检查步骤 " + step);
        }
    }

    private Gate primaryGate(int index) {
        return focusedGate == null ? GATES.get(index) : gate(focusedGate);
    }

    private static Gate gate(DimensionPortalSpec spec) {
        return GATES.stream().filter(gate -> gate.spec() == spec).findFirst().orElseThrow();
    }

    private boolean checkAdditionalGate(int step, int local) {
        Gate gate = GATES.get(2 + (step - 2) / 2);
        if ((step & 1) == 0) {
            additionalPhase = 0;
            additionalPhaseStart = 0;
            onServer(player -> {
                additionalTargetExists = player.server.getLevel(gate.spec().dimension()) != null;
                if (!additionalTargetExists) {
                    expectTrue(gate.name() + "缺失目标返回空转换", ((DimensionPortalBlock) portal(gate))
                            .getPortalDestination(player.serverLevel(), player, gate.base()) == null);
                }
                enter(player, gate.base(), gate.axis());
            });
            return true;
        }
        if (!additionalTargetExists) {
            if (!waitFor(local, gate.name() + "碰撞拒绝", local >= 12
                    && read(player -> player.level().dimension().equals(Level.OVERWORLD)), 160)) return false;
            onServer(player -> {
                expect(gate.name() + "真实门户碰撞保持原维度", Level.OVERWORLD, player.level().dimension());
                expectTrue(gate.name() + "拒绝后仍处于当前门户内", player.position().distanceTo(center(gate.base(), gate.axis())) < 1);
                expectTrue(gate.name() + "拒绝没有创建默认目标维度", player.server.getLevel(gate.spec().dimension()) == null);
                park(player, gate.base(), gate.axis());
            });
            return true;
        }
        if (additionalPhase == 0) {
            if (!arrived(local, gate.spec().dimension(), gate.name() + "真实碰撞入场")) return false;
            onServer(player -> {
                BlockUtil.FoundRectangle rectangle = find(player.serverLevel(), player.blockPosition(), gate);
                verifyPortal(player.serverLevel(), rectangle.minCorner, gate, "目标");
                expectTrue(gate.name() + "落点处于本组目标门户内", player.position().distanceTo(center(rectangle.minCorner, gate.axis())) < 3);
                expectTrue(gate.name() + "原生传送冷却已设置", player.isOnPortalCooldown());
                exits.put(gate.spec(), rectangle.minCorner.immutable());
                park(player, rectangle.minCorner, gate.axis());
            });
            additionalPhase = 1;
            additionalPhaseStart = local;
            return false;
        }
        if (additionalPhase == 1) {
            if (!waitFor(local - additionalPhaseStart, gate.name() + "目标画面同步",
                    local - additionalPhaseStart >= 10 && minecraft.screen == null, 200)) return false;
            screenshots.take("dimensionportal_" + gate.spec().dimensionName() + "_exit.png");
            onServer(player -> enter(player, exits.get(gate.spec()), gate.axis()));
            additionalPhase = 2;
            additionalPhaseStart = local;
            return false;
        }
        if (!arrived(local - additionalPhaseStart, Level.OVERWORLD, gate.name() + "真实碰撞回程")) return false;
        onServer(player -> {
            BlockUtil.FoundRectangle rectangle = find(player.serverLevel(), player.blockPosition(), gate);
            expect(gate.name() + "回程复用原主世界门户", gate.base(), rectangle.minCorner);
            verifyPortal(player.serverLevel(), rectangle.minCorner, gate, "主世界");
            expectTrue(gate.name() + "回程落点在原入口内", player.position().distanceTo(center(gate.base(), gate.axis())) < 3);
            park(player, gate.base(), gate.axis());
        });
        return true;
    }

    private void prepare(ServerPlayer player) {
        originalDimension = player.level().dimension();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalAbilities = new CompoundTag();
        player.getAbilities().addSaveData(originalAbilities);
        originalSelected = player.getInventory().selected;
        originalCooldown = player.getPortalCooldown();
        originalNoGravity = player.isNoGravity();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        prepared = true;
        report.add("INFO 天道门户 边界：独立冒烟存档受控布置框架和定位玩家；真实原生useItemOn与碰撞传送；后续入门前受控清零冷却，不验证完整自然等待或多人");
        if (focusedGate != null) {
            String name = gate(focusedGate).name();
            report.add("INFO " + name + "门户 边界：六组主世界工具/门户仍检查；只进入" + name + "，检查首次及真实重进后的两次完整往返");
            if (player.server.getLevel(focusedGate.dimension()) == null) throw new IllegalStateException(name + "维度缺失");
        }
        player.closeContainer();
        player.getInventory().clearContent();
        player.teleportTo(player.server.overworld(), 8, 160, 3, 0, 0);
        player.setNoGravity(true);
        player.setGameMode(GameType.SURVIVAL);
        for (int slot = 0; slot < GATES.size(); slot++) {
            Gate gate = GATES.get(slot);
            buildFrame(player.serverLevel(), gate);
            player.getInventory().selected = slot;
            ItemStack tool = new ItemStack(BuiltInRegistries.ITEM.get(id("dimension" + gate.spec().dimensionName())));
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);
            park(player, gate.base(), gate.axis());
            BlockPos bottom = gate.base().below();
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(bottom).add(0, 0.5, 0), Direction.UP, bottom, false);
            InteractionResult result = player.gameMode.useItemOn(player, player.level(), tool, InteractionHand.MAIN_HAND, hit);
            expectTrue(gate.name() + "生存主手点门成功", result.consumesAction());
            expect(gate.name() + "实际工具扣一点耐久", 1, tool.getDamageValue());
            verifyPortal(player.serverLevel(), gate.base(), gate, "工具创建");
        }
        for (int index = 0; index < 2; index++) {
            Gate gate = GATES.get(index);
            if (player.server.getLevel(gate.spec().dimension()) == null) throw new IllegalStateException(gate.name() + "正式目标维度缺失");
        }
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.getInventory().selected = 0;
        player.inventoryMenu.broadcastChanges();
        park(player, GATES.getFirst().base(), GATES.getFirst().axis());
    }

    private static void buildFrame(ServerLevel level, Gate gate) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, gate.axis());
        BlockState frame = BuiltInRegistries.BLOCK.get(id("tiandaodimensionblock")).defaultBlockState();
        for (int width = -1; width <= 2; width++) {
            for (int height = -1; height <= 3; height++) {
                BlockPos pos = gate.base().relative(direction, width).above(height);
                level.setBlock(pos, width == -1 || width == 2 || height == -1 || height == 3
                        ? frame : Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private void verifyPortal(ServerLevel level, BlockPos base, Gate gate, String phase) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, gate.axis());
        int matching = 0;
        for (int width = 0; width < 2; width++) {
            for (int height = 0; height < 3; height++) {
                BlockState state = level.getBlockState(base.relative(direction, width).above(height));
                if (state.is(portal(gate)) && state.getValue(NetherPortalBlock.AXIS) == gate.axis()) matching++;
            }
        }
        expect(gate.name() + phase + "六格门户与轴向", 6, matching);
        expect(gate.name() + phase + "独立POI六点", 6,
                level.getPoiManager().getInSquare(holder -> holder.is(gate.spec().poiKey()), base, 128, PoiManager.Occupancy.ANY).count());
        BlockUtil.FoundRectangle rectangle = find(level, base, gate);
        expect(gate.name() + phase + "POI矩形起点", base, rectangle.minCorner);
        expectTrue(gate.name() + phase + "POI矩形2乘3", rectangle.axis1Size == 2 && rectangle.axis2Size == 3);
    }

    private void verifyTools(ServerPlayer player, String phase) {
        for (int slot = 0; slot < GATES.size(); slot++) {
            Gate gate = GATES.get(slot);
            ItemStack stack = player.getInventory().getItem(slot);
            expectTrue(gate.name() + phase + "保留实际工具及一点耐久", stack.is(BuiltInRegistries.ITEM.get(id("dimension" + gate.spec().dimensionName())))
                    && stack.getCount() == 1 && stack.getDamageValue() == 1);
        }
    }

    private static BlockUtil.FoundRectangle find(ServerLevel level, BlockPos pos, Gate gate) {
        return DimensionPortalRuntime.findPortalAround(level, pos, true, level.getWorldBorder(), gate.spec()).orElseThrow(
                () -> new IllegalStateException(gate.name() + "在" + level.dimension().location() + "找不到门户POI，坐标" + pos));
    }

    private boolean arrived(int local, ResourceKey<Level> dimension, String label) {
        return waitFor(local, label, minecraft.player != null && minecraft.level != null
                && minecraft.level.dimension().equals(dimension) && minecraft.screen == null
                && read(player -> player.level().dimension().equals(dimension)), 500);
    }

    private boolean waitFor(int local, String label, boolean ready, int timeout) {
        if (ready) return true;
        if (local > timeout) abort(label + "超时");
        return false;
    }

    private <T> T read(Function<ServerPlayer, T> action) {
        return minecraft.getSingleplayerServer().submit(() -> action.apply(minecraft.getSingleplayerServer().getPlayerList().getPlayers().getFirst())).join();
    }

    private static void enter(ServerPlayer player, BlockPos base, Direction.Axis axis) {
        player.setPortalCooldown(0);
        move(player, center(base, axis), axis);
    }

    private static void park(ServerPlayer player, BlockPos base, Direction.Axis axis) {
        Vec3 position = center(base, axis).add(axis == Direction.Axis.X ? new Vec3(0, 0, -3) : new Vec3(-3, 0, 0));
        move(player, position, axis);
    }

    private static void move(ServerPlayer player, Vec3 position, Direction.Axis axis) {
        player.connection.teleport(position.x, position.y, position.z, axis == Direction.Axis.X ? 0 : -90, 0);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
    }

    private static Vec3 center(BlockPos base, Direction.Axis axis) {
        return Vec3.atBottomCenterOf(base).add(axis == Direction.Axis.X ? new Vec3(0.5, 0, 0) : new Vec3(0, 0, 0.5));
    }

    private static Block portal(Gate gate) { return DimensionPortalRuntime.portalBlock(gate.spec()); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }

    private void abort(String message) {
        fail(message);
        if (minecraft.getSingleplayerServer() != null && minecraft.player != null) onServer(this::cleanup);
        finish();
    }

    private void cleanup(ServerPlayer player) {
        if (!prepared) return;
        player.setPortalCooldown(originalCooldown);
        player.setNoGravity(originalNoGravity);
        player.teleportTo(player.server.getLevel(originalDimension), originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = originalSelected;
        player.setGameMode(originalMode);
        player.getAbilities().loadSaveData(originalAbilities);
        player.onUpdateAbilities();
        player.inventoryMenu.broadcastChanges();
        prepared = false;
    }

    private record Gate(DimensionPortalSpec spec, BlockPos base, Direction.Axis axis, String name) {}
}
