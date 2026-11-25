package net.yvanchuxiuzhen.system.xiulian;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationAfterBreakthroughEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationAfterRecoveryEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.LifetimeExhaustedEvent;
import net.yvanchuxiuzhen.system.xiulian.tribulation.TribulationCloud;
import net.yvanchuxiuzhen.system.xiulian.tribulation.Tribulations;

/**
 * 修炼的每秒推进（来源 {@code PlayerTick} 中以世界时间 {@code dayTime} 与 {@code tizhiitem.jishiqi} 相差 20 为间隔的部分）：
 * 闭关修炼、发出 {@link CultivationSecondEvent}、吸收待吸收修为（受每日上限约束）、妖化冷却、阳寿已尽提示、
 * 修为圆满时突破或开启雷劫、闭关冷却、恢复。世界时间倒退时重设计时基准。
 * 雷劫经 {@code ThunderBegin} 对应过程生成劫云；阳寿已尽后延迟请求轮回。
 */
public final class CultivationTick {
    private static final Set<ResourceKey<Level>> NO_BREAKTHROUGH = Set.of(
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fabao_009weidu")),
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "xvkong")));

    private CultivationTick() {
    }

    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tick(player);
        }
    }

    /** 来源 {@code WorldTickProcedure}：每 24000 世界刻对当前维度在线玩家寿元减 1。 */
    static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (XiulianWorldData.levelClock(serverLevel).advanceDay()) {
            for (ServerPlayer player : List.copyOf(serverLevel.players())) {
                Cultivation.add(player, CultivationValue.LIFETIME, -1);
            }
        }
    }

    /** 来源 {@code QianwangweiduProcedure}：玩家在雷劫开启状态切换维度时关闭当前雷劫并将待渡次数加 1。 */
    static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Cultivation.thunderOpen(player)) {
            interruptTribulation(player);
        }
    }

    /** 来源 {@code PlayerLeaveGameProcedure}：玩家在雷劫开启状态登出时关闭雷劫、补回待渡次数并清除头顶劫云。 */
    static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !Cultivation.thunderOpen(player)) {
            return;
        }
        interruptTribulation(player);
        Vec3 center = new Vec3(player.getX(), player.getY() + 64, player.getZ());
        for (TribulationCloud cloud : player.serverLevel().getEntitiesOfClass(TribulationCloud.class, new AABB(center, center).inflate(5))) {
            cloud.discard();
        }
    }

    private static void interruptTribulation(ServerPlayer player) {
        Cultivation.setThunderOpen(player, false);
        Cultivation.set(player, CultivationValue.THUNDER_MAX, 0);
        Cultivation.add(player, CultivationValue.THUNDER, 1);
    }

    /** 来源 {@code PlayerJoinGame}：设置了每日修为上限且记录日期不是今天（现实日期）时清零当日已获修为。 */
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (XiulianWorldData.get(player.server).dailyExperience() != 0) {
            String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            if (!Cultivation.tizhiString(player, "dayjilu").equals(today)) {
                Cultivation.setTizhiString(player, "dayjilu", today);
                Cultivation.setTizhiDouble(player, "dailyexperience", 0);
            }
        }
    }

    private static void tick(ServerPlayer player) {
        long time = player.level().getDayTime();
        double timer = Cultivation.tizhiDouble(player, "jishiqi");
        if (time >= timer + 20) {
            if (Cultivation.tizhiString(player, "xiuliankaiguan").equals("修炼")) {
                Cultivating.tick(player);
            }
            NeoForge.EVENT_BUS.post(new CultivationSecondEvent(player));
            absorb(player);
            if (Cultivation.tizhiDouble(player, "yaohualengqve") >= 1) {
                Cultivation.setTizhiDouble(player, "yaohualengqve", Cultivation.tizhiDouble(player, "yaohualengqve") - 1);
            }
            if (Cultivation.get(player, CultivationValue.LIFETIME) <= 0) {
                player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cultivation.lifetime_exhausted"), false);
                player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cultivation.lifetime_exhausted"), true);
                ServerLevel sourceLevel = player.serverLevel();
                Delayed.run(40, () -> NeoForge.EVENT_BUS.post(new LifetimeExhaustedEvent(player, sourceLevel)));
            }
            breakthrough(player);
            NeoForge.EVENT_BUS.post(new CultivationAfterBreakthroughEvent(player));
            cooldown(player);
            Recovery.tick(player);
            NeoForge.EVENT_BUS.post(new CultivationAfterRecoveryEvent(player));
            Cultivation.setTizhiDouble(player, "jishiqi", time);
        } else if (time < timer) {
            Cultivation.setTizhiDouble(player, "jishiqi", time);
        }
    }

    /**
     * 待吸收修为 × 修为效率（另加 {@code EwaiExperience} 与 {@code EwaiExperiencePercentage} × 突破所需修为）转入已有修为；
     * 雷劫中清空；设置了每日上限时超出部分作废并提示。
     */
    private static void absorb(ServerPlayer player) {
        double stand = Cultivation.get(player, CultivationValue.EXPERIENCE_STAND);
        if (stand < 0) {
            Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, 0);
            return;
        }
        if (stand == 0) {
            return;
        }
        if (Cultivation.thunderOpen(player)) {
            Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, 0);
            return;
        }
        double extra = 0;
        if (Cultivation.tizhiDouble(player, "EwaiExperience") != 0) {
            extra = Cultivation.tizhiDouble(player, "EwaiExperience");
        }
        if (Cultivation.tizhiDouble(player, "EwaiExperiencePercentage") != 0) {
            extra += Cultivation.get(player, CultivationValue.EXPERIENCE_MAX) * Cultivation.tizhiDouble(player, "EwaiExperiencePercentage");
        }
        double gained = (stand + extra) * Cultivation.get(player, CultivationValue.EXPERIENCE_RATE);
        Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, 0);
        if (gained <= 0) {
            return;
        }
        double cap = XiulianWorldData.get(player.server).dailyExperience();
        if (cap == 0) {
            Cultivation.add(player, CultivationValue.EXPERIENCE, gained);
            return;
        }
        double used = Cultivation.tizhiDouble(player, "dailyexperience");
        if (used + gained < cap) {
            Cultivation.add(player, CultivationValue.EXPERIENCE, gained);
            Cultivation.setTizhiDouble(player, "dailyexperience", used + gained);
        } else if (used >= cap) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cultivation.daily_limit").withStyle(ChatFormatting.DARK_RED), true);
            Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, 0);
            return;
        } else {
            Cultivation.add(player, CultivationValue.EXPERIENCE, cap - used);
            Cultivation.setTizhiDouble(player, "dailyexperience", cap);
        }
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cultivation.daily_progress",
                Cultivation.tizhiDouble(player, "dailyexperience"), cap), true);
    }

    /**
     * 已有修为达到突破所需时：在法宝 009 维度与虚空中停在所需值减 1；非创造模式且层数需要渡劫时开启雷劫并把修为置 1；
     * 否则突破并扣除所需修为，按新层数重算所需修为。另有 {@code jinji} 标记时直接突破一次。
     */
    private static void breakthrough(ServerPlayer player) {
        double experience = Cultivation.get(player, CultivationValue.EXPERIENCE);
        double max = Cultivation.get(player, CultivationValue.EXPERIENCE_MAX);
        if (experience >= max) {
            if (NO_BREAKTHROUGH.contains(player.level().dimension())) {
                Cultivation.set(player, CultivationValue.EXPERIENCE, max - 1);
            } else if (!player.isCreative() && LevelUp.needsTribulation(Cultivation.get(player, CultivationValue.LEVEL_NUMBER))) {
                Cultivation.setThunderOpen(player, true);
                Tribulations.begin(player);
                Cultivation.set(player, CultivationValue.EXPERIENCE, 1);
                player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cultivation.tribulation_begin"), false);
            } else {
                LevelUp.levelUp(player);
                Cultivation.set(player, CultivationValue.EXPERIENCE, Cultivation.get(player, CultivationValue.EXPERIENCE) - max);
                Cultivation.set(player, CultivationValue.EXPERIENCE_MAX, LevelUp.experienceMax(player));
            }
        } else if (Cultivation.tizhiBoolean(player, "jinji")) {
            LevelUp.levelUp(player);
            Cultivation.set(player, CultivationValue.EXPERIENCE_MAX, LevelUp.experienceMax(player));
            Cultivation.setTizhiBoolean(player, "jinji", false);
        }
    }

    /** 闭关冷却：每秒累计 1 + {@code XiulianColdCorrect}，满 15 时冷却减 1。 */
    private static void cooldown(ServerPlayer player) {
        if (Cultivation.get(player, CultivationValue.CULTIVATION_COOLDOWN) <= 0) {
            return;
        }
        double counter = Cultivation.tizhiDouble(player, "xiulianlengqve") + 1 + Cultivation.tizhiDouble(player, "XiulianColdCorrect");
        if (counter >= 15) {
            Cultivation.add(player, CultivationValue.CULTIVATION_COOLDOWN, -1);
            counter = 0;
        }
        Cultivation.setTizhiDouble(player, "xiulianlengqve", counter);
    }

    static boolean hasEffect(ServerPlayer player, String path) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path))
                .map(player::hasEffect).orElse(false);
    }
}
