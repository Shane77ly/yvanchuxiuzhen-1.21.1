package net.yvanchuxiuzhen.system.skill;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源 Fushui 在三个指定群系的水体环境中，对玩家施加反胃。 */
final class SkillBiomeEffects {
    private static final Set<ResourceKey<Biome>> WATER_BIOMES = Set.of(
            biome("biomeshijve_01"), biome("biomeshijve_02"), biome("biomeshijve_03"));

    private SkillBiomeEffects() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(SkillBiomeEffects::onPlayerTick);
    }

    private static ResourceKey<Biome> biome(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var biome = player.level().getBiome(BlockPos.containing(player.position()));
        boolean isTargetBiome = WATER_BIOMES.stream().anyMatch(biome::is);
        if (isTargetBiome && player.isInWaterRainOrBubble() && !player.hasEffect(MobEffects.CONFUSION)) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }
    }
}
