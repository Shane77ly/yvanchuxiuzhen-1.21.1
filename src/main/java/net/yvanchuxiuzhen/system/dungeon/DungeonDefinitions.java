package net.yvanchuxiuzhen.system.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;

/** 控制器把来源百分号定义展开为连续刷怪记录和原槽奖励，保留五波清理及零X占位符终止规则。 */
final class DungeonDefinitions {
    private DungeonDefinitions() {
    }

    static void reset(DungeonBlockEntity block) {
        CompoundTag data = block.state();
        for (int wave = 1; wave <= 5; wave++) data.putInt("fybenboci" + wave + "num", 0);
        for (String key : new String[]{"fubentick", "fubenbociMax", "fubenbocinum", "fubenjieshutime", "fubenentityspawnnum", "fubenentitynum"}) data.putInt(key, 0);
        for (int index = 1; data.getCompound("zhanwei" + index).getInt("entityshengchengX") != 0; index++) {
            CompoundTag marker = data.getCompound("zhanwei" + index);
            BlockPos pos = block.getBlockPos().offset(marker.getInt("entityshengchengX"), marker.getInt("entityshengchengY"), marker.getInt("entityshengchengZ"));
            if (block.getLevel().getBlockEntity(pos) instanceof DungeonBlockEntity found && found.kind().equals("fubenentity")) {
                block.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    static void parse(DungeonBlockEntity block) {
        CompoundTag data = block.state();
        String[] fields = data.getString("fubenshujv").split("%");
        data.putInt("fubenbociMax", (int) DungeonRewards.number(fields[1]));
        data.putInt("fubenjieshutime", (int) DungeonRewards.number(fields[2]));
        data.putString("giftlist", fields[3]);
        int cursor = 4;
        int spawn = 0;
        for (int wave = 1; wave <= data.getInt("fubenbociMax"); wave++) {
            int count = (int) DungeonRewards.number(fields[cursor++]);
            data.putInt("fybenboci" + wave + "num", count);
            for (int index = 0; index < count; index++) {
                CompoundTag mob = data.getCompound("mobtagdata" + spawn);
                mob.putString("entityshengchengid", DungeonRewards.remap(fields[cursor]));
                mob.putInt("entityshengchenglevel", (int) DungeonRewards.number(fields[cursor + 1]));
                CompoundTag marker = data.getCompound(fields[cursor + 2]);
                for (String key : new String[]{"entityshengchengX", "entityshengchengY", "entityshengchengZ", "entityshengchengchaoxiang"}) mob.putInt(key, marker.getInt(key));
                mob.putBoolean("entityshengchenggongji", marker.getBoolean("entityshengchenggongji"));
                data.put("mobtagdata" + spawn++, mob);
                cursor += 3;
            }
        }
        if (!fields[3].isEmpty()) {
            for (int slot = 0; slot < 18; slot++) data.putInt("gift" + slot + "qvanzhong", 0);
            String[] gifts = fields[3].split("&");
            for (int slot = 0; slot < gifts.length; slot++) {
                String[] gift = gifts[slot].split("/");
                data.putInt("gift" + slot + "qvanzhong", (int) DungeonRewards.number(gift[2]));
                block.setItem(slot, DungeonRewards.create(gift));
            }
        }
    }

    static void configure(DungeonBlockEntity block, double side) {
        CompoundTag data = block.state();
        if (side >= 1) {
            data.putInt("fubenbianchang", (int) side);
            for (String axis : new String[]{"X", "Y", "Z"}) data.putInt("fubenzhongxin" + axis, (int) (side / 2));
        }
        for (int x = 0; x < (int) side; x++) {
            for (int y = 0; y < (int) side; y++) {
                for (int z = 0; z < (int) side; z++) {
                    if (block.getLevel().getBlockEntity(block.getBlockPos().offset(x, y, z)) instanceof DungeonBlockEntity marker && marker.kind().equals("fubenentity")) {
                        CompoundTag tag = marker.state();
                        tag.putInt("entityshengchengX", x);
                        tag.putInt("entityshengchengY", y);
                        tag.putInt("entityshengchengZ", z);
                        data.put(tag.getString("zhanweifuid"), tag);
                        data.putInt("zhanweifunum", data.getInt("zhanweifunum") + 1);
                        marker.changed();
                    }
                }
            }
        }
        block.changed();
    }
}
