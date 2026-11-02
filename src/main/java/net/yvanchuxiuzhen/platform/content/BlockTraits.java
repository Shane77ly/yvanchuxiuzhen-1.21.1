package net.yvanchuxiuzhen.platform.content;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.phys.Vec3;

/**
 * 一行方块内容表解析后的运行期数据：Properties、状态属性、形状与各项钩子的取值。
 * 各方块类的覆写只把调用转给这里，使 14 个工厂类共用同一份来源语义。
 */
public final class BlockTraits {
    private final BlockRow row;
    private final Map<String, VoxelShape> shapes = new HashMap<>();
    private final VoxelShape defaultShape;

    BlockTraits(BlockRow row) {
        this.row = row;
        Map<String, ShapeNode> nodes = row.state().shape().orElse(Map.of());
        nodes.forEach((key, node) -> shapes.put(key, node.toShape()));
        this.defaultShape = shapes.get("default");
    }

    public BlockRow row() {
        return row;
    }

    public boolean waterlogged() {
        return row.state().waterlogged().orElse(false);
    }

    public boolean hasFacing() {
        return row.state().states().contains("FACING");
    }

    public boolean hasAxis() {
        return row.state().states().contains("AXIS");
    }

    /** 由构造器的 Properties 链还原 1.21.1 的 Properties。 */
    public BlockBehaviour.Properties properties() {
        BlockRow.Physics physics = row.physics();
        LegacyTables.MaterialRule material = LegacyTables.MATERIALS.get(row.identity().material());
        if (material == null) {
            throw new IllegalArgumentException("未知材质 " + row.identity().material() + "：" + row.id());
        }
        MapColor color = row.identity().mapColor().map(name -> LegacyTables.MAP_COLORS.get(name)).orElse(material.color());
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(color).pushReaction(material.pushReaction());
        if (material.ignitedByLava()) {
            properties.ignitedByLava();
        }
        if (material.replaceable()) {
            properties.replaceable();
        }
        properties.sound(sound());
        physics.strength().ifPresent(strength -> properties.strength(strength.get(0), strength.get(1)));
        if (physics.instabreak().orElse(false)) {
            properties.instabreak();
        }
        if (physics.noOcclusion().orElse(false)) {
            properties.noOcclusion();
        }
        if (physics.noCollision().orElse(false)) {
            properties.noCollission();
        }
        if (physics.randomTicks().orElse(false)) {
            properties.randomTicks();
        }
        if (physics.requiresTool().orElse(false)) {
            properties.requiresCorrectToolForDrops();
        }
        if (physics.dynamicShape().orElse(false)) {
            properties.dynamicShape();
        }
        if (physics.postProcess().orElse(false)) {
            properties.hasPostProcess((state, level, pos) -> true);
        }
        if (physics.emissive().orElse(false)) {
            properties.emissiveRendering((state, level, pos) -> true);
        }
        if (physics.noDrops().orElse(false)) {
            properties.noLootTable();
        }
        if (!physics.redstoneConductor().orElse(true)) {
            properties.isRedstoneConductor((state, level, pos) -> false);
        }
        physics.light().ifPresent(light -> properties.lightLevel(state -> light));
        physics.speedFactor().ifPresent(properties::speedFactor);
        physics.jumpFactor().ifPresent(properties::jumpFactor);
        physics.friction().ifPresent(properties::friction);
        row.state().offset().ifPresent(offset -> properties.offsetType(BlockBehaviour.OffsetType.valueOf(offset)));
        return properties;
    }

    public SoundType sound() {
        return row.identity().sound().map(
                name -> {
                    SoundType type = LegacyTables.SOUND_TYPES.get(name);
                    if (type == null) {
                        throw new IllegalArgumentException("未知音效 " + name + "：" + row.id());
                    }
                    return type;
                },
                LegacyTables::customSound);
    }

    public void addStates(StateDefinition.Builder<Block, BlockState> builder) {
        for (String name : row.state().states()) {
            builder.add(switch (name) {
                case "FACING" -> facingProperty();
                case "WATERLOGGED" -> BlockStateProperties.WATERLOGGED;
                case "AXIS" -> BlockStateProperties.AXIS;
                default -> throw new IllegalArgumentException("未知状态属性 " + name + "：" + row.id());
            });
        }
    }

    private DirectionProperty facingProperty() {
        return row.state().facingAll().orElse(false) ? BlockStateProperties.FACING : BlockStateProperties.HORIZONTAL_FACING;
    }

    /** 来源的默认状态：朝北、轴向 Y、不含水。 */
    public BlockState defaultState(BlockState any) {
        if (hasFacing()) {
            any = any.setValue(facingProperty(), Direction.NORTH);
        }
        if (hasAxis()) {
            any = any.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
        }
        if (waterlogged()) {
            any = any.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return any;
    }

    public BlockState stateForPlacement(BlockState state, BlockPlaceContext context) {
        String placement = row.state().placement().orElse("");
        state = switch (placement) {
            case "horizontal" -> state.setValue(facingProperty(), context.getHorizontalDirection().getOpposite());
            case "clicked_face" -> state.setValue(facingProperty(), context.getClickedFace());
            case "look" -> state.setValue(facingProperty(), context.getNearestLookingDirection().getOpposite());
            case "axis" -> state.setValue(BlockStateProperties.AXIS, context.getClickedFace().getAxis());
            case "" -> state;
            default -> throw new IllegalArgumentException("未知放置规则 " + placement + "：" + row.id());
        };
        if (waterlogged()) {
            boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
            state = state.setValue(BlockStateProperties.WATERLOGGED, water);
        }
        return state;
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        if (!row.state().rotate().orElse(false)) {
            return state;
        }
        if (hasAxis()) {
            return switch (rotation) {
                case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (state.getValue(BlockStateProperties.AXIS)) {
                    case X -> state.setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
                    case Z -> state.setValue(BlockStateProperties.AXIS, Direction.Axis.X);
                    default -> state;
                };
                default -> state;
            };
        }
        return state.setValue(facingProperty(), rotation.rotate(state.getValue(facingProperty())));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        if (!row.state().rotate().orElse(false) || hasAxis()) {
            return state;
        }
        return state.rotate(mirror.getRotation(state.getValue(facingProperty())));
    }

    /** 有 getShape 覆写的方块返回按朝向或轴选取的形状，没有覆写的使用完整方块。 */
    public VoxelShape shape(BlockState state, BlockGetter level, BlockPos pos, VoxelShape fallback) {
        if (shapes.isEmpty()) {
            return fallback;
        }
        String key = "default";
        if (hasFacing()) {
            key = state.getValue(facingProperty()).getName();
        } else if (hasAxis()) {
            key = state.getValue(BlockStateProperties.AXIS).getName();
        }
        VoxelShape shape = shapes.getOrDefault(key, defaultShape);
        if (row.state().shapeOffset().orElse(false)) {
            Vec3 offset = state.getOffset(level, pos);
            shape = shape.move(offset.x, offset.y, offset.z);
        }
        return shape;
    }

    public boolean emptyVisualShape() {
        return row.state().emptyVisualShape().orElse(false);
    }

    public int lightBlock(int fallback) {
        return row.state().lightBlock().orElse(fallback);
    }

    /** 返回 true/false 表示来源覆写了天空光传播，null 表示未覆写。 */
    public Boolean propagatesSkylight(BlockState state) {
        return row.state().skylight().map(rule -> rule.equals("always") || state.getFluidState().isEmpty()).orElse(null);
    }

    public PushReaction pushReaction(PushReaction fallback) {
        return row.state().pushReaction().map(PushReaction::valueOf).orElse(fallback);
    }

    /** 花的炖菜效果。 */
    public Holder<MobEffect> effect() {
        ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.parse(row.behavior().effect().orElseThrow()));
        return BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(key);
    }

    public float effectSeconds() {
        return row.behavior().effectSeconds().orElseThrow();
    }

    /** 来源 mayPlaceOn 的地面列表；没有覆写时返回 null，沿用 BushBlock 的判定。 */
    public Boolean mayPlaceOn(BlockState ground) {
        return row.behavior().ground().map(ids -> ids.stream()
                .anyMatch(id -> ground.is(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id))))).orElse(null);
    }

    public int flammability() {
        return row.behavior().flammability().orElse(-1);
    }

    public int fireSpread() {
        return row.behavior().fireSpread().orElse(-1);
    }

    public float enchantPower(float fallback) {
        return row.behavior().enchantPower().orElse(fallback);
    }

    /** 来源 canHarvestBlock：手持指定类的工具且工具等级不低于要求；没有覆写时返回 null，使用默认判定。 */
    public Boolean canHarvest(Player player) {
        return row.behavior().harvest().map(rule -> {
            ItemStack held = player.getInventory().getSelected();
            boolean matches = switch (rule.tool()) {
                case "PickaxeItem" -> held.getItem() instanceof PickaxeItem;
                case "AxeItem" -> held.getItem() instanceof AxeItem;
                case "HoeItem" -> held.getItem() instanceof HoeItem;
                case "ShovelItem" -> held.getItem() instanceof ShovelItem;
                case "TieredItem" -> held.getItem() instanceof TieredItem;
                default -> throw new IllegalArgumentException("未知挖掘工具类 " + rule.tool() + "：" + row.id());
            };
            return matches && LegacyTables.toolLevel(((TieredItem) held.getItem()).getTier()) >= rule.level();
        }).orElse(null);
    }
}
