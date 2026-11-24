package net.yvanchuxiuzhen.system.liandan.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.api.MedicineInstance;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipe;

/** 炼丹系统的强类型物品组件与药草解析附件注册；由炼丹系统统一管理读写。 */
public final class LiandanData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);

    public static final Supplier<DataComponentType<MedicineInstance>> MEDICINE = COMPONENTS.registerComponentType("medicine",
            builder -> builder.persistent(MedicineInstance.CODEC).networkSynchronized(MedicineInstance.STREAM_CODEC));
    public static final Supplier<DataComponentType<MedicineRecipe>> RECIPE = COMPONENTS.registerComponentType("medicine_recipe",
            builder -> builder.persistent(MedicineRecipe.CODEC).networkSynchronized(MedicineRecipe.STREAM_CODEC));
    public static final Supplier<AttachmentType<HerbAnalysis>> HERB_ANALYSIS = ATTACHMENTS.register("herb_analysis", () -> AttachmentType
            .builder(() -> HerbAnalysis.EMPTY)
            .serialize(HerbAnalysis.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.fromCodec(HerbAnalysis.CODEC))
            .build());

    private LiandanData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
