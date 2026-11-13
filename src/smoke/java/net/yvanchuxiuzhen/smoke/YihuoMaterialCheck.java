package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectile;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectiles;

/** 固定弹体出生点与方向，由自然游戏刻检查异火补燃料与首标签选择。 */
final class YihuoMaterialCheck extends ActionCheck {
    private BlockPos hitPos;
    private BlockPos abovePos;
    private BlockPos belowPos;
    private final List<BlockState> originalBlocks = new ArrayList<>();
    private final List<CompoundTag> originalBlockEntities = new ArrayList<>();
    private boolean sneaking;
    private YihuoProjectile projectile;
    private BlockPos observedHit;
    private final Consumer<ProjectileImpactEvent> impactListener = event -> {
        if (event.getProjectile() == projectile && !projectile.level().isClientSide()
                && event.getRayTraceResult() instanceof BlockHitResult hit) {
            observedHit = hit.getBlockPos();
        }
    };

    YihuoMaterialCheck(List<String> report, String levelId) {
        super("异火弹体", report, levelId);
        NeoForge.EVENT_BUS.addListener(impactListener);
        script(new Action(2, this::prepare),
                new Action(5, player -> startCampfire(player, 221)),
                new Action(2, player -> finishFuel(player, "221营火", abovePos, 17)),
                new Action(5, player -> startCampfire(player, 239)),
                new Action(2, player -> finishFuel(player, "239营火", abovePos, 2567)),
                new Action(5, this::startOwnBlock),
                new Action(2, player -> finishFuel(player, "221自有火块", abovePos, 20)),
                new Action(5, this::startFirstWithoutBlockEntity),
                new Action(2, player -> finishFuel(player, "首标签无BE，后方火块", belowPos, 10)));
    }

    private void prepare(ServerPlayer player) {
        sneaking = player.isShiftKeyDown();
        player.setShiftKeyDown(false);
        hitPos = player.blockPosition().east(4);
        abovePos = hitPos.above();
        belowPos = hitPos.below();
        ServerLevel level = player.serverLevel();
        for (BlockPos pos : List.of(hitPos, abovePos, belowPos)) {
            originalBlocks.add(level.getBlockState(pos));
            BlockEntity block = level.getBlockEntity(pos);
            originalBlockEntities.add(block == null ? null : block.saveWithFullMetadata(player.registryAccess()));
        }
        level.setBlock(hitPos, Blocks.STONE.defaultBlockState(), 2);
    }

    private void startCampfire(ServerPlayer player, int material) {
        player.serverLevel().setBlock(abovePos, Blocks.CAMPFIRE.defaultBlockState(), 2);
        setPower(player.serverLevel(), abovePos, 7);
        launch(player, material);
    }

    private void startOwnBlock(ServerPlayer player) {
        player.serverLevel().setBlock(abovePos, fireBlock().defaultBlockState(), 2);
        setPower(player.serverLevel(), abovePos, 10);
        launch(player, 221);
    }

    private void startFirstWithoutBlockEntity(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.setBlock(abovePos, Blocks.FIRE.defaultBlockState(), 2);
        level.setBlock(belowPos, fireBlock().defaultBlockState(), 2);
        setPower(level, belowPos, 10);
        launch(player, 221);
    }

    private void launch(ServerPlayer player, int material) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = hitPos.getCenter().add(-3, 0, 0);
        BlockHitResult ray = level.clip(new ClipContext(origin, hitPos.getCenter(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        expect("固定发射射线首命中", hitPos, ray.getBlockPos());
        observedHit = null;
        projectile = YihuoProjectiles.type(material).get().create(level);
        projectile.setOwner(player);
        projectile.setPos(origin);
        projectile.shoot(1, 0, 0, 2, 0);
        level.addFreshEntity(projectile);
    }

    private void finishFuel(ServerPlayer player, String label, BlockPos pos, double expected) {
        expect(label + "弹体实际命中", hitPos, observedHit);
        BlockEntity block = player.serverLevel().getBlockEntity(pos);
        expect(label + "燃料", expected, block.getPersistentData().getDouble("power"));
        projectile.discard();
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        NeoForge.EVENT_BUS.unregister(impactListener);
        if (projectile != null) {
            projectile.discard();
        }
        List<BlockPos> positions = List.of(hitPos, abovePos, belowPos);
        ServerLevel level = player.serverLevel();
        for (int index = 0; index < positions.size(); index++) {
            BlockPos pos = positions.get(index);
            BlockState state = originalBlocks.get(index);
            level.setBlock(pos, state, 3);
            CompoundTag data = originalBlockEntities.get(index);
            if (data != null) {
                BlockEntity restored = BlockEntity.loadStatic(pos, state, data, player.registryAccess());
                level.setBlockEntity(restored);
                restored.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
        }
        player.setShiftKeyDown(sneaking);
    }

    private static Block fireBlock() {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_221fireblock"));
    }

    private static void setPower(ServerLevel level, BlockPos pos, double value) {
        BlockEntity block = level.getBlockEntity(pos);
        block.getPersistentData().putDouble("power", value);
        block.setChanged();
        level.sendBlockUpdated(pos, block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
    }
}
