package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ScheduledTick;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.collection.tiandao.MultiStructureBlockEntity;

/** 多结构生成器的原生放置、空配置、真实保存重进与显式寻木生成必要单项。 */
final class TiandaoMultiStructureCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final Map<BlockPos, SavedBlock> blocks = new LinkedHashMap<>();
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ScheduledTick<Block>> blockTicks = new ArrayList<>();
    private final List<ScheduledTick<Fluid>> fluidTicks = new ArrayList<>();
    private BlockPos origin;
    private BoundingBox area;
    private Vec3 position;
    private Vec3 velocity;
    private float yaw;
    private float pitch;
    private int selected;
    private GameType mode;
    private boolean noGravity;
    private boolean flying;
    private boolean sneaking;
    private long deadline;
    private boolean ready;
    private boolean reached;

    TiandaoMultiStructureCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("天道多结构生成器", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(this::prepare);
                if (!ready) {
                    onServer(this::cleanup);
                    finish();
                }
            }
            case 1 -> {
                if (!elapsed(local)) return false;
                onServer(player -> {
                    expectTrue("普通放置经过两轮计划刻仍为空分组", generator(player).group().isEmpty());
                    expectTrue("Q199空配置保持空转并继续安排计划刻", player.serverLevel().getBlockTicks().hasScheduledTick(origin, generatorBlock()));
                    expect("空配置没有生成寻木", 0, woodCount(player));
                    generator(player).setGroup("受控保存分组");
                });
                leaveWorld();
            }
            case 2 -> { return reopened(local); }
            case 3 -> {
                if (!inWorld(local)) return false;
                onServer(player -> {
                    ready = player.level().getBlockEntity(origin) instanceof MultiStructureBlockEntity;
                    expectTrue("真实保存重进恢复多结构方块实体", ready);
                    if (!ready) return;
                    expect("真实保存重进恢复自定义分组", "受控保存分组", generator(player).group());
                    expectTrue("重进后来源计划刻仍持续", player.serverLevel().getBlockTicks().hasScheduledTick(origin, generatorBlock()));
                    expect("不匹配分组不生成寻木", 0, woodCount(player));
                    generator(player).setGroup("寻木");
                    deadline = player.serverLevel().getGameTime() + 25;
                });
                if (!ready) {
                    onServer(this::cleanup);
                    finish();
                }
            }
            case 4 -> {
                if (!elapsed(local)) return false;
                onServer(player -> {
                    expectTrue("显式寻木分组经真实计划刻摆放来源模板", woodCount(player) > 0);
                    player.connection.teleport(origin.getX() + 20.5, origin.getY() + 35, origin.getZ() - 20.5, 45, 15);
                    player.setDeltaMovement(Vec3.ZERO);
                });
            }
            case 5 -> {
                if (local < 15) return false;
                expect("生成器最终合并中文", "天道多结构生成器", I18n.get("block.yvanchuxiuzhen.tiandaoduojiegou"));
                screenshots.take("tiandao_multi_structure_wood.png");
                onServer(this::cleanup);
                finish();
            }
            default -> throw new IllegalStateException("未知多结构检查步骤 " + step);
        }
        return true;
    }

    private boolean elapsed(int local) {
        onServer(player -> reached = player.serverLevel().getGameTime() >= deadline);
        if (!reached && local > 200) {
            fail("服务端计划刻等待超时");
            onServer(this::cleanup);
            finish();
        }
        return reached;
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        player.stopUsingItem();
        position = player.position();
        velocity = player.getDeltaMovement();
        yaw = player.getYRot();
        pitch = player.getXRot();
        mode = player.gameMode.getGameModeForPlayer();
        selected = player.getInventory().selected;
        noGravity = player.isNoGravity();
        flying = player.getAbilities().flying;
        sneaking = player.isShiftKeyDown();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.setNoGravity(true);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        origin = new BlockPos(player.getBlockX(), Math.min(player.getBlockY() + 24, player.level().getMaxBuildHeight() - 70), player.getBlockZ());
        area = BoundingBox.fromCorners(origin.offset(-4, -4, -4), origin.offset(9, 68, 9));
        ServerLevel level = player.serverLevel();
        for (int x = area.minX() >> 4; x <= (area.maxX() >> 4); x++) {
            for (int z = area.minZ() >> 4; z <= (area.maxZ() >> 4); z++) {
                LevelChunkTicks<Block> queuedBlocks = (LevelChunkTicks<Block>) level.getChunk(x, z).getBlockTicks();
                LevelChunkTicks<Fluid> queuedFluids = (LevelChunkTicks<Fluid>) level.getChunk(x, z).getFluidTicks();
                blockTicks.addAll(queuedBlocks.getAll().filter(tick -> area.isInside(tick.pos())).toList());
                fluidTicks.addAll(queuedFluids.getAll().filter(tick -> area.isInside(tick.pos())).toList());
            }
        }
        for (BlockPos at : BlockPos.betweenClosed(origin.offset(-4, -4, -4), origin.offset(9, 68, 9))) {
            BlockPos pos = at.immutable();
            BlockEntity previous = player.level().getBlockEntity(pos);
            blocks.put(pos, new SavedBlock(player.level().getBlockState(pos), previous == null ? null : previous.saveWithFullMetadata(player.registryAccess())));
        }
        level.getBlockTicks().clearArea(area);
        level.getFluidTicks().clearArea(area);
        for (BlockPos pos : blocks.keySet()) {
            player.level().removeBlockEntity(pos);
            player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
        }
        player.level().setBlock(origin.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        player.connection.teleport(origin.getX() + .5, origin.getY(), origin.getZ() - 2, 0, 15);
        player.setDeltaMovement(Vec3.ZERO);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(generatorBlock()));
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(origin.below()), Direction.UP, origin.below(), false));
        ready = player.level().getBlockState(origin).is(generatorBlock()) && player.level().getBlockEntity(origin) instanceof MultiStructureBlockEntity;
        expectTrue("方块物品原生放置并扣除一件", ready && player.getMainHandItem().isEmpty());
        if (!ready) return;
        expect("负硬度转换恢复不可挖掘属性", -1, player.level().getBlockState(origin).getDestroySpeed(player.level(), origin));
        for (Direction direction : Direction.values()) {
            IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, origin, direction);
            expectTrue("来源零槽能力 " + direction, handler != null && handler.getSlots() == 0);
        }
        IItemHandler unsided = player.level().getCapability(Capabilities.ItemHandler.BLOCK, origin, null);
        expectTrue("无方向零槽能力保留父类契约", unsided != null && unsided.getSlots() == 0);
        deadline = player.serverLevel().getGameTime() + 45;
        player.inventoryMenu.broadcastChanges();
    }

    private int woodCount(ServerPlayer player) {
        Block wood = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "ancienttimeswood"));
        int count = 0;
        for (BlockPos pos : blocks.keySet()) if (player.level().getBlockState(pos).is(wood)) count++;
        return count;
    }

    private void cleanup(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.getBlockTicks().clearArea(area);
        level.getFluidTicks().clearArea(area);
        for (Map.Entry<BlockPos, SavedBlock> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            SavedBlock saved = entry.getValue();
            level.removeBlockEntity(pos);
            int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), flags);
            level.setBlock(pos, saved.state(), flags);
            if (saved.data() != null) {
                BlockEntity restored = BlockEntity.loadStatic(pos, saved.state(), saved.data(), player.registryAccess());
                level.setBlockEntity(restored);
                restored.setChanged();
                level.sendBlockUpdated(pos, saved.state(), saved.state(), Block.UPDATE_CLIENTS);
            }
        }
        level.getBlockTicks().clearArea(area);
        level.getFluidTicks().clearArea(area);
        blockTicks.forEach(level.getBlockTicks()::schedule);
        fluidTicks.forEach(level.getFluidTicks()::schedule);
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setGameMode(mode);
        player.getAbilities().flying = flying;
        player.onUpdateAbilities();
        player.setShiftKeyDown(sneaking);
        player.setNoGravity(noGravity);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        player.setDeltaMovement(velocity);
        player.inventoryMenu.broadcastChanges();
    }

    private MultiStructureBlockEntity generator(ServerPlayer player) { return (MultiStructureBlockEntity) player.level().getBlockEntity(origin); }
    private static Block generatorBlock() { return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "tiandaoduojiegou")); }
    private record SavedBlock(BlockState state, CompoundTag data) { }
}
