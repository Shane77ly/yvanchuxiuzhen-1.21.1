package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.BirthMenu;

/** 验证轮回符确认、跨系统清理和延迟出身菜单。 */
final class ReincarnationCheck extends SystemCheck {
    ReincarnationCheck(List<String> report, String levelId) { super("轮回生命周期", report, levelId); }

    @Override protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> {
                onServer(player -> {
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 12);
                    Cultivation.set(player, CultivationValue.EXPERIENCE, 70);
                    for (Profession profession : Profession.values()) Professions.setLevel(player, profession, 5);
                    Skills.setSpell(player, 1, new ItemStack(Items.DIAMOND));
                    Techniques.setSlot(player, new ItemStack(Items.EMERALD));
                    player.getInventory().setItem(3, new ItemStack(Items.GOLD_INGOT, 4));
                    ItemStack talisman = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "itemfulu_37")));
                    player.setItemInHand(InteractionHand.MAIN_HAND, talisman);
                    for (int i = 0; i < 3; i++) talisman.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
                    expect("前三次确认保留境界", 12, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
                    expectTrue("前三次确认保留物品", !player.getInventory().getItem(3).isEmpty());
                    talisman.getItem().use(player.level(), player, InteractionHand.MAIN_HAND);
                    expect("第四次确认重置境界", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
                    expect("第四次确认重置修为", 0, Cultivation.get(player, CultivationValue.EXPERIENCE));
                    expectTrue("清除玩家物品", player.getInventory().getItem(3).isEmpty());
                    expectTrue("清除技能与功法槽", Skills.spell(player, 1).isEmpty() && Techniques.slot(player).isEmpty());
                    boolean professionsReset = true;
                    for (Profession profession : Profession.values()) professionsReset &= Professions.level(player, profession) == 0;
                    expectTrue("十职业等级复位", professionsReset);
                });
                return true;
            }
            case 1 -> {
                if (local < 30) return false;
                onServer(player -> expectTrue("延迟打开出身菜单", player.containerMenu instanceof BirthMenu));
                finish();
                return true;
            }
            default -> { finish(); return true; }
        }
    }
}
