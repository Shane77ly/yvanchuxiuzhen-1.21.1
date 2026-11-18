package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.system.npc.content.QiankunBlock;

/** 在已验证首入的副本中复查原居所与原管家；不重新放置结构或实体。 */
final class RenjianVisualCheck extends SystemCheck {
    private static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, id("dimensionrenjian"));
    private static final BlockPos DAOREN = new BlockPos(0, 36, -21);
    private static final BlockPos TABLE = new BlockPos(0, 35, -27);
    private static final UUID STEWARD = UUID.fromString("42e62273-8d72-421b-9072-0a372b5dcca4");

    private final Consumer<String> shot;

    RenjianVisualCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("人间道固定居所", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (local > 500) {
            fail("等待客户端场景超时，步骤 " + step);
            finish();
            return false;
        }
        return switch (step) {
            case 0 -> {
                minecraft.options.hideGui = true;
                minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                onServer(player -> {
                    ServerLevel level = player.server.getLevel(DIMENSION);
                    player.setGameMode(GameType.SPECTATOR);
                    player.teleportTo(level, 0.5, 35, -14.5, 180, 4);
                });
                yield true;
            }
            case 1 -> {
                if (local < 40 || !clientAt(-14.5) || !minecraft.level.hasChunkAt(DAOREN) || !minecraft.level.hasChunkAt(TABLE)) yield false;
                onServer(player -> {
                    ServerLevel level = player.serverLevel();
                    BlockState table = level.getBlockState(TABLE);
                    BlockState previous = level.getBlockState(DAOREN);
                    boolean furniture = id("beechfurniture_1701").equals(key(level.getBlockState(TABLE.below())));
                    boolean comprehension = id("canwutai_block").equals(key(table))
                            && table.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH;
                    expectTrue("保留参悟台与原支撑家具", furniture && comprehension);
                    // 只复原首入检查留下的一格探针，且仅在两件原摆件都吻合时执行。
                    if (furniture && comprehension && previous.is(Blocks.EMERALD_BLOCK)) {
                        BlockState restored = BuiltInRegistries.BLOCK.get(id("qiankundaoren")).defaultBlockState()
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH).setValue(QiankunBlock.ANIMATION, 0);
                        level.setBlock(DAOREN, restored, Block.UPDATE_ALL);
                    }
                    expect("正厅核心方块", id("qiankundaoren"), key(level.getBlockState(DAOREN)));
                });
                yield true;
            }
            case 2 -> {
                if (local < 20 || !clientAt(-14.5) || !id("qiankundaoren").equals(key(minecraft.level.getBlockState(DAOREN)))) yield false;
                expect("客户端乾坤道人中文", "乾坤道人", Component.translatable("block.yvanchuxiuzhen.qiankundaoren").getString());
                shot.accept("renjian-visual-01-hall.png");
                onServer(player -> player.teleportTo(player.serverLevel(), 1.2, 31, 1.2, 159, 4));
                yield true;
            }
            case 3 -> {
                if (local < 40 || !clientAt(1.2)) yield false;
                Entity steward = null;
                for (Entity entity : minecraft.level.entitiesForRendering()) {
                    if (STEWARD.equals(entity.getUUID())) {
                        steward = entity;
                        break;
                    }
                }
                if (steward == null) yield false;
                expect("客户端跟踪原大管家", id("daguanjia"), BuiltInRegistries.ENTITY_TYPE.getKey(steward.getType()));
                expect("原大管家中文", "大管家", steward.getType().getDescription().getString());
                expect("门户中文", "人间道", Component.translatable("item.yvanchuxiuzhen.dimensionrenjian").getString());
                shot.accept("renjian-visual-02-steward.png");
                finish();
                yield true;
            }
            default -> throw new IllegalStateException("未知机位 " + step);
        };
    }

    private boolean clientAt(double z) {
        return minecraft.screen == null && minecraft.level != null && DIMENSION.equals(minecraft.level.dimension())
                && minecraft.player != null && Math.abs(minecraft.player.getZ() - z) < 0.1;
    }

    private static ResourceLocation key(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path);
    }
}
