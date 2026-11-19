package net.yvanchuxiuzhen.smoke;

import com.google.common.hash.Hashing;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
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
import net.minecraft.world.entity.Marker;
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

    /** 从施放后归档重播原生使用，并以侧视连帧观察实体碰撞与客户端绿爆。 */
final class RenjianRewardLeafImpactCheck extends SystemCheck {
    static final String SELECTOR = "renjianrewardleafimpact";
    static final String SAVE_ID = "renjian-q40-impact-visual-replay";
    private static final UUID PLAYER = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");
    private static final String LEVEL_HASH = "f515052493fdda603dc90f41b183b6ea7c8165ff7ec797627400aab842b5dc9";
    private static final String PLAYER_HASH = "5bcf98998122be9dced09a209fe673bd43c629b2ab914e50005f1621c5a668e6";
    private static final Vec3 ORIGIN = new Vec3(8.5, 180, 8.5);
    private static final Vec3 TARGET = ORIGIN.add(0, 0, 6);
    private static final Vec3 SIDE_EYE = new Vec3(12.9, 181.7, 13.1);
    private static final Vec3 SIDE_FOCUS = new Vec3(8.5, 181.35, 13.95);
    private static final ResourceLocation LEAF = id("projectile_skill_tree_01");
    private static final ResourceLocation ICON = id("cailiao_087");
    private static final ResourceKey<DamageType> SPELL = ResourceKey.create(Registries.DAMAGE_TYPE, id("linglidamage_player"));
    private static final String BURST = "smallexplosiongreen";
    private static final String SHOT_PREFIX = "renjian_q40_side_";

    private record Spawn(UUID uuid, int tick, Vec3 position, Vec3 velocity) {}

    private record Impact(int tick, int flightTicks, Vec3 position, UUID target, boolean basePresent, double base,
                          boolean multiplierPresent, double multiplier) {
        private static Impact read(SkillProjectile leaf, int tick, UUID target) {
            CompoundTag data = leaf.getPersistentData();
            return new Impact(tick, leaf.ticks(), leaf.position(), target, data.contains("AttackDamage"), data.getDouble("AttackDamage"),
                    data.contains("Damage_multiplier"), data.getDouble("Damage_multiplier"));
        }
    }

    private record BurstFrame(int age, int targetHurtTime, String file) {}

    private final Consumer<String> shot;
    private final Consumer<PlayerInteractEvent.RightClickItem> use = this::observeUse;
    private final Consumer<EntityJoinLevelEvent> join = this::observeJoin;
    private final Consumer<ProjectileImpactEvent> impact = this::observeImpact;
    private final Consumer<LivingIncomingDamageEvent> damage = this::observeDamage;
    private final Consumer<RenderFrameEvent.Post> frame = event -> captureImpact();
    private final List<BlockPos> temporaryFloor = new ArrayList<>();
    private final List<Float> arrowDamage = new ArrayList<>();
    private final BurstFrame[] burstFrames = new BurstFrame[3];
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
    private SkillProjectile clientLeaf;
    private Marker sideCamera;
    private String beforeShot;
    private String flightShot;
    private String afterShot;
    private String screenshotError;
    private Field particleRow;
    private Field particleAge;

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
                if (screenshotError != null) {
                    stop("侧视基线采证失败，未发送使用包：" + screenshotError);
                    return false;
                }
                boolean[] ready = new boolean[1];
                onServer(player -> ready[0] = readyToCast(player) && target.isAlive() && !Relations.isFriendly(player, target));
                if (ready[0] && readyToCast(minecraft.player) && minecraft.screen == null && clientTarget() != null) {
                    if (sideCamera == null) setSideCamera();
                    if (beforeShot != null) {
                        minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
                        return true;
                    }
                }
                if (local > 160) stop("等待原书、目标追踪、自然冷却及侧视基线超时；未发送使用包");
                return false;
            }
            case 2 -> {
                if (local < 20 || (afterShot == null && screenshotError == null && local < 100)) return false;
                onServer(this::verifyServer);
                expectTrue("客户端收到同 UUID 叶弹及显示物品；数量=" + trackedLeaves + "，物品=" + trackedIcon,
                        trackedLeaves == 1 && ICON.equals(trackedIcon));
                expectTrue("侧视素材待人工判读；相机=" + SIDE_EYE + " → " + SIDE_FOCUS + "，基线=" + beforeShot
                                + "，命中前叶弹=" + flightShot + "，" + BURST + " 年龄段=" + Arrays.toString(burstFrames)
                                + "，消散后=" + afterShot + (screenshotError == null ? "" : "；采证错误=" + screenshotError),
                        screenshotError == null && beforeShot != null && flightShot != null && burstFrames[0] != null
                                && burstFrames[0].targetHurtTime() > 0 && burstFrames[1] != null
                                && burstFrames[2] != null && afterShot != null);
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
                clientLeaf = projectile;
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
            hit = Impact.read(leaf, shooter.getServer().getTickCount(), entityHit.getEntity().getUUID());
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
        expectTrue("唯一一次原生碰撞为受控目标，第 5 个飞行刻两个字段缺省，叶弹被移除；碰撞数=" + nativeImpacts
                        + "，记录=" + hit + "，弹体现态=" + (leaf == null ? "未生成" : leaf.position() + " / " + leaf.getRemovalReason()),
                nativeImpacts == 1 && hit != null && hit.target().equals(target.getUUID()) && spawn != null && hit.tick() > spawn.tick()
                        && hit.flightTicks() == 5 && !hit.basePresent() && hit.base() == 0
                        && !hit.multiplierPresent() && hit.multiplier() == 0 && leaf.isRemoved());
        expectTrue("仅一笔原生箭伤，未进入该施放者的灵力受伤事件；灵力事件=" + spellDamageEvents + "，原生箭伤请求=" + arrowDamage
                        + "，目标生命=" + startingHealth + " → " + target.getHealth(),
                hit != null && spellDamageEvents == 0 && arrowDamage.equals(List.of(1.0F))
                        && startingHealth == 100.0F && target.getHealth() == 99.0F);
    }

    private IronGolem clientTarget() {
        return minecraft.level.getEntity(target.getId()) instanceof IronGolem golem && golem.getUUID().equals(target.getUUID())
                ? golem : null;
    }

    /** 只替换本地相机，不改变玩家视线或在世界中生成摄影实体。 */
    private void setSideCamera() {
        Vec3 direction = SIDE_FOCUS.subtract(SIDE_EYE);
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, direction.horizontalDistance()));
        sideCamera = new Marker(EntityType.MARKER, minecraft.level);
        sideCamera.moveTo(SIDE_EYE.x, SIDE_EYE.y, SIDE_EYE.z, yaw, pitch);
        minecraft.setCameraEntity(sideCamera);
        minecraft.options.hideGui = true;
    }

    /** 绿爆在命中回调时的叶弹坐标，用粒子年龄同步同一侧视机位的连帧。 */
    private void captureImpact() {
        if (sideCamera == null || afterShot != null || screenshotError != null || minecraft.level == null
                || minecraft.screen != null || minecraft.getOverlay() != null || !minecraft.options.hideGui) return;
        var camera = minecraft.gameRenderer.getMainCamera();
        IronGolem golem = clientTarget();
        if (golem == null || camera.getEntity() != sideCamera || camera.getPosition().distanceToSqr(SIDE_EYE) > 0.0004) return;
        try {
            if (beforeShot == null) beforeShot = capture("before");
            if (flightShot == null && clientLeaf != null && !clientLeaf.isRemoved() && golem.hurtTime == 0
                    && clientLeaf.getZ() >= ORIGIN.z + 3 && clientLeaf.getZ() < SIDE_FOCUS.z) {
                flightShot = capture("flight");
            }
            Impact observed = hit;
            if (observed == null) return;
            int age = burstAge(observed.position());
            if (age >= 0) {
                int band = age <= 3 ? 0 : age <= 8 ? 1 : 2;
                if (burstFrames[band] == null) {
                    burstFrames[band] = new BurstFrame(age, golem.hurtTime, capture("age_" + age));
                }
            } else if (burstFrames[0] != null || burstFrames[1] != null || burstFrames[2] != null) {
                afterShot = capture("after");
            }
        } catch (RuntimeException exception) {
            screenshotError = exception.toString();
        }
    }

    private String capture(String suffix) {
        String file = SHOT_PREFIX + suffix + ".png";
        shot.accept(file);
        return file;
    }

    private int burstAge(Vec3 position) {
        int[] age = {-1};
        minecraft.particleEngine.iterateParticles(particle -> {
            if (!particle.isAlive() || particle.getPos().distanceToSqr(position) > 1e-6
                    || !particle.getClass().getName().equals("net.yvanchuxiuzhen.system.particle.client.LibraryParticle")) return;
            try {
                if (particleRow == null) {
                    particleRow = particle.getClass().getDeclaredField("row");
                    particleRow.setAccessible(true);
                    particleAge = Particle.class.getDeclaredField("age");
                    particleAge.setAccessible(true);
                }
                if (BURST.equals(((ParticleRow) particleRow.get(particle)).id())) age[0] = particleAge.getInt(particle);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("无法读取客户端粒子年龄", exception);
            }
        });
        return age[0];
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
        if (sideCamera != null) {
            minecraft.setCameraEntity(minecraft.player);
            minecraft.options.hideGui = false;
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
