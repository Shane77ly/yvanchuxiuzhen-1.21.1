package net.yvanchuxiuzhen.system.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/** 天道 NPC 管理工具保存的档案、对话脚本、商店与六格穿戴装备（{@code yvanchuxiuzhen:npc_tool_profile}）。 */
public record NpcToolProfile(String name, double level, List<String> quests, String products,
                             String shopInfo, Map<String, String> ratings, boolean fixed,
                             List<ItemStack> equipment) {
    public static final NpcToolProfile EMPTY = new NpcToolProfile("", 0, List.of(), "", "", Map.of(), false, List.of());
    public static final Codec<NpcToolProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(NpcToolProfile::name),
            Codec.DOUBLE.optionalFieldOf("level", 0.0).forGetter(NpcToolProfile::level),
            Codec.STRING.listOf().optionalFieldOf("quests", List.of()).forGetter(NpcToolProfile::quests),
            Codec.STRING.optionalFieldOf("products", "").forGetter(NpcToolProfile::products),
            Codec.STRING.optionalFieldOf("shop_info", "").forGetter(NpcToolProfile::shopInfo),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("ratings", Map.of()).forGetter(NpcToolProfile::ratings),
            Codec.BOOL.optionalFieldOf("fixed", false).forGetter(NpcToolProfile::fixed),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("equipment", List.of()).forGetter(NpcToolProfile::equipment)
    ).apply(instance, NpcToolProfile::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, NpcToolProfile> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public NpcToolProfile {
        quests = List.copyOf(quests);
        ratings = Map.copyOf(ratings);
        List<ItemStack> copies = new ArrayList<>(6);
        for (int index = 0; index < 6; index++) {
            ItemStack stack = index < equipment.size() ? equipment.get(index) : ItemStack.EMPTY;
            copies.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
        equipment = List.copyOf(copies);
    }

    public String quest(int slotIndex) {
        return slotIndex >= 0 && slotIndex < quests.size() ? quests.get(slotIndex) : "";
    }

    public String fieldValue(String key) {
        if (key.equals("npcname")) return name;
        if (key.equals("npclevel")) return Double.toString(level);
        if (key.equals("productlist")) return products;
        if (key.equals("shopinformation")) return shopInfo;
        if (key.startsWith("renwulist")) {
            int slot = Integer.parseInt(key.substring("renwulist".length())) - 1;
            return quest(slot);
        }
        return ratings.getOrDefault(key, "");
    }

    public NpcToolProfile withEditedFields(List<String> values, double nextLevel, boolean nextFixed) {
        String nextName = name;
        List<String> nextQuests = new ArrayList<>(10);
        for (int index = 0; index < 10; index++) nextQuests.add(quest(index));
        String nextProducts = products;
        String nextShopInfo = shopInfo;
        Map<String, String> nextRatings = new LinkedHashMap<>(ratings);
        for (int index = 0; index < values.size(); index++) {
            String key = NpcManagementData.KEYS.get(index);
            String value = values.get(index);
            if (key.equals("npcname")) nextName = value;
            else if (key.equals("productlist")) nextProducts = value;
            else if (key.equals("shopinformation")) nextShopInfo = value;
            else if (key.startsWith("renwulist")) nextQuests.set(Integer.parseInt(key.substring("renwulist".length())) - 1, value);
            else if (!key.equals("npclevel")) nextRatings.put(key, value);
        }
        return new NpcToolProfile(nextName, nextLevel, nextQuests, nextProducts, nextShopInfo,
                nextRatings, fixed || nextFixed, equipment);
    }

    public NpcToolProfile withEquipment(List<ItemStack> nextEquipment) {
        return new NpcToolProfile(name, level, quests, products, shopInfo, ratings, fixed, nextEquipment);
    }
}
