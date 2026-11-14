package net.yvanchuxiuzhen.system.treasure.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.treasure.TreasureSetup;
import net.yvanchuxiuzhen.system.treasure.TreasureToolSettings;

/** 设置弹体在命中时读取射手当前主手，把五参数写入任意方块实体；寿命随实体持久数据保存。 */
public final class TreasureToolProjectile extends AbstractArrow implements ItemSupplier {
    public static final String ID = "projectile_tiandaobaoxiangitem";

    public TreasureToolProjectile(EntityType<? extends TreasureToolProjectile> type, Level level) {
        super(type, level);
    }

    public static void shoot(ServerPlayer player) {
        TreasureToolProjectile projectile = create(player);
        Vec3 direction = player.getViewVector(1);
        projectile.shoot(direction.x, direction.y, direction.z, 2, 0);
        player.serverLevel().addFreshEntity(projectile);
        projectile.pickup = Pickup.DISALLOWED;
    }

    /** 来源保留的目标生物射击重载没有本族自然调用者，按原弹道和十二度散布实现。 */
    public static TreasureToolProjectile shoot(LivingEntity owner, LivingEntity target) {
        TreasureToolProjectile projectile = create(owner);
        double dx = target.getX() - owner.getX();
        double dz = target.getZ() - owner.getZ();
        double dy = target.getY() + target.getEyeHeight() - 1.1 - projectile.getY() + Math.hypot(dx, dz) * 0.2F;
        projectile.shoot(dx, dy, dz, 2, 12);
        owner.level().addFreshEntity(projectile);
        return projectile;
    }

    private static TreasureToolProjectile create(LivingEntity owner) {
        TreasureToolProjectile projectile = TreasureSetup.projectile().get().create(owner.level());
        projectile.setOwner(owner);
        projectile.setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        projectile.setSilent(true);
        projectile.setCritArrow(false);
        projectile.setBaseDamage(1);
        return projectile;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_100")));
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!(level() instanceof ServerLevel level)) return;
        Entity owner = getOwner();
        if (owner == null) return;
        BlockPos pos = hit.getBlockPos();
        BlockEntity target = level.getBlockEntity(pos);
        CompoundTag held = TreasureToolSettings.data(owner instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY);
        if (target != null) {
            CompoundTag data = target.getPersistentData();
            for (String key : new String[]{"TreasureTypeBig", "TreasureTypeSmall"}) {
                data.putString(key, held.getString(key));
                level.sendBlockUpdated(pos, target.getBlockState(), target.getBlockState(), Block.UPDATE_ALL);
            }
            for (String key : new String[]{"TreasureLEVEL", "minimumvalue", "Maximumvalue"}) {
                data.putDouble(key, held.getDouble(key));
                level.sendBlockUpdated(pos, target.getBlockState(), target.getBlockState(), Block.UPDATE_ALL);
            }
            target.setChanged();
        }
        CompoundTag data = target == null ? new CompoundTag() : target.getPersistentData();
        if (!data.getString("TreasureTypeBig").isEmpty()) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 10, 0, 0, 0, 1);
            if (owner instanceof Player player) {
                message(player, "category", data.getString("TreasureTypeBig"));
                message(player, "subtype", data.getString("TreasureTypeSmall"));
                message(player, "level", data.getDouble("TreasureLEVEL"));
                message(player, "count", data.getDouble("minimumvalue"), data.getDouble("Maximumvalue"));
            }
        } else if (owner instanceof Player player) {
            message(player, "failed");
        }
    }

    private static void message(Player player, String key, Object... values) {
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.treasure_tool." + key, values), false);
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        double ticks = getPersistentData().getDouble("tick") + 1;
        getPersistentData().putDouble("tick", ticks);
        if ((!level().isClientSide && ticks >= 20) || inGround) discard();
    }
}
