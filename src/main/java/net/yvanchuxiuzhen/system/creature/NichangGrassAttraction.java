package net.yvanchuxiuzhen.system.creature;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.plant.api.NichangGrassBloomEvent;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 霓裳草开花的妖兽召引：最近活体仇恨、来源群系七槽表与最多16次选址。 */
final class NichangGrassAttraction {
    private static final String TEAM = "xiyinerlai";
    private static final TagKey<EntityType<?>> BEASTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private NichangGrassAttraction() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(NichangGrassAttraction::bloom);
    }

    private static void bloom(NichangGrassBloomEvent event) {
        ServerLevel level = event.level();
        BlockPos pos = event.pos();
        Vec3 center = Vec3.atLowerCornerOf(pos);
        List<Entity> nearby = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(16))
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
        Entity target = nearby.stream().filter(entity -> entity instanceof Player && entity.isAlive()).findFirst().orElse(null);
        if (target == null) {
            target = nearby.stream().filter(entity -> entity instanceof Mob && entity.isAlive()).findFirst().orElse(null);
        } else {
            for (Entity entity : nearby) {
                if (entity.isAlive() && entity.getType().is(BEASTS)) {
                    Relations.addFriendlyTeam(entity, TEAM);
                    entity.getPersistentData().putString("linshigongjizhe", target.getStringUUID());
                    Relations.setTeam(entity, TEAM);
                }
            }
        }
        ResourceLocation biome = level.getBiome(pos).unwrapKey().orElseThrow().location();
        int[] beasts = biome.getNamespace().equals(ModId.VALUE) ? group(biome.getPath(), new Random().nextInt(1, 101)) : new int[0];
        BlockPos spawn = findPosition(level, pos);
        if (spawn == null) {
            for (Entity entity : nearby) {
                if (entity instanceof Player player && player.isAlive()) {
                    player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.nichanggrass.silent"), false);
                }
            }
            return;
        }
        Entity sharedAllegiance = null;
        for (int number : beasts) {
            Entity entity = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                    "yaoshou_%03d".formatted(number))).create(level);
            entity.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), level.random.nextFloat() * 360, 0);
            if (target != null) {
                if (sharedAllegiance == null) {
                    Relations.addFriendlyTeam(entity, TEAM);
                    sharedAllegiance = entity;
                } else {
                    Relations.shareEntityAllegiance(sharedAllegiance, entity);
                }
                Relations.setTeam(entity, TEAM);
                entity.getPersistentData().putString("linshigongjizhe", target.getStringUUID());
                entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
            }
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            }
            level.addFreshEntity(entity);
        }
    }

    /** 来源只要求可见天空且脚下不是LiquidBlock；不另加地面、碰撞或生物出生条件。 */
    private static BlockPos findPosition(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos pos = origin.offset(new Random().nextInt(-16, 17), 0, new Random().nextInt(-16, 17));
            for (int step = 0; step < 16 && !level.canSeeSkyFromBelowWater(pos); step++) {
                pos = pos.above();
            }
            if (level.canSeeSkyFromBelowWater(pos) && !(level.getBlockState(pos.below()).getBlock() instanceof LiquidBlock)) {
                return pos;
            }
        }
        return null;
    }

    /** 来源逐次随机为1–100；50门槛为51%，翡翠林02的66/33门槛分别为35%/33%/32%。 */
    private static int[] group(String biome, int roll) {
        boolean upper = roll >= 50;
        return switch (biome) {
            case "biomewind_01" -> upper ? new int[] {3, 4, 4, 4} : new int[] {8, 7, 7, 7, 7};
            case "biomewind_02" -> upper ? new int[] {3, 4, 4, 4} : new int[] {11, 5, 5, 5};
            case "biomewind_03" -> upper ? new int[] {14, 14, 14} : new int[] {13, 13, 13, 13, 13};
            case "biomewind_04" -> upper ? new int[] {28, 31, 31} : new int[] {4, 5, 5, 6, 6};
            case "biomepeach_01" -> upper ? new int[] {15, 15, 16, 16} : new int[] {35, 13, 13, 13, 13};
            case "biomepeach_02" -> upper ? new int[] {48, 6, 6, 15} : new int[] {15, 15, 15};
            case "biomeautumn_01" -> upper ? new int[] {4, 5, 5} : new int[] {12, 5, 5};
            case "biomeautumn_02" -> upper ? new int[] {28, 28, 17, 17, 17} : new int[] {36, 36, 36};
            case "biomeautumn_03" -> new int[] {2, 2, 2, 1, 1, 1, 1};
            case "biomejade_01" -> upper ? new int[] {33, 33, 16, 16, 16, 24} : new int[] {37, 24, 24};
            case "biomejade_02" -> roll >= 66 ? new int[] {33, 33, 22, 22, 22}
                    : roll >= 33 ? new int[] {38, 16, 16, 16} : new int[] {23, 23, 23, 21, 21};
            case "biomejade_03" -> upper ? new int[] {31, 31, 21, 21, 21} : new int[] {26, 10, 10, 10};
            case "biomecloud_01" -> upper ? new int[] {30, 30, 30, 30, 30} : new int[] {30, 30, 27, 25, 25};
            case "biomecloud_02" -> upper ? new int[] {6, 6, 18, 18, 18} : new int[] {40, 39, 39, 39};
            case "biomecloud_03" -> upper ? new int[] {42, 42, 41, 41, 41} : new int[] {43, 41, 41, 41, 41};
            case "biomestone_01" -> new int[] {47, 46, 46};
            case "biomestone_02" -> new int[] {17, 17, 17, 17, 17};
            case "biomestone_03" -> new int[] {32, 45, 45, 45};
            case "biomewinter_01" -> upper ? new int[] {52, 52, 52, 50, 50} : new int[] {51, 52, 52, 50, 50};
            case "biomewinter_02" -> upper ? new int[] {50, 50, 54, 54} : new int[] {53, 49, 49, 49, 49};
            case "biomewinter_03" -> new int[] {53, 51, 54, 52, 49, 50, 49};
            default -> new int[0];
        };
    }
}
