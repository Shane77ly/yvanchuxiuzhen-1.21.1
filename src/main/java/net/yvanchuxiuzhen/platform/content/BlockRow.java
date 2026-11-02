package net.yvanchuxiuzhen.platform.content;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 方块内容表的一行（{@code content/<单位>/blocks.json}），字段取自来源生成的方块类：
 * 构造器的 Properties 链、状态属性与放置规则、形状、光照、可燃、挖掘判定与回退掉落。
 * 各分组在 JSON 中平铺为同一个对象；所有字段缺省即来源未设置。
 */
public record BlockRow(Identity identity, Physics physics, State state, Behavior behavior) {
    public static final Codec<BlockRow> CODEC = RecordCodecBuilder.<BlockRow>mapCodec(instance -> instance.group(
            Identity.MAP_CODEC.forGetter(BlockRow::identity),
            Physics.MAP_CODEC.forGetter(BlockRow::physics),
            State.MAP_CODEC.forGetter(BlockRow::state),
            Behavior.MAP_CODEC.forGetter(BlockRow::behavior)
    ).apply(instance, BlockRow::new)).codec();

    public String id() {
        return identity.id();
    }

    /** 方块身份与外观：路径、工厂类型、1.18.2 材质与地图颜色、音效。 */
    public record Identity(String id, String factory, String material, Optional<String> mapColor,
                           Either<String, Map<String, String>> sound) {
        static final MapCodec<Identity> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Identity::id),
                Codec.STRING.fieldOf("factory").forGetter(Identity::factory),
                Codec.STRING.fieldOf("material").forGetter(Identity::material),
                Codec.STRING.optionalFieldOf("map_color").forGetter(Identity::mapColor),
                Codec.either(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.STRING)).fieldOf("sound").forGetter(Identity::sound)
        ).apply(instance, Identity::new));
    }

    /** Properties 链上的物理与渲染属性。 */
    public record Physics(Optional<List<Float>> strength, Optional<Boolean> instabreak, Optional<Boolean> noOcclusion,
                          Optional<Boolean> noCollision, Optional<Boolean> randomTicks, Optional<Boolean> requiresTool,
                          Optional<Boolean> dynamicShape, Optional<Boolean> postProcess, Optional<Boolean> emissive,
                          Optional<Boolean> noDrops, Optional<Boolean> redstoneConductor, Optional<Integer> light,
                          Optional<Float> speedFactor, Optional<Float> jumpFactor, Optional<Float> friction) {
        static final MapCodec<Physics> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.listOf().optionalFieldOf("strength").forGetter(Physics::strength),
                Codec.BOOL.optionalFieldOf("instabreak").forGetter(Physics::instabreak),
                Codec.BOOL.optionalFieldOf("no_occlusion").forGetter(Physics::noOcclusion),
                Codec.BOOL.optionalFieldOf("no_collision").forGetter(Physics::noCollision),
                Codec.BOOL.optionalFieldOf("random_ticks").forGetter(Physics::randomTicks),
                Codec.BOOL.optionalFieldOf("requires_tool").forGetter(Physics::requiresTool),
                Codec.BOOL.optionalFieldOf("dynamic_shape").forGetter(Physics::dynamicShape),
                Codec.BOOL.optionalFieldOf("post_process").forGetter(Physics::postProcess),
                Codec.BOOL.optionalFieldOf("emissive").forGetter(Physics::emissive),
                Codec.BOOL.optionalFieldOf("no_drops").forGetter(Physics::noDrops),
                Codec.BOOL.optionalFieldOf("redstone_conductor").forGetter(Physics::redstoneConductor),
                Codec.INT.optionalFieldOf("light").forGetter(Physics::light),
                Codec.FLOAT.optionalFieldOf("speed_factor").forGetter(Physics::speedFactor),
                Codec.FLOAT.optionalFieldOf("jump_factor").forGetter(Physics::jumpFactor),
                Codec.FLOAT.optionalFieldOf("friction").forGetter(Physics::friction)
        ).apply(instance, Physics::new));
    }

    /** 状态属性、放置规则与形状；shape 的键为 default 与方向或轴的小写名。 */
    public record State(List<String> states, Optional<String> placement, Optional<Boolean> waterlogged,
                        Optional<Boolean> facingAll, Optional<Map<String, ShapeNode>> shape,
                        Optional<Boolean> emptyVisualShape, Optional<Integer> lightBlock, Optional<String> skylight,
                        Optional<String> pushReaction, Optional<String> offset, Optional<Boolean> shapeOffset,
                        Optional<Boolean> rotate) {
        static final MapCodec<State> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("states").forGetter(State::states),
                Codec.STRING.optionalFieldOf("placement").forGetter(State::placement),
                Codec.BOOL.optionalFieldOf("waterlogged").forGetter(State::waterlogged),
                Codec.BOOL.optionalFieldOf("facing_all").forGetter(State::facingAll),
                Codec.unboundedMap(Codec.STRING, ShapeNode.CODEC).optionalFieldOf("shape").forGetter(State::shape),
                Codec.BOOL.optionalFieldOf("empty_visual_shape").forGetter(State::emptyVisualShape),
                Codec.INT.optionalFieldOf("light_block").forGetter(State::lightBlock),
                Codec.STRING.optionalFieldOf("skylight").forGetter(State::skylight),
                Codec.STRING.optionalFieldOf("push_reaction").forGetter(State::pushReaction),
                Codec.STRING.optionalFieldOf("offset").forGetter(State::offset),
                Codec.BOOL.optionalFieldOf("shape_offset").forGetter(State::shapeOffset),
                Codec.BOOL.optionalFieldOf("rotate").forGetter(State::rotate)
        ).apply(instance, State::new));
    }

    /** 挖掘判定：手持 tool 类工具且其等级不低于 level。 */
    public record Harvest(String tool, int level) {
        static final Codec<Harvest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("tool").forGetter(Harvest::tool),
                Codec.INT.fieldOf("level").forGetter(Harvest::level)
        ).apply(instance, Harvest::new));
    }

    /** 数据掉落表为空时的回退掉落；item 为 self 表示方块自身。 */
    public record Drop(String item, int count) {
        static final Codec<Drop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(Drop::item),
                Codec.INT.fieldOf("count").forGetter(Drop::count)
        ).apply(instance, Drop::new));
    }

    /**
     * 可燃、挖掘、掉落、花的炖菜效果与可种植的地面（ground 为空表示沿用 BushBlock 的泥土类判定）；
     * plant_type 与 pending_hooks 只记录，B1 不实现。
     */
    public record Behavior(Optional<Integer> flammability, Optional<Integer> fireSpread, Optional<Harvest> harvest,
                           Optional<Float> enchantPower, Optional<Drop> fallbackDrop,
                           Optional<Boolean> doubleSlabDropsTwo, Optional<Boolean> dropLowerOnly,
                           Optional<String> effect, Optional<Float> effectSeconds, Optional<List<String>> ground,
                           Optional<String> plantType, Optional<List<String>> pendingHooks) {
        static final MapCodec<Behavior> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.optionalFieldOf("flammability").forGetter(Behavior::flammability),
                Codec.INT.optionalFieldOf("fire_spread").forGetter(Behavior::fireSpread),
                Harvest.CODEC.optionalFieldOf("harvest").forGetter(Behavior::harvest),
                Codec.FLOAT.optionalFieldOf("enchant_power").forGetter(Behavior::enchantPower),
                Drop.CODEC.optionalFieldOf("fallback_drop").forGetter(Behavior::fallbackDrop),
                Codec.BOOL.optionalFieldOf("double_slab_drops_two").forGetter(Behavior::doubleSlabDropsTwo),
                Codec.BOOL.optionalFieldOf("drop_lower_only").forGetter(Behavior::dropLowerOnly),
                Codec.STRING.optionalFieldOf("effect").forGetter(Behavior::effect),
                Codec.FLOAT.optionalFieldOf("effect_seconds").forGetter(Behavior::effectSeconds),
                Codec.STRING.listOf().optionalFieldOf("ground").forGetter(Behavior::ground),
                Codec.STRING.optionalFieldOf("plant_type").forGetter(Behavior::plantType),
                Codec.STRING.listOf().optionalFieldOf("pending_hooks").forGetter(Behavior::pendingHooks)
        ).apply(instance, Behavior::new));
    }
}
