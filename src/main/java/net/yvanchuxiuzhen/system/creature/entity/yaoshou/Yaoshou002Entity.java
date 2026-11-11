package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Random;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;

/** 002号妖兽：五种皮肤、三阶最低生成等级与来源评级初始化。 */
public final class Yaoshou002Entity extends YaoshouEntity {
    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }
    public Yaoshou002Entity(EntityType<? extends Yaoshou002Entity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return YaoshouEntity.attributes(model);
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        if (!YaoshouShared.initialize(this, 3, "贫弱", "强劲", "平庸", "强劲", "勉强", "贫弱",
                "贫弱", "贫弱", "强劲", "贫弱", "贫弱", "贫弱", "贫弱")) return;
        int variant = new Random().nextInt(5) + 1;
        getAttribute(YaoshouAttributes.SKIN).setBaseValue(variant);
        setTexture(variant == 5 ? "yaoshou00205" : "yaoshou0020" + variant);
    }

    @Override protected void sourceTick() { Yaoshou002Behavior.tick(this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou002Behavior.hurt(this, source); }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }
}
