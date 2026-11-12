package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.Codec;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源普通配置型地物的独立注册身份与逐地物维度门。 */
public final class NativeWorldFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModId.VALUE);
    private static final Set<ResourceLocation> RENJIAN = Set.of(id("dimensionrenjian"));
    private static final Set<ResourceLocation> YAOHUN = Set.of(id("dimensionyaohun"));
    private static final Set<ResourceLocation> DILING = Set.of(id("dimensiondiling"));
    private static final Set<ResourceLocation> WUGUI = Set.of(id("dimensionwugui"));
    private static final Set<ResourceLocation> UNRESTRICTED = Set.of();
    private static final Feature<BlockStateConfiguration> SOURCE_BLOB = new SourceBlockBlobFeature();

    static {
        add("barrenstonestingshengcheng", Feature.BLOCK_PILE, BlockPileConfiguration.CODEC, RENJIAN);
        add("bluestoneshengcheng", SOURCE_BLOB, BlockStateConfiguration.CODEC, RENJIAN);
        add("diplidisdiandrashengcheng", Feature.SIMPLE_BLOCK, SimpleBlockConfiguration.CODEC, RENJIAN);
        add("duckweedshengcheng", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, RENJIAN);
        add("gucidici", Feature.BLOCK_COLUMN, BlockColumnConfiguration.CODEC, DILING);
        add("huaguangjiegou", Feature.BLOCK_COLUMN, BlockColumnConfiguration.CODEC, UNRESTRICTED);
        add("icelakeshengcheng", Feature.LAKE, LakeFeature.Configuration.CODEC, RENJIAN);
        add("icerock_02", Feature.BLOCK_COLUMN, BlockColumnConfiguration.CODEC, RENJIAN);
        add("icerock", SOURCE_BLOB, BlockStateConfiguration.CODEC, RENJIAN);
        add("ironropeshengcheng", Feature.BLOCK_COLUMN, BlockColumnConfiguration.CODEC, WUGUI);
        add("lanhuashengcheng", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, YAOHUN);
        add("mushroomblue_01shengcheng", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, YAOHUN);
        add("mushroomblue_02fangzhi", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, YAOHUN);
        add("mushroomblue_03fangzhi", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, YAOHUN);
        add("mushroompinkwhite_01fangzhi", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, YAOHUN);
        add("mushroompinkwhite_02fangzhi", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, YAOHUN);
        add("mushroompinkwhite_03fangzhi", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, YAOHUN);
        add("ricciafluitansshengcheng", Feature.SIMPLE_BLOCK, SimpleBlockConfiguration.CODEC, RENJIAN);
        add("rockpile_01", SOURCE_BLOB, BlockStateConfiguration.CODEC, RENJIAN);
        add("rottingbambooshengcheng", Feature.BLOCK_COLUMN, BlockColumnConfiguration.CODEC, WUGUI);
        add("rushesshengcheng", Feature.SIMPLE_BLOCK, SimpleBlockConfiguration.CODEC, RENJIAN);
        add("shanhu_2shengcheng", Feature.CORAL_CLAW, NoneFeatureConfiguration.CODEC, YAOHUN);
        add("shanhu_3shengcheng", Feature.CORAL_MUSHROOM, NoneFeatureConfiguration.CODEC, YAOHUN);
        add("shanhushengcheng", Feature.CORAL_TREE, NoneFeatureConfiguration.CODEC, YAOHUN);
        add("snowdriftshengcheng", Feature.SIMPLE_BLOCK, SimpleBlockConfiguration.CODEC, RENJIAN);
        add("stonetree_01", Feature.BLOCK_COLUMN, BlockColumnConfiguration.CODEC, UNRESTRICTED);
        add("stonetree_05", Feature.REPLACE_BLOBS, ReplaceSphereConfiguration.CODEC, UNRESTRICTED);
        add("swampsiltdiwu", Feature.REPLACE_BLOBS, ReplaceSphereConfiguration.CODEC, DILING);
        add("tudibankuai_01", Feature.REPLACE_BLOBS, ReplaceSphereConfiguration.CODEC, DILING);
        add("wenqvandiwu", Feature.LAKE, LakeFeature.Configuration.CODEC, YAOHUN);
        add("witheredshrubshengcheng", Feature.SIMPLE_BLOCK, SimpleBlockConfiguration.CODEC, RENJIAN);
        add("zishuijing", Feature.RANDOM_PATCH, RandomPatchConfiguration.CODEC, YAOHUN);
        add("zishuijingzhu", Feature.BLOCK_COLUMN, BlockColumnConfiguration.CODEC, YAOHUN);
    }

    private NativeWorldFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    private static <C extends FeatureConfiguration> void add(String name, Feature<C> delegate, Codec<C> codec,
            Set<ResourceLocation> dimensions) {
        FEATURES.register(name, () -> new DimensionFeature<>(codec, delegate, dimensions));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
