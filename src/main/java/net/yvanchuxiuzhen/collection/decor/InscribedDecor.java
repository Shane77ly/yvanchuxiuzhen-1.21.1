package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;

/** 文字装饰拥有编辑、染色、逐字布局与带字收回的全部写入。 */
public final class InscribedDecor {
    private InscribedDecor() { }

    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() != player.getUsedItemHand() || !(player.getMainHandItem().getItem() instanceof AxeItem)
                || !(player instanceof ServerPlayer server)) return;
        boolean plaque = event.getLevel().getBlockState(event.getPos()).is(BlockTags.create(id("plaqueblock")));
        boolean memorial = event.getLevel().getBlockState(event.getPos()).is(BlockTags.create(id("paiweiblock")));
        if (!plaque && !memorial) return;
        InscribedDecorMenu.Kind kind = plaque ? InscribedDecorMenu.Kind.PLAQUE : InscribedDecorMenu.Kind.MEMORIAL;
        BlockPos pos = event.getPos();
        server.openMenu(new SeamlessMenuProvider((container, inventory, ignored) -> new InscribedDecorMenu(kind, container, inventory, pos),
                Component.translatable("menu.yvanchuxiuzhen.decor." + kind.id())), pos);
    }

    public static void interact(InscribedDecorBlockEntity block, Player player) {
        ItemStack held = player.getMainHandItem();
        CompoundTag data = block.getPersistentData();
        if (held.is(ItemTags.create(id("colouritem")))) {
            if (held.is(Items.GLOW_INK_SAC)) data.putBoolean("faguang", true);
            else {
                Integer colour = colour(held.getItem());
                if (colour != null) data.putInt("rgbcolour", colour);
            }
            held.shrink(1);
            block.syncText();
        } else if (block.variant().memorial() && player.isShiftKeyDown()) {
            ItemStack item = new ItemStack(BuiltInRegistries.ITEM.get(id(block.variant().itemId())));
            if (!data.getString("plaquetxt").isEmpty()) {
                item.set(DataComponents.CUSTOM_NAME, Component.literal(data.getString("plaquetxt")));
                CompoundTag saved = new CompoundTag();
                saved.putString("plaquetxt", data.getString("plaquetxt"));
                saved.putString("fangxiang", data.getString("fangxiang"));
                saved.putInt("zishu", data.getInt("zishu"));
                for (int index = 0; index < data.getInt("zishu"); index++) {
                    saved.putString("zidi" + index, data.getString("zidi" + index));
                    saved.putDouble("ziweizhi" + index, data.getDouble("ziweizhi" + index));
                }
                item.set(DataComponents.CUSTOM_DATA, CustomData.of(saved));
            }
            ItemHandlerHelper.giveItemToPlayer(player, item);
            block.getLevel().setBlock(block.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static Integer colour(Item item) {
        int rgb;
        if (item == Items.INK_SAC || item == Items.BLACK_DYE) rgb = 0x000000;
        else if (item == Items.RED_DYE) rgb = 0xFF0000;
        else if (item == Items.GREEN_DYE) rgb = 0x00FF00;
        else if (item == Items.COCOA_BEANS || item == Items.BROWN_DYE) rgb = 0x8B4513;
        else if (item == Items.LAPIS_LAZULI || item == Items.BLUE_DYE) rgb = 0x0000FF;
        else if (item == Items.PURPLE_DYE) rgb = 0xA020F0;
        else if (item == Items.CYAN_DYE) rgb = 0x00FFFF;
        else if (item == Items.LIGHT_GRAY_DYE) rgb = 0xD3D3D3;
        else if (item == Items.GRAY_DYE) rgb = 0x808080;
        else if (item == Items.PINK_DYE) rgb = 0xFF69B4;
        else if (item == Items.LIME_DYE) rgb = 0xBFFF00;
        else if (item == Items.YELLOW_DYE || item == Items.LIGHT_BLUE_DYE) rgb = 0xFFFF00;
        else if (item == Items.MAGENTA_DYE) rgb = 0xFF00FF;
        else if (item == Items.ORANGE_DYE) rgb = 0xFF681F;
        else if (item == Items.BONE_MEAL || item == Items.WHITE_DYE) rgb = 0xFFFFFF;
        else if (item == Items.GOLD_NUGGET) rgb = 0xFFD700;
        else if (item == Items.HONEY_BOTTLE) rgb = 0xFAFAD2;
        else return null;
        return rgb | 0xFF000000;
    }

    public static String sourceText(String serialized) {
        String[] fields = serialized.split(",");
        int selected = 0;
        for (int index = 0; index < fields.length; index++) {
            if (fields[index].contains("plaquetxt:")) { selected = index; break; }
        }
        return fields[selected].replace("plaquetxt:", "");
    }

    public static void setMemorial(InscribedDecorBlockEntity block, String text) {
        CompoundTag data = block.getPersistentData();
        data.putString("plaquetxt", text);
        data.putString("fangxiang", direction(block));
        data.putDouble("zishu", text.length());
        double spacing = text.length() > 4 ? .22 : 1.2 / (text.length() + 1);
        for (int index = 0; index < text.length(); index++) {
            data.putString("zidi" + index, text.substring(index, index + 1));
            data.putDouble("ziweizhi" + index, (text.length() > 4 ? index : index + 1) * spacing);
        }
        block.syncText();
    }

    public static void setPlaque(InscribedDecorBlockEntity block, Player player, String text) {
        if (text.isEmpty()) return;
        if (text.length() > 10) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.decor.plaque_too_long"), false);
            return;
        }
        CompoundTag data = block.getPersistentData();
        double extra = data.getBoolean("plaqueaddfive") ? .5 : 0;
        double inset = 1.1 + (text.length() - 1) * .32;
        int count = (int) Math.ceil(text.length() / 1.7 + 2);
        data.putDouble("blocknum", count);
        data.putString("plaquetxt", text);
        data.putDouble("plaquetxtnum", text.length());
        String direction = direction(block);
        data.putString("fangxiang", direction);
        int yaw = switch (direction) { case "北" -> 0; case "东" -> -90; case "南" -> 180; default -> 90; };
        data.putDouble("plaquejiaodu", yaw);
        boolean alongX = direction.equals("北") || direction.equals("南");
        boolean increasing = direction.equals("北") || direction.equals("东");
        data.putDouble("wenziX", alongX ? increasing ? inset + extra : -(inset - 1 - extra) : .5);
        data.putDouble("wenziZ", alongX ? .5 : increasing ? inset + extra : -(inset - 1 - extra));
        adjustAngle(data);
        double fixed = direction.equals("北") || direction.equals("西") ? .3 : .7;
        for (int index = 0; index < count; index++) {
            double moving = increasing ? 1 - count / 2.0 + index + extra : count / 2.0 - index + extra;
            data.putDouble("block" + index + "Xpianyi", alongX ? moving : fixed);
            data.putDouble("block" + index + "Zpianyi", alongX ? fixed : moving);
        }
        block.syncText();
    }

    public static void cycleAngle(InscribedDecorBlockEntity block) {
        CompoundTag data = block.getPersistentData();
        double angle = data.getDouble("qingjiao");
        data.putDouble("qingjiao", angle == 0 ? 20 : angle == 20 ? 45 : 0);
        adjustAngle(data);
        block.syncText();
    }
    public static void toggleHalf(InscribedDecorBlockEntity block) {
        CompoundTag data = block.getPersistentData();
        data.putBoolean("plaqueaddfive", !data.getBoolean("plaqueaddfive"));
        block.syncText();
    }
    private static void adjustAngle(CompoundTag data) {
        double near = data.getDouble("qingjiao") == 20 ? .3 : data.getDouble("qingjiao") == 45 ? 0 : .5;
        switch (data.getString("fangxiang")) {
            case "北" -> data.putDouble("wenziZ", near);
            case "南" -> data.putDouble("wenziZ", 1 - near);
            case "东" -> data.putDouble("wenziX", 1 - near);
            case "西" -> data.putDouble("wenziX", near);
            default -> { }
        }
    }
    public static String direction(InscribedDecorBlockEntity block) { return direction(block.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)); }
    public static String direction(Direction direction) {
        return switch (direction) { case NORTH -> "北"; case SOUTH -> "南"; case WEST -> "西"; default -> "东"; };
    }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
}
