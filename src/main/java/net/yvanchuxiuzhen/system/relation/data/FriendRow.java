package net.yvanchuxiuzhen.system.relation.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 好友界面一行的显示内容，由服务端刷新时生成并同步给本人，不存档（来源 {@code displayfriend*<n>}）。
 * {@code slot} 是该行对应的好友格序号（从 1 起）；{@code level} 为“(境界)名字”或“(未上线)名字”；属性文本只在对方在线时有值。
 */
public record FriendRow(String name, String team, boolean star, int slot, boolean online, String level,
                        String health, String power, String soul, String attack, String mana, String soulAttack) {
    public static final FriendRow EMPTY = new FriendRow("", "", false, 0, false, "", "", "", "", "", "", "");
    public static final Codec<FriendRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(FriendRow::name),
            Codec.STRING.fieldOf("team").forGetter(FriendRow::team),
            Codec.BOOL.fieldOf("star").forGetter(FriendRow::star),
            Codec.INT.fieldOf("slot").forGetter(FriendRow::slot),
            Codec.BOOL.fieldOf("online").forGetter(FriendRow::online),
            Codec.STRING.fieldOf("level").forGetter(FriendRow::level),
            Codec.STRING.fieldOf("health").forGetter(FriendRow::health),
            Codec.STRING.fieldOf("power").forGetter(FriendRow::power),
            Codec.STRING.fieldOf("soul").forGetter(FriendRow::soul),
            Codec.STRING.fieldOf("attack").forGetter(FriendRow::attack),
            Codec.STRING.fieldOf("mana").forGetter(FriendRow::mana),
            Codec.STRING.fieldOf("soul_attack").forGetter(FriendRow::soulAttack)
    ).apply(instance, FriendRow::new));
}
