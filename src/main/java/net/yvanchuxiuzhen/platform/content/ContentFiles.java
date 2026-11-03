package net.yvanchuxiuzhen.platform.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取类路径中的内容表文件（一个 JSON 数组，一个元素一行），行解码失败或含有 Codec 不认识的字段时报出文件与行号并中止启动。
 * 系统自己的数据表（如功法定义）也经此读取。
 */
public final class ContentFiles {
    private ContentFiles() {
    }

    /** 文件不存在时返回空列表：单位只在有对应内容时才有 blocks.json 或 items.json。 */
    public static <T> List<T> read(String path, Codec<T> codec) {
        try (InputStream stream = ContentFiles.class.getResourceAsStream("/" + path)) {
            if (stream == null) {
                return List.of();
            }
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonArray rows = JsonParser.parseReader(reader).getAsJsonArray();
            List<T> result = new ArrayList<>(rows.size());
            for (int i = 0; i < rows.size(); i++) {
                result.add(decode(path, i + 2, rows.get(i), codec));
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取内容表 " + path, exception);
        }
    }

    private static <T> T decode(String path, int line, JsonElement json, Codec<T> codec) {
        String where = path + " 第 " + line + " 行";
        T row = codec.parse(JsonOps.INSTANCE, json).getOrThrow(message -> new IllegalStateException(where + "：" + message));
        JsonObject encoded = codec.encodeStart(JsonOps.INSTANCE, row).getOrThrow().getAsJsonObject();
        for (String key : json.getAsJsonObject().keySet()) {
            if (!encoded.has(key)) {
                throw new IllegalStateException(where + "：未知字段 " + key);
            }
        }
        return row;
    }
}
