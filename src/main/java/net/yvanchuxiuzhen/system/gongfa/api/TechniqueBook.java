package net.yvanchuxiuzhen.system.gongfa.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 功法书实例（物品组件 {@code yvanchuxiuzhen:technique}，来源书的 NBT）：功法定义、研习者（{@code belongToMasterUUID}、
 * {@code belongToName}）、功法层数与经验（{@code ExercisesLevel}、{@code ExercisesExp}）与十三项加成数值
 * （{@code Exercises*}，顺序同 {@link TechniqueStat}）。未研习的书没有研习者，层数、经验与数值为 0。
 */
public record TechniqueBook(TechniqueDefinition definition, Optional<UUID> owner, String ownerName, double level, double exp, List<Double> values) {
    public static final Codec<TechniqueBook> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TechniqueDefinition.MAP_CODEC.forGetter(TechniqueBook::definition),
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(TechniqueBook::owner),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(TechniqueBook::ownerName),
            Codec.DOUBLE.optionalFieldOf("exercise_level", 0.0).forGetter(TechniqueBook::level),
            Codec.DOUBLE.optionalFieldOf("exercise_exp", 0.0).forGetter(TechniqueBook::exp),
            Codec.DOUBLE.listOf(TechniqueStat.values().length, TechniqueStat.values().length).optionalFieldOf("values", zeros()).forGetter(TechniqueBook::values)
    ).apply(instance, TechniqueBook::new));
    public static final StreamCodec<ByteBuf, TechniqueBook> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static TechniqueBook unstudied(TechniqueDefinition definition) {
        return new TechniqueBook(definition, Optional.empty(), "", 0, 0, zeros());
    }

    private static List<Double> zeros() {
        return Collections.nCopies(TechniqueStat.values().length, 0.0);
    }

    public double value(TechniqueStat stat) {
        return values.get(stat.ordinal());
    }

    public boolean ownedBy(UUID player) {
        return owner.filter(player::equals).isPresent();
    }

    /** 当前层数升级所需经验 = 层数² × 品阶 × 10（来源 {@code Kaishigongfa}、{@code ItemLore}）。 */
    public double expMax() {
        return Math.pow(level, 2) * definition.grade() * 10;
    }
}
