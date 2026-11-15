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
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.SkillPoint;
import net.yvanchuxiuzhen.system.xiulian.api.SkillPointPageEvent;

/** 来源 PlayerGUI02 的 36 格背包与六项属性加点，写入只经修炼公开操作执行。 */
public final class SkillPointMenu extends AbstractContainerMenu {
    private static final List<SkillPoint> POINTS = List.of(SkillPoint.HEALTH_MAX, SkillPoint.POWER_MAX,
            SkillPoint.SOUL_MAX, SkillPoint.ATTACK, SkillPoint.MANA, SkillPoint.SOUL);
    private static final List<String> HELP = List.of("消耗属性点，永久增加属性值。",
            "境界提升不会直接增加属性值，只会给您若干属性点，需要您自行分配。",
            "注意！选定离手，没有后悔的余地，除非重入轮回，否则没有洗点重选的能力。",
            "当前精神伤害类秘法的技能较少，慎重选择。");
    private final BlockPos pos;

    public SkillPointMenu(int id, Inventory inventory, BlockPos pos) {
        super(XiulianMenus.SKILL_POINTS.get(), id);
        this.pos = pos.immutable();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 251 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 309));
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || !player.level().hasChunkAt(pos)) return false;
        if (button >= 0 && button < POINTS.size()) {
            Cultivation.spendSkillPoint(serverPlayer, POINTS.get(button));
            return true;
        }
        int page = switch (button) {
            case 6 -> 1;
            case 7 -> 3;
            case 8 -> 4;
            case 9 -> 5;
            case 10 -> 6;
            default -> 0;
        };
        if (page != 0) {
            NeoForge.EVENT_BUS.post(new SkillPointPageEvent(serverPlayer, page, pos));
            return true;
        }
        if (button == 11) {
            for (String line : HELP) serverPlayer.displayClientMessage(Component.literal(line), false);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        if (index < 27) moveItemStackTo(stack, 27, 36, true);
        else moveItemStackTo(stack, 0, 27, false);
        // 来源无自定义槽的分支搬移后直接返回 EMPTY。
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
