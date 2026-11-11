package net.yvanchuxiuzhen.system.world.tree;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 七个来源群系内联树配置使用的十六个独立装饰器类型。 */
public final class WorldTreeDecorators {
    private static final DeferredRegister<TreeDecoratorType<?>> TYPES =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, ModId.VALUE);

    static {
        hanging("biomeautumn_03_tree_leave_decorator", modState("willowdrooping"));
        trunk("biomeautumn_03_tree_trunk_decorator", modState("willowdrooping"));
        fruit("biomejade_02_tree_fruit_decorator", modState("toothedlogtop"));
        hanging("biomejade_02_tree_leave_decorator", Blocks.VINE::defaultBlockState);
        trunk("biomejade_02_tree_trunk_decorator", Blocks.VINE::defaultBlockState);
        hanging("biomejade_03_tree_leave_decorator", modState("jadebambooleave"));
        trunk("biomejade_03_tree_trunk_decorator", modState("jadebambooleave"));
        fruit("biomesand_01_tree_fruit_decorator", modState("ancienttimeswood"));
        hanging("biometiger_04_tree_leave_decorator", Blocks.VINE::defaultBlockState);
        trunk("biometiger_04_tree_trunk_decorator", Blocks.VINE::defaultBlockState);
        fruit("biomewind_02_tree_fruit_decorator", Blocks.AIR::defaultBlockState);
        hanging("biomewind_02_tree_leave_decorator", Blocks.VINE::defaultBlockState);
        trunk("biomewind_02_tree_trunk_decorator", Blocks.VINE::defaultBlockState);
        fruit("biomewinter_03_tree_fruit_decorator", Blocks.ICE::defaultBlockState);
        hanging("biomewinter_03_tree_leave_decorator", Blocks.ICE::defaultBlockState);
        trunk("biomewinter_03_tree_trunk_decorator", Blocks.ICE::defaultBlockState);
    }

    private WorldTreeDecorators() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }

    private static void hanging(String id, Supplier<BlockState> state) {
        TYPES.register(id, () -> new HangingTreeDecorator(state).registeredType());
    }

    private static void trunk(String id, Supplier<BlockState> state) {
        TYPES.register(id, () -> new TrunkReplacementTreeDecorator(state).registeredType());
    }

    private static void fruit(String id, Supplier<BlockState> state) {
        TYPES.register(id, () -> new LowerTrunkFruitDecorator(state).registeredType());
    }

    private static Supplier<BlockState> modState(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        return () -> BuiltInRegistries.BLOCK.get(id).defaultBlockState();
    }
}
