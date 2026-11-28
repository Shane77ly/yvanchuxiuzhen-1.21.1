package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.guchong.api.GuchongCapture;
import net.yvanchuxiuzhen.system.guchong.api.GuchongRuntimeState;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanBlock;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanBlockEntity;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 03/04单虫入口与九槽罐自然生命周期；目标固定位置，放虫和回罐由真实服务端刻推进。 */
final class GuchongLifecycleCheck extends SystemCheck {
    private static final String JAR_UUID = "r10_lifecycle";
    private final List<String> diagnostics;
    private final Map<BlockPos, BlockState> originalBlocks = new LinkedHashMap<>();
    private final List<GuchongEntity> specimens = new ArrayList<>();
    private int executed = -1;
    private BlockPos floor;
    private BlockPos home;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private Difficulty originalDifficulty;
    private GameType originalMode;
    private ItemStack specimen;
    private ItemStack ownedSpecimen;
    private ItemStack untouchedJar;
    private GuchongEntity active;
    private UUID activeUuid;
    private Cow target;
    private Consumer<LivingExperienceDropEvent> experienceObserver;
    private int experienceEvents;
    private boolean experiencePlayerMatches;
    private int originalExperience;
    private boolean originalMobLoot;
    private boolean deathReady;
    private int groundReleases;

    GuchongLifecycleCheck(List<String> report, String levelId) {
        super("蛊虫生命周期", report, levelId);
        diagnostics = report;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> action(step, local, 8, this::prepare);
            case 1 -> action(step, local, 5, this::singleItems);
            case 2 -> action(step, local, 5, this::placeJar);
            case 3 -> action(step, local, 5, this::recognizeAndSave);
            case 4 -> action(step, local, 2, this::spawnTarget);
            case 5 -> released(local);
            case 6 -> local >= 60;
            case 7 -> action(step, local, 2, this::clearTarget);
            case 8 -> returned(local);
            case 9 -> action(step, local, 2, this::spawnTarget);
            case 10 -> released(local);
            case 11 -> action(step, local, 5, this::groundDeathAndPickup);
            case 12 -> action(step, local, 2, this::handheldRelease);
            case 13 -> action(step, local, 5, this::handheldRecall);
            case 14 -> action(step, local, 10, this::releaseForReload);
            case 15 -> { leaveWorld(); yield true; }
            case 16 -> reopened(local);
            case 17 -> inWorld(local);
            case 18 -> action(step, local, 5, this::checkReloadAndDeath);
            case 19 -> deathExperience(local);
            case 20 -> { onServer(this::cleanup); finish(); yield true; }
            default -> throw new IllegalStateException("未知蛊虫检查步骤 " + step);
        };
    }

    private boolean action(int step, int local, int wait, Consumer<ServerPlayer> run) {
        if (executed != step) {
            executed = step;
            onServer(run);
        }
        return local >= wait;
    }

    private void prepare(ServerPlayer player) {
        for (int grade = 1; grade <= 3; grade++) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "taoguan_0" + grade);
            if (!(BuiltInRegistries.BLOCK.get(id) instanceof TaoguanBlock)) {
                throw new IllegalStateException("蛊罐专属方块未实际装配 " + id);
            }
        }
        checkJarItemFields();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalDifficulty = player.level().getDifficulty();
        originalMode = player.gameMode.getGameModeForPlayer();
        player.closeContainer();
        player.getInventory().clearContent();
        player.setGameMode(GameType.SURVIVAL);
        player.server.setDifficulty(Difficulty.NORMAL, true);
        floor = player.blockPosition().above(16);
        home = floor.offset(2, 1, 0);
        setBlock(player, floor, Blocks.STONE.defaultBlockState());
        setBlock(player, home.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        originalBlocks.put(home, player.level().getBlockState(home));
        player.connection.teleport(floor.getX() + .5, floor.getY() + 1, floor.getZ() + .5, 0, 0);
    }

    private void checkJarItemFields() {
        for (String id : List.of("guchong_01", "guchong_03", "guchong_04")) {
            CompoundTag jar = new CompoundTag();
            jar.putString("guchongid0", "yvanchuxiuzhen:" + id);
            jar.putString("guchongname0", "[虫名]");
            jar.putString("guguanuuid0", "slot-uuid");
            jar.putString("belongToMasterUUID0", "owner");
            jar.putDouble("experience0", 37.5);
            jar.putDouble("health_Num0", 7);
            ItemStack menuItem = GuchongJarData.unpack(jar, 0, true);
            ItemStack groundItem = GuchongJarData.unpack(jar, 0, false);
            CompoundTag menu = GuchongJarData.read(menuItem);
            CompoundTag ground = GuchongJarData.read(groundItem);
            expectTrue(id + "菜单与地上罐重建名称仅作为显示名",
                    menuItem.getHoverName().getString().equals("虫名") && groundItem.getHoverName().getString().equals("虫名")
                            && !menu.contains("guchongname") && !ground.contains("guchongname"));
            expectTrue(id + "两条重建路径保留来源字段差异",
                    menu.getString("guguanuuid").equals("slot-uuid") && !ground.contains("guguanuuid")
                            && !menu.contains("health_Num") && ground.getDouble("health_Num") == 7
                            && menu.getDouble("experience") == 37.5 && ground.getString("belongToMasterUUID").equals("owner"));
        }
    }

    private void singleItems(ServerPlayer player) {
        for (String id : List.of("guchong_03", "guchong_04")) {
            ItemStack stack = item(id + "item");
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            useOn(player, floor, Direction.NORTH);
            expectTrue(id + "非顶面不释放且不消耗", stack.getCount() == 1 && insects(player).isEmpty());
            useOn(player, floor, Direction.UP);
            GuchongEntity wild = onlyInsect(player);
            specimens.add(wild);
            anchor(wild);
            expectTrue(id + "野生分支消耗物品并执行自然初始化", player.getMainHandItem().isEmpty()
                    && !EntityAttributes.realmName(wild).isEmpty() && !wild.isTame());
            String team = Relations.entityTeam(wild);
            GuchongCapture.captureWild(player.serverLevel(), wild, player.getStringUUID());
            ItemStack captured = player.getInventory().items.stream().filter(value -> value.is(item(id + "item").getItem())).findFirst().orElseThrow();
            CompoundTag data = GuchongJarData.read(captured);
            expectTrue(id + "捕获读取关系所有者的实体团队", !team.isEmpty() && data.getString("team").equals(team));
            specimen = captured.copy();
            player.getInventory().clearContent();
            data.putBoolean("guchongxvnfu", true);
            data.putDouble("health_Num", 7);
            data.putDouble("experience", 37.5);
            data.putString("belongToMasterUUID", "source_does_not_copy");
            GuchongJarData.write(captured, data);
            captured.set(DataComponents.CUSTOM_NAME, Component.literal("认主样本"));
            player.setItemInHand(InteractionHand.MAIN_HAND, captured);
            useOn(player, floor, Direction.UP);
            GuchongEntity tamed = onlyInsect(player);
            specimens.add(tamed);
            expectTrue(id + "认主分支恢复资质生命并认当前使用者为原生主人", player.getMainHandItem().isEmpty()
                    && tamed.isOwnedBy(player) && tamed.getHealth() == 7
                    && Relations.entityTeam(tamed).equals(Relations.team(player))
                    && EntityAttributes.rating(tamed, Stat.ATTACK).equals(data.getString("attack_Num_LEVEL")));
            expectTrue(id + "认主单虫恢复原生属性且不写当前生命字段",
                    tamed.getAttributeBaseValue(Attributes.MAX_HEALTH) == data.getDouble("health_Max")
                            && tamed.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == data.getDouble("attack_Num")
                            && tamed.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == data.getDouble("speed_number"));
            expectTrue(id + "保留来源遗漏经验与自定义主人UUID", EntityAttributes.experience(tamed) == 0
                    && tamed.getData(GuchongData.RUNTIME).ownerUuid().isEmpty());
            tamed.discard();
        }
    }

    private void placeJar(ServerPlayer player) {
        ItemStack jar = item("taoguan_03item");
        CompoundTag data = new CompoundTag();
        data.putString("guguanuuid", JAR_UUID);
        CompoundTag insect = GuchongJarData.read(specimen);
        insect.putDouble("health_Num", 7.75);
        GuchongJarData.write(specimen, insect);
        GuchongJarData.pack(specimen, data, 8, false);
        GuchongJarData.write(jar, data);
        player.setItemInHand(InteractionHand.MAIN_HAND, jar);
        useOn(player, home.below(), Direction.UP);
        TaoguanBlockEntity placed = jar(player);
        expectTrue("手持大罐实际放置并把第九槽移交地上罐", player.getMainHandItem().isEmpty()
                && placed.insectSlots() == 9 && placed.getItem(8).is(specimen.getItem()) && placed.getItem(0).isEmpty()
                && placed.jarData().uuid.equals(JAR_UUID));
    }

    private void recognizeAndSave(ServerPlayer player) {
        ItemStack blood = item("jingxve");
        CompoundTag data = new CompoundTag();
        data.putInt("playerlevelnum", 0);
        GuchongJarData.write(blood, data);
        player.setItemInHand(InteractionHand.MAIN_HAND, blood);
        useOn(player, home, Direction.UP);
        expectTrue("低境界精血拒绝认主并保留来源精血消耗", player.getMainHandItem().isEmpty()
                && !GuchongJarData.read(jar(player).getItem(8)).getBoolean("guchongxvnfu"));
        blood = item("jingxve");
        data.putInt("playerlevelnum", 1);
        GuchongJarData.write(blood, data);
        player.setItemInHand(InteractionHand.MAIN_HAND, blood);
        useOn(player, home, Direction.UP);
        ownedSpecimen = jar(player).getItem(8).copy();
        CompoundTag recognized = GuchongJarData.read(ownedSpecimen);
        expectTrue("足够境界精血认主写入第九槽并消耗精血", player.getMainHandItem().isEmpty()
                && recognized.getBoolean("guchongxvnfu") && recognized.getString("belongToMasterUUID").equals(player.getStringUUID()));
        TaoguanBlockEntity restored = (TaoguanBlockEntity) BlockEntity.loadStatic(home, jar(player).getBlockState(),
                jar(player).saveWithFullMetadata(player.registryAccess()), player.registryAccess());
        expectTrue("地上罐保存重读保留第九槽完整个体与罐UUID", ItemStack.isSameItemSameComponents(ownedSpecimen, restored.getItem(8))
                && restored.jarData().uuid.equals(JAR_UUID));
    }

    private void spawnTarget(ServerPlayer player) {
        target = EntityType.COW.create(player.serverLevel());
        target.moveTo(home.getX() + 4.5, home.getY(), home.getZ() + .5, 0, 0);
        target.setNoAi(true);
        target.setNoGravity(true);
        player.serverLevel().addFreshEntity(target);
    }

    private boolean released(int local) {
        boolean[] found = {false};
        onServer(player -> {
            if (!jar(player).jarData().released) return;
            active = onlyInsect(player);
            specimens.add(active);
            anchor(active);
            groundReleases++;
            CompoundTag slotData = GuchongJarData.read(jar(player).getItem(8));
            expect("地上虫物品名称字段第" + groundReleases + "次", groundReleases == 1 ? "" : "受控名称", slotData.getString("guchongname"));
            expect("地上罐来源名称表达式第" + groundReleases + "次", groundReleases == 1 ? "" : "]", active.getCustomName().getString());
            expectTrue("地上放虫恢复整数生命/攻击与双精度速度且不写当前生命字段",
                    active.getAttributeBaseValue(Attributes.MAX_HEALTH) == slotData.getInt("health_Max")
                            && active.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == slotData.getInt("attack_Num")
                            && active.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == slotData.getDouble("speed_number"));
            GuchongRuntimeState groundRuntime = active.getData(GuchongData.RUNTIME);
            expectTrue("地上罐自然计划刻释放第九槽并登记唯一计数", jar(player).jarData().releasedCount == 1
                    && groundRuntime.guarding() && groundRuntime.jarSlot() == 8
                    && active.getOwnerUUID().equals(player.getUUID()) && active.getHealth() == 7F);
            found[0] = true;
        });
        if (!found[0] && local > 200) throw new IllegalStateException("地上罐自然放虫超时");
        return found[0];
    }

    private void clearTarget(ServerPlayer player) {
        expectTrue("蛊虫自然20刻入口接收地上罐目标UUID", active.getTarget() == target);
        active.setHealth(5);
        active.setTarget(null);
        target.discard();
        ItemStack controlled = jar(player).getItem(8).copy();
        CompoundTag data = GuchongJarData.read(controlled);
        data.putString("guchongname", "受控名称");
        GuchongJarData.write(controlled, data);
        jar(player).setItem(8, controlled);
    }

    private boolean returned(int local) {
        boolean[] done = {false};
        onServer(player -> {
            done[0] = active.isRemoved() && jar(player).jarData().releasedCount == 0;
            if (done[0]) expectTrue("失去目标后自然回罐清计数并保留来源不回写生命的槽", !jar(player).jarData().released
                    && GuchongJarData.read(jar(player).getItem(8)).getDouble("health_Num") == 7.75);
        });
        if (!done[0] && local > 200) throw new IllegalStateException("地上罐自然回收超时");
        return done[0];
    }

    private void groundDeathAndPickup(ServerPlayer player) {
        active.hurt(player.damageSources().generic(), 10000);
        expectTrue("地上蛊虫自然死亡清第九槽和释放计数", jar(player).getItem(8).isEmpty()
                && jar(player).jarData().releasedCount == 0 && !jar(player).jarData().released);
        active.discard();
        target.discard();
        ModifierKeys.update(player, true, false);
        useOn(player, home, Direction.UP);
        ModifierKeys.update(player, false, false);
        ItemStack pickedUp = player.getInventory().items.stream().filter(value -> value.is(item("taoguan_03item").getItem())).findFirst().orElseThrow();
        expectTrue("Ctrl真实交互收起地上罐且保留罐UUID", player.level().isEmptyBlock(home)
                && GuchongJarData.read(pickedUp).getString("guguanuuid").equals(JAR_UUID)
                && player.getInventory().items.stream().filter(value -> value.is(pickedUp.getItem())).mapToInt(ItemStack::getCount).sum() == 1);
        player.getInventory().clearContent();
        CompoundTag data = GuchongJarData.read(pickedUp);
        GuchongJarData.pack(ownedSpecimen, data, 8, false);
        GuchongJarData.write(pickedUp, data);
        player.setItemInHand(InteractionHand.MAIN_HAND, pickedUp);
    }

    private void handheldRelease(ServerPlayer player) {
        useJar(player);
        active = onlyInsect(player);
        specimens.add(active);
        anchor(active);
        active.moveTo(player.position());
        CompoundTag slotData = GuchongJarData.read(player.getMainHandItem());
        expectTrue("手持放虫恢复双精度生命/攻击/速度且不写当前生命字段",
                active.getHealth() == (float) slotData.getDouble("health_Num8")
                        && active.getAttributeBaseValue(Attributes.MAX_HEALTH) == slotData.getDouble("health_Max8")
                        && active.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == slotData.getDouble("attack_Num8")
                        && active.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == slotData.getDouble("speed_number8"));
        GuchongRuntimeState runtime = active.getData(GuchongData.RUNTIME);
        expectTrue("手持罐释放真实认主个体并冻结罐UUID和槽号", GuchongJarData.read(player.getMainHandItem()).getDouble("shifangnum") == 1
                && runtime.jarUuid().equals(JAR_UUID)
                && runtime.jarSlot() == 8 && active.isOwnedBy(player));
    }

    private void handheldRecall(ServerPlayer player) {
        active.setHealth(6.5F);
        EntityAttributes.setExperience(active, 23.5);
        useJar(player);
        CompoundTag data = GuchongJarData.read(player.getMainHandItem());
        expectTrue("手持罐自然收回保存当前生命经验且清释放计数", active.isRemoved() && data.getDouble("health_Num8") == 6.5
                && data.getDouble("experience8") == 23.5 && data.getDouble("shifangnum") == 0 && !data.getBoolean("shifangguchong"));
    }

    private void releaseForReload(ServerPlayer player) {
        ItemStack jar = player.getMainHandItem();
        CompoundTag slots = GuchongJarData.read(jar);
        slots.putString("belongToMasterUUID0", player.getStringUUID());
        GuchongJarData.pack(ownedSpecimen, slots, 7, false);
        slots.putString("belongToMasterUUID7", "00000000-0000-0000-0000-000000000001");
        slots.putBoolean("guchongxvnfu8", false);
        GuchongJarData.write(jar, slots);
        useJar(player);
        active = onlyInsect(player);
        GuchongRuntimeState runtime = active.getData(GuchongData.RUNTIME);
        expectTrue("空槽和他主槽不计数，第九槽按主人放出并认主", GuchongJarData.read(player.getMainHandItem()).getDouble("shifangnum") == 1
                && runtime.jarSlot() == 8 && runtime.tamed()
                && runtime.ownerUuid().equals(player.getStringUUID()) && active.isOwnedBy(player));
        specimens.add(active);
        anchor(active);
        active.moveTo(player.position());
        activeUuid = active.getUUID();
        player.getInventory().setItem(9, player.getMainHandItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        untouchedJar = item("taoguan_03item");
        CompoundTag data = new CompoundTag();
        data.putString("guguanuuid", "unrelated_jar");
        GuchongJarData.pack(ownedSpecimen, data, 8, false);
        GuchongJarData.write(untouchedJar, data);
        player.getInventory().setItem(10, untouchedJar);
    }

    private void checkReloadAndDeath(ServerPlayer player) {
        active = (GuchongEntity) player.serverLevel().getEntity(activeUuid);
        if (active == null) throw new IllegalStateException("重登后未恢复已释放蛊虫 " + activeUuid);
        specimens.add(active);
        ItemStack saved = player.getInventory().getItem(9);
        expectTrue("真实退出重进保留活动虫与背包罐及归属", active.isOwnedBy(player) && active.getHealth() == 6.5F
                && EntityAttributes.experience(active) == 23.5 && active.getData(GuchongData.RUNTIME).jarUuid().equals(JAR_UUID)
                && GuchongJarData.read(saved).getBoolean("shifangguchong") && GuchongJarData.read(saved).getDouble("shifangnum") == 1);
        CompoundTag beforeDeath = GuchongJarData.read(saved);
        ItemStack otherBeforeDeath = player.getInventory().getItem(10).copy();
        originalMobLoot = player.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
        player.level().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(true, player.server);
        expectTrue("死亡经验事件前置：成年、经验未被消费、允许生物掉落",
                BuiltInRegistries.ENTITY_TYPE.getKey(active.getType()).getPath().equals("guchong_04")
                        && !active.isBaby() && !active.wasExperienceConsumed()
                        && player.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT));
        String playerTeam = Relations.team(player);
        String insectTeam = Relations.team(active);
        boolean friendly = Relations.isFriendly(player, active);
        diagnostics.add("INFO 蛊虫生命周期 重进关系：玩家团队=" + playerTeam + " 虫团队=" + insectTeam + " 玩家视虫为友方=" + friendly);
        boolean friendlyFixture = !playerTeam.isEmpty() && playerTeam.equals(insectTeam) && friendly;
        expectTrue("重进后虫与主人同队且为友方", friendlyFixture);
        if (!friendlyFixture) {
            player.level().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(originalMobLoot, player.server);
            return;
        }
        boolean emptyEquipment = true;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!player.getItemBySlot(slot).isEmpty()) {
                diagnostics.add("INFO 蛊虫生命周期 击杀玩家非空装备：" + slot + "=" + player.getItemBySlot(slot));
                emptyEquipment = false;
            }
        }
        expectTrue("经验数值前置：击杀玩家所有装备槽为空", emptyEquipment);
        if (!emptyEquipment) {
            player.level().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(originalMobLoot, player.server);
            return;
        }
        experienceEvents = 0;
        experiencePlayerMatches = false;
        originalExperience = -1;
        experienceObserver = event -> {
            if (event.getEntity() != active) return;
            experienceEvents++;
            experiencePlayerMatches = event.getAttackingPlayer() == player;
            originalExperience = event.getOriginalExperience();
            diagnostics.add("INFO 蛊虫生命周期 经验事件#" + experienceEvents
                    + " 攻击玩家=" + (event.getAttackingPlayer() == null ? "null" : event.getAttackingPlayer().getStringUUID())
                    + " 预期玩家=" + player.getStringUUID() + " 同一玩家=" + experiencePlayerMatches
                    + " 原始经验=" + originalExperience + " 当前掉落经验=" + event.getDroppedExperience());
        };
        List<String> damageEvents = new ArrayList<>();
        Consumer<LivingIncomingDamageEvent> incoming = event -> {
            if (event.getEntity() == active) damageEvents.add("incoming canceled=" + event.isCanceled()
                    + " original=" + event.getOriginalAmount() + " amount=" + event.getAmount());
        };
        Consumer<LivingDamageEvent.Pre> damage = event -> {
            if (event.getEntity() == active) damageEvents.add("damagePre amount=" + event.getNewDamage());
        };
        Consumer<LivingDeathEvent> death = event -> {
            if (event.getEntity() == active) damageEvents.add("death canceled=" + event.isCanceled());
        };
        NeoForge.EVENT_BUS.addListener(experienceObserver);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, incoming);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, damage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, death);
        try {
            DamageSource source = player.damageSources().playerAttack(player);
            float healthBefore = active.getHealth();
            boolean immuneBefore = active.isInvulnerableTo(source);
            int cooldownBefore = active.invulnerableTime;
            boolean friendlyHurt = active.hurt(source, 10000);
            diagnostics.add("INFO 蛊虫生命周期 友方伤害：原生免疫=" + immuneBefore + " 无敌刻=" + cooldownBefore
                    + " hurt=" + friendlyHurt + " 生命=" + healthBefore + "->" + active.getHealth()
                    + " 死亡=" + active.isDeadOrDying() + " 移除=" + active.isRemoved() + " 事件=" + damageEvents);
            boolean friendlyBlocked = active.getHealth() == healthBefore && !active.isDeadOrDying()
                    && damageEvents.stream().noneMatch(value -> value.startsWith("damagePre") || value.startsWith("death"));
            expectTrue("原关系下玩家攻击被盾后免伤拦截", friendlyBlocked);
            if (!friendlyBlocked) {
                stopExperienceObservation(player);
                return;
            }
            damageEvents.clear();
            // 受控改变虫的团队并清零同刻无敌帧，保留主人与罐关联，使玩家伤害能进入正常结算。
            Relations.setTeam(active, "");
            active.invulnerableTime = 0;
            healthBefore = active.getHealth();
            immuneBefore = active.isInvulnerableTo(source);
            cooldownBefore = active.invulnerableTime;
            boolean hurt = active.hurt(source, 10000);
            diagnostics.add("INFO 蛊虫生命周期 受控非友方伤害：玩家视虫为友方=" + Relations.isFriendly(player, active)
                    + " 虫团队=" + Relations.team(active) + " 原生免疫=" + immuneBefore + " 无敌刻=" + cooldownBefore
                    + " hurt=" + hurt + " 生命=" + healthBefore + "->" + active.getHealth()
                    + " 死亡=" + active.isDeadOrDying() + " 移除=" + active.isRemoved() + " 事件=" + damageEvents
                    + " 经验事件=" + experienceEvents);
            expect("非友方玩家伤害hurt返回", true, hurt);
            expect("伤害后死亡状态", true, active.isDeadOrDying());
            deathReady = hurt && active.isDeadOrDying();
            if (!deathReady) {
                Relations.setTeam(active, insectTeam);
                stopExperienceObservation(player);
                return;
            }
            expect("致死时经验事件次数", 1, experienceEvents);
            expect("致死经验事件归因击杀玩家", true, experiencePlayerMatches);
            expectTrue("致死经验事件原始经验应为1至3，实际 " + originalExperience,
                    originalExperience >= 1 && originalExperience <= 3);
        } catch (RuntimeException | Error exception) {
            stopExperienceObservation(player);
            throw exception;
        } finally {
            NeoForge.EVENT_BUS.unregister(incoming);
            NeoForge.EVENT_BUS.unregister(damage);
            NeoForge.EVENT_BUS.unregister(death);
        }
        CompoundTag result = GuchongJarData.read(player.getInventory().getItem(9));
        expectTrue("非手持背包罐按UUID清死亡槽且不修改另一只罐", result.getString("guchongid8").isEmpty()
                && result.getDouble("shifangnum") == 0 && !result.getBoolean("shifangguchong")
                && !beforeDeath.getString("guchongid7").isEmpty() && result.getString("guchongid7").equals(beforeDeath.getString("guchongid7"))
                && result.getString("belongToMasterUUID7").equals(beforeDeath.getString("belongToMasterUUID7"))
                && ItemStack.isSameItemSameComponents(otherBeforeDeath, player.getInventory().getItem(10)));
    }

    private boolean deathExperience(int local) {
        if (!deathReady) {
            diagnostics.add("INFO 蛊虫生命周期 未达到致死前置，跳过死亡刻与经验总次数检查");
            return true;
        }
        boolean[] finished = {false};
        onServer(player -> {
            if (!active.isRemoved() && local <= 200) return;
            try {
                expectTrue("04蛊虫到达第20死亡刻并自然移除", active.isRemoved() && active.deathTime == 20);
                expect("整段死亡生命周期的经验掉落事件次数", 1, experienceEvents);
            } finally {
                stopExperienceObservation(player);
            }
            finished[0] = true;
        });
        return finished[0];
    }

    private void stopExperienceObservation(ServerPlayer player) {
        if (experienceObserver == null) return;
        NeoForge.EVENT_BUS.unregister(experienceObserver);
        experienceObserver = null;
        player.level().getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(originalMobLoot, player.server);
    }

    private static void useJar(ServerPlayer player) {
        player.setShiftKeyDown(false);
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
    }

    private static void useOn(ServerPlayer player, BlockPos pos, Direction face) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
    }

    private static void anchor(GuchongEntity entity) {
        entity.setNoAi(true);
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
    }

    private List<GuchongEntity> insects(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(GuchongEntity.class, player.getBoundingBox().inflate(12), entity -> entity.isAlive());
    }

    private GuchongEntity onlyInsect(ServerPlayer player) {
        List<GuchongEntity> insects = insects(player);
        if (insects.size() != 1) throw new IllegalStateException("预期一只活动蛊虫，实际 " + insects.size());
        return insects.getFirst();
    }

    private TaoguanBlockEntity jar(ServerPlayer player) {
        return (TaoguanBlockEntity) player.level().getBlockEntity(home);
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id)));
    }

    private void setBlock(ServerPlayer player, BlockPos pos, BlockState state) {
        originalBlocks.put(pos, player.level().getBlockState(pos));
        player.level().setBlock(pos, state, 3);
    }

    private void cleanup(ServerPlayer player) {
        stopExperienceObservation(player);
        for (GuchongEntity insect : specimens) if (!insect.isRemoved()) insect.discard();
        if (target != null && !target.isRemoved()) target.discard();
        player.getInventory().clearContent();
        originalBlocks.forEach((pos, state) -> player.level().setBlock(pos, state, 3));
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.setGameMode(originalMode);
        player.server.setDifficulty(originalDifficulty, true);
    }
}
