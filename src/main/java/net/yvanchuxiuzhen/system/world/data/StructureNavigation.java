package net.yvanchuxiuzhen.system.world.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

/** 寻灵盘导航状态：同一主人召出的光环可在内存共享此对象，序列化后各自独立。 */
public final class StructureNavigation {
    public static final Codec<StructureNavigation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("belongToMasterUUID", "").forGetter(StructureNavigation::masterUuid),
            Codec.STRING.optionalFieldOf("playeruuid", "").forGetter(StructureNavigation::playerUuid),
            Codec.DOUBLE.optionalFieldOf("mubiaoX", 0.0).forGetter(n -> n.targetX),
            Codec.DOUBLE.optionalFieldOf("mubiaoY", 0.0).forGetter(n -> n.targetY),
            Codec.DOUBLE.optionalFieldOf("mubiaoZ", 0.0).forGetter(n -> n.targetZ)
    ).apply(instance, StructureNavigation::new));

    private String masterUuid = "";
    private String playerUuid = "";
    private double targetX;
    private double targetY;
    private double targetZ;

    public StructureNavigation() {
    }

    public StructureNavigation(String masterUuid, String playerUuid, double targetX, double targetY, double targetZ) {
        this.masterUuid = masterUuid == null ? "" : masterUuid;
        this.playerUuid = playerUuid == null ? "" : playerUuid;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    public String masterUuid() { return masterUuid; }
    public String playerUuid() { return playerUuid; }
    public Vec3 target() { return new Vec3(targetX, targetY, targetZ); }

    public void setOwner(String uuid) {
        String value = uuid == null ? "" : uuid;
        this.masterUuid = value;
        this.playerUuid = value;
    }

    public void setTarget(Vec3 position) {
        this.targetX = position.x;
        this.targetY = position.y;
        this.targetZ = position.z;
    }

    public void copyFrom(StructureNavigation other) {
        this.masterUuid = other.masterUuid;
        this.playerUuid = other.playerUuid;
        this.targetX = other.targetX;
        this.targetY = other.targetY;
        this.targetZ = other.targetZ;
    }

    public StructureNavigation copy() {
        return new StructureNavigation(masterUuid, playerUuid, targetX, targetY, targetZ);
    }
}
