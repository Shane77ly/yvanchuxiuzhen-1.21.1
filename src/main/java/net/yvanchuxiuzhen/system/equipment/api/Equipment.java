package net.yvanchuxiuzhen.system.equipment.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.equipment.EquipmentProducts;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 装备实例的公开构造与查询；调用方只负责给予、容器或奖励流程。 */
public final class Equipment {
    private Equipment() {
    }

    public static ItemStack createProduct(HolderLookup.Provider registries, List<String> arguments) {
        return EquipmentProducts.create(registries, arguments);
    }

    public static Optional<EquipmentProduct> product(ItemStack stack) {
        return Optional.ofNullable(stack.get(EquipmentData.PRODUCT.get()));
    }
}
