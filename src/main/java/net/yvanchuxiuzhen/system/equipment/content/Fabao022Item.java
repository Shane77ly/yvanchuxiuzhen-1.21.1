package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 赶仙鞭的主手属性；来源没有自定义使用过程，物品外观仍由客户端资源单独验收。 */
public final class Fabao022Item extends ContentItem {
    public Fabao022Item(Properties properties, List<Component> tooltip) {
        super(properties.attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, -2,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build()), tooltip);
    }
}
