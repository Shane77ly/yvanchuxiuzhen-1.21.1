import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

/** 独立读取锁定原版注册方块的材料身份与材料布尔位，不启动游戏或来源 Mod。 */
public final class LegacyMaterialDump {
    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 1) {
            throw new IllegalArgumentException("需要唯一导出文件参数");
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Map<Material, String> identities = new IdentityHashMap<>();
        Map<String, JsonObject> materials = new TreeMap<>();
        for (Field field : Material.class.getFields()) {
            if (field.getType() == Material.class) {
                Material material = (Material) field.get(null);
                if (identities.put(material, field.getName()) != null) {
                    throw new IllegalStateException("材料常量身份重复：" + field.getName());
                }
                JsonObject flags = new JsonObject();
                flags.addProperty("solid", material.isSolid());
                flags.addProperty("liquid", material.isLiquid());
                materials.put(field.getName(), flags);
            }
        }
        Map<String, String> blocks = new TreeMap<>();
        for (Block block : Registry.BLOCK) {
            Material material = block.defaultBlockState().getMaterial();
            String identity = identities.get(material);
            if (identity == null) {
                throw new IllegalStateException("原版方块使用未命名材料：" + Registry.BLOCK.getKey(block));
            }
            blocks.put(Registry.BLOCK.getKey(block).toString(), identity);
        }
        JsonObject result = new JsonObject();
        Gson gson = new Gson();
        result.add("materials", gson.toJsonTree(materials));
        result.add("blocks", gson.toJsonTree(blocks));
        Path target = Path.of(arguments[0]);
        Files.createDirectories(target.getParent());
        Files.writeString(target, new GsonBuilder().setPrettyPrinting().create().toJson(result) + "\n",
                StandardCharsets.UTF_8);
        System.out.println("DONE materials=" + materials.size() + " blocks=" + blocks.size());
    }
}
