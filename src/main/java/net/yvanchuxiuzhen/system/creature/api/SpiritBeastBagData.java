package net.yvanchuxiuzhen.system.creature.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 灵兽袋物品承载的强类型收纳妖兽快照与状态组件（{@code yvanchuxiuzhen:spirit_beast_bag}）。 */
public record SpiritBeastBagData(
        String bagUuid, String creatureId, String creatureName, String ownerUuid, String team,
        boolean released, boolean tamed, double skin, double size,
        String character, double charDef, double charAtt, double charInterval, double charResist,
        Map<String, String> texts, Map<String, Double> numbers) {
    public static final SpiritBeastBagData EMPTY = new SpiritBeastBagData(
            "", "", "", "", "", false, false, 0, 0, "", 0, 0, 0, 0, Map.of(), Map.of());
    public static final Codec<SpiritBeastBagData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("bag_uuid", "").forGetter(SpiritBeastBagData::bagUuid),
            Codec.STRING.optionalFieldOf("creature_id", "").forGetter(SpiritBeastBagData::creatureId),
            Codec.STRING.optionalFieldOf("creature_name", "").forGetter(SpiritBeastBagData::creatureName),
            Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(SpiritBeastBagData::ownerUuid),
            Codec.STRING.optionalFieldOf("team", "").forGetter(SpiritBeastBagData::team),
            Codec.BOOL.optionalFieldOf("released", false).forGetter(SpiritBeastBagData::released),
            Codec.BOOL.optionalFieldOf("tamed", false).forGetter(SpiritBeastBagData::tamed),
            Codec.DOUBLE.optionalFieldOf("skin", 0.0).forGetter(SpiritBeastBagData::skin),
            Codec.DOUBLE.optionalFieldOf("size", 0.0).forGetter(SpiritBeastBagData::size),
            Codec.STRING.optionalFieldOf("character", "").forGetter(SpiritBeastBagData::character),
            Codec.DOUBLE.optionalFieldOf("char_def", 0.0).forGetter(SpiritBeastBagData::charDef),
            Codec.DOUBLE.optionalFieldOf("char_att", 0.0).forGetter(SpiritBeastBagData::charAtt),
            Codec.DOUBLE.optionalFieldOf("char_interval", 0.0).forGetter(SpiritBeastBagData::charInterval),
            Codec.DOUBLE.optionalFieldOf("char_resist", 0.0).forGetter(SpiritBeastBagData::charResist),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("texts", Map.of()).forGetter(SpiritBeastBagData::texts),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("numbers", Map.of()).forGetter(SpiritBeastBagData::numbers)
    ).apply(instance, SpiritBeastBagData::new));
    public static final StreamCodec<ByteBuf, SpiritBeastBagData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public SpiritBeastBagData {
        texts = Map.copyOf(texts);
        numbers = Map.copyOf(numbers);
    }

    public boolean isEmpty() {
        return bagUuid.isEmpty() && creatureId.isEmpty() && creatureName.isEmpty() && ownerUuid.isEmpty()
                && team.isEmpty() && !released && !tamed && skin == 0 && size == 0
                && character.isEmpty() && charDef == 0 && charAtt == 0 && charInterval == 0 && charResist == 0
                && texts.isEmpty() && numbers.isEmpty();
    }

    public SpiritBeastBagData withReleased(boolean newReleased) {
        return new SpiritBeastBagData(bagUuid, creatureId, creatureName, ownerUuid, team, newReleased, tamed,
                skin, size, character, charDef, charAtt, charInterval, charResist, texts, numbers);
    }

    public static SpiritBeastBagData fromTag(CompoundTag data) {
        if (data.isEmpty()) return EMPTY;
        Map<String, String> texts = new LinkedHashMap<>();
        Map<String, Double> numbers = new LinkedHashMap<>();
        for (String key : data.getAllKeys()) {
            switch (key) {
                case "lingshoudaiuuid", "yaoshouid", "yaoshouname", "belongToMasterUUID", "team",
                        "yaoshoushifang", "yaoshouxvnfu", "yaoshoupifuxianshi", "entitydaxiao",
                        "character", "character_def", "character_att", "character_interval", "character_resist" -> { }
                default -> {
                    byte type = data.getTagType(key);
                    if (type == Tag.TAG_STRING) texts.put(key, data.getString(key));
                    else if (type == Tag.TAG_DOUBLE || type == Tag.TAG_FLOAT || type == Tag.TAG_INT
                            || type == Tag.TAG_LONG || type == Tag.TAG_SHORT) {
                        numbers.put(key, data.getDouble(key));
                    }
                }
            }
        }
        return new SpiritBeastBagData(
                data.getString("lingshoudaiuuid"),
                data.getString("yaoshouid"),
                data.getString("yaoshouname"),
                data.getString("belongToMasterUUID"),
                data.getString("team"),
                data.getBoolean("yaoshoushifang"),
                data.getBoolean("yaoshouxvnfu"),
                data.getDouble("yaoshoupifuxianshi"),
                data.getDouble("entitydaxiao"),
                data.getString("character"),
                data.getDouble("character_def"),
                data.getDouble("character_att"),
                data.getDouble("character_interval"),
                data.getDouble("character_resist"),
                texts,
                numbers);
    }

    public CompoundTag toTag() {
        CompoundTag data = new CompoundTag();
        if (!bagUuid.isEmpty()) data.putString("lingshoudaiuuid", bagUuid);
        if (!creatureId.isEmpty()) data.putString("yaoshouid", creatureId);
        if (!creatureName.isEmpty()) data.putString("yaoshouname", creatureName);
        if (!ownerUuid.isEmpty()) data.putString("belongToMasterUUID", ownerUuid);
        if (!team.isEmpty()) data.putString("team", team);
        if (released) data.putBoolean("yaoshoushifang", true);
        if (tamed) data.putBoolean("yaoshouxvnfu", true);
        if (skin != 0) data.putDouble("yaoshoupifuxianshi", skin);
        if (size != 0) data.putDouble("entitydaxiao", size);
        if (!character.isEmpty()) {
            data.putString("character", character);
            data.putDouble("character_def", charDef);
            data.putDouble("character_att", charAtt);
            data.putDouble("character_interval", charInterval);
            data.putDouble("character_resist", charResist);
        }
        texts.forEach(data::putString);
        numbers.forEach(data::putDouble);
        return data;
    }
}
