package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.smoke.ActionCheck.Action;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.yihuo.FireBlockEntity;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectile;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectiles;
import net.yvanchuxiuzhen.system.yihuo.api.FireHeatEvent;

/** 真实客户端逐类使用十八种异火材料，并检查弹体碰撞、燃料同步和首标签选择。 */
final class YihuoMaterialCheck extends SystemCheck {
    private static final int[] MATERIALS = {221, 222, 223, 224, 225, 226, 227, 228, 229,
            231, 232, 233, 234, 235, 236, 237, 238, 239};
    private static final String[] MATERIAL_NAMES = {"大风雷火", "八方炎火", "空爆烈火", "地裂罡火", "万劫烬火", "三昧真火", "修罗炽火", "太清天火", "炎阳精火",
            "幽罗阴火", "紫微星火", "无忧业火", "碧落华火", "凋零魔火", "幽冥地火", "黯无玄火", "太阴浮火", "流星空火"};
    private final Screenshots screenshots;
    private final ActionCheck actions;
    private BlockPos hitPos;
    private BlockPos abovePos;
    private BlockPos belowPos;
    private ServerLevel burnLevel;
    private final List<Long> burnTicks = new ArrayList<>();
    private final List<Double> burnAmounts = new ArrayList<>();
    private final List<BlockState> originalBlocks = new ArrayList<>();
    private final List<CompoundTag> originalBlockEntities = new ArrayList<>();
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private boolean sneaking;
    private boolean useKey;
    private Cow contactTarget;
    private GameType gameMode;
    private int selectedSlot;
    private double originalPower;
    private double originalCount;
    private double originalLastUse;
    private double originalCultivationTick;
    private float originalYaw;
    private float originalPitch;
    private MobEffectInstance originalSilence;
    private UUID ownerId;
    private int expectedMaterial = -1;
    private String observedTypeId;
    private boolean observedEmptyDisplay;
    private double expectedPower;
    private YihuoProjectile projectile;
    private BlockPos observedHit;
    private int observedEntityHit = -1;
    private boolean waitingNaturalUse;
    private boolean waitingNaturalBurn;
    private boolean aborted;
    private int waitStart = -1;
    private long naturalUseStart;
    private long naturalResolutionTick = -1;
    private long burnStart;
    private final Consumer<EntityJoinLevelEvent> joinListener = event -> {
        if (expectedMaterial >= 0 && projectile == null && event.getEntity() instanceof YihuoProjectile joined
                && joined.getOwner() instanceof ServerPlayer owner && owner.getUUID().equals(ownerId)) {
            projectile = joined;
            observedTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(joined.getType()).toString();
            observedEmptyDisplay = joined.getItem().isEmpty();
            minecraft.execute(() -> minecraft.options.keyUse.setDown(false));
        }
    };
    private final Consumer<FireHeatEvent> heatListener = event -> {
        if (burnLevel == event.level() && event.pos().equals(abovePos.above())) {
            burnTicks.add(event.level().getGameTime());
            burnAmounts.add(event.amount());
        }
    };
    private final Consumer<ProjectileImpactEvent> impactListener = event -> {
        if (event.getProjectile() == projectile && !projectile.level().isClientSide()) {
            if (event.getRayTraceResult() instanceof BlockHitResult hit) observedHit = hit.getBlockPos();
            if (event.getRayTraceResult() instanceof EntityHitResult hit) observedEntityHit = hit.getEntity().getId();
            naturalResolutionTick = projectile.level().getGameTime();
        }
    };

    YihuoMaterialCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("异火弹体", report, levelId);
        this.screenshots = screenshots;
        NeoForge.EVENT_BUS.addListener(joinListener);
        NeoForge.EVENT_BUS.addListener(heatListener);
        NeoForge.EVENT_BUS.addListener(impactListener);
        actions = new ActionCheck("异火弹体", report, levelId) {
            @Override
            protected void cleanup(ServerPlayer player) {
                YihuoMaterialCheck.this.cleanup(player);
            }
        };
        List<Action> steps = new ArrayList<>();
        steps.add(new Action(2, this::prepare));
        for (int material : MATERIALS) {
            steps.add(new Action(4, player -> startNaturalUse(player, material)));
            steps.add(new Action(0, this::useSelectedMaterial));
            steps.add(new Action(0, player -> finishNaturalUse(player, material)));
        }
        for (int material : new int[] {221, 239}) {
            steps.add(new Action(4, player -> startNaturalPlacement(player, material)));
            steps.add(new Action(0, this::useSelectedMaterial));
            steps.add(new Action(3, player -> finishNaturalPlacement(player, material)));
            steps.add(new Action(0, this::clearPlacement));
        }
        for (int material : new int[] {221, 234, 239}) {
            steps.add(new Action(4, player -> startNaturalContact(player, material)));
            steps.add(new Action(0, this::useSelectedMaterial));
            steps.add(new Action(3, player -> finishNaturalContact(player, material)));
            steps.add(new Action(0, this::clearContact));
        }
        steps.add(new Action(0, this::startNaturalBurn));
        steps.add(new Action(0, this::finishNaturalBurn));
        steps.add(new Action(5, player -> startCampfire(player, 221)));
        steps.add(new Action(2, player -> finishFuel(player, "221营火", abovePos, 17)));
        steps.add(new Action(5, player -> startCampfire(player, 239)));
        steps.add(new Action(2, player -> finishFuel(player, "239营火", abovePos, 2567)));
        steps.add(new Action(5, this::startOwnBlock));
        steps.add(new Action(2, player -> finishFuel(player, "221自有火块", abovePos, 20)));
        steps.add(new Action(5, this::startFirstWithoutBlockEntity));
        steps.add(new Action(2, player -> finishFuel(player, "首标签无BE，后方火块", belowPos, 10)));
        actions.script(steps.toArray(Action[]::new));
    }

    @Override
    protected boolean step(int step, int local) {
        if (waitingNaturalUse) {
            if (!awaitNaturalUse(local)) return false;
            if (aborted) return true;
        }
        if (waitingNaturalBurn && !awaitNaturalBurn(local)) return false;
        if (!actions.tick(local)) return false;
        finish();
        return true;
    }

    /** 等待真实碰撞或八刻寿命结束，再留出两个服务端刻供客户端处理同步包。 */
    private boolean awaitNaturalUse(int local) {
        if (waitStart < 0) waitStart = local;
        boolean[] ready = {false};
        onServer(player -> {
            long now = player.serverLevel().getGameTime();
            if (projectile != null && !projectile.isAlive() && naturalResolutionTick < 0) naturalResolutionTick = now;
            ready[0] = naturalResolutionTick >= 0 && now - naturalResolutionTick >= 2;
            if (!ready[0] && (now - naturalUseStart >= 40 || local - waitStart >= 120)) {
                fail("材料" + expectedMaterial + "自然使用等待超时；服务端经过" + (now - naturalUseStart)
                        + "刻，服务端使用=" + player.isUsingItem() + "，客户端使用=" + minecraft.player.isUsingItem()
                        + "，使用键=" + minecraft.options.keyUse.isDown() + "，弹体=" + observedTypeId
                        + "，方块命中=" + observedHit + "，实体命中=" + observedEntityHit);
                cleanup(player);
                finish();
                aborted = true;
                ready[0] = true;
            }
        });
        if (ready[0]) waitingNaturalUse = false;
        return ready[0];
    }

    /** 客户端动作暂停至服务端游戏时间经过45刻，保留两次计划刻的严格断言。 */
    private boolean awaitNaturalBurn(int local) {
        if (waitStart < 0) waitStart = local;
        boolean[] ready = {false};
        onServer(player -> {
            long elapsed = burnLevel.getGameTime() - burnStart;
            ready[0] = elapsed >= 45;
            if (!ready[0] && local - waitStart >= 120) {
                fail("221火块等待服务端45刻超时，实际经过" + elapsed + "刻");
                cleanup(player);
                finish();
            }
        });
        if (ready[0]) waitingNaturalBurn = false;
        return ready[0];
    }

    private void prepare(ServerPlayer player) {
        expect("客户端检查使用中文", "zh_cn", minecraft.getLanguageManager().getSelected());
        for (int index = 0; index < MATERIALS.length; index++) {
            expect("材料" + MATERIALS[index] + "显示来源中文名称", MATERIAL_NAMES[index], new ItemStack(item(MATERIALS[index])).getHoverName().getString());
        }
        sneaking = player.isShiftKeyDown();
        useKey = minecraft.options.keyUse.isDown();
        gameMode = player.gameMode.getGameModeForPlayer();
        selectedSlot = player.getInventory().selected;
        originalPower = Cultivation.get(player, CultivationValue.POWER);
        originalCount = Cultivation.tizhiDouble(player, "yihuoshifangshu");
        originalLastUse = Cultivation.tizhiDouble(player, "yihuojishi");
        originalCultivationTick = Cultivation.tizhiDouble(player, "jishiqi");
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        MobEffectInstance silence = player.getEffect(StatusEffects.holder("effect_002"));
        originalSilence = silence == null ? null : new MobEffectInstance(silence);
        ownerId = player.getUUID();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            originalInventory.add(player.getInventory().getItem(slot).copy());
        }
        player.setGameMode(GameType.SURVIVAL);
        player.setShiftKeyDown(false);
        player.removeEffect(StatusEffects.holder("effect_002"));
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), -90, 0);
        hitPos = player.blockPosition().east(4).above();
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

    private void startNaturalUse(ServerPlayer player, int material) {
        stageNaturalItem(player, material);
        ServerLevel level = player.serverLevel();
        replaceFixtureBlock(level, abovePos, Blocks.CAMPFIRE.defaultBlockState());
        setPower(level, abovePos, 7);
        player.setShiftKeyDown(false);
        minecraft.execute(() -> minecraft.options.keyShift.setDown(false));
    }

    private void useSelectedMaterial(ServerPlayer player) {
        boolean ready = player.getMainHandItem().is(item(expectedMaterial))
                && minecraft.player.getMainHandItem().is(item(expectedMaterial)) && inventorySynced(player);
        expectTrue("材料" + expectedMaterial + "使用前双端已持有本次材料", ready);
        if (!ready) {
            cleanup(player);
            finish();
            return;
        }
        Cultivation.setTizhiDouble(player, "jishiqi", player.level().getDayTime());
        naturalUseStart = player.serverLevel().getGameTime();
        waitingNaturalUse = true;
        waitStart = -1;
        minecraft.execute(() -> {
            minecraft.options.keyUse.setDown(true);
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
        });
    }

    private void finishNaturalUse(ServerPlayer player, int material) {
        String label = "客户端右键" + material;
        expect(label + "生成对应弹体", projectileId(material), observedTypeId);
        expectTrue(label + "保留来源空气显示栈", observedEmptyDisplay);
        expect(label + "弹体实际命中", hitPos, observedHit);
        expect(label + "服务端扣除来源灵力", expectedPower, Cultivation.get(player, CultivationValue.POWER));
        expect(label + "本人客户端收到灵力", expectedPower, Cultivation.get(minecraft.player, CultivationValue.POWER));
        expect(label + "燃料服务端增加", 7 + fuel(material), fuel(player.serverLevel().getBlockEntity(abovePos)));
        expectTrue(label + "物品与背包双端一致", inventorySynced(player));
        minecraft.execute(() -> minecraft.options.keyUse.setDown(false));
        if (projectile != null && projectile.isAlive()) projectile.discard();
        expectedMaterial = -1;
    }

    private void startNaturalPlacement(ServerPlayer player, int material) {
        stageNaturalItem(player, material);
        replaceFixtureBlock(player.serverLevel(), abovePos, Blocks.AIR.defaultBlockState());
        player.setShiftKeyDown(true);
        minecraft.execute(() -> minecraft.options.keyShift.setDown(true));
    }

    private void finishNaturalPlacement(ServerPlayer player, int material) {
        BlockPos firePos = abovePos;
        BlockEntity fire = player.serverLevel().getBlockEntity(firePos);
        String id = ModId.VALUE + ":cailiao_" + material + "fireblock";
        expect("潜行右键" + material + "放置对应火块", id,
                BuiltInRegistries.BLOCK.getKey(player.serverLevel().getBlockState(firePos).getBlock()).toString());
        expect("潜行右键" + material + "初始化燃料", fuel(material), fuel(fire));
        expect("潜行右键" + material + "记录施放者", player.getStringUUID(),
                fire == null ? "" : fire.getPersistentData().getString("ManaAttackUUID"));
        expect("潜行右键" + material + "记录团队", Relations.entityTeam(player),
                fire == null ? "" : fire.getPersistentData().getString("team"));
        expect("潜行右键" + material + "客户端火块燃料同步", fuel(material), fuel(minecraft.level.getBlockEntity(firePos)));
        expect("潜行右键" + material + "生成对应弹体", projectileId(material), observedTypeId);
        expect("潜行右键" + material + "弹体实际命中", hitPos, observedHit);
        expect("潜行右键" + material + "服务端扣除来源灵力", expectedPower, Cultivation.get(player, CultivationValue.POWER));
        expect("潜行右键" + material + "本人客户端收到灵力", expectedPower, Cultivation.get(minecraft.player, CultivationValue.POWER));
        if (material == 221) minecraft.execute(() -> screenshots.take("yihuo-natural-fireblock.png"));
        minecraft.execute(() -> minecraft.options.keyUse.setDown(false));
    }

    private void clearPlacement(ServerPlayer player) {
        if (projectile != null && projectile.isAlive()) projectile.discard();
        replaceFixtureBlock(player.serverLevel(), abovePos, Blocks.AIR.defaultBlockState());
        player.setShiftKeyDown(false);
        minecraft.execute(() -> minecraft.options.keyShift.setDown(false));
        expectedMaterial = -1;
    }

    private void startNaturalContact(ServerPlayer player, int material) {
        stageNaturalItem(player, material);
        ServerLevel level = player.serverLevel();
        level.setBlock(hitPos, Blocks.AIR.defaultBlockState(), 3);
        replaceFixtureBlock(level, abovePos, Blocks.AIR.defaultBlockState());
        contactTarget = EntityType.COW.create(level);
        contactTarget.setNoAi(true);
        contactTarget.setNoGravity(true);
        contactTarget.setSilent(true);
        Relations.setTeam(contactTarget, "smoke:yihuo-contact:" + contactTarget.getStringUUID());
        contactTarget.setPos(player.getX() + 2.25, player.getY() + 1, player.getZ());
        contactTarget.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);
        contactTarget.setHealth(500);
        level.addFreshEntity(contactTarget);
        player.setShiftKeyDown(false);
        minecraft.execute(() -> minecraft.options.keyShift.setDown(false));
    }

    private void finishNaturalContact(ServerPlayer player, int material) {
        String effectId = material < 230 ? "effect_023" : "effect_024";
        MobEffectInstance serverEffect = contactTarget.getEffect(StatusEffects.holder(effectId));
        expect("非友方弹体" + material + "实际命中受控实体", contactTarget.getId(), observedEntityHit);
        expectTrue("非友方弹体" + material + "命中并造成火伤", contactTarget.getHealth() < 500);
        expect("非友方弹体" + material + "施加炙烤等级", material % 10 - 1,
                serverEffect == null ? -1 : serverEffect.getAmplifier());
        expectTrue("非友方弹体" + material + "保留来源攻击者", player.getStringUUID().equals(
                contactTarget.getPersistentData().getString("fireAttackUUID")));
        Entity clientEntity = minecraft.level.getEntity(contactTarget.getId());
        List<String> expectedParticles = List.of(particleSignature(new MobEffectInstance(
                StatusEffects.holder(effectId), 200, material % 10 - 1, true, true).getParticleOptions()));
        expect("非友方弹体" + material + "服务端生成炙烤粒子元数据", expectedParticles, effectParticleMetadata(contactTarget));
        expect("非友方弹体" + material + "客户端收到炙烤粒子类型与RGBA", expectedParticles, effectParticleMetadata(clientEntity));
        expect("非友方弹体" + material + "客户端收到火伤生命值", contactTarget.getHealth(),
                clientEntity instanceof Cow cow ? cow.getHealth() : Double.NaN);
        expect("非友方弹体" + material + "生成对应弹体", projectileId(material), observedTypeId);
        if (material == 221) minecraft.execute(() -> screenshots.take("yihuo-natural-contact.png"));
        minecraft.execute(() -> minecraft.options.keyUse.setDown(false));
    }

    private void clearContact(ServerPlayer player) {
        if (contactTarget != null) {
            contactTarget.discard();
            contactTarget = null;
        }
        if (projectile != null && projectile.isAlive()) projectile.discard();
        player.serverLevel().setBlock(hitPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        expectedMaterial = -1;
    }

    private void startNaturalBurn(ServerPlayer player) {
        burnLevel = player.serverLevel();
        burnStart = burnLevel.getGameTime();
        burnTicks.clear();
        burnAmounts.clear();
        replaceFixtureBlock(burnLevel, abovePos, fireBlock().defaultBlockState());
        FireBlockEntity fire = (FireBlockEntity) burnLevel.getBlockEntity(abovePos);
        fire.initialize(player, 12);
        waitingNaturalBurn = true;
        waitStart = -1;
    }

    private void finishNaturalBurn(ServerPlayer player) {
        long elapsed = burnLevel.getGameTime() - burnStart;
        boolean ready = elapsed >= 40 && elapsed < 60;
        expectTrue("221火块检查前服务端已过40至59刻，实际" + elapsed + "刻", ready);
        if (!ready) return;
        expect("221火块自然计划刻每20刻消耗一次", 2, burnTicks.size());
        expect("221火块计划刻位于重新放置后第20和40刻", List.of(burnStart + 20, burnStart + 40), burnTicks);
        expect("221火块每次提交1单位热量", List.of(1.0, 1.0), burnAmounts);
        if (burnTicks.size() == 2) {
            expect("221火块计划刻相隔20刻", 20, burnTicks.get(1) - burnTicks.get(0));
        }
        expect("221火块两次计划刻后剩余燃料", 10, fuel(player.serverLevel().getBlockEntity(abovePos)));
        expect("221火块计划刻后本人客户端燃料同步", 10, fuel(minecraft.level.getBlockEntity(abovePos)));
    }

    private void stageNaturalItem(ServerPlayer player, int material) {
        ServerLevel level = player.serverLevel();
        expectedMaterial = material;
        observedTypeId = null;
        observedEmptyDisplay = false;
        observedHit = null;
        observedEntityHit = -1;
        naturalResolutionTick = -1;
        projectile = null;
        expectedPower = 3000 - cost(material);
        Cultivation.set(player, CultivationValue.POWER, 3000);
        Cultivation.setTizhiDouble(player, "jishiqi", level.getDayTime());
        Cultivation.setTizhiDouble(player, "yihuoshifangshu", 0);
        Cultivation.setTizhiDouble(player, "yihuojishi", 0);
        player.getInventory().setItem(0, new ItemStack(item(material)));
        player.getInventory().selected = 0;
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> {
            minecraft.options.keyUse.setDown(false);
            if (minecraft.player.isUsingItem()) minecraft.gameMode.releaseUsingItem(minecraft.player);
            minecraft.player.setYRot(-90);
            minecraft.player.setXRot(0);
            minecraft.player.getInventory().selected = 0;
        });
    }

    private void startCampfire(ServerPlayer player, int material) {
        replaceFixtureBlock(player.serverLevel(), abovePos, Blocks.CAMPFIRE.defaultBlockState());
        setPower(player.serverLevel(), abovePos, 7);
        launch(player, material);
    }

    private void startOwnBlock(ServerPlayer player) {
        replaceFixtureBlock(player.serverLevel(), abovePos, fireBlock().defaultBlockState());
        setPower(player.serverLevel(), abovePos, 10);
        launch(player, 221);
    }

    private void startFirstWithoutBlockEntity(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        replaceFixtureBlock(level, abovePos, Blocks.FIRE.defaultBlockState());
        replaceFixtureBlock(level, belowPos, fireBlock().defaultBlockState());
        setPower(level, belowPos, 10);
        launch(player, 221);
    }

    private void launch(ServerPlayer player, int material) {
        ServerLevel level = player.serverLevel();
        level.setBlock(hitPos, Blocks.STONE.defaultBlockState(), 2);
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
        expect(label + "燃料", expected, fuel(block));
        projectile.discard();
    }

    protected void cleanup(ServerPlayer player) {
        expectedMaterial = -1;
        waitingNaturalUse = false;
        waitingNaturalBurn = false;
        aborted = true;
        NeoForge.EVENT_BUS.unregister(joinListener);
        NeoForge.EVENT_BUS.unregister(heatListener);
        NeoForge.EVENT_BUS.unregister(impactListener);
        if (projectile != null) {
            projectile.discard();
        }
        if (contactTarget != null) contactTarget.discard();
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
        player.getInventory().clearContent();
        for (int slot = 0; slot < originalInventory.size(); slot++) {
            player.getInventory().setItem(slot, originalInventory.get(slot));
        }
        player.getInventory().selected = selectedSlot;
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        Cultivation.set(player, CultivationValue.POWER, originalPower);
        Cultivation.setTizhiDouble(player, "yihuoshifangshu", originalCount);
        Cultivation.setTizhiDouble(player, "yihuojishi", originalLastUse);
        Cultivation.setTizhiDouble(player, "jishiqi", originalCultivationTick);
        if (originalSilence != null) player.addEffect(originalSilence);
        player.setGameMode(gameMode);
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), originalYaw, originalPitch);
        player.setShiftKeyDown(sneaking);
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = selectedSlot;
            minecraft.options.keyShift.setDown(sneaking);
            minecraft.options.keyUse.setDown(useKey);
        });
    }

    private static Block fireBlock() {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_221fireblock"));
    }

    private static Item item(int material) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_" + material));
    }

    private static String projectileId(int material) {
        return ModId.VALUE + ":projectile_cailiao_" + material;
    }

    private static double cost(int material) {
        return material == 234 ? 80 : 10 * (1 << (material % 10 - 1));
    }

    private static double fuel(int material) {
        return 10 * (1 << (material % 10 - 1));
    }

    private static double fuel(BlockEntity block) {
        return block == null ? Double.NaN : block.getPersistentData().getDouble("power");
    }

    /** 同一测试格先清除旧方块与尚未执行的计划刻，再触发新方块的放置生命周期。 */
    private static void replaceFixtureBlock(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.getBlockTicks().clearArea(new BoundingBox(pos));
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    /** 普通观察者收到PARTICLES元数据，效果实例与等级仍在服务端效果表中核验。 */
    private static List<String> effectParticleMetadata(Entity entity) {
        if (!(entity instanceof Cow)) return List.of();
        List<SynchedEntityData.DataValue<?>> values = entity.getEntityData().getNonDefaultValues();
        if (values == null) return List.of();
        for (SynchedEntityData.DataValue<?> value : values) {
            if (value.serializer() == EntityDataSerializers.PARTICLES) {
                return ((List<?>) value.value()).stream().map(ParticleOptions.class::cast)
                        .map(YihuoMaterialCheck::particleSignature).toList();
            }
        }
        return List.of();
    }

    private static String particleSignature(ParticleOptions particle) {
        ColorParticleOption color = (ColorParticleOption) particle;
        return BuiltInRegistries.PARTICLE_TYPE.getKey(color.getType()) + ":"
                + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
    }

    private static void setPower(ServerLevel level, BlockPos pos, double value) {
        BlockEntity block = level.getBlockEntity(pos);
        block.getPersistentData().putDouble("power", value);
        block.setChanged();
        level.sendBlockUpdated(pos, block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
    }

    private static boolean inventorySynced(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (!ItemStack.matches(player.getInventory().getItem(slot), Minecraft.getInstance().player.getInventory().getItem(slot))) return false;
        }
        return true;
    }
}
