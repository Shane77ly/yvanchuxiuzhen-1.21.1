package net.yvanchuxiuzhen.collection.tiandao;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源 {@code BlockDestroy} 的六面固定块保护，只取消非创造模式玩家的破坏事件。 */
final class FixedBlockProtection {
    private static final ResourceLocation FIXED_BLOCK = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandaoguding");

    private FixedBlockProtection() {
    }

    static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos().relative(direction)).getBlock()).equals(FIXED_BLOCK)) {
                event.setCanceled(true);
                return;
            }
        }
    }
}
