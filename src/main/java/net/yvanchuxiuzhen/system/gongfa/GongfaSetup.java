package net.yvanchuxiuzhen.system.gongfa;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.combat.api.HitEvent;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.gongfa.content.ComprehensionTableBlock;
import net.yvanchuxiuzhen.system.gongfa.content.GongfaBlockEntities;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.gongfa.network.GongfaButtonPayload;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationAfterRecoveryEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationExperienceEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;
import net.yvanchuxiuzhen.system.xiulian.api.RetreatTechniqueEvent;

/**
 * 功法系统的启动装配，只在 Mod 构造时调用一次：内容表（参悟台换用专属方块类型）、功法组件与功法栏附件、参悟台的方块实体、
 * 菜单与按钮，以及对修炼推进的监听
 * （每秒推进时 {@code xiuliankaiguan} 为“功法”则进行功法修炼，闭关修炼吸纳元气后给主修功法加经验，恢复后比较功法切换）。
 */
public final class GongfaSetup {
    private static final ContentTables CONTENT = new ContentTables("gongfa")
            .blockFactory("gongfa:canwutai_block", traits -> ContentBlock.create(traits, ComprehensionTableBlock::new));

    private GongfaSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        GongfaData.register(modBus);
        GongfaBlockEntities.register(modBus);
        TechniqueCommands.register();
        modBus.addListener(GongfaSetup::registerPayloads);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, CultivationSecondEvent.class, event -> onSecond(event.player()));
        NeoForge.EVENT_BUS.addListener((CultivationExperienceEvent event) -> TechniqueRules.onCultivationExperience(event.player(), event.experience()));
        NeoForge.EVENT_BUS.addListener((CultivationAfterRecoveryEvent event) -> TechniqueRules.refreshEquipped(event.player()));
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, false, HitEvent.class, TechniqueHit::onHit);
        NeoForge.EVENT_BUS.addListener(GongfaSetup::retreatTechnique);
        NeoForge.EVENT_BUS.addListener(GongfaSetup::onStruckByLightning);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(GongfaButtonPayload.TYPE, GongfaButtonPayload.STREAM_CODEC, GongfaButtonPayload::handle);
    }

    private static void retreatTechnique(RetreatTechniqueEvent event) {
        TechniqueBook book = Techniques.main(event.player()).orElse(null);
        if (book == null || book.definition().name().isEmpty()) event.setAccess(RetreatTechniqueEvent.Access.NONE);
        else if (book.owner().isEmpty()) event.setAccess(RetreatTechniqueEvent.Access.UNLEARNED);
        else if (book.ownedBy(event.player().getUUID())) event.setAccess(RetreatTechniqueEvent.Access.OWNED);
        else event.setAccess(RetreatTechniqueEvent.Access.OTHER);
    }

    private static void onSecond(ServerPlayer player) {
        if (Cultivation.tizhiString(player, "xiuliankaiguan").equals("功法")) {
            TechniqueRules.train(player);
        }
    }

    /** 来源 {@code EntityThunderProcedure}：主修御雷系列功法且玩家被闪电击中时给予法攻增益与迅捷效果。 */
    private static void onStruckByLightning(EntityStruckByLightningEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        TechniqueBook book = Techniques.main(player).orElse(null);
        if (book == null) {
            return;
        }
        int amplifier = switch (book.definition().name()) {
            case "御雷法" -> 0;
            case "雷脉功" -> 1;
            case "五雷炼体诀" -> 2;
            case "九霄真雷功" -> 3;
            default -> -1;
        };
        if (amplifier >= 0) {
            StatusEffects.add(player, "effect_013", 2400, amplifier);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, amplifier));
        }
    }
}
