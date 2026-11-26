package net.yvanchuxiuzhen.system.zhenfa.content;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.zhenfa.ZhenfaSetup;
import net.yvanchuxiuzhen.system.zhenfa.api.FormationInspection;
import net.yvanchuxiuzhen.system.zhenfa.api.FormationProtection;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationActor;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationFalling;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationLauncher;

/** 阵法效果执行器；队伍允许状态在来源的整轮有序扫描中锁存。 */
final class FormationEffects {
    private FormationEffects() { }

    static void execute(FormationBaseBlockEntity base) {
        CompoundTag data = base.state();
        switch (data.getString("jinengleibie")) {
            case "阳" -> yang(base, data);
            case "阴", "金", "水", "木", "火", "土" -> yin(base, data);
            case "空间" -> space(base, data);
            default -> { }
        }
    }

    private static void yang(FormationBaseBlockEntity base, CompoundTag data) {
        ServerLevel level = (ServerLevel) base.getLevel();
        BlockPos origin = base.getBlockPos();
        Vec3 destination = targetWithoutRandom(data, origin);
        Vec3 offset = randomOffset(data, level);
        switch (data.getString("jinengname")) {
            case "阳1" -> {
                FormationLauncher launcher = new FormationLauncher(ZhenfaSetup.<FormationLauncher>entityType("zhenfashiti").get(), level);
                launcher.moveTo(destination.x + offset.x, destination.y + 2 + offset.y, destination.z + offset.z, level.random.nextFloat() * 360, 0);
                configureActor(base, launcher, "阳1");
                launcher.finalizeSpawn(level, level.getCurrentDifficultyAt(launcher.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                level.addFreshEntity(launcher);
            }
            case "阳2" -> {
                FormationProtection.add(level.getBlockEntity(BlockPos.containing(destination)), data.getInt("zhenLEVEL"));
                level.sendParticles(ParticleTypes.CLOUD, destination.x + .5, destination.y + .5, destination.z + .5, 5, .5, 3, .5, 1);
                level.sendParticles(ParticleTypes.CLOUD, origin.getX() + .5, origin.getY() + .5, origin.getZ() + .5, 5, .5, 3, .5, 1);
            }
            case "阳3" -> {
                Vec3 point = destination.add(offset).add(0, 32, 0);
                try {
                    SummonCommand.createEntity(level.getServer().createCommandSourceStack().withLevel(level),
                            ZhenfaSetup.entityType("zhenjineng_03").get().builtInRegistryHolder(), point, new CompoundTag(), true);
                } catch (CommandSyntaxException ignored) {
                    // 召唤被拒绝时仍检查原位置已有的落实体。
                }
                for (Entity entity : sortedEntities(level, point, .5)) {
                    if (entity instanceof FormationFalling actor) configureActor(base, actor, "阳3");
                }
            }
            case "阳4" -> {
                Vec3 point = new Vec3(origin.getX() + .5, origin.getY() + .5, origin.getZ() + .5).add(offset);
                level.sendParticles(ParticleTypes.FLASH, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, point.x, point.y, point.z, 3, .5, 0, .5, 0);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, point.x, point.y, point.z, 50, 1, 1, 1, 1);
                int rank = data.getInt("zhenLEVEL");
                for (Entity entity : sortedEntities(level, point, data.getInt("zhenjvli") / 2d)) {
                    SpellDamage.markFormationManaAttack(entity, rank * 10, "wu", .5 + rank * .5);
                }
                for (int z = -9; z <= 9; z++) {
                    for (int x = -9; x <= 9; x++) {
                        if (Math.random() >= .3) continue;
                        BlockPos pos = origin.offset(x, 0, z);
                        Block.dropResources(level.getBlockState(pos), level, pos, null);
                        level.destroyBlock(pos, false);
                    }
                }
            }
            default -> { }
        }
    }

    private static void configureActor(FormationBaseBlockEntity base, FormationActor entity, String name) {
        CompoundTag source = base.state();
        FormationMarkerBlockEntity marker = (FormationMarkerBlockEntity) base.getLevel().getBlockEntity(base.getBlockPos().above());
        Relations.setTeam(entity, marker.team());
        entity.configure(name, source.getInt("zhenLEVEL"), source.getInt("zhenmubiao"), source.getString("ManaAttackUUID"));
        entity.setYRot(source.getInt("zhenjiaodu"));
        entity.setXRot(source.getInt("zhentaitou"));
        entity.setYBodyRot(entity.getYRot());
        entity.setYHeadRot(entity.getYRot());
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();
        if (entity instanceof LivingEntity living) {
            living.yBodyRotO = living.getYRot();
            living.yHeadRotO = living.getYRot();
        }
    }

    private static void yin(FormationBaseBlockEntity base, CompoundTag data) {
        Level level = base.getLevel();
        BlockPos pos = base.getBlockPos();
        Vec3 center = target(data, pos, level);
        boolean allowed = false;
        for (Entity entity : sortedEntities(level, center, data.getInt("zhenjvli") / 2d)) {
            if (base.matchesTeam(entity)) allowed = true;
            if (!allowed) continue;
            int rank = data.getInt("zhenLEVEL");
            switch (data.getString("jinengname")) {
                case "阴1" -> Cultivation.addEntitySoul(entity, rank * 2);
                case "阴2" -> SpellDamage.markFormationSoulAttack(entity, rank);
                case "阴3" -> entity.discard();
                case "阴4" -> StatusEffects.markEntityNoAi(entity);
                default -> { }
            }
        }
    }

    private static void space(FormationBaseBlockEntity base, CompoundTag data) {
        Level level = base.getLevel();
        BlockPos origin = base.getBlockPos();
        Vec3 destination = target(data, origin, level);
        String name = data.getString("jinengname");
        if (name.equals("空间1") || name.equals("空间2") || name.equals("空间3")) {
            boolean allowed = false;
            for (Entity entity : sortedEntities(level, destination, data.getInt("zhenjvli") / 2d)) {
                if (base.matchesTeam(entity)) allowed = true;
                if (!allowed) continue;
                switch (name) {
                    case "空间1" -> {
                        if ((int) EntityAttributes.level(entity) <= data.getInt("zhenLEVEL") && entity instanceof LivingEntity living) {
                            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20, true, false));
                        }
                    }
                    case "空间2" -> StatusEffects.markEntityFlightBlocked(entity);
                    case "空间3" -> FormationInspection.report((ServerLevel) level, entity, data.getInt("zhenLEVEL"),
                            new Vec3(data.getInt("playerX"), data.getInt("playerY"), data.getInt("playerZ")));
                    default -> { }
                }
            }
            return;
        }

        Vec3 center = new Vec3(origin.getX(), origin.getY(), origin.getZ());
        boolean allowed = false;
        for (Entity entity : sortedEntities(level, center, data.getInt("zhenjvli") / 2d)) {
            if (base.matchesTeam(entity)) allowed = true;
            if (!allowed) continue;
            if (name.equals("空间4")) {
                entity.teleportTo(destination.x, destination.y, destination.z);
                if (entity instanceof ServerPlayer player) {
                    player.connection.teleport(destination.x, destination.y, destination.z, entity.getYRot(), entity.getXRot());
                }
            } else if (name.equals("空间5") && level instanceof ServerLevel serverLevel) {
                teleportAcrossDimension(serverLevel, origin, destination, entity, data);
            }
        }
    }

    private static Vec3 target(CompoundTag data, BlockPos origin, Level level) {
        return targetWithoutRandom(data, origin).add(randomOffset(data, level));
    }

    private static Vec3 targetWithoutRandom(CompoundTag data, BlockPos origin) {
        boolean absolute = data.getBoolean("zhenzuobiao");
        double x = data.getInt("zhenchufaX") + (absolute ? 0 : origin.getX());
        double y = data.getInt("zhenchufaY") + (absolute ? 0 : origin.getY());
        double z = data.getInt("zhenchufaZ") + (absolute ? 0 : origin.getZ());
        return new Vec3(x, y, z);
    }

    private static Vec3 randomOffset(CompoundTag data, Level level) {
        return data.getBoolean("zhensuiji")
                ? new Vec3(level.random.nextInt(11) - 5, level.random.nextInt(6), level.random.nextInt(11) - 5) : Vec3.ZERO;
    }

    private static List<Entity> sortedEntities(Level level, Vec3 center, double radius) {
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius)).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    private static void teleportAcrossDimension(ServerLevel level, BlockPos origin, Vec3 destination, Entity entity, CompoundTag data) {
        String dimension = "";
        boolean kun = data.getBoolean("zhenkun");
        boolean zhen = data.getBoolean("zhenzhen");
        boolean li = data.getBoolean("zhenli");
        boolean dui = data.getBoolean("zhendui");
        boolean qian = data.getBoolean("zhenqian");
        boolean xvn = data.getBoolean("zhenxvn");
        boolean kan = data.getBoolean("zhenkan");
        boolean gen = data.getBoolean("zhengen");
        int rank = data.getInt("zhenLEVEL");
        if (!kun && zhen && !li && dui && !qian && xvn && !kan && gen) {
            dimension = "minecraft:overworld";
        } else if (kun && !zhen && li && !dui && qian && !xvn && kan && !gen) {
            dimension = "minecraft:the_nether";
        } else if (kun && !qian) {
            if (rank >= 8) dimension = "yvanchuxiuzhen:dimensiondiling";
            else if (rank >= 6) dimension = "yvanchuxiuzhen:dimensionwugui";
            else if (rank >= 4) dimension = "yvanchuxiuzhen:dimensionyaohun";
        } else if (!kun && qian) {
            if (rank >= 8) dimension = "yvanchuxiuzhen:dimensiontianqve";
            else if (rank >= 6) dimension = "yvanchuxiuzhen:dimensionxiuluo";
            else if (rank >= 4) dimension = "yvanchuxiuzhen:dimensionrenjian";
        }
        if (dimension.isEmpty()) return;
        ServerLevel targetLevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)));
        if (targetLevel == null) return;
        // 显示名只解析为一个目标参数；选择器的参考点按目标维度缩放，绝对落点不缩放。
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, Vec3.atLowerCornerOf(origin), Vec2.ZERO, level,
                4, "", Component.empty(), level.getServer(), null).withLevel(targetLevel);
        StringReader name = new StringReader(entity.getDisplayName().getString());
        Collection<? extends Entity> targets;
        try {
            var selector = EntityArgument.entities().parse(name, source);
            if (name.canRead()) return;
            targets = selector.findEntities(source);
        } catch (CommandSyntaxException invalidTarget) {
            return;
        }
        for (Entity target : targets) {
            var event = EventHooks.onEntityTeleportCommand(target, destination.x, destination.y, destination.z);
            if (event.isCanceled()) continue;
            double x = event.getTargetX(), y = event.getTargetY(), z = event.getTargetZ();
            if (!Level.isInSpawnableBounds(BlockPos.containing(x, y, z))) break;
            if (target.teleportTo(targetLevel, x, y, z, Set.of(RelativeMovement.X_ROT, RelativeMovement.Y_ROT),
                    Mth.wrapDegrees(target.getYRot()), Mth.wrapDegrees(target.getXRot()))) {
                if (!(target instanceof LivingEntity living) || !living.isFallFlying()) {
                    target.setDeltaMovement(target.getDeltaMovement().multiply(1, 0, 1));
                    target.setOnGround(true);
                }
                if (target instanceof PathfinderMob mob) mob.getNavigation().stop();
            }
        }
    }
}
