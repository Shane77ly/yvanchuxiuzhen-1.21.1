package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.SilkwormBoxBlockEntity;

/** 第二次独立启动只观察原玩家、库存和两个工作位置，不执行任何采收、合成或放置。 */
final class SilkSurvivalReentryCheck extends SystemCheck {
    static final String SAVE = "Renjian-Silk-268-275";
    private static final BlockPos BOX = new BlockPos(4297, 66, 4413);
    private static final BlockPos TABLE = BOX.east();

    SilkSurvivalReentryCheck(List<String> report, String levelId) {
        super("蚕盒生存同档重进", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        var boards = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "camphorplanks"));
        onServer(player -> {
            expect("原存档玩家", UUID.fromString("380df991-f603-344c-a090-369bad2a924a"), player.getUUID());
            expect("仍是生存模式", GameType.SURVIVAL, player.gameMode.getGameModeForPlayer());
            expect("仍在人间道", ModId.VALUE + ":dimensionrenjian", player.level().dimension().location().toString());
            expectTrue("工作台原位保留", player.level().getBlockState(TABLE).is(Blocks.CRAFTING_TABLE));
            expectTrue("蚕盒原位恢复方块实体", player.level().getBlockEntity(BOX) instanceof SilkwormBoxBlockEntity);
            expect("原背包剩三板", 3, player.getInventory().countItem(boards));
        });
        expectTrue("客户端已同步蚕盒和工作台", minecraft.level.getBlockEntity(BOX) instanceof SilkwormBoxBlockEntity
                && minecraft.level.getBlockState(TABLE).is(Blocks.CRAFTING_TABLE));
        expect("客户端背包剩三板", 3, minecraft.player.getInventory().countItem(boards));
        finish();
        return true;
    }
}
