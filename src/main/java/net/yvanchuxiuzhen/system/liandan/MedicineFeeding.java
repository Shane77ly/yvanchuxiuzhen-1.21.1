package net.yvanchuxiuzhen.system.liandan;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.liandan.content.DuedanItem;

/** 毒厄丹优先处理生物喂用，药瓶潜行喂药沿用来源的任务交互分派条件。 */
public final class MedicineFeeding {
    private static final TagKey<Item> MEDICINE = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "danyaoguan"));
    private static final TagKey<EntityType<?>> NPC = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "npcentity"));

    private MedicineFeeding() {
    }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand()) {
            return;
        }
        Entity target = event.getTarget();
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof DuedanItem) {
            if (target instanceof Mob mob) {
                stack.shrink(1);
                StatusEffects.add(mob, "duyao_19", 1200, 0);
            }
            return;
        }
        // 来源先给 NPC 设置 renwuzuopen，再以该标记独占任务分支；此处只读取分派条件。
        if (target.getType().is(NPC) || target.getPersistentData().getBoolean("renwuzuopen")) {
            return;
        }
        if (!stack.is(MEDICINE)) {
            return;
        }
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.getInt("danyaonum") < 1) {
            return;
        }
        if (!target.isAlive()) {
            tellTarget(target, "§4对方已死。");
            return;
        }
        if (!player.isShiftKeyDown()) {
            return;
        }
        if (target instanceof Mob mob && mob.getTarget() == player) {
            tellTarget(target, "§4对方不吃你给的丹药。");
            return;
        }
        if (stack.getCount() >= 2) {
            // 来源的合并副本未写回；本分支仅向目标玩家发放空出的同类容器。
            if (target instanceof Player targetPlayer) {
                ItemHandlerHelper.giveItemToPlayer(targetPlayer, new ItemStack(stack.getItem(), stack.getCount() - 1));
            }
            return;
        }

        LiandanMedicine.applyEffects(data, target.createCommandSourceStack().withSuppressedOutput().withPermission(4), "@s", false);
        data.putInt("danyaonum", data.getInt("danyaonum") - 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        if (data.getInt("danyaonum") <= 0) {
            player.setItemInHand(InteractionHand.MAIN_HAND, LiandanMedicine.emptyContainer(stack.getItem()));
            player.getInventory().setChanged();
        }
    }

    private static void tellTarget(Entity target, String message) {
        if (target instanceof Player player) {
            player.displayClientMessage(Component.literal(message), false);
        }
    }
}
