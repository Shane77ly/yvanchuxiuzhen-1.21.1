package net.yvanchuxiuzhen.system.combat.effect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;

/**
 * 控制与削弱类状态效果：眩晕、沉默、剧痛、无力、发抖、重伤。开始时在身体中部播放同名音效（{@code effect_00Nyinxiao}）。
 * 转动视角的效果在服务端与本人客户端的实体上都执行，与来源的过程一致。
 */
final class ControlEffects {
    private ControlEffects() {
    }

    static void register(ContentTables tables) {
        tables.effectBehavior("effect_001", EffectSupport.withStartSound("effect_001yinxiao", new Spin()));
        tables.effectBehavior("effect_002", EffectSupport.withStartSound("effect_002yinxiao", new EffectBehavior() {
        }));
        tables.effectBehavior("effect_003", EffectSupport.withStartSound("effect_003yinxiao", new Pain()));
        tables.effectBehavior("effect_005", EffectSupport.withStartSound("effect_005yinxiao", new Disarm()));
        tables.effectBehavior("effect_006", EffectSupport.withStartSound("effect_006yinxiao", new Tremble()));
        tables.effectBehavior("effect_007", EffectSupport.withStartSound("effect_007yinxiao", new Wound()));
    }

    /** 设置朝向并同步身体、头部与上一刻的朝向，使转动不被插值抹平。 */
    private static void turn(LivingEntity entity, float yaw, float pitch) {
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.setYBodyRot(yaw);
        entity.setYHeadRot(yaw);
        entity.yRotO = yaw;
        entity.xRotO = pitch;
        entity.yBodyRotO = yaw;
        entity.yHeadRotO = yaw;
    }

    /** 眩晕：每刻水平转 5 × (等级 + 1) 度，俯仰随机 ±10 度，生物停止寻路。 */
    private static final class Spin implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            clientTick(entity, amplifier);
            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
            }
            return true;
        }

        @Override
        public void clientTick(LivingEntity entity, int amplifier) {
            turn(entity, entity.getYRot() + 5 * (amplifier + 1), entity.getXRot() + Mth.nextInt(entity.getRandom(), -10, 10));
        }
    }

    /** 剧痛：计数到 80–89 刻时每刻抖动视角，抖动幅度随等级增大；计数到 90 清零。 */
    private static final class Pain implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            clientTick(entity, amplifier);
            return true;
        }

        @Override
        public void clientTick(LivingEntity entity, int amplifier) {
            double count = entity.getPersistentData().getDouble("effect003tick") + 1;
            if (count >= 90) {
                count = 0;
            } else if (count >= 80) {
                int reach = (amplifier + 1) * 5;
                turn(entity, entity.getYRot() + Mth.nextInt(entity.getRandom(), reach - (amplifier + 1) * 10, reach), entity.getXRot() + 1);
            }
            entity.getPersistentData().putDouble("effect003tick", count);
        }
    }

    /** 无力：主手物品掉落（拾取延迟 10 × (等级 + 1) 刻），桃木精与第四个 Boss 不受影响。 */
    private static final class Disarm implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if ((entity instanceof Mob || entity instanceof Player) && !immune(entity)) {
                ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 1, entity.getZ(), entity.getMainHandItem());
                drop.setPickUpDelay(10 * (amplifier + 1));
                entity.level().addFreshEntity(drop);
                entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                if (entity instanceof Player player) {
                    player.getInventory().setChanged();
                }
            }
            return true;
        }

        private static boolean immune(LivingEntity entity) {
            String path = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
            return path.equals("ycxzboss_04") || path.equals("taomujing");
        }
    }

    /** 发抖：每刻水平随机抖动 ±3 × (等级 + 1) 度，俯仰加 1 度。 */
    private static final class Tremble implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            clientTick(entity, amplifier);
            return true;
        }

        @Override
        public void clientTick(LivingEntity entity, int amplifier) {
            int reach = (amplifier + 1) * 3;
            turn(entity, entity.getYRot() + Mth.nextInt(entity.getRandom(), reach - (amplifier + 1) * 6, reach), entity.getXRot() + 1);
        }
    }

    /** 重伤：当前生命压到 最大生命 − 4 × (等级 + 1)（至少 1）。恢复生命的过程读取该效果来限制恢复。 */
    private static final class Wound implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            double target = Math.max(entity.getMaxHealth() - (amplifier + 1) * 4, 1);
            if (entity.getHealth() >= target) {
                entity.setHealth((float) target);
            }
            return true;
        }
    }
}
