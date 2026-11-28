package net.yvanchuxiuzhen.system.npc;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.EconomyCatalogs;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;
import net.yvanchuxiuzhen.system.npc.api.NpcDefinitions;
import net.yvanchuxiuzhen.system.npc.api.NpcDialogueRequestEvent;
import net.yvanchuxiuzhen.system.npc.entity.NpcProfiles;

/** 来源全局修士右键入口，实例对话模板与商店目录由修士持有，玩家会话交任务与经济系统维护。 */
public final class NpcInteractions {
    private static final TagKey<EntityType<?>> NPC = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "npcentity"));

    private NpcInteractions() { }

    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand()) return;
        Entity npc = event.getTarget();
        if (player.getMainHandItem().getItem() instanceof NpcManagementItem) {
            if (npc instanceof Mob managed) NpcProfiles.apply(player.serverLevel(), event.getPos(), managed, player.getMainHandItem());
            return;
        }
        NpcDialogueProfile profile = npc.getData(NpcData.PROFILE);
        if (npc.getType().is(NPC)) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType()).getPath();
            profile = switch (id) {
                case "businessman_01" -> profile.withScript(0, "npc01");
                case "businessman_02", "businessman_05", "businessman_06", "businessman_07" -> {
                    String key = "npc" + id.substring(id.length() - 2);
                    yield profile.withScript(0, key).withShopId(key).withCatalog(Optional.empty());
                }
                case "businessman_03" -> refreshCatalog(npc, profile, 1, "npc03").withScript(0, "npc03");
                case "businessman_04" -> profile.withScript(0, "npc04").withShopId("").withCatalog(Optional.empty());
                case "daguanjia" -> profile.withScript(0, "npcdaguanjia04").withScript(1, "").withScript(2, "");
                case "caishenye" -> profile.withScript(0, "npccaishenye").withShopId("npccaishenye").withCatalog(Optional.empty());
                case "businessman_09" -> profile.withScript(0, "default_shop").withShopId("npc09_ores").withCatalog(Optional.empty());
                case "businessman_10", "businessman_11" -> {
                    boolean high = id.equals("businessman_11");
                    yield refreshCatalog(npc, profile, high ? 3 : 2, high ? "npc11_info" : "npc10_info").withScript(0, "default_shop");
                }
                default -> profile;
            };
            npc.setData(NpcData.PROFILE, profile);
            NpcDefinitions.setInteractiveNpc(npc, true);
        }
        if (!NpcDefinitions.isInteractiveNpc(npc) || BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem())
                .equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandao_np_csetitem"))) return;
        NeoForge.EVENT_BUS.post(new NpcDialogueRequestEvent(player, profile.activeScripts(),
                profile.products(), profile.shopInfo(), profile.shopId(), profile.catalog()));
    }

    private static NpcDialogueProfile refreshCatalog(Entity npc, NpcDialogueProfile profile, int grade, String infoCatalogId) {
        Scoreboard scoreboard = npc.level().getScoreboard();
        Objective objective = scoreboard.getObjective("keyishuaxin");
        boolean empty = profile.catalog().isEmpty() || profile.catalog().get().products().isEmpty();
        if (empty || (objective != null && scoreboard.getOrCreatePlayerScore(npc, objective).get() == 1)) {
            if (objective != null) scoreboard.getOrCreatePlayerScore(npc, objective).set(0);
            ShopCatalogDefinition generated = EconomyCatalogs.materialShop(grade, EconomyCatalogs.catalog(infoCatalogId).info());
            return profile.withShopId("").withCatalog(Optional.of(generated));
        }
        return profile;
    }
}
