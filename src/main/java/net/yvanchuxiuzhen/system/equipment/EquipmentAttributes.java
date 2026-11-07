package net.yvanchuxiuzhen.system.equipment;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.ArmorStatEvent;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.skill.api.WeaponStatEvent;

/** 护甲、主手炼器值及已保存饰品提交加成层；Q17 的生命、物攻只计一次，其余原生修饰随物品生效。 */
public final class EquipmentAttributes {
    private static final Map<ServerPlayer, Snapshot> PREVIOUS = new WeakHashMap<>();

    private record Snapshot(Contribution armor, Contribution weapon, Contribution accessories) {
    }

    private EquipmentAttributes() {
    }

    static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Map<Stat, Double> values = new EnumMap<>(Stat.class);
        for (ItemStack stack : player.getArmorSlots()) {
            EquipmentStats stats = stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY);
            values.merge(Stat.HEALTH_MAX, (double) stats.health(), Double::sum);
            values.merge(Stat.POWER_MAX, (double) stats.power(), Double::sum);
            values.merge(Stat.SOUL_MAX, (double) stats.soul(), Double::sum);
            values.merge(Stat.ATTACK_DEF, (double) stats.physicalDefense(), Double::sum);
            values.merge(Stat.MANA_DEF, (double) stats.spellDefense(), Double::sum);
            values.merge(Stat.SOUL_DEF, (double) stats.soulDefense(), Double::sum);
            values.merge(Stat.HEALTH_REC, (double) Enchants.level(player, stack, Enchants.key("dawn")), Double::sum);
            values.merge(Stat.POWER_REC, (double) Enchants.level(player, stack, Enchants.key("powerup")), Double::sum);
            values.merge(Stat.ATTACK_DEF, (double) Enchants.level(player, stack, Enchants.key("attdef")), Double::sum);
            values.merge(Stat.MANA_DEF, (double) Enchants.level(player, stack, Enchants.key("manadef")), Double::sum);
            values.merge(Stat.COOLDOWN_REDUCTION, Enchants.level(player, stack, Enchants.key("colddown")) * .01, Double::sum);
        }
        Contribution armor = new Contribution(Layer.BONUS, values);
        ItemStack held = player.getMainHandItem();
        EquipmentStats weaponStats = held.is(tag("wuqi")) || held.is(tag("fabao"))
                ? held.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY) : EquipmentStats.EMPTY;
        Contribution weapon = new Contribution(Layer.BONUS, Map.of(Stat.ATTACK, (double) weaponStats.physicalAttack(),
                Stat.MANA_ATT, (double) weaponStats.spellAttack(), Stat.SOUL_ATT, (double) weaponStats.soulAttack()));
        Map<Stat, Double> accessoryValues = new EnumMap<>(Stat.class);
        for (ItemStack stack : Equipment.accessories(player).items()) {
            EquipmentProperties properties = Equipment.properties(stack);
            accessoryValues.merge(Stat.HEALTH_MAX, properties.number("health_Max"), Double::sum);
            accessoryValues.merge(Stat.HEALTH_REC, properties.number("health_Rec"), Double::sum);
            accessoryValues.merge(Stat.POWER_MAX, properties.number("power_Max"), Double::sum);
            accessoryValues.merge(Stat.POWER_REC, properties.number("power_Rec"), Double::sum);
            accessoryValues.merge(Stat.SOUL_MAX, properties.number("soul_Max"), Double::sum);
            accessoryValues.merge(Stat.SOUL_REC, properties.number("soul_Rec"), Double::sum);
            accessoryValues.merge(Stat.ATTACK_DEF, properties.number("attack_Def"), Double::sum);
            accessoryValues.merge(Stat.MANA_DEF, properties.number("mana_Def"), Double::sum);
            accessoryValues.merge(Stat.SOUL_DEF, properties.number("soul_Def"), Double::sum);
            accessoryValues.merge(Stat.COOLDOWN_REDUCTION, Enchants.level(player, stack, Enchants.key("colddown")) * .01, Double::sum);
        }
        Contribution accessories = new Contribution(Layer.BONUS, accessoryValues);
        Snapshot previous = PREVIOUS.put(player, new Snapshot(armor, weapon, accessories));
        if (previous == null || !armor.equals(previous.armor())) PlayerAttributes.setContribution(player, "equipment:armor", armor);
        if (previous == null || !weapon.equals(previous.weapon())) PlayerAttributes.setContribution(player, "equipment:weapon", weapon);
        if (previous == null || !accessories.equals(previous.accessories())) PlayerAttributes.setContribution(player, "equipment:accessories", accessories);
    }

    static void armorStat(ArmorStatEvent event) {
        for (ItemStack stack : event.entity().getArmorSlots()) {
            EquipmentStats stats = stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY);
            event.add(switch (event.key()) {
                case "attack_Def" -> stats.physicalDefense();
                case "soul_Def" -> stats.soulDefense();
                // 来源伤害链的法术护甲项实际读取 mana_Att，不能替换成 mana_Def。
                case "mana_Att" -> stats.spellAttack();
                default -> throw new IllegalArgumentException("护甲查询项：" + event.key());
            });
        }
    }

    static void weaponStat(WeaponStatEvent event) {
        if (event.key().equals("lianqizong.item_level_num")) {
            EquipmentProduct product = event.weapon().get(EquipmentData.PRODUCT.get());
            if (product != null) event.add(product.level());
        } else if (event.key().equals("mana_Att")) {
            event.add(Equipment.properties(event.weapon()).number("mana_Att"));
        }
    }

    static void modifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        EquipmentStats stats = stack.get(EquipmentData.STATS.get());
        if (stats == null) return;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            String tag = switch (slot) {
                case HEAD -> "toukui";
                case CHEST -> "xiongjia";
                case LEGS -> "kuzi";
                case FEET -> "zhanxve";
                default -> throw new IllegalStateException("护甲部位：" + slot);
            };
            if (!stack.is(tag("yaohuaarmor")) || !stack.is(tag(tag))) continue;
            EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
            String prefix = "equipment/" + slot.getName() + "/";
            event.addModifier(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, prefix + "knockback"),
                    stats.knockbackResistance(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
            event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, prefix + "attack_speed"),
                    stats.attackSpeed(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
            event.addModifier(Attributes.MOVEMENT_SPEED, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, prefix + "move_speed"),
                    stats.moveSpeed(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
        }
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
