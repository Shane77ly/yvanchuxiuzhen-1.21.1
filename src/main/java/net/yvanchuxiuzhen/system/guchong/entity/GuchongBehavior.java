package net.yvanchuxiuzhen.system.guchong.entity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.api.GuchongRuntimeState;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarRuntime;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanBlockEntity;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;

/** 蛊虫 UUID 目标指令、地上回罐和死亡清槽的来源生命周期。 */
public final class GuchongBehavior {
    private static final TagKey<Item> JARS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taoguan"));

    private GuchongBehavior() { }

    public static void tick(GuchongEntity entity) {
        entity.setGuchongTick(entity.guchongTick() + 1);
        if (entity.guchongTick() < 20) return;
        GuchongRuntimeState state = entity.getData(GuchongData.RUNTIME);
        String targetId = state.commandTargetUuid();
        if (!targetId.isEmpty()) {
            if (entity.level() instanceof ServerLevel level && findEntity(level, targetId) instanceof LivingEntity living) entity.setTarget(living);
            entity.setData(GuchongData.RUNTIME, state.withCommandTargetUuid(""));
        }
        if (state.guarding()) {
            boolean first = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals("guchong_01");
            boolean returning = entity.getTarget() == null && !first;
            if (first) {
                entity.setGuchongTick2(entity.guchongTick2() + 1);
                if (entity.guchongTick2() >= 5) {
                    returning = true;
                    entity.setGuchongTick2(0);
                }
            }
            if (returning && entity.level() instanceof ServerLevel level) {
                updateHomeCount(level, entity, state, false);
                entity.discard();
            }
        }
        entity.setGuchongTick(0);
    }

    public static Entity findEntity(ServerLevel level, String uuid) {
        try {
            return level.getEntity(UUID.fromString(uuid));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static void died(GuchongEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        GuchongRuntimeState state = entity.getData(GuchongData.RUNTIME);
        if (!state.tamed()) return;
        if (state.guarding()) {
            updateHomeCount(level, entity, state, true);
            return;
        }
        for (Player player : level.players()) {
            if (!state.ownerUuid().equals(player.getStringUUID())) continue;
            IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
            if (inventory == null) continue;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack jar = inventory.getStackInSlot(slot).copy();
                CompoundTag item = GuchongJarData.read(jar);
                if (!jar.is(JARS) || !item.getString("guguanuuid").equals(state.jarUuid())) continue;
                String suffix = Integer.toString(state.jarSlot());
                GuchongJarData.clear(item, suffix);
                item.putString("guguanuuid" + suffix, "");
                decrement(item);
                jar.setCount(1);
                GuchongJarData.write(jar, item);
                if (inventory instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot(slot, jar);
                return;
            }
        }
    }

    private static void updateHomeCount(ServerLevel level, GuchongEntity entity, GuchongRuntimeState state, boolean clearSlot) {
        BlockPos home = state.homePos();
        if (!(level.getBlockEntity(home) instanceof TaoguanBlockEntity jar)) return;
        decrement(jar.jarData());
        if (clearSlot) {
            IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, home, null);
            if (inventory instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot(state.jarSlot(), ItemStack.EMPTY);
        }
        jar.setChanged();
        level.sendBlockUpdated(home, jar.getBlockState(), jar.getBlockState(), 3);
    }

    private static void decrement(CompoundTag data) {
        data.putDouble("shifangnum", data.getDouble("shifangnum") - 1);
        if (data.getDouble("shifangnum") < 1) {
            data.putDouble("shifangnum", 0);
            data.putBoolean("shifangguchong", false);
        }
    }

    public static void interactJar(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) GuchongJarRuntime.interact(server, pos, player);
    }

    public static void releaseFromJar(ServerLevel level, BlockPos pos) {
        GuchongJarRuntime.release(level, pos);
    }
}
