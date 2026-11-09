package net.yvanchuxiuzhen.system.liandan.data;

import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbCommand;

/** 药草解析附件由炼丹系统写入，随玩家保存并在死亡重生时保留，只同步给本人。 */
public final class LiandanData {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);

    public static final Supplier<AttachmentType<HerbAnalysis>> HERB_ANALYSIS = ATTACHMENTS.register("herb_analysis", () -> AttachmentType
            .builder(() -> HerbAnalysis.EMPTY)
            .serialize(HerbAnalysis.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.fromCodec(HerbAnalysis.CODEC))
            .build());

    private LiandanData() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(LiandanHerbCommand::register);
    }
}
