package net.yvanchuxiuzhen.system.fulu;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.LethalDamageEvent;

/** 承担替死符在致命伤害前的保护与消耗。 */
public final class FuluDeathWard {
    private static final String TALISMAN_ID = "itemfulu_16";

    private FuluDeathWard() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FuluDeathWard::onLethalDamage);
    }

    public static void onLethalDamage(LethalDamageEvent event) {
        if (!(event.victim() instanceof ServerPlayer player) || player.getHealth() >= event.amount()) {
            return;
        }

        Item talisman = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, TALISMAN_ID));
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || stack.getItem() != talisman) {
                continue;
            }

            double grade = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("fululevel");
            inventory.removeItem(slot, 1);

            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY(), player.getZ(), 20, 0, 0, 0, 0.5);
            level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 1, 1);
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
            player.setHealth((float) (player.getHealth() + (grade + 1) * 10));
            event.setSurvived();
            return;
        }
    }
}
