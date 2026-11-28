package net.yvanchuxiuzhen.smoke;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.npc.NpcData;
import net.yvanchuxiuzhen.system.npc.NpcSetup;
import net.yvanchuxiuzhen.system.npc.api.NpcDefinitions;
import net.yvanchuxiuzhen.system.npc.entity.NpcFlyingShuttle;

/** 显式生成08号后的真实右键乘坐、载人运动与阶段存档；不代表来源自然获取入口存在。 */
final class NpcFlyingShuttleCheck extends SystemCheck {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "businessman_08");
    private final Screenshots screenshots;
    private final Map<ChunkPos, Boolean> forced = new LinkedHashMap<>();
    private UUID shuttleId;
    private int networkId;
    private ListTag inventory;
    private int selected;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private GameType originalMode;
    private Difficulty originalDifficulty;
    private CameraType originalCamera;
    private Vec3 mountedPosition;
    private int mountedTick;

    NpcFlyingShuttleCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("08号飞梭", report, levelId);
        this.screenshots = screenshots;
    }

    @Override protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                originalCamera = minecraft.options.getCameraType();
                onServer(this::prepare);
                return true;
            }
            case 1 -> { return ready(local); }
            case 2 -> {
                Entity client = minecraft.level.getEntity(networkId);
                expect("来源中文身份", "黄飞", I18n.get("entity.yvanchuxiuzhen.businessman_08"));
                expectTrue("客户端已登记飞梭Gecko渲染器", minecraft.getEntityRenderDispatcher().getRenderer(client)
                        .getClass().getSimpleName().equals("AnimatedEntityRenderer"));
                minecraft.gameMode.interact(minecraft.player, client, InteractionHand.MAIN_HAND);
                return true;
            }
            case 3 -> {
                boolean[] mounted = new boolean[1];
                onServer(player -> mounted[0] = player.getVehicle() == shuttle(player)
                        && shuttle(player).isCutOff());
                if (!mounted[0] || minecraft.player.getVehicle() != minecraft.level.getEntity(networkId)) {
                    if (local > 200) abort("真实右键后乘坐状态未同步到双端");
                    return false;
                }
                onServer(player -> {
                    NpcFlyingShuttle shuttle = shuttle(player);
                    expectTrue("首次真实乘坐移除初生缓降", !shuttle.hasEffect(MobEffects.SLOW_FALLING));
                    expectTrue("NPC标签触发既有全局右键标记且不生成商店脚本", NpcDefinitions.isInteractiveNpc(shuttle)
                            && shuttle.getData(NpcData.PROFILE).script(0).isEmpty()
                            && shuttle.getData(NpcData.PROFILE).products().isEmpty());
                    shuttle.setYRot(90);
                    shuttle.baseTick();
                    expect("载人速度保持世界坐标方向，不随朝向旋转", new Vec3(0, .1, -.1), shuttle.getDeltaMovement());
                    mountedPosition = shuttle.position();
                    mountedTick = player.server.getTickCount();
                });
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                return true;
            }
            case 4 -> {
                boolean[] elapsed = new boolean[1];
                onServer(player -> elapsed[0] = player.server.getTickCount() - mountedTick >= 20);
                if (!elapsed[0]) {
                    if (local > 400) abort("载人运动期间服务端刻未推进");
                    return false;
                }
                onServer(player -> {
                    NpcFlyingShuttle shuttle = shuttle(player);
                    expectTrue("原生完整刻推进后载人升高并向世界北方移动", shuttle.getY() > mountedPosition.y
                            && shuttle.getZ() < mountedPosition.z && Math.abs(shuttle.getX() - mountedPosition.x) < .01);
                    expectTrue("载人时无重力", shuttle.isNoGravity());
                });
                screenshots.take("npc08_flying_shuttle.png");
                return true;
            }
            case 5 -> {
                onServer(player -> {
                    NpcFlyingShuttle shuttle = shuttle(player);
                    player.stopRiding();
                    shuttle.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, false));
                    shuttle.mobInteract(player, InteractionHand.MAIN_HAND);
                    shuttle.baseTick();
                    expectTrue("首次阶段已完成后再次乘坐不再移除缓降", player.getVehicle() == shuttle
                            && shuttle.hasEffect(MobEffects.SLOW_FALLING));
                    player.stopRiding();
                    player.teleportTo(player.serverLevel(), shuttle.getX() + 3, shuttle.getY() + 1, shuttle.getZ(), 90, 0);
                    CompoundTag saved = shuttle.saveWithoutId(new CompoundTag());
                    expectTrue("阶段随实体持久数据保存", saved.getBoolean("cut_off"));
                    expectTrue("来源纹理同步值不另写自定义存档字段", !saved.contains("Texture"));
                    deathBoundary(player);
                });
                return true;
            }
            case 6 -> { leaveWorld(); return true; }
            case 7 -> { return reopened(local); }
            case 8 -> { return inWorld(local); }
            case 9 -> { return ready(local); }
            case 10 -> {
                onServer(player -> {
                    NpcFlyingShuttle shuttle = shuttle(player);
                    expectTrue("正常存档重进保持原08身份与阶段", BuiltInRegistries.ENTITY_TYPE.getKey(shuttle.getType()).equals(ID)
                            && shuttle.isCutOff());
                    expectTrue("正常重进不执行初生过程覆盖已存效果", shuttle.hasEffect(MobEffects.SLOW_FALLING));
                    shuttle.mobInteract(player, InteractionHand.MAIN_HAND);
                    shuttle.baseTick();
                    expectTrue("重进后再次乘坐仍保持阶段门控", player.getVehicle() == shuttle && shuttle.hasEffect(MobEffects.SLOW_FALLING));
                });
                onServer(this::cleanup);
                finish();
                return true;
            }
            default -> throw new IllegalStateException("未知08号飞梭检查步骤 " + step);
        }
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalDifficulty = player.serverLevel().getDifficulty();
        inventory = player.getInventory().save(new ListTag());
        selected = player.getInventory().selected;
        player.server.setDifficulty(Difficulty.NORMAL, true);
        player.setGameMode(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
        BlockPos pos = player.blockPosition().above(20);
        ChunkPos center = new ChunkPos(pos);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                ChunkPos chunk = new ChunkPos(center.x + x, center.z + z);
                forced.put(chunk, player.serverLevel().getForcedChunks().contains(chunk.toLong()));
                player.serverLevel().setChunkForced(chunk.x, chunk.z, true);
                player.serverLevel().getChunk(chunk.x, chunk.z);
            }
        }
        EntityType<NpcFlyingShuttle> type = NpcSetup.<NpcFlyingShuttle>entityType("businessman_08").get();
        NpcFlyingShuttle shuttle = type.spawn(player.serverLevel(), pos, MobSpawnType.COMMAND);
        if (shuttle == null) throw new IllegalStateException("08号显式生成失败");
        shuttleId = shuttle.getUUID();
        networkId = shuttle.getId();
        player.teleportTo(player.serverLevel(), shuttle.getX() + 2, shuttle.getY(), shuttle.getZ(), 90, 0);
        expect("注册分类保持MONSTER", "MONSTER", type.getCategory().name());
        expect("来源碰撞宽度", 3, shuttle.getBbWidth());
        expect("来源碰撞高度", 1.5, shuttle.getBbHeight());
        expect("来源最大生命", 100, shuttle.getMaxHealth());
        expect("来源攻击", 3, shuttle.getAttributeValue(Attributes.ATTACK_DAMAGE));
        expect("来源飞行速度", .3, shuttle.getAttributeValue(Attributes.FLYING_SPEED));
        expectTrue("来源飞行导航与移动控制", shuttle.getNavigation() instanceof FlyingPathNavigation
                && shuttle.getMoveControl() instanceof FlyingMoveControl);
        expectTrue("来源初生过程赋予1200刻无粒子缓降", shuttle.hasEffect(MobEffects.SLOW_FALLING)
                && shuttle.getEffect(MobEffects.SLOW_FALLING).getDuration() == 1200
                && !shuttle.getEffect(MobEffects.SLOW_FALLING).isVisible());
        expectTrue("不使用商人等级初始化", EntityAttributes.realmName(shuttle).isEmpty()
                && EntityAttributes.level(shuttle) == 0);
        expectTrue("来源NPC标签身份", type.is(TagKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "npcentity"))));
        expectTrue("来源没有刷怪蛋物品", !BuiltInRegistries.ITEM.containsKey(
                ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "businessman_08_spawn_egg")));
        expectTrue("来源直接玩家伤害免疫", !shuttle.hurt(player.damageSources().playerAttack(player), 5));
        expectTrue("来源完整摔落入口免疫", !shuttle.causeFallDamage(20, 1, player.damageSources().fall()));
        shuttle.setNoGravity(false);
        expectTrue("禁止恢复重力", shuttle.isNoGravity());
    }

    private boolean ready(int local) {
        boolean[] serverReady = new boolean[1];
        onServer(player -> {
            Entity entity = player.serverLevel().getEntity(shuttleId);
            serverReady[0] = entity instanceof NpcFlyingShuttle && entity.tickCount > 0;
            if (serverReady[0]) networkId = entity.getId();
        });
        if (serverReady[0] && minecraft.level.getEntity(networkId) instanceof NpcFlyingShuttle) return true;
        if (local > 300) abort("08号实体未进入双端已加载集合");
        return false;
    }

    private NpcFlyingShuttle shuttle(ServerPlayer player) {
        return (NpcFlyingShuttle) player.serverLevel().getEntity(shuttleId);
    }

    private void deathBoundary(ServerPlayer player) {
        NpcFlyingShuttle dying = NpcSetup.<NpcFlyingShuttle>entityType("businessman_08").get().create(player.serverLevel());
        dying.setHealth(0);
        for (int tick = 0; tick < 19; tick++) dying.baseTick();
        expectTrue("来源死亡十九刻仍未移除", !dying.isRemoved());
        dying.baseTick();
        expectTrue("来源死亡二十刻移除", dying.isRemoved());
    }

    private void abort(String reason) {
        fail(reason);
        onServer(this::cleanup);
        finish();
    }

    private void cleanup(ServerPlayer player) {
        player.stopRiding();
        Entity shuttle = player.serverLevel().getEntity(shuttleId);
        if (shuttle != null) shuttle.discard();
        player.getInventory().load(inventory);
        player.getInventory().selected = selected;
        player.inventoryMenu.broadcastChanges();
        player.server.setDifficulty(originalDifficulty, true);
        player.setGameMode(originalMode);
        player.teleportTo(player.serverLevel(), originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        forced.forEach((chunk, wasForced) -> {
            if (!wasForced) player.serverLevel().setChunkForced(chunk.x, chunk.z, false);
        });
        minecraft.options.setCameraType(originalCamera);
    }
}
