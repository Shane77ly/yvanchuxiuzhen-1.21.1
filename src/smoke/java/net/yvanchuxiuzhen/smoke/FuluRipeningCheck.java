package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 荫翠符共享付款、空间边界、催熟分支与霓裳草真实召引的必要单项。 */
final class FuluRipeningCheck extends ActionCheck {
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final List<Entity> created = new ArrayList<>();
    private final Set<UUID> before = new HashSet<>();
    private Vec3 originalPosition;
    private float yaw;
    private float pitch;
    private float headYaw;
    private float bodyYaw;
    private GameType mode;
    private ItemStack hand;
    private double soul;
    private double fixed;
    private double percentage;
    private boolean noGravity;
    private int randomTickSpeed;
    private BlockPos center;
    private BlockPos flower;
    private BlockPos sapling;
    private BlockPos wheat;
    private BlockPos taggedCrop;
    private BlockPos outside;
    private BlockPos upper;
    private BlockPos aboveRange;
    private final Map<BlockPos, String> originalBiomes = new LinkedHashMap<>();
    private Entity existingBeast;
    private int bonemealCalls;
    private final Consumer<BonemealEvent> boneMealListener = event -> {
        if (event.getPos().equals(sapling)) {
            bonemealCalls++;
            event.setSuccessful(true);
        }
    };

    FuluRipeningCheck(List<String> report, String levelId) {
        super("荫翠符", report, levelId);
        script(new Action(2, this::prepare), new Action(2, this::insufficient),
                new Action(2, this::ripen), new Action(2, this::emptyRange));
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        headYaw = player.getYHeadRot();
        bodyYaw = player.yBodyRot;
        mode = player.gameMode.getGameModeForPlayer();
        hand = player.getMainHandItem().copy();
        soul = Cultivation.get(player, CultivationValue.SOUL);
        fixed = Cultivation.tizhiDouble(player, "FuluConsumeJingli");
        percentage = Cultivation.tizhiDouble(player, "FuluConsumeJingliPercentage");
        noGravity = player.isNoGravity();
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        Cultivation.editTizhi(player, data -> {
            data.putDouble("FuluConsumeJingli", 0);
            data.putDouble("FuluConsumeJingliPercentage", 0);
        });
        player.setGameMode(GameType.SURVIVAL);
        player.teleportTo(originalPosition.x, player.serverLevel().getMaxBuildHeight() - 16, originalPosition.z);
        aim(player, -90);
        center = BlockPos.containing(player.getEyePosition()).east(4);
        ServerLevel level = player.serverLevel();
        randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, player.server);
        for (int distance = 0; distance < 4; distance++) {
            put(level, center.west(distance + 1), Blocks.AIR.defaultBlockState());
        }
        put(level, center, Blocks.STONE.defaultBlockState());
        for (int stage = 1; stage <= 6; stage++) {
            plant(level, center.offset(stage - 3, -1, 2), block("nichanggrass_0" + stage));
        }
        flower = center.offset(3, -1, 2);
        outside = center.offset(7, -1, 2);
        upper = center.offset(0, 2, 4);
        aboveRange = center.offset(2, 3, 4);
        plant(level, outside, block("nichanggrass_03"));
        plant(level, upper, block("nichanggrass_01"));
        plant(level, aboveRange, block("nichanggrass_01"));
        wheat = center.offset(-3, -1, -2);
        put(level, wheat.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
        put(level, wheat, Blocks.WHEAT.defaultBlockState());
        sapling = center.offset(2, -1, -2);
        plant(level, sapling, Blocks.OAK_SAPLING);
        taggedCrop = center.offset(0, -1, -3);
        put(level, taggedCrop.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
        put(level, taggedCrop, block("shicai_011_plant_01").defaultBlockState());
        BlockPos biomeMin = flower.offset(-8, -8, -8);
        BlockPos biomeMax = flower.offset(8, 8, 8);
        for (int x = QuartPos.fromBlock(biomeMin.getX()); x <= QuartPos.fromBlock(biomeMax.getX()); x++) {
            for (int y = QuartPos.fromBlock(biomeMin.getY()); y <= QuartPos.fromBlock(biomeMax.getY()); y++) {
                for (int z = QuartPos.fromBlock(biomeMin.getZ()); z <= QuartPos.fromBlock(biomeMax.getZ()); z++) {
                    originalBiomes.put(new BlockPos(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z)),
                            level.getNoiseBiome(x, y, z).unwrapKey().orElseThrow().location().toString());
                }
            }
        }
        fillBiome(player, biomeMin, biomeMax, "yvanchuxiuzhen:biomeautumn_03");
        expect("召引群系夹具", "yvanchuxiuzhen:biomeautumn_03", level.getBiome(flower).unwrapKey().orElseThrow().location().toString());
        existingBeast = BuiltInRegistries.ENTITY_TYPE.get(id("yaoshou_001")).create(level);
        existingBeast.moveTo(center.getX() + 1, center.getY(), center.getZ() - 4, 0, 0);
        ((Mob) existingBeast).setNoAi(true);
        level.addFreshEntity(existingBeast);
        created.add(existingBeast);
        NeoForge.EVENT_BUS.addListener(boneMealListener);
        Vec3 eye = player.getEyePosition();
        expect("六格射线首命中", center, level.clip(new ClipContext(eye, eye.add(player.getViewVector(1).scale(6)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos());
    }

    private void insufficient(ServerPlayer player) {
        aim(player, -90);
        ItemStack stack = talisman(2, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Cultivation.set(player, CultivationValue.SOUL, 39);
        stack.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        expect("不足40精力不扣费", 39, Cultivation.get(player, CultivationValue.SOUL));
        expect("不足精力不耗符", 2, stack.getCount());
        expectTrue("不足精力不推进", player.level().getBlockState(flower).is(block("nichanggrass_06")));
    }

    private void ripen(ServerPlayer player) {
        aim(player, -90);
        ServerLevel level = player.serverLevel();
        level.getAllEntities().forEach(entity -> before.add(entity.getUUID()));
        ItemStack stack = talisman(2, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Cultivation.set(player, CultivationValue.SOUL, 500);
        stack.getItem().use(level, player, InteractionHand.MAIN_HAND);
        expect("二阶扣40精力", 460, Cultivation.get(player, CultivationValue.SOUL));
        expect("成功耗一符", 1, stack.getCount());
        for (int stage = 1; stage <= 6; stage++) {
            expectTrue("霓裳草" + stage + "只推进一阶段",
                    level.getBlockState(center.offset(stage - 3, -1, 2)).is(block("nichanggrass_0" + (stage + 1))));
        }
        expectTrue("水平7格排除", level.getBlockState(outside).is(block("nichanggrass_03")));
        expectTrue("垂直上2格包含", level.getBlockState(upper).is(block("nichanggrass_02")));
        expectTrue("垂直上3格排除", level.getBlockState(aboveRange).is(block("nichanggrass_01")));
        expectTrue("原版作物获得骨粉生长", level.getBlockState(wheat).getValue(CropBlock.AGE) >= 2);
        expect("树苗受控骨粉尝试次数", 12, bonemealCalls);
        expectTrue("来源标签作物已安排生长刻", level.getBlockTicks().hasScheduledTick(taggedCrop, block("shicai_011_plant_01")));
        expect("已有妖兽团队", "xiyinerlai", Relations.entityTeam(existingBeast));
        expect("已有妖兽仇恨", player.getStringUUID(), existingBeast.getPersistentData().getString("linshigongjizhe"));
        List<Entity> summoned = level.getEntitiesOfClass(Entity.class, new AABB(center).inflate(40),
                entity -> !before.contains(entity.getUUID()) && entity.getType().builtInRegistryHolder().key().location().getPath().startsWith("yaoshou_"));
        expect("秋林03七槽全部生成", 7, summoned.size());
        expect("秋林03二号三只", 3, summoned.stream().filter(entity -> entity.getType() == EntityType.byString("yvanchuxiuzhen:yaoshou_002").orElseThrow()).count());
        expect("秋林03一号四只", 4, summoned.stream().filter(entity -> entity.getType() == EntityType.byString("yvanchuxiuzhen:yaoshou_001").orElseThrow()).count());
        for (Entity entity : summoned) {
            created.add(entity);
            ((Mob) entity).setNoAi(true);
            expectTrue("召引共享友方表", Relations.friendlyTeams(entity).containsAll(Set.of("xiyinerlai", "yvanchuxiuzhen_yaoshou_001", "yvanchuxiuzhen_yaoshou_002")));
            expect("召引保留初始化后物种团队", entity.getType().builtInRegistryHolder().key().location().toString().replace(':', '_'), Relations.entityTeam(entity));
            expect("召引仇恨", player.getStringUUID(), entity.getPersistentData().getString("linshigongjizhe"));
        }
        ItemStack loaded = ItemStack.parseOptional(player.registryAccess(), (CompoundTag) stack.save(player.registryAccess()));
        expect("剩余符保存数量", 1, loaded.getCount());
        expect("剩余符保存品阶", 2, Fulu.talisman(loaded).level());
    }

    private void emptyRange(ServerPlayer player) {
        aim(player, 90);
        ItemStack stack = talisman(2, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        Cultivation.set(player, CultivationValue.SOUL, 500);
        stack.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
        expect("空范围仍扣40精力", 460, Cultivation.get(player, CultivationValue.SOUL));
        expectTrue("空范围仍耗符", stack.isEmpty());
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        NeoForge.EVENT_BUS.unregister(boneMealListener);
        created.forEach(Entity::discard);
        originalBiomes.forEach((pos, biome) -> fillBiome(player, pos, pos, biome));
        blocks.forEach((pos, state) -> player.serverLevel().setBlock(pos, state, Block.UPDATE_ALL));
        player.serverLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(randomTickSpeed, player.server);
        player.setItemInHand(InteractionHand.MAIN_HAND, hand);
        player.setGameMode(mode);
        player.setNoGravity(noGravity);
        player.teleportTo(originalPosition.x, originalPosition.y, originalPosition.z);
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(headYaw);
        player.setYBodyRot(bodyYaw);
        Cultivation.set(player, CultivationValue.SOUL, soul);
        Cultivation.editTizhi(player, data -> {
            data.putDouble("FuluConsumeJingli", fixed);
            data.putDouble("FuluConsumeJingliPercentage", percentage);
        });
    }

    private void fillBiome(ServerPlayer player, BlockPos min, BlockPos max, String biome) {
        String first = min.getX() + " " + min.getY() + " " + min.getZ();
        String last = max.getX() + " " + max.getY() + " " + max.getZ();
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                "fillbiome " + first + " " + last + " " + biome);
    }

    private void aim(ServerPlayer player, float angle) {
        player.setYRot(angle);
        player.setYHeadRot(angle);
        player.setYBodyRot(angle);
        player.setXRot(0);
    }

    private void plant(ServerLevel level, BlockPos pos, Block block) {
        put(level, pos.below(), Blocks.DIRT.defaultBlockState());
        put(level, pos, block.defaultBlockState());
    }

    private void put(ServerLevel level, BlockPos pos, BlockState state) {
        blocks.putIfAbsent(pos.immutable(), level.getBlockState(pos));
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private static ItemStack talisman(int grade, int count) {
        ItemStack stack = Fulu.createReward(BuiltInRegistries.ITEM.get(id("itemfulu_30")), grade);
        stack.setCount(count);
        return stack;
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(id(path));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
