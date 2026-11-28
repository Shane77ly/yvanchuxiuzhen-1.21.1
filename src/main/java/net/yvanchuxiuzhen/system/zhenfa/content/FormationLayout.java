package net.yvanchuxiuzhen.system.zhenfa.content;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;

/** 五段布局检测依次写控制参数、坐标算式、材料等级和配方；配方定义由 {@code content/zhenfa/formations.json} 驱动。 */
final class FormationLayout {
    private static final int[] X = {
        0, 1, 1, 1, 0, -1, -1, -1, 0, 2, 2, 2, 0, -2, -2, -2,
        0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 2, 1, 0, -1, -2, -3,
        -3, -3, -3, -3, -3, -3, -2, -1, -1, 0, 1, 3, 4, 4, 4, 4,
        4, 4, 4, 3, 1, 0, -1, -3, -4, -4, -4, -4, -4, -4, -4, -3,
        -1, 0, 1, 4, 5, 5, 5, 5, 5, 5, 5, 4, 1, 0, -1, -4,
        -5, -5, -5, -5, -5, -5, -5, -4, -6, -4, -2, 0, 2, 4, 6, 6,
        6, 6, 6, 6, 6, 4, 2, 0, -2, -4, -6, -6, -6, -6, -6, -6,
        -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 6,
        5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -8, -8,
        -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -7, -6, -5,
        -4, -2, -1, 0, 1, 2, 4, 5, 6, 7, 8, 9, 9, 9, 9, 9,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 7, 6, 5, 4, 2,
        1, 0, -1, -2, -4, -5, -6, -7, -8, -9, -9, -9, -9, -9, -9, -9,
        -9, -9, -9, -9, -9, -9, -9, -9
    };
    private static final int[] Z = {
        -1, -1, 0, 1, 1, 1, 0, -1, -2, -2, 0, 2, 2, 2, 0, -2,
        -3, -3, -3, -3, -2, -1, 0, 1, 2, 3, 3, 3, 3, 3, 3, 3,
        2, 1, 0, -1, -2, -3, -3, -3, -4, -4, -4, -4, -4, -3, -1, 0,
        1, 3, 4, 4, 4, 4, 4, 4, 4, 3, 1, 0, -1, -3, -4, -4,
        -5, -5, -5, -5, -5, -4, -1, 0, 1, 4, 5, 5, 5, 5, 5, 5,
        5, 4, 1, 0, -1, -4, -5, -5, -6, -6, -6, -6, -6, -6, -6, -4,
        -2, 0, 2, 4, 6, 6, 6, 6, 6, 6, 6, 4, 2, 0, -2, -4,
        -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -7,
        -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 6, 5,
        4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -9, -9, -9, -9,
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -8, -7, -6, -5, -4,
        -2, -1, 0, 1, 2, 4, 5, 6, 7, 8, 9, 9, 9, 9, 9, 9,
        9, 9, 9, 9, 9, 9, 9, 9, 9, 8, 7, 6, 5, 4, 2, 1,
        0, -1, -2, -4, -5, -6, -7, -8
    };
    private static final float[] ANGLE = {
        -90.0f, -45.0f, 0.0f, 45.0f, 90.0f, 135.0f, -180.0f, -135.0f, -90.0f, -45.0f, 0.0f, 45.0f, 90.0f, 135.0f, -180.0f, -135.0f,
        -90.0f, -75.0f, -60.0f, -45.0f, -30.0f, -15.0f, 0.0f, 15.0f, 30.0f, 45.0f, 60.0f, 75.0f, 90.0f, 105.0f, 120.0f, 135.0f,
        150.0f, 165.0f, -180.0f, -165.0f, -150.0f, -135.0f, -120.0f, -105.0f, -100.0f, -90.0f, -80.0f, -55.0f, -45.0f, -35.0f, -10.0f, 0.0f,
        10.0f, 35.0f, 45.0f, 55.0f, 80.0f, 90.0f, 100.0f, 125.0f, 135.0f, 145.0f, 170.0f, -180.0f, -170.0f, -145.0f, -135.0f, -125.0f,
        -100.0f, -90.0f, -80.0f, -55.0f, -45.0f, -35.0f, -10.0f, 0.0f, 10.0f, 35.0f, 45.0f, 55.0f, 80.0f, 90.0f, 100.0f, 125.0f,
        135.0f, 145.0f, 170.0f, -180.0f, -170.0f, -145.0f, -135.0f, -125.0f, -90.0f, -75.0f, -60.0f, -45.0f, -30.0f, -15.0f, 0.0f, 15.0f,
        30.0f, 45.0f, 60.0f, 75.0f, 90.0f, 105.0f, 120.0f, 135.0f, 150.0f, 165.0f, -180.0f, -165.0f, -150.0f, -135.0f, -120.0f, -105.0f,
        -132.0f, -126.0f, -120.0f, -114.0f, -108.0f, -102.0f, -96.0f, -90.0f, -84.0f, -78.0f, -72.0f, -66.0f, -60.0f, -54.0f, -48.0f, -42.0f,
        -36.0f, -30.0f, -24.0f, -18.0f, -12.0f, -6.0f, 0.0f, 6.0f, 12.0f, 18.0f, 24.0f, 30.0f, 36.0f, 42.0f, 48.0f, 54.0f,
        60.0f, 66.0f, 72.0f, 78.0f, 84.0f, 90.0f, 96.0f, 102.0f, 108.0f, 114.0f, 120.0f, 126.0f, 132.0f, 138.0f, 144.0f, 150.0f,
        156.0f, 162.0f, 168.0f, 174.0f, -180.0f, -174.0f, -168.0f, -162.0f, -156.0f, -150.0f, -144.0f, -138.0f, -132.0f, -126.0f, -120.0f, -114.0f,
        -108.0f, -102.0f, -96.0f, -90.0f, -84.0f, -78.0f, -72.0f, -66.0f, -60.0f, -54.0f, -48.0f, -42.0f, -36.0f, -30.0f, -24.0f, -18.0f,
        -12.0f, -6.0f, 0.0f, 6.0f, 12.0f, 18.0f, 24.0f, 30.0f, 36.0f, 42.0f, 48.0f, 54.0f, 60.0f, 66.0f, 72.0f, 78.0f,
        84.0f, 90.0f, 96.0f, 102.0f, 108.0f, 114.0f, 120.0f, 126.0f, 132.0f, 138.0f, 144.0f, 150.0f, 156.0f, 162.0f, 168.0f, 174.0f,
        -180.0f, -174.0f, -168.0f, -162.0f, -156.0f, -150.0f, -144.0f, -138.0f
    };
    private static final String[] ELEMENTS = {"gold", "water", "tree", "fire", "soil", "yinyang"};
    private static final int[] RANGE = {1, 8, 3, 4, 9, 2, 7, 6};
    private static final int[] HEIGHT = {5, -1, 5, 5, 3, -5, -3, 5, 1, -3, 3, 1, -1, -1, -1, 3, -1, -5, 3, 3, -5, -1, -1, 1};
    private static final int[] NEGATIVE = {3, 5, 4, 0, 2, 1};
    private static final int[][] POSITIVE = {{3, 1}, {5}, {4, 2}, {0, 3}, {2, 0}, {1, 4}};

    private FormationLayout() { }

    static void detect(Level level, BlockPos origin, FormationBaseBlockEntity base) {
        base.resetLayout();
        for (int i = 0; i < 88; i++) {
            if (material(level, origin, i).isEmpty()) continue;
            if (i < 8) {
                base.setTrigram(i, true);
                base.addLimit(5);
            } else if (i < 16) {
                base.addRange(RANGE[i - 8]);
            } else if (i < 40) {
                switch (i) {
                    case 20, 28, 39 -> base.addTriggerMode(1);
                    case 21 -> base.setAbsoluteCoords(true);
                    case 22, 30, 33, 38 -> base.addEffectFlag(1);
                    case 31 -> base.setRandomOffset(true);
                    case 18, 26, 29, 34 -> base.addFollowCaster(1);
                    case 16, 17, 24, 32, 36, 37 -> base.addSightScale(1);
                    case 25, 35 -> base.addTargetMode(1);
                    case 23 -> base.setInspectFlag(true);
                    case 19, 27 -> base.addControlFlag(1);
                    default -> { }
                }
            } else if (i < 64) {
                base.addElement(NEGATIVE[(i - 40) % 6], -1);
            } else {
                for (int element : POSITIVE[(i - 64) % 6]) base.addElement(element, 1);
            }
        }
        ArithmeticState xMath = new ArithmeticState();
        for (int i = 88; i < 232; i++) {
            if (material(level, origin, i).isEmpty()) continue;
            if (i < 112) base.addTriggerY(HEIGHT[i - 88]);
            else if (i < 172) arithmetic(xMath, i - 112);
        }
        base.setTriggerX(finishAxis((int) base.triggerX(), xMath));
        ArithmeticState zMath = new ArithmeticState();
        for (int i = 172; i < 232; i++) {
            if (!material(level, origin, i).isEmpty()) arithmetic(zMath, i - 172);
        }
        base.setTriggerZ(finishAxis((int) base.triggerZ(), zMath));
        materials(level, origin, base);
        recipe(base);
    }

    private static void arithmetic(ArithmeticState state, int index) {
        int group = index / 15;
        int slot = index % 15;
        if (slot < 2) {
            state.op[group] = slot == 0 ? "加" : "乘";
        } else if (slot >= 11) {
            state.combineOp[group] = new String[] {"加", "减", "乘", "除"}[slot - 11];
        } else if (state.op[group].equals("乘")) {
            if (state.values[group] == 0) state.values[group] = 1;
            state.values[group] *= slot - 1;
        } else {
            state.values[group] += slot - 1;
        }
    }

    private static int finishAxis(int current, ArithmeticState state) {
        for (int i = 0; i < 4; i++) {
            String operation = state.combineOp[i];
            if ((operation.equals("乘") || operation.equals("除")) && current == 0) current = 1;
            double result = switch (operation) {
                case "除" -> current / state.values[i];
                case "乘" -> current * state.values[i];
                case "减" -> current - state.values[i];
                default -> current + state.values[i];
            };
            current = (int) result;
        }
        return current;
    }

    private static void materials(Level level, BlockPos origin, FormationBaseBlockEntity base) {
        double[] totals = new double[6];
        double count = 0;
        double levels = 0;
        for (int i = 0; i < 232; i++) {
            ItemStack stack = material(level, origin, i);
            if (stack.isEmpty()) continue;
            FormationElementBlockEntity element = (FormationElementBlockEntity) level.getBlockEntity(origin.offset(X[i], 0, Z[i]));
            element.assignFormation(origin, ANGLE[i]);
            int rank = 1;
            for (int candidate = 9; candidate >= 2; candidate--) {
                if (tagged(stack, "zhenitemlevel_" + candidate)) {
                    rank = candidate;
                    break;
                }
            }
            for (int e = 0; e < 5; e++) {
                if (tagged(stack, "buzhen" + ELEMENTS[e])) {
                    totals[e] += rank;
                    break;
                }
            }
            if (tagged(stack, "buzhenyang")) totals[5] += rank;
            else if (tagged(stack, "buzhenyin")) totals[5] -= rank;
            count++;
            levels += rank;
        }
        boolean valid = true;
        for (int e = 0; e < 5; e++) valid &= totals[e] >= Math.pow(base.elementValue(e), 2);
        int yinYang = base.yinYang();
        valid &= yinYang == 0 || (yinYang > 0 ? totals[5] >= yinYang : totals[5] <= yinYang);
        base.applyLayoutSummary((int) Math.floor(levels / count), (int) Math.ceil(levels / 4), valid);
    }

    private static void recipe(FormationBaseBlockEntity base) {
        StringBuilder code = new StringBuilder();
        for (int e = 0; e < 6; e++) code.append(base.elementValue(e));
        Optional<FormationRecipeDefinition> match = FormationRecipes.match(code.toString());
        if (match.isPresent()) {
            FormationRecipeDefinition recipe = match.get();
            base.applyRecipeResult(recipe.name(), recipe.category(), (int) Math.round(base.powerNeed() * recipe.costMultiplier()));
        } else {
            base.applyRecipeResult("", "", 0);
        }
    }

    private static ItemStack material(Level level, BlockPos origin, int index) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, origin.offset(X[index], 0, Z[index]), null);
        return handler != null && handler.getSlots() > 0 ? handler.getStackInSlot(0) : ItemStack.EMPTY;
    }

    private static boolean tagged(ItemStack stack, String path) {
        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }

    private static final class ArithmeticState {
        final double[] values = new double[4];
        final String[] op = {"", "", "", ""};
        final String[] combineOp = {"", "", "", ""};
    }
}
