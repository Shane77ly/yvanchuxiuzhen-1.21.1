package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.guchong.api.GuchongRuntimeState;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanBlockEntity;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanMenu;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong03Entity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 核对同一只飞蝗经过实际菜单、精血认主和地上罐计划刻。 */
final class RenjianWildJarWitness {
    private static final ResourceLocation FLY = id("guchong_03");
    private static final ResourceLocation SMALL_JAR = id("taoguan_01item");
    private static final ResourceLocation BLOOD = id("jingxve");
    private final Consumer<PlayerInteractEvent.RightClickBlock> clicking = this::onClick;
    private final Consumer<EntityJoinLevelEvent> joining = this::onJoin;
    private final List<String> report;
    private final RenjianLocustCombatWitness combat;
    private ServerPlayer observer;
    private ItemStack capturedStack;
    private CompoundTag insect;
    private UUID wildUuid;
    private ItemStack handJar = ItemStack.EMPTY;
    private String jarId = "";
    private BlockPos placement;
    private TaoguanBlockEntity groundJar;
    private ItemStack blood = ItemStack.EMPTY;
    private int bloodCount;
    private Guchong03Entity released;
    private UUID targetUuid;
    private UUID controlledTarget;
    private ItemStack savedSlot = ItemStack.EMPTY;
    private CompoundTag savedJar;
    private boolean releasedBeforeSave;
    private boolean reopened;
    private boolean fed;
    private boolean complete;

    RenjianWildJarWitness(List<String> report, RenjianLocustCombatWitness combat) {
        this.report = report;
        this.combat = combat;
    }

    void start() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, clicking);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, joining);
        if (combat != null) combat.start();
    }
    void stop() {
        NeoForge.EVENT_BUS.unregister(clicking);
        NeoForge.EVENT_BUS.unregister(joining);
        if (combat != null) combat.stop();
    }
    void capture(UUID uuid, ItemStack stack) {
        wildUuid = uuid;
        capturedStack = stack.copy();
        insect = GuchongJarData.read(stack);
    }
    boolean packed() { return !jarId.isEmpty(); }
    boolean menuVerified() { return !handJar.isEmpty(); }
    boolean placed() { return groundJar != null; }
    boolean fed() { return fed; }
    boolean complete() { return complete; }
    BlockPos position() { return placement; }
    String team() { return groundJar.jarData().getString("team"); }
    RenjianReplayPlayer.Target releasedTarget() { return new RenjianReplayPlayer.Target(released); }

    boolean releaseAreaEmpty() { return eligibleAnimals(observer.serverLevel(), groundJar).isEmpty(); }

    void expectControlledTarget(Animal target) {
        List<Animal> eligible = eligibleAnimals(observer.serverLevel(), groundJar);
        if (!reopened || active(groundJar) || eligible.size() != 1 || eligible.getFirst() != target
                || target.distanceToSqr(Vec3.atCenterOf(placement)) > 8 * 8) {
            throw new IllegalStateException("受控目标未成为重进后八格内唯一合法异队动物");
        }
        controlledTarget = target.getUUID();
    }

    boolean prepareSave() {
        ServerLevel level = observer.serverLevel();
        if (!fed || releasedBeforeSave || active(groundJar) || !eligibleAnimals(level, groundJar).isEmpty()
                || !level.getBlockTicks().hasScheduledTick(placement, groundJar.getBlockState().getBlock())) return false;
        savedSlot = groundJar.getItem(0).copy();
        savedJar = groundJar.jarData().copy();
        report.add("INFO REPLAY_JAR_SAVE wild=" + wildUuid + " jar=" + jarId + " slot=0 pos=" + placement
                + " owner=" + observer.getUUID() + " active=0 components=" + GuchongJarData.read(savedSlot));
        return true;
    }

    boolean rejoined(ServerPlayer player) {
        if (releasedBeforeSave || !player.serverLevel().hasChunkAt(placement)
                || !(player.serverLevel().getBlockEntity(placement) instanceof TaoguanBlockEntity restored)
                || restored == groundJar || restored.grade() != 1 || active(restored)
                || !jarId.equals(restored.jarData().getString("guguanuuid")) || !savedJar.equals(restored.jarData())
                || !ItemStack.matches(savedSlot, restored.getItem(0)) || !eligibleAnimals(player.serverLevel(), restored).isEmpty()
                || !player.serverLevel().getBlockTicks().hasScheduledTick(placement, restored.getBlockState().getBlock())) return false;
        groundJar = restored;
        observer = player;
        reopened = true;
        report.add("INFO REPLAY_JAR_REOPEN wild=" + wildUuid + " jar=" + jarId + " slot=0 pos=" + placement
                + " blockEntityRecreated=true componentsMatch=true scheduledTick=true active=0");
        return true;
    }

    String stage() {
        if (reopened) return "已同档重进，未见匹配的原生活动虫";
        if (fed) return "已认主，尚未重进";
        if (groundJar != null) return "已落地，未见有效精血认主";
        if (!jarId.isEmpty()) return "已关闭并保存小蛊罐，未落地";
        return insect == null ? "待捕获" : "已捕获，未保存到小蛊罐";
    }

    void observe(ServerPlayer player, int inventoryFlies) {
        observer = player;
        if (insect == null) return;
        ItemStack held = player.getMainHandItem();
        ServerLevel level = player.serverLevel();
        if (jarId.isEmpty()) {
            if (player.containerMenu instanceof TaoguanMenu menu && menu.grade() == 1
                    && itemId(held).equals(SMALL_JAR) && ItemStack.matches(capturedStack, menu.getSlot(0).getItem())
                    && inventoryFlies == 0) {
                handJar = held;
            } else if (player.containerMenu == player.inventoryMenu && held == handJar && inventoryFlies == 0) {
                CompoundTag packed = GuchongJarData.read(held);
                if (matches(packed, "0") && !packed.getString("guguanuuid").isEmpty()) {
                    jarId = packed.getString("guguanuuid");
                    report.add("INFO REPLAY_JAR_MENU wild=" + wildUuid + " jar=" + jarId + " slot=0 capturedComponentsMatch=true inventory=1->0");
                }
            }
            return;
        }
        if (groundJar == null) {
            if (placement != null && level.getBlockEntity(placement) instanceof TaoguanBlockEntity candidate
                    && candidate.grade() == 1 && jarId.equals(candidate.jarData().getString("guguanuuid"))
                    && candidate.getItem(0).getCount() == 1 && matches(GuchongJarData.read(candidate.getItem(0)), "")
                    && player.getMainHandItem() != handJar) {
                groundJar = candidate;
                report.add("INFO 同一罐经顶面实放，捕获的飞蝗仍在槽0：" + placement);
            }
            return;
        }
        if (level.getBlockEntity(placement) != groundJar) return;
        CompoundTag slot = GuchongJarData.read(groundJar.getItem(0));
        if (!fed && bloodCount > 0 && blood.getCount() == bloodCount - 1
                && slot.getBoolean("guchongxvnfu") && player.getStringUUID().equals(slot.getString("belongToMasterUUID"))
                && matchesStats(slot, "") && slot.getDouble("health_Num") == insect.getDouble("health_Num")) {
            fed = true;
            report.add("INFO 真实精血右键扣1，原槽0飞蝗认当前玩家");
        }
        if (!complete && reopened && released != null && released.isAlive() && level.getEntity(released.getUUID()) == released
                && ItemStack.matches(savedSlot, groundJar.getItem(0)) && jarId.equals(groundJar.jarData().getString("guguanuuid"))
                && groundJar.jarData().getBoolean("shifangguchong") && groundJar.jarData().getInt("shifangnum") == 1) {
            complete = true;
            report.add("INFO REPLAY_JAR_RELEASE wild=" + wildUuid + " released=" + released.getUUID() + " jar=" + jarId
                    + " slot=0 pos=" + placement + " owner=" + observer.getUUID() + " target=" + targetUuid + " active=1");
        }
    }

    private void onClick(PlayerInteractEvent.RightClickBlock event) {
        if (observer == null || event.getEntity() != observer || event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND
                || !event.getLevel().dimension().location().equals(id("dimensionrenjian")) || jarId.isEmpty()) return;
        ItemStack stack = event.getItemStack();
        if (groundJar == null && stack == handJar && event.getHitVec().getDirection() == Direction.UP) {
            BlockPos pos = event.getPos().above();
            if (event.getLevel().isEmptyBlock(pos) && (event.getLevel().getBlockState(event.getPos()).is(Blocks.GRASS_BLOCK)
                    || event.getLevel().getBlockState(event.getPos()).is(Blocks.DIRT))) placement = pos;
        } else if (groundJar != null && !fed && event.getPos().equals(placement) && itemId(stack).equals(BLOOD)) {
            blood = stack;
            bloodCount = stack.getCount();
        }
    }

    private void onJoin(EntityJoinLevelEvent event) {
        if (groundJar == null || bloodCount == 0 || released != null || event.loadedFromDisk() || event.isCanceled()
                || !(event.getLevel() instanceof ServerLevel level) || !level.dimension().location().equals(id("dimensionrenjian"))
                || !(event.getEntity() instanceof Guchong03Entity fly)
                || !level.hasChunkAt(placement) || !(level.getBlockEntity(placement) instanceof TaoguanBlockEntity source)
                || !jarId.equals(source.jarData().getString("guguanuuid"))) return;
        GuchongRuntimeState runtime = fly.getData(GuchongData.RUNTIME);
        String target = runtime.commandTargetUuid();
        if (!fly.isAlive() || fly.getUUID().equals(wildUuid) || !runtime.guarding() || !runtime.tamed()
                || !observer.getUUID().equals(fly.getOwnerUUID()) || runtime.jarUuid().isEmpty() || runtime.jarSlot() != 0
                || !placement.equals(runtime.homePos())) return;
        if (!reopened) {
            releasedBeforeSave = true;
            return;
        }
        CompoundTag slot = GuchongJarData.read(savedSlot);
        EntityStats flyStats = EntityAttributes.get(fly);
        if (!matchesEntityStats(flyStats) || fly.getHealth() != slot.getInt("health_Num")) return;
        Animal animal = eligibleAnimals(level, source).stream().filter(candidate -> candidate.getStringUUID().equals(target))
                .findFirst().orElse(null);
        if (animal == null || (controlledTarget != null && !controlledTarget.equals(animal.getUUID()))) return;
        released = fly;
        targetUuid = animal.getUUID();
        if (combat != null) combat.track(level, released, animal, source, jarId, savedSlot);
        report.add("INFO REPLAY_JAR_RELEASE_JOIN released=" + fly.getUUID() + " target=" + targetUuid + " gameTime=" + level.getGameTime());
    }

    private static List<Animal> eligibleAnimals(ServerLevel level, TaoguanBlockEntity jar) {
        Vec3 center = Vec3.atCenterOf(jar.getBlockPos());
        String team = jar.jarData().getString("team");
        return level.getEntitiesOfClass(Animal.class, new AABB(center, center).inflate(8),
                animal -> animal.isAlive() && !Relations.entityTeam(animal).equals(team));
    }

    private static boolean active(TaoguanBlockEntity jar) {
        return jar.jarData().getBoolean("shifangguchong") || jar.jarData().getInt("shifangnum") != 0;
    }

    private boolean matches(CompoundTag data, String slot) {
        return matchesStats(data, slot) && !data.getBoolean("guchongxvnfu" + slot)
                && data.getDouble("health_Num" + slot) == insect.getDouble("health_Num");
    }

    private boolean matchesEntityStats(EntityStats stats) {
        return stats.level() == insect.getDouble("LEVELnumber")
                && stats.stat(Stat.ATTACK) == insect.getDouble("attack_Num")
                && stats.stat(Stat.MOVE_SPEED) == insect.getDouble("speed_number");
    }

    private boolean matchesStats(CompoundTag data, String slot) {
        return FLY.toString().equals(data.getString("guchongid" + slot))
                && data.getDouble("LEVELnumber" + slot) == insect.getDouble("LEVELnumber")
                && data.getDouble("attack_Num" + slot) == insect.getDouble("attack_Num")
                && data.getDouble("speed_number" + slot) == insect.getDouble("speed_number");
    }

    private static ResourceLocation itemId(ItemStack stack) { return BuiltInRegistries.ITEM.getKey(stack.getItem()); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }
}
