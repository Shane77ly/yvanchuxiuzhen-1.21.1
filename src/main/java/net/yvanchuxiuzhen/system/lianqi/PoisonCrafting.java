package net.yvanchuxiuzhen.system.lianqi;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.lianqi.content.PoisonCauldronBlockEntity;

/** 水锅转毒锅与材料直接淬毒；锅只保存毒种，装备累计记录由装备系统写入。 */
public final class PoisonCrafting {
    private static final Map<String, String> POISONS = Map.of(
            "cailiao_096", "spore", "cailiao_141", "sinomeninevine", "cailiao_142", "coldtoxin",
            "cailiao_143", "redrefining", "cailiao_144", "cautery", "cailiao_145", "snakevenom",
            "cailiao_146", "firepoison", "cailiao_040", "beepoison");

    private PoisonCrafting() {
    }

    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand()) return;
        Level level = event.getLevel();
        ItemStack material = player.getMainHandItem();
        if (level.getBlockState(event.getPos()).is(Blocks.WATER_CAULDRON)
                && material.is(ItemTags.create(id("duyaofumo")))) {
            level.setBlock(event.getPos(), BuiltInRegistries.BLOCK.get(id("cauldronduyao")).defaultBlockState(), 3);
            PoisonCauldronBlockEntity cauldron = (PoisonCauldronBlockEntity) level.getBlockEntity(event.getPos());
            String poison = poison(material);
            if (poison != null) {
                cauldron.getPersistentData().putString("linshikongjian", ModId.VALUE);
                cauldron.getPersistentData().putString("linshifumo", poison);
                cauldron.changed();
            }
            material.shrink(1);
        }
    }

    /** 事件只代替六件材料自身的 use 回调，仍由主手选毒并对副手应用。 */
    public static void rightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !directMaterial(event.getItemStack())) return;
        ItemStack material = player.getMainHandItem();
        ItemStack target = player.getOffhandItem();
        if (!directMaterial(material) || target.isEmpty()) return;
        String poison = poison(material);
        Holder<Enchantment> enchantment = enchantment(player.level(), id(poison));
        if (EnchantmentHelper.getItemEnchantmentLevel(enchantment, target) != 0) return;
        Equipment.recordPoisonEnchantment(target, poison);
        target.enchant(enchantment, 1);
        material.shrink(1);
    }

    public static void useCauldron(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return;
        CompoundTag data = ((PoisonCauldronBlockEntity) level.getBlockEntity(pos)).getPersistentData();
        String poison = data.getString("linshifumo");
        ItemStack target = player.getMainHandItem();
        if (poison.isEmpty() || target.isEmpty() || !POISONS.containsValue(poison)) return;
        Holder<Enchantment> enchantment = enchantment(level,
                ResourceLocation.fromNamespaceAndPath(data.getString("linshikongjian"), poison));
        boolean enchanted = EnchantmentHelper.getItemEnchantmentLevel(enchantment, target) != 0;
        if (poison.equals("beepoison") ? enchanted : !enchanted) return;
        // 来源追加同名附魔后，命中消费者仍读取首项；组件升级保留已有有效等级。
        target.enchant(enchantment, 1);
        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
    }

    private static boolean directMaterial(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getNamespace().equals(ModId.VALUE) && switch (key.getPath()) {
            case "cailiao_141", "cailiao_142", "cailiao_143", "cailiao_144", "cailiao_145", "cailiao_146" -> true;
            default -> false;
        };
    }

    private static String poison(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getNamespace().equals(ModId.VALUE) ? POISONS.get(key.getPath()) : null;
    }

    private static Holder<Enchantment> enchantment(Level level, ResourceLocation id) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, id));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
