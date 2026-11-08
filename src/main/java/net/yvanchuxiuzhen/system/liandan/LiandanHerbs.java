package net.yvanchuxiuzhen.system.liandan;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.yvanchuxiuzhen.platform.content.ContentFiles;

/** 药草目录按来源顺序生成每台装置独立保存的药性快照，重复药效仍分别参与运算。 */
public final class LiandanHerbs {
    private static final List<Herb> HERBS = ContentFiles.read("content/liandan/herbs.json", Herb.CODEC);

    private LiandanHerbs() {
    }

    public static CompoundTag snapshot() {
        CompoundTag result = new CompoundTag();
        for (Herb herb : HERBS) {
            result.putInt(herb.itemId() + "number", herb.effects().size());
            for (int i = 0; i < herb.effects().size(); i++) {
                Effect effect = herb.effects().get(i);
                String key = herb.itemId() + "effect" + i;
                result.putString(key + "name", effect.name());
                result.putString(key + "symbollevel", effect.levelOp());
                result.putInt(key + "symbollevelnum", effect.level());
                result.putString(key + "symboltime", effect.timeOp());
                result.putInt(key + "symboltimenum", effect.time());
            }
        }
        return result;
    }

    private record Herb(String itemId, List<Effect> effects) {
        private static final Codec<Herb> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item_id").forGetter(Herb::itemId),
                Effect.CODEC.listOf().fieldOf("effects").forGetter(Herb::effects)
        ).apply(instance, Herb::new));
    }

    private record Effect(String name, String levelOp, int level, String timeOp, int time) {
        private static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Effect::name),
                Codec.STRING.fieldOf("level_op").forGetter(Effect::levelOp),
                Codec.INT.fieldOf("level").forGetter(Effect::level),
                Codec.STRING.fieldOf("time_op").forGetter(Effect::timeOp),
                Codec.INT.fieldOf("time").forGetter(Effect::time)
        ).apply(instance, Effect::new));
    }
}
