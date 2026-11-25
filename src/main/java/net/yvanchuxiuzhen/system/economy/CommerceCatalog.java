package net.yvanchuxiuzhen.system.economy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** 经济系统的估价、基础收购与随机材料单价表读取入口（全 JSON 结构化）。 */
public final class CommerceCatalog {
    private static final JsonObject VALUATION = load("valuation");
    private static final JsonObject MATERIAL_SHOP = VALUATION.getAsJsonObject("material_shop");
    private static final Map<String, Double> BASIC_PRICES = prices();

    private CommerceCatalog() { }

    public static double basicPrice(String item) { return BASIC_PRICES.getOrDefault(item, 0.0); }
    public static double materialShopPrice(int level) { return MATERIAL_SHOP.get(Integer.toString(level)).getAsDouble(); }

    static JsonObject load(String file) {
        try (var input = CommerceCatalog.class.getResourceAsStream("/economy/" + file + ".json");
             var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException error) {
            throw new UncheckedIOException("无法读取经济来源表", error);
        }
    }

    private static Map<String, Double> prices() {
        JsonObject table = VALUATION.getAsJsonObject("basic_prices");
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : table.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAsDouble());
        }
        return Map.copyOf(result);
    }
}
