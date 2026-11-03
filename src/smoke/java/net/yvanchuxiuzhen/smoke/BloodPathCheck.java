package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.BloodStone;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianFluids;

/**
 * 血道的实机检查：吸血石充血 150 后经 20 tick 的检查在上方放一格血流并剩 50（血池截图），血流属于水标签，
 * 血精右键回复 2 点生命并消耗 1 个。
 */
final class BloodPathCheck extends SystemCheck {
    private final Screenshots screenshots;
    private BlockPos stone;

    BloodPathCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("血道", report, levelId);
        this.screenshots = screenshots;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                stone = player.blockPosition().offset(4, 0, 0);
                for (int height = 0; height <= 5; height++) {
                    player.level().setBlock(stone.above(height), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                player.level().setBlock(stone, BuiltInRegistries.BLOCK.get(id("xixveshi")).defaultBlockState(), Block.UPDATE_ALL);
                BloodStone.addBlood(player.level(), stone, 150);
                expect("充血量", 150, BloodStone.blood(player.level(), stone));
                player.connection.teleport(stone.getX() - 3.5, stone.getY() + 1, stone.getZ() + 0.5, -90, 30);
            });
            case 1 -> {
                if (local < 50) {
                    return false;
                }
                onServer(player -> {
                    expectTrue("吸血石上方放出血流", player.level().getFluidState(stone.above()).getType() == XiulianFluids.BLOOD.get());
                    expect("放出血流后剩余充血量", 50, BloodStone.blood(player.level(), stone));
                    expectTrue("血流属于水标签", XiulianFluids.BLOOD.get().defaultFluidState().is(FluidTags.WATER));
                    expectTrue("腥桶已注册", BuiltInRegistries.ITEM.containsKey(id("xveliu_bucket")));
                });
            }
            case 2 -> {
                if (local < 20) {
                    return false;
                }
                screenshots.take("xiulian_bloodstone.png");
                onServer(player -> {
                    Item essence = BuiltInRegistries.ITEM.get(id("xvejing"));
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(essence, 3));
                    player.setHealth(10);
                    essence.use(player.level(), player, InteractionHand.MAIN_HAND);
                    expect("血精回复 2 点生命", 12, player.getHealth());
                    expect("血精消耗 1 个", 2, player.getMainHandItem().getCount());
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    for (int height = 0; height <= 5; height++) {
                        player.level().setBlock(stone.above(height), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }
}
