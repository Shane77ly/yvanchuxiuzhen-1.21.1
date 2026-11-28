package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou003Entity;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 受控的003真实死亡、普通右键与采集上限；自然出生另由独立夹具覆盖。 */
final class RenjianCorpseHarvestCheck extends SystemCheck {
    private static final ResourceKey<Level> RENJIAN = ResourceKey.create(Registries.DIMENSION, id("dimensionrenjian"));
    private static final ResourceLocation CORE = id("cailiao_181");
    private static final ResourceLocation HIDE = id("cailiao_001");
    private static final Set<ResourceLocation> PRODUCTS = Set.of(CORE, HIDE,
            ResourceLocation.withDefaultNamespace("mutton"), ResourceLocation.withDefaultNamespace("bone"));

    RenjianCorpseHarvestCheck(List<String> report, String levelId) {
        super("人间道长风原野羊尸体采集", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(player -> {
            ServerLevel renjian = player.server.getLevel(RENJIAN);
            if (renjian == null) throw new IllegalStateException("缺少人间道");
            if (player.serverLevel() != renjian) {
                player.teleportTo(renjian, 8.5, 180, 8.5, 0, 0);
                return;
            }
            renjian.setBlockAndUpdate(new BlockPos(8, 179, 8), Blocks.STONE.defaultBlockState());
            player.setGameMode(GameType.SURVIVAL);
            player.connection.teleport(8.5, 180, 8.5, 0, 0);
            check(player, false);
            check(player, true);
            finish();
        });
        return false;
    }

    private void check(ServerPlayer player, boolean coreTaken) {
        ServerLevel level = player.serverLevel();
        Vec3 position = Vec3.atBottomCenterOf(player.blockPosition()).add(0, 6.5, -2);
        Yaoshou003Entity sheep = CreatureSetup.<Yaoshou003Entity>entityType("yaoshou_003").get().create(level);
        if (sheep == null) {
            fail("未创建受控003");
            return;
        }
        sheep.moveTo(position.x, position.y, position.z, 0, 0);
        if (!level.noCollision(sheep, sheep.getBoundingBox())) {
            fail("003夹具位置不空；从附近有上方空位的场地入场");
            return;
        }
        sheep.finalizeSpawn(level, level.getCurrentDifficultyAt(sheep.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        sheep.setNoAi(true);
        sheep.setNoGravity(true);
        if (!level.addFreshEntity(sheep)) {
            fail("受控003未入场");
            return;
        }
        var data = CreatureData.state(sheep);
        if (coreTaken) data.setBeastCoreDropped(true);
        var items = new ArrayList<ItemEntity>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            Vec3 dropPosition = new Vec3(sheep.getX(), sheep.blockPosition().getY() + 1, sheep.getZ());
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                    && item.position().distanceToSqr(dropPosition) < 1e-6) items.add(item);
        };
        ItemStack hand = player.getMainHandItem();
        boolean sneaking = player.isShiftKeyDown();
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(false);
        NeoForge.EVENT_BUS.addListener(listener);
        String label = coreTaken ? "已有妖丹" : "新尸体";
        try {
            player.interactOn(sheep, InteractionHand.MAIN_HAND);
            expectTrue(label + "活体不采集", items.isEmpty() && data.harvestCount() == 0);
            if (!sheep.hurt(sheep.damageSources().genericKill(), Float.MAX_VALUE) || sheep.isAlive() || sheep.isRemoved()) {
                fail(label + "未走真实死亡且保留尸体");
                return;
            }
            expectTrue(label + "普通死亡未在采集坐标发放采集品", items.isEmpty());
            for (int count = 1; count <= 5; count++) {
                player.interactOn(sheep, InteractionHand.MAIN_HAND);
                expectTrue(label + "第" + count + "次采集计数和落物", count == data.harvestCount() && count == items.size());
                if (count < 5) expectTrue(label + "前四次保留尸体", !sheep.isRemoved());
            }
            expectTrue(label + "第五次直接移除", sheep.getRemovalReason() == Entity.RemovalReason.DISCARDED);
            expectTrue(label + "产物来自003表、落物延迟10刻和材料实例字段", items.stream().allMatch(item -> {
                ItemStack stack = item.getItem();
                ResourceLocation kind = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (!PRODUCTS.contains(kind) || item.saveWithoutId(new CompoundTag()).getInt("PickupDelay") != 10) return false;
                if (!kind.equals(HIDE) && !kind.equals(CORE)) return true;
                var material = Materials.instance(stack);
                return material.level() == (int) EntityAttributes.level(sheep) && material.quality() >= 0 && material.quality() <= 4;
            }));
            long cores = items.stream().filter(item -> BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).equals(CORE)).count();
            expectTrue(label + "妖丹上限及持久标记", coreTaken ? cores == 0 : cores <= 1 && (cores == 1) == data.beastCoreDropped());
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
            player.setItemInHand(InteractionHand.MAIN_HAND, hand);
            player.setShiftKeyDown(sneaking);
            items.forEach(Entity::discard);
            sheep.discard();
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path);
    }
}
