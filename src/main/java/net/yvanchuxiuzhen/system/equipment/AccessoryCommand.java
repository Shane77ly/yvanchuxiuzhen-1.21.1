package net.yvanchuxiuzhen.system.equipment;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties.Value;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 来源 /shipin：无权限门槛，根字段依三组显式参数顺序写入，原生附魔仍由物品自身保存。 */
final class AccessoryCommand {
    private AccessoryCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("shipin")
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(context -> run(context.getSource(),
                        Arrays.asList(StringArgumentType.getString(context, "arguments").trim().split("\\s+")))))
                .executes(context -> run(context.getSource(), List.of())));
    }

    private static int run(CommandSourceStack source, List<String> arguments) {
        ResourceLocation id = ResourceLocation.tryParse(text(arguments, 0).toLowerCase(Locale.ENGLISH));
        if (id == null) return 0;
        if (id.getNamespace().equals("yvanchuxitong")) id = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id.getPath());
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id));
        if (stack.isEmpty()) return 0;
        if (!text(arguments, 1).equals("无")) stack.set(DataComponents.CUSTOM_NAME, Component.literal(text(arguments, 1)));
        Map<String, Value> fields = new LinkedHashMap<>();
        if (!text(arguments, 2).equals("无")) fields.put("fabaoshuoming", Value.text(text(arguments, 2)));
        fields.put("fabaotype", Value.text(text(arguments, 3)));
        fields.put("item_level", Value.text(text(arguments, 4)));
        fields.put("item_level_num", Value.integer((int) number(arguments, 5)));
        for (int start = 6; start <= 12 && !text(arguments, start).isEmpty(); start += 3) {
            String key = text(arguments, start);
            String kind = text(arguments, start + 1);
            switch (kind) {
                case "str", "char" -> fields.put(key, Value.text(text(arguments, start + 2)));
                case "int" -> fields.put(key, Value.integer((int) number(arguments, start + 2)));
                case "double" -> fields.put(key, Value.decimal(number(arguments, start + 2)));
                default -> {
                    String namespace = key.equals("yvanchuxitong") ? ModId.VALUE : key;
                    ResourceKey<Enchantment> enchantment = ResourceKey.create(Registries.ENCHANTMENT,
                            ResourceLocation.fromNamespaceAndPath(namespace, kind));
                    stack.enchant(source.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment), (int) number(arguments, start + 2));
                }
            }
        }
        stack.set(EquipmentData.PROPERTIES.get(), new EquipmentProperties(fields));
        ItemEntity drop = new ItemEntity(source.getLevel(), source.getPosition().x + .5, source.getPosition().y + .8, source.getPosition().z + .5, stack);
        drop.setPickUpDelay(10);
        source.getLevel().addFreshEntity(drop);
        return 0;
    }

    private static String text(List<String> arguments, int index) {
        return index < arguments.size() ? arguments.get(index) : "";
    }

    /** MCreator 数值转换对缺失或非法数值取 0，只在命令输入边界使用。 */
    private static double number(List<String> arguments, int index) {
        try {
            return Double.parseDouble(text(arguments, index));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
