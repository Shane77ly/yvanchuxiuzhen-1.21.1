package net.yvanchuxiuzhen.system.economy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.List;

/** 七页经济菜单的来源画布、纹理、控件与槽位，服务端和客户端读取同一份几何。 */
public final class EconomyLayout {
    private static final JsonObject SOURCE = CommerceCatalog.load("screens");
    private static final Gson GSON = new Gson();

    private EconomyLayout() { }

    public static Page page(String sourceName) { return GSON.fromJson(SOURCE.get(sourceName), Page.class); }

    public record Page(int width, int height, List<Layer> layers, List<ImageButton> buttons, List<Label> labels,
                       List<List<Integer>> slots, int inventory_x, int inventory_y) { }
    public record Layer(String texture, int x, int y, int width, int height, int texture_width, int texture_height) { }
    public record ImageButton(String texture, int x, int y, int width, int height, int texture_width, int texture_height, int operation) { }
    public record Label(String procedure, String key, int x, int y, int color) { }
}
