package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 将来源无群系筛选的地物追加至尚未包含它们的群系。 */
record AddMissingBiomeFeatures(HolderSet<PlacedFeature> features, GenerationStep.Decoration step) implements BiomeModifier {
    private static final MapCodec<AddMissingBiomeFeatures> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(AddMissingBiomeFeatures::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddMissingBiomeFeatures::step))
            .apply(instance, AddMissingBiomeFeatures::new));
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ModId.VALUE);

    static {
        SERIALIZERS.register("add_missing_biome_features", () -> CODEC);
    }

    static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) return;
        BiomeGenerationSettingsBuilder settings = builder.getGenerationSettings();
        List<Holder<PlacedFeature>> existing = settings.getFeatures(step);
        features.forEach(feature -> {
            if (!existing.contains(feature)) settings.addFeature(step, feature);
        });
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
