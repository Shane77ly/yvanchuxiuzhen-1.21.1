package net.yvanchuxiuzhen.system.zhenfa.content;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public final class FormationBlockIds {
    private FormationBlockIds() { }
    public static String path(BlockState state) { return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath(); }
}
