package net.yvanchuxiuzhen.system.zhenfa.content;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.zhenfa.ZhenfaSetup;
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
                CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, point, Vec2.ZERO, level, 4,
                        "", Component.literal(""), level.getServer(), null).withSuppressedOutput();
                level.getServer().getCommands().performPrefixedCommand(source, "summon yvanchuxiuzhen:zhenjineng_03");
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
                    case "空间3" -> inspectFrom(entity, data);
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

    private static void inspectFrom(Entity entity, CompoundTag data) {
        if (entity.getServer() == null) return;
        String command = "zizhijiancefankui " + new DecimalFormat("0").format(data.getInt("zhenLEVEL")) + " "
                + new DecimalFormat("0").format(data.getInt("playerX")) + " "
                + new DecimalFormat("0").format(data.getInt("playerY")) + " "
                + new DecimalFormat("0").format(data.getInt("playerZ"));
        entity.getServer().getCommands().performPrefixedCommand(entity.createCommandSourceStack().withSuppressedOutput().withPermission(4), command);
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
        String command = "execute in " + dimension + " run teleport " + entity.getDisplayName().getString() + " "
                + destination.x + " " + destination.y + " " + destination.z;
        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, new Vec3(origin.getX(), origin.getY(), origin.getZ()),
                Vec2.ZERO, level, 4, "", Component.literal(""), level.getServer(), null).withSuppressedOutput();
        level.getServer().getCommands().performPrefixedCommand(source, command);
    }
}
