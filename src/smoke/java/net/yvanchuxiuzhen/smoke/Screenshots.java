package net.yvanchuxiuzhen.smoke;

/** 系统检查用的截图入口，由 SmokeRun 提供，文件写入 build/smoke/screenshots。 */
interface Screenshots {
    void take(String name);
}
