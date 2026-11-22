package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 野外反应方块的正式交互、受控随机刻与原生计划刻检查。 */
final class ReactiveWildPlantsCheck extends SystemCheck {
    private final Map<BlockPos, SavedBlock> savedBlocks = new LinkedHashMap<>();
    private final List<Entity> createdEntities = new ArrayList<>();
    private final List<BlockPos> recoveryPositions = new ArrayList<>();
    private static final List<String> RECOVERY_STATES = List.of("cannibalmoss_02", "bitegrass_02", "cloudgrass_02");
    private static final List<String> RECOVERED_STATES = List.of("cannibalmoss", "bitegrass_01", "cloudgrass_01");
    private BlockPos origin;
    private ItemStack mainHand;
    private ItemStack offHand;
    private GameType mode;
    private Difficulty difficulty;
    private boolean sneaking;
    private boolean ready;
    private int started;
    private int randomTickSpeed;

    ReactiveWildPlantsCheck(List<String> report, String levelId) {
        super("野外反应植物", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (!ready) return true;
                boolean[] elapsed = {false};
                onServer(player -> elapsed[0] = player.server.getTickCount() - started >= 6);
                if (!elapsed[0] && local < 200) return false;
                expectTrue("受控计划刻经过至少六服务端刻", elapsed[0]);
                onServer(this::checkRecovery);
            }
            case 2 -> {
                onServer(this::cleanup);
                finish();
            }
            default -> throw new IllegalStateException("未知反应植物检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        for (String path : List.of("huaguang", "cannibalmoss", "cannibalmoss_02", "bitegrass_01", "bitegrass_02",
                "lurker", "cloudgrass_01", "cloudgrass_02", "snowdrift", "moeye", "iceboundrock", "thundergrass", "peachsaplingxve")) {
            if (!BuiltInRegistries.BLOCK.containsKey(id(path))) {
                fail("正式方块未注册，停止依赖步骤 " + path);
                return;
            }
        }
        for (String path : List.of("moshou_003", "taomujing")) {
            if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id(path))) {
                fail("正式实体未注册，停止依赖步骤 " + path);
                return;
            }
        }
        mainHand = player.getMainHandItem().copy();
        offHand = player.getOffhandItem().copy();
        mode = player.gameMode.getGameModeForPlayer();
        difficulty = player.serverLevel().getDifficulty();
        randomTickSpeed = player.serverLevel().getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        sneaking = player.isShiftKeyDown();
        origin = player.blockPosition().above(12);
        ready = true;
        player.setGameMode(GameType.CREATIVE);
        player.setShiftKeyDown(false);
        player.server.setDifficulty(Difficulty.NORMAL, true);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, player.server);
        contact(player);
        snowdrift(player);
        demonEye(player);
        silkTouchSuppression(player);
        prepareRecovery(player);
    }

    private void contact(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = origin;
        Cow probe = EntityType.COW.create(level);
        if (probe == null) {
            fail("碰触样本创建失败，停止碰触步骤");
            return;
        }
        createdEntities.add(probe);
        probe.setPos(Vec3.atCenterOf(pos));
        place(player, pos, "huaguang");
        touch(level, pos, probe);
        expect("华光碰触重设竖直速度", new Vec3(0, 0.2, 0), probe.getDeltaMovement());
        place(player, pos, "cannibalmoss");
        touch(level, pos, probe);
        expectTrue("食人苔碰触切换闭合状态", level.getBlockState(pos).is(block("cannibalmoss_02")));
        touch(level, pos, probe);
        MobEffectInstance slow = probe.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        expectTrue("闭合食人苔施加二十刻二十级缓慢", slow != null && slow.getDuration() == 20 && slow.getAmplifier() == 20);
        probe.removeAllEffects();
        probe.setHealth(probe.getMaxHealth());
        probe.invulnerableTime = 0;
        place(player, pos, "bitegrass_01");
        float health = probe.getHealth();
        touch(level, pos, probe);
        expectTrue("咬草碰触造成实际伤害并切换闭合状态", probe.getHealth() < health
                && level.getBlockState(pos).is(block("bitegrass_02")));
        probe.setHealth(probe.getMaxHealth());
        probe.invulnerableTime = 0;
        place(player, pos, "lurker");
        health = probe.getHealth();
        touch(level, pos, probe);
        slow = probe.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        expectTrue("潜伏者碰触造成伤害及一千二百刻一级缓慢", probe.getHealth() < health
                && slow != null && slow.getDuration() == 1200 && slow.getAmplifier() == 1);
        place(player, pos, "cloudgrass_01");
        touch(level, pos, probe);
        expectTrue("云草碰触切换压缩状态", level.getBlockState(pos).is(block("cloudgrass_02")));
        probe.discard();
    }

    private void snowdrift(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = origin.east(2);
        place(player, pos, "snowdrift");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        for (ItemStack stack : List.of(ItemStack.EMPTY, new ItemStack(Items.STICK))) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            Set<UUID> before = ids(level.getEntitiesOfClass(Snowball.class, area()));
            player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            List<Snowball> added = level.getEntitiesOfClass(Snowball.class, area(), entity -> !before.contains(entity.getUUID()));
            createdEntities.addAll(added);
            expectTrue((stack.isEmpty() ? "空手" : "持物") + "正式雪堆交互恰好发射一枚本人雪球",
                    added.size() == 1 && added.getFirst().getOwner() == player);
            added.forEach(Entity::discard);
        }
    }

    private void demonEye(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = origin.east(4);
        place(player, pos, "moeye");
        Set<UUID> before = ids(level.getEntitiesOfClass(Mob.class, area()));
        expectTrue("正式玩家破坏魔眼成功移除方块", player.gameMode.destroyBlock(pos) && level.getBlockState(pos).isAir());
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id("moshou_003"));
        List<Mob> added = level.getEntitiesOfClass(Mob.class, area(), entity -> !before.contains(entity.getUUID()));
        createdEntities.addAll(added);
        expectTrue("同步事件消费者只生成一只魔兽003", added.size() == 1 && added.getFirst().getType() == type);
        if (added.size() == 1 && added.getFirst().getType() == type) {
            expectTrue("魔兽003生成时仇恨目标为破坏玩家", added.getFirst().getTarget() == player);
        }
        added.forEach(Entity::discard);
    }

    private void silkTouchSuppression(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = origin.west(2);
        ItemStack silk = new ItemStack(Items.DIAMOND_PICKAXE);
        silk.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        for (InteractionHand hand : InteractionHand.values()) {
            remember(player, pos.below());
            remember(player, pos);
            remember(player, pos.above());
            level.setBlock(pos.below(), Blocks.SNOW_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
            DoublePlantBlock.placeAt(level, block("iceboundrock").defaultBlockState(), pos, Block.UPDATE_ALL);
            player.setItemInHand(InteractionHand.MAIN_HAND, hand == InteractionHand.MAIN_HAND ? silk.copy() : new ItemStack(Items.STICK));
            player.setItemInHand(InteractionHand.OFF_HAND, hand == InteractionHand.OFF_HAND ? silk.copy() : ItemStack.EMPTY);
            Set<UUID> before = ids(level.getEntitiesOfClass(Mob.class, area()));
            expectTrue(hand + "精准采集样本正式破坏冰封岩", player.gameMode.destroyBlock(pos));
            List<Mob> added = level.getEntitiesOfClass(Mob.class, area(), entity -> !before.contains(entity.getUUID()));
            createdEntities.addAll(added);
            expectTrue(hand + "精准采集抑制冰封岩动物生成", added.isEmpty());
            added.forEach(Entity::discard);
        }
    }

    private void prepareRecovery(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (int index = 0; index < RECOVERY_STATES.size(); index++) {
            BlockPos pos = origin.south(3).east(index * 2);
            recoveryPositions.add(pos);
            place(player, pos, RECOVERY_STATES.get(index));
            expectTrue(RECOVERY_STATES.get(index) + "正式状态启用随机刻", level.getBlockState(pos).isRandomlyTicking());
            level.getBlockState(pos).randomTick(level, pos, level.random);
            expectTrue(RECOVERY_STATES.get(index) + "受控随机刻委派来源回调并恢复状态",
                    level.getBlockState(pos).is(block(RECOVERED_STATES.get(index))));
            expectTrue(RECOVERY_STATES.get(index) + "放置和随机刻均未补造计划调度",
                    !level.getBlockTicks().hasScheduledTick(pos, block(RECOVERY_STATES.get(index))));
            place(player, pos, RECOVERY_STATES.get(index));
            level.scheduleTick(pos, block(RECOVERY_STATES.get(index)), 2);
        }
        BlockPos thunder = origin.south(3).west(2);
        place(player, thunder, "thundergrass");
        expectTrue("雷草正式状态启用随机刻", level.getBlockState(thunder).isRandomlyTicking());
        Set<UUID> before = ids(level.getEntitiesOfClass(LightningBolt.class, area()));
        level.getBlockState(thunder).randomTick(level, thunder, level.random);
        List<LightningBolt> added = level.getEntitiesOfClass(LightningBolt.class, area(), entity -> !before.contains(entity.getUUID()));
        createdEntities.addAll(added);
        expectTrue("雷草受控随机刻生成一枚来源水平范围内的真实闪电", added.size() == 1
                && Math.abs(added.getFirst().getX() - thunder.getX() - 0.5) <= 3
                && added.getFirst().getY() == thunder.getY()
                && Math.abs(added.getFirst().getZ() - thunder.getZ() - 0.5) <= 3);
        added.forEach(Entity::discard);
        bloodPeachRandomTick(player);
        started = player.server.getTickCount();
    }

    private void bloodPeachRandomTick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = origin.north(3).east(4);
        place(player, pos, "peachsaplingxve");
        expectTrue("血桃苗正式状态启用随机刻", level.getBlockState(pos).isRandomlyTicking());
        Set<UUID> before = ids(level.getEntitiesOfClass(Mob.class, area()));
        int attempts = 0;
        while (attempts < 512 && level.getBlockState(pos).is(block("peachsaplingxve"))) {
            level.getBlockState(pos).randomTick(level, pos, level.random);
            attempts++;
        }
        List<Mob> added = level.getEntitiesOfClass(Mob.class, area(), entity -> !before.contains(entity.getUUID()));
        createdEntities.addAll(added);
        expectTrue("血桃苗受控随机刻成功分支同步生成一只桃木精并置空气（本轮" + attempts + "次）",
                level.getBlockState(pos).isAir() && added.size() == 1
                        && added.getFirst().getType() == BuiltInRegistries.ENTITY_TYPE.get(id("taomujing")));
        expectTrue("血桃苗受控随机刻不追加计划调度", !level.getBlockTicks().hasScheduledTick(pos, block("peachsaplingxve")));
        added.forEach(Entity::discard);
    }

    private void checkRecovery(ServerPlayer player) {
        for (int index = 0; index < recoveryPositions.size(); index++) {
            expectTrue(RECOVERY_STATES.get(index) + "关闭自然随机刻后受控原生计划刻仍恢复状态",
                    player.serverLevel().getBlockState(recoveryPositions.get(index)).is(block(RECOVERED_STATES.get(index))));
        }
    }

    private void cleanup(ServerPlayer player) {
        if (!ready) return;
        createdEntities.forEach(Entity::discard);
        for (SavedBlock saved : savedBlocks.values()) saved.restore(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        player.setItemInHand(InteractionHand.OFF_HAND, offHand);
        player.setShiftKeyDown(sneaking);
        player.setGameMode(mode);
        player.server.setDifficulty(difficulty, true);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(randomTickSpeed, player.server);
    }

    private void place(ServerPlayer player, BlockPos pos, String path) {
        remember(player, pos.below());
        remember(player, pos);
        remember(player, pos.above());
        ServerLevel level = player.serverLevel();
        level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos, block(path).defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private void remember(ServerPlayer player, BlockPos pos) {
        savedBlocks.computeIfAbsent(pos.immutable(), key -> SavedBlock.capture(player, key));
    }

    private static void touch(ServerLevel level, BlockPos pos, Entity entity) {
        level.getBlockState(pos).entityInside(level, pos, entity);
    }

    private AABB area() {
        return new AABB(origin).inflate(8);
    }

    private static Set<UUID> ids(List<? extends Entity> entities) {
        Set<UUID> ids = new HashSet<>();
        for (Entity entity : entities) ids.add(entity.getUUID());
        return ids;
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(id(path));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private record SavedBlock(BlockPos pos, BlockState state, CompoundTag data) {
        static SavedBlock capture(ServerPlayer player, BlockPos pos) {
            BlockEntity entity = player.serverLevel().getBlockEntity(pos);
            return new SavedBlock(pos.immutable(), player.serverLevel().getBlockState(pos),
                    entity == null ? null : entity.saveWithFullMetadata(player.registryAccess()));
        }

        void restore(ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            if (data != null) level.setBlockEntity(BlockEntity.loadStatic(pos, state, data, player.registryAccess()));
        }
    }
}
