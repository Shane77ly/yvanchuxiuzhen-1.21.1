package net.yvanchuxiuzhen.system.skill;

import java.text.DecimalFormat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 技能界面的自爆、神识扫描和召唤丹火。境界、资源、寿元及物品产出全部由服务端判断并写入。 */
public final class SkillMenuActions {
    private SkillMenuActions() {
    }

    public static void summonFire(ServerPlayer player) {
        double level = Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        double cost = level * 2;
        if (realm(player) < 11) {
            message(player, "§4只有筑基期修士才可召唤丹火。丹火用于炼丹炼器，但对于作战帮助不大。");
        } else if (Cultivation.get(player, CultivationValue.POWER) < cost) {
            message(player, "§4灵力不足" + whole(cost) + "点，无法召唤丹火。丹火用于炼丹炼器，但对于作战帮助不大。");
        } else {
            player.serverLevel().sendParticles(ParticleTypes.FLAME, player.getX(), middleY(player), player.getZ(), 10, .2, 0, .2, 0);
            ItemStack fire = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "danhuo")));
            CompoundTag data = new CompoundTag();
            data.putDouble("power", cost);
            data.putString("ManaAttackUUID", player.getStringUUID());
            fire.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            Cultivation.add(player, CultivationValue.POWER, -cost);
            ItemHandlerHelper.giveItemToPlayer(player, fire);
            player.closeContainer();
        }
    }

    /** 来源在服务端刻回调中调用 Level.addParticle，该方法不广播粒子；扫描的有效结果只有扣费与关闭菜单。 */
    public static void scan(ServerPlayer player) {
        double cost = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 2;
        if (realm(player) < 11) {
            message(player, "§4只有筑基以上修为，才可使用神识扫描！");
        } else if (Cultivation.get(player, CultivationValue.SOUL) < cost) {
            message(player, "§4精力不足" + cost + "点，无法放出神识扫描。");
        } else {
            Cultivation.add(player, CultivationValue.SOUL, -cost);
            player.closeContainer();
        }
    }

    public static void explode(ServerPlayer player) {
        double loss = Cultivation.tizhiDouble(player, "zibaonumber") <= 0 ? 3650 : Cultivation.tizhiDouble(player, "zibaokouchu") * 3;
        if (realm(player) < 21) {
            message(player, "§4此为自爆！当您等级达到结丹期以后才可使用，造成大范围高额法术伤害，自身也会死亡，并且会额外扣除" + whole(loss / 365) + "年寿元！");
            return;
        }
        double cost = PlayerAttributes.value(player, Stat.POWER_MAX) * .1;
        if (Cultivation.get(player, CultivationValue.POWER) < cost) {
            message(player, "§4自爆需要您至少拥有10%的灵力！");
            return;
        }
        ServerLevel level = player.serverLevel();
        Cultivation.add(player, CultivationValue.POWER, -cost);
        level.playSound(null, player.getX(), middleY(player), player.getZ(),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "jinengzibao")), SoundSource.NEUTRAL, 1, 1);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 20));
        // 回调捕获原玩家与世界，保留死亡、离线或重生后仍对原对象现读坐标和境界的来源顺序。
        SkillDelays.after(30, () -> {
            level.sendParticles(ParticleTypes.FLASH, player.getX(), middleY(player), player.getZ(), 1, 0, 0, 0, 0);
            level.explode(null, player.getX(), player.getY(), player.getZ(), 16, Level.ExplosionInteraction.TNT);
            blast(level, player);
        });
        for (int delay : new int[]{35, 40, 45}) {
            SkillDelays.after(delay, () -> blast(level, player));
        }
        player.closeContainer();
        double count = Cultivation.tizhiDouble(player, "zibaonumber") + 1;
        Cultivation.setTizhiDouble(player, "zibaonumber", count);
        loss = count <= 1 ? 3650 : Cultivation.tizhiDouble(player, "zibaokouchu") * 3;
        Cultivation.setTizhiDouble(player, "zibaokouchu", loss);
        Cultivation.add(player, CultivationValue.LIFETIME, -loss);
        message(player, "§4寿元额外减少" + whole(loss / 365) + "年");
        player.kill();
    }

    private static void blast(ServerLevel level, ServerPlayer player) {
        Vec3 center = new Vec3(player.getX(), middleY(player), player.getZ());
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 60, 0, 0, 0, 1);
        double damage = (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) - 20) * 10 + 10;
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(16))) {
            if (!Relations.isFriendly(player, target) && target instanceof LivingEntity living) {
                SpellDamage.dealRounded(level, living, DamageCategory.FIRE, player.getUUID(), false, damage, 2);
            }
        }
        // 来源圆环直接把角度数值作为弧度，命令 count=0 产生定向速度。
        for (int angle = 0; angle < 360; angle += 6) {
            level.sendParticles(ParticleTypes.FLAME, center.x + 2 * Math.cos(angle), center.y, center.z + 2 * Math.sin(angle),
                    0, .5 * Math.cos(angle), 0, .5 * Math.sin(angle), .5);
        }
    }

    private static double realm(ServerPlayer player) {
        return Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + Cultivation.tizhiDouble(player, "LevelNumberCorrect");
    }

    private static double middleY(ServerPlayer player) {
        return player.getY() + player.getBbHeight() / 2;
    }

    private static String whole(double value) {
        return new DecimalFormat("0").format(value);
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }
}
