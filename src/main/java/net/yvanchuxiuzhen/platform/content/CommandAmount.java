package net.yvanchuxiuzhen.platform.content;

import java.text.DecimalFormat;

/** 来源将格式化数值作为命令参数再按 Java 浮点数读取；无法识别的本地化符号使该项为零。 */
public final class CommandAmount {
    private CommandAmount() { }

    public static double formatted(DecimalFormat format, double value) {
        try {
            return Double.parseDouble(format.format(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
