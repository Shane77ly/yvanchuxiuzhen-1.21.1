package net.yvanchuxiuzhen.collection.festival;

/** 三种方块烟花的注册身份与来源计划刻间隔。 */
public enum BlockFireworkKind {
    FIRECRACKER("pao_10", 1), FOUNTAIN("pao_11", 1), BATTERY("pao_12", 20);

    private final String id;
    private final int interval;

    BlockFireworkKind(String id, int interval) {
        this.id = id;
        this.interval = interval;
    }

    public String id() { return id; }
    public int interval() { return interval; }
}
