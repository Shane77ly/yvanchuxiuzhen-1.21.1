package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.LoomRuntime;
import net.yvanchuxiuzhen.system.craft.SilkReelRuntime;
import net.yvanchuxiuzhen.system.craft.SilkwormBoxRuntime;
import net.yvanchuxiuzhen.system.craft.content.SilkReelBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.SilkwormBoxBlockEntity;

/** 核对桑叶自然掉落、蚕盒结蛹、缫丝轮产丝和织布机消费的最小跨系统链。 */
final class NaturalSilkChainCheck extends ActionCheck {
    private BlockPos leavesPos;
    private BlockPos boxPos;
    private BlockPos reelPos;
    private BlockPos inputPos;
    private BlockPos loomPos;
    private ItemStack leaves = ItemStack.EMPTY;

    NaturalSilkChainCheck(List<String> report, String levelId) {
        super("naturalsilkchain", report, levelId);
        script(
                new Action(4, player -> {
                    leavesPos = player.blockPosition().offset(2, 0, 2);
                    boxPos = player.blockPosition().offset(4, 0, 2);
                    reelPos = player.blockPosition().offset(6, 0, 2);
                    inputPos = reelPos.south().below();
                    loomPos = player.blockPosition().offset(8, 0, 2);
                    clear(player);
                    player.serverLevel().setBlockAndUpdate(leavesPos, block("mulberryleaves"));
                    for (int attempt = 0; attempt < 64 && leaves.isEmpty(); attempt++) {
                        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                                player.serverLevel().getBlockState(leavesPos), player.serverLevel(), leavesPos, null);
                        for (ItemStack drop : drops) {
                            if (drop.is(item("mulberryleaves").getItem())) {
                                leaves = drop.copy();
                                break;
                            }
                        }
                    }
                    expectTrue("桑叶自然掉落表可取得桑叶", !leaves.isEmpty());
                    player.serverLevel().setBlockAndUpdate(boxPos, block("canhe"));
                    player.serverLevel().setBlockAndUpdate(reelPos,
                            block("saosilun").setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
                    player.serverLevel().setBlockAndUpdate(inputPos, block("canhe"));
                    player.serverLevel().setBlockAndUpdate(loomPos, block("zhibuji"));
                }),
                new Action(4, player -> {
                    player.setItemInHand(InteractionHand.MAIN_HAND, item("silkworm"));
                    SilkwormBoxRuntime.use(player.serverLevel(), boxPos, player);
                    player.setItemInHand(InteractionHand.MAIN_HAND, leaves.copy());
                    SilkwormBoxRuntime.use(player.serverLevel(), boxPos, player);
                    SilkwormBoxBlockEntity box = box(player, boxPos);
                    box.value("tick", 119);
                    box.value("shiwu", 1);
                    SilkwormBoxRuntime.tick(player.serverLevel(), boxPos);
                    expectTrue("桑叶喂蚕后蚕盒临界刻生产普通蚕蛹",
                            box.getItem(0).is(item("silkwormchrysalis").getItem()) && box.value("tick") == 1);
                    SilkwormBoxBlockEntity input = box(player, inputPos);
                    input.setItem(0, item("silkwormchrysalis"));
                }),
                new Action(4, player -> {
                    SilkReelBlockEntity reel = (SilkReelBlockEntity) player.serverLevel().getBlockEntity(reelPos);
                    reel.setReeling(true);
                    reel.setTick(4);
                    reel.setDirection("south");
                    SilkReelRuntime.tick(player.serverLevel(), reelPos);
                    expectTrue("缫丝轮消费蚕蛹并产出灵丝线",
                            player.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(reelPos).inflate(1.0)).stream()
                                    .anyMatch(entity -> entity.getItem().is(item("silkthread").getItem())));
                    player.setItemInHand(InteractionHand.MAIN_HAND, item("silkthread"));
                    LoomRuntime.use(player.serverLevel(), loomPos, player);
                    expectTrue("织布机消费灵丝线并生成灵布材料",
                            player.getInventory().contains(stack -> stack.is(item("cailiao_174").getItem())));
                }));
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        clear(player);
        leaves = ItemStack.EMPTY;
    }

    private void clear(ServerPlayer player) {
        if (player.serverLevel() == null) return;
        for (BlockPos pos : List.of(leavesPos, boxPos, reelPos, inputPos, loomPos)) {
            if (pos != null) player.serverLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        player.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(player.blockPosition()).inflate(12)).forEach(ItemEntity::discard);
        player.getInventory().clearContent();
    }

    private static SilkwormBoxBlockEntity box(ServerPlayer player, BlockPos pos) {
        return (SilkwormBoxBlockEntity) player.serverLevel().getBlockEntity(pos);
    }

    private static BlockState block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)).defaultBlockState();
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)));
    }
}
