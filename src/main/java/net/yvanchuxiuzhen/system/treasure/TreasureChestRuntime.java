package net.yvanchuxiuzhen.system.treasure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockEntity;
import net.yvanchuxiuzhen.system.treasure.data.TreasureData;

/** 开箱状态归宝箱方块实体；来源计时每二十游戏刻增加一次，第二次发奖、第五次移除。 */
public final class TreasureChestRuntime {
    private TreasureChestRuntime() {
    }

    public static void use(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !(level.getBlockEntity(pos) instanceof TreasureChestBlockEntity chest)) return;
        if (chest.opened()) return;
        if (chest.locked()) {
            Item key = BuiltInRegistries.ITEM.get(ResourceLocation.parse("yvanchuxiuzhen:treasure_chest_1101"));
            if (!player.getInventory().contains(stack -> stack.is(key))) {
                chest.animate(1);
                return;
            }
            chest.setData(TreasureData.BLOCK_CONTAINER, TreasureChestSettings.initialize(serverLevel.random));
            chest.animate(2);
            chest.setOpened(true);
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(key), 1, player.inventoryMenu.getCraftSlots());
        } else {
            chest.animate(1);
            chest.setOpened(true);
        }
        chest.sync();
    }

    public static void tick(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof TreasureChestBlockEntity chest)) return;
        if (!chest.opened()) return;
        double tick = chest.tickCount() + 1;
        chest.setTickCount(tick);
        if (tick >= 2 && chest.openState().isEmpty()) {
            if (chest.locked()) {
                chest.setOpenState("爆物品");
                TreasureContainerConfig config = chest.getData(TreasureData.BLOCK_CONTAINER);
                TreasureRewards.open(level, Vec3.atLowerCornerOf(pos), config.category(), config.subtype(),
                        Math.rint(config.level()), (int) Math.rint(config.minimum()),
                        (int) Math.rint(config.maximum()));
            } else {
                TreasureRewards.open(level, Vec3.atLowerCornerOf(pos), "宝箱", Mth.nextInt(level.random, 1, 10) <= 7 ? "妖兽材料" : "金属材料", 1, 2, 5);
                chest.setOpenState("爆物品");
            }
        } else if (tick >= 5 && chest.openState().equals("爆物品")) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        chest.sync();
    }
}
