package net.yvanchuxiuzhen.system.relation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.relation.content.RecruitmentItem;
import net.yvanchuxiuzhen.system.relation.content.RelationMenus;
import net.yvanchuxiuzhen.system.relation.data.Allegiance;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;
import net.yvanchuxiuzhen.system.relation.network.RelationButtonPayload;

/**
 * 关系系统的启动装配，只在 Mod 构造时调用一次。
 * 登录时没有团队的玩家得到 8 个随机小写字母的团队名，并把本人团队加入友方集合（来源 {@code PlayerJoinGame}）；
 * 主手持团队招募令右键玩家时发出好友申请（来源 {@code RightClickEntity} 的玩家分支）。
 */
public final class RelationSetup {
    private static final ContentTables CONTENT = new ContentTables("relation").itemFactory("relation:ling_15", RecruitmentItem::new);
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private RelationSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        RelationAttachments.register(modBus);
        RelationMenus.register(modBus);
        modBus.addListener(RelationSetup::registerPayloads);
        NeoForge.EVENT_BUS.addListener(RelationSetup::onLogin);
        NeoForge.EVENT_BUS.addListener(RelationSetup::onInteractEntity);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(RelationButtonPayload.TYPE, RelationButtonPayload.STREAM_CODEC, RelationButtonPayload::handle);
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        Allegiance allegiance = player.getData(RelationAttachments.PLAYER).allegiance();
        if (allegiance.team().isEmpty()) {
            RandomSource random = player.getRandom();
            StringBuilder team = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                team.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
            }
            allegiance.setTeam(team.toString());
        }
        allegiance.setFriendly(allegiance.team(), true);
        player.syncData(RelationAttachments.PLAYER);
    }

    /** 来源按 {@code getHand() == getUsedItemHand()} 过滤，未在使用物品时即主手。 */
    private static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity() instanceof ServerPlayer source && event.getTarget() instanceof ServerPlayer target
                && event.getHand() == source.getUsedItemHand() && source.getMainHandItem().getItem() instanceof RecruitmentItem) {
            TeamRequest.request(source, target);
        }
    }
}
