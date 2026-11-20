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
import net.yvanchuxiuzhen.system.combat.api.FightingState;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;

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
        beast.getPersistentData().putDouble("entityhuifutick", 38);
        tick(beast);
        CompoundTag saved = beast.saveWithoutId(new CompoundTag());
        LivingEntity restored = (LivingEntity) beast.getType().create(player.serverLevel());
        restored.load(saved);
        expectTrue("39刻计数随原生实体数据保存重读", restored.getPersistentData().getDouble("entityhuifutick") == 39);
        tick(restored);
        expectTrue("第40刻按完整倍率恢复并限制体力与神识上限",
                restored.getHealth() == 14 && restored.getPersistentData().getDouble("power") == 10
                        && restored.getPersistentData().getDouble("soul") == 10
                        && restored.getPersistentData().getDouble("entityhuifutick") == 0);

        LivingEntity peach = sample(player, "taomujing");
        seed(peach);
        FightingState.mark(peach);
        StatusEffects.addHidden(peach, "effect_007", 200, 0);
        peach.getPersistentData().putDouble("entityhuifutick", 39);
        tick(peach);
        expectTrue("桃木精重伤禁止生命恢复但体力神识按战斗倍率恢复",
                peach.getHealth() == 10 && peach.getPersistentData().getDouble("power") == 8.5
                        && peach.getPersistentData().getDouble("soul") == 8.5);
        StatusEffects.remove(peach, "effect_007");
        peach.getPersistentData().putDouble("entityhuifutick", 39);
        tick(peach);
        expectTrue("战斗中生命按四分之一倍率恢复", peach.getHealth() == 11);

        LivingEntity insect = sample(player, "guchong_01");
        seed(insect);
        insect.getPersistentData().putDouble("entityhuifutick", 39);
        tick(insect);
        expectTrue("仅具蛊虫标签的实体不进入妖兽恢复",
                insect.getPersistentData().getDouble("entityhuifutick") == 39 && insect.getHealth() == 10);

        beast.getPersistentData().putDouble("entityhuifutick", Double.NaN);
        float healthBeforeNanTick = beast.getHealth();
        tick(beast);
        expectTrue("非数字计数不满足来源大于等于40的恢复条件",
                Double.isNaN(beast.getPersistentData().getDouble("entityhuifutick")) && beast.getHealth() == healthBeforeNanTick);

        LivingEntity pig = EntityType.PIG.create(player.serverLevel());
        seed(pig);
        pig.getPersistentData().putDouble("entityhuifutick", 39);
        tick(pig);
        expectTrue("无妖兽标签的实体不进入共享恢复",
                pig.getPersistentData().getDouble("entityhuifutick") == 39 && pig.getHealth() == 10);
    }

    private static LivingEntity sample(ServerPlayer player, String id) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id));
        return (LivingEntity) type.create(player.serverLevel());
    }

    private static void seed(LivingEntity entity) {
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
        entity.setHealth(10);
        CompoundTag data = entity.getPersistentData();
        data.putDouble("health_Rec", 4);
        data.putDouble("power", 8);
        data.putDouble("power_Max", 10);
        data.putDouble("power_Rec", 2);
        data.putDouble("soul", 8);
        data.putDouble("soul_Max", 10);
        data.putDouble("soul_Rec", 2);
    }

    private static void tick(LivingEntity entity) {
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Pre(entity));
    }
}
