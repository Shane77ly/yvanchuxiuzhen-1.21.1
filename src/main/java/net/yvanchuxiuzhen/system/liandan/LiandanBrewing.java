package net.yvanchuxiuzhen.system.liandan;

import java.text.DecimalFormat;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockEntity;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;

/** 丹炉开炉、丹方装料与经验交付；服务端按槽位顺序累积药效，炉次存入装置。 */
public final class LiandanBrewing {
    private LiandanBrewing() {
    }

    /** 开炉只排除正在炼制的炉次，材料槽逐格消耗一件，第一颗妖丹之后的槽位保留。 */
    public static void begin(AlchemyBlockEntity furnace) {
        CompoundTag state = furnace.getPersistentData();
        if (state.getBoolean("liandanBegin")) return;

        ItemStack result = new ItemStack(Items.GUNPOWDER);
        CompoundTag medicine = new CompoundTag();
        Item container = furnace.getItem(0).getItem();
        String outputId = BuiltInRegistries.ITEM.getKey(container).toString();
        if (container == item("itemdanyaohulu_04")) {
            outputId = ModId.VALUE + ":itemdanyaohulu_0401";
        } else if (container == item("itemdanyaohulu_05")) {
            outputId = ModId.VALUE + ":itemdanyaohulu_0501";
        }
        medicine.putString("danyaoid", outputId);
        if (!furnace.getItem(10).isEmpty()) {
            CompoundTag recipe = data(furnace.getItem(10));
            medicine.putString("danyaoname", recipe.getString("danyaoname"));
            medicine.putString("danyaoshuoming", recipe.getString("danyaoshuoming"));
        } else {
            medicine.putString("danyaoname", furnace.getItem(1).getDisplayName().getString().substring(1, 3) + "丹");
        }

        CompoundTag herbs = state.getCompound("zong");
        boolean monsterCoreSeen = false;
        int baseDuration = 0;
        for (int slot = 1; slot <= 8; slot++) {
            ItemStack ingredient = furnace.getItem(slot);
            if (ingredient.isEmpty() || monsterCoreSeen) continue;
            if (ingredient.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaodan")))) {
                monsterCoreSeen = true;
            }
            String herb = BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString();
            for (int effect = 0; effect < herbs.getInt(herb + "number"); effect++) {
                String prefix = herb + "effect" + effect;
                String name = herbs.getString(prefix + "name");
                int index = 0;
                boolean existing = false;
                for (; index < medicine.getInt("shuliang") + 1; index++) {
                    if (medicine.getString("danyaoxiaoguo" + index).equals(name)) {
                        combine(medicine, "danyaolevel" + index, herbs.getString(prefix + "symbollevel"),
                                herbs.getInt(prefix + "symbollevelnum"), false);
                        combine(medicine, "danyaotick" + index, herbs.getString(prefix + "symboltime"),
                                herbs.getInt(prefix + "symboltimenum"), true);
                        existing = true;
                        break;
                    }
                }
                if (!existing) {
                    index--;
                    medicine.putString("danyaoxiaoguo" + index, name);
                    initialize(medicine, "danyaolevel" + index, herbs.getString(prefix + "symbollevel"),
                            herbs.getInt(prefix + "symbollevelnum"));
                    initialize(medicine, "danyaotick" + index, herbs.getString(prefix + "symboltime"),
                            herbs.getInt(prefix + "symboltimenum"));
                    medicine.putInt("shuliang", medicine.getInt("shuliang") + 1);
                }
                baseDuration += 4;
            }
            ItemStack remainder = ingredient.copy();
            remainder.shrink(1);
            furnace.setItem(slot, remainder);
        }
        medicine.putInt("danyaonum", 1 + Mth.nextInt(RandomSource.create(), 0, state.getInt("alchemyDannum")));
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(medicine));
        result.set(DataComponents.CUSTOM_NAME, Component.literal("丹药残渣"));
        furnace.setItem(11, result);
        ItemStack containerRemainder = furnace.getItem(0).copy();
        containerRemainder.shrink(1);
        furnace.setItem(0, containerRemainder);
        state.putInt("lianqiTickMax", (int) (baseDuration * (1 - (state.getInt("alchemyFire") + state.getInt("Alchemy_LEVEL")) * 0.05)));
        state.putBoolean("liandanBegin", true);

        double maximumEffect = 0;
        for (int index = 0; index < medicine.getInt("shuliang"); index++) {
            if (medicine.getInt("danyaolevel" + index) > maximumEffect) {
                maximumEffect = medicine.getInt("danyaolevel" + index);
            }
        }
        maximumEffect /= 100;
        state.putInt("jingyanhuoqv", (int) (maximumEffect * 4));
        int supportedLevel = state.getInt("Alchemy_LEVEL") + state.getInt("alchemyLevel");
        if (maximumEffect > supportedLevel) {
            state.putBoolean("zhalu", true);
            state.putDouble("zhalugailv", supportedLevel / Math.ceil(maximumEffect));
        } else {
            state.putBoolean("zhalu", false);
        }
        furnace.changed();
    }

    /** 丹方自动装料先退回八槽全部物品，再按玩家物品能力中的顺序各取一件匹配物品。 */
    public static void loadRecipe(AlchemyBlockEntity furnace, ServerPlayer player) {
        CompoundTag recipe = data(furnace.getItem(10));
        if (recipe.getString("danyaoname").isEmpty()) {
            player.displayClientMessage(Component.literal("§4请在左边的物品栏放入丹方。"), false);
            return;
        }
        for (int slot = 1; slot <= 8; slot++) {
            ItemStack stack = furnace.getItem(slot);
            if (!stack.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, stack.copy());
                furnace.setItem(slot, ItemStack.EMPTY);
            }
        }
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        for (int slot = 1; slot <= 8; slot++) {
            String id = recipe.getString("cailiao0" + slot);
            if (id.isEmpty()) continue;
            Item ingredient = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id.toLowerCase(Locale.ENGLISH)));
            if (inventory != null) {
                for (int index = 0; index < inventory.getSlots(); index++) {
                    ItemStack stack = inventory.getStackInSlot(index);
                    if (stack.is(ingredient)) {
                        furnace.setItem(slot, stack.copyWithCount(1));
                        stack.shrink(1);
                        break;
                    }
                }
            }
        }
        furnace.changed();
    }

    /** 来源保存过程只修改槽位副本；保留其条件与反馈，成功分支不写回丹方。 */
    public static void saveRecipe(AlchemyBlockEntity furnace, ServerPlayer player) {
        ItemStack recipe = furnace.getItem(10);
        if (recipe.isEmpty()) {
            player.displayClientMessage(Component.literal("§4请放置一个承载丹方的媒介，比如线装书。"), false);
        } else if (data(recipe).getString("danyaoname").isEmpty()) {
            player.displayClientMessage(Component.literal("§4此物已承载了一份丹方，无法覆盖。"), false);
        } else {
            int count = 0;
            for (int slot = 1; slot <= 8; slot++) count += furnace.getItem(slot).getCount();
            if (count < 1) player.displayClientMessage(Component.literal("§4至少需要一株药草。"), false);
        }
    }

    /** 结果槽变化时领取炉内经验；炸炉先除四，再按来源 DecimalFormat 的整数文本交付。 */
    public static void claimExperience(AlchemyBlockEntity furnace, ServerPlayer player) {
        CompoundTag state = furnace.getPersistentData();
        if (state.getBoolean("liandanBegin") || state.getInt("jingyanhuoqv") < 1) return;
        double experience = state.getInt("jingyanhuoqv");
        if (state.getBoolean("zhenzhale")) {
            experience /= 4;
            state.putBoolean("zhenzhale", false);
        }
        String amount = new DecimalFormat("0").format(experience);
        Professions.addExperience(player, Profession.ALCHEMY, Double.parseDouble(amount));
        player.displayClientMessage(Component.literal("§6炼丹经验增减量为" + amount), false);
        state.putInt("jingyanhuoqv", 0);
        furnace.changed();
    }

    public static void help(ServerPlayer player, int button) {
        String[] lines = switch (button) {
            case 3 -> new String[] {
                    "购买、宝箱或者副本都有可能获得丹方，把丹方放入左上角的物品槽位中，点击旁边的按钮，就可以显示需要的材料了。",
                    "如果您身上有丹方需要的药材，那么炼丹炉此时就会自动把药材放进炼丹炉对应位置。",
                    "点击中间的炼制按钮开始炼制丹药。",
                    "在丹炉下方点燃异火来为炼丹炉充能：手持异火在潜行状态下右键方块顶部即可放置异火。" };
            case 4 -> new String[] {
                    "您可能不满足于固定的丹药配方，那么着手自行搭配一个丹方吧。",
                    "用任意1株药材与书本合成，得到佰草鉴，佰草鉴可以通过消耗药材获取其功效。",
                    "在药理台处制作丹方，或是在炼丹炉内直接保存。",
                    "如果是在炼丹炉内保存丹方，则需要一个空白的玉简，放置在左上角物品槽中。",
                    "在药材槽位放置药材，每个位置只会保存一株药材。点击玉简右侧第二个按钮来保存丹方。" };
            case 5 -> new String[] {
                    "§b灵力：§f炼丹炉需要有修仙界的火焰为其供能，请在丹炉下方点燃一团修仙界的火焰。",
                    "§6丹炉等级：§f炼制高于丹炉等级的丹药时，会有几率炸炉。",
                    "§6丹炉火力：§f百分比减少炼丹时长。",
                    "§6分丹能力：§f随机增加成丹数量上限，额外随机获得0到“分丹能力”个丹药。" };
            default -> new String[0];
        };
        for (String line : lines) player.displayClientMessage(Component.literal(line), false);
    }

    private static void combine(CompoundTag medicine, String key, String operation, int amount, boolean duration) {
        int current = medicine.getInt(key);
        switch (operation) {
            case "加" -> medicine.putInt(key, current + amount);
            case "减" -> medicine.putInt(key, current - amount);
            case "乘" -> medicine.putInt(key, (int) (current * (amount / 100.0)));
            // 来源持续时间的除法使用整数百分比，等级的除法使用小数百分比。
            case "除" -> medicine.putInt(key, duration ? current / (amount / 100) : (int) (current / (amount / 100.0)));
        }
    }

    private static void initialize(CompoundTag medicine, String key, String operation, int amount) {
        if (operation.equals("加")) medicine.putInt(key, amount);
        else if (operation.equals("减")) medicine.putInt(key, -amount);
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static Item item(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }
}
