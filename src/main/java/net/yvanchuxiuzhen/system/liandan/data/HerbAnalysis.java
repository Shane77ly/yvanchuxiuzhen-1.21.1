package net.yvanchuxiuzhen.system.liandan.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 玩家药草解析真值与最近一次界面缓存的强类型快照，取代旧版 {@code CompoundTag} 载体。 */
public record HerbAnalysis(
        Map<String, HerbProgress> entries,
        String selectedName,
        String selectedId,
        String progressText,
        String needText,
        List<RevealedEffect> displayEffects) {

    public static final HerbAnalysis EMPTY = new HerbAnalysis(
            Map.of(), "", "", "", "", Collections.nCopies(10, RevealedEffect.EMPTY));

    public static final Codec<HerbAnalysis> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, HerbProgress.CODEC).fieldOf("entries").forGetter(HerbAnalysis::entries),
            Codec.STRING.fieldOf("selected_name").forGetter(HerbAnalysis::selectedName),
            Codec.STRING.fieldOf("selected_id").forGetter(HerbAnalysis::selectedId),
            Codec.STRING.fieldOf("progress_text").forGetter(HerbAnalysis::progressText),
            Codec.STRING.fieldOf("need_text").forGetter(HerbAnalysis::needText),
            RevealedEffect.CODEC.listOf().fieldOf("display_effects").forGetter(HerbAnalysis::displayEffects)
    ).apply(instance, HerbAnalysis::new));

    public HerbAnalysis {
        entries = Map.copyOf(entries);
        displayEffects = List.copyOf(displayEffects);
    }

    public int progress(String itemId) {
        HerbProgress entry = entries.get(itemId);
        return entry == null ? 0 : entry.progress();
    }

    public int maximum(String itemId) {
        HerbProgress entry = entries.get(itemId);
        return entry == null ? 0 : entry.maximum();
    }

    /** 界面药效使用从一开始的原序号，未解析行仍保留来源冒号。 */
    public String effectText(int ordinal) {
        RevealedEffect effect = ordinal >= 1 && ordinal <= displayEffects.size()
                ? displayEffects.get(ordinal - 1) : RevealedEffect.EMPTY;
        return "§b" + effect.name() + ":§8" + effect.description();
    }

    public List<RevealedEffect> revealed(String itemId) {
        HerbProgress entry = entries.get(itemId);
        if (entry == null || entry.progress() <= 0) return List.of();
        return entry.revealed().subList(0, Math.min(entry.progress(), entry.revealed().size()));
    }

    public HerbAnalysis withSelection(String selectedName, String selectedId, String progressText,
                                      String needText, List<RevealedEffect> displayEffects) {
        return new HerbAnalysis(entries, selectedName, selectedId, progressText, needText, displayEffects);
    }

    public HerbAnalysis withEntry(String itemId, HerbProgress entry) {
        Map<String, HerbProgress> updated = new LinkedHashMap<>(entries);
        updated.put(itemId, entry);
        return new HerbAnalysis(updated, selectedName, selectedId, progressText, needText, displayEffects);
    }

    public HerbAnalysis withEntries(Map<String, HerbProgress> updated) {
        return new HerbAnalysis(updated, selectedName, selectedId, progressText, needText, displayEffects);
    }

    public record HerbProgress(int progress, int maximum, List<RevealedEffect> revealed) {
        public static final HerbProgress EMPTY = new HerbProgress(0, 0, List.of());
        public static final Codec<HerbProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("progress").forGetter(HerbProgress::progress),
                Codec.INT.fieldOf("maximum").forGetter(HerbProgress::maximum),
                RevealedEffect.CODEC.listOf().fieldOf("revealed").forGetter(HerbProgress::revealed)
        ).apply(instance, HerbProgress::new));

        public HerbProgress {
            revealed = List.copyOf(revealed);
        }
    }

    public record RevealedEffect(String name, String description) {
        public static final RevealedEffect EMPTY = new RevealedEffect("", "");
        public static final Codec<RevealedEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(RevealedEffect::name),
                Codec.STRING.fieldOf("description").forGetter(RevealedEffect::description)
        ).apply(instance, RevealedEffect::new));
    }
}
