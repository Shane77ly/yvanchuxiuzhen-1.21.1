package net.yvanchuxiuzhen.collection.decor;

import java.util.function.DoubleConsumer;

/** 瓦片神笔的两面屋顶坐标、坡片与屋脊公式；顺序与来源逐层逐列写入一致。 */
final class RoofBrushLayout {
    record Area(int x1, int y, int z1, int x2, int z2, boolean northSouth) { }

    /** facing沿原版水平编号：南0、西1、北2、东3。 */
    @FunctionalInterface
    interface Placement {
        void place(double x, double y, double z, String tile, int facing);
    }

    private RoofBrushLayout() { }

    static void generate(Area area, boolean curved, String tile, Placement output, DoubleConsumer temporary) {
        double length = Math.abs((double) (area.northSouth() ? area.x1() : area.z1())
                - (area.northSouth() ? area.x2() : area.z2()));
        double width = Math.abs((double) (area.northSouth() ? area.z1() : area.x1())
                - (area.northSouth() ? area.z2() : area.x2()));
        boolean ridge = width / 2 - Math.round(width / 2) == 0;
        double rows = ridge ? Math.floor(width / 2) : Math.ceil(width / 2);
        double radius = width * 0.6;
        double start = Math.min(area.northSouth() ? area.x1() : area.z1(), area.northSouth() ? area.x2() : area.z2());
        for (int row = 0; row < (int) rows; row++) {
            double root = Math.sqrt(Math.pow(radius, 2) - Math.pow(row, 2));
            double height = curved ? area.y() - Math.floor(root) + radius : area.y() + row;
            String slope = tile + "_09";
            if (curved && row <= rows * 0.65) {
                double fraction = Math.abs(root) - Math.floor(Math.abs(root));
                if (fraction >= 0 && fraction < 0.25) slope = tile + "_04";
                else if (fraction >= 0.25 && fraction < 0.5) slope = tile + "_03";
                else if (fraction >= 0.5 && fraction < 0.75) slope = tile + "_02";
                else if (fraction >= 0.75 && fraction < 1) slope = tile + "_01";
            }
            for (int column = 0; column < (int) (length + 1); column++) {
                if (area.northSouth()) {
                    output.place(start + column, height, area.z1() - row, slope, 0);
                    output.place(start + column, height, area.z2() + row, slope, 2);
                } else {
                    output.place(area.x1() - row, height, start + column, slope, 3);
                    output.place(area.x2() + row, height, start + column, slope, 1);
                }
            }
        }
        if (ridge) {
            double height = area.y() + rows;
            if (curved) {
                double root = Math.sqrt(Math.pow(radius, 2) - Math.pow(rows, 2));
                double previous = Math.sqrt(Math.pow(radius, 2) - Math.pow(rows - 1, 2));
                temporary.accept(previous);
                if (Math.abs(previous - root) > 1) root = root - 1;
                height = area.y() - root + radius;
            }
            String ridgeTile = (curved ? "tileimbrexblack" : tile) + "_10";
            for (int column = 0; column < (int) (length + 1); column++) {
                if (area.northSouth()) output.place(start + column, height, area.z1() - Math.ceil(width / 2), ridgeTile, 2);
                else output.place(area.x1() - Math.ceil(width / 2), height, start + column, ridgeTile, 3);
            }
        }
    }
}
