package net.yvanchuxiuzhen.system.guchong.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong01Entity;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong03Entity;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong04Entity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 蛊笛按玩家物品能力中的真实槽位操作蛊罐，界面中的罐只是显示副本。 */
public final class GuchongFluteRuntime {
    public static final TagKey<Item> JARS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taoguan"));
    private static final TagKey<EntityType<?>> INSECT_ENTITIES = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchongentity"));

    private GuchongFluteRuntime() {
    }

    /** 潜行收回本人名下的标签蛊虫，每只虫立即写回第一个匹配 UUID 的真实蛊罐。 */
    public static void recall(ServerLevel level, Player player) {
        for (Entity insect : nearby(level, player)) {
            if (!insect.getType().is(INSECT_ENTITIES)
                    || !insect.getPersistentData().getString("belongToMasterUUID").equals(player.getStringUUID())) continue;
            IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY, null);
            if (inventory == null) continue;
            for (int index = 0; index < inventory.getSlots(); index++) {
                ItemStack jar = inventory.getStackInSlot(index).copy();
                if (!jar.is(JARS)) continue;
                CompoundTag data = GuchongJarData.read(jar);
                if (!insect.getPersistentData().getString("guguanuuid").equals(data.getString("guguanuuid"))) continue;
                GuchongJarData.capture(insect, data, Relations.team(player));
                if (data.getDouble("shifangnum") < 1) data.putBoolean("shifangguchong", false);
                insect.discard();
                writeJar(inventory, index, jar, data);
                break;
            }
        }
    }

    /** 按展示罐 UUID 寻找真实罐；释放只检查编号槽主人，收回按来源只检查虫的罐 UUID。 */
    public static void activate(ServerLevel level, Player player, GuchongReleaseMenu menu, int selected) {
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (inventory == null) return;
        String uuid = GuchongJarData.read(menu.getSlot(selected).getItem()).getString("guguanuuid");
        double count = 0;
        for (int index = 0; index < inventory.getSlots(); index++) {
            ItemStack jar = inventory.getStackInSlot(index).copy();
            if (!jar.is(JARS)) continue;
            CompoundTag data = GuchongJarData.read(jar);
            if (!data.getString("guguanuuid").equals(uuid)) continue;
            if (!data.getBoolean("shifangguchong")) {
                int grade = TaoguanItem.grade(jar);
                int capacity = grade == 1 ? 1 : grade == 2 ? 4 : 9;
                for (int slot = 0; slot < capacity; slot++) {
                    String suffix = Integer.toString(slot);
                    if (!data.getString("belongToMasterUUID" + suffix).equals(player.getStringUUID())) continue;
                    spawn(level, player, data, slot);
                    count++;
                }
                if (count >= 1) {
                    data.putBoolean("shifangguchong", true);
                    data.putDouble("shifangnum", count);
                    writeJar(inventory, index, jar, data);
                    menu.updateDisplayedJar(player, selected, data, true);
                    break;
                }
            } else {
                count = data.getDouble("shifangnum");
                for (Entity insect : nearby(level, player)) {
                    if (!(insect instanceof Guchong01Entity || insect instanceof Guchong03Entity || insect instanceof Guchong04Entity)) continue;
                    if (!insect.getPersistentData().getString("guguanuuid").equals(data.getString("guguanuuid"))) continue;
                    GuchongJarData.capture(insect, data, Relations.team(player));
                    count = data.getDouble("shifangnum");
                    insect.discard();
                    if (count < 1) {
                        data.putBoolean("shifangguchong", false);
                        data.putDouble("shifangnum", 0);
                        writeJar(inventory, index, jar, data);
                        menu.updateDisplayedJar(player, selected, data, false);
                        break;
                    }
                }
            }
        }
    }

    private static void spawn(ServerLevel level, Player player, CompoundTag jar, int slot) {
        GuchongLifecycle.releaseOwnedJar(level, player, jar, slot, 1, jar.getBoolean("guchongxvnfu" + slot));
    }
    private static List<Entity> nearby(ServerLevel level, Player player) {
        Vec3 center = player.position();
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(8), entity -> true)
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    private static void writeJar(IItemHandler inventory, int index, ItemStack jar, CompoundTag data) {
        jar.setCount(1);
        GuchongJarData.write(jar, data);
        if (inventory instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot(index, jar);
    }
}
