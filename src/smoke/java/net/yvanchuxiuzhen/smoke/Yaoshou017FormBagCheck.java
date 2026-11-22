package net.yvanchuxiuzhen.smoke;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.SpiritBeastBagRuntime;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou017Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou01701Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 原生袋收放、自然技能候选、受控形态替换及同存档重进。 */
final class Yaoshou017FormBagCheck extends SystemCheck {
    private static final int HAND_SLOT = 2;
    private static final int SAVED_SLOT = 20;
    private static final double SPECIMEN_SIZE = 1.2;
    private static final String SHED = "脱壳而逃";
    private static final String REGROW = "长壳";
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Set<UUID> samples = new HashSet<>();
    private final Consumer<EntityTickEvent.Post> tickListener = this::onTick;
    private final Consumer<EntityJoinLevelEvent> joinListener = this::onJoin;
    private final Consumer<LivingDeathEvent> deathListener = this::onDeath;
    private ServerLevel initialLevel;
    private Vec3 originalPosition;
    private BlockPos floor;
    private float yaw;
    private float pitch;
    private int selected;
    private boolean shift;
    private GameType gameMode;
    private ItemStack handBefore;
    private ItemStack slotBefore;
    private ItemStack savedBag;
    private String bagId;
    private Pig target;
    private Yaoshou017Entity wild;
    private Yaoshou017Entity active;
    private Probe wildProbe;
    private Probe tameProbe;
    private Probe formProbe;
    private Yaoshou01701Entity probingForm;
    private Mob replacing;
    private Yaoshou01701Entity replacement;
    private UUID finalId;
    private int reloadedNetworkId = -1;
    private int deaths;
    private int replacements;
    private double replacementTick;
    private boolean stageStarted;
    private boolean listening;

    Yaoshou017FormBagCheck(List<String> report, String levelId) {
        super("寄魂蟹017形态与灵兽袋", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return switch (step) {
                case 0 -> { onServer(this::prepare); yield true; }
                case 1 -> { onServer(this::bagRoundTrip); yield true; }
                case 2 -> await(local, "野生与已驯候选", player -> {
                    if (wildProbe == null || tameProbe == null) return false;
                    expectCandidate("野生017低生命有脱壳候选", wildProbe, 5, 50);
                    expectCandidate("同条件已驯017没有脱壳候选", tameProbe, 0, 0);
                    wild.discard();
                    // 已驯样本仅验证替换时的原袋关联，不是自然可达路径。
                    startReplacement(active, SHED);
                    return true;
                });
                case 3 -> await(local, "第9刻脱壳", player -> {
                    if (replacement == null) return false;
                    checkReplacement(9, List.of("贫弱", "贫弱", "贫弱"));
                    rejectForm(player);
                    observeForm(16);
                    return true;
                });
                case 4 -> await(local, "远距虚弱对照", player -> {
                    if (formProbe == null) return false;
                    expectCandidate("脱壳后的虚弱阻止远距长壳", formProbe, 0, 0);
                    replacement.removeEffect(MobEffects.WEAKNESS);
                    observeForm(5);
                    return true;
                });
                case 5 -> await(local, "近距门槛", player -> {
                    if (formProbe == null) return false;
                    expectCandidate("无虚弱但距离不足时没有长壳候选", formProbe, 0, 0);
                    observeForm(16);
                    return true;
                });
                case 6 -> await(local, "远距长壳候选", player -> {
                    if (formProbe == null) return false;
                    expectCandidate("无虚弱且有效距离大于12时有长壳候选", formProbe, 20, 80);
                    startReplacement(replacement, REGROW);
                    return true;
                });
                case 7 -> await(local, "第60刻长壳", player -> {
                    if (replacement == null) return false;
                    prepareReload(player);
                    return true;
                });
                case 8 -> { leaveWorld(); yield true; }
                case 9 -> {
                    if (local > 1200) throw new IllegalStateException("退出后未进入标题界面");
                    yield reopened(local);
                }
                case 10 -> inWorld(local);
                case 11 -> {
                    onServer(this::verifyReload);
                    Entity tracked = minecraft.level.getEntity(reloadedNetworkId);
                    expectTrue("重进后客户端追踪同UUID的01701形态",
                            tracked instanceof Yaoshou01701Entity form && form.getUUID().equals(finalId)
                                    && Math.abs(form.getAttributeValue(YaoshouAttributes.SIZE) - SPECIMEN_SIZE) < 1e-6);
                    onServer(this::cleanup);
                    finish();
                    yield true;
                }
                default -> throw new IllegalStateException("未定义的夹具步骤 " + step);
            };
        } catch (RuntimeException error) {
            fail("步骤 " + step + "：" + error.getMessage());
            unlisten();
            if (handBefore != null && minecraft.getSingleplayerServer() != null && minecraft.player != null) onServer(this::cleanup);
            finish();
            return false;
        }
    }

    private boolean await(int local, String label, Predicate<ServerPlayer> ready) {
        boolean[] finished = {false};
        onServer(player -> finished[0] = ready.test(player));
        if (!finished[0] && local > 240) throw new IllegalStateException(label + "超时");
        return finished[0];
    }

    private void prepare(ServerPlayer player) {
        initialLevel = player.serverLevel();
        originalPosition = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        selected = player.getInventory().selected;
        shift = player.isShiftKeyDown();
        gameMode = player.gameMode.getGameModeForPlayer();
        handBefore = player.getInventory().getItem(HAND_SLOT).copy();
        slotBefore = player.getInventory().getItem(SAVED_SLOT).copy();
        player.getInventory().selected = HAND_SLOT;
        player.setGameMode(GameType.SURVIVAL);
        floor = player.blockPosition().above(16);
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos pos = floor.offset(x, 0, z);
                blocks.put(pos, initialLevel.getBlockState(pos));
                initialLevel.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
            }
        }
        player.connection.teleport(floor.getX() + .5, floor.getY() + 1, floor.getZ() + .5, 0, 35);
        NeoForge.EVENT_BUS.addListener(tickListener);
        NeoForge.EVENT_BUS.addListener(joinListener);
        NeoForge.EVENT_BUS.addListener(deathListener);
        listening = true;
    }

    private void bagRoundTrip(ServerPlayer player) {
        player.getInventory().setItem(HAND_SLOT, newBag());
        wild = newCrab(-2);
        capture(player, wild);
        expectTrue("野生017拒绝潜行收纳且不分配袋ID", wild.isAlive() && bagData(player).isEmpty());
        Yaoshou017Entity tamed = newCrab(2);
        tamed.tame(player);
        capture(player, tamed);
        bagId = bagData(player).getString("lingshoudaiuuid");
        require(tamed.isRemoved() && !bagId.isEmpty() && hasBagIdentity(player, false), "原生收纳未绑定017和实际槽位");
        active = releaseOne(player);
        expectTrue("收纳与首次放出保留低生命、体型和经验样本", active.getHealth() == 21
                && active.getMaxHealth() == 100 && active.getAttributeValue(YaoshouAttributes.SIZE) == SPECIMEN_SIZE
                && active.getPersistentData().getDouble("experience") == 37.5);
        release(player);
        expect("待收回状态不能重复释放", 1, released(player).size());
        UUID firstRelease = active.getUUID();
        capture(player, active);
        require(active.isRemoved() && hasBagIdentity(player, false), "同袋原生回收未恢复收纳状态");
        active = releaseOne(player);
        expectTrue("二次放出具有新实体UUID和原袋ID", !firstRelease.equals(active.getUUID()) && hasBagIdentity(player, true));
        savedBag = player.getInventory().getItem(HAND_SLOT).copy();
        Relations.setTeam(active, "smoke_017_team");
        Relations.addFriendlyTeam(active, "smoke_017_ally");
        target = EntityType.PIG.create(initialLevel);
        require(target != null, "无法创建活目标");
        anchor(target);
        placeTarget(active, 16);
        require(initialLevel.addFreshEntity(target), "无法加入活目标");
        samples.add(target.getUUID());
        aim(wild);
        aim(active);
    }

    private Yaoshou017Entity newCrab(int x) {
        Yaoshou017Entity crab = CreatureSetup.<Yaoshou017Entity>entityType("yaoshou_017").get().create(initialLevel);
        require(crab != null, "无法创建017样本");
        crab.moveTo(Vec3.atBottomCenterOf(floor.offset(x, 1, 2)));
        anchor(crab);
        CompoundTag data = crab.getPersistentData();
        data.putString("LEVELname", "一阶中级");
        data.putDouble("LEVELnumber", 4);
        data.putDouble("experience_Max", 350);
        data.putDouble("experience", 37.5);
        data.putDouble("health_Max", 100);
        data.putDouble("attack_Num", 12);
        data.putDouble("speed_number", .25);
        crab.getAttribute(YaoshouAttributes.SIZE).setBaseValue(SPECIMEN_SIZE);
        crab.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        crab.setHealth(21);
        require(initialLevel.addFreshEntity(crab), "017样本未入场");
        samples.add(crab.getUUID());
        return crab;
    }

    private Yaoshou017Entity releaseOne(ServerPlayer player) {
        release(player);
        List<Yaoshou017Entity> matches = released(player);
        require(matches.size() == 1, "以袋ID定位的放出实体数量为 " + matches.size());
        Yaoshou017Entity crab = matches.getFirst();
        samples.add(crab.getUUID());
        require(hasBagIdentity(player, true) && crab.isOwnedBy(player)
                && crab.getPersistentData().getString("belongToMasterUUID").equals(player.getStringUUID()), "放出未绑定主人或袋");
        crab.moveTo(Vec3.atBottomCenterOf(floor.offset(2, 1, 2)));
        anchor(crab);
        return crab;
    }

    private List<Yaoshou017Entity> released(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Yaoshou017Entity.class, player.getBoundingBox().inflate(8),
                entity -> entity.isAlive() && bagId.equals(entity.getPersistentData().getString("lingshoudaiuuid")));
    }

    private boolean hasBagIdentity(ServerPlayer player, boolean out) {
        CompoundTag data = bagData(player);
        return bagId.equals(data.getString("lingshoudaiuuid"))
                && data.getString("yaoshouid").equals(ModId.VALUE + ":yaoshou_017")
                && data.getString("belongToMasterUUID").equals(player.getStringUUID())
                && data.getBoolean("yaoshoushifang") == out;
    }

    private static CompoundTag bagData(ServerPlayer player) {
        return SpiritBeastBagRuntime.read(player.getInventory().getItem(HAND_SLOT));
    }

    private static ItemStack newBag() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshoudai_01")));
    }

    private void rejectForm(ServerPlayer player) {
        ItemStack original = player.getMainHandItem();
        expectTrue("脱壳后原袋完整组件未变", ItemStack.matches(savedBag, original));
        player.getInventory().setItem(HAND_SLOT, newBag());
        capture(player, replacement);
        expectTrue("用空袋也不能收纳01701或生成新袋ID", replacement.isAlive() && bagData(player).isEmpty());
        player.getInventory().setItem(HAND_SLOT, original);
    }

    private static void capture(ServerPlayer player, Entity entity) {
        player.setShiftKeyDown(true);
        try {
            player.interactOn(entity, InteractionHand.MAIN_HAND);
        } finally {
            player.setShiftKeyDown(false);
        }
    }

    private static void release(ServerPlayer player) {
        player.setShiftKeyDown(false);
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    private static void anchor(Mob mob) {
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.setPersistenceRequired();
        mob.setDeltaMovement(Vec3.ZERO);
    }

    private void placeTarget(Entity entity, int distance) {
        target.moveTo(entity.position().add(distance, 0, 0));
    }

    private void aim(Entity entity) {
        entity.getPersistentData().putString("linshigongjizhe", target.getStringUUID());
        entity.getPersistentData().putString("gongjizhe", target.getStringUUID());
    }

    private void observeForm(int distance) {
        placeTarget(replacement, distance);
        resetSkill(replacement);
        aim(replacement);
        formProbe = null;
        probingForm = replacement;
    }

    private static void resetSkill(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        for (String key : List.of("jineng", "jinengjinxing", "yvdingjineng", "yvdingjinengjishi", "entitytick", "jinengshuliang")) data.remove(key);
    }

    private void startReplacement(Mob source, String skill) {
        probingForm = null;
        replacing = source;
        replacement = null;
        stageStarted = false;
        resetSkill(source);
        aim(source);
        CompoundTag data = source.getPersistentData();
        if (skill.equals(REGROW)) {
            // 固定已观察到的候选，延迟与阶段仍由正常实体刻推进。
            data.putString("yvdingjineng", skill);
            data.putDouble("yvdingjinengjishi", formProbe.delay());
        } else {
            data.putString("jineng", skill);
        }
    }

    private void onTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level() != initialLevel || !samples.contains(entity.getUUID())) return;
        CompoundTag data = entity.getPersistentData();
        if (entity == replacing && data.getDouble("entitytick") == 1) {
            stageStarted = data.getString("jinengjinxing").equals(entity == active ? "结束" : REGROW);
        }
        if (entity == active && tameProbe == null) tameProbe = probe(entity, SHED);
        if (entity == wild && wildProbe == null) wildProbe = probe(entity, SHED);
        if (entity == probingForm && formProbe == null) formProbe = probe(entity, REGROW);
    }

    private static Probe probe(Entity entity, String skill) {
        CompoundTag data = entity.getPersistentData();
        for (int index = 1; index <= data.getDouble("jinengshuliang"); index++) {
            if (data.getString("linshijineng" + index).equals(skill)) {
                return new Probe(data.getDouble("jinengjishi" + index), data.getDouble("jinengqvanzhong" + index), true);
            }
        }
        return new Probe(0, 0, data.getDouble("jinengshuliang") > 0);
    }

    private void expectCandidate(String label, Probe probe, int delay, int weight) {
        expectTrue(label, probe.evaluated() && probe.delay() == delay && probe.weight() == weight);
    }

    private void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() != initialLevel || replacing == null || !(event.getEntity() instanceof Yaoshou01701Entity form)
                || replacing.distanceToSqr(form) > 1e-6) return;
        replacement = form;
        replacements++;
        replacementTick = replacing.getPersistentData().getDouble("entitytick");
        samples.add(form.getUUID());
        anchor(form);
        if (replacing == active) {
            expect("脱壳新形态初始技能", "逃跑", form.getPersistentData().getString("jineng"));
            var speed = form.getEffect(MobEffects.MOVEMENT_SPEED);
            var weakness = form.getEffect(MobEffects.WEAKNESS);
            expectTrue("脱壳速度II 100刻、虚弱II 400刻", speed != null && speed.getAmplifier() == 1 && speed.getDuration() == 100
                    && weakness != null && weakness.getAmplifier() == 1 && weakness.getDuration() == 400);
        }
    }

    private void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level() == initialLevel && samples.contains(event.getEntity().getUUID())) deaths++;
    }

    private void checkReplacement(int tick, List<String> defense) {
        String label = "替换第" + tick + "刻";
        require(initialLevel.getEntity(replacement.getUUID()) == replacement, label + "新实体未实际入场");
        expect(label + "源计时器", tick, replacementTick);
        expectTrue(label + "进入阶段及旧实体非死亡移除", stageStarted
                && replacing.getRemovalReason() == Entity.RemovalReason.DISCARDED && !replacing.getUUID().equals(replacement.getUUID()));
        expect(label + "生命", replacing.getHealth(), replacement.getHealth());
        expect(label + "体型", replacing.getAttributeValue(YaoshouAttributes.SIZE), replacement.getAttributeValue(YaoshouAttributes.SIZE));
        expect(label + "阵营", Relations.entityTeam(replacing), Relations.entityTeam(replacement));
        expect(label + "友方集合", Relations.friendlyTeams(replacing), Relations.friendlyTeams(replacement));
        CompoundTag source = replacing.getPersistentData();
        CompoundTag dest = replacement.getPersistentData();
        expect(label + "等级", source.getString("LEVELname"), dest.getString("LEVELname"));
        for (String field : List.of("LEVELnumber", "experience", "experience_Max", "health_Max", "attack_Num")) {
            expect(label + " " + field, source.getDouble(field), dest.getDouble(field));
        }
        expect(label + "防御资质", defense, List.of(dest.getString("attack_Def_LEVEL"), dest.getString("mana_Def_LEVEL"), dest.getString("soul_Def_LEVEL")));
        expectTrue(label + "不继承主人或袋ID", unlinked(replacement));
        replacing = null;
    }

    private static boolean unlinked(Yaoshou01701Entity form) {
        CompoundTag data = form.getPersistentData();
        return data.getString("lingshoudaiuuid").isEmpty() && data.getString("belongToMasterUUID").isEmpty();
    }

    private void prepareReload(ServerPlayer player) {
        checkReplacement(60, List.of("强劲", "勉强", "强劲"));
        expect("两次替换各产生一个新实体", 2, replacements);
        expect("收放和两次替换不产生死亡事件", 0, deaths);
        expectTrue("长壳后原槽仍持有放出状态的原编号袋",
                ItemStack.matches(savedBag, player.getInventory().getItem(HAND_SLOT)));
        release(player);
        expectTrue("原袋失去实体关联后仍不能再次放出", released(player).isEmpty());
        finalId = replacement.getUUID();
        target.discard();
        player.getInventory().setItem(SAVED_SLOT, player.getInventory().getItem(HAND_SLOT));
        player.getInventory().setItem(HAND_SLOT, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
        unlisten();
    }

    private void verifyReload(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        expectTrue("正常重进使用新服务端层", level != initialLevel);
        expectTrue("重进后槽 " + SAVED_SLOT + " 的原袋完整组件保留（ID " + bagId + "）",
                ItemStack.matches(savedBag, player.getInventory().getItem(SAVED_SLOT))
                        && ItemStack.matches(savedBag, minecraft.player.getInventory().getItem(SAVED_SLOT))
                        && player.getInventory().getItem(HAND_SLOT).isEmpty());
        expectTrue("重进后两次替换前的UUID均未复活",
                samples.stream().filter(id -> !id.equals(finalId)).noneMatch(id -> level.getEntity(id) != null));
        expectTrue("重进后最终01701以同一UUID存在且不关联袋或主人",
                level.getEntity(finalId) instanceof Yaoshou01701Entity form && form.isAlive() && unlinked(form));
        Entity loaded = level.getEntity(finalId);
        if (loaded != null) reloadedNetworkId = loaded.getId();
    }

    private void unlisten() {
        if (!listening) return;
        NeoForge.EVENT_BUS.unregister(tickListener);
        NeoForge.EVENT_BUS.unregister(joinListener);
        NeoForge.EVENT_BUS.unregister(deathListener);
        listening = false;
    }

    private void cleanup(ServerPlayer player) {
        for (UUID id : samples) {
            Entity entity = player.serverLevel().getEntity(id);
            if (entity != null) entity.discard();
        }
        player.getInventory().setItem(HAND_SLOT, handBefore);
        player.getInventory().setItem(SAVED_SLOT, slotBefore);
        player.getInventory().selected = selected;
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, yaw, pitch);
        blocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        player.setGameMode(gameMode);
        player.setShiftKeyDown(shift);
        player.inventoryMenu.broadcastChanges();
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException(reason);
    }

    private record Probe(double delay, double weight, boolean evaluated) { }
}
