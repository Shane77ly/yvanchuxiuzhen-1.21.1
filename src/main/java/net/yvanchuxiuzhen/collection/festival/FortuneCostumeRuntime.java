package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 财神服只读取本人穿戴的四槽；金币周期保存在胸甲实例，不要求穿齐其他部位。 */
final class FortuneCostumeRuntime {
    private FortuneCostumeRuntime() {
    }

    static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof FortuneCostumeItem armor)) continue;
            CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            String animation = data.getString("geckoAnim");
            if (!animation.isEmpty()) {
                CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("geckoAnim", ""));
                if (player.level().isClientSide()) armor.requestAnimation(animation);
            }
            if (slot != EquipmentSlot.CHEST || !(player.level() instanceof ServerLevel level)) continue;
            double tick = data.getDouble("fuqitick") + 1;
            if (tick >= 20) {
                level.sendParticles(Particles.type("fuqi_0201"), player.getX(), player.getY() + 1, player.getZ(), 2, 1, 1, 1, 0);
                if (Math.random() < 0.01) {
                    ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "money_01"))));
                }
                tick = 0;
            }
            double savedTick = tick;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putDouble("fuqitick", savedTick));
        }
    }
}
