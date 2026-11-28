package net.yvanchuxiuzhen.system.zhenfa.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 阵旗物品绑定子旗列表与母旗坐标（{@code yvanchuxiuzhen:formation_flag}）。 */
public record FormationFlagData(List<BlockPos> children, Optional<BlockPos> mother) {
    public static final FormationFlagData EMPTY = new FormationFlagData(List.of(), Optional.empty());
    public static final Codec<FormationFlagData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(FormationFlagData::children),
            BlockPos.CODEC.optionalFieldOf("mother").forGetter(FormationFlagData::mother)
    ).apply(instance, FormationFlagData::new));
    public static final StreamCodec<ByteBuf, FormationFlagData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public FormationFlagData {
        children = List.copyOf(children);
    }

    public FormationFlagData addChild(BlockPos pos) {
        List<BlockPos> next = new ArrayList<>(children);
        next.add(pos.immutable());
        return new FormationFlagData(next, mother);
    }

    public FormationFlagData withMother(BlockPos pos) {
        return new FormationFlagData(children, Optional.of(pos.immutable()));
    }
}
