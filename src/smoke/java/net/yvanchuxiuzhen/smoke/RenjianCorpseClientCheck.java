package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou003Entity;

/** 在受控003身上验证客户端准星拾取、使用键、服务端交互及实际落物。 */
final class RenjianCorpseClientCheck extends SystemCheck {
    private static final ResourceLocation RENJIAN = id("dimensionrenjian");
    private static final Set<ResourceLocation> PRODUCTS = Set.of(id("cailiao_001"), id("cailiao_181"),
            ResourceLocation.withDefaultNamespace("mutton"), ResourceLocation.withDefaultNamespace("bone"));
    private final Consumer<PlayerInteractEvent.EntityInteract> interacting = this::onInteract;
    private final Consumer<EntityJoinLevelEvent> joining = this::onJoin;
    private ServerPlayer observer;
    private Yaoshou003Entity sheep;
    private int clicks;
    private int sentAt;
    private int accepted;
    private int drops;
    private boolean positioned;

    RenjianCorpseClientCheck(List<String> report, String levelId) {
        super("人间道003客户端尸体右键", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            if (local > 3600) {
                fail("等待人间道空旷位置及空主手超时");
                finish();
                return false;
            }
            if (minecraft.player != null && minecraft.level.dimension().location().equals(RENJIAN)
                    && !minecraft.player.getMainHandItem().isEmpty()) {
                var inventory = minecraft.player.getInventory();
                for (int slot = 0; slot < 9; slot++) {
                    if (inventory.getItem(slot).isEmpty()) {
                        // 首入赠礼保持在原槽，只通过原生热栏同步换到空手。
                        inventory.selected = slot;
                        break;
                    }
                }
            }
            onServer(this::prepare);
            return sheep != null;
        }
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) return false;
        int[] state = new int[3];
        onServer(player -> {
            state[0] = accepted;
            state[1] = drops;
            state[2] = (int) CreatureData.state(sheep).harvestCount();
        });
        int expected = clicks;
        if (state[0] > expected || state[1] > expected || state[2] > expected) {
            fail("一次使用键产生了多次交互或采集");
            stop();
        } else if (expected == 5 && state[0] == 5 && state[1] == 5 && state[2] == 5) {
            onServer(player -> expectTrue("五次客户端主手右键各落一份，第五次实际移除",
                    sheep.getRemovalReason() == Entity.RemovalReason.DISCARDED));
            stop();
        } else if (local > 160 || (clicks > 0 && local - sentAt > 25 && state[2] != expected)) {
            fail("客户端右键未闭合：发送=" + clicks + "，服务端交互=" + state[0] + "，落物=" + state[1] + "，采集=" + state[2]);
            stop();
        } else if (state[0] == expected && state[1] == expected && state[2] == expected
                && local - sentAt >= 8 && minecraft.screen == null && !minecraft.player.isShiftKeyDown()
                && minecraft.level.getEntity(sheep.getId()) instanceof Yaoshou003Entity clientSheep && !clientSheep.isAlive()) {
            minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, clientSheep.getBoundingBox().getCenter());
            minecraft.gameRenderer.pick(1.0F);
            if (minecraft.hitResult instanceof EntityHitResult hit && hit.getEntity() == clientSheep) {
                KeyMapping.click(minecraft.options.keyUse.getKey());
                clicks++;
                sentAt = local;
            }
        }
        return false;
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!level.dimension().location().equals(RENJIAN)) {
            ServerLevel renjian = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, RENJIAN));
            if (renjian == null) throw new IllegalStateException("缺少人间道");
            player.teleportTo(renjian, 8.5, 180, 8.5, 0, 0);
            return;
        }
        if (!positioned) {
            BlockPos center = new BlockPos(8, 179, 9);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, 0, -2), center.offset(2, 3, 3))) {
                level.setBlockAndUpdate(pos, pos.getY() == center.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
            player.setGameMode(GameType.SURVIVAL);
            player.connection.teleport(8.5, 180, 8.5, 0, 0);
            positioned = true;
            return;
        }
        if (player.isSpectator() || player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) return;
        Vec3 pos = player.position().add(0, 0, 2.4);
        Yaoshou003Entity candidate = CreatureSetup.<Yaoshou003Entity>entityType("yaoshou_003").get().create(level);
        if (candidate == null) throw new IllegalStateException("003 实体工厂未装配");
        candidate.moveTo(pos.x, pos.y, pos.z, 0, 0);
        if (!level.noCollision(candidate, candidate.getBoundingBox()) || !level.isEmptyBlock(candidate.blockPosition())) return;
        candidate.finalizeSpawn(level, level.getCurrentDifficultyAt(candidate.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        candidate.setNoAi(true);
        candidate.setNoGravity(true);
        if (!level.addFreshEntity(candidate)) {
            fail("受控003未入场");
            finish();
            return;
        }
        if (!candidate.hurt(candidate.damageSources().genericKill(), Float.MAX_VALUE) || candidate.isAlive() || candidate.isRemoved()) {
            candidate.discard();
            fail("003未原生死亡或未留下尸体");
            finish();
            return;
        }
        sheep = candidate;
        observer = player;
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, interacting);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, joining);
    }

    private void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() == observer && event.getTarget() == sheep && event.getHand() == InteractionHand.MAIN_HAND
                && !observer.isShiftKeyDown() && observer.getMainHandItem().isEmpty() && !sheep.isAlive()) accepted++;
    }

    private void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() != sheep.level() || event.loadedFromDisk() || event.isCanceled()
                || !(event.getEntity() instanceof ItemEntity item) || accepted != drops + 1) return;
        Vec3 pos = new Vec3(sheep.getX(), sheep.blockPosition().getY() + 1, sheep.getZ());
        if (item.position().distanceToSqr(pos) < 1e-6 && PRODUCTS.contains(BuiltInRegistries.ITEM.getKey(item.getItem().getItem()))
                && item.saveWithoutId(new CompoundTag()).getInt("PickupDelay") == 10) drops++;
    }

    private void stop() {
        onServer(player -> {
            NeoForge.EVENT_BUS.unregister(interacting);
            NeoForge.EVENT_BUS.unregister(joining);
            sheep.discard();
        });
        finish();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path);
    }
}
