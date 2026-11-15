package net.yvanchuxiuzhen.smoke;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 九级灵石服用对灵力与待吸收修为的来源公式检查。 */
final class SpiritStoneCheck extends SystemCheck {
    private final Map<CultivationValue, Double> original = new EnumMap<>(CultivationValue.class);

    SpiritStoneCheck(List<String> report, String levelId) {
        super("灵石食用", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (local < 10) return false;
        if (step == 0) {
            onServer(player -> {
                for (CultivationValue value : CultivationValue.values()) original.put(value, Cultivation.get(player, value));
                Cultivation.set(player, CultivationValue.POWER, 0);
                Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, 0);
                Cultivation.set(player, CultivationValue.EXPERIENCE_RATE, 1);
            });
            return true;
        }
        if (step >= 1 && step <= 9) {
            int tier = step;
            onServer(player -> {
                double beforePower = Cultivation.get(player, CultivationValue.POWER);
                double beforeStand = Cultivation.get(player, CultivationValue.EXPERIENCE_STAND);
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "cailiao_19" + tier));
                ItemStack consumed = item.finishUsingItem(new ItemStack(item), player.serverLevel(), player);
                int power = 1 << ((tier - 1) * 2);
                expect("" + tier + "级灵石增加灵力", power, Cultivation.get(player, CultivationValue.POWER) - beforePower);
                expect("" + tier + "级灵石增加待吸收修为", tier == 1 ? 0 : power / 4,
                        Cultivation.get(player, CultivationValue.EXPERIENCE_STAND) - beforeStand);
                expectTrue("" + tier + "级灵石服用消耗一枚", consumed.isEmpty());
            });
            return true;
        }
        onServer(player -> {
            for (Map.Entry<CultivationValue, Double> entry : original.entrySet()) Cultivation.set(player, entry.getKey(), entry.getValue());
        });
        finish();
        return true;
    }
}
