package net.yvanchuxiuzhen.flow;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 玩家首次受到缓慢时按护甲与饰品的挣脱附魔缩短持续时间。 */
public final class SlownessResistanceFlow {
    private static final ResourceKey<Enchantment> BREAKFREE = Enchants.key("breakfree");
    private static final String PROCESSED = "slownessget";

    private SlownessResistanceFlow() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SlownessResistanceFlow::onTick);
    }

    private static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Holder<MobEffect> slowness = MobEffects.MOVEMENT_SLOWDOWN;
        MobEffectInstance effect = player.getEffect(slowness);
        boolean hasPlayerData = !(player instanceof FakePlayer);
        boolean processed = hasPlayerData && Cultivation.tizhiBoolean(player, PROCESSED);
        if (effect != null && !processed) {
            int duration = effect.getDuration();
            int amplifier = effect.getAmplifier();
            player.removeEffect(slowness);
            int reduction = Enchants.armorSum(player, BREAKFREE);
            if (hasPlayerData) {
                for (ItemStack accessory : Equipment.accessories(player).items()) {
                    reduction += Enchants.level(player, accessory, BREAKFREE);
                }
            }
            if (duration > reduction * 20.0) {
                player.addEffect(new MobEffectInstance(slowness, (int) (duration - reduction * 20.0), amplifier));
            }
            if (hasPlayerData) Cultivation.setTizhiBoolean(player, PROCESSED, true);
        } else if (effect == null && processed) {
            Cultivation.setTizhiBoolean(player, PROCESSED, false);
        }
    }
}
