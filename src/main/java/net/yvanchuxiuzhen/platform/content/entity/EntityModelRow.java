package net.yvanchuxiuzhen.platform.content.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 动画实体的表现与属性（各系统的 {@code entity_models.json}，由 {@code extract_entity_models.py} 从来源的
 * Model、Layer、Renderer 与 Entity 类提取）：GeckoLib 几何与动画文件名（不含后缀）、阴影半径、默认纹理、头部骨骼、发光层纹理、
 * 默认循环动画与离地时的循环动画、生物属性、是否无 AI，以及来源的导航、重力、伤害免疫和死亡移除时序。
 */
public record EntityModelRow(String id, String geo, String animation, float shadow, String texture, Optional<String> head, Optional<String> glow, String idle,
                             Optional<String> air, double speed, double health, double armor, double attack, double follow, boolean noAi, Lifecycle lifecycle) {
    public static final Codec<EntityModelRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(EntityModelRow::id),
            Codec.STRING.fieldOf("geo").forGetter(EntityModelRow::geo),
            Codec.STRING.fieldOf("animation").forGetter(EntityModelRow::animation),
            Codec.FLOAT.fieldOf("shadow").forGetter(EntityModelRow::shadow),
            Codec.STRING.fieldOf("texture").forGetter(EntityModelRow::texture),
            Codec.STRING.optionalFieldOf("head").forGetter(EntityModelRow::head),
            Codec.STRING.optionalFieldOf("glow").forGetter(EntityModelRow::glow),
            Codec.STRING.fieldOf("idle").forGetter(EntityModelRow::idle),
            Codec.STRING.optionalFieldOf("air").forGetter(EntityModelRow::air),
            Codec.DOUBLE.fieldOf("speed").forGetter(EntityModelRow::speed),
            Codec.DOUBLE.fieldOf("health").forGetter(EntityModelRow::health),
            Codec.DOUBLE.fieldOf("armor").forGetter(EntityModelRow::armor),
            Codec.DOUBLE.fieldOf("attack").forGetter(EntityModelRow::attack),
            Codec.DOUBLE.fieldOf("follow").forGetter(EntityModelRow::follow),
            Codec.BOOL.fieldOf("no_ai").forGetter(EntityModelRow::noAi),
            Lifecycle.CODEC.fieldOf("lifecycle").forGetter(EntityModelRow::lifecycle)
    ).apply(instance, EntityModelRow::new));

    /** 默认循环动画：有离地动画的实体离地时用它。 */
    public String loop(boolean onGround) {
        return air.isPresent() && !onGround ? air.get() : idle;
    }

    /** 生物属性：移速、生命、护甲、攻击、跟踪范围。 */
    public AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, speed).add(Attributes.MAX_HEALTH, health).add(Attributes.ARMOR, armor)
                .add(Attributes.ATTACK_DAMAGE, attack).add(Attributes.FOLLOW_RANGE, follow);
    }

    /** 来源实体类的运动、伤害免疫与死亡配置；零刻死亡条件按来源保留。 */
    public record Lifecycle(Navigation navigation, boolean noGravity, Optional<Double> flyingSpeed, Optional<Double> swimSpeed,
                            boolean breathesUnderwater, boolean pushedByFluid, int deathTicks, boolean ignoresFallDamage,
                            List<DamageImmunity> damageImmunities) {
        public static final Codec<Lifecycle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Navigation.CODEC.fieldOf("navigation").forGetter(Lifecycle::navigation),
                Codec.BOOL.fieldOf("no_gravity").forGetter(Lifecycle::noGravity),
                Codec.DOUBLE.optionalFieldOf("flying_speed").forGetter(Lifecycle::flyingSpeed),
                Codec.DOUBLE.optionalFieldOf("swim_speed").forGetter(Lifecycle::swimSpeed),
                Codec.BOOL.fieldOf("breathes_underwater").forGetter(Lifecycle::breathesUnderwater),
                Codec.BOOL.fieldOf("pushed_by_fluid").forGetter(Lifecycle::pushedByFluid),
                Codec.INT.fieldOf("death_ticks").forGetter(Lifecycle::deathTicks),
                Codec.BOOL.fieldOf("ignores_fall_damage").forGetter(Lifecycle::ignoresFallDamage),
                DamageImmunity.CODEC.listOf().fieldOf("damage_immunities").forGetter(Lifecycle::damageImmunities)
        ).apply(instance, Lifecycle::new));
    }

    /** 来源 hurt 方法明确拒绝的伤害条件；直接实体与伤害类型分别保留。 */
    public enum DamageImmunity implements StringRepresentable {
        ARROW("arrow"), PLAYER("player"), POTION("potion"), AREA_EFFECT_CLOUD("area_effect_cloud"),
        FALL("fall"), CACTUS("cactus"), DROWN("drown"), LIGHTNING("lightning"), EXPLOSION("explosion"),
        TRIDENT("trident"), ANVIL("anvil"), DRAGON_BREATH("dragon_breath"), WITHER("wither"), WITHER_SKULL("wither_skull");

        public static final Codec<DamageImmunity> CODEC = StringRepresentable.fromEnum(DamageImmunity::values);
        private final String name;

        DamageImmunity(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Navigation implements StringRepresentable {
        GROUND("ground"), WATER("water"), FLYING("flying");

        public static final Codec<Navigation> CODEC = StringRepresentable.fromEnum(Navigation::values);
        private final String name;

        Navigation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
