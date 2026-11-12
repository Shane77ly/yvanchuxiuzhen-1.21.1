package net.yvanchuxiuzhen.system.skill;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 箭种轮盘的十一项选择（来源技能界面按钮 6–16），库存与灵力条件由服务端判定；选择本身不扣资源。 */
public final class ArrowSelection {
    private ArrowSelection() {
    }

    private static Item item(int choice) {
        return switch (choice) {
            case 1 -> skillArrow(1);
            case 2 -> Items.ARROW;
            case 3 -> skillArrow(2);
            case 4, 5, 6, 7, 8, 9, 10 -> skillArrow(choice - 1);
            case 11 -> Items.SPECTRAL_ARROW;
            default -> throw new IllegalArgumentException("箭种选择：" + choice);
        };
    }

    private static Item skillArrow(int kind) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_arrow_%02d".formatted(kind)));
    }

    public static boolean available(Player player, int choice) {
        if (choice == 3) {
            return Cultivation.get(player, CultivationValue.POWER) >= Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 5;
        }
        return player.getInventory().contains(new ItemStack(item(choice)));
    }

    public static boolean select(ServerPlayer player, int choice) {
        if (!available(player, choice)) {
            return false;
        }
        player.setData(SkillData.STATE, Skills.state(player).withArrow(new ItemStack(item(choice)), choice));
        player.closeContainer();
        return true;
    }
}
