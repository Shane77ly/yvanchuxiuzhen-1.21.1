package net.yvanchuxiuzhen.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * 开发运行用的注册表导出：设置系统属性 {@value #PROPERTY} 为目录时，开发服务端启动完成后
 * 把本 Mod 命名空间下的方块、物品、声音事件、状态效果、粒子类型、实体类型、菜单类型、附魔与群系 ID 写入该目录，供 {@code tools/porting/check_ledger.py} 与台账比对。
 * 附魔与群系取自服务端加载的数据包注册表；群系出生名单另导出修饰后的全部命名空间。
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
            write(directory.resolve("particle_type.txt"), BuiltInRegistries.PARTICLE_TYPE);
            write(directory.resolve("entity_type.txt"), BuiltInRegistries.ENTITY_TYPE);
            write(directory.resolve("menu.txt"), BuiltInRegistries.MENU);
            write(directory.resolve("enchantment.txt"), event.getServer().registryAccess().registryOrThrow(Registries.ENCHANTMENT));
            Registry<Biome> biomes = event.getServer().registryAccess().registryOrThrow(Registries.BIOME);
            write(directory.resolve("biome.txt"), biomes);
            writeBiomeSpawns(directory.resolve("biome_spawns.tsv"), biomes);
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

    private static void writeBiomeSpawns(Path file, Registry<Biome> biomes) throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("biome\tcategory\tentity\tweight\tmin_count\tmax_count");
        for (ResourceLocation id : biomes.keySet().stream().sorted().toList()) {
            MobSpawnSettings settings = biomes.get(id).getMobSettings();
            for (MobCategory category : MobCategory.values()) {
                for (MobSpawnSettings.SpawnerData spawn : settings.getMobs(category).unwrap()) {
                    rows.add(id + "\t" + category.getName() + "\t" + BuiltInRegistries.ENTITY_TYPE.getKey(spawn.type)
                            + "\t" + spawn.getWeight().asInt() + "\t" + spawn.minCount + "\t" + spawn.maxCount);
                }
            }
        }
        Files.write(file, rows, StandardCharsets.UTF_8);
    }
}
