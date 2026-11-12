package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Random;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;

/** 001号妖兽：三种皮肤与来源评级初始化。 */
public final class Yaoshou001Entity extends YaoshouEntity {
    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }
    public Yaoshou001Entity(EntityType<? extends Yaoshou001Entity> type, Level level, EntityModelRow model) {
        super(type, level, model);
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return YaoshouEntity.attributes(model);
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        if (!YaoshouShared.initialize(this, 1, "贫弱", "强劲", "平庸", "平庸", "勉强", "贫弱",
                "贫弱", "贫弱", "平庸", "勉强", "贫弱", "贫弱", "贫弱")) return;
        int variant = new Random().nextInt(3) + 1;
        getAttribute(YaoshouAttributes.SKIN).setBaseValue(variant);
        setTexture(variant == 3 ? "yaoshou00103" : variant == 2 ? "yaoshou00102" : "yaoshou001");
    }

    @Override protected void sourceTick() { Yaoshou001Behavior.tick(this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou001Behavior.hurt(this, source); }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }
}
