package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 内容表中的碰撞箱表达式，对应来源生成代码里的 {@code box}、{@code Shapes.or}、
 * {@code Shapes.join(.., BooleanOp.ONLY_FIRST)} 与 {@code Shapes.empty}，恰有一个字段存在。
 */
public record ShapeNode(Optional<List<Double>> box, Optional<List<ShapeNode>> or, Optional<List<ShapeNode>> subtract,
                        Optional<Boolean> empty) {
    public static final Codec<ShapeNode> CODEC = Codec.recursive("ShapeNode", self -> RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.listOf().optionalFieldOf("box").forGetter(ShapeNode::box),
            self.listOf().optionalFieldOf("or").forGetter(ShapeNode::or),
            self.listOf().optionalFieldOf("subtract").forGetter(ShapeNode::subtract),
            Codec.BOOL.optionalFieldOf("empty").forGetter(ShapeNode::empty)
    ).apply(instance, ShapeNode::new)));

    public VoxelShape toShape() {
        if (box.isPresent()) {
            List<Double> b = box.get();
            return Block.box(b.get(0), b.get(1), b.get(2), b.get(3), b.get(4), b.get(5));
        }
        if (or.isPresent()) {
            VoxelShape result = Shapes.empty();
            for (ShapeNode node : or.get()) {
                result = Shapes.or(result, node.toShape());
            }
            return result;
        }
        if (subtract.isPresent()) {
            return Shapes.join(subtract.get().get(0).toShape(), subtract.get().get(1).toShape(), BooleanOp.ONLY_FIRST);
        }
        return Shapes.empty();
    }
}
