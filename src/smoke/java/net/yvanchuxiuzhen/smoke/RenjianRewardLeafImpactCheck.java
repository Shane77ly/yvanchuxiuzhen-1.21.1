package net.yvanchuxiuzhen.smoke;

import com.google.common.hash.Hashing;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.particle.ParticleRow;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 从施放后归档重播一次原生使用，观察实际碰撞与客户端绿爆。 */
final class RenjianRewardLeafImpactCheck extends SystemCheck {
    static final String SELECTOR = "renjianrewardleafimpact";
    static final String SAVE_ID = "renjian-reward-leaf-impact-replay";
    private static final UUID PLAYER = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");
    private static final String LEVEL_HASH = "7676c01345c5e9129e88be352fa78396b6afc31d96660d93e711aefd1f322953";
    private static final String PLAYER_HASH = "021acd6fab3e4b079e478b6a532245e5ea1a2b8c97883ec9561ff2c8833b628f";
    private static final Vec3 ORIGIN = new Vec3(8.5, 180, 8.5);
    private static final Vec3 TARGET = ORIGIN.add(0, 0, 6);
    private static final ResourceLocation LEAF = id("projectile_skill_tree_01");
    private static final ResourceLocation ICON = id("cailiao_087");
    private static final ResourceKey<DamageType> SPELL = ResourceKey.create(Registries.DAMAGE_TYPE, id("linglidamage_player"));
    private static final String BURST = "smallexplosiongreen";
    private static final String SHOT = "renjian_reward_leaf_impact.png";

    private record Spawn(UUID uuid, int tick, Vec3 position, Vec3 velocity) {}

    private record Impact(int tick, int flightTicks, Vec3 position, boolean basePresent, double base,
                          boolean multiplierPresent, double multiplier) {
        private static Impact read(SkillProjectile leaf, int tick) {
            CompoundTag data = leaf.getPersistentData();
            return new Impact(tick, leaf.ticks(), leaf.position(), data.contains("AttackDamage"), data.getDouble("AttackDamage"),
                    data.contains("Damage_multiplier"), data.getDouble("Damage_multiplier"));
        }
    }

    private final Consumer<String> shot;
    private final Consumer<PlayerInteractEvent.RightClickItem> use = this::observeUse;
    private final Consumer<EntityJoinLevelEvent> join = this::observeJoin;
    private final Consumer<ProjectileImpactEvent> impact = this::observeImpact;
    private final Consumer<LivingIncomingDamageEvent> damage = this::observeDamage;
    private final Consumer<RenderFrameEvent.Post> frame = event -> captureImpact();
    private final List<BlockPos> temporaryFloor = new ArrayList<>();
    private final List<Float> arrowDamage = new ArrayList<>();
    private ServerPlayer shooter;
    private IronGolem target;
    private ItemStack originalBook;
    private SkillProjectile leaf;
    private boolean observing;
    private int useTick = -1;
    private int uses;
    private int ownProjectiles;
    private int nativeImpacts;
    private int spellDamageEvents;
    private float startingHealth;
    private volatile Spawn spawn;
    private volatile Impact hit;
    private int trackedLeaves;
    private ResourceLocation trackedIcon;
    private String clientEvidence;
    private String screenshotError;
    private Field particleRow;

    RenjianRewardLeafImpactCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("人间奖励拈花飞叶 Q40", report, levelId);
        this.shot = shot;
    }

    static boolean matchesArchive(Path save) throws IOException {
        return hashMatches(save.resolve("level.dat"), LEVEL_HASH)
                && hashMatches(save.resolve("playerdata").resolve(PLAYER + ".dat"), PLAYER_HASH);
    }

    private static boolean hashMatches(Path file, String expected) throws IOException {
        return Files.isRegularFile(file) && Hashing.sha256().hashBytes(Files.readAllBytes(file)).toString().equals(expected);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                if (minecraft.screen != null || minecraft.options.hideGui || minecraft.options.particles().get() != ParticleStatus.ALL
                        || minecraft.options.getCameraType() != CameraType.FIRST_PERSON || !archivePlayer(minecraft.player)) {
                    stop("需第一人称、全粒子、HUD 可见且奖励原书已在本人槽 1 的人间道归档");
                    return false;
                }
                originalBook = Skills.selected(minecraft.player).copy();
                String[] error = new String[1];
                onServer(player -> error[0] = prepare(player));
                if (error[0] != null) {
                    stop(error[0]);
                    return false;
                }
                NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, use);
                NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, join);
                NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, impact);
                NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, damage);
                NeoForge.EVENT_BUS.addListener(frame);
                observing = true;
            }
            case 1 -> {
                boolean[] ready = new boolean[1];
                onServer(player -> ready[0] = readyToCast(player) && target.isAlive() && !Relations.isFriendly(player, target));
                if (!ready[0] || !readyToCast(minecraft.player) || minecraft.screen != null
                        || !(minecraft.level.getEntity(target.getId()) instanceof IronGolem clientTarget)
                        || !clientTarget.getUUID().equals(target.getUUID())) {
                    if (local > 160) stop("等待原书、视角、目标追踪及自然冷却与灵力就绪超时；未发送使用包");
                    return false;
                }
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            }
            case 2 -> {
                if (local < 20 || (clientEvidence == null && screenshotError == null && local < 100)) return false;
                onServer(this::verifyServer);
                expectTrue("客户端收到同 UUID 叶弹及显示物品；数量=" + trackedLeaves + "，物品=" + trackedIcon,
                        trackedLeaves == 1 && ICON.equals(trackedIcon));
                expectTrue("命中点绿爆进入客户端粒子批次并截取绘制帧；" + clientEvidence
                        + (screenshotError == null ? "" : "；采证错误=" + screenshotError), clientEvidence != null);
                stop(null);
            }
            default -> throw new IllegalStateException("未知阶段 " + step);
        }
        return true;
    }

    private String prepare(ServerPlayer player) {
        if (!archivePlayer(player) || !ItemStack.matches(originalBook, Skills.selected(player))
                || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return "服务端与客户端归档签名不一致，或不是生存模式";
        }
        var level = player.serverLevel();
        BlockPos start = new BlockPos(7, 179, 12);
        BlockPos end = new BlockPos(9, 179, 15);
        AABB corridor = new AABB(7, 180, 9, 10, 183, 16);
        if (BlockPos.betweenClosedStream(start, end).anyMatch(pos -> !level.isEmptyBlock(pos))
                || !level.noCollision(player, corridor) || !level.getEntities(player, corridor).isEmpty()) {
            return "射线走廊或目标垫石位置已被占用，未调整场地";
        }
        shooter = player;
        for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
            if (!level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL)) return "布置目标垫石失败";
            temporaryFloor.add(pos.immutable());
        }
        target = EntityType.IRON_GOLEM.create(level);
        target.moveTo(TARGET.x, TARGET.y, TARGET.z, 180, 0);
        target.setNoAi(true);
        target.setPersistenceRequired();
        startingHealth = target.getHealth();
        if (!level.addFreshEntity(target) || Relations.isFriendly(player, target)) return "未加入受控非友方目标；未发送使用包";
        player.connection.teleport(ORIGIN.x, ORIGIN.y, ORIGIN.z, 0, 0);
        expectTrue("人间道生存、落魄家族、练气一层、槽 1 已选本人研习的原书且缺少 ManaAttackDamage；目标="
                + target.getUUID() + " @ " + TARGET, true);
        return null;
    }

    private static boolean archivePlayer(Player player) {
        ItemStack selected = Skills.selected(player);
        var state = Skills.state(player);
        return PLAYER.equals(player.getUUID()) && player.isAlive() && !player.isPassenger()
                && player.level().dimension().equals(DimensionPortalSpec.RENJIAN.dimension())
                && player.position().distanceToSqr(ORIGIN) < 0.01 && player.getMainHandItem().is(Items.WOODEN_SWORD)
                && Cultivation.get(player, CultivationText.BIRTH).equals("落魄家族")
                && Cultivation.get(player, CultivationValue.LEVEL_NUMBER) == 1 && Cultivation.get(player, CultivationValue.LIFETIME) == 32800
                && state.page() == 1 && state.selectedSlot() == 1 && !state.combo() && ItemStack.matches(selected, Skills.spell(player, 1))
                && BuiltInRegistries.ITEM.getKey(selected.getItem()).equals(id("book_012")) && selected.getCount() == 1
                && Skills.book(selected).filter(book -> book.kind() == SkillKind.SPELL && book.ownedBy(PLAYER)
                        && book.name().equals("拈花飞叶") && book.body().equals("木") && book.damageMultiplier() == 1
                        && !book.numbers().containsKey("ManaAttackDamage")).isPresent();
    }

    private boolean readyToCast(Player player) {
        return archivePlayer(player) && ItemStack.matches(Skills.selected(player), originalBook)
                && player.getLookAngle().z > 0.9999
                && Skills.cooldown(player, SkillElement.TREE) == 0 && Skills.cooldown(player, SkillElement.FIRE) == 0
                && Cultivation.get(player, CultivationValue.POWER) >= 1;
    }

    private void observeUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() == shooter && event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().is(Items.WOODEN_SWORD)) {
            uses++;
            useTick = shooter.getServer().getTickCount();
        }
    }

    private void observeJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof SkillProjectile projectile)) return;
        if (event.getLevel().isClientSide()) {
            Spawn observed = spawn;
            if (observed != null && projectile.getUUID().equals(observed.uuid()) && EntityType.getKey(projectile.getType()).equals(LEAF)) {
                trackedLeaves++;
                trackedIcon = BuiltInRegistries.ITEM.getKey(projectile.getItem().getItem());
            }
        } else if (event.getLevel() == shooter.level() && projectile.getOwner() == shooter) {
            ownProjectiles++;
            if (leaf == null && EntityType.getKey(projectile.getType()).equals(LEAF)) {
                leaf = projectile;
                spawn = new Spawn(leaf.getUUID(), shooter.getServer().getTickCount(), leaf.position(), leaf.getDeltaMovement());
            }
        }
    }

    private void observeImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() != leaf) return;
        nativeImpacts++;
        if (!event.isCanceled() && hit == null && event.getRayTraceResult() instanceof EntityHitResult entityHit
                && entityHit.getEntity() == target) {
            hit = Impact.read(leaf, shooter.getServer().getTickCount());
        }
    }

    private void observeDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() != target || event.getSource().getEntity() != shooter) return;
        if (event.getSource().is(DamageTypes.ARROW) && event.getSource().getDirectEntity() == leaf) {
            arrowDamage.add(event.getOriginalAmount());
        } else if (event.getSource().is(SPELL)) {
            spellDamageEvents++;
        }
    }

    private void verifyServer(ServerPlayer player) {
        expectTrue("一次原生木剑右键同刻只生成一枚本人叶弹，原选书未改；使用=" + uses + " @ " + useTick
                        + "，施放=" + spawn + "，本人弹体数=" + ownProjectiles,
                uses == 1 && ownProjectiles == 1 && spawn != null && spawn.tick() == useTick
                        && ItemStack.matches(originalBook, Skills.selected(player)) && ItemStack.matches(originalBook, Skills.spell(player, 1)));
        expectTrue("唯一一次原生碰撞为受控目标，发生在第 10 个技能刻写值前；碰撞数=" + nativeImpacts
                        + "，记录=" + hit + "，弹体现态=" + (leaf == null ? "未生成" : leaf.position() + " / " + leaf.getRemovalReason()),
                nativeImpacts == 1 && hit != null && spawn != null && hit.tick() > spawn.tick()
                        && hit.flightTicks() > 0 && hit.flightTicks() < 10 && !hit.basePresent() && hit.base() == 0
                        && !hit.multiplierPresent() && hit.multiplier() == 0);
        expectTrue("未进入该施放者的灵力受伤事件；灵力事件=" + spellDamageEvents + "，原生箭伤请求=" + arrowDamage
                        + "，目标生命=" + startingHealth + " → " + target.getHealth(), hit != null && spellDamageEvents == 0);
    }

    /** 绿爆位于命中回调当时的弹体坐标，不使用目标中心。 */
    private void captureImpact() {
        Impact observed = hit;
        if (observed == null || clientEvidence != null || screenshotError != null || minecraft.level == null
                || minecraft.screen != null || minecraft.getOverlay() != null || minecraft.options.hideGui) return;
        minecraft.particleEngine.iterateParticles(particle -> {
            if (clientEvidence != null || screenshotError != null || !particle.isAlive()
                    || particle.getPos().distanceToSqr(observed.position()) > 1e-6
                    || !particle.getClass().getName().equals("net.yvanchuxiuzhen.system.particle.client.LibraryParticle")) return;
            try {
                if (particleRow == null) {
                    particleRow = particle.getClass().getDeclaredField("row");
                    particleRow.setAccessible(true);
                }
                if (!BURST.equals(((ParticleRow) particleRow.get(particle)).id())) return;
                shot.accept(SHOT);
                clientEvidence = BURST + " @ " + particle.getPos() + " / " + SHOT;
            } catch (ReflectiveOperationException | RuntimeException exception) {
                screenshotError = exception.toString();
            }
        });
    }

    private void stop(String reason) {
        if (reason != null) fail(reason);
        if (observing) {
            NeoForge.EVENT_BUS.unregister(use);
            NeoForge.EVENT_BUS.unregister(join);
            NeoForge.EVENT_BUS.unregister(impact);
            NeoForge.EVENT_BUS.unregister(damage);
            NeoForge.EVENT_BUS.unregister(frame);
        }
        if (shooter != null) {
            onServer(player -> {
                if (leaf != null && !leaf.isRemoved()) leaf.discard();
                if (target != null && player.serverLevel().getEntity(target.getUUID()) == target) target.discard();
                for (BlockPos pos : temporaryFloor) player.serverLevel().removeBlock(pos, false);
            });
        }
        finish();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
