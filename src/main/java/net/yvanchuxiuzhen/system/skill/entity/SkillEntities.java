package net.yvanchuxiuzhen.system.skill.entity;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/**
 * 技能动画实体的登记与召唤。实体行为由各元素族登记到 {@link #SKILLS}（实体 ID → {@link EntitySkill}）；模型、属性取自
 * {@code content/skill/entity_models.json}；只有台账中已登记的实体才会进入内容表。
 */
public final class SkillEntities {
    private static final Map<String, EntitySkill> SKILLS = new HashMap<>();

    static {
        FireEntities.register(SKILLS);
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
        for (EntityModelRow row : models().values()) {
            EntitySkill skill = SKILLS.get(row.id());
            if (skill != null) {
                tables.<SkillEntity>livingEntity(row.id(), (type, level) -> new SkillEntity(type, level, row, skill), () -> SkillEntity.createAttributes(row));
            }
        }
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

    /** 放入世界；{@code initialize} 为真时先走一遍初始生成（来源 {@code finalizeSpawn}，触发实体的初始生成过程）。 */
    public static void spawn(ServerLevel level, SkillEntity entity, boolean initialize) {
        if (initialize) {
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        }
        level.addFreshEntity(entity);
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
