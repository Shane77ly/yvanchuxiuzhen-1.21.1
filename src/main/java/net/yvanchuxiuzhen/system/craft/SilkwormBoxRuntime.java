package net.yvanchuxiuzhen.system.craft;

import java.text.DecimalFormat;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.SilkwormBoxBlockEntity;

/** 蚕盒按秒累计食物、饥饿和成长，阶段转换后继续处理当前槽的卵蛹阶段。 */
public final class SilkwormBoxRuntime {
    private SilkwormBoxRuntime() {
    }

    public static void use(Level world, BlockPos pos, Player player) {
        if (!(world instanceof ServerLevel level)
                || !(level.getBlockEntity(pos) instanceof SilkwormBoxBlockEntity box)) return;
        ItemStack stored = box.getItem(0);
        ItemStack main = player.getMainHandItem();
        if (stored.isEmpty()) {
            insert(level, pos, box, main);
        } else if (player.isShiftKeyDown()) {
            ItemStack output = stored.copy();
            if (egg(stored)) {
                for (int index = 0; index < new Random().nextInt(1, 6); index++) drop(level, pos, output.copy());
            } else {
                drop(level, pos, output);
            }
            box.setItem(0, ItemStack.EMPTY);
        } else if (chrysalis(stored)) {
            if (display(level.getBlockState(pos.above()).getBlock())) {
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            drop(level, pos, stored.copy());
            box.setItem(0, ItemStack.EMPTY);
        }
        if (main.is(item("mulberryleaves"))) {
            box.value("shiwu", box.value("shiwu") + 60);
            box.value("jihanjiaopotick", 0);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    5, 0.5, 0.5, 0.5, 0);
            main.shrink(1);
        } else if (main.is(item("cailiao_091"))) {
            box.value("shiwu", box.value("shiwu") + 120);
            box.value("bianyi", box.value("bianyi") + 1);
            box.value("jihanjiaopotick", 0);
            level.sendParticles(ParticleTypes.GLOW_SQUID_INK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    3, 0.5, 0.5, 0.5, 0);
            main.shrink(1);
        }
        status(player, box);
    }

    private static void insert(ServerLevel level, BlockPos pos, SilkwormBoxBlockEntity box, ItemStack main) {
        for (String id : new String[] {"silkworm", "silkwormgold", "silkwormchrysalis", "silkwormchrysalisgold",
                "silkwormegg", "silkwormegggold"}) {
            if (!main.is(item(id))) continue;
            if ((id.equals("silkwormchrysalis") || id.equals("silkwormchrysalisgold")) && level.isEmptyBlock(pos.above())) {
                level.setBlock(pos.above(), block(id + "_02").defaultBlockState(), Block.UPDATE_ALL);
            }
            box.setItem(0, new ItemStack(item(id)));
            main.shrink(1);
            return;
        }
    }

    private static void status(Player player, SilkwormBoxBlockEntity box) {
        ItemStack stored = box.getItem(0);
        DecimalFormat number = new DecimalFormat("0");
        String remaining = number.format(120 - box.value("tick"));
        if (worm(stored)) {
            Component message = box.value("shiwu") <= 0
                    ? Component.translatable("message.yvanchuxiuzhen.craft.silkworm_needs_food", remaining)
                    : Component.translatable("message.yvanchuxiuzhen.craft.silkworm_growth",
                            number.format(box.value("shiwu")), remaining);
            player.displayClientMessage(message, true);
        } else if (stored.is(item("silkwormegg"))) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.craft.silkworm_egg", remaining), true);
        } else if (stored.is(item("silkwormegggold"))) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.craft.silkworm_gold_egg", remaining), true);
        }
    }

    public static void tick(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof SilkwormBoxBlockEntity box)) return;
        ItemStack initial = box.getItem(0);
        if (worm(initial)) {
            box.value("tick2", box.value("tick2") + 1);
            box.animate(0);
            if (box.value("tick2") >= 10) {
                box.animate(initial.is(item("silkworm")) ? 1 : 2);
                box.value("tick2", 0);
            }
        } else {
            box.animate(0);
        }
        if (box.value("shiwu") >= 1) {
            if (worm(box.getItem(0))) {
                happy(level, pos);
                box.value("shiwu", box.value("shiwu") - 1);
                box.value("tick", box.value("tick") + 1);
                if (box.value("tick") >= 120) pupate(level, pos, box);
            }
        } else if (worm(box.getItem(0))) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    2, 0, 0, 0, 0.5);
            box.value("jihanjiaopotick", box.value("jihanjiaopotick") + 1);
            if (box.value("jihanjiaopotick") >= 60) {
                box.setItem(0, ItemStack.EMPTY);
                box.value("jihanjiaopotick", 0);
            }
        }
        ItemStack current = box.getItem(0);
        if (egg(current)) {
            happy(level, pos);
            box.value("tick", box.value("tick") + 1);
            if (box.value("tick") >= 120) {
                box.setItem(0, new ItemStack(item(current.is(item("silkwormegg")) ? "silkworm" : "silkwormgold")));
                box.value("tick", 0);
            }
        } else if (chrysalis(current)) {
            happy(level, pos);
            box.value("tick", box.value("tick") + 1);
            if (box.value("tick") >= 120) emerge(level, pos, box);
        }
    }

    private static void pupate(ServerLevel level, BlockPos pos, SilkwormBoxBlockEntity box) {
        if (box.getItem(0).is(item("silkworm"))) {
            box.setItem(0, new ItemStack(item("silkwormchrysalis")));
            if (level.isEmptyBlock(pos.above())) {
                level.setBlock(pos.above(), block("silkwormchrysalis_02").defaultBlockState(), Block.UPDATE_ALL);
            }
        } else if (box.value("bianyi") >= 1) {
            box.setItem(0, new ItemStack(item("silkwormchrysalisred")));
            if (level.isEmptyBlock(pos.above())) {
                level.setBlock(pos.above(), block("silkwormchrysalisred_02").defaultBlockState(), Block.UPDATE_ALL);
                box.value("bianyi", box.value("bianyi") - 1);
            }
        } else {
            box.setItem(0, new ItemStack(item("silkwormchrysalisgold")));
            if (level.isEmptyBlock(pos.above())) {
                level.setBlock(pos.above(), block("silkwormchrysalisgold_02").defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        box.value("tick", 0);
    }

    private static void emerge(ServerLevel level, BlockPos pos, SilkwormBoxBlockEntity box) {
        ItemStack current = box.getItem(0);
        boolean ordinary = current.is(item("silkwormchrysalis"));
        String displayId = ordinary ? "silkwormchrysalis_02"
                : current.is(item("silkwormchrysalisgold")) ? "silkwormchrysalisgold_02" : "silkwormchrysalisred_02";
        if (level.getBlockState(pos.above()).is(block(displayId))) {
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        box.setItem(0, new ItemStack(item(ordinary ? "silkwormegg" : "silkwormegggold")));
        Entity moth = BuiltInRegistries.ENTITY_TYPE.get(id(ordinary ? "silkwormmoth" : "silkwormmothgold")).create(level);
        moth.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
        moth.setYBodyRot(0);
        moth.setYHeadRot(0);
        if (moth instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(moth.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        }
        level.addFreshEntity(moth);
        box.value("tick", 0);
    }

    private static boolean worm(ItemStack stack) {
        return stack.is(item("silkworm")) || stack.is(item("silkwormgold"));
    }

    private static boolean egg(ItemStack stack) {
        return stack.is(item("silkwormegg")) || stack.is(item("silkwormegggold"));
    }

    private static boolean chrysalis(ItemStack stack) {
        return stack.is(item("silkwormchrysalis")) || stack.is(item("silkwormchrysalisgold")) || stack.is(item("silkwormchrysalisred"));
    }

    private static boolean display(Block value) {
        return value == block("silkwormchrysalis_02") || value == block("silkwormchrysalisgold_02")
                || value == block("silkwormchrysalisred_02");
    }

    private static void happy(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                1, 0, 0, 0, 0.5);
    }

    private static void drop(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack);
        drop.setPickUpDelay(10);
        level.addFreshEntity(drop);
    }

    private static Item item(String value) {
        return BuiltInRegistries.ITEM.get(id(value));
    }

    private static Block block(String value) {
        return BuiltInRegistries.BLOCK.get(id(value));
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, value);
    }
}
