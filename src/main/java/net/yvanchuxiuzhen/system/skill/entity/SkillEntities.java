package net.yvanchuxiuzhen.system.skill.entity;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;

/**
 * 技能动画实体的登记与召唤。实体行为由各元素族登记到 {@link #SKILLS}（实体 ID → {@link EntitySkill}）；模型、属性取自
 * {@code content/skill/entity_models.json}；只有台账中已登记的实体才会进入内容表。
 */
public final class SkillEntities {
    private static final Map<String, EntitySkill> SKILLS = new HashMap<>();

    static {
        FireEntities.register(SKILLS);
        GoldEntities.register(SKILLS);
        IceEntities.register(SKILLS);
        LightEntities.register(SKILLS);
        LingwenEntities.register(SKILLS);
        RockEntities.register(SKILLS);
        SwordEntities.register(SKILLS);
        ThunderEntities.register(SKILLS);
        TreeEntities.register(SKILLS);
        WaterEntities.register(SKILLS);
        WaterFormations.register(SKILLS);
        WindEntities.register(SKILLS);
        Corpses.register();
    }

    private SkillEntities() {
    }

    public static Map<String, EntityModelRow> models() {
        Map<String, EntityModelRow> rows = new HashMap<>();
        for (EntityModelRow row : ContentFiles.read("content/skill/entity_models.json", EntityModelRow.CODEC)) {
            rows.put(row.id(), row);
        }
        return rows;
    }

    public static void register(ContentTables tables) {
        Map<String, EntityModelRow> models = models();
        for (EntityModelRow row : models.values()) {
            EntitySkill skill = SKILLS.get(row.id());
            if (skill != null) {
                tables.<SkillEntity>livingEntity(row.id(), (type, level) -> new SkillEntity(type, level, row, skill), row::attributes);
            }
        }
        EntityModelRow bird = models.get(FireBird.ID);
        tables.<FireBird>livingEntity(FireBird.ID, (type, level) -> new FireBird(type, level, bird), () -> FireBird.attributes(bird));
        EntityModelRow corpse = models.get(Corpse.ID);
        tables.<Corpse>livingEntity(Corpse.ID, (type, level) -> new Corpse(type, level, corpse), corpse::attributes);
        EntityModelRow rift = models.get(VoidRift.ID);
        tables.<VoidRift>livingEntity(VoidRift.ID, (type, level) -> new VoidRift(type, level, rift), () -> VoidRift.attributes(rift));
        EntityModelRow area = models.get(DamageArea.ID);
        tables.<DamageArea>livingEntity(DamageArea.ID, (type, level) -> new DamageArea(type, level, area), () -> DamageArea.attributes(area));
        EntityModelRow yuanying = models.get(Yuanying.ID);
        tables.<Yuanying>livingEntity(Yuanying.ID, (type, level) -> new Yuanying(type, level, yuanying), () -> Yuanying.attributes(yuanying));
    }

    /** 以施放者所选技能的攻击量、伤害倍率与名字召唤（尚未放入世界）；实体继承施放者的团队与友方集合。 */
    public static SkillEntity summon(ServerPlayer caster, SkillBook book, String id, String damageType) {
        return summon((ServerLevel) caster.level(), caster, id, damageType, book.manaAttack(), book.damageMultiplier(), book.name());
    }

    public static SkillEntity summon(ServerLevel level, Entity owner, String id, String damageType, double attackDamage, double multiplier, String skillName) {
        SkillEntity entity = SkillSetup.<SkillEntity>entityType(id).get().create(level);
        entity.configure(owner, attackDamage, multiplier, damageType, skillName);
        Relations.copyAllegiance(owner, entity);
        return entity;
    }

    /** 由 {@code parent} 召唤同一攻击者的后续实体（继承 {@code parent} 的团队与友方集合，类别与技能名同 {@code parent}）。 */
    public static SkillEntity chain(SkillEntity parent, String id, double attackDamage) {
        SkillEntity entity = SkillSetup.<SkillEntity>entityType(id).get().create((ServerLevel) parent.level());
        entity.configure(parent.attacker(), attackDamage, parent.multiplier(), parent.damageType(), parent.skillName());
        Relations.copyAllegiance(parent, entity);
        return entity;
    }

    /** 放入世界；{@code initialize} 为真时先走一遍初始生成（来源 {@code finalizeSpawn}，触发实体的初始生成过程）。 */
    public static void spawn(ServerLevel level, SkillEntity entity, boolean initialize) {
        if (initialize) {
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        }
        level.addFreshEntity(entity);
    }

    /** 实体对 {@code other} 造成自己记下的类别、攻击量与倍率的灵力伤害；{@code other} 不是生物时没有效果。 */
    public static void strike(SkillEntity self, Entity other) {
        self.cast().strike((ServerLevel) self.level(), other);
    }

    /** 同 {@link #strike}，攻击量与倍率先取整（来源经 {@code DecimalFormat("0")} 拼进命令的调用）。 */
    public static void strikeRounded(SkillEntity self, Entity other) {
        self.cast().strikeRounded((ServerLevel) self.level(), other);
    }

    /** 实体是不是登记为 {@code id} 的技能实体。 */
    public static boolean is(Entity entity, String id) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals(id);
    }

    /** 没有霸体状态的实体才会被技能的抛起、牵引、击飞改变速度；非生物（掉落物等）总会被改变。 */
    public static boolean movable(Entity entity) {
        return !(entity instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"));
    }

    /** 设置朝向并同步身体、头部与上一刻的朝向，使转向不被插值抹平（来源的“转向”样板）。 */
    public static void turn(Entity entity, float yaw, float pitch) {
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.setYBodyRot(yaw);
        entity.setYHeadRot(yaw);
        entity.yRotO = yaw;
        entity.xRotO = pitch;
        if (entity instanceof LivingEntity living) {
            living.yBodyRotO = yaw;
            living.yHeadRotO = yaw;
        }
    }

    /** 放在 {@code at}，朝向随机，随后按施放者朝向转向。 */
    public static void place(ServerLevel level, SkillEntity entity, Vec3 at, Entity caster, float pitch) {
        entity.moveTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360F, 0);
        turn(entity, caster.getYRot(), pitch);
    }
}
