package net.yvanchuxiuzhen.system.skill;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.api.WeaponStatEvent;
import net.yvanchuxiuzhen.system.skill.data.SkillData;

/** C 键的格挡显示计时（来源 Canjian 与 PlayerTick），不参与伤害结算。计时保存在玩家附件，剩余值同步给本人。 */
public final class SkillGuard {
    private static final TagKey<Item> SHIELDS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dunpai"));

    private SkillGuard() {
    }

    public static void press(ServerPlayer player) {
        if (player.getData(SkillData.GUARD_COOLDOWN) > 0) {
            return;
        }
        boolean shield = player.getOffhandItem().is(SHIELDS);
        double grade = level(player.getMainHandItem());
        if (shield) {
            grade += level(player.getOffhandItem());
        }
        setRemaining(player, (shield ? 21 : 20) + grade / 2);
        player.setData(SkillData.GUARD_COOLDOWN, 20.0);
    }

    public static void release(ServerPlayer player) {
        setRemaining(player, 0);
    }

    private static int level(ItemStack stack) {
        WeaponStatEvent event = new WeaponStatEvent(stack, "lianqizong.item_level_num");
        NeoForge.EVENT_BUS.post(event);
        return (int) event.total();
    }

    public static void tick(ServerPlayer player) {
        double cooldown = player.getData(SkillData.GUARD_COOLDOWN);
        if (cooldown > 0) {
            player.setData(SkillData.GUARD_COOLDOWN, cooldown - 1);
        }
        double remaining = player.getData(SkillData.GUARD_REMAINING);
        setRemaining(player, remaining > 0 ? remaining - 1 : remaining);
    }

    private static void setRemaining(ServerPlayer player, double value) {
        if (player.getData(SkillData.GUARD_REMAINING) != value) {
            player.setData(SkillData.GUARD_REMAINING, value);
        }
    }
}
