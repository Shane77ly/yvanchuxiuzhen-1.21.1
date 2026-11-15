package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.api.BenmingData;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.xiulian.api.BenmingProgress;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/** 经受控修炼秒事件验证本命培养门槛、增长、修复、存档与本人同步；不替代自然秒调度和丹田操作。 */
final class BenmingCultivationCheck extends SystemCheck {
    private CultivationData original;
    private ItemStack expected;

    BenmingCultivationCheck(List<String> report, String levelId) {
        super("本命培养", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step > 0 && local < 10) return false;
        switch (step) {
            case 0 -> onServer(player -> {
                original = player.getData(XiulianAttachments.CULTIVATION);
                player.setData(XiulianAttachments.CULTIVATION, new CultivationData());
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 20);
                Cultivation.set(player, CultivationValue.LIFETIME, 1000);
                Cultivation.setTizhiDouble(player, "WeaponCultivateSpeed", 3);
                Cultivation.setTizhiDouble(player, "ShortWeaponCultivateSpeed", 5);
                ItemStack sword = new ItemStack(Items.IRON_SWORD);
                sword.enchant(player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key("qiling")), 2);
                sword.setDamageValue(10);
                sword.set(EquipmentData.BENMING.get(), new BenmingData(0, 1995, 2000));
                sword.set(EquipmentData.STATS.get(), EquipmentStats.EMPTY.addSpellAttack(2).addSoulAttack(4));
                CompoundTag raw = new CompoundTag();
                raw.putInt("mana_Att", 2);
                raw.putInt("soul_Att", 4);
                raw.putInt("mananum", 200);
                raw.putInt("soulnum", 400);
                sword.set(EquipmentData.REFINEMENT.get(), CustomData.of(raw));
                Cultivation.setBenming(player, sword);
                NeoForge.EVENT_BUS.post(new CultivationSecondEvent(player));
                expect("境界20不推进本命经验", new BenmingData(0, 1995, 2000), progress(player));
                expect("境界20不修复耐久", 10, Cultivation.benming(player).getDamageValue());
                expect("未到结丹不显示培养进度", BenmingProgress.NONE, Cultivation.benmingProgress(player));

                pulse(player, 21);
                expect("器灵2加通用3和短兵器5升级并保留余量", new BenmingData(1, 5, 4000), progress(player));
                expectAttacks(player, "首次培养增加精神攻击", 2, 5, 200, 500);
                expect("培养后修复一点耐久", 9, Cultivation.benming(player).getDamageValue());
                expectTrue("首次升级切换为下次法攻", Cultivation.tizhiBoolean(player, "benmingfabaojioupanduan"));
                expect("升级进度提交丹田显示", new BenmingProgress(true, 1, 5, 4000), Cultivation.benmingProgress(player));

                seed(player, new BenmingData(1, 3995, 4000));
                pulse(player, 21);
                expect("第二次升级重算6000上限", new BenmingData(2, 5, 6000), progress(player));
                expectAttacks(player, "第二次培养交替增加法术攻击", 3, 5, 300, 500);
                expectTrue("第二次升级切回下次精神攻击", !Cultivation.tizhiBoolean(player, "benmingfabaojioupanduan"));

                seed(player, new BenmingData(2, 12000, 6000));
                pulse(player, 21);
                expect("单次事件只升一级并保留超过旧上限的余量", new BenmingData(3, 6010, 8000), progress(player));

                seed(player, new BenmingData(21, 25, 44000));
                pulse(player, 21);
                expect("本命达到玩家等级时停止增长", new BenmingData(21, 25, 44000), progress(player));
                expect("达到等级上限仍修复耐久", 6, Cultivation.benming(player).getDamageValue());
                Cultivation.setTizhiDouble(player, "LevelNumberCorrect", 1);
                pulse(player, 21);
                expect("等级修正1允许继续培养", new BenmingData(21, 35, 44000), progress(player));

                seed(player, new BenmingData(2, 7, 0));
                pulse(player, 21);
                expect("缺失上限按当前等级先初始化再增长", new BenmingData(2, 17, 6000), progress(player));
                ItemStack cultivated = Cultivation.benming(player);
                ItemStack restored = ItemStack.parse(player.registryAccess(), cultivated.save(player.registryAccess())).orElseThrow();
                expectTrue("完整本命组件和耐久保存读回一致", ItemStack.matches(cultivated, restored));
                expected = restored.copy();
                Cultivation.setBenming(player, ItemStack.EMPTY);
                NeoForge.EVENT_BUS.post(new CultivationSecondEvent(player));
                expect("空本命槽清除培养显示", BenmingProgress.NONE, Cultivation.benmingProgress(player));
                Cultivation.setBenming(player, restored);
            });
            case 1 -> {
                expectTrue("本人同步本命完整物品与耐久", ItemStack.matches(expected, Cultivation.benming(minecraft.player)));
                expect("本人同步本命等级经验上限", new BenmingData(2, 17, 6000), progress(minecraft.player));
                onServer(player -> {
                    player.setData(XiulianAttachments.CULTIVATION, original);
                    player.syncData(XiulianAttachments.CULTIVATION);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知本命培养检查步骤 " + step);
        }
        return true;
    }

    private static void seed(ServerPlayer player, BenmingData progress) {
        ItemStack stack = Cultivation.benming(player);
        stack.set(EquipmentData.BENMING.get(), progress);
        Cultivation.setBenming(player, stack);
    }

    private static void pulse(ServerPlayer player, double level) {
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, level);
        NeoForge.EVENT_BUS.post(new CultivationSecondEvent(player));
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 20);
    }

    private static BenmingData progress(Player player) {
        return Cultivation.benming(player).getOrDefault(EquipmentData.BENMING.get(), BenmingData.EMPTY);
    }

    private void expectAttacks(ServerPlayer player, String label, int spell, int soul, int spellRaw, int soulRaw) {
        ItemStack stack = Cultivation.benming(player);
        EquipmentStats stats = stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY);
        CompoundTag raw = stack.getOrDefault(EquipmentData.REFINEMENT.get(), CustomData.EMPTY).copyTag();
        expectTrue(label + "且原料累计量增加100", stats.spellAttack() == spell && stats.soulAttack() == soul
                && raw.getInt("mana_Att") == spell && raw.getInt("soul_Att") == soul
                && raw.getInt("mananum") == spellRaw && raw.getInt("soulnum") == soulRaw);
    }
}
