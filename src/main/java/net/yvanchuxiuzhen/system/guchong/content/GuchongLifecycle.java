package net.yvanchuxiuzhen.system.guchong.content;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongBehavior;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 蛊虫跨入口的来源生命周期装配；各入口只恢复其来源实际写入的字段。 */
public final class GuchongLifecycle {
    private static final String[] RATINGS = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
            "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"};

    private GuchongLifecycle() {
    }

    /** 单虫物品释放：严格对应 Guchong01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure。 */
    public static boolean releaseSingleItem(ServerLevel level, BlockPos pos, Player owner, ItemStack stack, String entityId) {
        GuchongEntity insect = (GuchongEntity) GuchongSetup.entityType(entityId).get().create(level);
        if (insect == null) return false;
        insect.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, level.random.nextFloat() * 360F, 0);
        CompoundTag source = GuchongJarData.read(stack);
        if (source.getBoolean("guchongxvnfu")) {
            CompoundTag data = insect.getPersistentData();
            data.putString("LEVELname", source.getString("LEVELname"));
            data.putDouble("LEVELnumber", source.getDouble("LEVELnumber"));
            data.putBoolean("guchongxvnfu", true);
            insect.setCustomName(Component.literal(stack.getDisplayName().getString()));
            insect.tame(owner);
            Relations.setTeam(insect, Relations.team(owner));
            copySingleRatings(source, data);
            data.putDouble("speed_number", source.getDouble("speed_number"));
            data.putDouble("health_Num", source.getDouble("health_Num"));
            GuchongJarRuntime.applyAttributes(insect, data);
            data.remove("health_Num");
        }
        GuchongJarRuntime.finishSpawn(level, insect);
        return true;
    }

    /** 手持蛊罐或蛊笛释放：对应手持罐与 GuchongAnniuzhixing 的共同来源字段。 */
    public static Entity releaseOwnedJar(ServerLevel level, Player owner, CompoundTag jar, int slot, double yOffset, boolean tamed) {
        String suffix = Integer.toString(slot);
        Entity insect = GuchongJarRuntime.create(level, jar.getString("guchongid" + suffix));
        if (insect == null) return null;
        insect.moveTo(owner.getX(), owner.getY() + yOffset, owner.getZ(), level.random.nextFloat() * 360F, 0);
        Relations.shareEntityAllegiance(owner, insect);
        insect.setYRot(owner.getYRot());
        insect.setXRot(0);
        insect.setYBodyRot(insect.getYRot());
        insect.setYHeadRot(insect.getYRot());
        insect.yRotO = insect.getYRot();
        insect.xRotO = insect.getXRot();
        if (insect instanceof LivingEntity living) {
            living.yBodyRotO = living.getYRot();
            living.yHeadRotO = living.getYRot();
        }
        insect.setDeltaMovement(owner.getLookAngle().x, owner.getLookAngle().y + 1, owner.getLookAngle().z);
        CompoundTag data = insect.getPersistentData();
        data.putString("guguanuuid", jar.getString("guguanuuid"));
        data.putDouble("guguanweizhi", slot);
        data.putString("LEVELname", jar.getString("LEVELname" + suffix));
        data.putDouble("LEVELnumber", jar.getDouble("LEVELnumber" + suffix));
        data.putDouble("experience_Max", jar.getDouble("experience_Max" + suffix));
        data.putDouble("experience", jar.getDouble("experience" + suffix));
        insect.setCustomName(Component.literal(jar.getString("guchongname" + suffix).replace("[", "").replace("]", "")));
        data.putBoolean("guchongxvnfu", tamed);
        if (insect instanceof TamableAnimal tamable) tamable.tame(owner);
        data.putString("belongToMasterUUID", jar.getString("belongToMasterUUID" + suffix));
        copyJarRatings(jar, suffix, data);
        data.putDouble("speed_number", jar.getDouble("speed_number" + suffix));
        data.putDouble("health_Num", jar.getDouble("health_Num" + suffix));
        GuchongJarRuntime.applyAttributes(insect, data);
        data.remove("health_Num");
        GuchongJarRuntime.finishSpawn(level, insect);
        return insect;
    }

    /** 地上蛊罐自然释放：保留看家、回罐坐标和目标UUID等罐专属字段。 */
    public static boolean releaseGroundJar(ServerLevel level, BlockPos pos, CompoundTag blockData,
            CompoundTag source, int slot, Entity target) {
        Entity insect = GuchongJarRuntime.create(level, source.getString("guchongid"));
        if (insect == null) return false;
        insect.moveTo(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, level.random.nextFloat() * 360F, 0);
        CompoundTag data = insect.getPersistentData();
        data.putString("LEVELname", source.getString("LEVELname"));
        data.putInt("guguanweizhi", slot);
        data.putInt("LEVELnumber", source.getInt("LEVELnumber"));
        data.putInt("experience_Max", source.getInt("experience_Max"));
        data.putInt("experience", source.getInt("experience"));
        // 来源生成器在此处以空字符串调用 replace；保留该实际来源结果，不改写为手持罐名称规则。
        insect.setCustomName(Component.literal(""));
        if (GuchongBehavior.findEntity(level, source.getString("belongToMasterUUID")) instanceof Player owner
                && insect instanceof TamableAnimal tamable) tamable.tame(owner);
        data.putBoolean("kanjia", true);
        data.putBoolean("guchongxvnfu", true);
        data.putString("belongToMasterUUID", source.getString("belongToMasterUUID"));
        data.putString("team", source.getString("team"));
        data.putInt("homeblockX", pos.getX());
        data.putInt("homeblockY", pos.getY());
        data.putInt("homeblockZ", pos.getZ());
        Relations.shareBlockAllegiance(blockData, insect, source.getString("team"));
        copyGroundRatings(source, data);
        data.putDouble("speed_number", source.getDouble("speed_number"));
        data.putDouble("health_Num", source.getDouble("health_Num"));
        GuchongJarRuntime.applyAttributes(insect, data);
        data.remove("health_Num");
        data.putString("gongjimubiao", target.getStringUUID());
        GuchongJarRuntime.finishSpawn(level, insect);
        return true;
    }

    private static void copySingleRatings(CompoundTag source, CompoundTag target) {
        for (String key : RATINGS) {
            target.putString(key + "_LEVEL", source.getString(key + "_LEVEL"));
            if (!key.equals("speed")) target.putDouble(key, source.getDouble(key));
        }
    }

    private static void copyJarRatings(CompoundTag source, String suffix, CompoundTag target) {
        for (String key : RATINGS) {
            target.putString(key + "_LEVEL", source.getString(key + "_LEVEL" + suffix));
            if (!key.equals("speed")) target.putDouble(key, source.getDouble(key + suffix));
        }
    }

    private static void copyGroundRatings(CompoundTag source, CompoundTag target) {
        for (String key : RATINGS) {
            target.putString(key + "_LEVEL", source.getString(key + "_LEVEL"));
            if (!key.equals("speed")) target.putInt(key, source.getInt(key));
        }
    }


}
