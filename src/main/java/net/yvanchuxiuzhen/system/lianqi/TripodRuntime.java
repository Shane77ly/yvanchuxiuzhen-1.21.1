package net.yvanchuxiuzhen.system.lianqi;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.content.TripodHelp;
import net.yvanchuxiuzhen.system.lianqi.content.TripodMenu;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.yihuo.api.FireExhaustedEvent;
import net.yvanchuxiuzhen.system.yihuo.api.FireHeatEvent;

/** 炼器鼎每二十刻消费灵力，按来源顺序分派制作、升级、熔矿、精炼与延迟爆炸。 */
public final class TripodRuntime {
    private static final String[] NAMES = {"四足青纹鼎", "黄铜小鼎", "黑方鼎", "弘樽鼎", "玄靛鼎", "棱柱鼎", "金纹鼎", "重阳金鼎"};
    private static final String[] EQUIPMENT_TAGS = {"wuqi", "fabao", "toukui", "xiongjia", "kuzi", "zhanxve"};
    private static final List<String> REFINING_INPUTS = List.of("cailiao_201", "cailiao_203", "cailiao_211", "cailiao_213");
    private static final List<String> REFINING_OUTPUTS = List.of("cailiao_202melt", "cailiao_204melt", "cailiao_212melt", "cailiao_214melt");

    private TripodRuntime() {
    }

    public static void initialize(TripodBlockEntity furnace) {
        CompoundTag state = furnace.getPersistentData();
        state.putString("TripodName", NAMES[furnace.tier() - 1]);
        state.putInt("TripodLevel", furnace.tier());
        state.putInt("TripodFire", furnace.tier());
        state.putInt("TripodPurification", furnace.tier() - 1);
        state.putString("lianqiwenzi", "升级");
        state.putBoolean("lianqiBegin", false);
        state.putInt("lianpower", 0);
        if (furnace.tier() <= 7) {
            Direction facing = furnace.getBlockState().getBlock().getStateDefinition().getProperty("facing") instanceof DirectionProperty property
                    ? furnace.getBlockState().getValue(property) : Direction.NORTH;
            state.putInt("fangxiang", switch (facing) {
                case NORTH -> 180;
                case SOUTH -> 0;
                case EAST -> -90;
                default -> 90;
            });
        }
        furnace.changed();
    }

    /** 先打开当前坐标菜单，再刷新共享职业字段；运行中的炉次同样允许覆盖该字段。 */
    public static void use(Level level, BlockPos pos, Player player, TripodBlockEntity furnace) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new TripodMenu(id, inventory, furnace),
                    Component.literal("TripodGUI")), pos);
            furnace.getPersistentData().putInt("RefinerLEVEL", (int) Professions.level(player, Profession.REFINING));
            furnace.changed();
        }
    }

    public static void heat(FireHeatEvent event) {
        if (event.level().getBlockEntity(event.pos()) instanceof TripodBlockEntity furnace) {
            CompoundTag state = furnace.getPersistentData();
            state.putInt("lianpower", (int) (state.getInt("lianpower") + event.amount()));
            furnace.changed();
        }
    }

    public static void exhausted(FireExhaustedEvent event) {
        if (event.level().getBlockEntity(event.pos()) instanceof TripodBlockEntity furnace) {
            CompoundTag state = furnace.getPersistentData();
            if (state.getInt("lianpower") >= 1) {
                state.putBoolean("gengxin", !state.getBoolean("gengxin"));
                furnace.changed();
                event.retain();
            }
        }
    }

    public static void button(ServerPlayer player, TripodBlockEntity furnace, int button) {
        switch (button) {
            case 0 -> begin(player, furnace);
            case 1 -> TripodRecipes.load(furnace, player);
            case 2 -> TripodRecipes.save(furnace, player);
            case 7 -> TripodRecipes.claim(furnace, player);
            default -> TripodHelp.show(player, button);
        }
        furnace.changed();
    }

    private static void begin(ServerPlayer player, TripodBlockEntity furnace) {
        CompoundTag state = furnace.getPersistentData();
        if (state.getBoolean("lianqiBegin")) {
            message(player, "§4正在炼制");
            return;
        }
        if (furnace.getItem(10).isEmpty() && equipmentType(furnace.getItem(0)) >= 0) {
            if (!player.serverLevel().getGameRules().getBoolean(LianqiRules.WEAPON_LEVEL_UP)) {
                message(player, "§4规则禁止升级。");
            } else if (!hasMaterial(furnace)) {
                message(player, "§4至少需要一个炼器材料。");
            } else if (Equipment.refiningMaterials(furnace.getItem(0)).getString("fabaotype").isEmpty()) {
                message(player, "§4此物并不是由炼器鼎制作，也没有炼制痕迹，无法为其升级。");
            } else {
                message(player, "开始炼制");
                state.putBoolean("lianqiBegin", true);
                TripodMaterials.calculate(furnace, true);
            }
        } else if (furnace.getItem(10).isEmpty()) {
            if (furnace.getItem(9).getCount() >= 64) {
                message(player, "§4请先拿走已炼制好的物品。");
                return;
            }
            for (int slot = 0; slot <= 8; slot++) {
                if (!smelted(player.serverLevel(), furnace.getItem(slot)).isEmpty()) {
                    state.putBoolean("lianqiBegin", true);
                    state.putInt("lianqiTickMax", 3);
                    state.putString("lianqiwenzi", "矿石");
                    return;
                }
            }
            ItemStack output = furnace.getItem(9);
            if (!output.isEmpty() && !REFINING_OUTPUTS.stream().anyMatch(name -> itemId(output).equals(ModId.VALUE + ":" + name))) {
                message(player, "§4请先拿走已炼制好的物品。");
                return;
            }
            int count = 0;
            for (String input : REFINING_INPUTS) count += count(furnace, ModId.VALUE + ":" + input);
            if (count >= 4) {
                state.putBoolean("lianqiBegin", true);
                state.putInt("lianqiTickMax", 5);
                state.putString("lianqiwenzi", "精炼");
            }
        } else {
            CompoundTag recipe = TripodMaterials.data(furnace.getItem(10));
            if (recipe.getString("fabaoid").isEmpty()) return;
            int cursor = 0;
            for (int index = 0; index < 9; index++) {
                ItemStack stack = furnace.getItem(cursor);
                String required = recipe.getString("cailiao0" + cursor);
                if (itemId(stack).equals(required)) {
                    if (stack.getCount() >= recipe.getInt("cailiao0" + cursor + "num")) cursor++;
                    else message(player, "§4材料" + (cursor + 1) + "的数量小于图纸内记录材料数量");
                } else if (required.isEmpty() || required.equals("空")) cursor++;
                else {
                    message(player, "§4材料" + (cursor + 1) + "与图纸内记录材料不同");
                    break;
                }
            }
            if (cursor >= 9) {
                message(player, "开始炼制");
                state.putBoolean("lianqiBegin", true);
                TripodMaterials.calculate(furnace, false);
            }
        }
    }

    public static void tick(ServerLevel level, TripodBlockEntity furnace) {
        CompoundTag state = furnace.getPersistentData();
        BlockPos pos = furnace.getBlockPos();
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (state.getInt("firepower") >= 1) {
            level.sendParticles(ParticleTypes.LAVA, x + .5, y + .5, z + .5, 5, 0, 0, 0, 1);
            state.putInt("firepowertick", state.getInt("firepowertick") + 1);
            if (state.getInt("firepowertick") >= 12000) {
                state.putInt("firepowertick", 0);
                state.putInt("firepower", 0);
            }
        }
        if (!state.getBoolean("lianqiBegin")) {
            String type = TripodMaterials.data(furnace.getItem(10)).getString("fabaotype");
            state.putString("lianqiwenzi", type.isEmpty() ? "升级" : type);
        }
        if (state.getBoolean("lianqiBegin") && state.getInt("lianpower") >= state.getInt("TripodLevel")) {
            level.sendParticles(ParticleTypes.FLAME, x + .5, y, z + .5, 10, 1, .2, 1, 0);
            state.putInt("lianqiTick", state.getInt("lianqiTick") + 1 + state.getInt("firepower"));
            state.putInt("lianpower", state.getInt("lianpower") - state.getInt("TripodLevel"));
            if (state.getInt("lianqiTick") >= state.getInt("lianqiTickMax")) {
                switch (state.getString("lianqiwenzi")) {
                    case "矿石" -> smelt(level, furnace);
                    case "精炼" -> purify(furnace);
                    default -> {
                        if (state.getBoolean("lianqishibai")) {
                            level.sendParticles(ParticleTypes.LARGE_SMOKE, x + .5, y + .5, z + .5, 30, .2, .2, .2, 1);
                            ItemStack ash = new ItemStack(Items.GUNPOWDER);
                            ash.set(DataComponents.CUSTOM_NAME, Component.literal("灰烬"));
                            furnace.setItem(9, ash);
                        } else furnace.setItem(9, ItemStack.parseOptional(level.registryAccess(), state.getCompound("item")));
                        furnace.setItem(11, ItemStack.EMPTY);
                        finish(state);
                    }
                }
            }
            double progress = (double) state.getInt("lianqiTick") / state.getInt("lianqiTickMax") * 100;
            state.putInt("lianzhijindu", (int) Math.ceil(progress));
        } else if (state.getBoolean("lianqiBegin")) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x + .5, y, z + .5, 5, .5, .2, .5, .1);
        }
        if (state.getInt("zhadingtick") >= 1) {
            state.putInt("zhadingtick", state.getInt("zhadingtick") - 1);
            if (state.getInt("zhadingtick") <= 0) explode(level, pos, state.getInt("zhadinglevel"));
        }
        if (!furnace.isRemoved()) furnace.changed();
    }

    private static ItemStack smelted(ServerLevel level, ItemStack input) {
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level)
                .map(recipe -> recipe.value().getResultItem(level.registryAccess()).copyWithCount(1)).orElse(ItemStack.EMPTY);
    }

    /** 熔矿每次只消费和产出一件；结果已有物品时只按物品身份匹配并保留旧实例数据。 */
    private static void smelt(ServerLevel level, TripodBlockEntity furnace) {
        ItemStack output = furnace.getItem(9);
        if (output.getCount() < 64) {
            for (int slot = 0; slot <= 8; slot++) {
                ItemStack result = smelted(level, furnace.getItem(slot));
                if (!result.isEmpty() && (output.isEmpty() || result.is(output.getItem()))) {
                    if (!output.isEmpty()) result = output.copyWithCount(output.getCount() + 1);
                    furnace.setItem(9, result);
                    furnace.getItem(slot).shrink(1);
                    furnace.getPersistentData().putInt("lianqiTick", 0);
                    return;
                }
            }
        }
        finish(furnace.getPersistentData());
    }

    private static void purify(TripodBlockEntity furnace) {
        ItemStack output = furnace.getItem(9);
        if (output.getCount() >= 64) {
            finish(furnace.getPersistentData());
            return;
        }
        int selected = -1;
        for (int index = 0; index < REFINING_OUTPUTS.size(); index++) {
            if (itemId(output).equals(ModId.VALUE + ":" + REFINING_OUTPUTS.get(index))) {
                selected = index;
                break;
            }
        }
        if (selected >= 0) {
            if (count(furnace, ModId.VALUE + ":" + REFINING_INPUTS.get(selected)) < 4) selected = -1;
        } else {
            for (int index = 0; index < REFINING_INPUTS.size(); index++) {
                if (count(furnace, ModId.VALUE + ":" + REFINING_INPUTS.get(index)) >= 4) {
                    selected = index;
                    break;
                }
            }
        }
        if (selected < 0) {
            finish(furnace.getPersistentData());
            return;
        }
        furnace.setItem(9, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                REFINING_OUTPUTS.get(selected))), output.getCount() + 1));
        int remaining = 4;
        String input = ModId.VALUE + ":" + REFINING_INPUTS.get(selected);
        for (int slot = 0; slot <= 8 && remaining >= 1; slot++) {
            ItemStack stack = furnace.getItem(slot);
            if (itemId(stack).equals(input)) {
                int consumed = Math.min(remaining, stack.getCount());
                stack.shrink(consumed);
                remaining -= consumed;
            }
        }
        furnace.getPersistentData().putInt("lianqiTick", 0);
    }

    private static int count(TripodBlockEntity furnace, String id) {
        int count = 0;
        for (int slot = 0; slot <= 8; slot++) if (itemId(furnace.getItem(slot)).equals(id)) count += furnace.getItem(slot).getCount();
        return count;
    }

    private static void finish(CompoundTag state) {
        state.putBoolean("lianqiBegin", false);
        state.putInt("lianqiTick", 0);
        state.putInt("lianqiTickMax", 0);
    }

    /** 来源 DESTROY 直接采用爆炸掉落衰减，不额外经过生物破坏或新增世界规则分派。 */
    private static void explode(ServerLevel level, BlockPos pos, int radius) {
        Explosion explosion = new Explosion(level, null, pos.getX(), pos.getY(), pos.getZ(), radius, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY);
        if (EventHooks.onExplosionStart(level, explosion)) return;
        explosion.explode();
        explosion.finalizeExplosion(false);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 4096) {
                player.connection.send(new ClientboundExplodePacket(pos.getX(), pos.getY(), pos.getZ(), radius,
                        explosion.getToBlow(), explosion.getHitPlayers().get(player), explosion.getBlockInteraction(),
                        explosion.getSmallExplosionParticles(), explosion.getLargeExplosionParticles(), explosion.getExplosionSound()));
            }
        }
    }

    static int equipmentType(ItemStack stack) {
        for (int index = 0; index < EQUIPMENT_TAGS.length; index++) if (tagged(stack, EQUIPMENT_TAGS[index])) return index;
        return -1;
    }

    static boolean hasMaterial(TripodBlockEntity furnace) {
        for (int slot = 1; slot <= 8; slot++) if (tagged(furnace.getItem(slot), "cailiao")) return true;
        return false;
    }

    static boolean tagged(ItemStack stack, String tag) {
        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, tag)));
    }

    static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }
}
