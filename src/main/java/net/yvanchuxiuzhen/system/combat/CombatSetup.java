package net.yvanchuxiuzhen.system.combat;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.combat.content.CombatMenus;
import net.yvanchuxiuzhen.system.combat.content.CrackedBedrockBlock;
import net.yvanchuxiuzhen.system.combat.content.WoodenDummyEntity;
import net.yvanchuxiuzhen.system.combat.content.WoodenDummyItem;
import net.yvanchuxiuzhen.system.combat.damage.HurtHandler;
import net.yvanchuxiuzhen.system.combat.damage.LightningLanding;
import net.yvanchuxiuzhen.system.combat.damage.ShanghaiCommand;
import net.yvanchuxiuzhen.system.combat.effect.CombatEffects;
import net.yvanchuxiuzhen.system.combat.enchant.BedrockCracking;
import net.yvanchuxiuzhen.system.combat.enchant.ChainMining;
import net.yvanchuxiuzhen.system.combat.network.DummyButtonPayload;

/**
 * 战斗系统的启动装配：状态效果、伤害事件、伤害与范围落雷命令、共享彩色闪电、连锁采集与穿石。
 */
public final class CombatSetup {
    private static final ContentTables CONTENT = new ContentTables("combat")
            .blockFactory("combat:bedrock_01", traits -> ContentBlock.create(traits, t -> new CrackedBedrockBlock(t, "bedrock_02")))
            .blockFactory("combat:bedrock_02", traits -> ContentBlock.create(traits, t -> new CrackedBedrockBlock(t, "bedrock_03")))
            .itemFactory("combat:murenzhuangitem", WoodenDummyItem::new)
            .livingEntity("murenzhuang", WoodenDummyEntity::new, WoodenDummyEntity::createAttributes);

    private CombatSetup() {
    }

    /** 本系统登记的实体类型（木人桩等），须在 {@link #register} 之后取用。 */
    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }

    public static void register(IEventBus modBus) {
        CombatEffects.register(CONTENT);
        CONTENT.register(modBus);
        CombatMenus.register(modBus);
        modBus.addListener(CombatSetup::registerPayloads);
        HurtHandler.register();
        ShanghaiCommand.register();
        LightningLanding.register(modBus);
        ChainMining.register();
        BedrockCracking.register();
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(DummyButtonPayload.TYPE, DummyButtonPayload.STREAM_CODEC, DummyButtonPayload::handle);
    }
}
