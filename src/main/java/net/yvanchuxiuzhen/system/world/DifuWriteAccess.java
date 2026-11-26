package net.yvanchuxiuzhen.system.world;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** 将当前区块存储的提交结果交给服务端首次地府放置调用者。 */
public interface DifuWriteAccess {
    void yvanchuxiuzhen$difuWrites(Consumer<CompletableFuture<Void>> receiver);
}
