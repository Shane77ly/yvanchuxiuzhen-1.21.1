package net.yvanchuxiuzhen.system.skill.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 技能书实例（物品组件 {@code yvanchuxiuzhen:skill}，来源书的 NBT）。公共字段：类别、名字（{@code jinengName} 等）、
 * 品阶 {@code belongTopinjie}、所需境界 {@code belongToLevel}、灵根要求 {@code belongTobody}、简介 {@code belongTojianjie}、
 * 研习者 {@code belongToMasterUUID} 与 {@code belongToName}。各类别的其余参数在 {@code numbers}、{@code texts} 中，键沿用来源：
 * 技能为 {@code jinengneeditem}、{@code jinengmanaatt}、{@code jinengshanghai}、{@code jinengxiaohao}、{@code jinengjieyin}
 * 与六组 {@link SkillElement} 的冷却键；武器技为 {@code smanshiptype}、{@code smanshipID}、{@code smanshipnumMax}；
 * 身法为 {@code shenfatick} 等九项；轻功为 {@code flyskillxiaohao}、{@code flyskillspeed}、{@code flyskilltexiao}。
 * 品阶与境界的文字由数值推出（{@link net.yvanchuxiuzhen.system.skill.SkillText}），不另存。
 */
public record SkillBook(SkillKind kind, String name, double grade, double level, String body, String intro, Optional<UUID> owner,
                        String ownerName, Map<String, Double> numbers, Map<String, String> texts) {
    public static final Codec<SkillBook> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SkillKind.CODEC.fieldOf("kind").forGetter(SkillBook::kind),
            Codec.STRING.fieldOf("name").forGetter(SkillBook::name),
            Codec.DOUBLE.fieldOf("grade").forGetter(SkillBook::grade),
            Codec.DOUBLE.fieldOf("level").forGetter(SkillBook::level),
            Codec.STRING.fieldOf("body").forGetter(SkillBook::body),
            Codec.STRING.fieldOf("intro").forGetter(SkillBook::intro),
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(SkillBook::owner),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(SkillBook::ownerName),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("numbers", Map.of()).forGetter(SkillBook::numbers),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("texts", Map.of()).forGetter(SkillBook::texts)
    ).apply(instance, SkillBook::new));
    public static final StreamCodec<ByteBuf, SkillBook> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public double number(String key) {
        return numbers.getOrDefault(key, 0.0);
    }

    public String text(String key) {
        return texts.getOrDefault(key, "");
    }

    public boolean ownedBy(UUID player) {
        return owner.filter(player::equals).isPresent();
    }

    /** 技能施放消耗系数 {@code jinengxiaohao}。 */
    public double consumption() {
        return number("jinengxiaohao");
    }

    /** 技能的法术攻击倍率 {@code jinengmanaatt}。 */
    public double manaAttack() {
        return number("jinengmanaatt");
    }

    /** 技能的伤害倍率 {@code jinengshanghai}。 */
    public double damageMultiplier() {
        return number("jinengshanghai");
    }

    /** 施放后给施放者的禁施时间 {@code jinengjieyin}（刻）。 */
    public double seal() {
        return number("jinengjieyin");
    }

    /** 所需武器类别 {@code jinengneeditem}：空或“无”表示任意武器。 */
    public String neededWeapon() {
        return text("jinengneeditem");
    }

    public double cooldown(SkillElement element) {
        return number(element.cooldownKey());
    }

    public double cooldownIgnore(SkillElement element) {
        return number(element.ignoreKey());
    }

    public SkillBook withOwner(UUID player, String playerName) {
        return new SkillBook(kind, name, grade, level, body, intro, Optional.of(player), playerName, numbers, texts);
    }

    public SkillBook withNumber(String key, double value) {
        Map<String, Double> next = new HashMap<>(numbers);
        next.put(key, value);
        return new SkillBook(kind, name, grade, level, body, intro, owner, ownerName, Map.copyOf(next), texts);
    }

    public SkillBook withText(String key, String value) {
        Map<String, String> next = new HashMap<>(texts);
        next.put(key, value);
        return new SkillBook(kind, name, grade, level, body, intro, owner, ownerName, numbers, Map.copyOf(next));
    }
}
