package net.yvanchuxiuzhen.system.treasure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockEntity;

/** 开箱状态归宝箱方块实体；来源计时每二十游戏刻增加一次，第二次发奖、第五次移除。 */
public final class TreasureChestRuntime {
    private TreasureChestRuntime() {
    }

    public static void use(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !(level.getBlockEntity(pos) instanceof TreasureChestBlockEntity chest)) return;
        CompoundTag data = chest.getPersistentData();
        if (data.getBoolean("dakai")) return;
        if (chest.locked()) {
            ItemStack key = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("yvanchuxiuzhen:treasure_chest_1101")));
            if (!player.getInventory().contains(key)) {
                chest.animate(1);
                return;
            }
            TreasureChestSettings.initialize(data, serverLevel.random);
            chest.animate(2);
            data.putBoolean("dakai", true);
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(key.getItem()), 1, player.inventoryMenu.getCraftSlots());
        } else {
            chest.animate(1);
            data.putBoolean("dakai", true);
        }
        chest.sync();
    }

    public static void tick(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof TreasureChestBlockEntity chest)) return;
        CompoundTag data = chest.getPersistentData();
        if (!data.getBoolean("dakai")) return;
        double tick = data.getDouble("tick") + 1;
        data.putDouble("tick", tick);
        if (tick >= 2 && data.getString("zhuangtai").isEmpty()) {
            if (chest.locked()) {
                data.putString("zhuangtai", "爆物品");
                TreasureRewards.open(level, Vec3.atLowerCornerOf(pos), data.getString("TreasureTypeBig"), data.getString("TreasureTypeSmall"),
                        Math.rint(data.getDouble("TreasureLEVEL")), (int) Math.rint(data.getDouble("minimumvalue")),
                        (int) Math.rint(data.getDouble("Maximumvalue")));
            } else {
                TreasureRewards.open(level, Vec3.atLowerCornerOf(pos), "宝箱", Mth.nextInt(level.random, 1, 10) <= 7 ? "妖兽材料" : "金属材料", 1, 2, 5);
                data.putString("zhuangtai", "爆物品");
            }
        } else if (tick >= 5 && data.getString("zhuangtai").equals("爆物品")) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        chest.sync();
    }
}
