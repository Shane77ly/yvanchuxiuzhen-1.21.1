package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenu;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 新档内实际使用出身物品，由服务端 tick 启动会话，使用普通菜单继续按钮执行收尾。 */
final class BirthItemDialogueCheck extends SystemCheck {
    private static final String RELIC = "chuanchenglingwurenwu";
    private static final String TOKEN = "sxling";
    private int tokensBefore;
    private BlockPos probe;

    BirthItemDialogueCheck(List<String> report, String levelId) {
        super("出身物品对话", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(p -> {
                ItemStack relic = stack("ling_18");
                setCount(relic, 29);
                Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 30);
                p.setItemInHand(InteractionHand.MAIN_HAND, relic);
                use(p, true);
                expect("29 次潜行先走领取，不在本次启动", "", script(p));
                expect("领取后同一灵物增至 30", 30, relic.get(DataComponents.CUSTOM_DATA).copyTag().getInt("yikaiqi"));
                use(p, false);
                expect("30 次但未潜行不启动", "", script(p));
                use(p, true);
                expectTrue("旧次数 30 潜行启动", pending(p, RELIC));
            });
        } else if (step == 1) {
            if (!awaitFirst(RELIC, local)) return false;
            onServer(p -> {
                p.closeContainer();
                setCount(p.getMainHandItem(), 30.5);
                use(p, true);
                expectTrue("double 30.5 按来源 getInt 重新启动", pending(p, RELIC));
            });
        } else if (step == 2) {
            if (!awaitFirst(RELIC, local)) return false;
            onServer(p -> {
                expectTrue("新档没有符头3", !Fulu.knowledge(p).heads().contains(3));
                if (!advance(p, 12)) return;
                expect("传承结束后目录状态", "完成", DialogueActions.journal(p).getString(RELIC + "zhuangtai"));
                expect("传承储存步骤", 16, DialogueActions.journal(p).getInt(RELIC + "jindu"));
                expectTrue("裸命令不凭空解锁符头3", !Fulu.knowledge(p).heads().contains(3));
                expect("传承会话结束", "", script(p));
                p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                ItemStack token = stack("ling_09");
                p.setItemInHand(InteractionHand.OFF_HAND, token);
                p.gameMode.useItem(p, p.level(), token, InteractionHand.OFF_HAND);
                expect("仅副手升仙令不启动", "", script(p));
                p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                p.setItemInHand(InteractionHand.MAIN_HAND, token);
                tokensBefore = p.getInventory().countItem(token.getItem());
                expectTrue("接引起点为主世界", p.level().dimension().equals(Level.OVERWORLD));
                use(p, false);
                expectTrue("站立使用升仙令启动", pending(p, TOKEN));
            });
        } else if (step == 3) {
            if (!awaitFirst(TOKEN, local)) return false;
            onServer(p -> {
                p.closeContainer();
                use(p, true);
                expectTrue("潜行使用升仙令同样重新启动", pending(p, TOKEN));
            });
        } else if (step == 4) {
            if (!awaitFirst(TOKEN, local)) return false;
            onServer(p -> {
                if (!advance(p, 9)) return;
                expect("未经过传送门，剧情将玩家送入人间", ModId.VALUE + ":dimensionrenjian", p.level().dimension().location().toString());
                expect("剧情只扣一枚升仙令", tokensBefore - 1, p.getInventory().countItem(stack("ling_09").getItem()));
                expect("升仙令会话结束", "", script(p));
                probe = new BlockPos(p.getBlockX(), p.level().getMaxBuildHeight() - 2, p.getBlockZ());
                DialogueActions.request(p, "删除验证/delete_probe/坐标组/0/未完成/无/无%文字/删除前%删除方块/" + coords(probe) + "/" + coords(probe.east()) + "%结束");
            });
        } else {
            if (!awaitFirst("delete_probe", local)) return false;
            onServer(p -> {
                ServerLevel level = p.serverLevel();
                BlockPos pos = probe;
                if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.east())) {
                    fail("删除探针需要两个空位");
                    return;
                }
                try {
                    level.setBlockAndUpdate(pos, Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
                    level.setBlockAndUpdate(pos.east(), Blocks.STONE.defaultBlockState());
                    expectTrue("删除前探针含水", !level.getFluidState(pos).isEmpty());
                    if (!advance(p, 1)) return;
                    expectTrue("同一动作将含水方块和第二个坐标立即设为空气", level.getBlockState(pos).is(Blocks.AIR) && level.getBlockState(pos.east()).is(Blocks.AIR));
                    expect("删除后继续到结束", "", script(p));
                } finally {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());
                }
            });
            finish();
        }
        return true;
    }

    private boolean awaitFirst(String id, int local) {
        boolean[] ready = {false};
        onServer(p -> {
            CompoundTag s = DialogueActions.snapshot(p);
            ready[0] = p.containerMenu instanceof DialogueMenu && s.getString("renwulist").contains("/" + id + "/")
                    && !s.getBoolean("renwubegin") && s.getInt("renwuweizhi") == 1 && !s.getString("TXTxianshi").isEmpty();
        });
        if (!ready[0] && local > 200) {
            fail(id + " 未经服务端 tick 打开第一页");
            finish();
        }
        return ready[0];
    }

    private boolean advance(ServerPlayer p, int pages) {
        for (int i = 1; i <= pages; i++) {
            if (DialogueActions.snapshot(p).getInt("renwuweizhi") != i || !(p.containerMenu instanceof DialogueMenu menu) || !menu.clickMenuButton(p, 0)) {
                fail("无法从对话第 " + i + " 页继续");
                return false;
            }
        }
        return true;
    }

    private static boolean pending(ServerPlayer p, String id) {
        CompoundTag s = DialogueActions.snapshot(p);
        return s.getBoolean("renwubegin") && s.getInt("renwuweizhi") == 0 && s.getString("renwulist").contains("/" + id + "/");
    }

    private static void use(ServerPlayer p, boolean sneaking) {
        p.setShiftKeyDown(sneaking);
        p.gameMode.useItem(p, p.level(), p.getMainHandItem(), InteractionHand.MAIN_HAND);
        p.setShiftKeyDown(false);
    }

    private static void setCount(ItemStack stack, double count) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putDouble("yikaiqi", count));
    }

    private static ItemStack stack(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }

    private static String script(ServerPlayer p) { return DialogueActions.snapshot(p).getString("renwulist"); }
    private static String coords(BlockPos p) { return p.getX() + "/" + p.getY() + "/" + p.getZ(); }
}
