package net.yvanchuxiuzhen.system.fulu.content;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 镇宅符放置后共享放置者友方集合的只读视图，重载后使用独立集合执行排斥规则。 */
public final class RepellingTalismanBlockEntity extends BlockEntity {
    private static final String FRIENDLIST = "friendlist";
    private static final String TEAM = "team";
    private Set<String> friendlyTeams = Set.of();

    public RepellingTalismanBlockEntity(BlockPos pos, BlockState state) {
        super(FuluBlockEntities.REPELLING.get(), pos, state);
    }

    public void captureAllegiance(Entity placer) {
        CompoundTag data = getPersistentData();
        friendlyTeams = placer instanceof Player ? Relations.friendlyTeams(placer) : Set.of();
        storeFriendlyTeams();
        data.putString(TEAM, placer instanceof Player ? Relations.team(placer) : "");
        setChanged();
    }

    private void storeFriendlyTeams() {
        CompoundTag friendlist = new CompoundTag();
        for (String team : friendlyTeams) friendlist.putBoolean(team, true);
        getPersistentData().put(FRIENDLIST, friendlist);
    }

    public void repel(ServerLevel level) {
        Vec3 center = Vec3.atCenterOf(worldPosition);
        AABB bounds = new AABB(center, center).inflate(16.0);
        level.getEntitiesOfClass(Entity.class, bounds, entity -> true).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center)))
                .filter(entity -> entity instanceof Player || entity instanceof Mob)
                .filter(entity -> !friendlyTeams.contains(Relations.team(entity)))
                .forEach(entity -> entity.setDeltaMovement(new Vec3(
                        (entity.getX() - center.x) * 0.1,
                        0.1,
                        (entity.getZ() - center.z) * 0.1)));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag friendlist = getPersistentData().getCompound(FRIENDLIST);
        friendlyTeams = friendlist.getAllKeys().stream().filter(friendlist::getBoolean)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        storeFriendlyTeams();
        super.saveAdditional(tag, registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
