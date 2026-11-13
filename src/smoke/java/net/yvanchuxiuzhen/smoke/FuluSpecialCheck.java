package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.content.ColoredLightningBolt;
import net.yvanchuxiuzhen.system.fulu.DrawingTableRuntime;
import net.yvanchuxiuzhen.system.fulu.FiveThunderCurse;
import net.yvanchuxiuzhen.system.fulu.FuluSetup;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionProgress;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 惊庭五雷符制作、自然松手命中、完整五轮与持久阶段的必要单项。 */
final class FuluSpecialCheck extends SystemCheck {
    private record Strike(String phase, Vec3 at, int color, long time) {
    }

    private final Consumer<String> shot;
    private final List<Strike> strikes = new ArrayList<>();
    private final List<Entity> created = new ArrayList<>();
    private final Consumer<EntityJoinLevelEvent> joined = this::joined;
    private final Consumer<EntityTickEvent.Post> flight = this::flight;
    private final Consumer<ProjectileImpactEvent> impact = this::impact;
    private final List<String> flightTrace = new ArrayList<>();
    private final List<String> report;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private ItemStack originalHand;
    private GameType mode;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private boolean noGravity;
    private boolean invulnerable;
    private double soul;
    private double fixed;
    private double percentage;
    private ProfessionProgress rune;
    private CompoundTag playerData;
    private Pig target;
    private TalismanProjectile naturalProjectile;
    private ItemStack drawn;
    private double soulAtLaunch;
    private double soulBeforeLaunch;
    private int lifeTicks;
    private long firstObserved;
    private long cycleTicks;
    private boolean damageObserved;
    private boolean screenshot;
    private boolean hitReady;
    private boolean launchAligned;
    private boolean collisionObserved;
    private long launchedAt = -1;
    private long flightElapsed;

    FuluSpecialCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("惊庭五雷符", report, levelId);
        this.shot = shot;
        this.report = report;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::prepare);
            return true;
        }
        if (step == 1) {
            if (local < 10) return false;
            onServer(this::drawAndReject);
            return true;
        }
        if (step == 2) {
            if (local < 8) return false;
            minecraft.player.setYRot(0);
            minecraft.player.setXRot(0);
            minecraft.player.setYHeadRot(0);
            minecraft.player.setYBodyRot(0);
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            return true;
        }
        if (step == 3) {
            if (local < 4) return false;
            minecraft.gameMode.releaseUsingItem(minecraft.player);
            return true;
        }
        if (step == 4) {
            if (local < 8) return false;
            onServer(player -> flightElapsed = launchedAt < 0 ? -1 : player.serverLevel().getGameTime() - launchedAt);
            if (flightElapsed < 20 && local < 200) return false;
            onServer(player -> {
                hitReady = naturalProjectile != null && target.hasEffect(effect());
                expectTrue("真实客户端松手生成41弹体", naturalProjectile != null);
                expectTrue("发射轨迹对准受控目标", launchAligned);
                expectTrue("原生箭碰撞事件命中目标", collisionObserved);
                expectTrue("自然弹体命中并施加五雷效果", target.hasEffect(effect()));
                if (!hitReady) flightTrace.forEach(line -> report.add("INFO 惊庭五雷符 诊断 " + line));
                expect("二阶发射时精力扣40", soulBeforeLaunch - 40, soulAtLaunch);
                expect("自然发射消耗制作成品", 1, player.getMainHandItem().getCount());
                expect("独立交接清零fulu41level", 0, player.getPersistentData().getDouble("fulu41level"));
                expect("普通远程符fululevel不被41改写", 77, player.getPersistentData().getDouble("fululevel"));
                if (!hitReady) return;
                expect("命中品阶等级", 1, target.getEffect(effect()).getAmplifier());
                expect("主人UUID", player.getStringUUID(), target.getPersistentData().getString("belongToMasterUUID"));
                expect("主人团队", Relations.team(player), target.getPersistentData().getString("belongToMasterteam"));
            });
            if (!hitReady) {
                onServer(this::cleanup);
                finish();
            }
            return true;
        }
        if (step == 5) {
            if (!screenshot && damageObserved) {
                shot.accept("fulu41-thunder.png");
                screenshot = true;
            }
            onServer(player -> {
                cycleTicks = player.serverLevel().getGameTime() - firstObserved;
                if (!damageObserved && target.getHealth() < 18) {
                    damageObserved = true;
                    target.setInvulnerable(true);
                }
            });
            if (cycleTicks < 790) return false;
            onServer(this::verifyNaturalCycle);
            return true;
        }
        if (step == 6) {
            expect("客户端成品中文名", "惊庭五雷符", minecraft.player.getMainHandItem().getHoverName().getString());
            expect("客户端状态中文名", "五雷轰顶", effect().value().getDisplayName().getString());
            expect("客户端状态图集加载真实图标", id("fulutexiao_41"), minecraft.getMobEffectTextures().get(effect()).contents().name());
            onServer(this::verifyPersistenceAndBoundaries);
            return true;
        }
        if (step == 7) {
            if (local < 18) return false;
            onServer(player -> {
                expectTrue("无目标弹体16刻寿命结束", lifeTicks == 16 && naturalProjectile.isRemoved());
                cleanup(player);
            });
            finish();
            return true;
        }
        return false;
    }

    private void prepare(ServerPlayer player) {
        originalHand = player.getMainHandItem().copy();
        mode = player.gameMode.getGameModeForPlayer();
        position = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        noGravity = player.isNoGravity();
        invulnerable = player.isInvulnerable();
        soul = Cultivation.get(player, CultivationValue.SOUL);
        fixed = Cultivation.tizhiDouble(player, "FuluConsumeJingli");
        percentage = Cultivation.tizhiDouble(player, "FuluConsumeJingliPercentage");
        rune = player.getData(ProfessionAttachments.PROGRESS).get(Profession.RUNE);
        playerData = player.getPersistentData().copy();
        player.setGameMode(GameType.SURVIVAL);
        player.setNoGravity(true);
        player.setInvulnerable(true);
        player.setDeltaMovement(Vec3.ZERO);
        BlockPos floor = player.blockPosition().above(6);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                put(player, floor.offset(x, 0, z), Blocks.STONE.defaultBlockState());
            }
        }
        player.connection.teleport(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5, 0, 0);
        player.setYHeadRot(0);
        player.setYBodyRot(0);
        PlayerAttributes.setContribution(player, "smoke:fulu41", new Contribution(Layer.BASE, Map.of(Stat.SOUL_MAX, 1000.0)));
        Cultivation.editTizhi(player, data -> {
            data.putDouble("FuluConsumeJingli", 0);
            data.putDouble("FuluConsumeJingliPercentage", 0);
        });
        NeoForge.EVENT_BUS.addListener(joined);
        NeoForge.EVENT_BUS.addListener(flight);
        NeoForge.EVENT_BUS.addListener(impact);
    }

    private void drawAndReject(ServerPlayer player) {
        player.setYRot(0);
        player.setXRot(0);
        player.setYHeadRot(0);
        player.setYBodyRot(0);
        target = pig(player, player.getEyePosition().add(player.getLookAngle().scale(5)).add(0, -0.3, 0));
        Relations.setTeam(target, "fulu41_target");
        expect("受控目标发射前生命", 20, target.getHealth());
        BlockPos pos = player.blockPosition().offset(3, 0, 0);
        put(player, pos, BuiltInRegistries.BLOCK.get(id("huafutai")).defaultBlockState());
        DrawingTableBlockEntity table = (DrawingTableBlockEntity) player.serverLevel().getBlockEntity(pos);
        CompoundTag data = table.getPersistentData();
        data.putInt("futou", 11);
        data.putInt("fudan", 1);
        data.putInt("tiandizhu", 2);
        data.putInt("RuneLEVEL", 1);
        table.setItem(0, item("fulufuzhi", 2));
        table.setItem(1, item("motiao_02", 24));
        double experience = Professions.experience(player, Profession.RUNE);
        DrawingTableRuntime.button(table, player, 6);
        DrawingTableRuntime.button(table, player, 6);
        drawn = table.getItem(2).copy();
        expectTrue("符头11符胆1制作两张二阶符并消耗24墨2纸", drawn.is(item("itemfulu_41", 1).getItem())
                && drawn.getCount() == 2 && drawn.get(DataComponents.CUSTOM_DATA).copyTag().getInt("fululevel") == 2
                && table.getItem(0).isEmpty() && table.getItem(1).isEmpty());
        expect("两张二阶画符经验", 40, Professions.experience(player, Profession.RUNE) - experience);
        table.setItem(2, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, drawn);
        Cultivation.set(player, CultivationValue.SOUL, 39);
        player.getPersistentData().putDouble("fulu41level", 19);
        drawn.getItem().releaseUsing(drawn, player.level(), player, 71990);
        expect("不足精力不扣费", 39, Cultivation.get(player, CultivationValue.SOUL));
        expect("不足精力不耗符", 2, drawn.getCount());
        expect("不足精力不改独立品阶", 19, player.getPersistentData().getDouble("fulu41level"));
        expectTrue("不足精力没有弹体", projectiles(player).isEmpty());
        player.getPersistentData().putDouble("fululevel", 77);
        soulBeforeLaunch = PlayerAttributes.value(player, Stat.SOUL_MAX);
        Cultivation.set(player, CultivationValue.SOUL, soulBeforeLaunch);
        player.inventoryMenu.broadcastChanges();
    }

    private void joined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof TalismanProjectile projectile && projectile.getType() == FuluSetup.projectile(Kind.FIVE_THUNDER).get()) {
            naturalProjectile = projectile;
            launchedAt = event.getLevel().getGameTime();
            soulAtLaunch = Cultivation.get((ServerPlayer) projectile.getOwner(), CultivationValue.SOUL);
            launchAligned = target.getBoundingBox().inflate(0.3).clip(projectile.position(),
                    projectile.position().add(projectile.getDeltaMovement().scale(4))).isPresent();
        }
        if (event.getEntity() instanceof ColoredLightningBolt bolt && target != null && target.hasEffect(effect())) {
            strikes.add(new Strike(target.getPersistentData().getString("fulu41jindu"), bolt.position(), bolt.color(), event.getLevel().getGameTime()));
            created.add(bolt);
        }
    }

    private void flight(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() == target && target.hasEffect(effect()) && !damageObserved && target.getHealth() < 18) {
            damageObserved = true;
            target.setInvulnerable(true);
        }
        if (event.getEntity() != naturalProjectile || flightTrace.size() >= 20) return;
        flightTrace.add("tick=" + (event.getEntity().level().getGameTime() - launchedAt) + " projectile=" + naturalProjectile.position()
                + " velocity=" + naturalProjectile.getDeltaMovement() + " removed=" + naturalProjectile.isRemoved()
                + " projectileTeam=" + Relations.entityTeam(naturalProjectile) + " targetBox=" + target.getBoundingBox()
                + " health=" + target.getHealth() + " targetTeam=" + Relations.entityTeam(target)
                + " effect=" + target.hasEffect(effect()));
    }

    private void impact(ProjectileImpactEvent event) {
        if (event.getProjectile() != naturalProjectile || naturalProjectile.level().isClientSide()) return;
        flightTrace.add("impact=" + event.getRayTraceResult().getType() + " at=" + event.getRayTraceResult().getLocation());
        if (event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() == target) {
            collisionObserved = true;
            firstObserved = naturalProjectile.level().getGameTime();
        }
    }

    private void verifyNaturalCycle(ServerPlayer player) {
        expectTrue("首轮真实雷伤已命中目标", damageObserved);
        expectTrue("五轮自然结束移除效果并清阶段计数", !target.hasEffect(effect())
                && target.getPersistentData().getString("fulu41jindu").isEmpty()
                && target.getPersistentData().getDouble("fulu41tick") == 0 && target.getPersistentData().getDouble("fulu41num") == 0);
        expect("完整五轮总83道雷", 83, strikes.size());
        expect("首轮白雷十道", 10, strikes.stream().filter(strike -> strike.phase().isEmpty() && strike.color() == -1).count());
        expect("次轮红雷十六道", 16, strikes.stream().filter(strike -> strike.phase().equals("二雷") && strike.color() == 0xffff0000).count());
        expect("三轮绿雷二十四道", 24, strikes.stream().filter(strike -> strike.phase().equals("三雷") && strike.color() == 0xff99ff99).count());
        expect("四轮来源蓝色解析默认白色二十四道", 24, strikes.stream().filter(strike -> strike.phase().equals("四雷") && strike.color() == -1).count());
        expect("末轮金雷九道", 9, strikes.stream().filter(strike -> strike.phase().equals("五雷") && strike.color() == 0xffffcc66).count());
        if (strikes.size() == 83) {
            expect("首雷至末雷间隔770刻", 770, strikes.getLast().time() - strikes.getFirst().time());
            expectTrue("自然命中后首雷在十刻内出现", strikes.getFirst().time() - firstObserved <= 10);
            List<Strike> green = strikes.stream().filter(strike -> strike.phase().equals("三雷")).toList();
            expectTrue("第三轮最后四雷同坐标", green.subList(20, 24).stream().map(Strike::at).distinct().count() == 1);
        }
    }

    private void verifyPersistenceAndBoundaries(ServerPlayer player) {
        target.getPersistentData().putString("fulu41jindu", "四雷");
        target.getPersistentData().putDouble("fulu41tick", 3);
        target.getPersistentData().putDouble("fulu41num", 7);
        TalismanProjectile projectile = FuluSetup.projectile(Kind.FIVE_THUNDER).get().create(player.serverLevel());
        projectile.getPersistentData().putString("belongToMasterUUID", player.getStringUUID());
        Relations.setTeam(projectile, "fulu41_target");
        FiveThunderCurse.hit(player.serverLevel(), projectile, target, 3);
        expectTrue("同实体团队拒绝效果", !target.hasEffect(effect()));
        Relations.setTeam(projectile, Relations.team(player));
        FiveThunderCurse.hit(player.serverLevel(), projectile, target, 3);
        expectTrue("重施使用新等级且保留旧进度", target.getEffect(effect()).getAmplifier() == 2
                && target.getPersistentData().getString("fulu41jindu").equals("四雷")
                && target.getPersistentData().getDouble("fulu41tick") == 3 && target.getPersistentData().getDouble("fulu41num") == 7);
        CompoundTag saved = new CompoundTag();
        target.save(saved);
        Pig loaded = (Pig) EntityType.loadEntityRecursive(saved, player.serverLevel(), entity -> entity);
        expectTrue("效果与阶段完整保存重读", loaded.getEffect(effect()).getAmplifier() == 2 && loaded.getEffect(effect()).getDuration() == 2400
                && loaded.getPersistentData().equals(target.getPersistentData()) && Relations.entityTeam(loaded).equals("fulu41_target"));
        loaded.removeEffect(effect());
        expectTrue("提前移除不清来源残余阶段", loaded.getPersistentData().getString("fulu41jindu").equals("四雷")
                && loaded.getPersistentData().getDouble("fulu41tick") == 3 && loaded.getPersistentData().getDouble("fulu41num") == 7);
        loaded.discard();
        target.removeEffect(effect());
        projectile.discard();
        NeoForge.EVENT_BUS.unregister(joined);
        NeoForge.EVENT_BUS.unregister(flight);
        NeoForge.EVENT_BUS.unregister(impact);
        player.getPersistentData().putDouble("fulu41level", 4);
        naturalProjectile = FuluSetup.projectile(Kind.FIVE_THUNDER).get().create(player.serverLevel());
        naturalProjectile.setOwner(player);
        naturalProjectile.setPos(player.position().add(0, 10, 0));
        naturalProjectile.setDeltaMovement(Vec3.ZERO);
        naturalProjectile.tick();
        CompoundTag arrow = new CompoundTag();
        naturalProjectile.save(arrow);
        expectTrue("弹体首刻交接身份品阶且原生内部物品可编码", arrow.getDouble("fululevel") == 4 && naturalProjectile.isNoGravity()
                && naturalProjectile.getPersistentData().getString("belongToMasterUUID").equals(player.getStringUUID())
                && Relations.entityTeam(naturalProjectile).equals(Relations.team(player)));
        TalismanProjectile restored = (TalismanProjectile) EntityType.loadEntityRecursive(arrow, player.serverLevel(), entity -> entity);
        naturalProjectile.discard();
        naturalProjectile = restored;
        for (lifeTicks = 1; lifeTicks < 16 && !restored.isRemoved(); lifeTicks++) restored.tick();
        ItemStack loadedItem = ItemStack.parseOptional(player.registryAccess(), (CompoundTag) drawn.save(player.registryAccess()));
        expectTrue("剩余符品阶和数量保存重读", loadedItem.getCount() == 1 && loadedItem.get(DataComponents.CUSTOM_DATA).copyTag().getInt("fululevel") == 2);
    }

    private void cleanup(ServerPlayer player) {
        NeoForge.EVENT_BUS.unregister(joined);
        NeoForge.EVENT_BUS.unregister(flight);
        NeoForge.EVENT_BUS.unregister(impact);
        created.forEach(Entity::discard);
        blocks.forEach((pos, state) -> player.serverLevel().setBlock(pos, state, Block.UPDATE_ALL));
        player.setItemInHand(InteractionHand.MAIN_HAND, originalHand);
        player.setGameMode(mode);
        player.setNoGravity(noGravity);
        player.setInvulnerable(invulnerable);
        player.connection.teleport(position.x, position.y, position.z, yaw, pitch);
        PlayerAttributes.clearContribution(player, "smoke:fulu41");
        Cultivation.set(player, CultivationValue.SOUL, soul);
        Cultivation.editTizhi(player, data -> {
            data.putDouble("FuluConsumeJingli", fixed);
            data.putDouble("FuluConsumeJingliPercentage", percentage);
        });
        player.getData(ProfessionAttachments.PROGRESS).set(Profession.RUNE, rune);
        player.syncData(ProfessionAttachments.PROGRESS);
        for (String key : List.of("fululevel", "fulu41level")) {
            if (playerData.contains(key)) player.getPersistentData().put(key, playerData.get(key).copy());
            else player.getPersistentData().remove(key);
        }
    }

    private Pig pig(ServerPlayer player, Vec3 at) {
        Pig pig = EntityType.PIG.create(player.serverLevel());
        pig.setNoAi(true);
        pig.setNoGravity(true);
        pig.setPos(at);
        player.serverLevel().addFreshEntity(pig);
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
        pig.setHealth(20);
        created.add(pig);
        return pig;
    }

    private void put(ServerPlayer player, BlockPos pos, BlockState state) {
        blocks.putIfAbsent(pos.immutable(), player.serverLevel().getBlockState(pos));
        player.serverLevel().setBlock(pos, state, Block.UPDATE_ALL);
    }

    private static List<TalismanProjectile> projectiles(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(TalismanProjectile.class, new AABB(player.position(), player.position()).inflate(40));
    }

    private static ItemStack item(String path, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(path)), count);
    }

    private static Holder<MobEffect> effect() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(id("fulutexiao_41")).orElseThrow();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
