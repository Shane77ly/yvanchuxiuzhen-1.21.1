package net.yvanchuxiuzhen.collection.decor;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 装饰合集使用的结构化物品组件与实体附件。 */
public final class DecorData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<DataComponentType<PlaqueInscription>> PLAQUE_INSCRIPTION =
            COMPONENTS.registerComponentType("plaque_inscription",
                    builder -> builder.persistent(PlaqueInscription.CODEC).networkSynchronized(PlaqueInscription.STREAM_CODEC));
    public static final Supplier<DataComponentType<CritterCageData>> CRITTER_CAGE =
            COMPONENTS.registerComponentType("critter_cage",
                    builder -> builder.persistent(CritterCageData.CODEC).networkSynchronized(CritterCageData.STREAM_CODEC));
    public static final Supplier<DataComponentType<RoofBrushConfig>> ROOF_BRUSH =
            COMPONENTS.registerComponentType("roof_brush",
                    builder -> builder.persistent(RoofBrushConfig.CODEC).networkSynchronized(RoofBrushConfig.STREAM_CODEC));
    public static final Supplier<AttachmentType<Double>> HOT_OIL_CONTACT_TICKS = ATTACHMENTS.register(
            "hot_oil_contact_ticks", () -> AttachmentType.builder(() -> 0.0).serialize(Codec.DOUBLE).build());
    public static final Supplier<AttachmentType<RoofBrushSelection>> ROOF_BRUSH_SELECTION = ATTACHMENTS.register(
            "roof_brush_selection", () -> AttachmentType.builder(RoofBrushSelection::new).serialize(RoofBrushSelection.CODEC).build());

    private DecorData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
