package net.yvanchuxiuzhen.system.book.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.api.BookTextData;
import net.yvanchuxiuzhen.system.book.api.QuizOwner;

/** 题号持久化并在全部玩家克隆中复制；本人菜单投影与文本/答题归属物品组件。 */
public final class BookQuizData {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<AttachmentType<Integer>> QUESTION = TYPES.register("book_quiz_question", () -> AttachmentType
            .builder(() -> 0)
            .serialize(Codec.intRange(0, 40))
            .copyOnDeath()
            .build());
    public static final Supplier<AttachmentType<BookQuizState>> SESSION = TYPES.register("book_quiz_session", () -> AttachmentType
            .builder(() -> BookQuizState.EMPTY)
            .sync((holder, player) -> holder == player, BookQuizState.STREAM_CODEC)
            .build());
    public static final Supplier<DataComponentType<BookTextData>> TEXT = COMPONENTS.registerComponentType("book_text",
            builder -> builder.persistent(BookTextData.CODEC).networkSynchronized(BookTextData.STREAM_CODEC));
    public static final Supplier<DataComponentType<QuizOwner>> QUIZ_OWNER = COMPONENTS.registerComponentType("quiz_owner",
            builder -> builder.persistent(QuizOwner.CODEC).networkSynchronized(QuizOwner.STREAM_CODEC));

    private BookQuizData() {
    }

    public static BookQuizState state(Player player) {
        return player.getData(SESSION);
    }

    public static void publish(Player player) {
        player.setData(SESSION, new BookQuizState(player.getData(QUESTION), player.getPersistentData().getDouble("daduishuliang")));
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        COMPONENTS.register(modBus);
    }
}
