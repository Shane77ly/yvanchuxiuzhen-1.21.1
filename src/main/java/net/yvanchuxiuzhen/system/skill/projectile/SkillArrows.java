package net.yvanchuxiuzhen.system.skill.projectile;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 十种技能箭的飞行与命中过程；有模式的箭在首刻消费发射者标记，第 02 箭只推进自身轨迹与寿命。 */
public final class SkillArrows {
    private SkillArrows() {
    }

    public static String id(int kind) {
        return "projectile_skill_arrow_%02d".formatted(kind);
    }

    public static void register(ContentTables tables) {
        for (int kind = 1; kind <= 10; kind++) {
            int number = kind;
            tables.<SkillProjectile>entity(id(kind), (type, level) -> new Arrow(type, level, number));
        }
    }

    public static SkillProjectile create(ServerPlayer owner, int kind, int knockback, int pierce, boolean control, boolean alt,
                                          boolean fullCharge, boolean infinity) {
        SkillProjectile arrow = SkillSetup.<SkillProjectile>entityType(id(kind)).get().create(owner.serverLevel()).launchedWith(knockback, pierce);
        arrow.setOwner(owner);
        arrow.getPersistentData().putBoolean("arrowControl", control);
        arrow.getPersistentData().putBoolean("arrowAlt", alt);
        arrow.getPersistentData().putBoolean("infinity", infinity);
        Cultivation.setTizhiBoolean(owner, "arrowskill", true);
        Cultivation.setTizhiBoolean(owner, "manxvli", fullCharge);
        return arrow;
    }

    /** 技能箭不提供物品模型，拾取由命中方块过程生成物品；共用生命周期由 SkillProjectile 承担。 */
    private static final class Arrow extends SkillProjectile {
        private Arrow(EntityType<SkillProjectile> type, Level level, int kind) {
            super(type, level, new Behavior(kind));
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }
    }

    private record Behavior(int kind) implements ProjectileSkill {
        private static String mode(SkillProjectile arrow) {
            return arrow.getPersistentData().getString("jinengName");
        }

        private void initialize(SkillProjectile arrow, Entity owner) {
            CompoundTag data = arrow.getPersistentData();
            // 来源第 02 箭只推进重力与寿命，不消费玩家的箭种施放标记。
            if (kind == 2 || data.getBoolean("jilu")) {
                return;
            }
            if (owner instanceof ServerPlayer player && Cultivation.tizhiBoolean(player, "arrowskill")) {
                boolean control = data.getBoolean("arrowControl");
                boolean alt = data.getBoolean("arrowAlt");
                boolean full = Cultivation.tizhiBoolean(player, "manxvli");
                boolean sneak = player.isShiftKeyDown();
                String selected = switch (kind) {
                    case 1, 8, 10 -> control ? "箭雨" : alt && full ? "增伤" : "";
                    case 3 -> sneak ? "响箭" : control ? "铃箭" : alt ? "音爆箭" : "打草惊蛇";
                    case 4 -> sneak ? "旋风箭" : control ? "风压箭" : alt ? "飓风箭" : "";
                    case 5 -> sneak ? "爆燃" : control ? "爆炎" : alt && full ? "增伤" : "";
                    case 6 -> sneak ? "冲击波" : control ? "内爆" : alt ? "震爆" : "";
                    case 7 -> sneak ? "坎坷路" : control ? "飞沙走石" : alt ? "震灵" : "";
                    case 9 -> control ? "暗流" : alt ? "漩涡" : "";
                    default -> throw new IllegalStateException("技能箭类型：" + kind);
                };
                data.putString("jinengName", selected);
                if ((kind == 1 || kind == 10) && selected.equals("增伤")
                        || selected.equals("飓风箭") || selected.equals("漩涡")) {
                    arrow.setBaseDamage(arrow.getBaseDamage() * 1.5);
                }
                if (selected.equals("震灵")) {
                    arrow.addPiercing(6);
                    arrow.setNoGravity(true);
                } else if (selected.equals("震爆")) {
                    player.setDeltaMovement(player.getLookAngle().scale(-1));
                }
                data.putBoolean("diaoluo", true);
                Cultivation.setTizhiBoolean(player, "arrowskill", false);
                Cultivation.setTizhiBoolean(player, "manxvli", false);
            }
            data.putBoolean("jilu", true);
        }

        @Override
        public void tick(SkillProjectile arrow, Entity owner) {
            initialize(arrow, owner);
            ServerLevel level = (ServerLevel) arrow.level();
            switch (kind) {
                case 2 -> {
                    arrow.setNoGravity(true);
                    ProjectileSupport.particles(level, Particles.type("lingqi"), arrow.position(), 2, .02, .02, .02, 0);
                    CompoundTag data = arrow.getPersistentData();
                    int ticks = data.getInt("xiaoshitick") + 1;
                    data.putInt("xiaoshitick", ticks >= 20 ? 0 : ticks);
                    if (ticks >= 20) {
                        ProjectileSupport.particles(level, Particles.type("lingqi"), arrow.position(), 20, 0, 0, 0, 1);
                        arrow.discard();
                    }
                }
                case 4 -> windFlight(arrow, owner);
                case 5 -> {
                    ProjectileSupport.particles(level, ParticleTypes.FLAME, arrow.position(), 5, .2, .2, .2, 0);
                    ProjectileSupport.particles(level, ParticleTypes.FLAME, arrow.position(), 2, 0, 0, 0, 0);
                }
                case 6 -> ProjectileSupport.particles(level, ParticleTypes.WAX_ON, arrow.position(), 3, 0, 0, 0, 0);
                case 7 -> ProjectileSupport.particles(level, ParticleTypes.SMOKE, arrow.position(), 5, .2, .2, .2, 0);
                case 8 -> ProjectileSupport.particles(level, ParticleTypes.FALLING_OBSIDIAN_TEAR, arrow.position(), 2, .1, .1, .1, 0);
                case 9 -> waterFlight(arrow);
                default -> { }
            }
            if ((kind == 1 || kind == 8) && mode(arrow).equals("增伤")) {
                ProjectileSupport.burst(arrow, "jibo");
            }
        }

        private static void windFlight(SkillProjectile arrow, Entity owner) {
            String mode = mode(arrow);
            if (!mode.equals("旋风箭") && !mode.equals("飓风箭")) {
                return;
            }
            ServerLevel level = (ServerLevel) arrow.level();
            ProjectileSupport.particles(level, ParticleTypes.CLOUD, arrow.position(), 5, 0, 0, 0, 0);
            if (mode.equals("飓风箭")) {
                ProjectileSupport.particles(level, ParticleTypes.CLOUD, arrow.position(), 10, 0, 0, 0, 1);
            }
            for (Entity target : enemies(arrow, owner, 2)) {
                if (target == arrow) {
                    continue;
                }
                if (mode.equals("飓风箭")) {
                    damage(arrow, target, owner, "风", .3);
                    if (SkillEntities.movable(target)) {
                        target.setDeltaMovement(new Vec3((target.getX() - owner.getX()) * .3, .2, (target.getZ() - owner.getZ()) * .3));
                    }
                } else if (SkillEntities.movable(target)) {
                    target.setDeltaMovement(new Vec3((arrow.getX() - target.getX()) * .4, .5, (arrow.getZ() - target.getZ()) * .4));
                }
            }
        }

        private static void waterFlight(SkillProjectile arrow) {
            ServerLevel level = (ServerLevel) arrow.level();
            CompoundTag data = arrow.getPersistentData();
            if (level.isRaining() || arrow.isInWater()) {
                ProjectileSupport.particles(level, ParticleTypes.BUBBLE_POP, arrow.position(), 5, .2, .2, .2, 0);
                if (!data.getBoolean("bishui")) {
                    data.putBoolean("bishui", true);
                    arrow.setNoGravity(true);
                }
                int ticks = data.getInt("flytick") + 1;
                data.putInt("flytick", ticks >= 100 ? 0 : ticks);
                if (ticks >= 100) {
                    arrow.discard();
                }
            } else if (data.getBoolean("bishui")) {
                ProjectileSupport.particles(level, ParticleTypes.RAIN, arrow.position(), 10, .5, 0, .5, 0);
                data.putBoolean("bishui", false);
                arrow.setNoGravity(false);
            }
        }

        @Override
        public void hitEntity(SkillProjectile arrow, Entity target, Entity owner) {
            rain(arrow, target);
            String mode = mode(arrow);
            double multiplier = mode.equals("增伤") ? 1.5 : 1;
            boolean enemy = !ProjectileSupport.friendly(owner, target);
            switch (kind) {
                case 2 -> {
                    if (enemy) {
                        ProjectileSupport.burst(arrow, "smallexplosionskyblue");
                        damage(arrow, target, owner, "无", multiplier);
                    }
                }
                case 3 -> {
                    if (enemy && !mode.equals("音爆箭")) damage(arrow, target, owner, "音波", multiplier);
                    sound(arrow, owner);
                }
                case 4 -> {
                    if (mode.equals("风压箭")) windPressure(arrow, owner);
                    else if (enemy) {
                        ProjectileSupport.burst(arrow, "smallexplosionlightblue");
                        damage(arrow, target, owner, "风", multiplier);
                        if (SkillEntities.movable(target)) target.setDeltaMovement(0, 1.5, 0);
                    }
                }
                case 5 -> {
                    if (mode.equals("爆燃") || mode.equals("爆炎")) fireArea(arrow, owner);
                    else if (enemy) {
                        ProjectileSupport.burst(arrow, "smallexplosionred");
                        damage(arrow, target, owner, "火", multiplier);
                        burn(target);
                    }
                }
                case 6 -> {
                    if (enemy) {
                        double defense = target instanceof Player player ? PlayerAttributes.value(player, Stat.ATTACK_DEF)
                                : target.getPersistentData().getDouble("attack_Def");
                        if (arrow.getBaseDamage() > defense) {
                            target.hurt(target.damageSources().source(DamageTypes.FELL_OUT_OF_WORLD, owner), (float) (arrow.getBaseDamage() - defense));
                        }
                        if (mode.equals("冲击波")) target.setDeltaMovement(new Vec3(owner.getLookAngle().x * 2, .3, owner.getLookAngle().z * 2));
                        else if (mode.equals("内爆") && target instanceof LivingEntity living) {
                            boolean present = StatusEffects.has(living, "skill_arrow_0601");
                            StatusEffects.add(living, "skill_arrow_0601", present ? 100 : 200,
                                    present ? StatusEffects.amplifier(living, "skill_arrow_0601") + 1 : 0);
                        } else if (mode.equals("震爆")) shock(arrow, owner);
                    }
                }
                case 7 -> {
                    if (enemy) {
                        if (target.onGround()) slow(target, 120, 1);
                        if (mode.equals("坎坷路") || mode.equals("飞沙走石")) earthArea(arrow, owner);
                        else {
                            ProjectileSupport.burst(arrow, "smallexplosiondeeporange");
                            damage(arrow, target, owner, "土", multiplier);
                        }
                    }
                }
                case 8 -> {
                    if (enemy) {
                        target.getPersistentData().putDouble("Damage_multiplier", mode.equals("增伤") ? 1.2 : .8);
                        if (target instanceof LivingEntity living) StatusEffects.add(living, "duyao_11", 600, 1);
                        ProjectileSupport.burst(arrow, "smallexplosionblack");
                        damage(arrow, target, owner, "暗", multiplier);
                    }
                }
                case 9 -> {
                    if (enemy) {
                        if (arrow.isInWater()) {
                            multiplier = 1.5;
                            if (mode.equals("暗流") && SkillEntities.movable(target)) target.setDeltaMovement(owner.getLookAngle().scale(3));
                            else if (mode.equals("漩涡")) slow(target, 200, 20);
                        }
                        ProjectileSupport.burst(arrow, "smallexplosionblue");
                        damage(arrow, target, owner, "水", multiplier);
                    }
                }
                case 10 -> {
                    if (enemy && target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 12000, 0));
                }
                default -> { }
            }
        }

        @Override
        public void hitBlock(SkillProjectile arrow, BlockHitResult hit, Entity owner) {
            switch (kind) {
                case 2 -> ProjectileSupport.particles((ServerLevel) arrow.level(), Particles.type("xvlilizi"), arrow.position(), 10, 0, 0, 0, 1);
                case 3 -> sound(arrow, owner);
                case 4 -> { if (mode(arrow).equals("风压箭")) windPressure(arrow, owner); }
                case 5 -> fireArea(arrow, owner);
                case 6 -> { if (mode(arrow).equals("震爆")) shock(arrow, owner); }
                case 7 -> earthArea(arrow, owner);
                case 8 -> darkCloud(arrow);
                default -> { }
            }
            rain(arrow, owner);
            if (kind != 2 && !arrow.getPersistentData().getBoolean("infinity") && arrow.getPersistentData().getBoolean("diaoluo")) {
                ItemStack stack = kind == 10 ? new ItemStack(Items.SPECTRAL_ARROW) : new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_arrow_%02d".formatted(kind))));
                ItemEntity item = new ItemEntity(arrow.level(), arrow.getX(), arrow.getY(), arrow.getZ(), stack);
                item.setPickUpDelay(10);
                arrow.level().addFreshEntity(item);
            }
        }

        private static List<Entity> enemies(SkillProjectile arrow, Entity owner, double radius) {
            return ProjectileSupport.around((ServerLevel) arrow.level(), arrow.position(), radius).stream()
                    .filter(target -> !ProjectileSupport.friendly(owner, target)).toList();
        }

        private static void damage(SkillProjectile arrow, Entity target, Entity owner, String category, double multiplier) {
            ProjectileSupport.deal((ServerLevel) arrow.level(), target, owner, category, 1, multiplier);
        }

        private static void sound(SkillProjectile arrow, Entity owner) {
            String mode = mode(arrow);
            if (mode.equals("音爆箭")) {
                ProjectileSupport.burst(arrow, "yinbo");
            }
            for (Entity target : ProjectileSupport.around((ServerLevel) arrow.level(), arrow.position(), 8)) {
                boolean friendly = ProjectileSupport.friendly(owner, target);
                if (mode.equals("音爆箭") && !friendly) damage(arrow, target, owner, "音波", 1);
                else if (target instanceof Mob mob) {
                    if (mode.equals("打草惊蛇") && !friendly && owner instanceof LivingEntity living) mob.setTarget(living);
                    else if (mode.equals("响箭") && friendly || mode.equals("铃箭") && !friendly) mob.getNavigation().moveTo(arrow.getX(), arrow.getY(), arrow.getZ(), 2);
                }
            }
        }

        private static void windPressure(SkillProjectile arrow, Entity owner) {
            ProjectileSupport.particles((ServerLevel) arrow.level(), ParticleTypes.CLOUD, arrow.position(), 40, 0, 0, 0, 1);
            for (Entity target : enemies(arrow, owner, 5)) {
                ProjectileSupport.burst(target, "smallexplosionlightblue", target.getBbHeight() / 2);
                damage(arrow, target, owner, "风", .3);
                if (SkillEntities.movable(target)) target.setDeltaMovement(new Vec3(target.getX() - arrow.getX(), .2, target.getZ() - arrow.getZ()));
            }
        }

        private static void burn(Entity target) {
            if (target instanceof LivingEntity living) {
                int amplifier = StatusEffects.has(living, "effect_023") ? Math.min(2, StatusEffects.amplifier(living, "effect_023") + 1) : 0;
                StatusEffects.add(living, "effect_023", 400, amplifier);
            }
        }

        private static void fireArea(SkillProjectile arrow, Entity owner) {
            String mode = mode(arrow);
            if (!mode.equals("爆燃") && !mode.equals("爆炎")) return;
            ServerLevel level = (ServerLevel) arrow.level();
            ProjectileSupport.particles(level, ParticleTypes.FLAME, arrow.position(), 20, 0, 0, 0, 1);
            if (mode.equals("爆炎")) {
                ProjectileSupport.particles(level, ParticleTypes.EXPLOSION, arrow.position(), 1, 0, 0, 0, 0);
                explosionSound(arrow);
            }
            for (Entity target : enemies(arrow, owner, 2.5)) {
                if (mode.equals("爆燃")) burn(target);
                else {
                    ProjectileSupport.burst(target, "smallexplosionred", target.getBbHeight() / 2);
                    damage(arrow, target, owner, "火", .5);
                }
            }
        }

        private static void shock(SkillProjectile arrow, Entity owner) {
            ProjectileSupport.particles((ServerLevel) arrow.level(), ParticleTypes.CLOUD, arrow.position(), 40, 0, 0, 0, 1);
            explosionSound(arrow);
            for (Entity target : enemies(arrow, owner, 2.5)) {
                if (SkillEntities.movable(target)) target.setDeltaMovement(new Vec3(target.getX() - arrow.getX(), .4, target.getZ() - arrow.getZ()));
            }
        }

        private static void explosionSound(SkillProjectile arrow) {
            arrow.level().playSound(null, arrow.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 1, 1);
        }

        private static void earthArea(SkillProjectile arrow, Entity owner) {
            String mode = mode(arrow);
            if (mode.equals("坎坷路")) {
                for (Entity target : enemies(arrow, owner, 5)) {
                    if (target.onGround()) slow(target, 40, 1);
                }
            } else if (mode.equals("飞沙走石")) {
                ProjectileSupport.particles((ServerLevel) arrow.level(), ParticleTypes.CRIT, arrow.position(), 20, 2.5, 1, 2.5, 1);
                for (Entity target : enemies(arrow, owner, 2.5)) {
                    ProjectileSupport.burst(target, "smallexplosiondeeporange", target.getBbHeight() / 2);
                    damage(arrow, target, owner, "土", .4);
                }
            }
        }

        private static void slow(Entity target, int duration, int amplifier) {
            if (target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
        }

        private void rain(SkillProjectile arrow, Entity owner) {
            if (!mode(arrow).equals("箭雨") || kind != 1 && kind != 2 && kind != 8 && kind != 10) return;
            for (int i = 0; i < 20; i++) {
                Vec3 origin = arrow.position().add(arrow.level().random.nextDouble() * 12 - 6, 16, arrow.level().random.nextDouble() * 12 - 6);
                ProjectileSupport.shoot((ServerLevel) arrow.level(), owner, id(kind), origin, new Vec3(0, -1, 0), (float) arrow.getBaseDamage(), 0, 0);
            }
        }

        private static void darkCloud(SkillProjectile arrow) {
            ServerLevel level = (ServerLevel) arrow.level();
            CommandSourceStack source = level.getServer().createCommandSourceStack().withLevel(level)
                    .withPosition(arrow.position().add(0, .5, 0)).withPermission(4).withSuppressedOutput();
            // 保留来源命令：Potion 的命名空间值未加引号，SNBT 解析失败，不生成药水云。
            level.getServer().getCommands().performPrefixedCommand(source,
                    "summon minecraft:area_effect_cloud ~ ~1 ~ {CustomNameVisible:0b,Potion:minecraft:bad_luck,WaitTime:0,ReapplicationDelay:20,Age:0,"
                            + "Duration:60,DurationOnUse:100,Particle:spell,Color:5242960,Radius:2,RadiusOnUse:0,RadiusPerTick:0}");
        }
    }
}
