package net.yvanchuxiuzhen.system.skill.content;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.skill.SkillMenuActions;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 来源 JinengGuiMenu 没有物品槽。按钮经原生菜单消息绑定当前容器，在服务端执行选择、翻页与功能入口。 */
public final class SkillMenu extends AbstractContainerMenu {
    public SkillMenu(int id, Inventory inventory) {
        super(SkillMenus.SKILL.get(), id);
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || button < 0 || button > 23) {
            return false;
        }
        if (button < 4) {
            SkillSelection.select(serverPlayer, button + 1);
        } else if (button == 4 || button == 5) {
            SkillSelection.turnPage(serverPlayer, button == 4 ? 1 : -1);
        } else if (button <= 16) {
            Skills.selectArrow(serverPlayer, button - 5);
        } else {
            switch (button) {
                case 17 -> SkillSelection.toggleCombo(serverPlayer);
                case 18 -> Skills.openVoidRift(serverPlayer);
                case 20 -> SkillMenuActions.explode(serverPlayer);
                case 21 -> Skills.toggleYuanying(serverPlayer);
                case 22 -> SkillMenuActions.scan(serverPlayer);
                case 23 -> SkillMenuActions.summonFire(serverPlayer);
                default -> { return false; }
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
