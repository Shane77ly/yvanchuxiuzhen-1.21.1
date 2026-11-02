package net.yvanchuxiuzhen.platform.content;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 一个系统或内容族的内容表加载器：读取 {@code content/<单位>/blocks.json} 与 {@code items.json}，
 * 解码后登记到本单位的 DeferredRegister，并把带创造栏的物品交给 {@link ContentTabs}。
 */
public final class ContentTables {
    private final String unit;
    private final DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(ModId.VALUE);
    private final DeferredRegister.Items items = DeferredRegister.createItems(ModId.VALUE);
    private final DeferredRegister<ArmorMaterial> armorMaterials = DeferredRegister.create(Registries.ARMOR_MATERIAL, ModId.VALUE);

    public ContentTables(String unit) {
        this.unit = unit;
    }

    public void register(IEventBus modBus) {
        Map<String, DeferredBlock<Block>> registered = new HashMap<>();
        for (BlockRow row : ContentFiles.read("content/" + unit + "/blocks.json", BlockRow.CODEC)) {
            registered.put(row.id(), blocks.register(row.id(), () -> ContentBlocks.create(row)));
        }
        Map<String, Holder<ArmorMaterial>> materials = new HashMap<>();
        for (ArmorMaterialRow row : ContentFiles.read("content/" + unit + "/armor_materials.json", ArmorMaterialRow.CODEC)) {
            materials.put(row.id(), armorMaterials.register(row.id(), () -> armorMaterial(row)));
        }
        for (ItemRow row : ContentFiles.read("content/" + unit + "/items.json", ItemRow.CODEC)) {
            registerItem(row, registered, materials);
        }
        blocks.register(modBus);
        items.register(modBus);
        armorMaterials.register(modBus);
    }

    private void registerItem(ItemRow row, Map<String, DeferredBlock<Block>> registered, Map<String, Holder<ArmorMaterial>> materials) {
        List<Component> tooltip = tooltip(row);
        DeferredItem<? extends Item> holder = switch (row.factory()) {
            case "item" -> items.register(row.id(), () -> new ContentItem(properties(row), tooltip));
            case "block_item" -> {
                DeferredBlock<Block> block = registered.get(row.block().orElseThrow());
                if (block == null) {
                    throw new IllegalStateException("方块物品 " + row.id() + " 的方块 " + row.block().get() + " 不在单位 " + unit + " 的方块表中");
                }
                yield row.doubleHigh().orElse(false)
                        ? items.register(row.id(), () -> new ContentDoubleBlockItem(block.get(), properties(row), tooltip))
                        : items.register(row.id(), () -> new ContentBlockItem(block.get(), properties(row), tooltip));
            }
            case "tool" -> items.register(row.id(), () -> tool(row, tooltip));
            case "armor" -> {
                ItemRow.Armor armor = row.armor().orElseThrow();
                Holder<ArmorMaterial> material = materials.get(armor.material());
                if (material == null) {
                    throw new IllegalStateException("盔甲 " + row.id() + " 的材质 " + armor.material() + " 不在单位 " + unit + " 的材质表中");
                }
                ArmorItem.Type type = ArmorItem.Type.valueOf(armor.slot());
                yield items.register(row.id(), () -> new ContentArmorItem(material, type,
                        properties(row).durability(type.getDurability(row.durabilityMultiplier().orElseThrow())), tooltip));
            }
            default -> throw new IllegalStateException("未知物品工厂 " + row.factory() + "：" + row.id());
        };
        row.tab().ifPresent(tab -> ContentTabs.add(tab, row.order(), holder));
    }

    /** 来源的 SwordItem(tier, 攻击, 攻速) 等价于 1.21.1 的属性组件；耐久为 0 的工具不可损坏。 */
    private static Item tool(ItemRow row, List<Component> tooltip) {
        ItemRow.Tool tool = row.tool().orElseThrow();
        LegacyTier tier = LegacyTier.of(tool.tier());
        Item.Properties properties = properties(row);
        if (tier.uses() == 0) {
            properties.component(DataComponents.UNBREAKABLE, new Unbreakable(false));
        }
        if (tool.damage().isPresent()) {
            int damage = tool.damage().get();
            float speed = tool.attackSpeed().orElseThrow();
            return switch (tool.kind()) {
                case "sword" -> new ContentSword(tier, properties.attributes(SwordItem.createAttributes(tier, damage, speed)), tooltip);
                case "axe" -> new ContentAxe(tier, properties.attributes(AxeItem.createAttributes(tier, damage, speed)), tooltip);
                case "pickaxe" -> new ContentPickaxe(tier, properties.attributes(PickaxeItem.createAttributes(tier, damage, speed)), tooltip);
                case "shovel" -> new ContentShovel(tier, properties.attributes(ShovelItem.createAttributes(tier, damage, speed)), tooltip);
                default -> throw new IllegalStateException("未知工具类型 " + tool.kind() + "：" + row.id());
            };
        }
        if (!tool.kind().equals("tiered")) {
            throw new IllegalStateException("工具 " + row.id() + " 缺少攻击参数");
        }
        return new ContentTieredItem(tier, properties, tooltip);
    }

    private static ArmorMaterial armorMaterial(ArmorMaterialRow row) {
        List<Integer> defense = row.defense();
        Map<ArmorItem.Type, Integer> byType = new EnumMap<>(ArmorItem.Type.class);
        byType.put(ArmorItem.Type.BOOTS, defense.get(0));
        byType.put(ArmorItem.Type.LEGGINGS, defense.get(1));
        byType.put(ArmorItem.Type.CHESTPLATE, defense.get(2));
        byType.put(ArmorItem.Type.HELMET, defense.get(3));
        return new ArmorMaterial(byType, row.enchantability(), BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY),
                () -> LegacyTier.ingredient(row.repair().orElse(List.of())),
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, row.texture()))),
                row.toughness(), row.knockback());
    }

    private static List<Component> tooltip(ItemRow row) {
        int count = row.tooltip().map(List::size).orElse(0);
        return IntStream.rangeClosed(1, count)
                .<Component>mapToObj(number -> Component.translatable("tooltip." + ModId.VALUE + ".item." + row.id() + "." + number))
                .toList();
    }

    private static Item.Properties properties(ItemRow row) {
        Item.Properties properties = new Item.Properties();
        row.stack().ifPresent(properties::stacksTo);
        row.rarity().ifPresent(rarity -> properties.rarity(Rarity.valueOf(rarity)));
        if (row.fireResistant().orElse(false)) {
            properties.fireResistant();
        }
        row.durability().ifPresent(properties::durability);
        row.food().ifPresent(food -> properties.food(new FoodProperties(food.nutrition(),
                FoodConstants.saturationByModifier(food.nutrition(), food.saturationMod()), food.alwaysEat().orElse(false),
                food.useTicks().map(ticks -> ticks / 20.0F).orElse(1.6F), Optional.empty(), List.of())));
        return properties;
    }
}
