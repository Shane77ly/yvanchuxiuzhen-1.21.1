package net.yvanchuxiuzhen.system.lianqi.content;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** 炼器鼎五类帮助按钮逐段发送 1.18.2 版本原说明。 */
public final class TripodHelp {
    private TripodHelp() {
    }

    public static void show(ServerPlayer player, int button) {
        switch (button) {
            case 3 -> lines(player,
                    "炼器鼎也可以为武器、护甲、法宝升级。",
                    "首先确保图纸位置为空，然后在材料槽位第一个地方放置你想要升级的武器（护甲、法宝），在后面放置想要融合进去的材料。",
                    "记得要在炼器鼎下方放置异火为炼器鼎充能，然后点击中间的炼制按钮开始炼制即可。",
                    "武器、法宝、护甲的重量如果大于等于100点，则会有负面状态，超重越多，负面状态越剧烈。");
            case 4 -> lines(player,
                    "把想要冶炼的矿石放入中间九个槽位中，点击按钮即可把矿石烧制成对应产出。",
                    "放入四个精铁锭，可以精炼成一个精钢锭。",
                    "同样的，四个霜铁锭可以精炼成一个霜金锭；四个玄铁锭可以精炼成一个玄钢锭；四个陨铁锭可以精炼成一个陨钢锭。",
                    "基质合金由四个基岩和四个百炼合金在工作台合成，基岩可以由有穿石附魔的镐子开采。（穿石附魂符可以在符箓商人处购买）",
                    "黑曜合金由四个黑曜石和四个寒潭精铁在工作台合成。");
            case 5 -> lines(player,
                    "制作一柄趁手的武器法宝，或是做一套甲胄，都是需要在炼器鼎炼制的。",
                    "购买或者宝箱、副本奖励等方式，获得的图纸，放入左上角的物品槽位中，",
                    "点击旁边的按钮，可以自动检测您身上是否有足够的材料，有足够材料就会自动放入进炼器鼎中。",
                    "点击材料槽位下面的按钮，开始炼制。",
                    "注意！需要在炼器鼎下方放置异火来为炼器鼎充能，手持异火在潜行状态下右键方块顶部即可放置。",
                    "可以对着放置的异火不断释放手中异火，来延长其燃烧的时间。",
                    "注意！如果您的炼器鼎等级和异火等级都比较低，那么炼制高级图纸的武器（法宝、护甲）就会有失败的风险！");
            case 6 -> lines(player,
                    "您是否已经不再满足于固定的炼器配方了？可以尝试原创一些炼器图纸。",
                    "在炼器鼎的图纸槽位放置一个空的玉简，然后在材料槽位第一个位置放置你想要的武器（护甲、法宝）作为模板。",
                    "然后在后面的材料槽位放置任意种类、数量的材料，点击图纸右侧第二个按钮，即可把当前配方保存成图纸。",
                    "放心，这个过程不会消耗任何武器、法宝、护甲、材料。",
                    "不过需要注意的是，最终成品的属性与您图纸需要的材料密切相关，可以使用千材录解锁并查看材料属性。",
                    "千材录需要任意材料1个与书本合成获得。");
            case 8 -> {
                player.displayClientMessage(Component.literal("炼器鼎等级：").append(Component.translatable("lianqibiao05")), false);
                player.displayClientMessage(Component.literal("炼器鼎火力：").append(Component.translatable("lianqibiao06")), false);
                player.displayClientMessage(Component.literal("炼器鼎提纯：").append(Component.translatable("lianqibiao07"))
                        .append(Component.translatable("lianqibiao08")), false);
            }
            default -> { }
        }
    }

    private static void lines(ServerPlayer player, String... lines) {
        for (String line : lines) player.displayClientMessage(Component.literal(line), false);
    }
}
