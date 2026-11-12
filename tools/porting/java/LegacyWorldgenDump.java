import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** 独立读取1.18.2原版注册表并经来源Codec导出，不启动游戏或加载项目Mod。 */
public final class LegacyWorldgenDump {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) {
            throw new IllegalArgumentException("需要唯一导出目录参数");
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        RegistryAccess registries = RegistryAccess.BUILTIN.get();
        DynamicOps<JsonElement> operations = RegistryOps.create(JsonOps.INSTANCE, registries);
        Path output = Path.of(arguments[0]);
        int configured = dump(output, "configured_feature", registries.ownedRegistryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY),
                ConfiguredFeature.DIRECT_CODEC, operations);
        int placed = dump(output, "placed_feature", registries.ownedRegistryOrThrow(Registry.PLACED_FEATURE_REGISTRY),
                PlacedFeature.DIRECT_CODEC, operations);
        int carvers = dump(output, "configured_carver", registries.ownedRegistryOrThrow(Registry.CONFIGURED_CARVER_REGISTRY),
                ConfiguredWorldCarver.DIRECT_CODEC, operations);
        System.out.println("DONE configured=" + configured + " placed=" + placed + " carvers=" + carvers);
    }

    private static <T> int dump(Path output, String kind, Registry<T> registry, Codec<T> codec,
            DynamicOps<JsonElement> operations) throws Exception {
        int count = 0;
        for (Map.Entry<ResourceKey<T>, T> entry : registry.entrySet()) {
            JsonElement data = codec.encodeStart(operations, entry.getValue()).getOrThrow(false,
                    error -> { throw new IllegalStateException(entry.getKey() + ": " + error); });
            Path target = output.resolve(entry.getKey().location().getNamespace()).resolve("worldgen")
                    .resolve(kind).resolve(entry.getKey().location().getPath() + ".json");
            Files.createDirectories(target.getParent());
            Files.writeString(target, GSON.toJson(data) + "\n", StandardCharsets.UTF_8);
            count++;
        }
        return count;
    }
}
