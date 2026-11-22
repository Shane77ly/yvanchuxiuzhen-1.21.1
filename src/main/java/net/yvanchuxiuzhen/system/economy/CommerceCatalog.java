package net.yvanchuxiuzhen.system.economy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** 来源银行固定商品、对话和基础收购表，按来源顺序保留首个同名条目。 */
public final class CommerceCatalog {
    private static final JsonObject SOURCE = load();
    private static final JsonObject LANGUAGE = load("language_zh_cn");
    private static final JsonObject MATERIAL_SHOP = load("valuation").getAsJsonObject("material_shop");
    private static final Map<String, Double> BASIC_PRICES = prices();

    private CommerceCatalog() { }

    public static String text(String key) { return SOURCE.get(key).getAsString(); }

    /** 来源商品表允许直接文本和语言键；服务端使用同一份锁定中文数据。 */
    public static String translated(String key) { return LANGUAGE.has(key) ? LANGUAGE.get(key).getAsString() : key; }

    public static double basicPrice(String item) { return BASIC_PRICES.getOrDefault(item, 0.0); }
    public static double materialShopPrice(int level) { return MATERIAL_SHOP.get(Integer.toString(level)).getAsDouble(); }

    private static JsonObject load() {
        return load("commerce_catalog");
    }

    static JsonObject load(String file) {
        try (var input = CommerceCatalog.class.getResourceAsStream("/economy/" + file + ".json");
             var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException error) {
            throw new UncheckedIOException("无法读取经济来源表", error);
        }
    }

    private static Map<String, Double> prices() {
        String[] values = text("basic_prices").split("/");
        Map<String, Double> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length / 2; index++) {
            result.putIfAbsent(values[index * 2], Double.parseDouble(values[index * 2 + 1]));
        }
        return Map.copyOf(result);
    }
}
