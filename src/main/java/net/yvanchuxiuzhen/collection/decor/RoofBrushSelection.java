package net.yvanchuxiuzhen.collection.decor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 瓦片神笔选区两点坐标与临时计算缓存（附着于玩家）。 */
public final class RoofBrushSelection {
    public static final Codec<RoofBrushSelection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("xp1", 0).forGetter(s -> s.xp1),
            Codec.INT.optionalFieldOf("yp1", 0).forGetter(s -> s.yp1),
            Codec.INT.optionalFieldOf("zp1", 0).forGetter(s -> s.zp1),
            Codec.INT.optionalFieldOf("xp2", 0).forGetter(s -> s.xp2),
            Codec.INT.optionalFieldOf("yp2", 0).forGetter(s -> s.yp2),
            Codec.INT.optionalFieldOf("zp2", 0).forGetter(s -> s.zp2),
            Codec.DOUBLE.optionalFieldOf("linshi", 0.0).forGetter(s -> s.temp)
    ).apply(instance, RoofBrushSelection::new));

    private int xp1, yp1, zp1, xp2, yp2, zp2;
    private double temp;

    public RoofBrushSelection() {
    }

    public RoofBrushSelection(int xp1, int yp1, int zp1, int xp2, int yp2, int zp2, double temp) {
        this.xp1 = xp1;
        this.yp1 = yp1;
        this.zp1 = zp1;
        this.xp2 = xp2;
        this.yp2 = yp2;
        this.zp2 = zp2;
        this.temp = temp;
    }

    public int xp1() { return xp1; }
    public int yp1() { return yp1; }
    public int zp1() { return zp1; }
    public int xp2() { return xp2; }
    public int yp2() { return yp2; }
    public int zp2() { return zp2; }
    public double temp() { return temp; }
    public void setTemp(double temp) { this.temp = temp; }

    public void setPoint(int index, int x, int y, int z) {
        if (index == 1) { xp1 = x; yp1 = y; zp1 = z; }
        else { xp2 = x; yp2 = y; zp2 = z; }
    }

    public void swapPoints() {
        int tx = xp1, ty = yp1, tz = zp1;
        xp1 = xp2; yp1 = yp2; zp1 = zp2;
        xp2 = tx; yp2 = ty; zp2 = tz;
    }
}
