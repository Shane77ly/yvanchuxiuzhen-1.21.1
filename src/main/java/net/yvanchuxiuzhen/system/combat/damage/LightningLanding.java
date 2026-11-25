package net.yvanchuxiuzhen.system.combat.damage;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.content.ColoredLightningBolt;

/** 来源 LightningLandingCommand / LightningLandingGo：先生成彩色闪电，再按命令位置的包围盒逐目标调用伤害入口。 */
public final class LightningLanding {
    private static final Pattern FIELD_SEPARATOR = Pattern.compile("\\s+");
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ModId.VALUE);
    public static final DeferredHolder<EntityType<?>, EntityType<ColoredLightningBolt>> TYPE = ENTITIES.register("colorful_lightning_bolt",
            () -> EntityType.Builder.<ColoredLightningBolt>of(ColoredLightningBolt::new, MobCategory.MISC).noSave().sized(0, 0)
                    .clientTrackingRange(16).updateInterval(Integer.MAX_VALUE).build(ModId.VALUE + ":colorful_lightning_bolt"));

    private LightningLanding() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        NeoForge.EVENT_BUS.addListener(LightningLanding::registerCommand);
    }

    private static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("lightninglanding")
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(LightningLanding::run))
                .executes(LightningLanding::run));
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        execute(context.getSource().getLevel(), context.getSource().getPosition(), context.getInput());
        return 0;
    }

    /** 完整命令文字按空白分隔；队伍参数只占位，缺失时后续参数前移，与来源一致。 */
    public static void execute(ServerLevel level, Vec3 center, String input) {
        String[] words = input.trim().split("\\s+");
        String attacker = word(words, 2);
        boolean fixed = word(words, 7).equals("独立");
        // 缺失攻击者时，内层伤害命令的 false 前移为无来源，基础量位置为空。
        Shanghai.Request damage = attacker.isEmpty() ? new Shanghai.Request(DamageCategory.THUNDER, null, false, 0, 0)
                : ShanghaiCommand.request(DamageCategory.THUNDER, attacker, fixed, number(word(words, 1)), number(word(words, 3)));
        strike(level, center, number(word(words, 5)), word(words, 6), damage);
    }

    /** 实体施法者直接保留 UUID；只有队名词数异常时才需要重现字段错位。 */
    public static void cast(ServerLevel level, Vec3 center, Entity caster, double base, double multiplier, String team, int diameter, String color, boolean fixed) {
        if (singleWord(team)) {
            strike(level, center, diameter, color, new Shanghai.Request(DamageCategory.THUNDER, caster.getUUID(), fixed, base, multiplier));
        } else {
            castSaved(level, center, caster.getStringUUID(), base, multiplier, team, diameter, color, fixed);
        }
    }

    /** 存档来源可能是无来源标志、未加载或非法 UUID；无效伤害不取消闪电。 */
    public static void castSaved(ServerLevel level, Vec3 center, String owner, double base, double multiplier, String team, int diameter, String color, boolean fixed) {
        if (singleWord(owner) && singleWord(team)) {
            strike(level, center, diameter, color, ShanghaiCommand.request(DamageCategory.THUNDER, owner, fixed, base, multiplier));
            return;
        }
        // 空字段不占词位，多词字段跨越后续槽位。只对这类存档重排字段，不再分发命令。
        String[] fields = Stream.of(owner, Double.toString(multiplier), team, Integer.toString(diameter), color, fixed ? "独立" : "")
                .flatMap(FIELD_SEPARATOR::splitAsStream).filter(field -> !field.isEmpty()).toArray(String[]::new);
        Shanghai.Request damage = ShanghaiCommand.request(DamageCategory.THUNDER, word(fields, 0), word(fields, 5).equals("独立"), base, number(word(fields, 1)));
        strike(level, center, number(word(fields, 3)), word(fields, 4), damage);
    }

    public static void tribulation(ServerLevel level, Vec3 center, double base, double multiplier, int diameter, String color) {
        strike(level, center, diameter, color, new Shanghai.Request(DamageCategory.THUNDER, null, false, base, multiplier));
    }

    private static boolean singleWord(String value) {
        return !value.isEmpty() && value.chars().noneMatch(c -> c == ' ' || c >= '\t' && c <= '\r');
    }

    private static void strike(ServerLevel level, Vec3 center, double diameter, String colorName, Shanghai.Request damage) {
        int color = switch (colorName) {
            case "灰色" -> 0xff999999;
            case "黄色" -> 0xffffff99;
            case "金色" -> 0xffffcc66;
            case "红色" -> 0xffff0000;
            case "血色" -> 0xff990000;
            case "绿色" -> 0xff99ff99;
            case "天蓝" -> 0xff99ffff;
            case "海蓝" -> 0xff3333ff;
            case "紫色" -> 0xffcc33cc;
            case "黑色" -> 0xff000000;
            default -> 0xffffffff;
        };
        ColoredLightningBolt bolt = TYPE.get().create(level);
        bolt.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(center)));
        bolt.setDamage(0.1F);
        bolt.setColor(color);
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
        if (damage == null) return;
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(diameter / 2)).stream()
                .sorted(Comparator.comparingDouble(target -> target.distanceToSqr(center))).toList()) {
            if ((target instanceof Mob || target instanceof Player) && target instanceof LivingEntity living) {
                Shanghai.deal(level, living, damage);
            }
        }
    }

    private static String word(String[] words, int index) {
        return index < words.length ? words[index] : "";
    }

    private static double number(String word) {
        try {
            return Double.parseDouble(word.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
