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
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 四件护甲的器物数值提交加成层；Q17 的生命只计一次，攻速、移速及抗性保留原版乘基础值修饰。 */
public final class EquipmentAttributes {
    private static final Map<ServerPlayer, Contribution> PREVIOUS = new WeakHashMap<>();

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
        }
        Contribution contribution = new Contribution(Layer.BONUS, values);
        if (!contribution.equals(PREVIOUS.put(player, contribution))) {
            PlayerAttributes.setContribution(player, "equipment:armor", contribution);
        }
    }

    static void armorStat(ArmorStatEvent event) {
        for (ItemStack stack : event.entity().getArmorSlots()) {
            EquipmentStats stats = stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY);
            event.add(switch (event.key()) {
                case "attack_Def" -> stats.physicalDefense();
                case "soul_Def" -> stats.soulDefense();
                // 来源伤害链在法术护甲项读取 mana_Att；本批妖化防具没有写入该项。
                case "mana_Att" -> 0;
                default -> throw new IllegalArgumentException("护甲查询项：" + event.key());
            });
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
