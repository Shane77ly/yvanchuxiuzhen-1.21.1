package net.yvanchuxiuzhen.system.equipment;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.api.BenmingData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.xiulian.api.BenmingProgress;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 本命器物的在线培养；每个修炼推进周期处理一次玩家丹田槽内的物品。 */
final class BenmingCultivation {
    private static final TagKey<Item> YISPEAR = tag("yispear");
    private static final TagKey<Item> YIBOW = tag("yibow");
    private static final TagKey<Item> DAOJIA = tag("daojiaweapon");
    private static final TagKey<Item> FOJIAO = tag("fojiaoweapon");
    private static final TagKey<Item> RUJIA = tag("rujiaweapon");

    private BenmingCultivation() {
    }

    static void onCultivationSecond(CultivationSecondEvent event) {
        ServerPlayer player = event.player();
        ItemStack stack = Cultivation.benming(player);
        if (stack.isEmpty() || Cultivation.get(player, CultivationValue.LEVEL_NUMBER) < 21) {
            setProgress(player, stack, false);
            return;
        }

        BenmingData progress = stack.getOrDefault(EquipmentData.BENMING.get(), BenmingData.initial());
        double levelLimit = Cultivation.get(player, CultivationValue.LEVEL_NUMBER)
                + Cultivation.tizhiDouble(player, "LevelNumberCorrect");
        if (progress.level() < levelLimit) {
            if (progress.experienceMax() <= 0) {
                progress = new BenmingData(progress.level(), progress.experience(), 2000 + progress.level() * 2000D);
                stack.set(EquipmentData.BENMING.get(), progress);
            }
            double gain = Enchants.level(player, stack, Enchants.key("qiling")) + extraSpeed(player, stack);
            double experience = progress.experience() + gain;
            if (experience >= progress.experienceMax()) {
                experience -= progress.experienceMax();
                int level = progress.level() + 1;
                stack.set(EquipmentData.BENMING.get(), new BenmingData(level, experience, 2000 + level * 2000D));
                increaseAttack(player, stack);
            } else {
                stack.set(EquipmentData.BENMING.get(), new BenmingData(progress.level(), experience, progress.experienceMax()));
            }
        } else if (!stack.has(EquipmentData.BENMING.get())) {
            stack.set(EquipmentData.BENMING.get(), progress);
        }
        if (stack.getDamageValue() > 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
        Cultivation.setBenming(player, stack);
        setProgress(player, stack, true);
    }

    private static double extraSpeed(ServerPlayer player, ItemStack stack) {
        double speed = Cultivation.tizhiDouble(player, "WeaponCultivateSpeed");
        if (stack.getItem() instanceof SwordItem) speed += Cultivation.tizhiDouble(player, "ShortWeaponCultivateSpeed");
        if (stack.is(YISPEAR)) speed += Cultivation.tizhiDouble(player, "LongWeaponCultivateSpeed");
        if (stack.is(YIBOW)) speed += Cultivation.tizhiDouble(player, "RemoteWeaponCultivateSpeed");
        if (stack.is(DAOJIA)) speed += Cultivation.tizhiDouble(player, "TaoismWeaponCultivateSpeed");
        if (stack.is(FOJIAO)) speed += Cultivation.tizhiDouble(player, "BuddhismWeaponCultivateSpeed");
        if (stack.is(RUJIA)) speed += Cultivation.tizhiDouble(player, "ConfucianWeaponCultivateSpeed");
        return speed;
    }

    private static void increaseAttack(ServerPlayer player, ItemStack stack) {
        boolean spell = Cultivation.tizhiBoolean(player, "benmingfabaojioupanduan");
        EquipmentRefining.cultivateAttack(stack, spell);
        Cultivation.setTizhiBoolean(player, "benmingfabaojioupanduan", !spell);
    }

    private static void setProgress(ServerPlayer player, ItemStack stack, boolean owned) {
        BenmingData data = stack.get(EquipmentData.BENMING.get());
        BenmingProgress next = owned && data != null
                ? new BenmingProgress(true, data.level(), data.experience(), data.experienceMax()) : BenmingProgress.NONE;
        if (!next.equals(Cultivation.benmingProgress(player))) {
            Cultivation.setBenmingProgress(player, next);
        }
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
