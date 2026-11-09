package net.yvanchuxiuzhen.system.fulu;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.Lightning;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.fulu.content.TalismanBeast;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanSummon;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 一次性释放的符箓（来源 {@code Itemfulu04...} 与 {@code Itemfulu25...} 共用过程中不依赖新实体的分支）：
 * 04 弹飞、06 落雷、07 烈焰、08 冰封、11 护体、25 加速、46 火球。统一流程为先付精力、播释放声、取品阶，再按编号执行，最后消耗一张。
 */
final class FuluInstant {
    private static final Set<Integer> NUMBERS = Set.of(1, 4, 5, 40, 43, 45, 47, 48, 49, 6, 7, 8, 9, 11, 12, 14, 25, 26, 27, 29, 32, 34, 36, 38, 46);

    private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ores"));

    private FuluInstant() {
    }

    static boolean handles(int number) {
        return NUMBERS.contains(number);
    }

    static void use(ServerPlayer player, ItemStack stack, int number) {
        if (number >= 47 && number <= 49) {
            enchantOffhand(player, stack, number);
            return;
        }
        if (!FuluUse.pay(player, stack)) {
            return;
        }
        if (number != 43 || player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
            FuluUse.cast(player);
        }
        double grade = FuluUse.grade(stack);
        ServerLevel level = player.serverLevel();
        switch (number) {
            case 1 -> thunder(level, player, stack, grade);
            case 4 -> burst(level, player, grade);
            case 5 -> qiSword(level, player, grade);
            case 6 -> lightning(level, player, grade);
            case 7 -> scorch(level, player, grade);
            case 8 -> freeze(level, player, grade);
            case 9 -> summon(level, player, grade, TalismanSummon.FLOAT, false);
            case 11 -> shield(level, player, grade);
            case 12 -> summon(level, player, grade, TalismanSummon.THUNDERSTORM, true);
            case 14 -> summon(level, player, grade, TalismanSummon.AIRBURST, true);
            case 25 -> haste(level, player, grade);
            case 26 -> beast(level, player, grade, TalismanBeast.EARTH_HAND, 6, "神压符");
            case 27 -> beast(level, player, grade, TalismanBeast.SWORD_SOUL, 5, "剑魂符");
            case 29 -> frost(level, player, grade);
            case 32 -> cluster(level, player, grade);
            case 34 -> beast(level, player, grade, TalismanBeast.FATE_WHEEL, 7.5, "命轮符");
            case 36 -> ores(level, player, stack, grade);
            case 38 -> beast(level, player, grade, TalismanBeast.WATER_DRAGON, 6, "水龙符");
            case 40 -> taunt(level, player, grade);
            case 43 -> {
                if (!earthSplit(level, player, grade)) {
                    return;
                }
            }
            case 45 -> cannon(level, player, grade);
            case 46 -> fireball(level, player, grade);
            default -> throw new IllegalArgumentException("not an instant talisman: " + number);
        }
        stack.shrink(1);
    }

    /** 04：20 枚符箓 21 粒子；速度提升 100 刻（等级 {@code 3+2×品阶}）；3 格内非友方被向外上抛。 */
    private static void burst(ServerLevel level, ServerPlayer player, double grade) {
        level.sendParticles(Particles.type("fulutexiao_21"), player.getX(), player.getY() + 1, player.getZ(), 20, 0, 0, 0, 1);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, (int) (3 + grade * 2)));
        for (Entity target : FuluTick.nearby(level, player.position(), 3)) {
            if (!Relations.isFriendly(player, target)) {
                target.setDeltaMovement(new Vec3((target.getX() - player.getX()) * 0.4, 0.5, (target.getZ() - player.getZ()) * 0.4));
            }
        }
    }

    /** 06：视线 7 格处（下移至空气）落下红色独立落雷，伤害基数 {@code 30+20×品阶}。 */
    private static void lightning(ServerLevel level, ServerPlayer player, double grade) {
        BlockPos spot = lowered(level, player, 7);
        Lightning.command(level, new Vec3(spot.getX(), spot.getY(), spot.getZ()),
                "lightninglanding " + (30 + grade * 20) + " " + player.getStringUUID() + " 1 " + Relations.team(player) + " 4 红色 独立");
    }

    /** 07：向前喷 20 枚火焰；视线 6 格处 3 格内非友方记录点燃来源，并受 {@code effect_023}（{@code 400+200×品阶} 刻，等级 {@code 2×(品阶−1)}）。 */
    private static void scorch(ServerLevel level, ServerPlayer player, double grade) {
        Vec3 look = player.getLookAngle();
        for (int index = 0; index < 20; index++) {
            FuluTick.spray(level, ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(),
                    look.x * Mth.nextDouble(player.getRandom(), 0, 2), look.y * Mth.nextDouble(player.getRandom(), 0, 2), look.z * Mth.nextDouble(player.getRandom(), 0, 2));
        }
        BlockPos spot = looked(level, player, 6);
        for (Entity target : FuluTick.nearby(level, Vec3.atLowerCornerOf(spot), 3)) {
            if (!Relations.isFriendly(player, target)) {
                markFire(player, target);
                level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1, target.getZ(), 20, 0.5, 0.8, 0.5, 0.1);
                if (target instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(effect("effect_023"), (int) (400 + grade * 200), (int) ((grade - 1) * 2)));
                }
            }
        }
    }

    /** 08：向前喷云雾与火焰；视线 5 格处 3 格内非友方受 {@code effect_023} 与缓慢（{@code 200+40×品阶} 刻，等级 1），并被向外击飞。 */
    private static void freeze(ServerLevel level, ServerPlayer player, double grade) {
        Vec3 look = player.getLookAngle();
        for (int index = 0; index < 30; index++) {
            FuluTick.spray(level, ParticleTypes.CLOUD, player.getX(), player.getY() + 1, player.getZ(),
                    look.x * Mth.nextDouble(player.getRandom(), 0, 2), look.y * Mth.nextDouble(player.getRandom(), 0, 2), look.z * Mth.nextDouble(player.getRandom(), 0, 2));
        }
        for (int index = 0; index < 15; index++) {
            FuluTick.spray(level, ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(),
                    look.x * Mth.nextDouble(player.getRandom(), 0, 2), look.y * Mth.nextDouble(player.getRandom(), 0, 2), look.z * Mth.nextDouble(player.getRandom(), 0, 2));
        }
        BlockPos spot = looked(level, player, 5);
        for (Entity target : FuluTick.nearby(level, Vec3.atLowerCornerOf(spot), 3)) {
            if (!Relations.isFriendly(player, target)) {
                for (int index = 0; index < 15; index++) {
                    FuluTick.spray(level, ParticleTypes.CLOUD, target.getX(), target.getY() + 1, target.getZ(),
                            look.x * Mth.nextDouble(player.getRandom(), 0, 2), look.y * Mth.nextDouble(player.getRandom(), 0, 2), look.z * Mth.nextDouble(player.getRandom(), 0, 2));
                }
                markFire(player, target);
                if (target instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(effect("effect_023"), (int) (200 + grade * 40), 1));
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (200 + grade * 40), 1));
                }
                target.setDeltaMovement(new Vec3(target.getX() - player.getX(), 0.5, target.getZ() - player.getZ()));
            }
        }
    }

    /** 11：吸收 4800 刻（等级 {@code 6×品阶}）与抗性提升 4800 刻（等级 {@code 品阶−1}），均为环境效果且不显示粒子。 */
    private static void shield(ServerLevel level, ServerPlayer player, double grade) {
        level.sendParticles(Particles.type("fulutexiao_11"), player.getX(), player.getY() + 1, player.getZ(), 10, 1, 1, 1, 1);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 4800, (int) (6 * grade), true, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4800, (int) (grade - 1), true, false));
    }

    /** 25：速度提升 3600 刻、力量提升 1200 刻，等级均为 {@code 2×品阶−1}。 */
    private static void haste(ServerLevel level, ServerPlayer player, double grade) {
        level.sendParticles(ParticleTypes.SPIT, player.getX(), player.getY() + 1, player.getZ(), 20, 0, 0, 0, 1);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, (int) (grade * 2 - 1)));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, (int) (grade * 2 - 1)));
    }

    /**
     * 09、12、14：在视线 7 格处（下移至空气）召唤不可见的飞行体，继承使用者的阵营并记入品阶与符名；
     * 12、14 另记使用者 UUID 作为雷与风伤害的来源（09 来源没有记）。具体行为见 {@link TalismanSummon}。
     */
    private static void summon(ServerLevel level, ServerPlayer player, double grade, String name, boolean master) {
        BlockPos spot = lowered(level, player, 7);
        TalismanSummon mob = FuluSetup.summon().get().create(level);
        mob.moveTo(spot.getX(), spot.getY(), spot.getZ(), level.random.nextFloat() * 360F, 0);
        Relations.copyAllegiance(player, mob);
        mob.getPersistentData().putDouble("fululevel", grade);
        mob.getPersistentData().putString("fuluname", name);
        if (master) {
            mob.getPersistentData().putString("belongToMasterUUID", player.getStringUUID());
        }
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(mob);
    }

    /**
     * 26、27、34、38：在视线 {@code distance} 格处（下移至空气）召唤动画实体，朝向与使用者一致，继承使用者阵营，
     * 记入品阶、符名与召唤者 UUID；27 号另把最大生命与攻击设为 {@code 100×品阶}、{@code 10×品阶} 并写入 {@code health_Max}、{@code attack_Num}。
     */
    private static void beast(ServerLevel level, ServerPlayer player, double grade, String id, double distance, String name) {
        BlockPos spot = lowered(level, player, distance);
        TalismanBeast mob = FuluSetup.beast(id).get().create(level);
        mob.moveTo(spot.getX(), spot.getY(), spot.getZ(), level.random.nextFloat() * 360F, 0);
        Relations.copyAllegiance(player, mob);
        mob.setYRot(player.getYRot());
        mob.setXRot(0);
        mob.setYBodyRot(mob.getYRot());
        mob.setYHeadRot(mob.getYRot());
        mob.yRotO = mob.getYRot();
        mob.xRotO = mob.getXRot();
        mob.yBodyRotO = mob.getYRot();
        mob.yHeadRotO = mob.getYRot();
        mob.getPersistentData().putDouble("fululevel", grade);
        mob.getPersistentData().putString("fuluname", name);
        mob.getPersistentData().putString("belongToMasterUUID", player.getStringUUID());
        if (id.equals(TalismanBeast.SWORD_SOUL)) {
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100 * grade);
            mob.getPersistentData().putDouble("health_Max", 100 * grade);
            mob.setHealth((float) (100 * grade));
            mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10 * grade);
            mob.getPersistentData().putDouble("attack_Num", 10 * grade);
        }
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(mob);
    }

    /** 01：在视线 7 格处（先试下移 2、再试 1）召唤雷符实体，记入品阶、阵营与召唤者 UUID。 */
    private static void thunder(ServerLevel level, ServerPlayer player, ItemStack stack, double grade) {
        BlockPos spot = looked(level, player, 7);
        if (level.isEmptyBlock(spot.below(2))) {
            spot = spot.below(2);
        } else if (level.isEmptyBlock(spot.below(1))) {
            spot = spot.below(1);
        }
        TalismanBeast mob = FuluSetup.beast(TalismanBeast.THUNDER_PULL).get().create(level);
        mob.moveTo(spot.getX(), spot.getY(), spot.getZ(), level.random.nextFloat() * 360F, 0);
        Relations.copyAllegiance(player, mob);
        mob.getPersistentData().putDouble("fululevel", grade);
        mob.getPersistentData().putString("belongToMasterUUID", player.getStringUUID());
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(mob);
    }

    /**
     * 36：以使用者所在位置为中心，水平 ±3 格、垂直自 {@code −20×品阶} 至 {@code −1} 格（只含脚下方，来源循环如此）内的矿石标签方块
     * （来源 {@code forge:ores}，含原版矿石，对应本项目矿石标签与 {@code c:ores}）被破坏，掉落物落在使用者脚下；之后该符冷却 200 刻。
     */
    private static void ores(ServerLevel level, ServerPlayer player, ItemStack stack, double grade) {
        BlockPos feet = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        for (int dx = -3; dx <= 3; dx++) {
            for (int layer = 0; layer < (int) (grade * 20); layer++) {
                double dy = grade * 20 - grade * 40 + layer;
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos pos = BlockPos.containing(player.getX() + dx, player.getY() + dy, player.getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(ORES) || state.is(Tags.Blocks.ORES)) {
                        Block.dropResources(state, level, feet, null);
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
        player.getCooldowns().addCooldown(stack.getItem(), 200);
    }

    /**
     * 40：防御与效果 022 各 1200 刻（等级 {@code 2×品阶−1}、{@code 品阶−1}），身周 3 格球面粒子；
     * 64 格内所有非友方生物转向使用者并以其为攻击目标。
     */
    private static void taunt(ServerLevel level, ServerPlayer player, double grade) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, (int) (grade * 2 - 1)));
        player.addEffect(new MobEffectInstance(effect("effect_022"), 1200, (int) (grade - 1)));
        double radius = 3;
        for (double xx = -radius; xx <= radius; xx += 0.1) {
            for (double yy = -radius; yy <= radius; yy += 0.1) {
                for (double zz = -radius; zz <= radius; zz += 0.1) {
                    if (Math.abs(xx * xx + yy * yy + zz * zz - radius * radius) <= 0.15) {
                        level.sendParticles(Particles.type("fulutexiao_40"), player.getX() + xx, player.getY() + 1 + yy, player.getZ() + zz, 1, 0.01, 0.01, 0.01, 0);
                    }
                }
            }
        }
        for (Entity target : FuluTick.nearby(level, new Vec3(player.getX(), player.getY() + 1, player.getZ()), 32)) {
            if (target instanceof Mob mob && !Relations.isFriendly(player, target)) {
                mob.getNavigation().moveTo(player.getX(), player.getY() + 1, player.getZ(), 2);
                mob.setTarget(player);
            }
        }
    }

    /**
     * 43：冒险模式下已付精力却不生效，只提示；否则在视线 7 格处边长 {@code 6×品阶+1} 的立方体内，
     * 破坏硬度不低于 0 且低于 50 的方块并在原位掉落。返回是否生效（决定是否消耗符）。
     */
    private static boolean earthSplit(ServerLevel level, ServerPlayer player, double grade) {
        if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.adventure_forbidden"), false);
            return false;
        }
        BlockPos spot = looked(level, player, 7);
        int size = (int) (grade * 6 + 1);
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                for (int dz = 0; dz < size; dz++) {
                    BlockPos pos = spot.offset((int) (dx - grade * 3), (int) (dy - grade * 3), (int) (dz - grade * 3));
                    BlockState state = level.getBlockState(pos);
                    float hardness = state.getDestroySpeed(level, pos);
                    if (hardness < 50 && hardness >= 0) {
                        Block.dropResources(state, level, pos, null);
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
        return true;
    }

    /** 45：落点 2 格处爆炸并击飞、灼烧 2 格内非友方，再射出一枚火炮弹；向前喷火焰与烟。 */
    private static void cannon(ServerLevel level, ServerPlayer player, double grade) {
        BlockPos spot = looked(level, player, 2);
        level.sendParticles(Particles.type("smallexplosionorange"), spot.getX(), spot.getY(), spot.getZ(), 1, 0, 0, 0, 0);
        for (Entity target : FuluTick.nearby(level, Vec3.atLowerCornerOf(spot), 2)) {
            if (!Relations.isFriendly(player, target)) {
                target.setDeltaMovement(new Vec3((target.getX() - player.getX()) * 0.85, 0.5, (target.getZ() - player.getZ()) * 0.85));
                if (target instanceof LivingEntity living) {
                    SpellDamage.command(level, living, "shanghai 火 " + player.getStringUUID() + " true " + 25 * grade + " 1");
                }
            }
        }
        player.getPersistentData().putDouble("fulu45level", grade);
        TalismanProjectile.launch(level, FuluSetup.projectile(Kind.CANNON).get(), player, eyeFront(player), player.getLookAngle());
        Vec3 look = player.getLookAngle();
        for (int index = 0; index < 20; index++) {
            FuluTick.spray(level, ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(),
                    look.x * Mth.nextDouble(player.getRandom(), 0.3, 1.7), look.y * Mth.nextDouble(player.getRandom(), 0.3, 1.7), look.z * Mth.nextDouble(player.getRandom(), 0.3, 1.7));
            FuluTick.spray(level, ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(),
                    look.x * Mth.nextDouble(player.getRandom(), 0.3, 1.7), look.y * Mth.nextDouble(player.getRandom(), 0.3, 1.7), look.z * Mth.nextDouble(player.getRandom(), 0.3, 1.7));
        }
    }

    /**
     * 47、48、49：不付精力；主手拿着该符时，给副手物品附魔连锁、开灵、穿石（品阶 1、2、3 对应等级 1、2、3，其余为 4），并消耗一张。
     * 副手为空时来源写在空堆上没有可见效果，仍消耗符。
     */
    private static void enchantOffhand(ServerPlayer player, ItemStack stack, int number) {
        if (!player.getMainHandItem().is(stack.getItem())) {
            return;
        }
        double grade = FuluUse.grade(stack);
        int level = grade == 1 ? 1 : grade == 2 ? 2 : grade == 3 ? 3 : 4;
        ItemStack target = player.getOffhandItem();
        if (!target.isEmpty()) {
            String name = switch (number) {
                case 47 -> "chain";
                case 48 -> "kailing";
                default -> "chuanshi";
            };
            Holder<Enchantment> enchantment = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key(name));
            EnchantmentHelper.updateEnchantments(target, mutable -> mutable.set(enchantment, level));
        }
        stack.shrink(1);
    }

    /** 05：品阶记入使用者持久数据，从眼前射出一枚穿透 10 的气剑。 */
    private static void qiSword(ServerLevel level, ServerPlayer player, double grade) {
        player.getPersistentData().putDouble("fulu05level", grade);
        TalismanProjectile.launch(level, FuluSetup.projectile(Kind.QI_SWORD).get(), player, eyeFront(player), player.getLookAngle());
    }

    /** 29：品阶记入持久数据，从胸前向前扇形射出 12 枚冰锥，方向各分量各乘 0.2–1.8 的随机系数。 */
    private static void frost(ServerLevel level, ServerPlayer player, double grade) {
        player.getPersistentData().putDouble("fulu29level", grade);
        Vec3 look = player.getLookAngle();
        for (int index = 0; index < 12; index++) {
            TalismanProjectile.launch(level, FuluSetup.projectile(Kind.FROST).get(), player, new Vec3(player.getX(), player.getY() + 1.2, player.getZ()),
                    new Vec3(look.x * Mth.nextDouble(player.getRandom(), 0.2, 1.8), look.y * Mth.nextDouble(player.getRandom(), 0.2, 1.8),
                            look.z * Mth.nextDouble(player.getRandom(), 0.2, 1.8)));
        }
    }

    /** 32：品阶与“可分裂”标记记入持久数据，从眼前射出子母弹主弹。 */
    private static void cluster(ServerLevel level, ServerPlayer player, double grade) {
        player.getPersistentData().putDouble("fulu32level", grade);
        player.getPersistentData().putBoolean("fulu32shoufa", true);
        TalismanProjectile.launch(level, FuluSetup.projectile(Kind.CLUSTER).get(), player, eyeFront(player), player.getLookAngle());
    }

    private static Vec3 eyeFront(ServerPlayer player) {
        return new Vec3(player.getX(), player.getEyeY() - 0.1, player.getZ());
    }

    /**
     * 46：从眼前射出一枚火球。来源把品阶写进玩家的实体持久数据，而火球读的是玩家体质里的 {@code fululevel46}（仅 13 号符写入），
     * 所以玩家直接使用 46 号符的火球按品阶 1 结算；保持来源（登记为 Q66）。
     */
    private static void fireball(ServerLevel level, ServerPlayer player, double grade) {
        TalismanProjectile.launch(level, FuluSetup.projectile(Kind.FIREBALL).get(), player, eyeFront(player), player.getLookAngle());
        player.getPersistentData().putDouble("fululevel46", grade);
    }

    private static BlockPos looked(ServerLevel level, Entity viewer, double distance) {
        Vec3 eye = viewer.getEyePosition(1f);
        return level.clip(new ClipContext(eye, eye.add(viewer.getViewVector(1f).scale(distance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, viewer)).getBlockPos();
    }

    /** 视线落点向下取至多 3 格内最先为空气的位置（先试 3、再 2、再 1）。 */
    private static BlockPos lowered(ServerLevel level, Entity viewer, double distance) {
        BlockPos spot = looked(level, viewer, distance);
        for (int drop = 3; drop >= 1; drop--) {
            if (level.isEmptyBlock(spot.below(drop))) {
                return spot.below(drop);
            }
        }
        return spot;
    }

    private static void markFire(Entity caster, Entity target) {
        String team = Relations.team(caster);
        if (target instanceof Player victim && victim instanceof ServerPlayer serverVictim) {
            Cultivation.setTizhiString(serverVictim, "fireteam", team);
            Cultivation.setTizhiString(serverVictim, "fireAttackUUID", caster.getStringUUID());
        } else {
            target.getPersistentData().putString("fireteam", team);
            target.getPersistentData().putString("fireAttackUUID", caster.getStringUUID());
        }
    }

    private static Holder<MobEffect> effect(String id) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)).orElseThrow();
    }
}
