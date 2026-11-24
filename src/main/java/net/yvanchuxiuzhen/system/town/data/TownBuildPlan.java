package net.yvanchuxiuzhen.system.town.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 城镇规划卷轴携带的建筑蓝图参数（{@code yvanchuxiuzhen:town_build_plan}）。 */
public record TownBuildPlan(CompoundTag data) {
    public static final TownBuildPlan EMPTY = new TownBuildPlan(new CompoundTag());
    public static final Codec<TownBuildPlan> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(TownBuildPlan::data)
    ).apply(instance, TownBuildPlan::new));
    public static final StreamCodec<ByteBuf, TownBuildPlan> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public TownBuildPlan {
        data = data.copy();
    }

    @Override
    public CompoundTag data() {
        return data.copy();
    }
}
