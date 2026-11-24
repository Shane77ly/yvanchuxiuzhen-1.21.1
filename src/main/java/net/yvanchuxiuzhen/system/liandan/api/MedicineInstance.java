package net.yvanchuxiuzhen.system.liandan.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 丹药瓶与炼丹中间产物的强类型药效实例组件（{@code yvanchuxiuzhen:medicine}）。 */
public record MedicineInstance(String outputItemId, String name, String description,
                               int doses, boolean treasure, List<Effect> effects) {
    public static final MedicineInstance EMPTY = new MedicineInstance("", "", "", 0, false, List.of());
    public static final Codec<MedicineInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("output_item_id", "").forGetter(MedicineInstance::outputItemId),
            Codec.STRING.optionalFieldOf("name", "").forGetter(MedicineInstance::name),
            Codec.STRING.optionalFieldOf("description", "").forGetter(MedicineInstance::description),
            Codec.INT.optionalFieldOf("doses", 0).forGetter(MedicineInstance::doses),
            Codec.BOOL.optionalFieldOf("treasure", false).forGetter(MedicineInstance::treasure),
            Effect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(MedicineInstance::effects)
    ).apply(instance, MedicineInstance::new));
    public static final StreamCodec<ByteBuf, MedicineInstance> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public MedicineInstance {
        effects = List.copyOf(effects);
    }

    public boolean isPresent() {
        return !name.isEmpty() || doses > 0 || !effects.isEmpty();
    }

    public MedicineInstance withDoses(int newDoses) {
        return new MedicineInstance(outputItemId, name, description, newDoses, treasure, effects);
    }

    public MedicineInstance withDosesAndEffects(int newDoses, List<Effect> newEffects) {
        return new MedicineInstance(outputItemId, name, description, newDoses, treasure, newEffects);
    }

    public record Effect(String id, int level, int duration) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Effect::id),
                Codec.INT.fieldOf("level").forGetter(Effect::level),
                Codec.INT.fieldOf("duration").forGetter(Effect::duration)
        ).apply(instance, Effect::new));
    }
}
