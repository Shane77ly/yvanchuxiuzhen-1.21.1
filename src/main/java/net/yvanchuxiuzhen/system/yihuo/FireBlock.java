package net.yvanchuxiuzhen.system.yihuo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.yihuo.api.FireExhaustedEvent;
import net.yvanchuxiuzhen.system.yihuo.api.FireHeatEvent;

/** 十八种阴阳火块共用的燃料、二十刻供热与接触灼烧；物理差异保留在内容表。 */
public final class FireBlock extends ContentBlock implements EntityBlock {
    private static final TagKey<Block> FURNACES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "danludinglu"));
    private final int material;
    private final int grade;
    private final int unit;
    private final String effect;

    public FireBlock(BlockTraits traits) {
        super(traits);
        material = Integer.parseInt(traits.row().id().substring(8, 11));
        grade = material % 10 - 1;
        unit = 1 << grade;
        effect = material < 230 ? "effect_023" : "effect_024";
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FireBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel server) {
            server.scheduleTick(pos, this, 20);
            ((FireBlockEntity) server.getBlockEntity(pos)).power(10 * unit);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        FireBlockEntity fire = (FireBlockEntity) level.getBlockEntity(pos);
        level.sendParticles(Particles.type("cailiao_" + material + "particle"), pos.getX() + .5, pos.getY(), pos.getZ() + .5, 4, 1, 1, 1, .1);
        if (fire.power() >= unit) {
            fire.power(fire.power() - unit);
            NeoForge.EVENT_BUS.post(new FireHeatEvent(level, pos.above(), unit));
        } else {
            FireExhaustedEvent event = new FireExhaustedEvent(level, pos.above());
            if (level.getBlockState(pos.above()).is(FURNACES)) NeoForge.EVENT_BUS.post(event);
            if (!event.retained()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
        }
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!(level instanceof ServerLevel server)) return;
        FireBlockEntity fire = (FireBlockEntity) server.getBlockEntity(pos);
        if (Relations.isFriendly(fire.friendlyTeams(), entity)) return;
        CompoundTag data = fire.getPersistentData();
        String team = data.getString("team");
        String attacker = data.getString("ManaAttackUUID");
        if (entity instanceof ServerPlayer player) {
            Cultivation.setTizhiString(player, "fireteam", team);
            Cultivation.setTizhiString(player, "fireAttackUUID", attacker);
        } else {
            entity.getPersistentData().putString("fireteam", team);
            entity.getPersistentData().putString("fireAttackUUID", attacker);
        }
        if (fire.power() >= unit) {
            if (entity instanceof LivingEntity target) {
                SpellDamage.command(server, target, "shanghai 火 " + attacker + " false " + (3 * (grade + 1)) + " 1.5");
                MobEffectInstance previous = target.getEffect(StatusEffects.holder(effect));
                int duration = previous == null ? 100 : previous.getDuration() + 100;
                target.addEffect(new MobEffectInstance(StatusEffects.holder(effect), duration, grade, true, true));
            }
        } else {
            server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1, 1);
            server.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
        }
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return (FireBlockEntity) level.getBlockEntity(pos);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, parameter);
    }
}
