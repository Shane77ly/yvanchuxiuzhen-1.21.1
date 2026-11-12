package net.yvanchuxiuzhen.system.profession;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;

/** 来源 {@code BlockDestroy} 的矿石采集分支：双手任一工具等级达到方块硬度时加经验并掷一次额外掉落。 */
final class Gathering {
    private static final ResourceLocation FIXED_BLOCK = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandaoguding");
    private static final TagKey<Block> OWN_ORES = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ores"));

    private Gathering() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(Gathering::onBreak);
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        for (Direction direction : Direction.values()) {
            if (BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos.relative(direction)).getBlock()).equals(FIXED_BLOCK)) {
                return;
            }
        }
        BlockState state = event.getState();
        if (!state.is(Tags.Blocks.ORES) && !state.is(OWN_ORES)) {
            return;
        }
        float hardness = level.getBlockState(pos).getDestroySpeed(level, pos);
        if (toolLevel(player.getMainHandItem()) < hardness && toolLevel(player.getOffhandItem()) < hardness) {
            return;
        }
        Professions.addExperience(player, Profession.GATHERING, 2);
        double roll = Mth.nextDouble(RandomSource.create(), 1, 100);
        double levelValue = Professions.level(player, Profession.GATHERING);
        if (roll <= levelValue * 0.5) {
            duplicate(level, pos, state, 3, 10);
        } else if (roll <= levelValue) {
            duplicate(level, pos, state, 2, 5);
        }
    }

    /** 来源没有传入工具或玩家，额外掉落不继承时运与精准采集；每次还原方块后掉落并移除。 */
    private static void duplicate(ServerLevel level, BlockPos pos, BlockState state, int count, int particles) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                particles, 0.1, 0.1, 0.1, 1);
        for (int i = 0; i < count; i++) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            Block.dropResources(level.getBlockState(pos), level, pos, null);
            level.destroyBlock(pos, false);
        }
    }

    /** 1.21.1 移除了 Tier 数字等级；来源工具保留在 LegacyTier，原版等级按不能正确采掘的标签对应。 */
    private static int toolLevel(ItemStack stack) {
        if (!(stack.getItem() instanceof TieredItem item)) {
            return 0;
        }
        Tier tier = item.getTier();
        if (tier instanceof LegacyTier legacy) {
            return legacy.level();
        }
        if (tier.getIncorrectBlocksForDrops() == BlockTags.INCORRECT_FOR_NETHERITE_TOOL) {
            return 4;
        }
        if (tier.getIncorrectBlocksForDrops() == BlockTags.INCORRECT_FOR_DIAMOND_TOOL) {
            return 3;
        }
        if (tier.getIncorrectBlocksForDrops() == BlockTags.INCORRECT_FOR_IRON_TOOL) {
            return 2;
        }
        if (tier.getIncorrectBlocksForDrops() == BlockTags.INCORRECT_FOR_STONE_TOOL) {
            return 1;
        }
        return 0;
    }
}
