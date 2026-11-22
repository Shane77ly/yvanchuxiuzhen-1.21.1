package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 还原无群系筛选的来源出生事件，同时保留已内联的条目及其顺序。 */
record AddMissingCreatureSpawn(MobSpawnSettings.SpawnerData spawn) implements BiomeModifier {
    private static final MapCodec<AddMissingCreatureSpawn> CODEC = MobSpawnSettings.SpawnerData.CODEC
            .fieldOf("spawn").xmap(AddMissingCreatureSpawn::new, AddMissingCreatureSpawn::spawn);
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ModId.VALUE);

    static {
        SERIALIZERS.register("add_missing_creature_spawn", () -> CODEC);
    }

    static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) return;
        MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
        if (spawns.getSpawner(MobCategory.CREATURE).stream().noneMatch(entry -> entry.type == spawn.type)) {
            spawns.addSpawn(MobCategory.CREATURE, spawn);
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
