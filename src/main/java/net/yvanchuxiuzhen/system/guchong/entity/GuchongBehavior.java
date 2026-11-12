package net.yvanchuxiuzhen.system.guchong.entity;

import java.text.DecimalFormat;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarRuntime;

/** 蛊虫 UUID 目标指令、地上回罐和死亡清槽的来源生命周期。 */
public final class GuchongBehavior {
    private static final TagKey<Item> JARS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taoguan"));

    private GuchongBehavior() { }

    public static void tick(GuchongEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.putDouble("guchongtick", data.getDouble("guchongtick") + 1);
        if (data.getDouble("guchongtick") < 20) return;
        String targetId = data.getString("gongjimubiao");
        if (!targetId.isEmpty()) {
            if (entity.level() instanceof ServerLevel level && findEntity(level, targetId) instanceof LivingEntity living) entity.setTarget(living);
            data.putString("gongjimubiao", "");
        }
        if (data.getBoolean("kanjia")) {
            boolean first = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals("guchong_01");
            boolean returning = entity.getTarget() == null && !first;
            if (first) {
                data.putDouble("guchongtick2", data.getDouble("guchongtick2") + 1);
                if (data.getDouble("guchongtick2") >= 5) {
                    returning = true;
                    data.putDouble("guchongtick2", 0);
                }
            }
            if (returning && entity.level() instanceof ServerLevel level) {
                updateHomeCount(level, entity, false);
                entity.discard();
            }
        }
        data.putDouble("guchongtick", 0);
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
        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean("guchongxvnfu")) return;
        if (data.getBoolean("kanjia")) {
            updateHomeCount(level, entity, true);
            return;
        }
        for (Player player : level.players()) {
            if (!data.getString("belongToMasterUUID").equals(player.getStringUUID())) continue;
            IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
            if (inventory == null) continue;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack jar = inventory.getStackInSlot(slot).copy();
                CompoundTag item = GuchongJarData.read(jar);
                if (!jar.is(JARS) || !item.getString("guguanuuid").equals(data.getString("guguanuuid"))) continue;
                String suffix = new DecimalFormat("0").format(data.getDouble("guguanweizhi"));
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

    private static void updateHomeCount(ServerLevel level, GuchongEntity entity, boolean clearSlot) {
        CompoundTag data = entity.getPersistentData();
        BlockPos home = BlockPos.containing(data.getDouble("homeblockX"), data.getDouble("homeblockY"), data.getDouble("homeblockZ"));
        BlockEntity jar = level.getBlockEntity(home);
        if (jar == null) return;
        decrement(jar.getPersistentData());
        if (clearSlot) {
            IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, home, null);
            if (inventory instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot((int) data.getDouble("guguanweizhi"), ItemStack.EMPTY);
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
