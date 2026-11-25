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
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.api.GuchongRuntimeState;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongBehavior;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 蛊虫跨入口的来源生命周期装配；各入口只恢复其来源实际写入的字段。 */
public final class GuchongLifecycle {
    private GuchongLifecycle() {
    }

    /** 单虫物品释放：严格对应 Guchong01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure。 */
    public static boolean releaseSingleItem(ServerLevel level, BlockPos pos, Player owner, ItemStack stack, String entityId) {
        GuchongEntity insect = (GuchongEntity) GuchongSetup.entityType(entityId).get().create(level);
        if (insect == null) return false;
        insect.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, level.random.nextFloat() * 360F, 0);
        CompoundTag source = GuchongJarData.read(stack);
        if (source.getBoolean("guchongxvnfu")) {
            EntityAttributes.update(insect, stats -> stats
                    .withRealmName(source.getString("LEVELname"))
                    .withLevel(source.getDouble("LEVELnumber")));
            insect.setData(GuchongData.RUNTIME, insect.getData(GuchongData.RUNTIME).withTamed(true));
            insect.setCustomName(Component.literal(stack.getDisplayName().getString()));
            insect.tame(owner);
            Relations.setTeam(insect, Relations.team(owner));
            GuchongJarData.restoreEntityAttributes(source, "", insect, false);
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
        GuchongJarData.copyEntityProgress(jar, suffix, insect, false);
        insect.setData(GuchongData.RUNTIME, insect.getData(GuchongData.RUNTIME)
                .withJar(jar.getString("guguanuuid"), slot)
                .withTamed(tamed)
                .withOwnerUuid(jar.getString("belongToMasterUUID" + suffix)));
        insect.setCustomName(Component.literal(jar.getString("guchongname" + suffix).replace("[", "").replace("]", "")));
        if (insect instanceof TamableAnimal tamable) tamable.tame(owner);
        GuchongJarData.restoreEntityAttributes(jar, suffix, insect, false);
        GuchongJarRuntime.finishSpawn(level, insect);
        return insect;
    }

    /** 地上蛊罐自然释放：保留看家、回罐坐标和目标UUID等罐专属字段。 */
    public static boolean releaseGroundJar(ServerLevel level, BlockPos pos, CompoundTag blockData,
            CompoundTag source, int slot, Entity target) {
        Entity insect = GuchongJarRuntime.create(level, source.getString("guchongid"));
        if (insect == null) return false;
        insect.moveTo(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, level.random.nextFloat() * 360F, 0);
        GuchongJarData.copyEntityProgress(source, "", insect, true);
        // 来源两次对空字符串执行替换，虫物品字段为空与非空时结果不同。
        String name = "".replace(source.getString("guchongname"), "[");
        name = "".replace(name, "]");
        insect.setCustomName(Component.literal(name));
        if (GuchongBehavior.findEntity(level, source.getString("belongToMasterUUID")) instanceof Player owner
                && insect instanceof TamableAnimal tamable) tamable.tame(owner);
        insect.setData(GuchongData.RUNTIME, new GuchongRuntimeState(
                true,
                source.getString("belongToMasterUUID"),
                "",
                slot,
                true,
                pos,
                target.getStringUUID()));
        Relations.shareBlockAllegiance(blockData, insect, source.getString("team"));
        GuchongJarData.restoreEntityAttributes(source, "", insect, true);
        GuchongJarRuntime.finishSpawn(level, insect);
        return true;
    }

}
