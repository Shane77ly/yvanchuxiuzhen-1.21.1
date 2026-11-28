package net.yvanchuxiuzhen.system.liandan;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.yvanchuxiuzhen.platform.content.ContentFiles;

/** 药草目录按来源顺序生成每台装置独立保存的药性快照，重复药效仍分别参与运算。 */
public final class LiandanHerbs {
    private static final List<Herb> HERBS = List.copyOf(ContentFiles.read("content/liandan/herbs.json", Herb.CODEC));

    private LiandanHerbs() {
    }

    public static List<Herb> all() {
        return HERBS;
    }

    public static Herb definition(String itemId) {
        for (Herb herb : HERBS) {
            if (herb.itemId().equals(itemId)) return herb;
        }
        return null;
    }

    public static HerbCatalogSnapshot snapshot() {
        Map<String, Herb> byItem = new LinkedHashMap<>();
        for (Herb herb : HERBS) {
            byItem.put(herb.itemId(), herb);
        }
        return new HerbCatalogSnapshot(byItem);
    }

    public record HerbCatalogSnapshot(Map<String, Herb> byItem) {
        public static final HerbCatalogSnapshot EMPTY = new HerbCatalogSnapshot(Map.of());
        public static final Codec<HerbCatalogSnapshot> CODEC = Codec.unboundedMap(Codec.STRING, Herb.CODEC)
                .xmap(HerbCatalogSnapshot::new, HerbCatalogSnapshot::byItem);

        public HerbCatalogSnapshot {
            byItem = Map.copyOf(byItem);
        }

        public boolean isEmpty() {
            return byItem.isEmpty();
        }

        public Herb herb(String itemId) {
            return byItem.get(itemId);
        }

        public int effectCount(String itemId) {
            Herb herb = byItem.get(itemId);
            return herb == null ? 0 : herb.effects().size();
        }
    }

    public record Herb(String itemId, List<Effect> effects) {
        public static final Codec<Herb> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item_id").forGetter(Herb::itemId),
                Effect.CODEC.listOf().fieldOf("effects").forGetter(Herb::effects)
        ).apply(instance, Herb::new));

        public Herb {
            effects = List.copyOf(effects);
        }
    }

    public record Effect(String name, String levelOp, int level, String timeOp, int time) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Effect::name),
                Codec.STRING.fieldOf("level_op").forGetter(Effect::levelOp),
                Codec.INT.fieldOf("level").forGetter(Effect::level),
                Codec.STRING.fieldOf("time_op").forGetter(Effect::timeOp),
                Codec.INT.fieldOf("time").forGetter(Effect::time)
        ).apply(instance, Effect::new));
    }
}
