package net.yvanchuxiuzhen.system.combat.enchant;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.GatherExperienceEvent;

/**
 * 连锁采集（来源原初系统的 {@code ChainProcedure}，附魔 {@code chain}）：用主手工具挖掉一个方块时，
 * 从它出发按 3×3×3 邻域展开，把相同方块一并挖掉，展开的条目数不超过 附魔等级 × 5 + 1（条目可重复，来源如此）；
 * 每挖一个方块工具损耗 1 点。矿石按玩家持久化数据中的 {@code collection_LEVEL}（采集等级，随 profession 写入）
 * 以 等级 × 0.5 % 的概率额外掉 3 倍、等级 % 的概率额外掉 2 倍，每个矿石给采集职业 2 点经验（经 {@link GatherExperienceEvent}）。
 * 掉落不带工具，不受时运、精准采集影响，一律掉在被挖的方块位置（来源如此）。
 */
public final class ChainMining {
    /** 来源的 {@code forge:ores} 同时含原版矿石与本 Mod 的矿石；转换数据把后者收在本 Mod 命名空间的 {@code ores} 标签里，两个标签都算矿石。 */
    private static final TagKey<Block> OWN_ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ores"));

    private ChainMining() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ChainMining::onBreak);
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getLevel() instanceof Level level)) {
            return;
        }
        int chain = Enchants.mainHand(player, Enchants.key("chain"));
        BlockPos origin = event.getPos();
        if (chain == 0 || !player.getMainHandItem().isCorrectToolForDrops(level.getBlockState(origin))) {
            return;
        }
        double collectionLevel = player.getPersistentData().getInt("collection_LEVEL");
        Block block = level.getBlockState(origin).getBlock();
        List<BlockPos> queue = new ArrayList<>();
        queue.add(origin);
        int processed = 0;
        double experience = 0;
        while (processed <= chain * 5 && queue.size() != processed) {
            BlockPos center = queue.get(processed);
            processed++;
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (level.getBlockState(pos).getBlock() != block) {
                            continue;
                        }
                        queue.add(pos);
                        if (level.getBlockState(pos).is(Tags.Blocks.ORES) || level.getBlockState(pos).is(OWN_ORES)) {
                            double roll = Mth.nextDouble(player.getRandom(), 1, 100);
                            if (roll <= collectionLevel * 0.5) {
                                duplicate(level, pos, origin, event.getState(), 3);
                            } else if (roll <= collectionLevel) {
                                duplicate(level, pos, origin, event.getState(), 2);
                            }
                            experience += 2;
                        }
                        breakAndDrop(level, pos, origin);
                        // 来源的 hurt(1, random, null) 不带玩家，创造模式也损耗。
                        player.getMainHandItem().hurtAndBreak(1, player.serverLevel(), (LivingEntity) null, item -> {
                        });
                    }
                }
            }
        }
        if (experience >= 1) {
            NeoForge.EVENT_BUS.post(new GatherExperienceEvent(player, experience));
        }
    }

    /** 把被挖的方块状态放回去再挖掉，掉落一份额外的产物。 */
    private static void duplicate(Level level, BlockPos pos, BlockPos origin, BlockState state, int times) {
        for (int i = 0; i < times; i++) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
            breakAndDrop(level, pos, origin);
        }
    }

    private static void breakAndDrop(Level level, BlockPos pos, BlockPos origin) {
        Block.dropResources(level.getBlockState(pos), level, origin, null);
        level.destroyBlock(pos, false);
    }
}
