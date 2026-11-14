package net.yvanchuxiuzhen.smoke;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.npc.NpcSetup;
import net.yvanchuxiuzhen.system.npc.entity.NpcUnderworldGuard;
import net.yvanchuxiuzhen.system.npc.entity.NpcUnderworldJudge;

/** 阴司五实体的伤害、亡灵标签、生成、存档和死亡边界，以及独立的客户端模型截图。 */
final class NpcUnderworldCheck extends SystemCheck {
    private static final List<String> IDS = List.of("yanwang", "wuchangbai", "wuchanghei", "niutou", "mamian");
    private static final List<String> NAMES = List.of("阎王", "白无常", "黑无常", "牛头", "马面");
    private static final TagKey<EntityType<?>> NPC = TagKey.create(Registries.ENTITY_TYPE, id("npcentity"));
    private final Screenshots screenshots;
    private final Map<String, UUID> entities = new LinkedHashMap<>();
    private final Map<String, Integer> networkIds = new LinkedHashMap<>();
    private final Map<ChunkPos, Boolean> forced = new LinkedHashMap<>();
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private Difficulty originalDifficulty;
    private GameType originalMode;
    private CameraType originalCamera;

    NpcUnderworldCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("阴司五实体", report, levelId);
        this.screenshots = screenshots;
    }

    @Override protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                originalCamera = minecraft.options.getCameraType();
                minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                onServer(this::prepare);
                return true;
            }
            case 1 -> {
                boolean serverReady = true;
                for (String kind : IDS) {
                    boolean[] ready = new boolean[1];
                    onServer(player -> {
                        Entity entity = player.serverLevel().getEntity(entities.get(kind));
                        ready[0] = entity != null && entity.tickCount > 0;
                        if (ready[0]) networkIds.put(kind, entity.getId());
                    });
                    serverReady &= ready[0];
                }
                if (serverReady && networkIds.values().stream().allMatch(value -> minecraft.level.getEntity(value) != null)) return true;
                if (local > 300) abort("五实体未全部进入双端已加载集合");
                return false;
            }
            case 2 -> {
                if (local < 20) return false;
                for (int index = 0; index < IDS.size(); index++) {
                    String kind = IDS.get(index);
                    Entity entity = minecraft.level.getEntity(networkIds.get(kind));
                    expect(kind + " 来源中文名称", NAMES.get(index), I18n.get("entity.yvanchuxiuzhen." + kind));
                    expect(kind + " 来源刷怪蛋中文名称", NAMES.get(index), I18n.get("item.yvanchuxiuzhen." + kind + "_spawn_egg"));
                    expectTrue(kind + " 客户端使用 Gecko 渲染器", minecraft.getEntityRenderDispatcher().getRenderer(entity)
                            .getClass().getSimpleName().equals("AnimatedEntityRenderer"));
                }
                return true;
            }
            case 3 -> {
                screenshots.take("npc_underworld_five.png");
                onServer(this::cleanup);
                finish();
                return true;
            }
            default -> { return false; }
        }
    }

    private void prepare(ServerPlayer player) {
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalDifficulty = player.server.getWorldData().getDifficulty();
        originalMode = player.gameMode.getGameModeForPlayer();
        player.server.setDifficulty(Difficulty.NORMAL, true);
        player.setGameMode(GameType.SPECTATOR);
        double centerX = Math.floor(player.getX());
        double centerZ = Math.floor(player.getZ());
        for (int index = 0; index < IDS.size(); index++) {
            String kind = IDS.get(index);
            checkEntity(player, kind);
            Mob entity = fresh(player, kind);
            entity.moveTo(centerX - 14 + index * 7, 180, centerZ, 0, 0);
            entity.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWN_EGG, null);
            entity.setNoAi(true);
            entity.setNoGravity(true);
            ChunkPos chunk = new ChunkPos(entity.blockPosition());
            forced.putIfAbsent(chunk, player.serverLevel().getForcedChunks().contains(chunk.toLong()));
            player.serverLevel().setChunkForced(chunk.x, chunk.z, true);
            player.serverLevel().addFreshEntity(entity);
            entities.put(kind, entity.getUUID());
        }
        player.server.setDifficulty(Difficulty.PEACEFUL, true);
        for (String kind : IDS) {
            Mob entity = fresh(player, kind);
            entity.checkDespawn();
            expectTrue(kind + " 和平难度遵循来源基类", entity.isRemoved() != kind.equals("yanwang"));
        }
        player.server.setDifficulty(Difficulty.NORMAL, true);
        player.teleportTo(player.serverLevel(), centerX, 187, centerZ + 32, 180, 7);
    }

    private void checkEntity(ServerPlayer player, String kind) {
        Mob entity = fresh(player, kind);
        boolean undead = kind.equals("niutou") || kind.equals("mamian");
        expectTrue(kind + " 默认启用 AI 且持久存在", !entity.isNoAi() && entity.isPersistenceRequired() && !entity.removeWhenFarAway(10000));
        expectTrue(kind + " 来源并无火焰免疫或内置对话标签", !entity.getType().fireImmune() && !entity.getType().is(NPC)
                && !entity.getPersistentData().getBoolean("renwuzuopen"));
        expectTrue(kind + " 亡灵总标签", entity.getType().is(EntityTypeTags.UNDEAD) == undead);
        expectTrue(kind + " 治疗伤害反转", entity.isInvertedHealAndHarm() == undead);
        expectTrue(kind + " 毒与再生接收条件", entity.canBeAffected(new MobEffectInstance(MobEffects.POISON, 20)) != undead
                && entity.canBeAffected(new MobEffectInstance(MobEffects.REGENERATION, 20)) != undead);
        expectTrue(kind + " 亡灵杀手判定", entity.getType().is(EntityTypeTags.SENSITIVE_TO_SMITE) == undead);
        expectTrue(kind + " 亡灵水下呼吸", entity.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER) == undead);
        expectTrue(kind + " 凋灵友方判定", entity.getType().is(EntityTypeTags.WITHER_FRIENDS) == undead);
        expect(kind + " 来源生命上限", undead ? 10 : 100, entity.getMaxHealth());
        expectTrue(kind + " 接收仙人掌伤害", entity.hurt(player.damageSources().cactus(), 1));
        expectTrue(kind + " 刷怪蛋指向来源实体", BuiltInRegistries.ITEM.get(id(kind + "_spawn_egg")) instanceof SpawnEggItem egg
                && egg.getType(new ItemStack(egg)).equals(entity.getType()));
        if (kind.equals("yanwang")) {
            entity.moveTo(12, 180, 15, 113, 31);
            entity.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWN_EGG, null);
            expect("阎王初生横向偏移半格", new Vec3(12.5, 180, 15), entity.position());
            expectTrue("阎王初生当前和上次朝向清零", entity.getYRot() == 0 && entity.getXRot() == 0 && entity.yRotO == 0
                    && entity.xRotO == 0 && entity.yBodyRot == 0 && entity.yBodyRotO == 0 && entity.yHeadRot == 0 && entity.yHeadRotO == 0);
        }
        entity.getPersistentData().putBoolean("renwuzuopen", true);
        entity.getPersistentData().putString("renwulist1", "阴司实例脚本存档检查");
        setTexture(entity, "temporary_underworld_texture");
        CompoundTag saved = entity.saveWithoutId(new CompoundTag());
        Mob restored = fresh(player, kind);
        restored.load(saved);
        expectTrue(kind + " 保留实例脚本字段", restored.getPersistentData().getBoolean("renwuzuopen")
                && restored.getPersistentData().getString("renwulist1").equals("阴司实例脚本存档检查"));
        expectTrue(kind + " 来源纹理并不单独持久化", !saved.contains("Texture") && !texture(restored).equals("temporary_underworld_texture"));
        int deathTicks = undead ? 120 : 20;
        Mob dying = fresh(player, kind);
        dying.setHealth(0);
        for (int tick = 1; tick < deathTicks; tick++) dying.baseTick();
        expectTrue(kind + " 死亡期限前仍保留实体", !dying.isRemoved());
        dying.baseTick();
        expectTrue(kind + " 来源死亡期限移除实体", dying.isRemoved());
    }

    private static Mob fresh(ServerPlayer player, String kind) { return NpcSetup.<Mob>entityType(kind).get().create(player.serverLevel()); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }

    private static void setTexture(Mob entity, String texture) {
        if (entity instanceof NpcUnderworldJudge judge) judge.setTexture(texture);
        else ((NpcUnderworldGuard) entity).setTexture(texture);
    }

    private static String texture(Mob entity) {
        return entity instanceof NpcUnderworldJudge judge ? judge.texture() : ((NpcUnderworldGuard) entity).texture();
    }

    private void abort(String reason) {
        fail(reason);
        onServer(this::cleanup);
        finish();
    }

    private void cleanup(ServerPlayer player) {
        entities.values().forEach(uuid -> {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
        });
        player.server.setDifficulty(originalDifficulty, true);
        player.setGameMode(originalMode);
        player.teleportTo(player.serverLevel(), originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        forced.forEach((chunk, wasForced) -> {
            if (!wasForced) player.serverLevel().setChunkForced(chunk.x, chunk.z, false);
        });
        minecraft.options.setCameraType(originalCamera);
    }
}
