package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 妖兽来源同步属性。 */
public final class YaoshouAttributes {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, ModId.VALUE);
    public static final DeferredHolder<Attribute, Attribute> SKIN = ATTRIBUTES.register("yaoshoupifuxianshi",
            () -> new RangedAttribute("attribute." + ModId.VALUE + ".yaoshoupifuxianshi", 1, 1, 5).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SIZE = ATTRIBUTES.register("entitydaxiao",
            () -> new RangedAttribute("attribute." + ModId.VALUE + ".entitydaxiao", 1, 0.7, 10).setSyncable(true));

    private YaoshouAttributes() { }

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }
}
