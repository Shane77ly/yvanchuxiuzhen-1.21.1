package net.yvanchuxiuzhen.system.creature;

import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;

/** 已驯服活体妖兽的公共交互门槛与普通饲喂、亲近反馈。 */
final class YaoshouInteractions {
    private static final TagKey<Item> FOOD = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "food"));
    private static final Set<String> PRIOR_ITEMS = Set.of("fabao_015", "duedan", "tiandao_np_csetitem");

    private YaoshouInteractions() {
    }

    static boolean accepts(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || event.getHand() != player.getUsedItemHand()) return false;
        ResourceLocation held = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem());
        if (held.getNamespace().equals(ModId.VALUE) && PRIOR_ITEMS.contains(held.getPath())) return false;
        Entity target = event.getTarget();
        return target instanceof YaoshouEntity beast && beast.isTame() && beast.isAlive();
    }

    static void interact(Player player, TamableAnimal beast) {
        ItemStack held = player.getMainHandItem();
        if (held.is(FOOD)) {
            particles(beast);
            beast.setHealth(beast.getHealth() + 6);
            held.shrink(1);
            player.displayClientMessage(Component.translatable("message." + ModId.VALUE + ".yaoshou_interaction.healed"), false);
        } else {
            double itemTameness = held.getOrDefault(CreatureData.TAME_VALUE.get(), 0.0);
            if (itemTameness > 0 && itemTameness < 100) {
                particles(beast);
                beast.getPersistentData().putDouble("tame_num", beast.getPersistentData().getDouble("tame_num") + 5);
                held.shrink(1);
                player.displayClientMessage(Component.translatable("message." + ModId.VALUE + ".yaoshou_interaction.obedient"), false);
            } else {
                player.displayClientMessage(Component.translatable("message." + ModId.VALUE + ".yaoshou_interaction.greeting",
                        beast.getDisplayName().getString()), false);
            }
        }
    }

    private static void particles(TamableAnimal beast) {
        ((ServerLevel) beast.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                beast.getX(), beast.getY() + 0.5, beast.getZ(), 6, 1, 1, 1, 0.1);
    }
}
