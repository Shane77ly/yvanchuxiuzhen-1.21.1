package net.yvanchuxiuzhen.system.xiulian;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationExperienceEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;

/**
 * 闭关修炼的每天推进（来源 {@code Kaishixiulian}，{@code tizhiitem.xiuliankaiguan} 为“修炼”时每秒一次）。
 * 条件：闭关天数至少 1、功法栏有书、骑乘（打坐）且闭关冷却小于 100。每天消耗 1 点饥饿（饿时停止闭关并虚弱，本次仍结算），
 * 罡煞向 0 靠 1，天数与寿元各减 1，吸纳元气 = 功法品阶 ×（功法层数 + 1）× 维度与群系系数，另按特定功法、血流与修炼速度修正给额外修为，
 * 两者加入待吸收修为，闭关冷却加 1，并发出 {@link CultivationExperienceEvent} 供 gongfa 给主修功法加经验。
 */
final class Cultivating {
    private static final Map<String, Double> DIMENSION_FACTORS = Map.of(
            "minecraft:overworld", 0.5, "minecraft:the_nether", 0.2, "minecraft:the_end", 1.0,
            ModId.VALUE + ":dimensiontianqve", 3.0, ModId.VALUE + ":dimensionxiuluo", 2.0, ModId.VALUE + ":dimensionrenjian", 1.0,
            ModId.VALUE + ":dimensionyaohun", 1.5, ModId.VALUE + ":dimensionwugui", 0.5, ModId.VALUE + ":dimensiondiling", 1.0);
    private static final Set<String> BIOMES_PLUS_01 = Set.of("biomejade_01", "biomejade_02", "biomejade_03", "biomepeach_01", "biomepeach_02");
    private static final Set<String> BIOMES_MINUS_01 = Set.of("biomecloud_01", "biomestone_01", "biomestone_02", "biomestone_03");
    private static final Set<String> BIOMES_MINUS_02 = Set.of("biomecloud_03", "biomewinter_01", "biomewinter_02", "biomewinter_03");
    private static final Set<String> BLOOD_TECHNIQUES = Set.of("御血术", "汲血功", "地血大法", "血魔祭魂秘法");
    private static final Set<String> YIN_TECHNIQUES = Set.of("玄阴诀", "化阴大法", "擎天阴魔功");
    private static final Set<String> LIGHT_TECHNIQUES = Set.of("明光诀", "大光明功", "明镜映心诀", "亘古元光功");
    private static final Set<String> BLOOD_FLUIDS = Set.of(ModId.VALUE + ":xveliu", ModId.VALUE + ":flowing_xveliu");
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private Cultivating() {
    }

    static void tick(ServerPlayer player) {
        if (Cultivation.get(player, CultivationValue.CULTIVATION_DAYS) < 1) {
            return;
        }
        MainTechnique technique = Cultivation.mainTechnique(player);
        double cooldown = Cultivation.get(player, CultivationValue.CULTIVATION_COOLDOWN);
        if (technique.present() && player.isPassenger() && cooldown < 100) {
            cultivate(player, technique);
        } else if (!technique.present()) {
            removeCultivationEffects(player);
            stop(player, "结束修炼");
        } else if (cooldown >= 100 + Cultivation.tizhiDouble(player, "XiulianColdShangxianCorrect")) {
            BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "effect_013"))
                    .ifPresent(effect -> player.addEffect(new MobEffectInstance(effect, 2400, 1)));
            player.displayClientMessage(Component.literal("你感到心湖泛起汹涌的力量，让你无法进入修炼状态，不如趁着力量上涌时外出游历一番。"), false);
            player.displayClientMessage(Component.literal("【" + (cooldown - (100 + Cultivation.tizhiDouble(player, "XiulianColdShangxianCorrect"))) * 15 + "秒】"), false);
            Cultivation.setTizhiString(player, "xiuliankaiguan", "");
            Cultivation.set(player, CultivationValue.CULTIVATION_DAYS, 0);
        } else {
            stop(player, "结束修炼");
        }
    }

    private static void cultivate(ServerPlayer player, MainTechnique technique) {
        if (player.getFoodData().getFoodLevel() >= 1) {
            player.causeFoodExhaustion(1);
        } else {
            removeCultivationEffects(player);
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
            stop(player, "太饿了，无法继续修炼。");
        }
        double vigorous = Cultivation.get(player, CultivationValue.VIGOROUS);
        if (vigorous >= 1) {
            Cultivation.add(player, CultivationValue.VIGOROUS, -1);
        } else if (vigorous <= -1) {
            Cultivation.add(player, CultivationValue.VIGOROUS, 1);
        }
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        double factor = DIMENSION_FACTORS.getOrDefault(level.dimension().location().toString(), 1.0) + biomeBonus(level, pos);
        factor += effectAmplifierBonus(player);
        double extra = 0;
        String name = technique.name();
        if (BLOOD_TECHNIQUES.contains(name) && nearBlood(level, pos)) {
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "xvejing"))));
            extra += 5;
        }
        Cultivation.add(player, CultivationValue.CULTIVATION_DAYS, -1);
        Cultivation.add(player, CultivationValue.LIFETIME, -1);
        double gained = technique.grade() * (technique.level() + 1) * factor;
        double grade = technique.grade();
        if (YIN_TECHNIQUES.contains(name)) {
            String dimension = level.dimension().location().toString();
            if (dimension.equals(ModId.VALUE + ":dimensionwugui")) {
                extra = grade * 4;
            } else if (dimension.equals(ModId.VALUE + ":dimensiondiling")) {
                extra = grade * 8;
            } else if (!level.isDay() && level.getMaxLocalRawBrightness(pos) <= 5) {
                extra = grade * 2;
            }
        } else if (LIGHT_TECHNIQUES.contains(name)) {
            if (level.canSeeSkyFromBelowWater(pos) && level.isDay() && level.getMaxLocalRawBrightness(pos) >= 13) {
                extra = grade * 4;
            }
        } else if (name.equals("聚元功")) {
            extra = gained * 0.0025 * technique.level();
        } else if (name.equals("聚灵小周天神功")) {
            extra = gained * 0.005 * technique.level();
        } else if (name.equals("聚灵大周天神功")) {
            extra = gained * 0.01 * technique.level();
        } else if (name.equals("紫薇吐纳")) {
            if (level.canSeeSkyFromBelowWater(pos) && !level.isDay() && level.getMaxLocalRawBrightness(pos) <= 5) {
                extra = grade * 4;
            }
        }
        if (Cultivation.tizhiDouble(player, "Xiuliansudu") != 0) {
            extra += Cultivation.tizhiDouble(player, "Xiuliansudu");
        }
        if (Cultivation.tizhiDouble(player, "XiuliansuduPercentage") != 0) {
            extra += Cultivation.tizhiDouble(player, "XiuliansuduPercentage") * Cultivation.get(player, CultivationValue.EXPERIENCE_MAX);
        }
        String days = WHOLE.format(Cultivation.get(player, CultivationValue.CULTIVATION_DAYS));
        player.displayClientMessage(Component.literal("§f距离出关" + days + "天（§b吸纳天地元气" + WHOLE.format(gained)
                + (extra >= 1 ? "点 §6额外修为" + WHOLE.format(extra) + "点§f）" : "点§f）")), true);
        Cultivation.add(player, CultivationValue.EXPERIENCE_STAND, gained + extra);
        Cultivation.add(player, CultivationValue.CULTIVATION_COOLDOWN, 1);
        NeoForge.EVENT_BUS.post(new CultivationExperienceEvent(player, gained));
    }

    /** 群系修正：云海二 +0.2；玉林与桃林 +0.1；云海一与石林 −0.1；云海三与冬林 −0.2。 */
    private static double biomeBonus(Level level, BlockPos pos) {
        ResourceKey<?> key = level.getBiome(pos).unwrapKey().orElseThrow();
        ResourceLocation id = key.location();
        if (!id.getNamespace().equals(ModId.VALUE)) {
            return 0;
        }
        String path = id.getPath();
        if (path.equals("biomecloud_02")) {
            return 0.2;
        }
        if (BIOMES_PLUS_01.contains(path)) {
            return 0.1;
        }
        if (BIOMES_MINUS_01.contains(path)) {
            return -0.1;
        }
        return BIOMES_MINUS_02.contains(path) ? -0.2 : 0;
    }

    /** 效果 {@code effect_025}：系数加（等级 + 1）× 0.2。 */
    private static double effectAmplifierBonus(ServerPlayer player) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "effect_025"))
                .map(player::getEffect).map(instance -> (instance.getAmplifier() + 1) * 0.2).orElse(0.0);
    }

    /** 以玩家所在方块为中心 3 × 3 × 3 范围内有血流。 */
    private static boolean nearBlood(Level level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (BLOOD_FLUIDS.contains(BuiltInRegistries.FLUID.getKey(level.getFluidState(pos).getType()).toString())) {
                return true;
            }
        }
        return false;
    }

    private static void removeCultivationEffects(ServerPlayer player) {
        for (String path : new String[]{"effect_025", "effect_026", "effect_027"}) {
            BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)).ifPresent(player::removeEffect);
        }
    }

    private static void stop(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), false);
        Cultivation.setTizhiString(player, "xiuliankaiguan", "");
        Cultivation.set(player, CultivationValue.CULTIVATION_DAYS, 0);
    }
}
