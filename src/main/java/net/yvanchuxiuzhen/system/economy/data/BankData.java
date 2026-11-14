package net.yvanchuxiuzhen.system.economy.data;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.BankAccount;
import net.yvanchuxiuzhen.system.economy.api.BankCard;
import net.yvanchuxiuzhen.system.economy.api.BankDisplay;

/** 账户存盘并在全部玩家克隆中复制，只同步本人；物品组件与菜单投影分别登记。 */
public final class BankData {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<DataComponentType<BankCard>> CARD = COMPONENTS.registerComponentType("bank_card",
            builder -> builder.persistent(BankCard.CODEC).networkSynchronized(BankCard.STREAM_CODEC));
    public static final Supplier<AttachmentType<BankAccount>> ACCOUNT = TYPES.register("bank_account", () -> AttachmentType
            .builder(() -> BankAccount.EMPTY)
            .serialize(BankAccount.CODEC)
            .copyOnDeath()
            .sync((holder, player) -> holder == player, BankAccount.STREAM_CODEC)
            .build());
    public static final Supplier<AttachmentType<BankDisplay>> DISPLAY = TYPES.register("bank_display", () -> AttachmentType
            .builder(() -> BankDisplay.EMPTY)
            .sync((holder, player) -> holder == player, BankDisplay.STREAM_CODEC)
            .build());

    private BankData() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        TYPES.register(modBus);
    }
}
