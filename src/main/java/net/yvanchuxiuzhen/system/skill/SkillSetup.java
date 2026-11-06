package net.yvanchuxiuzhen.system.skill;

import java.util.Arrays;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.ItemSwingEvent;
import net.yvanchuxiuzhen.system.combat.api.HitEvent;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStudiedEvent;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.effect.SkillEffects;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.projectile.ElementProjectiles;
import net.yvanchuxiuzhen.system.skill.network.SkillActionPayload;

/**
 * 技能系统的启动装配，只在 Mod 构造时调用一次：内容表、技能书组件与玩家附件、技能按键与生成命令的服务端处理、
 * 右键施放、挥动武器触发武器技、每刻推进冷却，以及研习指定功法后附赠技能。
 */
public final class SkillSetup {
    private static final ContentTables CONTENT = new ContentTables("skill");

    private SkillSetup() {
    }

    /** 本系统内容表登记的实体类型（弹体与实体技能）。 */
    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }

    public static void register(IEventBus modBus) {
        SkillEffects.register(CONTENT);
        ElementProjectiles.register(CONTENT);
        SkillEntities.register(CONTENT);
        CONTENT.register(modBus);
        SkillData.register(modBus);
        modBus.addListener(SkillSetup::registerPayloads);
        SkillCommands.register();
        SkillDelays.register();
        NeoForge.EVENT_BUS.addListener(SkillSetup::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(SkillSetup::onPlayerTick);
        NeoForge.EVENT_BUS.addListener((ItemSwingEvent event) -> WeaponArts.onSwing(event));
        NeoForge.EVENT_BUS.addListener((HitEvent event) -> LingwenHit.onHit(event));
        NeoForge.EVENT_BUS.addListener((TechniqueStudiedEvent event) -> grantBonus(event.player(), event.technique()));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SkillActionPayload.TYPE, SkillActionPayload.STREAM_CODEC, SkillActionPayload::handle);
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getEntity() instanceof ServerPlayer player) {
            SkillCasting.onRightClick(player);
        }
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SkillCasting.tick(player);
            WeaponArts.tick(player);
        }
    }

    /** 研习“行尸御火”“真言吒雷诀”后，来源以权限 4 执行 {@code /jineng} 命令，书掉落在玩家位置。 */
    private static void grantBonus(ServerPlayer player, String technique) {
        SkillLibrary.bonusArguments(technique).ifPresent(arguments -> {
            ItemStack book = SkillLibrary.fromArguments(SkillKind.SPELL, Arrays.asList(arguments.split("\\s+")));
            SkillCommands.drop((ServerLevel) player.level(), player.position(), book);
        });
    }
}
