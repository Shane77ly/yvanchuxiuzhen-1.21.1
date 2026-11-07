package net.yvanchuxiuzhen.system.yihuo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 异火材料投掷物：无重力飞行八刻，命中实体灼烧，命中方块放置或补充火块。 */
public final class YihuoProjectile extends AbstractArrow implements ItemSupplier {
    private static final TagKey<Block> FIRE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "blockfire"));
    private final int material;
    private int flyTicks;

    public YihuoProjectile(EntityType<YihuoProjectile> type, Level level, int material) {
        super(type, level);
        this.material = material;
        setCritArrow(false);
        setBaseDamage(0.1);
        setSilent(true);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "cailiao_" + material)));
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("yihuoFlyTicks", flyTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        flyTicks = tag.getInt("yihuoFlyTicks");
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(Particles.type("cailiao_" + material + "particle"), getX(), getY() + 0.5, getZ(),
                    4, 0.3, 0.3, 0.3, 0.1);
            if (++flyTicks >= 8) {
                discard();
            }
        }
        if (inGround) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level() instanceof ServerLevel server && getOwner() != null) {
            hitEntity(server, result.getEntity(), getOwner());
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel server && getOwner() != null) {
            hitBlock(server, result.getBlockPos(), getOwner());
        }
    }

    private void hitEntity(ServerLevel level, Entity target, Entity owner) {
        if (Relations.isFriendly(owner, target)) {
            return;
        }
        String team = Relations.entityTeam(owner);
        String attacker = owner.getStringUUID();
        if (target instanceof net.minecraft.server.level.ServerPlayer player) {
            Cultivation.setTizhiString(player, "fireteam", team);
            Cultivation.setTizhiString(player, "fireAttackUUID", attacker);
        } else {
            target.getPersistentData().putString("fireteam", team);
            target.getPersistentData().putString("fireAttackUUID", attacker);
        }
        if (!(target instanceof LivingEntity living)) {
            return;
        }
        int grade = material % 10 - 1;
        SpellDamage.command(level, living, "shanghai 火 " + attacker + " false " + (3 * (grade + 1)) + " 1.5");
        String effect = material < 230 ? "effect_023" : "effect_024";
        MobEffectInstance previous = living.getEffect(StatusEffects.holder(effect));
        int duration = previous == null ? 200 : previous.getDuration() + 200;
        living.addEffect(new MobEffectInstance(StatusEffects.holder(effect), duration, grade, true, true));
        level.sendParticles(Particles.type("cailiao_" + material + "particle"), getX(), getY(), getZ(), 10, 0, 0, 0, 0.5);
        if (delayedReset(material)) {
            YihuoEffects.resetInvulnerabilityNextTick(living);
        } else {
            living.invulnerableTime = 0;
        }
    }

    private void hitBlock(ServerLevel level, BlockPos hit, Entity owner) {
        int grade = material % 10 - 1;
        int fuel = 1 << grade;
        if (owner.isShiftKeyDown()) {
            BlockPos placement = hit.above();
            if (level.getBlockState(placement).is(Blocks.AIR)) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_" + material + "fireblock");
                level.setBlock(placement, BuiltInRegistries.BLOCK.get(id).defaultBlockState(), Block.UPDATE_ALL);
                BlockEntity blockEntity = level.getBlockEntity(placement);
                if (blockEntity instanceof FireBlockEntity fire && owner instanceof Player player) {
                    fire.initialize(player, 10 * fuel);
                }
            }
            return;
        }
        level.sendParticles(Particles.type("cailiao_" + material + "particle"), getX(), getY(), getZ(), 10, 0, 0, 0, 0.5);
        for (BlockPos position : new BlockPos[] {hit, hit.above(), hit.below()}) {
            if (level.getBlockState(position).is(FIRE) && level.getBlockEntity(position) instanceof FireBlockEntity fire) {
                fire.power(fire.power() + fuel);
                break;
            }
        }
    }

    private static boolean delayedReset(int material) {
        return material == 221 || material == 222 || material == 223 || material == 224
                || material == 225 || material == 226 || material == 236;
    }
}
