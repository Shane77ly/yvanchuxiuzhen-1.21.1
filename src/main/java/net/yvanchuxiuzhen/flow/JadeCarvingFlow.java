package net.yvanchuxiuzhen.flow;

import java.util.Random;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.creature.api.CreatureEncounters;
import net.yvanchuxiuzhen.system.guchong.api.GuchongEncounters;
import net.yvanchuxiuzhen.system.material.api.JadeCarvingEvent;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 璞玉雕琢及其跨系统奇遇，按来源次序交付奖励和刷新体质字段。 */
public final class JadeCarvingFlow {
    private static final String[] REWARDS = {
            "yvanchuxiuzhen:cailiao_021",
            "yvanchuxiuzhen:cailiao_022",
            "yvanchuxiuzhen:cailiao_023",
            "yvanchuxiuzhen:cailiao_024",
            "yvanchuxiuzhen:cailiao_025",
            "yvanchuxiuzhen:cailiao_026",
            "yvanchuxiuzhen:cailiao_027",
            "yvanchuxiuzhen:cailiao_028",
            "yvanchuxiuzhen:cailiao_029",
            "yvanchuxiuzhen:cailiao_030",
            "yvanchuxiuzhen:cailiao_031",
            "yvanchuxiuzhen:cailiao_032",
            "yvanchuxiuzhen:cailiao_033",
            "yvanchuxiuzhen:cailiao_034",
            "yvanchuxiuzhen:cailiao_035",
            "yvanchuxiuzhen:cailiao_036",
            "yvanchuxiuzhen:cailiao_060",
            "yvanchuxiuzhen:cailiao_061",
            "yvanchuxiuzhen:cailiao_062",
            "yvanchuxiuzhen:cailiao_063",
            "yvanchuxiuzhen:cailiao_064",
            "yvanchuxiuzhen:cailiao_065",
            "yvanchuxiuzhen:cailiao_066",
            "yvanchuxiuzhen:cailiao_067",
            "yvanchuxiuzhen:cailiao_068",
            "yvanchuxiuzhen:cailiao_069",
            "yvanchuxiuzhen:cailiao_070",
            "yvanchuxiuzhen:cailiao_071",
            "yvanchuxiuzhen:cailiao_072",
            "yvanchuxiuzhen:cailiao_073",
            "yvanchuxiuzhen:cailiao_074",
            "yvanchuxiuzhen:cailiao_075",
            "yvanchuxiuzhen:cailiao_076",
            "yvanchuxiuzhen:cailiao_077",
            "yvanchuxiuzhen:cailiao_078",
            "yvanchuxiuzhen:cailiao_079",
            "yvanchuxiuzhen:cailiao_080",
            "yvanchuxiuzhen:cailiao_081",
            "yvanchuxiuzhen:cailiao_082",
            "yvanchuxiuzhen:cailiao_083",
            "yvanchuxiuzhen:cailiao_084",
            "yvanchuxiuzhen:cailiao_085",
            "yvanchuxiuzhen:cailiao_086",
            "yvanchuxiuzhen:cailiao_098",
            "yvanchuxiuzhen:cailiao_102",
            "yvanchuxiuzhen:cailiao_103",
            "yvanchuxiuzhen:cailiao_106",
            "yvanchuxiuzhen:cailiao_107",
            "yvanchuxiuzhen:cailiao_109",
            "yvanchuxiuzhen:cailiao_110",
            "yvanchuxiuzhen:cailiao_112",
            "yvanchuxiuzhen:cailiao_115",
            "yvanchuxiuzhen:cailiao_121",
            "yvanchuxiuzhen:cailiao_127",
            "yvanchuxiuzhen:cailiao_128",
            "yvanchuxiuzhen:cailiao_129",
            "yvanchuxiuzhen:cailiao_138",
            "minecraft:diamond",
            "minecraft:emerald",
            "minecraft:redstone",
            "minecraft:quartz",
            "minecraft:netherite_scrap",
            "minecraft:nether_star",
            "minecraft:raw_iron",
            "minecraft:raw_copper",
            "minecraft:raw_gold",
            "yvanchuxiuzhen:cailiao_201raw",
            "yvanchuxiuzhen:cailiao_203raw",
            "yvanchuxiuzhen:cailiao_205raw",
            "yvanchuxiuzhen:cailiao_207raw",
            "yvanchuxiuzhen:cailiao_208raw",
            "yvanchuxiuzhen:cailiao_209raw",
            "yvanchuxiuzhen:cailiao_211raw",
            "yvanchuxiuzhen:cailiao_213raw",
            "yvanchuxiuzhen:cailiao_215raw",
            "yvanchuxiuzhen:cailiao_216raw",
            "yvanchuxiuzhen:cailiao_217raw",
            "yvanchuxiuzhen:cailiao_219raw",
            "minecraft:obsidian",
            "minecraft:crying_obsidian",
            "minecraft:bedrock",
            "minecraft:sand",
            "minecraft:clay_ball",
            "minecraft:flint",
            "minecraft:coal",
            "minecraft:amethyst_shard",
            "minecraft:nautilus_shell",
            "minecraft:bone",
            "minecraft:bone_meal"
    };
    private static final String[] BOOK_COMMANDS = {
            "technique yvanchuxiuzhen:book_017 尘丰诀 13 13 土 土属性进阶功法，额外增加土系技能攻击力。 0 0 0 0 2 下档 下档 下档 下档 下档 下档 中档 中档 下档 中档 下档 下档 最下档",
            "technique yvanchuxiuzhen:book_017 重峦真经 23 23 土 土属性高级功法，额外增加土系技能攻击力。 0 0 0 0 3 中档 中档 中档 中档 中档 中档 上档 上档 中档 中档 中档 中档 最下档",
            "technique yvanchuxiuzhen:book_017 擎山震地功 33 33 土 土属性顶级功法之一，额外增加土系技能攻击力。 0 0 0 0 4 上档 上档 上档 上档 上档 上档 上档 最上档 上档 上档 上档 上档 最下档"
    };
    private static final String[] SOIL_MESSAGES = {
            "观察玉石纹路，稍微对土属性技能加深了了解。土属性熟练度+10",
            "观察玉石纹路，对土属性技能加深了了解。土属性熟练度+20",
            "观察玉石纹路，对土属性技能有了更深入的了解。土属性熟练度+40",
            "观察玉石纹路，悟出了五行中土行的一丝真谛。土属性熟练度+80"
    };
    private JadeCarvingFlow() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(JadeCarvingFlow::carve);
    }

    private static int quality() {
        int roll = new Random().nextInt(1, 101);
        return roll < 5 ? 4 : roll < 20 ? 3 : roll < 60 ? 2 : roll < 90 ? 1 : 0;
    }

    private static void carve(JadeCarvingEvent event) {
        ServerPlayer player = event.player();
        ItemStack input = event.input();
        int number = event.number();
        int level = Math.min(40, 2 + (number - 161) * 5);
        if (player.isShiftKeyDown()) {
            int count = Math.min(input.getCount(), 64);
            for (int i = 0; i < count; i++) giveJade(player, number, level, quality());
            input.shrink(count);
            return;
        }
        int grade = quality();
        double itemFortune = input.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("qiyvbianhao");
        boolean active = itemFortune >= 1 || Cultivation.tizhiDouble(player, "jiyvgailv") >= 94;
        double fortune = itemFortune >= 1 ? itemFortune : Cultivation.tizhiDouble(player, "qiyvbianhao");
        if (active) {
            if (fortune == 1) level += 2;
            else if (fortune == 2) level += 5;
            else if (fortune == 3) level += 10;
            else if (fortune == 4) level = 40;
            else if (fortune == 5) grade++;
            else if (fortune == 6) grade = 4;
            else if (fortune == 7) level = 1;
            else if (fortune == 8) grade = 0;
            else encounter(player, fortune);
            level = Math.min(level, 40);
            grade = Math.min(grade, 4);
        }
        giveJade(player, number, level, grade);
        if (itemFortune <= 0) {
            Cultivation.setTizhiDouble(player, "jiyvgailv", new Random().nextInt(1, 101));
            Cultivation.setTizhiDouble(player, "qiyvbianhao", new Random().nextInt(1, 110));
        }
        input.shrink(1);
    }

    private static void giveJade(ServerPlayer player, int number, int level, int quality) {
        ItemStack stack = item("yvanchuxiuzhen:cailiao_" + number);
        Materials.setInstance(stack, level, quality);
        ItemHandlerHelper.giveItemToPlayer(player, stack);
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)));
    }

    private static void encounter(ServerPlayer player, double fortune) {
        if (fortune >= 9 && fortune <= 97) {
            if (fortune == Math.rint(fortune)) {
                ItemStack reward = item(REWARDS[(int) fortune - 9]);
                if (fortune <= 65) Materials.setInstance(reward, new Random().nextInt(1, 41), quality());
                ItemHandlerHelper.giveItemToPlayer(player, reward);
            }
        } else if (fortune >= 97 && fortune <= 101) {
            if (fortune >= 98 && fortune <= 100 && fortune == Math.rint(fortune)) {
                player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4).withSuppressedOutput(), BOOK_COMMANDS[(int) fortune - 98]);
            }
            player.displayClientMessage(Component.literal("你居然在玉石的沟壑纹理之间悟出了一部功法！"), false);
        } else if (fortune == 102) {
            ServerLevel level = player.serverLevel();
            for (int i = 0; i < 3; i++) {
                Silverfish mob = EntityType.SILVERFISH.create(level);
                mob.moveTo(player.getX(), player.getY(), player.getZ(), level.getRandom().nextFloat() * 360F, 0F);
                EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                level.addFreshEntity(mob);
            }
        } else if (fortune == 103 || fortune == 104) {
            CreatureEncounters.spawnJadeEncounter(player, (int) fortune);
        } else if (fortune == 105) {
            GuchongEncounters.spawnJadeEncounter(player);
        } else if (fortune >= 106 && fortune <= 109 && fortune == Math.rint(fortune)) {
            int index = (int) fortune - 106;
            int amount = 10 << index;
            Cultivation.setTizhiDouble(player, "killentitypointssoil", Cultivation.tizhiDouble(player, "killentitypointssoil") + amount);
            player.displayClientMessage(Component.literal(SOIL_MESSAGES[index]), false);
        }
    }
}
