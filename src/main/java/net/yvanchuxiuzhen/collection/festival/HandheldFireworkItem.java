package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 电光花、呲花和喷花的点燃分栈与物品实例燃放；服务端保存点燃状态和已燃刻数。 */
public final class HandheldFireworkItem extends ContentItem {
    enum Kind {
        ELECTRIC, RANDOM_COLOR, SEQUENCE_COLOR
    }

    private final Kind kind;

    HandheldFireworkItem(Properties properties, List<Component> tooltip, Kind kind) {
        super(properties, tooltip);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!level.isClientSide) {
            ItemStack stack = result.getObject();
            int count = stack.getCount();
            stack.setCount(1);
            HandheldFireworkState current = stack.getOrDefault(FestivalData.FIREWORK_STATE.get(), HandheldFireworkState.UNLIT);
            stack.set(FestivalData.FIREWORK_STATE.get(), new HandheldFireworkState(true, current.ticks()));
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(this, count - 1));
            // 1.18.2 原生右键在创造模式恢复调用栈数量，分出的余量仍保留在背包或掉落物中。
            if (player.isCreative()) stack.setCount(count);
        }
        return result;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        super.inventoryTick(stack, level, entity, slotId, selected);
        if (!(level instanceof ServerLevel serverLevel) || !sourceSelected(entity, slotId, selected)) return;
        HandheldFireworkState state = stack.getOrDefault(FestivalData.FIREWORK_STATE.get(), HandheldFireworkState.UNLIT);
        if (!state.lit()) {
            // 来源 getOrCreateTag 的空标签也参与同种物品的合栈身份比较。
            if (!stack.has(FestivalData.FIREWORK_STATE.get())) {
                stack.set(FestivalData.FIREWORK_STATE.get(), HandheldFireworkState.UNLIT);
            }
            return;
        }
        double tick = state.ticks() + 1;
        if (kind == Kind.ELECTRIC) {
            BlockPos target = level.clip(new ClipContext(entity.getEyePosition(1),
                    entity.getEyePosition(1).add(entity.getViewVector(1).scale(5)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
            serverLevel.sendParticles(Particles.type("cailiao_229particle"), target.getX(), target.getY(), target.getZ(),
                    5, 0, 0, 0, 0.5);
        } else {
            for (int index = 0; index < 5; index++) directedParticle(serverLevel, entity, "pao_0201");
        }
        boolean expired = tick >= (kind == Kind.SEQUENCE_COLOR ? 1200 : 600);
        stack.set(FestivalData.FIREWORK_STATE.get(), new HandheldFireworkState(true, expired ? 0 : tick));
        if (expired) stack.shrink(1);
        if (kind == Kind.RANDOM_COLOR) {
            directedParticle(serverLevel, entity, "pao_030" + randomColor());
        } else if (kind == Kind.SEQUENCE_COLOR && !expired) {
            directedParticle(serverLevel, entity, "pao_030" + sequenceColor(tick));
        }
    }

    private static boolean sourceSelected(Entity entity, int slotId, boolean selected) {
        // 1.18.2 背包逐分区使用局部槽号；护甲区从 0 起，副手的局部槽号也为 0。
        int compartmentStart = slotId >= Inventory.SLOT_OFFHAND ? Inventory.SLOT_OFFHAND : Inventory.INVENTORY_SIZE;
        return selected || slotId >= Inventory.INVENTORY_SIZE && entity instanceof Player player
                && slotId - compartmentStart == player.getInventory().selected;
    }

    /** 来源各分支各自抽样，不能改为用一次随机值划分六个等分区间。 */
    private static int randomColor() {
        if (Math.random() < 0.166) return 6;
        if (Math.random() < 0.2) return 5;
        if (Math.random() < 0.25) return 4;
        if (Math.random() < 0.333) return 3;
        if (Math.random() < 0.5) return 2;
        return 1;
    }

    private static int sequenceColor(double tick) {
        if (tick >= 1000) return 6;
        if (tick >= 800) return 5;
        if (tick >= 600) return 4;
        if (tick >= 400) return 3;
        if (tick >= 200) return 2;
        return 1;
    }

    /** 来源 particle 命令的 count=0 把三个偏移量作为速度，force 对本维度 512 格内玩家广播。 */
    private static void directedParticle(ServerLevel level, Entity entity, String id) {
        Vec3 look = entity.getLookAngle();
        double x = look.x * new Random().nextDouble(0.5, 1.5);
        double y = look.y * new Random().nextDouble(0.5, 1.5);
        double z = look.z * new Random().nextDouble(0.5, 1.5);
        SimpleParticleType particle = Particles.type(id);
        for (ServerPlayer viewer : level.players()) {
            level.sendParticles(viewer, particle, true, entity.getX(), entity.getY() + 1, entity.getZ(),
                    0, x, y, z, 1);
        }
    }
}
