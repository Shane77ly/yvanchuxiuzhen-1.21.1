package net.yvanchuxiuzhen.system.zhenfa.content;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;

/** 五段布局检测依次写控制参数、坐标算式、材料等级和配方；各段保留来源的扫描区间。 */
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
    private static final String[] TRIGRAMS = {"kun", "zhen", "li", "dui", "qian", "xvn", "kan", "gen"};
    private static final int[] RANGE = {1, 8, 3, 4, 9, 2, 7, 6};
    private static final int[] HEIGHT = {5, -1, 5, 5, 3, -5, -3, 5, 1, -3, 3, 1, -1, -1, -1, 3, -1, -5, 3, 3, -5, -1, -1, 1};
    private static final int[] NEGATIVE = {3, 5, 4, 0, 2, 1};
    private static final int[][] POSITIVE = {{3, 1}, {5}, {4, 2}, {0, 3}, {2, 0}, {1, 4}};

    private FormationLayout() { }

    static void detect(Level level, BlockPos origin, CompoundTag data) {
        reset(data);
        for (int i = 0; i < 88; i++) {
            if (material(level, origin, i).isEmpty()) continue;
            if (i < 8) {
                data.putBoolean("zhen" + TRIGRAMS[i], true);
                add(data, "zhenshangxian", 5);
            } else if (i < 16) {
                add(data, "zhenjvli", RANGE[i - 8]);
            } else if (i < 40) {
                switch (i) {
                    case 20, 28, 39 -> add(data, "zhenchufa", 1);
                    case 21 -> data.putBoolean("zhenzuobiao", true);
                    case 22, 30, 33, 38 -> add(data, "zhentexiao", 1);
                    case 31 -> data.putBoolean("zhensuiji", true);
                    case 18, 26, 29, 34 -> add(data, "zhenqian", 1);
                    case 16, 17, 24, 32, 36, 37 -> add(data, "zhenshijiao", 1);
                    case 25, 35 -> add(data, "zhenmubiao", 1);
                    case 23 -> data.putBoolean("zhenjiance", true);
                    case 19, 27 -> add(data, "zhenkongzhi", 1);
                    default -> { }
                }
            } else if (i < 64) {
                add(data, "zhen" + ELEMENTS[NEGATIVE[(i - 40) % 6]], -1);
            } else {
                for (int element : POSITIVE[(i - 64) % 6]) add(data, "zhen" + ELEMENTS[element], 1);
            }
        }
        double[] values = new double[4];
        for (int i = 88; i < 232; i++) {
            if (material(level, origin, i).isEmpty()) continue;
            if (i < 112) add(data, "zhenchufaY", HEIGHT[i - 88]);
            else if (i < 172) arithmetic(data, values, i - 112);
        }
        finishAxis(data, values, "X");
        values = new double[4];
        for (int i = 172; i < 232; i++) {
            if (!material(level, origin, i).isEmpty()) arithmetic(data, values, i - 172);
        }
        finishAxis(data, values, "Z");
        materials(level, origin, data);
        recipe(data);
    }

    private static void reset(CompoundTag data) {
        for (String trigram : TRIGRAMS) data.putBoolean("zhen" + trigram, false);
        for (String name : new String[] {"zhenshangxian", "zhenjvli", "zhenchufa", "zhentexiao", "zhenqian",
                "zhenshijiao", "zhenmubiao", "zhenkongzhi", "zhenchufaX", "zhenchufaY", "zhenchufaZ"}) data.putInt(name, 0);
        for (String name : new String[] {"zhenzuobiao", "zhensuiji", "zhenjiance"}) data.putBoolean(name, false);
        for (String element : ELEMENTS) data.putInt("zhen" + element, 0);
        for (int i = 1; i <= 4; i++) {
            data.putString("jisuan" + i, "");
            data.putString("jisuanzong" + i, "");
        }
    }

    private static void arithmetic(CompoundTag data, double[] values, int index) {
        int group = index / 15;
        int slot = index % 15;
        String operation = "jisuan" + (group + 1);
        if (slot < 2) {
            data.putString(operation, slot == 0 ? "加" : "乘");
        } else if (slot >= 11) {
            data.putString("jisuanzong" + (group + 1), new String[] {"加", "减", "乘", "除"}[slot - 11]);
        } else if (data.getString(operation).equals("乘")) {
            if (values[group] == 0) values[group] = 1;
            values[group] *= slot - 1;
        } else {
            values[group] += slot - 1;
        }
    }

    private static void finishAxis(CompoundTag data, double[] values, String axis) {
        String key = "zhenchufa" + axis;
        for (int i = 0; i < 4; i++) {
            int current = data.getInt(key);
            String operation = data.getString("jisuanzong" + (i + 1));
            if ((operation.equals("乘") || operation.equals("除")) && current == 0) current = 1;
            double result = switch (operation) {
                case "除" -> current / values[i];
                case "乘" -> current * values[i];
                case "减" -> current - values[i];
                default -> current + values[i];
            };
            data.putInt(key, (int) result);
        }
    }

    private static void materials(Level level, BlockPos origin, CompoundTag data) {
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
        for (int e = 0; e < 5; e++) valid &= totals[e] >= Math.pow(data.getInt("zhen" + ELEMENTS[e]), 2);
        int yinYang = data.getInt("zhenyinyang");
        valid &= yinYang == 0 || (yinYang > 0 ? totals[5] >= yinYang : totals[5] <= yinYang);
        data.putInt("zhenLEVEL", (int) Math.floor(levels / count));
        data.putInt("powerneed", (int) Math.ceil(levels / 4));
        data.putBoolean("zhenfazhengqve", valid);
    }

    private static void recipe(CompoundTag data) {
        StringBuilder code = new StringBuilder();
        for (String element : ELEMENTS) code.append(data.getInt("zhen" + element));
        String name = "";
        String category = "";
        double multiplier = 0;
        String key = code.toString();
        for (int stage = 1; stage <= 4; stage++) {
            if (key.equals("00000" + stage)) {
                category = "阳";
                name = category + stage;
                multiplier = new double[] {0.5, 0.5, 1, 2}[stage - 1];
            } else if (key.equals("00000-" + stage)) {
                category = "阴";
                name = category + stage;
                multiplier = new double[] {0.1, 0.5, 1.2, 1.5}[stage - 1];
            }
        }
        String[] spaceCodes = {"060040", "002640", "222220", "333330", "555554"};
        for (int i = 0; i < spaceCodes.length; i++) {
            if (key.equals(spaceCodes[i])) {
                category = "空间";
                name = category + (i + 1);
                multiplier = new double[] {0.1, 0.2, 0.1, 2, 8}[i];
            }
        }
        for (int e = 0; e < 5; e++) {
            for (int stage = 1; stage <= 4; stage++) {
                StringBuilder pure = new StringBuilder("000000");
                pure.setCharAt(e, (char) ('0' + stage * 2));
                if (key.contentEquals(pure)) {
                    category = new String[] {"金", "水", "木", "火", "土"}[e];
                    name = category + stage;
                    if (e == 3 && stage == 2) category = "金";
                }
            }
        }
        data.putString("jinengname", name);
        data.putString("jinengleibie", category);
        data.putInt("powerneed", (int) Math.round(data.getInt("powerneed") * multiplier));
    }

    private static ItemStack material(Level level, BlockPos origin, int index) {
        BlockPos pos = origin.offset(X[index], 0, Z[index]);
        if (!(level.getBlockState(pos).getBlock() instanceof FormationElementBlock)) return ItemStack.EMPTY;
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        ItemStack stack = handler == null ? ItemStack.EMPTY : handler.getStackInSlot(0).copy();
        return tagged(stack, "buzhencailiao") ? stack : ItemStack.EMPTY;
    }

    private static boolean tagged(ItemStack stack, String path) {
        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }

    private static void add(CompoundTag data, String key, int delta) { data.putInt(key, data.getInt(key) + delta); }
}
