package net.yvanchuxiuzhen.system.cooking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockEntity;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 无菜谱墨料坯按材料槽均级分档，延迟交付重新查坐标并覆盖当时库存的首槽。 */
final class InkBlankCooking {
    private static final TagKey<Item> MATERIALS = tag("cailiao");
    private static final TagKey<Item> SHELL = tag("yingke");
    private static final TagKey<Item> BONE = tag("guge");
    private static final TagKey<Item> HIDE = tag("pige");
    private static final TagKey<Item> SCALE = tag("linpian");
    private static final TagKey<Item> TOOTH = tag("yachi");
    private static final TagKey<Item> CLAW = tag("lizhua");
    private static final String ASH = ModId.VALUE + ":yanhuiitem";
    private static final String CINNABAR = ModId.VALUE + ":fuluzhushaitem";

    private InkBlankCooking() {
    }

    static void begin(ServerPlayer player, CookingPotBlockEntity pot) {
        CompoundTag temporary = pot.getPersistentData().getCompound("kong");
        double levelSum = 0;
        double materialSlots = 0;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack input = pot.getItem(slot);
            String identity = BuiltInRegistries.ITEM.getKey(input.getItem()).toString();
            temporary.putBoolean(identity, true);
            temporary.putInt(identity + "number", temporary.getInt(identity + "number") + input.getCount());
            if (input.is(MATERIALS)) {
                MaterialInstance material = Materials.instance(input);
                temporary.putInt(identity + "level", material.level());
                temporary.putInt(identity + "pinjie", material.quality());
                int category = category(input);
                if (category != 0) temporary.putBoolean("motiaocailiao" + category, true);
                materialSlots++;
                levelSum += material.level();
            }
        }
        if (!temporary.getBoolean("motiaocailiao1") || !temporary.getBoolean("motiaocailiao2")
                || !temporary.getBoolean("motiaocailiao3") || !temporary.getBoolean(ASH)
                || temporary.getInt(ASH + "number") < 3 || !temporary.getBoolean(CINNABAR)
                || temporary.getInt(CINNABAR + "number") < 3) return;
        double average = levelSum / materialSlots;
        int grade = average >= 31 ? 4 : average >= 21 ? 3 : average >= 11 ? 2 : 1;
        temporary.putInt("motiaolevel", grade);
        ServerLevel level = player.serverLevel();
        BlockPos pos = pot.getBlockPos();
        CookingDelays.run(10, () -> {
            if (level.getBlockEntity(pos) != null
                    && level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null) instanceof IItemHandlerModifiable inventory) {
                inventory.setStackInSlot(0, CookingRecipes.stack(ModId.VALUE + ":motiao_0" + grade + "01"));
            }
        });
        temporary.putInt("item1num", 3);
        temporary.putInt("item2num", 3);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack input = pot.getItem(slot);
            if (input.is(MATERIALS)) {
                int category = category(input);
                if (category != 0 && temporary.getBoolean("motiaocailiao" + category)) {
                    pot.removeItem(slot, 1);
                    temporary.putBoolean("motiaocailiao" + category, false);
                }
            } else {
                String identity = BuiltInRegistries.ITEM.getKey(input.getItem()).toString();
                if (identity.equals(ASH)) consumePowder(pot, temporary, slot, "item1num");
                else if (identity.equals(CINNABAR)) consumePowder(pot, temporary, slot, "item2num");
            }
        }
        pot.getPersistentData().putBoolean("cooking", true);
        if (pot.iron()) pot.getPersistentData().putInt("cookanimation", 9);
        pot.changed();
        player.closeContainer();
    }

    private static void consumePowder(CookingPotBlockEntity pot, CompoundTag temporary, int slot, String key) {
        int removed = Math.min(pot.getItem(slot).getCount(), temporary.getInt(key));
        pot.removeItem(slot, removed);
        temporary.putInt(key, temporary.getInt(key) - removed);
    }

    private static int category(ItemStack stack) {
        if (stack.is(SHELL) || stack.is(BONE)) return 1;
        if (stack.is(HIDE) || stack.is(SCALE)) return 2;
        if (stack.is(TOOTH) || stack.is(CLAW)) return 3;
        return 0;
    }

    private static TagKey<Item> tag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
