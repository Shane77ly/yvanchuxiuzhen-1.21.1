package net.yvanchuxiuzhen.platform;

/**
 * 两个来源 Mod 的资源命名空间。合并后只有一个 Mod ID，即 {@link #XIUZHEN}；
 * 原初系统的内容继续使用 {@link #XITONG}，保持来源注册 ID 不变。
 */
public final class Namespaces {
    public static final String XIUZHEN = "yvanchuxiuzhen";
    public static final String XITONG = "yvanchuxitong";

    private Namespaces() {
    }
}
