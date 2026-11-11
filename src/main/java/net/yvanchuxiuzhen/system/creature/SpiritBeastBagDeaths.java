package net.yvanchuxiuzhen.system.creature;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.scores.Objective;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 来源Lingshoudaiqingkong：同维度主人首个同UUID袋重建，以及召唤物死亡粒子与移除。 */
public final class SpiritBeastBagDeaths {
    private static final TagKey<Item> BAGS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshoudai"));

    private SpiritBeastBagDeaths() {
    }

    public static void afterDeath(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        clearLinkedBag(level, entity);
        Objective objective = level.getScoreboard().getObjective("zhaohuanwu");
        if (objective == null || level.getScoreboard().getOrCreatePlayerScore(entity, objective).get() != 1) return;
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (type.getNamespace().equals(ModId.VALUE)) {
            switch (type.getPath()) {
                case "yaoshou_007" -> {
                    particle(level, entity, "brokenblue", 25);
                    particle(level, entity, "brokenpurple", 10);
                }
                case "yaoshou_020" -> particle(level, entity, "brokendeeporange", 25);
                case "yaoshou_013" -> {
                    particle(level, entity, "brokenblack", 15);
                    particle(level, entity, "smokeblack", 20);
                }
            }
        }
        entity.discard();
    }

    private static void clearLinkedBag(ServerLevel level, Entity entity) {
        String bagUuid = entity.getPersistentData().getString("lingshoudaiuuid");
        if (bagUuid.isEmpty()) return;
        String ownerUuid = entity.getPersistentData().getString("belongToMasterUUID");
        for (Player player : level.players()) {
            if (!player.getStringUUID().equals(ownerUuid)) continue;
            // 来源Player仅在存活时提供空朝向物品能力；该能力依次包含主背包、护甲与副手。
            if (player.isAlive()) clearFirstMatchingBag(player.getCapability(Capabilities.ItemHandler.ENTITY), bagUuid);
            return;
        }
    }

    private static void clearFirstMatchingBag(IItemHandler inventory, String bagUuid) {
        if (inventory == null) return;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack previous = inventory.getStackInSlot(slot);
            if (!previous.is(BAGS) || !previous.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                    .getString("lingshoudaiuuid").equals(bagUuid)) continue;
            ItemStack replacement = new ItemStack(previous.getItem());
            String name = previous.getDisplayName().getString().replace("[", "").replace("]", "");
            replacement.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            if (inventory instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot(slot, replacement);
            return;
        }
    }

    private static void particle(ServerLevel level, Entity entity, String id, int count) {
        level.sendParticles(Particles.type(id), entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                count, entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth(), 0.1);
    }
}
