package net.yvanchuxiuzhen.system.equipment.content;

import java.util.Map;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 飞行法器物品：首次右键写入来源字段，随后召唤对应的飞行法器实体并清空手持槽。 */
public final class FlyingInstrumentItem extends ContentItem {
    public record Definition(String itemId, String entityId, String name, double health, double power, double speed) {
    }

    private static final Map<String, Definition> DEFINITIONS = Map.of(
            "flyinginstrument_0101", new Definition("flyinginstrument_0101", "flyinginstrument_01", "飞叶飞行法器", 26, 280, .3),
            "flyinginstrument_0201", new Definition("flyinginstrument_0201", "flyinginstrument_02", "银盘飞行法器", 30, 260, .25),
            "flyinginstrument_0301", new Definition("flyinginstrument_0301", "flyinginstrument_03", "花瓣飞行法器", 20, 280, .25),
            "flyinginstrument_0401", new Definition("flyinginstrument_0401", "flyinginstrument_04", "飞舟", 30, 240, .35),
            "flyinginstrument_0501", new Definition("flyinginstrument_0501", "flyinginstrument_05", "千纸鹤", 20, 260, .3),
            "flyinginstrument_0601", new Definition("flyinginstrument_0601", "flyinginstrument_06", "木鱼飞行法器", 40, 300, .2),
            "flyinginstrument_0701", new Definition("flyinginstrument_0701", "flyinginstrument_07", "飞毯", 30, 280, .2),
            "flyinginstrument_0801", new Definition("flyinginstrument_0801", "flyinginstrument_08", "御风舟", 80, 400, .5));

    private final Definition definition;

    public FlyingInstrumentItem(Definition definition, Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
        this.definition = definition;
    }

    public static Definition definition(String itemId) {
        Definition definition = DEFINITIONS.get(itemId);
        if (definition == null) throw new IllegalArgumentException("未知飞行法器物品：" + itemId);
        return definition;
    }

    public static Definition definitionByEntity(String entityId) {
        return DEFINITIONS.values().stream().filter(definition -> definition.entityId().equals(entityId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知飞行法器实体：" + entityId));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            initialize(stack);
            FlyingInstrumentEntity.spawn(server, serverPlayer, definition, stack);
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    private void initialize(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putString("flyitemID", ModId.VALUE + ":" + definition.itemId());
        if (data.getString("flyname").isEmpty()) {
            data.putString("flyname", definition.name());
            data.putString("flyentityID", ModId.VALUE + ":" + definition.entityId());
            data.putDouble("health_Max", definition.health());
            data.putDouble("power_Max", definition.power());
            data.putDouble("speed_num", definition.speed());
            data.putDouble("health", definition.health());
            data.putDouble("power", definition.power());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }
}
