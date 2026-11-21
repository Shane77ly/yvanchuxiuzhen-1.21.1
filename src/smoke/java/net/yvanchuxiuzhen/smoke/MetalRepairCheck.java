package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;

/** 比较同阶双锭和异阶锭，并通过铁砧的结果槽核对真实扣料。 */
final class MetalRepairCheck extends SystemCheck {
    MetalRepairCheck(List<String> report, String levelId) {
        super("金属锭铁砧修理", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(this::repair);
        finish();
        return true;
    }

    private void repair(ServerPlayer player) {
        for (int n : new int[]{202, 204, 212, 214, 206, 218}) {
            int grade = n % 10;
            ItemStack ingot = stack("cailiao_" + n);
            ItemStack wrong = stack("cailiao_201");
            for (String prefix : List.of("weaponsword_0", "weaponepee_0", "weaponspear_0", "weaponspeedbow_0", "weaponlongbow_0", "weaponbroadsword_0")) {
                checkMaterial(prefix + grade, ingot, wrong);
            }
            String tool = "gongjv_0" + (n - 190);
            checkMaterial(tool, ingot, wrong);
            AnvilMenu menu = new AnvilMenu(41, player.getInventory());
            ItemStack damaged = stack(tool);
            damaged.setDamageValue(1);
            menu.getSlot(0).set(damaged);
            menu.getSlot(1).set(wrong);
            expectTrue(tool + " 异阶无修理结果", !menu.getSlot(2).hasItem());
            ingot.setCount(2);
            menu.getSlot(1).set(ingot);
            menu.clicked(2, 0, ClickType.PICKUP, player);
            ItemStack repaired = menu.getCarried();
            expectTrue(tool + " 取出修复原件并扣一锭", repaired.is(damaged.getItem()) && repaired.getDamageValue() == 0
                    && !menu.getSlot(0).hasItem() && menu.getSlot(1).getItem().getCount() == 1);
        }
    }

    private void checkMaterial(String name, ItemStack ingot, ItemStack wrong) {
        ItemStack item = stack(name);
        expectTrue(name + " 接受本阶锭并拒绝异阶", item.getItem().isValidRepairItem(item, ingot)
                && !item.getItem().isValidRepairItem(item, wrong));
    }

    private static ItemStack stack(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name)));
    }
}
