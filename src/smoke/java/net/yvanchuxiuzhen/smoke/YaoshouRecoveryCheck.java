package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.FightingState;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 妖兽标签恢复、战斗倍率、重伤和实体持久化的定向检查。 */
final class YaoshouRecoveryCheck extends SystemCheck {
    private boolean executed;

    YaoshouRecoveryCheck(List<String> report, String levelId) {
        super("妖兽共享恢复", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (!executed) {
            executed = true;
            onServer(this::check);
        }
        finish();
        return true;
    }

    private void check(ServerPlayer player) {
        LivingEntity beast = sample(player, "yaoshou_004");
        seed(beast);
        CreatureData.state(beast).setRecoveryTicks(38);
        tick(beast);
        CompoundTag saved = beast.saveWithoutId(new CompoundTag());
        LivingEntity restored = (LivingEntity) beast.getType().create(player.serverLevel());
        restored.load(saved);
        expectTrue("39刻计数随原生实体数据保存重读", CreatureData.state(restored).recoveryTicks() == 39);
        tick(restored);
        expectTrue("第40刻按完整倍率恢复并限制体力与神识上限",
                restored.getHealth() == 14 && EntityAttributes.power(restored) == 10
                        && Cultivation.entitySoul(restored) == 10
                        && CreatureData.state(restored).recoveryTicks() == 0);

        LivingEntity peach = sample(player, "taomujing");
        seed(peach);
        FightingState.mark(peach);
        StatusEffects.addHidden(peach, "effect_007", 200, 0);
        CreatureData.state(peach).setRecoveryTicks(39);
        tick(peach);
        expectTrue("桃木精重伤禁止生命恢复但体力神识按战斗倍率恢复",
                peach.getHealth() == 10 && EntityAttributes.power(peach) == 8.5
                        && Cultivation.entitySoul(peach) == 8.5);
        StatusEffects.remove(peach, "effect_007");
        CreatureData.state(peach).setRecoveryTicks(39);
        tick(peach);
        expectTrue("战斗中生命按四分之一倍率恢复", peach.getHealth() == 11);

        LivingEntity insect = sample(player, "guchong_01");
        seed(insect);
        CreatureData.state(insect).setRecoveryTicks(39);
        tick(insect);
        expectTrue("仅具蛊虫标签的实体不进入妖兽恢复",
                CreatureData.state(insect).recoveryTicks() == 39 && insect.getHealth() == 10);

        CreatureData.state(beast).setRecoveryTicks(Double.NaN);
        float healthBeforeNanTick = beast.getHealth();
        tick(beast);
        expectTrue("非数字计数不满足来源大于等于40的恢复条件",
                Double.isNaN(CreatureData.state(beast).recoveryTicks()) && beast.getHealth() == healthBeforeNanTick);

        LivingEntity pig = EntityType.PIG.create(player.serverLevel());
        seed(pig);
        CreatureData.state(pig).setRecoveryTicks(39);
        tick(pig);
        expectTrue("无妖兽标签的实体不进入共享恢复",
                CreatureData.state(pig).recoveryTicks() == 39 && pig.getHealth() == 10);
    }

    private static LivingEntity sample(ServerPlayer player, String id) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id));
        return (LivingEntity) type.create(player.serverLevel());
    }

    private static void seed(LivingEntity entity) {
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
        entity.setHealth(10);
        EntityAttributes.update(entity, stats -> stats
                .withStat(Stat.HEALTH_REC, 4)
                .withStat(Stat.POWER_MAX, 10)
                .withStat(Stat.POWER_REC, 2)
                .withStat(Stat.SOUL_MAX, 10)
                .withStat(Stat.SOUL_REC, 2));
        EntityAttributes.setPower(entity, 8);
        Cultivation.setEntitySoul(entity, 8);
    }

    private static void tick(LivingEntity entity) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Pre(entity));
    }
}
