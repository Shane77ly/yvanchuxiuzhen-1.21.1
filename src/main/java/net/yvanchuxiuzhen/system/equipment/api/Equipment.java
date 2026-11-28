package net.yvanchuxiuzhen.system.equipment.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.equipment.Accessories;
import net.yvanchuxiuzhen.system.equipment.EquipmentAttributes;
import net.yvanchuxiuzhen.system.equipment.EquipmentProducts;
import net.yvanchuxiuzhen.system.equipment.EquipmentRefining;
import net.yvanchuxiuzhen.system.equipment.data.AccessoryData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 装备实例的公开构造与查询；调用方只负责给予、容器或奖励流程。 */
public final class Equipment {
    private Equipment() {
    }

    public static ItemStack createProduct(HolderLookup.Provider registries, List<String> arguments) {
        return EquipmentProducts.create(registries, arguments);
    }

    /** 由自然炼器累计量重建单件器物；旧实例仅用于复制来源的四个本命字段。 */
    public static ItemStack createRefinedProduct(HolderLookup.Provider registries, EquipmentRefinement recipe,
                                                EquipmentRefinement raw, ItemStack previous) {
        return EquipmentRefining.create(registries, recipe, raw, previous);
    }

    /** 返回供下一炉升级使用的原始累计量副本，不修改输入物品。 */
    public static EquipmentRefinement refiningMaterials(ItemStack stack) {
        return EquipmentRefining.materials(stack);
    }

    /** 修理读取炼器实例的嵌套等级；宝箱成品从等价投影读取，不采用顶层等级或升级反推值。 */
    public static int refiningLevel(ItemStack stack) {
        EquipmentRefinement refinement = stack.get(EquipmentData.REFINEMENT.get());
        if (refinement != null) return refinement.integer("item_level_num");
        EquipmentProduct product = stack.get(EquipmentData.PRODUCT.get());
        return product == null ? 0 : product.level();
    }

    public static int weight(ItemStack stack) {
        EquipmentProduct product = stack.get(EquipmentData.PRODUCT.get());
        if (product != null) return product.weight();
        EquipmentRefinement refinement = stack.get(EquipmentData.REFINEMENT.get());
        return refinement == null ? 0 : refinement.integer("weightnum");
    }

    public static FlyingInstrumentData flyingInstrument(ItemStack stack) {
        return stack.getOrDefault(EquipmentData.FLYING_INSTRUMENT.get(), FlyingInstrumentData.EMPTY);
    }

    /** 飞行法器修理资格按整数比较，不触发首次使用初始化。 */
    public static boolean flyingInstrumentNeedsRepair(ItemStack stack) {
        FlyingInstrumentData data = flyingInstrument(stack);
        return (int) data.healthMax() > (int) data.health();
    }

    /** 修理按 double 加血并以上限截取，其他飞行实例字段保持原值。 */
    public static void repairFlyingInstrument(ItemStack stack, double amount) {
        FlyingInstrumentData data = flyingInstrument(stack);
        double health = Math.min(data.health() + amount, data.healthMax());
        stack.set(EquipmentData.FLYING_INSTRUMENT.get(), data.withHealth(health));
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

    public static String ownerId(ItemStack stack) {
        return properties(stack).text("belongToMasterUUID");
    }

    public static void bindOwner(ItemStack stack, String uuid) {
        updateRootField(stack, "belongToMasterUUID", StringTag.valueOf(uuid));
    }

    public static BloodEssenceData bloodEssence(ItemStack stack) {
        return stack.getOrDefault(EquipmentData.BLOOD_ESSENCE.get(), BloodEssenceData.EMPTY);
    }

    public static String consumeGeckoAnimation(ItemStack stack) {
        String animation = stack.getOrDefault(EquipmentData.GECKO_ANIM.get(), "");
        if (!animation.isEmpty()) stack.remove(EquipmentData.GECKO_ANIM.get());
        return animation;
    }

    /** 通用编辑器修改来源根字段时，由装备所有者同步根字段投影与两项本命经验组件。 */
    public static void updateRootField(ItemStack stack, String key, Tag value) {
        CompoundTag root = new CompoundTag();
        root.put(key, value);
        Map<String, EquipmentProperties.Value> fields = new LinkedHashMap<>(properties(stack).fields());
        EquipmentProperties.Value projected = value.getId() == Tag.TAG_STRING
                ? EquipmentProperties.Value.text(root.getString(key))
                : EquipmentProperties.Value.decimal(root.getDouble(key));
        fields.put(key, projected);
        stack.set(EquipmentData.PROPERTIES.get(), new EquipmentProperties(fields));
        if (key.equals("benmingexp") || key.equals("benmingexpmax")) {
            BenmingData progress = stack.getOrDefault(EquipmentData.BENMING.get(), BenmingData.EMPTY);
            stack.set(EquipmentData.BENMING.get(), key.equals("benmingexp")
                    ? new BenmingData(progress.level(), root.getDouble(key), progress.experienceMax())
                    : new BenmingData(progress.level(), progress.experience(), root.getDouble(key)));
        }
    }

    public static AccessoryInventory accessories(Player player) {
        return player.getData(EquipmentData.ACCESSORIES).snapshot();
    }

    /** 轮回清四个饰品槽，不触碰投掷法宝临时数据或其他装备系统持有物。 */
    public static void clearReincarnationAccessories(ServerPlayer player) {
        player.setData(EquipmentData.ACCESSORIES, new AccessoryData());
    }

    /** 库存与饰品归零后立即撤回旧器物贡献，不发完整重算粒子。 */
    public static void refreshContributions(ServerPlayer player) {
        EquipmentAttributes.submit(player);
    }

    public static void openAccessories(ServerPlayer player) {
        Accessories.open(player);
    }

    /** 在指定的最终打开坐标创建饰品页，保留跨页导航沿旧菜单坐标向上累加的行为。 */
    public static void openAccessories(ServerPlayer player, BlockPos pos) {
        Accessories.open(player, pos);
    }
}
