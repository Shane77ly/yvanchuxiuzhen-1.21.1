package net.yvanchuxiuzhen.system.xiulian;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MeditationRefineEvent;
import net.yvanchuxiuzhen.system.xiulian.api.RetreatTechniqueEvent;
import net.yvanchuxiuzhen.system.xiulian.content.RetreatMenu;

/** 来源修炼、观想和参悟三页的服务端操作，日数、寿元与资源均交回修炼档案。 */
public final class RetreatActions {
    private static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private RetreatActions() {
    }

    public static void open(ServerPlayer player, BlockPos pos, int mode) {
        String title = mode == 7 ? "观想" : mode == 8 ? "参悟功法" : "修炼";
        player.openMenu(new SeamlessMenuProvider((id, inventory, opener) -> new RetreatMenu(id, inventory, pos, mode), Component.literal(title)));
    }

    /** 六个按钮保留来源各自边界，减一天在一天时保持不变，加十/百天可能设置为寿元减一。 */
    public static void adjustDays(ServerPlayer player, int button) {
        double days = Cultivation.tizhiDouble(player, "xiuliandaynum");
        double lifetime = Cultivation.get(player, CultivationValue.LIFETIME);
        switch (button) {
            case 1 -> {
                if (lifetime - days > 1) Cultivation.setTizhiDouble(player, "xiuliandaynum", days + 1);
            }
            case 2, 3 -> {
                int amount = button == 2 ? 10 : 100;
                Cultivation.setTizhiDouble(player, "xiuliandaynum", lifetime - days > amount ? days + amount : lifetime - 1);
            }
            case 4 -> {
                if (days > 1) Cultivation.setTizhiDouble(player, "xiuliandaynum", days - 1);
            }
            case 5, 6 -> {
                int amount = button == 5 ? 10 : 100;
                Cultivation.setTizhiDouble(player, "xiuliandaynum", days > amount ? days - amount : 0);
            }
            default -> throw new IllegalArgumentException("不是修炼天数按钮：" + button);
        }
    }

    /** 修炼有冷却门槛，参悟没有；两者保留骑乘、天数和当前闭关状态的原判定顺序。 */
    public static void start(ServerPlayer player, boolean study) {
        double days = Cultivation.tizhiDouble(player, "xiuliandaynum");
        double cooldown = Cultivation.get(player, CultivationValue.CULTIVATION_COOLDOWN);
        boolean idle = Cultivation.get(player, CultivationValue.CULTIVATION_DAYS) <= 0;
        if (player.isPassenger() && idle && days >= 1 && (study || cooldown < 100)) {
            RetreatTechniqueEvent technique = new RetreatTechniqueEvent(player);
            NeoForge.EVENT_BUS.post(technique);
            switch (technique.access()) {
                case OWNED -> {
                    message(player, study ? "§6开始参悟功法！" : "§6开始修炼！");
                    Cultivation.set(player, CultivationValue.CULTIVATION_DAYS, days);
                    Cultivation.setTizhiDouble(player, "xiuliandaynum", 0);
                    Cultivation.setTizhiString(player, "xiuliankaiguan", study ? "功法" : "修炼");
                    player.closeContainer();
                }
                case NONE -> message(player, study ? "§4没有装备任何功法，无法参悟！" : "§4没有装备任何功法，无法修炼！");
                case UNLEARNED -> message(player, study ? "§4此功法还未被修习，无法参悟！" : "§4此功法还未被修习，无法修炼！");
                case OTHER -> message(player, "§4此功法修习者另有其人。");
            }
        } else if (!player.isPassenger()) {
            message(player, study ? "§4只有坐下后才可参悟功法。" : "§4只有坐下后才可修炼。");
        } else if (days < 1) {
            message(player, "§4需要设定修炼时间。");
        } else if (!study && cooldown >= 100) {
            message(player, "§4你感到心湖还是无法平静，再等一等吧。（" + cooldown * 15 + "秒）");
        } else {
            message(player, study ? "§4已经开始参悟功法了。" : "§4已经开始修炼了。");
        }
    }

    /** 来源按距离扫描，遇到坐具便结束扫描；只在安全且寿元大于三时结算观想。 */
    public static void meditate(ServerPlayer player) {
        Vec3 center = player.position();
        List<Entity> nearby = player.serverLevel().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(8), entity -> true)
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
        boolean blocked = false;
        for (Entity entity : nearby) {
            if (entity instanceof Player || !(entity instanceof Mob)) continue;
            if (entity.getType().is(YAOSHOU) && !(entity instanceof TamableAnimal tame && tame.getOwner() == player)) {
                blocked = true;
                break;
            }
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (id.equals(ModId.VALUE + ":shitiputuan") || id.equals(ModId.VALUE + ":shitiyizi")) break;
            if (!(entity instanceof AmbientCreature) && !(entity instanceof Animal)) {
                blocked = true;
                break;
            }
        }
        if (blocked) {
            message(player, "§4周围有怪物在游荡，无法观想。");
            return;
        }
        if (Cultivation.get(player, CultivationValue.LIFETIME) > 3) {
            for (int index = 0; index < 3; index++) {
                double vigorous = Cultivation.get(player, CultivationValue.VIGOROUS);
                if (vigorous >= 1) Cultivation.add(player, CultivationValue.VIGOROUS, -1);
                else if (vigorous <= -1) Cultivation.add(player, CultivationValue.VIGOROUS, 1);
                else break;
            }
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 20, false, false));
            NeoForge.EVENT_BUS.post(new MeditationRefineEvent(player));
            message(player, "观想三天，恢复所有灵力与精力。");
            Cultivation.add(player, CultivationValue.LIFETIME, -3);
            Cultivation.set(player, CultivationValue.POWER, PlayerAttributes.value(player, Stat.POWER_MAX));
            Cultivation.set(player, CultivationValue.SOUL, PlayerAttributes.value(player, Stat.SOUL_MAX));
            player.setHealth(player.getHealth() + 30);
        } else {
            message(player, "§4您的大限将至");
        }
        player.closeContainer();
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }
}
