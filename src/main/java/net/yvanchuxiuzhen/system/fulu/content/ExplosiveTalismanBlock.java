package net.yvanchuxiuzhen.system.fulu.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 被爆炸或投射物命中后按符箓品阶引爆的爆裂符方块。 */
public final class ExplosiveTalismanBlock extends ContentBlock {
    private final float explosionPower;

    public ExplosiveTalismanBlock(BlockTraits traits, float explosionPower) {
        super(traits);
        this.explosionPower = explosionPower;
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
        detonate(level, pos);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        detonate(level, hit.getBlockPos());
    }

    private void detonate(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        // 1.18.2 的 DESTROY 向掉落上下文传入爆炸半径，对应此版本固定的掉落衰减。
        Explosion explosion = new Explosion(serverLevel, null, x, y, z, explosionPower,
                false, Explosion.BlockInteraction.DESTROY_WITH_DECAY);
        if (!EventHooks.onExplosionStart(serverLevel, explosion)) {
            explosion.explode();
            explosion.finalizeExplosion(false);
        }
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(x, y, z) < 4096) {
                player.connection.send(new ClientboundExplodePacket(x, y, z, explosionPower,
                        explosion.getToBlow(), explosion.getHitPlayers().get(player), explosion.getBlockInteraction(),
                        explosion.getSmallExplosionParticles(), explosion.getLargeExplosionParticles(),
                        explosion.getExplosionSound()));
            }
        }
    }
}
