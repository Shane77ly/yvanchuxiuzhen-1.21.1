package net.yvanchuxiuzhen.system.guchong.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongBehavior;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 地上三档蛊罐的精血认主、装饰物、收罐和逐目标逐槽释放。 */
public final class GuchongJarRuntime {
    private static final TagKey<Block> GRASS = material("grass");
    private static final TagKey<Block> DIRT = material("dirt");
    private static final TagKey<Block> STONE = material("stone");
    private static final TagKey<Block> WOOD = material("wood");
    private static final TagKey<EntityType<?>> NPC = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "npcentity"));

    private GuchongJarRuntime() { }

    private static TagKey<Block> material(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_material_" + name));
    }

    public static void interact(ServerLevel level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof TaoguanBlockEntity jar)) return;
        initializeContents(jar);
        ItemStack held = player.getMainHandItem();
        if (BuiltInRegistries.ITEM.getKey(held.getItem()).equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "jingxve"))) {
            CompoundTag blood = GuchongJarData.read(held);
            for (int slot = 0; slot < jar.insectSlots(); slot++) {
                ItemStack insect = jar.getItem(slot).copy();
                CompoundTag data = GuchongJarData.read(insect);
                String name = insect.getDisplayName().getString();
                if (!insect.isEmpty() && blood.getInt("playerlevelnum") >= data.getInt("LEVELnumber") && !data.getBoolean("guchongxvnfu")) {
                    data.putBoolean("guchongxvnfu", true);
                    data.putString("team", Relations.entityTeam(player));
                    data.putString("belongToMasterUUID", player.getStringUUID());
                    insect.setCount(1);
                    GuchongJarData.write(insect, data);
                    jar.setItem(slot, insect);
                    message(player, "§6" + name + "§f认主成功！");
                } else if (blood.getInt("playerlevelnum") < data.getInt("LEVELnumber")) {
                    message(player, "§4" + name + "等级过高，无法认主！");
                } else if (data.getBoolean("guchongxvnfu")) {
                    message(player, data.getString("belongToMasterUUID").equals(player.getStringUUID())
                            ? "§6" + name + "§f已经认主。" : "§4您不是" + name + "的主人，无法强行认主！");
                }
            }
            held.shrink(1);
        }
        int decoration = jar.insectSlots();
        if (player.isShiftKeyDown() && !jar.getItem(decoration).isEmpty()) {
            player.getInventory().placeItemBackInInventory(jar.getItem(decoration).copyWithCount(1));
            jar.setItem(decoration, ItemStack.EMPTY);
        } else if (held.getItem() instanceof BlockItem item && decorative(item.getBlock().defaultBlockState())) {
            if (!jar.getItem(decoration).isEmpty()) player.getInventory().placeItemBackInInventory(jar.getItem(decoration).copyWithCount(1));
            jar.setItem(decoration, held.copyWithCount(1));
            held.shrink(1);
        } else if (ModifierKeys.ctrl(player) || ModifierKeys.alt(player)) {
            if (!jar.jarData().getBoolean("shifangguchong")) {
                ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taoguan_0" + jar.grade() + "item")));
                CompoundTag data = new CompoundTag();
                data.putString("guguanuuid", jar.jarData().getString("guguanuuid"));
                for (int slot = 0; slot < jar.insectSlots(); slot++) {
                    ItemStack insect = jar.getItem(slot).copy();
                    if (!insect.isEmpty()) {
                        GuchongJarData.pack(insect, data, slot, true);
                        jar.setItem(slot, ItemStack.EMPTY);
                    }
                }
                GuchongJarData.write(result, data);
                player.getInventory().placeItemBackInInventory(result);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return;
            }
            message(player, "§4还有蛊虫未回到蛊罐，无法拿起蛊罐！");
        }
        jar.sync();
    }

    private static boolean decorative(BlockState state) {
        return state.is(DIRT) || state.is(STONE) || state.is(GRASS) || state.is(WOOD);
    }

    private static void initializeContents(TaoguanBlockEntity jar) {
        StringBuilder team = new StringBuilder();
        for (int slot = 0; slot < jar.insectSlots(); slot++) {
            ItemStack stack = jar.getItem(slot).copy();
            if (!stack.is(TaoguanItem.INSECTS)) continue;
            CompoundTag data = GuchongJarData.read(stack);
            if (!data.getString("health_Max_LEVEL").isEmpty()) continue;
            data.putString("LEVELname", "一阶初级");
            data.putInt("LEVELnumber", 1);
            data.putInt("experience_Max", 120);
            // 来源以完整注册标识与裸路径比较；保留不会命中三种资质分支的实际结果。
            for (int letter = 0; letter < 8; letter++) team.append((char) ('a' + jar.getLevel().random.nextInt(26)));
            data.putString("team", team.toString());
            for (String key : new String[]{"health_Max", "health_Num", "power_Max", "power", "soul_Max", "soul"}) data.putInt(key, 120);
            for (String key : new String[]{"health_Rec", "power_Rec", "soul_Rec"}) data.putInt(key, 1);
            for (String key : new String[]{"attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"}) data.putInt(key, 3);
            double speed = switch (data.getString("speed_LEVEL")) {
                case "绝佳" -> .3;
                case "强劲" -> .25;
                case "平庸" -> .2;
                case "勉强" -> .15;
                default -> .1;
            };
            data.putDouble("speed_number", speed + .005);
            GuchongJarData.write(stack, data);
            stack.setCount(1);
            jar.setItem(slot, stack);
        }
    }

    public static void release(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof TaoguanBlockEntity jar)) return;
        CompoundTag data = jar.jarData();
        if (data.getBoolean("shifangguchong")) return;
        Vec3 center = Vec3.atCenterOf(pos);
        List<Entity> targets = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(8), entity -> true)
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
        boolean releasing = false;
        BlockState floor = level.getBlockState(pos.below());
        for (Entity target : targets) {
            if (target.isAlive() && !Relations.entityTeam(target).equals(data.getString("team")) && matches(floor, target)) releasing = true;
            if (!releasing) continue;
            level.sendParticles(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, 10, 0, 0, 0, .1);
            int released = 0;
            for (int slot = 0; slot < jar.insectSlots(); slot++) {
                CompoundTag insectData = GuchongJarData.read(jar.getItem(slot).copy());
                if (!insectData.getBoolean("guchongxvnfu")) continue;
                Entity insect = create(level, insectData.getString("guchongid"));
                if (insect != null) {
                    insect.moveTo(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, level.random.nextFloat() * 360F, 0);
                    CompoundTag insectTag = insect.getPersistentData();
                    GuchongJarData.copy(insectData, "", insectTag, "", true);
                    insectTag.putInt("guguanweizhi", slot);
                    String name = "".replace(insectData.getString("guchongname"), "[");
                    name = "".replace(name, "]");
                    insect.setCustomName(Component.literal(name));
                    if (insect instanceof TamableAnimal tamable && GuchongBehavior.findEntity(level, insectData.getString("belongToMasterUUID")) instanceof Player owner) tamable.tame(owner);
                    insectTag.putBoolean("kanjia", true);
                    insectTag.putBoolean("guchongxvnfu", true);
                    insectTag.putDouble("homeblockX", pos.getX());
                    insectTag.putDouble("homeblockY", pos.getY());
                    insectTag.putDouble("homeblockZ", pos.getZ());
                    Relations.shareBlockAllegiance(data, insect, insectData.getString("team"));
                    applyAttributes(insect, insectTag);
                    insectTag.putString("gongjimubiao", target.getStringUUID());
                    finishSpawn(level, insect);
                }
                released++;
            }
            data.putInt("shifangnum", released);
            if (released >= 1) data.putBoolean("shifangguchong", true);
        }
        jar.sync();
    }

    private static boolean matches(BlockState floor, Entity target) {
        if (floor.is(GRASS) || floor.is(DIRT)) return target instanceof Animal;
        if (floor.is(STONE)) return target instanceof Mob || target instanceof Animal || target instanceof AmbientCreature || target instanceof Player;
        if (floor.is(WOOD)) return target instanceof Mob && !target.getType().is(NPC);
        return target instanceof Player;
    }

    public static Entity create(ServerLevel level, String id) {
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id)).create(level);
    }

    public static void applyAttributes(Entity entity, CompoundTag data) {
        LivingEntity living = (LivingEntity) entity;
        living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
        living.setHealth((float) data.getDouble("health_Num"));
        living.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble("attack_Num"));
        living.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.getDouble("speed_number"));
    }

    public static void finishSpawn(ServerLevel level, Entity entity) {
        if (entity instanceof Mob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(entity);
    }

    private static void message(Player player, String message) {
        player.displayClientMessage(Component.literal(message), false);
    }
}
