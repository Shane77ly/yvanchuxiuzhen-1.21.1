package net.yvanchuxiuzhen.system.plant.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.plant.NichangGrassBlock;

/** 荫翠符逐方块的催熟操作，保留霓裳草、来源作物标签、原版作物与树苗的分支顺序。 */
public final class PlantRipening {
    private static final TagKey<Block> CROPS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yvanchuzuowu"));

    private PlantRipening() {
    }

    public static void ripen(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof NichangGrassBlock grass) {
            grass.ripen(level, pos);
        } else if (state.is(CROPS)) {
            level.scheduleTick(pos, state.getBlock(), 1);
            level.scheduleTick(pos, state.getBlock(), 3);
            level.scheduleTick(pos, state.getBlock(), 5);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5,
                    pos.getZ() + 0.5, 5, 0.3, 0.5, 0.3, 0);
        } else if (state.getBlock() instanceof CropBlock || state.getBlock() instanceof StemBlock) {
            bonemeal(level, pos);
        } else if (state.is(BlockTags.SAPLINGS)) {
            for (int attempt = 0; attempt < 12; attempt++) {
                if (level.getBlockState(pos).is(BlockTags.SAPLINGS)) {
                    bonemeal(level, pos);
                }
            }
        }
    }

    /** 来源Forge的CROP由CropBlock与StemBlock提供；新版本直接按这两个原生类型保留判定。 */
    private static void bonemeal(ServerLevel level, BlockPos pos) {
        if (BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, null)
                || BoneMealItem.growWaterPlant(new ItemStack(Items.BONE_MEAL), level, pos, null)) {
            growthParticles(level, pos);
        }
    }

    /** 来源2005事件只发骨粉粒子；目标1505会额外播声，按来源采样后单独广播粒子。 */
    private static void growthParticles(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        int count = 15;
        double width = 0.5;
        double height;
        if (state.is(Blocks.WATER)) {
            count *= 3;
            width = 3;
            height = 1;
        } else if (state.isSolidRender(level, pos)) {
            pos = pos.above();
            count *= 3;
            width = 3;
            height = 1;
        } else {
            height = state.getShape(level, pos).max(Direction.Axis.Y);
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 0, 0, 0, 0, 0);
        RandomSource random = RandomSource.create();
        for (int index = 0; index < count; index++) {
            double speedX = random.nextGaussian() * 0.02;
            double speedY = random.nextGaussian() * 0.02;
            double speedZ = random.nextGaussian() * 0.02;
            double x = pos.getX() + 0.5 - width + random.nextDouble() * width * 2;
            double y = pos.getY() + random.nextDouble() * height;
            double z = pos.getZ() + 0.5 - width + random.nextDouble() * width * 2;
            if (!level.getBlockState(BlockPos.containing(x, y, z).below()).isAir()) {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, speedX, speedY, speedZ, 1);
            }
        }
    }
}
