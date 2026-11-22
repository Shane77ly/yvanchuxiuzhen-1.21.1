package net.yvanchuxiuzhen.system.gongfa;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.HitEvent;
import net.yvanchuxiuzhen.system.combat.api.TechniqueMeteorEvent;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;

/** 主修功法的命中效果；来源按功法名称判断，不检查研习者。 */
final class TechniqueHit {
    private TechniqueHit() { }

    static void onHit(HitEvent event) {
        if (!(event.attacker() instanceof ServerPlayer player)) return;
        TechniqueBook book = Techniques.main(player).orElse(null);
        if (book == null) return;
        switch (book.definition().name()) {
            case "流星春水", "星君诀" -> {
                if (Math.random() < 0.2) {
                    double multiplier = book.definition().name().equals("流星春水") ? 1.2 : 2.4;
                    NeoForge.EVENT_BUS.post(new TechniqueMeteorEvent(player, event.victim(), multiplier));
                }
            }
            case "金廷落", "血魔祭魂秘法" -> {
                event.victim().setHealth((float) (event.victim().getHealth() - book.level() * 0.5));
                if (book.definition().name().equals("血魔祭魂秘法")) {
                    ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "xvejing")), 2));
                }
            }
            default -> { }
        }
    }
}
