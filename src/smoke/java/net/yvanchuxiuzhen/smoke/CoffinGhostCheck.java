package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.system.creature.content.CoffinBlock;
import net.yvanchuxiuzhen.system.creature.content.CoffinBlockEntity;
import net.yvanchuxiuzhen.system.creature.content.CoffinBlockItem;
import net.yvanchuxiuzhen.system.creature.entity.Guiguai01Entity;

/** 棺材自然计划刻、游魂原生蛋与自然接触攻击、真实重进和来源死亡条件的必要检查。 */
final class CoffinGhostCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final Consumer<LivingIncomingDamageEvent> incomingDamage = this::observeGhostAttack;
    private int executed = -1;
    private BlockPos origin;
    private Vec3 originalPosition;
    private Vec3 originalMovement;
    private GameType originalMode;
    private Difficulty originalDifficulty;
    private ItemStack originalHand;
    private boolean originalInvulnerable;
    private boolean originalNoGravity;
    private float originalYaw;
    private float originalPitch;
    private UUID zombieId;
    private UUID ghostId;
    private UUID deadGhostId;
    private Guiguai01Entity ghost;
    private Vec3 flightStart;
    private long intervalStart;
    private long deathStart;
    private boolean damageObserverRegistered;
    private volatile boolean contactObserved;
    private boolean targetObserved;
    private boolean movementObserved;
    private boolean screenshotTaken;

    CoffinGhostCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("棺材与游魂", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> action(step, local, this::prepare);
            case 1 -> modeInterval(step, local, "创造", GameType.ADVENTURE);
            case 2 -> modeInterval(step, local, "冒险", GameType.SURVIVAL);
            case 3 -> naturalSpawn(local);
            case 4 -> scheduledBoundary(step, local, 60, 59);
            case 5 -> scheduledBoundary(step, local, 50, 49);
            case 6 -> scheduledBoundary(step, local, 48, 47);
            case 7 -> scheduledBoundary(step, local, 1, 0);
            case 8 -> action(step, local, this::spawnGhostWithEgg);
            case 9 -> naturalGhostAttack(step, local);
            case 10 -> beforeSave(step, local);
            case 11 -> {
                onServer(player -> unregisterDamageObserver());
                leaveWorld();
                yield true;
            }
            case 12 -> reopened(local);
            case 13 -> inWorld(local);
            case 14 -> action(step, local, this::checkAfterSave);
            case 15 -> action(step, local, this::killGhost);
            case 16 -> deathAndBreak(local);
            case 17 -> {
                onServer(this::cleanup);
                finish();
                yield true;
            }
            default -> throw new IllegalStateException("未知棺材游魂检查步骤 " + step);
        };
    }

    private boolean action(int step, int local, Consumer<ServerPlayer> operation) {
        if (executed != step) {
            executed = step;
            onServer(operation);
        }
        return local >= 2;
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        originalPosition = player.position();
        originalMovement = player.getDeltaMovement();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalDifficulty = level.getDifficulty();
        originalHand = player.getMainHandItem().copy();
        originalInvulnerable = player.isInvulnerable();
        originalNoGravity = player.isNoGravity();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        ResourceLocation coffinId = id("guancai_01");
        ResourceLocation ghostType = id("guiguai_01");
        ResourceLocation eggId = id("guiguai_01_spawn_egg");
        if (!(BuiltInRegistries.BLOCK.get(coffinId) instanceof CoffinBlock)
                || !(BuiltInRegistries.ITEM.get(coffinId) instanceof CoffinBlockItem)
                || !BuiltInRegistries.ENTITY_TYPE.containsKey(ghostType)
                || !(BuiltInRegistries.ITEM.get(eggId) instanceof SpawnEggItem egg)
                || egg.getType(ItemStack.EMPTY) != BuiltInRegistries.ENTITY_TYPE.get(ghostType)) {
            abort(player, "棺材/游魂或原生蛋工厂未完成登记，停止依赖步骤");
            return;
        }
        player.server.setDifficulty(Difficulty.NORMAL, true);
        player.setGameMode(GameType.CREATIVE);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        origin = player.blockPosition().above(16);
        for (int x = -4; x <= 8; x++) {
            for (int z = -4; z <= 8; z++) {
                level.setBlock(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
                for (int y = 0; y <= 5; y++) level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(origin.offset(x, 6, z), Blocks.STONE.defaultBlockState(), 3);
            }
        }
        level.getEntitiesOfClass(Zombie.class, area()).forEach(Entity::discard);
        player.teleportTo(level, origin.getX() + 2.5, origin.getY(), origin.getZ() + 2.5, 135, 15);
        player.setItemInHand(InteractionHand.MAIN_HAND, item("guancai_01"));
        useOn(player, origin.below());
        boolean placed = level.getBlockState(origin).getBlock() instanceof CoffinBlock
                && level.getBlockEntity(origin) instanceof CoffinBlockEntity;
        expectTrue("原生方块物品放置棺材与方块实体", placed);
        if (!placed) {
            abort(player, "原生棺材物品未在预期位置创建方块实体，停止依赖步骤");
            return;
        }
        expect("来源零槽容器不增加储物槽", 0, coffin(player).getContainerSize());
        expectTrue("无方向能力保持零槽", zeroSlots(level, null));
        for (Direction direction : Direction.values()) expectTrue("有方向能力保持零槽 " + direction, zeroSlots(level, direction));
    }

    private boolean modeInterval(int step, int local, String mode, GameType next) {
        if (executed != step) {
            executed = step;
            onServer(player -> intervalStart = player.serverLevel().getGameTime());
        }
        boolean[] ready = {false};
        onServer(player -> {
            if (player.serverLevel().getGameTime() - intervalStart < 25) return;
            ready[0] = true;
            expect(mode + "玩家经过自然计划刻不触发", 0, coffin(player).getPersistentData().getDouble("opentick"));
            player.setGameMode(next);
        });
        if (!ready[0] && local > 200) onServer(player -> abort(player, mode + "排除检查未推进二十五个服务端刻"));
        return ready[0];
    }

    private boolean zeroSlots(ServerLevel level, Direction direction) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, origin, direction);
        return handler != null && handler.getSlots() == 0;
    }

    private boolean naturalSpawn(int local) {
        boolean[] ready = {false};
        onServer(player -> {
            double remaining = coffin(player).getPersistentData().getDouble("opentick");
            if (remaining <= 0 || remaining > 59) return;
            ready[0] = true;
            checkNaturalSpawn(player);
        });
        if (!ready[0] && local > 240) onServer(player -> abort(player, "生存玩家未在时限内由自然计划刻生成僵尸"));
        return ready[0];
    }

    private void checkNaturalSpawn(ServerPlayer player) {
        CoffinBlockEntity coffin = coffin(player);
        expectTrue("生存玩家由自然二十刻计划刻触发并推进倒计时",
                coffin.getPersistentData().getDouble("opentick") > 0 && coffin.getPersistentData().getDouble("opentick") <= 59);
        expectTrue("来源感应随机动画保持1至3", coffin.getBlockState().getValue(CoffinBlock.ANIMATION) >= 1
                && coffin.getBlockState().getValue(CoffinBlock.ANIMATION) <= 3);
        List<Zombie> zombies = zombies(player);
        expect("自然六十到五十九只生成一只僵尸", 1, zombies.size());
        expectTrue("生成标记已持久化", coffin.getPersistentData().getBoolean("shengcheng"));
        if (zombies.size() != 1) {
            abort(player, "自然僵尸生成数量前置失败，停止依赖步骤");
            return;
        }
        Zombie zombie = zombies.getFirst();
        zombieId = zombie.getUUID();
        zombie.setPersistenceRequired();
        zombie.setNoAi(true);
        zombie.setInvulnerable(true);
        zombie.moveTo(origin.getX() + 1.5, origin.getY(), origin.getZ() + 0.5, 0, 0);
        player.setGameMode(GameType.CREATIVE);
        expect("来源僵尸名称", "被炼化的僵尸", zombie.getName().getString());
        expect("来源僵尸最大生命基值", 350, zombie.getAttribute(Attributes.MAX_HEALTH).getBaseValue());
        expect("来源僵尸攻击基值", 28, zombie.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
        expectTrue("来源铁剑或原生Zombie终结器选出的铁武器", zombie.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.IRON_SWORD)
                || zombie.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.IRON_SHOVEL));
        String armorSet = BuiltInRegistries.ITEM.getKey(zombie.getItemBySlot(EquipmentSlot.HEAD).getItem()).getPath()
                .replace("clothes_", "").replace("_helmet", "");
        expectTrue("来源头盔选中完整服装套装", List.of("05", "01", "02").contains(armorSet));
        for (EquipmentSlot slot : List.of(EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            String piece = switch (slot) {
                case CHEST -> "chestplate";
                case LEGS -> "leggings";
                default -> "boots";
            };
            expectTrue("来源僵尸服装部件匹配 " + piece, BuiltInRegistries.ITEM.getKey(zombie.getItemBySlot(slot).getItem())
                    .getPath().equals("clothes_" + armorSet + "_" + piece));
        }
        expect("来源物防", 24, zombie.getPersistentData().getDouble("attack_Def"));
        expect("来源法防", 15, zombie.getPersistentData().getDouble("mana_Def"));
        expect("来源魂防", 40, zombie.getPersistentData().getDouble("soul_Def"));
    }

    private boolean scheduledBoundary(int step, int local, double before, double expected) {
        if (executed != step) {
            executed = step;
            onServer(player -> {
                CompoundTag data = coffin(player).getPersistentData();
                data.putDouble("opentick", before);
                data.putBoolean("shengcheng", true);
                coffin(player).setChanged();
                intervalStart = player.serverLevel().getGameTime();
            });
        }
        boolean[] ready = {false};
        onServer(player -> {
            CompoundTag data = coffin(player).getPersistentData();
            if (data.getDouble("opentick") != expected) return;
            ready[0] = true;
            expectTrue("受控" + (int) expected + "边界由后续自然计划刻推进",
                    player.serverLevel().getGameTime() > intervalStart);
            if (expected == 59) expect("生成标记已为真时不重复生成", 1, zombies(player).size());
            if (expected == 49) expect("自然计划刻四十九边界播放来源动画四", 4,
                    coffin(player).getBlockState().getValue(CoffinBlock.ANIMATION).intValue());
            if (expected == 47) expect("自然计划刻四十七边界恢复来源动画零", 0,
                    coffin(player).getBlockState().getValue(CoffinBlock.ANIMATION).intValue());
            if (expected == 0) {
                expectTrue("自然计划刻冷却归零清除生成标记", !data.getBoolean("shengcheng"));
                data.putDouble("opentick", 42);
                data.putBoolean("shengcheng", true);
                coffin(player).setChanged();
            }
        });
        if (!ready[0] && local > 200) onServer(player -> abort(player, "自然计划刻未到达受控" + (int) expected + "边界"));
        return ready[0];
    }

    private void spawnGhostWithEgg(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, item("guiguai_01_spawn_egg"));
        useOn(player, origin.offset(4, -1, 0));
        List<Guiguai01Entity> ghosts = player.serverLevel().getEntitiesOfClass(Guiguai01Entity.class, area());
        expectTrue("生存原生刷怪蛋消耗并生成独立游魂", player.getMainHandItem().isEmpty() && ghosts.size() == 1);
        if (ghosts.size() != 1) {
            abort(player, "游魂刷怪蛋数量前置失败，停止依赖步骤");
            return;
        }
        ghost = ghosts.getFirst();
        ghostId = ghost.getUUID();
        ghost.setPersistenceRequired();
        ghost.setNoAi(true);
        ghost.setNoGravity(false);
        expectTrue("来源飞行移动控制、导航与强制无重力", ghost.getMoveControl() instanceof FlyingMoveControl
                && ghost.getNavigation() instanceof FlyingPathNavigation && ghost.isNoGravity());
        expect("原生蛋触发来源等级初始化", 3, ghost.getPersistentData().getDouble("LEVELnumber"));
        expect("原生蛋触发来源境界初始化", "一阶中级", ghost.getPersistentData().getString("LEVELname"));
        expect("原生蛋触发来源资质生命公式", 131, ghost.getMaxHealth());
        expect("来源默认透明基础纹理", "kong", ghost.texture());
        ghost.getPersistentData().putBoolean("IsAngry", true);
        CompoundTag angrySnapshot = ghost.saveWithoutId(new CompoundTag());
        expectTrue("受控IsAngry自由字段保存于NeoForgeData子项", angrySnapshot.getCompound("NeoForgeData").getBoolean("IsAngry"));
        expectTrue("受控自由字段不产生来源读取的根IsAngry键", !angrySnapshot.contains("IsAngry"));
        ghost.getPersistentData().putBoolean("IsAngry", false);
        float health = ghost.getHealth();
        expectTrue("来源坠落免疫", !ghost.hurt(ghost.damageSources().fall(), 1));
        expectTrue("来源仙人掌免疫", !ghost.hurt(ghost.damageSources().cactus(), 1));
        expectTrue("来源溺水免疫", !ghost.hurt(ghost.damageSources().drown(), 1));
        expect("来源免疫不扣生命", health, ghost.getHealth());
        ghost.invulnerableTime = 0;
        expectTrue("非指定魔法伤害仍进入原生扣血", ghost.hurt(ghost.damageSources().magic(), 1) && ghost.getHealth() < health);
        ghost.setHealth(health);
    }

    private boolean naturalGhostAttack(int step, int local) {
        if (executed != step) {
            executed = step;
            onServer(player -> {
                player.setGameMode(GameType.SURVIVAL);
                player.setInvulnerable(false);
                player.setNoGravity(true);
                player.setDeltaMovement(Vec3.ZERO);
                player.teleportTo(player.serverLevel(), origin.getX() + 2.5, origin.getY() + 1,
                        origin.getZ() + 2.5, 90, 0);
                ghost.moveTo(player.getX() + 3, player.getY(), player.getZ(), 90, 0);
                ghost.setDeltaMovement(Vec3.ZERO);
                flightStart = ghost.position();
                ghost.setNoAi(false);
                NeoForge.EVENT_BUS.addListener(incomingDamage);
                damageObserverRegistered = true;
            });
        }
        boolean[] ready = {false};
        onServer(player -> {
            targetObserved |= ghost.getTarget() == player;
            movementObserved |= ghost.position().distanceToSqr(flightStart) > 0.04;
            if (!targetObserved || !movementObserved || !contactObserved) return;
            ready[0] = true;
            expectTrue("游魂自然AI选中可攻击玩家", targetObserved);
            expectTrue("游魂自然飞行接近目标", movementObserved);
            expectTrue("游魂自然接触触发原生伤害入口", contactObserved);
            ghost.setNoAi(true);
            ghost.setTarget(null);
            ghost.getNavigation().stop();
            ghost.setDeltaMovement(Vec3.ZERO);
            player.setInvulnerable(true);
            player.setGameMode(GameType.CREATIVE);
            unregisterDamageObserver();
        });
        if (!ready[0] && local > 300) onServer(player -> abort(player, "游魂自然索敌、飞行或接触攻击未在时限内完成"));
        return ready[0];
    }

    private void observeGhostAttack(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer && event.getSource().getEntity() == ghost) {
            contactObserved = true;
            event.setCanceled(true);
        }
    }

    private void unregisterDamageObserver() {
        if (damageObserverRegistered) {
            NeoForge.EVENT_BUS.unregister(incomingDamage);
            damageObserverRegistered = false;
        }
    }

    private boolean beforeSave(int step, int local) {
        if (executed != step) {
            executed = step;
            onServer(player -> {
                ghost.moveTo(origin.getX() + 4.5, origin.getY() + 0.8, origin.getZ() + 0.5, 180, 0);
                ghost.setDeltaMovement(Vec3.ZERO);
                CompoundTag data = coffin(player).getPersistentData();
                data.putDouble("opentick", 42);
                data.putBoolean("shengcheng", true);
                coffin(player).setChanged();
                player.teleportTo(player.serverLevel(), origin.getX() + 2.5, origin.getY() + 0.6,
                        origin.getZ() + 6.5, 180, 10);
            });
        }
        if (local < 20) return false;
        if (!screenshotTaken) {
            expect("棺材中文名称", "棺材", I18n.get("block.yvanchuxiuzhen.guancai_01"));
            expect("游魂中文名称", "游魂", I18n.get("entity.yvanchuxiuzhen.guiguai_01"));
            expect("游魂刷怪蛋中文名称", "游魂", I18n.get("item.yvanchuxiuzhen.guiguai_01_spawn_egg"));
            expectTrue("客户端棺材渲染器已装配", minecraft.level.getBlockEntity(origin) instanceof CoffinBlockEntity
                    && minecraft.getBlockEntityRenderDispatcher().getRenderer(minecraft.level.getBlockEntity(origin)) != null);
            expectTrue("客户端棺材物品渲染器已装配", ((CoffinBlockItem) item("guancai_01").getItem()).getRenderProvider() != null);
            Entity clientGhost = null;
            for (Entity candidate : minecraft.level.entitiesForRendering()) {
                if (candidate.getUUID().equals(ghostId)) clientGhost = candidate;
            }
            expectTrue("客户端游魂同步与渲染器已装配", clientGhost instanceof Guiguai01Entity
                    && minecraft.getEntityRenderDispatcher().getRenderer(clientGhost) != null);
            for (String path : List.of("geo/guancai.geo.json", "geo/guiguai01.geo.json",
                    "animations/guancai.animation.json", "animations/guiguai01.animation.json",
                    "textures/block/guancai01.png", "textures/entities/guiguai01.png", "textures/entities/kong.png")) {
                expectTrue("来源模型资源已加载 " + path, minecraft.getResourceManager().getResource(id(path)).isPresent());
            }
            screenshots.take("r07_coffin_ghost.png");
            screenshotTaken = true;
        }
        return true;
    }

    private void checkAfterSave(ServerPlayer player) {
        if (!(player.serverLevel().getBlockEntity(origin) instanceof CoffinBlockEntity)) {
            abort(player, "真实重进缺少棺材方块实体，停止依赖步骤");
            return;
        }
        CompoundTag data = coffin(player).getPersistentData();
        expectTrue("真实保存重进保留开棺计时并继续自然推进", data.getDouble("opentick") > 0 && data.getDouble("opentick") < 42);
        expectTrue("真实保存重进保留生成标记", data.getBoolean("shengcheng"));
        expectTrue("真实保存重进保留原僵尸且未重复生成", player.serverLevel().getEntity(zombieId) instanceof Zombie
                && zombies(player).size() == 1);
        Entity loaded = player.serverLevel().getEntity(ghostId);
        if (!(loaded instanceof Guiguai01Entity restored)) {
            abort(player, "真实重进缺少同UUID游魂，停止依赖死亡步骤");
            return;
        }
        ghost = restored;
        expectTrue("真实保存重进保留游魂类型与来源资质", ghost.getPersistentData().getDouble("LEVELnumber") == 3
                && ghost.getPersistentData().getString("soul_Att_LEVEL").equals("强劲") && ghost.getMaxHealth() == 131);
    }

    private void killGhost(ServerPlayer player) {
        ghost.setInvulnerable(false);
        ghost.invulnerableTime = 0;
        deadGhostId = ghostId;
        deathStart = player.serverLevel().getGameTime();
        boolean dying = ghost.hurt(ghost.damageSources().genericKill(), Float.MAX_VALUE) && ghost.isDeadOrDying();
        expectTrue("受控原生致死进入游魂死亡生命周期", dying);
        if (!dying) abort(player, "游魂原生致死前置失败，停止依赖死亡步骤");
    }

    private boolean deathAndBreak(int local) {
        boolean[] ready = {false};
        onServer(player -> {
            if (player.serverLevel().getGameTime() - deathStart < 20) return;
            ready[0] = true;
            Entity dead = player.serverLevel().getEntity(deadGhostId);
            expectTrue("来源零刻条件保持死亡二十个自然服务端刻后未移除",
                    dead instanceof Guiguai01Entity dying && dying.isDeadOrDying() && dying.deathTime >= 20);
            player.serverLevel().destroyBlock(origin, true, player);
            expectTrue("棺材原生破坏移除方块实体并掉落自身", player.serverLevel().getBlockEntity(origin) == null
                    && player.serverLevel().getEntitiesOfClass(ItemEntity.class, area()).stream()
                            .anyMatch(drop -> drop.getItem().is(item("guancai_01").getItem())));
        });
        if (!ready[0] && local > 200) onServer(player -> abort(player, "游魂死亡检查未推进二十个自然服务端刻"));
        return ready[0];
    }

    private void abort(ServerPlayer player, String message) {
        fail(message);
        cleanup(player);
        finish();
    }

    private void cleanup(ServerPlayer player) {
        unregisterDamageObserver();
        if (origin != null) {
            player.serverLevel().getEntitiesOfClass(Entity.class, area(), entity -> entity instanceof Zombie
                    || entity instanceof Guiguai01Entity || entity instanceof ItemEntity).forEach(Entity::discard);
            if (player.serverLevel().getBlockState(origin).getBlock() instanceof CoffinBlock) {
                player.serverLevel().destroyBlock(origin, false, player);
            }
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, originalHand);
        player.setInvulnerable(originalInvulnerable);
        player.setNoGravity(originalNoGravity);
        player.setGameMode(originalMode);
        player.server.setDifficulty(originalDifficulty, true);
        player.teleportTo(player.serverLevel(), originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.setDeltaMovement(originalMovement);
    }

    private CoffinBlockEntity coffin(ServerPlayer player) { return (CoffinBlockEntity) player.serverLevel().getBlockEntity(origin); }
    private List<Zombie> zombies(ServerPlayer player) { return player.serverLevel().getEntitiesOfClass(Zombie.class, area()); }
    private AABB area() { return new AABB(origin).inflate(10); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
    private static ItemStack item(String path) { return new ItemStack(BuiltInRegistries.ITEM.get(id(path))); }
    private static void useOn(ServerPlayer player, BlockPos pos) {
        player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }
}
