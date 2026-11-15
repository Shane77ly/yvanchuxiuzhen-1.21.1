package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.xiulian.RetreatActions;
import net.yvanchuxiuzhen.system.xiulian.api.RetreatPageEvent;

/** 修炼、观想和参悟三页共享原生背包，子页切换保持来源菜单坐标。 */
public final class RetreatMenu extends AbstractContainerMenu {
    private static final List<String> CULTIVATION_HELP = List.of(
            "当您装备了功法，且坐下（骑乘）后，便可以修炼了。",
            "修炼会直接增加您的修为，不同群系，不同维度也会影响修炼的效率，越是繁荣的群系，修炼效率越高。",
            "不过请不要尝试只靠修炼提升到化神，因为您还需要渡劫，需要置办渡劫物资。",
            "如果您的心湖无法平静，则出门走走吧，看看风景，抓抓妖兽，过一段时间就好了。");
    private static final List<String> MEDITATION_HELP = List.of(
            "消耗三天寿元，恢复一些生命值，同时让自身灵力、精力补满，也会解除醉酒状态。");
    private static final List<String> STUDY_HELP = List.of(
            "此界面，专门用于升级功法等级。",
            "功法的品阶是固定不变的，但功法等级会随着您的修炼而不断精进。品阶代表着功法的上限，等级代表着当前您能发挥出的实力。",
            "您应该发现了，在修炼时您的功法经验会慢慢增加，直到经验达到上限后，功法就会升级。而专注于功法修炼只会大幅增加您功法经验，并不会影响您的修为，且功法修炼没有时限。");
    private final BlockPos pos;
    private final int mode;

    public RetreatMenu(int id, Inventory inventory, int mode) {
        this(id, inventory, inventory.player.blockPosition().above(), mode);
    }

    public RetreatMenu(int id, Inventory inventory, BlockPos pos, int mode) {
        super(switch (mode) {
            case 6 -> XiulianMenus.RETREAT.get();
            case 7 -> XiulianMenus.MEDITATION.get();
            case 8 -> XiulianMenus.STUDY.get();
            default -> throw new IllegalArgumentException("未知修炼子页：" + mode);
        }, id);
        this.pos = pos.immutable();
        this.mode = mode;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 251 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 309));
    }

    public int mode() {
        return mode;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || !player.level().hasChunkAt(pos)) return false;
        if (button == 0) {
            if (mode == 7) RetreatActions.meditate(serverPlayer);
            else RetreatActions.start(serverPlayer, mode == 8);
            return true;
        }
        if (mode == 7) {
            if (button >= 1 && button <= 5) {
                NeoForge.EVENT_BUS.post(new RetreatPageEvent(serverPlayer, button, pos));
            } else if (button == 6 || button == 8) {
                RetreatActions.open(serverPlayer, pos, button == 6 ? 6 : 8);
            } else if (button == 7) {
                help(serverPlayer, MEDITATION_HELP);
            } else return false;
        } else if (button >= 1 && button <= 6) {
            RetreatActions.adjustDays(serverPlayer, button);
        } else if (button >= 7 && button <= 11) {
            NeoForge.EVENT_BUS.post(new RetreatPageEvent(serverPlayer, button - 6, pos));
        } else if (button == (mode == 6 ? 12 : 13)) {
            RetreatActions.open(serverPlayer, pos, 7);
        } else if (button == (mode == 6 ? 13 : 12)) {
            help(serverPlayer, mode == 6 ? CULTIVATION_HELP : STUDY_HELP);
        } else return false;
        return true;
    }

    private static void help(ServerPlayer player, List<String> lines) {
        lines.forEach(line -> player.displayClientMessage(Component.literal(line), false));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        if (index < 27) moveItemStackTo(stack, 27, 36, true);
        else moveItemStackTo(stack, 0, 27, false);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
