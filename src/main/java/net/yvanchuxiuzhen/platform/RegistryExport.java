package net.yvanchuxiuzhen.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * 开发运行用的注册表导出：设置系统属性 {@value #PROPERTY} 为目录时，专用服务器启动完成后
 * 把本 Mod 命名空间下的方块、物品、声音事件、状态效果与附魔（数据包定义，取自服务端注册表）ID 写入该目录，供 {@code tools/porting/check_ledger.py} 与台账比对。
 * 发布环境不设置该属性，监听器不登记。
 */
public final class RegistryExport {
    static final String PROPERTY = "yvanchuxiuzhen.exportRegistries";

    private RegistryExport() {
    }

    public static void register() {
        if (System.getProperty(PROPERTY) != null) {
            NeoForge.EVENT_BUS.addListener(RegistryExport::export);
        }
    }

    private static void export(ServerStartedEvent event) {
        Path directory = Path.of(System.getProperty(PROPERTY));
        try {
            Files.createDirectories(directory);
            write(directory.resolve("block.txt"), BuiltInRegistries.BLOCK);
            write(directory.resolve("item.txt"), BuiltInRegistries.ITEM);
            write(directory.resolve("sound_event.txt"), BuiltInRegistries.SOUND_EVENT);
            write(directory.resolve("mob_effect.txt"), BuiltInRegistries.MOB_EFFECT);
            write(directory.resolve("enchantment.txt"), event.getServer().registryAccess().registryOrThrow(Registries.ENCHANTMENT));
        } catch (IOException exception) {
            throw new IllegalStateException("无法写出注册表导出 " + directory, exception);
        }
    }

    private static void write(Path file, Registry<?> registry) throws IOException {
        List<String> ids = registry.keySet().stream()
                .filter(id -> id.getNamespace().equals(ModId.VALUE))
                .map(ResourceLocation::toString)
                .sorted()
                .toList();
        Files.write(file, ids, StandardCharsets.UTF_8);
    }
}
