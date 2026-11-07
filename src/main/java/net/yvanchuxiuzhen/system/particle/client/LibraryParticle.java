package net.yvanchuxiuzhen.system.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.yvanchuxiuzhen.system.particle.ParticleRow;

/** 原生贴图粒子的已核实来源模板；原版引擎负责批次、碰撞、阻尼、生命周期及资源重载。 */
final class LibraryParticle extends TextureSheetParticle {
    private final ParticleRow row;
    private final SpriteSet sprites;
    private float angularVelocity;

    LibraryParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                    ParticleRow row, SpriteSet sprites) {
        super(level, x, y, z);
        this.row = row;
        this.sprites = sprites;
        setSize(row.width(), row.height());
        quadSize *= row.scale();
        lifetime = row.lifetimeJitter() == 0 ? row.lifetime()
                : Math.max(1, row.lifetime() + random.nextInt(row.lifetimeJitter() * 2) - row.lifetimeJitter());
        gravity = row.gravity();
        hasPhysics = row.physics();
        xd = vx * row.speed();
        yd = vy * row.speed();
        zd = vz * row.speed();
        angularVelocity = row.angularVelocity();
        if (row.frames() == 0) {
            pickSprite(sprites);
        } else {
            setSpriteFromAge(sprites);
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return row.fullBright() ? 15728880 : super.getLightColor(partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return switch (row.render()) {
            case OPAQUE -> ParticleRenderType.PARTICLE_SHEET_OPAQUE;
            case LIT -> ParticleRenderType.PARTICLE_SHEET_LIT;
            case TRANSLUCENT -> ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        };
    }

    @Override
    public void tick() {
        super.tick();
        oRoll = roll;
        roll += angularVelocity;
        angularVelocity += row.angularAcceleration();
        if (!removed && row.frames() > 0) {
            setSprite(sprites.get((age / row.frameTicks()) % row.frames() + 1, row.frames()));
        }
    }
}
