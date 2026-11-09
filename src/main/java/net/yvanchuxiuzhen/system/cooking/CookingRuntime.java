package net.yvanchuxiuzhen.system.cooking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockEntity;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotMenu;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;

/** 两锅在事件阶段开窗，在方块使用阶段记录操作者等级并推进五次翻炒。 */
public final class CookingRuntime {
    private CookingRuntime() {
    }

    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (event.getEntity() instanceof ServerPlayer player
                && event.getLevel().getBlockEntity(event.getPos()) instanceof CookingPotBlockEntity pot
                && !pot.getPersistentData().getBoolean("cooking")) {
            player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new CookingPotMenu(id, inventory, pot),
                    pot.getDisplayName()), event.getPos());
        }
    }

    public static void used(Level world, BlockPos pos, Player player) {
        if (!(world instanceof ServerLevel level) || !(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot)) return;
        CompoundTag data = pot.getPersistentData();
        data.putDouble("playercooklevel", Professions.level(player, Profession.COOKING));
        if (data.getBoolean("cooking")) {
            if (pot.iron()) {
                double animation = data.getDouble("cookanimation");
                for (int number = 1; number <= 9; number++) if (animation == number) pot.animate(number);
            } else {
                pot.animate(2);
                particles(level, pos, ParticleTypes.CLOUD, 12, 0.2);
                particles(level, pos, ParticleTypes.CAMPFIRE_COSY_SMOKE, 4, 0);
            }
            double count = data.getDouble("cooknumber") + 1;
            data.putDouble("cooknumber", count);
            if (count >= 5) {
                if (pot.iron()) {
                    particles(level, pos, ParticleTypes.FLAME, 20, 0.8);
                    particles(level, pos, ParticleTypes.CLOUD, 15, 0.3);
                    particles(level, pos, ParticleTypes.WAX_OFF, 10, 1);
                }
                data.putBoolean("cooking", false);
                data.putDouble("cooknumber", 0);
                data.putDouble("cookanimation", 0);
                if (data.getDouble("cookexp") >= 1) {
                    Professions.addExperience(serverPlayer, Profession.COOKING, data.getDouble("cookexp"));
                    data.putDouble("cookexp", 0);
                }
            } else if (pot.iron()) {
                if (count == 4) {
                    particles(level, pos, ParticleTypes.FLAME, 15, 0);
                    particles(level, pos, ParticleTypes.CLOUD, 12, 0.2);
                } else if (count == 3) {
                    particles(level, pos, ParticleTypes.FLAME, 15, 0);
                    particles(level, pos, ParticleTypes.CLOUD, 4, 0);
                } else if (count == 2) particles(level, pos, ParticleTypes.FLAME, 10, 0);
                else if (count == 1) particles(level, pos, ParticleTypes.FLAME, 5, 0);
            }
        } else if (!pot.iron()) pot.animate(1);
        pot.changed();
    }

    public static void button(ServerPlayer player, CookingPotBlockEntity pot, int button) {
        if (button == 1) {
            if (pot.iron()) {
                message(player, "请把铁锅放置在点燃的熔炉、烟熏炉上，或者直接放置在营火或火焰上，这样铁锅才会正常工作。");
                message(player, "如果您有食谱，请放入右边的食谱物品栏中，并按照食谱显示的材料来放置食材。");
                message(player, "左边还需要放入需要的容器，碗、盘子、大碗、铁盘要放入对应位置。");
                message(player, "最后点击中间的烹饪按钮，然后不断右键铁锅完成烹饪。");
            }
        } else if (button == 2) {
            message(player, (pot.iron() ? "铁锅" : "砂锅") + "不仅可以烹饪多种美食，也可以用来制作画符所需墨的料坯，具体配方如下：");
            String[] grades = {"黄", "玄", "地", "天"};
            for (int grade = 0; grade < grades.length; grade++) {
                String range = (grade * 10 + 1) + "到" + (grade * 10 + 10);
                message(player, grades[grade] + "阶料坯需要一个壳类或骨类的" + range + "级妖兽材料，一个皮类或鳞类的"
                        + range + "级妖兽材料，一个爪类或牙类的" + range + "级妖兽材料，烟灰三捧以及朱砂三份；");
            }
            message(player, "制作好的料坯，可以在工作台处切割成墨。");
        } else if (button == 0) {
            if (pot.getPersistentData().getBoolean("cooking")) message(player, "§4正在烹饪！");
            else if (!pot.getItem(13).isEmpty()) CookingRecipes.begin(player, pot);
            else if (heated(player, pot)) InkBlankCooking.begin(player, pot);
        }
    }

    static boolean heated(ServerPlayer player, CookingPotBlockEntity pot) {
        BlockState state = player.serverLevel().getBlockState(pot.getBlockPos().below());
        boolean lit = (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.FURNACE) || state.is(Blocks.SMOKER)) && state.getValue(BlockStateProperties.LIT);
        boolean heated = lit || state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.LAVA_CAULDRON)
                || state.is(BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "blockfire")));
        if (!heated) message(player, "§4铁锅缺少热源！可以在下方点燃篝火或放置各类炉灶，或直接点一团火。");
        return heated;
    }

    static void message(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), false);
    }

    private static void particles(ServerLevel level, BlockPos pos, ParticleOptions type, int count, double speed) {
        level.sendParticles(type, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, count, 0.2, 0.2, 0.2, speed);
    }
}
