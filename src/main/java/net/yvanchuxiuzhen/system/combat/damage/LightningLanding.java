package net.yvanchuxiuzhen.system.combat.damage;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Comparator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.yvanchuxiuzhen.system.combat.content.ColoredLightningBolt;

/** 来源 LightningLandingCommand / LightningLandingGo：先生成彩色闪电，再按命令位置的包围盒逐目标调用伤害入口。 */
public final class LightningLanding {
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
        double diameter = number(word(words, 5));
        int color = switch (word(words, 6)) {
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
        String command = "shanghai 雷 " + word(words, 2) + " " + word(words, 7).equals("独立") + " " + word(words, 1) + " " + word(words, 3);
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(diameter / 2)).stream()
                .sorted(Comparator.comparingDouble(target -> target.distanceToSqr(center))).toList()) {
            if (target instanceof Mob || target instanceof Player) {
                level.getServer().getCommands().performPrefixedCommand(target.createCommandSourceStack().withSuppressedOutput().withPermission(4), command);
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
