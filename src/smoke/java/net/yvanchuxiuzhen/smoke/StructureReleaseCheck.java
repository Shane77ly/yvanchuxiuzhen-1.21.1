package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.dungeon.DungeonPlayers;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import net.yvanchuxiuzhen.system.world.StructureExploration;

/** 真实管理命令检查：目标匹配、未知结构清理、固定模板放置与镇妖塔四十刻补充；不重跑副本战斗。 */
final class StructureReleaseCheck extends SystemCheck {
    private static final BlockPos ICE_SAMPLE = new BlockPos(-1315, 72, -456);
    private static final BlockPos MALFORMED = new BlockPos(1278, 83, -587);
    private static final List<BlockPos> TOWER = List.of(
            new BlockPos(1289, 88, -582), new BlockPos(1286, 88, -579),
            new BlockPos(1281, 88, -579), new BlockPos(1278, 88, -582),
            new BlockPos(1278, 88, -587), new BlockPos(1281, 88, -590),
            new BlockPos(1286, 88, -590), new BlockPos(1289, 88, -587),
            new BlockPos(1281, 83, -590), new BlockPos(1286, 83, -590),
            new BlockPos(1289, 83, -587), new BlockPos(1289, 83, -582),
            new BlockPos(1286, 83, -579), new BlockPos(1281, 83, -579),
            new BlockPos(1278, 83, -582), MALFORMED,
            new BlockPos(1291, 78, -581), new BlockPos(1287, 78, -577),
            new BlockPos(1280, 78, -577), new BlockPos(1276, 78, -581),
            new BlockPos(1276, 78, -588), new BlockPos(1280, 78, -592),
            new BlockPos(1287, 78, -592), new BlockPos(1291, 78, -588),
            new BlockPos(1292, 72, -580), new BlockPos(1288, 72, -576),
            new BlockPos(1279, 72, -576), new BlockPos(1275, 72, -580),
            new BlockPos(1275, 72, -589), new BlockPos(1279, 72, -593),
            new BlockPos(1288, 72, -593), new BlockPos(1292, 72, -589),
            new BlockPos(1279, 65, -576), new BlockPos(1276, 65, -577),
            new BlockPos(1275, 65, -580), new BlockPos(1275, 65, -589),
            new BlockPos(1276, 65, -592), new BlockPos(1279, 65, -593),
            new BlockPos(1288, 65, -593), new BlockPos(1291, 65, -592),
            new BlockPos(1292, 65, -589), new BlockPos(1292, 65, -580),
            new BlockPos(1291, 65, -577), new BlockPos(1288, 65, -576));
    private final List<String> messages = new ArrayList<>();
    private UUID localDrop;
    private UUID otherDrop;
    private int commandTick;
    private String beforeTower;
    private boolean paintingWasForced;

    StructureReleaseCheck(List<String> report, String levelId) {
        super("世界结构释放", report, levelId);
        NeoForge.EVENT_BUS.addListener(this::chat);
    }

    private void chat(ClientChatReceivedEvent event) {
        messages.add(event.getMessage().getString());
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(player -> {
                    player.serverLevel().setBlock(ICE_SAMPLE, Blocks.AIR.defaultBlockState(), 3);
                    command(player, "__不存在的玩家__ unlock sealrelease 冰封巨骨 true");
                    expectTrue("显示名不匹配时不放置固定结构", player.serverLevel().isEmptyBlock(ICE_SAMPLE));
                    StructureExploration.release(player.serverLevel(), target(player) + " unlock sealrelease 冰封巨骨", "镇妖塔");
                    expectTrue("裂隙中的多词玩家名不能改变解封目标", player.serverLevel().isEmptyBlock(ICE_SAMPLE));
                    localDrop = drop(player.serverLevel(), player.getX() + 8, player.getY() + 8, player.getZ());
                    ServerLevel painting = player.server.getLevel(DungeonPlayers.PAINTING);
                    paintingWasForced = painting.getForcedChunks().contains(new ChunkPos(0, 0).toLong());
                    painting.setChunkForced(0, 0, true);
                    painting.getChunk(0, 0);
                    otherDrop = drop(painting, 0.5, 160, 0.5);
                });
                return true;
            }
            case 1 -> {
                boolean[] visible = new boolean[1];
                onServer(player -> visible[0] = player.serverLevel().getEntity(localDrop) != null
                        && player.server.getLevel(DungeonPlayers.PAINTING).getEntity(otherDrop) != null);
                if (!visible[0]) {
                    if (local > 200) {
                        fail("夹具掉落物未进入两个维度的已加载实体集合");
                        finish();
                    }
                    return false;
                }
                onServer(player -> {
                    command(player, target(player) + " unlock sealrelease __未知结构__ true");
                    commandTick = player.server.getTickCount();
                    expectTrue("未知结构调用后掉落物不立即清理", player.serverLevel().getEntity(localDrop) != null);
                });
                return true;
            }
            case 2 -> {
                if (!elapsed(14, local)) return false;
                onServer(player -> {
                    Entity localItem = player.serverLevel().getEntity(localDrop);
                    expectTrue("未知结构仍执行十刻掉落物清理", localItem == null || localItem.isRemoved());
                    Entity remote = player.server.getLevel(DungeonPlayers.PAINTING).getEntity(otherDrop);
                    expectTrue("无空间限定的来源命令跨维度清理已加载掉落物", remote == null || remote.isRemoved());
                    if (remote != null) remote.discard();
                    if (!paintingWasForced) player.server.getLevel(DungeonPlayers.PAINTING).setChunkForced(0, 0, false);
                    StructureExploration.release(player.serverLevel(), target(player), "冰封巨骨 true");
                    expect("冰封巨骨固定坐标的来源方块", "yvanchuxiuzhen:iceboundrock",
                            BuiltInRegistries.BLOCK.getKey(player.serverLevel().getBlockState(ICE_SAMPLE).getBlock()).toString());
                    commandTick = player.server.getTickCount();
                });
                return true;
            }
            case 3 -> {
                if (!elapsed(15, local)) return false;
                if (messages.stream().noneMatch(text -> text.contains("解锁结构冰封巨骨")) && local <= 200) return false;
                expectTrue("找不到玩家的中文提示", messages.stream().anyMatch(text -> text.contains("未找到名为")));
                expectTrue("来源解锁中文提示含实际结构名", messages.stream().anyMatch(text -> text.contains("解锁结构冰封巨骨")));
                onServer(player -> {
                    command(player, target(player) + " unlock sealrelease 镇妖塔 true");
                    int present = 0;
                    for (BlockPos pos : TOWER) if (player.serverLevel().getBlockEntity(pos) instanceof DungeonBlockEntity) present++;
                    expect("真实模板放置四十四个副本入口", 44, present);
                    DungeonBlockEntity first = (DungeonBlockEntity) player.serverLevel().getBlockEntity(TOWER.getFirst());
                    beforeTower = first.state().getString("fubenshujv");
                    commandTick = player.server.getTickCount();
                });
                return true;
            }
            case 4 -> {
                if (!elapsed(45, local)) return false;
                onServer(player -> {
                    int configured = 0;
                    int fullHeaders = 0;
                    int floors = 0;
                    for (BlockPos pos : TOWER) {
                        if (player.serverLevel().getBlockEntity(pos) instanceof DungeonBlockEntity block) {
                            String definition = block.state().getString("fubenshujv");
                            if (!definition.isEmpty()) configured++;
                            if (definition.contains("%")) fullHeaders++;
                        }
                        if (BuiltInRegistries.BLOCK.getKey(player.serverLevel().getBlockState(pos.below()).getBlock()).getPath().equals("ginkgofloor")) floors++;
                    }
                    expect("四十刻后四十四点均补充来源定义", 44, configured);
                    expect("来源四十三份波次头保持", 43, fullHeaders);
                    expect("四十四点楼板均为坐标Y减一", 44, floors);
                    String after = ((DungeonBlockEntity) player.serverLevel().getBlockEntity(TOWER.getFirst())).state().getString("fubenshujv");
                    expectTrue("四十刻后处理已覆盖模板中的原定义", !after.equals(beforeTower));
                    String broken = ((DungeonBlockEntity) player.serverLevel().getBlockEntity(MALFORMED)).state().getString("fubenshujv");
                    expectTrue("Q81畸形定义保持且未自行补造波次头", !broken.isEmpty() && !broken.contains("%"));
                    player.closeContainer();
                });
                finish();
                return true;
            }
            default -> throw new IllegalStateException("未知结构检查步骤 " + step);
        }
    }

    private boolean elapsed(int ticks, int local) {
        int[] passed = new int[1];
        onServer(player -> passed[0] = player.server.getTickCount() - commandTick);
        if (passed[0] >= ticks) return true;
        if (local > 800) {
            fail("服务端计划刻未推进：" + passed[0]);
            finish();
        }
        return false;
    }

    private static String target(ServerPlayer player) {
        return player.getDisplayName().getString();
    }

    private static void command(ServerPlayer player, String arguments) {
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4).withSuppressedOutput(), "yvanchu " + arguments);
    }

    private static UUID drop(ServerLevel level, double x, double y, double z) {
        ItemEntity item = new ItemEntity(level, x, y, z, new ItemStack(Items.DIAMOND));
        item.setNoGravity(true);
        item.setNeverPickUp();
        if (!level.addFreshEntity(item)) throw new IllegalStateException("结构检查夹具未能添加掉落物");
        return item.getUUID();
    }
}
