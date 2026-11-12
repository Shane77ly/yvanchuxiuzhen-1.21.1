package net.yvanchuxiuzhen.system.world;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 四组来源原版树闭包使用的树干、树冠和叶藤序列化类型。 */
public final class SourceTreeComponents {
    private static final DeferredRegister<TrunkPlacerType<?>> TRUNKS =
            DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, ModId.VALUE);
    private static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE =
            DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, ModId.VALUE);
    private static final DeferredRegister<TreeDecoratorType<?>> DECORATORS =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, ModId.VALUE);

    static final Supplier<TrunkPlacerType<?>> STRAIGHT_TRUNK = TRUNKS.register("legacy_straight_trunk_placer",
            () -> new TrunkPlacerType<>(SourceStraightTrunkPlacer.CODEC));
    static final Supplier<TrunkPlacerType<?>> FANCY_TRUNK = TRUNKS.register("legacy_fancy_trunk_placer",
            () -> new TrunkPlacerType<>(SourceFancyTrunkPlacer.CODEC));
    static final Supplier<TrunkPlacerType<?>> MEGA_JUNGLE_TRUNK = TRUNKS.register("legacy_mega_jungle_trunk_placer",
            () -> new TrunkPlacerType<>(SourceMegaJungleTrunkPlacer.CODEC));
    static final Supplier<TrunkPlacerType<?>> BENDING_TRUNK = TRUNKS.register("legacy_bending_trunk_placer",
            () -> new TrunkPlacerType<>(SourceBendingTrunkPlacer.CODEC));
    static final Supplier<FoliagePlacerType<?>> BLOB_FOLIAGE = FOLIAGE.register("legacy_blob_foliage_placer",
            () -> new FoliagePlacerType<>(SourceBlobFoliagePlacer.CODEC));
    static final Supplier<FoliagePlacerType<?>> FANCY_FOLIAGE = FOLIAGE.register("legacy_fancy_foliage_placer",
            () -> new FoliagePlacerType<>(SourceFancyFoliagePlacer.CODEC));
    static final Supplier<FoliagePlacerType<?>> BUSH_FOLIAGE = FOLIAGE.register("legacy_bush_foliage_placer",
            () -> new FoliagePlacerType<>(SourceBushFoliagePlacer.CODEC));
    static final Supplier<FoliagePlacerType<?>> MEGA_JUNGLE_FOLIAGE = FOLIAGE.register("legacy_mega_jungle_foliage_placer",
            () -> new FoliagePlacerType<>(SourceMegaJungleFoliagePlacer.CODEC));
    static final Supplier<FoliagePlacerType<?>> RANDOM_SPREAD_FOLIAGE = FOLIAGE.register("legacy_random_spread_foliage_placer",
            () -> new FoliagePlacerType<>(SourceRandomSpreadFoliagePlacer.CODEC));
    static final Supplier<TreeDecoratorType<?>> LEAVE_VINE = DECORATORS.register("legacy_leave_vine",
            () -> new TreeDecoratorType<>(SourceLeaveVineDecorator.CODEC));

    private SourceTreeComponents() {
    }

    public static void register(IEventBus modBus) {
        TRUNKS.register(modBus);
        FOLIAGE.register(modBus);
        DECORATORS.register(modBus);
    }
}
