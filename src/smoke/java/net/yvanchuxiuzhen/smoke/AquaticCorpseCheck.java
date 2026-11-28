package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;

/** 在一次性人间道存档中核对水生四族真实死亡后跨重进的采集次数与产物。 */
final class AquaticCorpseCheck extends SystemCheck {
    private static final List<String> TYPES = List.of("yaoshou_009", "yaoshou_028", "yaoshou_029", "yaoshou_031");
    private final List<Sample> samples = new ArrayList<>();

    private record Sample(String type, UUID id, boolean core) { }

    AquaticCorpseCheck(List<String> report, String levelId) {
        super("人间道水生四族尸体采集", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id("dimensionrenjian")));
                level.setBlockAndUpdate(new BlockPos(8, 179, 8), Blocks.STONE.defaultBlockState());
                player.teleportTo(level, 8.5, 180, 8.5, 0, 0);
                player.setGameMode(GameType.SURVIVAL);
            });
            case 1 -> {
                if (local < 20) return false;
                onServer(this::prepare);
            }
            case 2 -> leaveWorld();
            case 3 -> { return reopened(local); }
            case 4 -> { return inWorld(local); }
            case 5 -> {
                onServer(this::resume);
                finish();
            }
            default -> throw new IllegalStateException();
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        var level = player.serverLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        for (int i = 0; i < TYPES.size(); i++) {
            String type = TYPES.get(i);
            YaoshouEntity mob = CreatureSetup.<YaoshouEntity>entityType(type).get().create(level);
            mob.moveTo(7.5 + i, 181, 10.5, 0, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            mob.setNoAi(true);
            mob.setNoGravity(true);
            if (!level.addFreshEntity(mob) || !mob.hurt(mob.damageSources().genericKill(), Float.MAX_VALUE)
                    || mob.isAlive() || mob.isRemoved()) {
                fail(type + " 未能留下真实死亡尸体");
                continue;
            }
            collect(player, type, mob, 1, 2);
            samples.add(new Sample(type, mob.getUUID(), CreatureData.state(mob).beastCoreDropped()));
        }
        expect("重进前有四种尸体", 4, samples.size());
    }

    private void resume(ServerPlayer player) {
        expectTrue("重进后仍在人间道", player.level().dimension().location().equals(id("dimensionrenjian")));
        expectTrue("重进后仍持普通木棍且未潜行", player.getMainHandItem().is(Items.STICK) && !player.isShiftKeyDown());
        for (Sample sample : samples) {
            Entity entity = player.serverLevel().getEntity(sample.id());
            if (!(entity instanceof YaoshouEntity mob) || mob.isAlive() || !BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).equals(id(sample.type()))) {
                fail(sample.type() + " 重进后未找到同 UUID、同类型的尸体 " + sample.id());
                continue;
            }
            expect(sample.type() + " 重进后采集次数", 2, CreatureData.state(mob).harvestCount());
            expect(sample.type() + " 重进后妖丹标记", sample.core(), CreatureData.state(mob).beastCoreDropped());
            collect(player, sample.type(), mob, 3, 5);
        }
    }

    private void collect(ServerPlayer player, String type, YaoshouEntity mob, int start, int end) {
        var level = player.serverLevel();
        AABB box = new AABB(mob.blockPosition().above()).inflate(1);
        for (int n = start; n <= end; n++) {
            Set<UUID> existing = new HashSet<>();
            level.getEntitiesOfClass(ItemEntity.class, box).forEach(item -> existing.add(item.getUUID()));
            player.interactOn(mob, InteractionHand.MAIN_HAND);
            var items = level.getEntitiesOfClass(ItemEntity.class, box, item -> !existing.contains(item.getUUID()));
            expectTrue(type + " 第" + n + "次采集，新增落物=" + items.stream().map(item -> item.getItem().toString()).toList(),
                    items.size() == 1 && !items.getFirst().getItem().isEmpty() && CreatureData.state(mob).harvestCount() == n);
            expectTrue(type + " 第" + n + "次后的移除状态", n == 5 ? mob.getRemovalReason() == Entity.RemovalReason.DISCARDED : !mob.isRemoved());
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
