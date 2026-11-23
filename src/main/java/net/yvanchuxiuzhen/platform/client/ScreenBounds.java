package net.yvanchuxiuzhen.platform.client;

/** 判定扩展槽位/附加背景面板界面的鼠标点击是否落在所有面板区域之外。 */
public final class ScreenBounds {
    private ScreenBounds() {
    }

    public static boolean isOutside(double mouseX, double mouseY, int leftPos, int topPos, int width, int height, PanelRect... extraPanels) {
        if (contains(mouseX, mouseY, leftPos, topPos, width, height)) {
            return false;
        }
        for (PanelRect panel : extraPanels) {
            if (contains(mouseX, mouseY, leftPos + panel.x(), topPos + panel.y(), panel.width(), panel.height())) {
                return false;
            }
        }
        return true;
    }

    private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public record PanelRect(int x, int y, int width, int height) {
    }
}
