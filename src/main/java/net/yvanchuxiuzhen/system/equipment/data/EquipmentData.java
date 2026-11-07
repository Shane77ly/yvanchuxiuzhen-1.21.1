package net.yvanchuxiuzhen.system.equipment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProduct;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.api.FabaoFireData;

/** 装备实例组件随物品保存、同步；四饰品附件随玩家保存、死亡复制并只同步本人。 */
public final class EquipmentData {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<AccessoryData>> ACCESSORIES = ATTACHMENTS.register("accessories", () -> AttachmentType
            .builder(() -> new AccessoryData()).serialize(AccessoryData.CODEC).copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.fromCodecWithRegistries(AccessoryData.CODEC)).build());
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<DataComponentType<EquipmentProperties>> PROPERTIES = COMPONENTS.registerComponentType("equipment_properties",
            builder -> builder.persistent(EquipmentProperties.CODEC).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(EquipmentProperties.CODEC)));
    public static final Supplier<DataComponentType<EquipmentStats>> STATS = COMPONENTS.registerComponentType("equipment_stats",
            builder -> builder.persistent(EquipmentStats.CODEC).networkSynchronized(EquipmentStats.STREAM_CODEC));
    public static final Supplier<DataComponentType<EquipmentProduct>> PRODUCT = COMPONENTS.registerComponentType("equipment_product",
            builder -> builder.persistent(EquipmentProduct.CODEC).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(EquipmentProduct.CODEC)));
    public static final Supplier<DataComponentType<FabaoFireData>> FABAO_FIRE = COMPONENTS.registerComponentType("fabao_fire",
            builder -> builder.persistent(FabaoFireData.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(FabaoFireData.CODEC)));
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
        ATTACHMENTS.register(modBus);
        COMPONENTS.register(modBus);
    }
}
