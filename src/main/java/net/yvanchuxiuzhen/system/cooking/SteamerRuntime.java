package net.yvanchuxiuzhen.system.cooking;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.SteamerBlockEntity;
import net.yvanchuxiuzhen.system.cooking.content.SteamerMenu;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;

/** 蒸锅开始与五次完成、四层笼屉食材处理；设备状态由烹饪系统独占写入。 */
public final class SteamerRuntime {
    private static final String[] CONTAINERS = {"foodwan", "foodcipan", "fooddawan", "foodtiepan"};
    private static final String[] CONTAINER_ERRORS = {"bowl", "plate", "large_bowl", "iron_plate"};

    private SteamerRuntime() {
    }

    /** 来源全局事件只为未烹饪的蒸锅开窗，不取消随后方块或物品使用。 */
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (event.getEntity() instanceof ServerPlayer player
                && event.getLevel().getBlockEntity(event.getPos()) instanceof SteamerBlockEntity vessel
                && !vessel.tray() && !vessel.getPersistentData().getBoolean("cooking")) open(player, vessel);
    }

    public static void used(Level world, BlockPos pos, Player player) {
        if (!(world instanceof ServerLevel level) || !(player instanceof ServerPlayer serverPlayer)
                || !(world.getBlockEntity(pos) instanceof SteamerBlockEntity vessel)) return;
        if (vessel.tray()) {
            vessel.animate(level.isEmptyBlock(pos.above()) ? 1 : 2);
            for (int depth = 1; depth <= 4; depth++) {
                if (level.getBlockEntity(pos.below(depth)) instanceof SteamerBlockEntity below
                        && !below.tray() && below.getPersistentData().getBoolean("cooking")) return;
            }
            open(serverPlayer, vessel);
            return;
        }
        CompoundTag state = vessel.getPersistentData();
        state.putDouble("playercooklevel", Professions.level(player, Profession.COOKING));
        if (state.getBoolean("cooking")) {
            double count = state.getDouble("cooknumber") + 1;
            state.putDouble("cooknumber", count);
            if (count >= 5) {
                level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        15, 0.2, 0.2, 0.2, 0.8);
                level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        5, 0.5, 0.2, 0.5, 0.3);
                state.putBoolean("cooking", false);
                state.putDouble("cooknumber", 0);
                state.putDouble("cookanimation", 0);
                if (state.getDouble("cookexp") >= 1) {
                    Professions.addExperience(serverPlayer, Profession.COOKING, state.getDouble("cookexp"));
                    state.putDouble("cookexp", 0);
                }
            }
        } else {
            vessel.animate(level.isEmptyBlock(pos.above()) ? 1 : 2);
        }
        vessel.changed();
    }

    public static void button(ServerPlayer player, SteamerBlockEntity pot, int button) {
        if (button == 1) {
            for (int line = 1; line <= 5; line++) message(player, "guide_" + line);
        } else if (button == 0) {
            begin(player, pot);
        }
    }

    /** 上层累计检查先于本锅食材和热源判定；失败留下的上层标记沿来源保留。 */
    private static void begin(ServerPlayer player, SteamerBlockEntity pot) {
        CompoundTag state = pot.getPersistentData();
        if (state.getBoolean("cooking")) {
            message(player, "busy");
            return;
        }
        if (pot.getItem(13).isEmpty()) return;
        CompoundTag recipe = CookingRecipes.data(pot.getItem(13));
        if (!recipe.getString("MenuType").equals("蒸锅")) {
            message(player, "wrong_appliance", recipe.getString("MenuType"));
            return;
        }
        if (recipe.getInt("belongTopinjie") > state.getInt("playercooklevel")) {
            message(player, "insufficient_level");
            return;
        }
        if (!hasContainer(player, pot, recipe.getString("needcontainer"))) return;
        int matching = matches(pot, recipe);
        ServerLevel level = player.serverLevel();
        BlockPos pos = pot.getBlockPos();
        int cumulative = 0;
        for (int height = 1; height <= 4; height++) {
            if (!(level.getBlockEntity(pos.above(height)) instanceof SteamerBlockEntity tray) || !tray.tray()) break;
            cumulative += matches(tray, recipe);
            if (cumulative >= 9) {
                tray.getPersistentData().putBoolean("cooking", true);
                tray.changed();
            }
        }
        if (matching < 9) {
            message(player, "missing_ingredients");
            return;
        }
        if (!heated(level.getBlockState(pos.below()))) {
            message(player, "missing_heat");
            return;
        }
        state.putBoolean("cooking", true);
        player.closeContainer();
        state.putInt("cookanimation", recipe.getInt("cookanimation"));
        consume(pot, recipe);
        ItemStack output = CookingRecipes.stack(recipe.getString("Menuid").toLowerCase(Locale.ROOT));
        pot.setItem(4, output.copy());
        double experience = Math.pow(state.getInt("belongToLevel"), 2) + 5;
        double extra = 0;
        for (int height = 1; height <= 4; height++) {
            if (!(level.getBlockEntity(pos.above(height)) instanceof SteamerBlockEntity tray) || !tray.tray()
                    || !tray.getPersistentData().getBoolean("cooking")) continue;
            consume(tray, recipe);
            tray.setItem(4, output.copy());
            extra += experience;
            tray.getPersistentData().putBoolean("cooking", false);
            tray.changed();
        }
        state.putInt("cookexp", (int) (experience + extra));
        pot.changed();
        message(player, "started");
    }

    private static boolean hasContainer(ServerPlayer player, SteamerBlockEntity pot, String identity) {
        if (identity.isEmpty() || identity.equals("无")) return true;
        for (int index = 0; index < CONTAINERS.length; index++) {
            if (normalize(identity).equals(ModId.VALUE + ":" + CONTAINERS[index])) {
                if (pot.getItem(index + 9).is(CookingRecipes.stack(identity).getItem())) return true;
                message(player, "container_" + CONTAINER_ERRORS[index]);
                return false;
            }
        }
        return false;
    }

    private static int matches(SteamerBlockEntity vessel, CompoundTag recipe) {
        int count = 0;
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            String identity = recipe.getString("cailiao0" + ingredient);
            if (identity.isEmpty() || find(vessel, identity) >= 0) count++;
        }
        return count;
    }

    private static void consume(SteamerBlockEntity vessel, CompoundTag recipe) {
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            String identity = recipe.getString("cailiao0" + ingredient);
            if (!identity.isEmpty()) {
                int slot = find(vessel, identity);
                if (slot >= 0) vessel.removeItem(slot, 1);
            }
        }
    }

    private static int find(SteamerBlockEntity vessel, String identity) {
        for (int slot = 0; slot < 9; slot++) {
            if (BuiltInRegistries.ITEM.getKey(vessel.getItem(slot).getItem()).toString().equals(normalize(identity))) return slot;
        }
        return -1;
    }

    private static String normalize(String identity) {
        return identity.startsWith("yvanchuxitong:") ? ModId.VALUE + identity.substring("yvanchuxitong".length()) : identity;
    }

    private static boolean heated(BlockState state) {
        boolean lit = (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.FURNACE) || state.is(Blocks.SMOKER)) && state.getValue(BlockStateProperties.LIT);
        return lit || state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.LAVA_CAULDRON)
                || state.is(BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "blockfire")));
    }

    private static void open(ServerPlayer player, SteamerBlockEntity vessel) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new SteamerMenu(id, inventory, vessel),
                vessel.getDisplayName()), vessel.getBlockPos());
    }

    private static void message(ServerPlayer player, String key, Object... arguments) {
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cooking.steamer." + key, arguments), false);
    }
}
