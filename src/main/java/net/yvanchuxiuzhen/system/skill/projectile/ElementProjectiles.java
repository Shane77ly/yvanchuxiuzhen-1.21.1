package net.yvanchuxiuzhen.system.skill.projectile;

import com.mojang.util.UndashedUuid;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 五行与光、风、冰的技能弹体（来源 {@code SkillGold01}、{@code SkillWater03/05/0101}、{@code SkillIce01–03}、{@code SkillTree01}、
 * {@code Skillwind01}、{@code SkillFire01}、{@code SkillLight01/02/04}、{@code SkillRock01} 的弹体过程与 {@code Jinenglist01} 中对应的施放），以及灵纹弹 {@link LingwenBolt}（由灵纹特效实体射出，没有施放入口）。
 * 原版与自定义粒子均按来源过程的条件、顺序和参数由服务端发送。
 */
public final class ElementProjectiles {
    private static final Map<String, ProjectileSkill> SKILLS = Map.ofEntries(
                Map.entry("projectile_skill_gold_01", new GoldBolt()),
                Map.entry("projectile_skill_gold_03", new GoldWheel()),
                Map.entry("projectile_skill_water_03", new WaterOrb()),
                Map.entry("projectile_skill_water_05", new WaterSpirit()),
                Map.entry("projectile_skill_water_0101", new WaterSeeker()),
                Map.entry("projectile_skill_ice_01", new IceCone()),
                Map.entry("projectile_skill_ice_02", new IceLance()),
                Map.entry("projectile_skill_ice_03", new IceFlower()),
                Map.entry("projectile_skill_tree_01", new TreeLeaf()),
                Map.entry("projectile_skillwind_01", new WindBullet()),
                Map.entry("projectile_skill_fire_01", new Fireball()),
                Map.entry("projectile_skill_light_01", new LightBullet()),
                Map.entry("projectile_skill_light_02", new SoulLight()),
                Map.entry("projectile_skill_light_04", new PhantomLight()),
                Map.entry("projectile_skill_rock_01", new FlyingStone()),
                Map.entry("projectile_lingwentexiao", new LingwenBolt()));

    private ElementProjectiles() {
    }

    /** 本类登记的弹体实体 ID，客户端为它们登记渲染。 */
    public static Set<String> ids() {
        return SKILLS.keySet();
    }

    public static void register(ContentTables tables) {
        SKILLS.forEach((id, skill) -> tables.<SkillProjectile>entity(id, (type, level) -> new SkillProjectile(type, level, skill, icon(id))));
        release("金", "projectile_skill_gold_01", 1, 0, 5, "金芒术", "碎裂金芒");
        release("金", "projectile_skill_gold_03", 1, 0, 0, "捆妖轮", "金弧轮", "天堑金轮");
        release("水", "projectile_skill_water_03", 1, 1, 0, "水球术");
        release("水", "projectile_skill_water_05", 1, 0, 0, "水灵弹");
        release("水", "projectile_skill_ice_01", 1, 0, 0, "冰锥术", "连锥术");
        release("水", "projectile_skill_ice_02", 1, 1, 0, "破碎冰矛");
        release("水", "projectile_skill_ice_03", 1, 1, 0, "飞羽冰花");
        release("木", "projectile_skill_tree_01", 1, 1, 0, "拈花飞叶");
        SkillReleases.register("木", (caster, book) -> {
            double angle = caster.getYRot() + 90 - 30;
            double vertical = 5 * Math.sin(-caster.getXRot() * (Math.PI / 180));
            for (int index = 0; index < 5; index++) {
                double cosine = Math.cos(angle * (Math.PI / 180));
                double sine = Math.sin(angle * (Math.PI / 180));
                Vec3 origin = caster.position().add(cosine, 1.5, sine);
                Vec3 direction = new Vec3(3 * cosine, vertical, 3 * sine);
                ProjectileSupport.shoot(caster.serverLevel(), caster, "projectile_skill_tree_01", origin, direction, 2, 1, 0).setSilent(true);
                angle += 15;
            }
        }, "满天花雨");
        release("木", "projectile_skillwind_01", 0.7F, 2, 0, "风弹术");
        release("火", "projectile_skill_fire_01", 1, 2, 0, "火球术", "连环火球术");
        release("火", "projectile_skill_light_01", 1, 0, 10, "光弹术");
        release("火", "projectile_skill_light_02", 3, 0, 0, "定魄神光");
        release("火", "projectile_skill_light_04", 0.2F, 0, 2, "幻光术");
        SkillReleases.register("木", (caster, book) -> {
            caster.addEffect(new MobEffectInstance(StatusEffects.holder("skill_tree_03"), 600, 1));
            ProjectileSupport.particles(caster.serverLevel(), Particles.type("peachflowerlizi"), caster.position().add(0, 1, 0), 20, 0, 0, 0, 0.1);
            caster.displayClientMessage(Component.literal("§630秒内，拈花飞叶技能自动追踪敌方实体！"), false);
        }, "生命感知");
        SkillReleases.register("土", (caster, book) -> ProjectileSupport.shootFromEyes((ServerLevel) caster.level(), caster, "projectile_skill_rock_01",
                (float) book.manaAttack(), 1, 0, 1.2F), "飞石术");
    }

    /** 来源 ItemSupplier 的实际显示物品；专用模型及两种纯粒子弹体返回空栈。 */
    private static ItemStack icon(String id) {
        return switch (id) {
            case "projectile_skill_ice_01", "projectile_skill_ice_02", "projectile_skill_ice_03",
                    "projectile_lingwentexiao", "projectile_skill_gold_03", "projectile_skill_water_0101" -> ItemStack.EMPTY;
            case "projectile_skill_tree_01" -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_087")));
            default -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id.substring("projectile_".length()))));
        };
    }

    /** 施放：从眼睛下 0.1 格朝视线发射一枚基础伤害 0.1 的弹体（来源 {@code Jinenglist01}）。 */
    private static void release(String body, String id, float speed, int knockback, int pierce, String... names) {
        SkillReleases.register(body, (caster, book) -> ProjectileSupport.shootFromEyes((ServerLevel) caster.level(), caster, id, 0.1F, knockback, pierce, speed), names);
    }

    private static boolean hostile(Entity owner, Entity other) {
        return !ProjectileSupport.friendly(owner, other);
    }

    /** 弹体计时加一，到达寿命时消失。 */
    private static void expire(SkillProjectile projectile, int lifetime) {
        if (projectile.advance() >= lifetime) {
            projectile.discard();
        }
    }

    /** 向目标加速：速度取 (目标位置 − 弹体位置) × 系数。 */
    private static void homeIn(SkillProjectile projectile, Vec3 target, double factor) {
        projectile.setDeltaMovement(target.subtract(projectile.position()).scale(factor));
    }

    private static void burst(Entity at, ParticleOptions particle, int count, double spread, double speed, double lift) {
        ProjectileSupport.particles((ServerLevel) at.level(), particle, at.position().add(0, lift, 0), count, spread, spread, spread, speed);
    }

    /** 金芒术与碎裂金芒：碎裂金芒第 4 刻起朝 4 格内最近的敌方生物追踪，7 刻消失；命中读发射者当前所选技能。 */
    private static final class GoldBolt implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            ProjectileSupport.burst(projectile, "skill_gold_0101");
            if (projectile.begin()) {
                boolean shattering = owner instanceof Player player && Skills.book(Skills.selected(player)).filter(book -> book.name().equals("碎裂金芒")).isPresent();
                projectile.setSkillName(shattering ? "碎裂金芒" : "金芒术");
            }
            int ticks = projectile.advance();
            if (ticks >= 7) {
                projectile.discard();
            } else if (projectile.skillName().equals("碎裂金芒") && ticks >= 4) {
                for (Entity other : ProjectileSupport.around((ServerLevel) projectile.level(), projectile.position(), 4)) {
                    if (hostile(owner, other) && other instanceof Mob) {
                        homeIn(projectile, other.position().add(0, 1, 0), 0.4);
                        break;
                    }
                }
            }
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            if (hostile(owner, target)) {
                double[] live = ProjectileSupport.liveValues(owner);
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "金", live[0], live[1]);
            }
        }
    }

    /** 水球术：7 刻消失，命中溅水并造成水系伤害。 */
    private static final class WaterOrb implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            burst(projectile, ParticleTypes.SPLASH, 5, 0.2, 0, 0.4);
            expire(projectile, 7);
            if (projectile.begin()) {
                projectile.snapshot(owner, 1, 1, 1);
            }
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            burst(projectile, ParticleTypes.SPLASH, 10, 0, 0.5, 0);
            ProjectileSupport.burst(projectile, "smallexplosionblue");
            if (hostile(owner, target)) {
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "水", projectile.mana(), projectile.multiplier());
            }
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            burst(projectile, ParticleTypes.SPLASH, 10, 0, 0.5, 0);
            ProjectileSupport.burst(projectile, "smallexplosionblue");
        }
    }

    /**
     * 水灵弹与水灵阵放出的水弹：水灵弹第 7 刻起追踪 4.5 格内最近的敌方玩家或生物，20 刻消失；其他名字（水灵阵）全程追踪，
     * 目标不含水灵阵的法阵实体，7 刻消失。追踪高度读发射者的碰撞箱高度（来源如此）。
     */
    private static final class WaterSpirit implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            burst(projectile, ParticleTypes.BUBBLE, 5, 0.2, 0, 0.4);
            if (projectile.begin() && owner instanceof Player) {
                projectile.snapshot(owner, 1, 1, 1);
            }
            boolean spirit = projectile.skillName().equals("水灵弹");
            int ticks;
            if (spirit) {
                ticks = projectile.advance();
                if (ticks >= 20) {
                    projectile.discard();
                    return;
                }
                if (ticks < 7) {
                    return;
                }
            }
            for (Entity other : ProjectileSupport.around((ServerLevel) projectile.level(), projectile.position(), 4.5)) {
                boolean formation = BuiltInRegistries.ENTITY_TYPE.getKey(other.getType()).getPath().equals("skill_water_06");
                if (hostile(owner, other) && (spirit || !formation) && (other instanceof Player || other instanceof Mob)) {
                    projectile.setDeltaMovement(new Vec3((other.getX() - projectile.getX()) * 0.2, (other.getY() + owner.getBbHeight() / 2 - projectile.getY()) * 0.2,
                            (other.getZ() - projectile.getZ()) * 0.2));
                    break;
                }
            }
            if (!spirit) {
                expire(projectile, 7);
            }
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            burst(projectile, ParticleTypes.SPLASH, 10, 0, 0.5, 0);
            ProjectileSupport.burst(projectile, "smallexplosionlightblue");
            if (hostile(owner, target)) {
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "水", projectile.mana(), projectile.multiplier());
            }
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            burst(projectile, ParticleTypes.SPLASH, 10, 0, 0.5, 0);
            ProjectileSupport.burst(projectile, "smallexplosionlightblue");
        }
    }

    /** 水系法阵放出的小水弹：攻击量乘 0.5、倍率乘 0.2；第 5 刻起追踪发射者玩家体质里记下的目标，30 刻消失。 */
    private static final class WaterSeeker implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            burst(projectile, ParticleTypes.BUBBLE_POP, 2, 0, 0, 0);
            if (projectile.begin()) {
                projectile.snapshot(owner, 0.5, 0.2, 0.2);
            }
            int ticks = projectile.advance();
            if (ticks >= 30) {
                projectile.discard();
            } else if (ticks >= 5 && owner instanceof Player player) {
                String target = Cultivation.tizhiString(player, "mubiaouid");
                UUID targetId;
                try {
                    targetId = UndashedUuid.fromStringLenient(target);
                } catch (IllegalArgumentException exception) {
                    // 来源对无效目标 UUID 保持当前飞行速度。
                    return;
                }
                Entity found = ((ServerLevel) projectile.level()).getEntity(targetId);
                if (found != null) {
                    homeIn(projectile, found.position(), 0.4);
                }
            }
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            if (hostile(owner, target)) {
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "水", projectile.mana(), projectile.multiplier());
            }
        }
    }

    /** 冰锥术与连锥术：20 刻消失，命中破冰、去除霸体、冰系伤害并叠加 40 刻冷冻。 */
    private static final class IceCone implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            burst(projectile, Particles.type("flash_ice"), 2, 0.2, 0, 0.3);
            if (projectile.begin()) {
                projectile.snapshot(owner, 1, 1, 1);
            }
            expire(projectile, 20);
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            ProjectileSupport.breakEffect((ServerLevel) projectile.level(), projectile.position(), Blocks.ICE.defaultBlockState());
            freeze(projectile, target, owner, 40);
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            ProjectileSupport.breakEffect((ServerLevel) projectile.level(), projectile.position(), Blocks.ICE.defaultBlockState());
        }
    }

    /** 敌方目标：去除霸体（只有冰锥术）、冰系伤害、叠加冷冻刻数。 */
    private static void freeze(SkillProjectile projectile, Entity target, Entity owner, int ticks) {
        if (hostile(owner, target)) {
            ProjectileSupport.burst(projectile, "smallexplosionskyblue");
            if (target instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08")) {
                StatusEffects.remove(living, "skilleffect_08");
            }
            ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "冰", projectile.mana(), projectile.multiplier());
            target.setTicksFrozen(target.getTicksFrozen() + ticks);
        }
    }

    /** 破碎冰矛：飞行同冰锥术；命中冰系伤害、冷冻 120 刻，并在命中处向四周射出 6 枚冰锥；打到方块同样射出冰锥。 */
    private static final class IceLance implements ProjectileSkill {
        private final IceCone flight = new IceCone();

        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            flight.tick(projectile, owner);
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            ServerLevel level = (ServerLevel) projectile.level();
            ProjectileSupport.breakEffect(level, projectile.position(), Blocks.ICE.defaultBlockState());
            if (hostile(owner, target)) {
                ProjectileSupport.burst(projectile, "smallexplosionskyblue");
                ProjectileSupport.deal(level, target, owner, "冰", projectile.mana(), projectile.multiplier());
                target.setTicksFrozen(target.getTicksFrozen() + 120);
                scatter(level, owner, target.position());
            }
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            Entity owner = projectile.getOwner();
            if (owner == null) {
                return;
            }
            ServerLevel level = (ServerLevel) projectile.level();
            ProjectileSupport.breakEffect(level, projectile.position(), Blocks.ICE.defaultBlockState());
            scatter(level, owner, projectile.position());
        }

        private static void scatter(ServerLevel level, Entity owner, Vec3 at) {
            for (int index = 0; index < 6; index++) {
                Vec3 direction = new Vec3(Mth.nextDouble(level.random, -5, 5), 0, Mth.nextDouble(level.random, -5, 5));
                ProjectileSupport.shoot(level, owner, "projectile_skill_ice_01", at, direction, 0.1F, 0, 0);
            }
        }
    }

    /** 飞羽冰花：绕发射者视线两侧的圆弧飞行，每刻转 20 度，10 刻消失；命中冰系伤害、冷冻 80 刻。 */
    private static final class IceFlower implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            if (projectile.begin()) {
                projectile.setReversed(owner.isShiftKeyDown());
                projectile.setAngle(owner.getYRot() + (owner.isShiftKeyDown() ? 90 : -90));
                projectile.snapshot(owner, 1, 1, 1);
            }
            expire(projectile, 20);
            projectile.advance();
            projectile.setAngle(projectile.angle() + (projectile.reversed() ? -20 : 20));
            double radians = Math.toRadians(projectile.angle());
            projectile.setDeltaMovement(new Vec3(-Math.sin(radians), -Math.sin(Math.toRadians(owner.getXRot())), Math.cos(radians)));
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            ProjectileSupport.breakEffect((ServerLevel) projectile.level(), projectile.position(), Blocks.ICE.defaultBlockState());
            if (hostile(owner, target)) {
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "冰", projectile.mana(), projectile.multiplier());
                target.setTicksFrozen(target.getTicksFrozen() + 80);
            }
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            ProjectileSupport.breakEffect((ServerLevel) projectile.level(), projectile.position(), Blocks.ICE.defaultBlockState());
        }
    }

    /**
     * 拈花飞叶：10 刻消失；发射者带“生命感知”（{@code skill_tree_03}）时第 4 刻起追踪 3 格内最近的敌方玩家或生物。
     * {@code AttackDamage} 与 {@code Damage_multiplier} 只在到期时从所选书写入；攻击量键为 {@code ManaAttackDamage}。
     * 常规技能书不生产该键，正常飞行期间也未写入这两个值；命中仍按弹体实际保存的值请求木系伤害（Q40）。
     */
    private static final class TreeLeaf implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            projectile.begin();
            int ticks = projectile.advance();
            if (ticks >= 10) {
                SkillBook book = owner instanceof Player player ? Skills.book(Skills.selected(player)).orElse(null) : null;
                projectile.getPersistentData().putDouble("AttackDamage", book == null ? 0 : book.number("ManaAttackDamage"));
                projectile.getPersistentData().putDouble("Damage_multiplier", book == null ? 0 : book.damageMultiplier());
                projectile.discard();
            } else if (ticks >= 4 && owner instanceof LivingEntity living && StatusEffects.has(living, "skill_tree_03")) {
                for (Entity other : ProjectileSupport.around((ServerLevel) projectile.level(), projectile.position(), 3)) {
                    if (hostile(owner, other) && (other instanceof Player || other instanceof Mob)) {
                        homeIn(projectile, other.position(), 0.4);
                        break;
                    }
                }
            }
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            if (hostile(owner, target)) {
                ProjectileSupport.burst(projectile, "smallexplosiongreen");
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "木",
                        projectile.getPersistentData().getDouble("AttackDamage"), projectile.getPersistentData().getDouble("Damage_multiplier"));
            }
        }
    }

    /** 风弹术：10 刻消失，命中风系伤害。 */
    private static final class WindBullet implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            burst(projectile, ParticleTypes.CLOUD, 2, 0.2, 0, 0.3);
            if (projectile.begin()) {
                projectile.snapshot(owner, 1, 1, 1);
            }
            expire(projectile, 10);
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            burst(projectile, ParticleTypes.CLOUD, 5, 0, 0.1, 0);
            if (hostile(owner, target)) {
                ProjectileSupport.burst(projectile, "smallexplosionwhite");
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "风", projectile.mana(), projectile.multiplier());
            }
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            burst(projectile, ParticleTypes.CLOUD, 5, 0, 0.1, 0);
        }
    }

    /** 火球术按 2 格内非友方实体数，对直接命中目标重复请求火伤；连环火球术对非友方直接目标请求一次。两者 10 刻消失。 */
    private static final class Fireball implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            burst(projectile, ParticleTypes.FLAME, 2, 0.2, 0, 0.3);
            if (projectile.begin()) {
                projectile.snapshot(owner, 1, 1, 1);
                if (!(owner instanceof Player)) {
                    projectile.setSkillName("火球术");
                }
            }
            expire(projectile, 10);
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            ServerLevel level = (ServerLevel) projectile.level();
            burst(projectile, ParticleTypes.FLAME, 10, 0, 0.4, 0);
            if (projectile.skillName().equals("火球术")) {
                for (Entity other : ProjectileSupport.around(level, projectile.position(), 2)) {
                    if (hostile(owner, other)) {
                        ProjectileSupport.burst(projectile, "smallexplosionred");
                        ProjectileSupport.deal(level, target, owner, "火", projectile.mana(), projectile.multiplier());
                    }
                }
            } else if (hostile(owner, target)) {
                ProjectileSupport.burst(projectile, "smallexplosionred");
                ProjectileSupport.deal(level, target, owner, "火", projectile.mana(), projectile.multiplier());
            }
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            burst(projectile, ParticleTypes.FLAME, 10, 0, 0.4, 0);
        }
    }

    /** 光弹术：15 刻消失，命中光系伤害。 */
    private static class LightBullet implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            burst(projectile, ParticleTypes.GLOW, 1, 0, 0, 0.3);
            if (projectile.begin()) {
                projectile.snapshot(owner, 1, 1, 1);
            }
            expire(projectile, 15);
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            if (hostile(owner, target)) {
                ProjectileSupport.burst(projectile, "smallexplosionlightyellow");
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "光", projectile.mana(), projectile.multiplier());
            }
        }
    }

    /** 定魄神光：飞行同光弹术；命中闪光、光系伤害并施加 600 刻 20 级缓慢。 */
    private static final class SoulLight extends LightBullet {
        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            burst(projectile, ParticleTypes.FLASH, 1, 0, 0, 0);
            if (hostile(owner, target)) {
                ProjectileSupport.burst(projectile, "smallexplosionlightblue");
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "光", projectile.mana(), projectile.multiplier());
                if (target instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 20));
                }
            }
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            burst(projectile, ParticleTypes.FLASH, 1, 0, 0, 0);
        }
    }

    /** 幻光术：10 刻消失，命中光系伤害。 */
    private static final class PhantomLight implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            if (projectile.begin()) {
                projectile.snapshot(owner, 1, 1, 1);
            }
            expire(projectile, 10);
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            if (hostile(owner, target)) {
                ProjectileSupport.burst(projectile, "smallexplosionlightpurple");
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "光", projectile.mana(), projectile.multiplier());
            }
        }
    }

    /** 飞石术：保留重力走抛物线，第一刻记下数值；命中或打到方块都破碎并消失，命中时造成土系伤害。 */
    private static final class FlyingStone implements ProjectileSkill {
        @Override
        public void tick(SkillProjectile projectile, Entity owner) {
            burst(projectile, Particles.type("smokelightgray"), 2, 0.2, 0, 0.3);
            if (!projectile.saved()) {
                projectile.snapshot(owner, 1, 1, 1);
                projectile.markSaved();
            }
        }

        @Override
        public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
            ProjectileSupport.breakEffect((ServerLevel) projectile.level(), projectile.position(), Blocks.STONE.defaultBlockState());
            if (hostile(owner, target)) {
                ProjectileSupport.burst(projectile, "smallexplosionlightgray");
                ProjectileSupport.deal((ServerLevel) projectile.level(), target, owner, "土", projectile.mana(), projectile.multiplier());
            }
            projectile.discard();
        }

        @Override
        public void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
            ProjectileSupport.breakEffect((ServerLevel) projectile.level(), projectile.position(), Blocks.STONE.defaultBlockState());
            projectile.discard();
        }
    }
}
