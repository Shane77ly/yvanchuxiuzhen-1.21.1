package net.yvanchuxiuzhen.system.equipment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;

/** 装备数值、成品记录与妖化头盔计时随物品保存、同步；卸下头盔暂停，不另存玩家变身副本。 */
public final class EquipmentData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<EquipmentStats>> STATS = COMPONENTS.registerComponentType("equipment_stats",
            builder -> builder.persistent(EquipmentStats.CODEC).networkSynchronized(EquipmentStats.STREAM_CODEC));
    public static final Supplier<DataComponentType<EquipmentProduct>> PRODUCT = COMPONENTS.registerComponentType("equipment_product",
            builder -> builder.persistent(EquipmentProduct.CODEC).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(EquipmentProduct.CODEC)));
    public static final Supplier<DataComponentType<DemonTimer>> DEMON_TIMER = COMPONENTS.registerComponentType("demon_timer",
            builder -> builder.persistent(DemonTimer.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(DemonTimer.CODEC)));

    public record DemonTimer(double ticks, double seconds, boolean ending) {
        public static final DemonTimer EMPTY = new DemonTimer(0, 0, false);
        public static final Codec<DemonTimer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("ticks").forGetter(DemonTimer::ticks),
                Codec.DOUBLE.fieldOf("seconds").forGetter(DemonTimer::seconds),
                Codec.BOOL.fieldOf("ending").forGetter(DemonTimer::ending)
        ).apply(instance, DemonTimer::new));
    }

    private EquipmentData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
