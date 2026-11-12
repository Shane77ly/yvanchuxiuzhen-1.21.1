package net.yvanchuxiuzhen.system.equipment.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

    /** 修理读取炼器实例的嵌套等级；宝箱成品从等价投影读取，不采用顶层等级或升级反推值。 */
    public static int refiningLevel(ItemStack stack) {
        CustomData refinement = stack.get(EquipmentData.REFINEMENT.get());
        if (refinement != null) return refinement.copyTag().getInt("item_level_num");
        EquipmentProduct product = stack.get(EquipmentData.PRODUCT.get());
        return product == null ? 0 : product.level();
    }

    /** 飞行法器修理资格按来源根字段的整数读取比较，不触发首次使用初始化。 */
    public static boolean flyingInstrumentNeedsRepair(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.getInt("health_Max") > data.getInt("health");
    }

    /** 修理按根字段的 double 加血并以上限截取，其他飞行实例字段保持原值。 */
    public static void repairFlyingInstrument(ItemStack stack, double amount) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            double health = data.getDouble("health") + amount;
            if (health > data.getDouble("health_Max")) health = data.getDouble("health_Max");
            data.putDouble("health", health);
        });
    }

    /** 记录来源淬毒附魔的动态原料条目；原生附魔由调用方的工艺流程写入。 */
    public static void recordPoisonEnchantment(ItemStack stack, String enchantmentPath) {
        EquipmentRefining.recordPoisonEnchantment(stack, enchantmentPath);
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
