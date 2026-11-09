package net.yvanchuxiuzhen.system.equipment.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.equipment.Accessories;
import net.yvanchuxiuzhen.system.equipment.EquipmentProducts;
import net.yvanchuxiuzhen.system.equipment.EquipmentRefining;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 装备实例的公开构造与查询；调用方只负责给予、容器或奖励流程。 */
public final class Equipment {
    private Equipment() {
    }

    public static ItemStack createProduct(HolderLookup.Provider registries, List<String> arguments) {
        return EquipmentProducts.create(registries, arguments);
    }

    /** 由自然炼器累计量重建单件器物；旧实例仅用于复制来源的四个本命字段。 */
    public static ItemStack createRefinedProduct(HolderLookup.Provider registries, CompoundTag recipe,
                                                CompoundTag raw, ItemStack previous) {
        return EquipmentRefining.create(registries, recipe, raw, previous);
    }

    /** 返回供下一炉升级使用的原始累计量副本，不修改输入物品。 */
    public static CompoundTag refiningMaterials(ItemStack stack) {
        return EquipmentRefining.materials(stack);
    }

    public static Optional<EquipmentProduct> product(ItemStack stack) {
        return Optional.ofNullable(stack.get(EquipmentData.PRODUCT.get()));
    }

    public static EquipmentProperties properties(ItemStack stack) {
        return stack.getOrDefault(EquipmentData.PROPERTIES.get(), EquipmentProperties.EMPTY);
    }

    public static AccessoryInventory accessories(Player player) {
        return player.getData(EquipmentData.ACCESSORIES).snapshot();
    }

    public static void openAccessories(ServerPlayer player) {
        Accessories.open(player);
    }

    /** 在指定的最终打开坐标创建饰品页，保留跨页导航沿旧菜单坐标向上累加的行为。 */
    public static void openAccessories(ServerPlayer player, BlockPos pos) {
        Accessories.open(player, pos);
    }
}
