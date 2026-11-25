package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;

/** 预选两种亲代的召援技能，以正式逐刻回调生成援兵，检查同档重进后的服务端状态。 */
final class RenjianPackCheck extends SystemCheck {
    private record Child(UUID uuid, ResourceLocation type, double time) { }
    private final List<Child> children = new ArrayList<>();
    private final List<YaoshouEntity> parents = new ArrayList<>();

    RenjianPackCheck(List<String> report, String levelId) { super("人间道狼猴召援", report, levelId); }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                var level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id("dimensionrenjian")));
                for (BlockPos p : BlockPos.betweenClosed(1, 179, 1, 15, 179, 15)) level.setBlockAndUpdate(p, Blocks.STONE.defaultBlockState());
                player.teleportTo(level, 8.5, 180, 8.5, 0, 0);
            });
            case 1 -> {
                if (local < 20) return false;
                onServer(player -> {
                    parent(player, "yaoshou_008", "召唤小狼", 5);
                    parent(player, "yaoshou_035", "召唤小猴", 11);
                });
            }
            case 2 -> {
                if (local < 25) return false;
                onServer(this::observe);
            }
            case 3 -> leaveWorld();
            case 4 -> { return reopened(local); }
            case 5 -> { return inWorld(local); }
            case 6 -> {
                onServer(this::restored);
                finish();
            }
            default -> throw new IllegalStateException();
        }
        return true;
    }

    private void parent(ServerPlayer player, String type, String skill, int x) {
        var level = player.serverLevel();
        var mob = CreatureSetup.<YaoshouEntity>entityType(type).get().create(level);
        mob.moveTo(x + 0.5, 180, 9.5, 0, 0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(mob);
        var data = CreatureData.state(mob);
        data.setAttackerUuid(player.getStringUUID());
        data.setTempAttackerUuid(player.getStringUUID());
        data.setSkill(skill);
        data.setTick(0);
        parents.add(mob);
    }

    private void observe(ServerPlayer player) {
        var level = player.serverLevel();
        for (int i = 0; i < 2; i++) {
            var type = id(i == 0 ? "yaoshou_007" : "yaoshou_013");
            var mobs = level.getEntitiesOfClass(YaoshouEntity.class, new AABB(player.blockPosition()).inflate(24),
                    mob -> BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).equals(type));
            expect(type + " 召出两只", 2, mobs.size());
            for (var mob : mobs) {
                var data = CreatureData.state(mob);
                expect(type + " 继承半级向上取整", Math.ceil(EntityAttributes.level(parents.get(i)) / 2), EntityAttributes.level(mob));
                double time = data.countdown();
                expectTrue(type + " 召援倒计时已启动", time > 0 && time < 600);
                if (i == 0) expect("召唤月狼外观", "yaoshou00702", mob.texture());
                children.add(new Child(mob.getUUID(), type, time));
            }
        }
        parents.forEach(YaoshouEntity::discard);
        parents.clear();
    }

    private void restored(ServerPlayer player) {
        for (Child child : children) {
            if (!(player.serverLevel().getEntity(child.uuid()) instanceof YaoshouEntity mob)) {
                fail(child.type() + " 重进后未找到援兵 " + child.uuid());
                continue;
            }
            expect("援兵保持类型", child.type(), BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
            double time = CreatureData.state(mob).countdown();
            expectTrue(child.type() + " 重进后倒计时继续：" + time, time > 0 && time < child.time());
            if (child.type().equals(id("yaoshou_007"))) expect("同档恢复月狼外观", "yaoshou00702", mob.texture());
        }
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
}
