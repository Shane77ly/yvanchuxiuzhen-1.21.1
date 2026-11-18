package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.yvanchuxiuzhen.platform.ModId;

/** 用原生桶和计划刻验证热油源、流动体和取回路径。 */
final class HotOilCheck extends SystemCheck {
    private final Screenshots screenshots;
    private BlockPos source;

    HotOilCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("热油", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                var level = player.serverLevel();
                source = player.blockPosition().east(4);
                for (BlockPos pos : BlockPos.betweenClosed(source.offset(-2, -1, -2), source.offset(2, 4, 2))) {
                    Block block = pos.getY() == source.getY() - 1 || pos.getY() == source.getY()
                            && (Math.abs(pos.getX() - source.getX()) == 2 || Math.abs(pos.getZ() - source.getZ()) == 2)
                            ? Blocks.STONE : Blocks.AIR;
                    level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
                }
                BucketItem bucket = (BucketItem) BuiltInRegistries.ITEM.get(id("hotoil_bucket"));
                expect("滚油桶名称", "滚油桶", bucket.getDefaultInstance().getHoverName().getString());
                expectTrue("滚油桶原生放出源", bucket.emptyContents(player, level, source, null));
                var state = level.getFluidState(source);
                expectTrue("源块和源流体 ID 一致", level.getBlockState(source).is(BuiltInRegistries.BLOCK.get(id("hotoil")))
                        && state.getType() == BuiltInRegistries.FLUID.get(id("hotoil")));
                expectTrue("源量为8且属于水标签", state.isSource() && state.getAmount() == 8 && state.is(FluidTags.WATER));
                expect("密度", 850, state.getFluidType().getDensity());
                expect("温度", 550, state.getFluidType().getTemperature());
                expectTrue("滚油桶容器是空桶", bucket.hasCraftingRemainingItem() && bucket.getCraftingRemainingItem() == Items.BUCKET);
                player.connection.teleport(source.getX() + 0.5, source.getY() + 1, source.getZ() - 1.5, 0, 55);
            });
            case 1 -> {
                if (local < 30) return false;
                screenshots.take("decor_hotoil.png");
                onServer(player -> {
                    var level = player.serverLevel();
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        var flow = level.getFluidState(source.relative(direction));
                        expectTrue("原生计划刻产生流动态 " + direction,
                                flow.getType() == BuiltInRegistries.FLUID.get(id("flowing_hotoil"))
                                        && !flow.isSource() && flow.getAmount() == 7 && flow.is(FluidTags.WATER));
                    }
                    var state = level.getBlockState(source);
                    var bucket = ((LiquidBlock) state.getBlock()).pickupBlock(player, level, source, state);
                    expectTrue("原生源块装回滚油桶", bucket.is(BuiltInRegistries.ITEM.get(id("hotoil_bucket"))));
                    expectTrue("取回后源格为空", level.getFluidState(source).isEmpty());
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
