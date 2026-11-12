package net.yvanchuxiuzhen.system.fulu;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.FuluKnowledge;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableBlockEntity;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;

/** 画符台共享选择、职业等级快照与制作；玩家知识和职业经验经所属公开操作读写。 */
public final class DrawingTableRuntime {
    private static final String[] SELECTIONS = {"futou", "tiandizhu", "fudan"};
    private static final String[] EMPTY_MESSAGES = {"§4您还未掌握任何一种符头！", "§4您还未掌握任何一种天地柱！", "§4您还未掌握任何一种符胆！"};
    private static final String[] USAGE = {
            "画符台是用于绘制符箓的工作台。",
            "符箓根据威力分为天、地、玄、黄四个品阶，天阶最强，黄阶最弱。",
            "您在各地旅行时，一定见过传承之光吧？",
            "接受了传承之光的传承后，就可在画符台借用相应的神明之力，搭配不同的符胆来绘制各色符箓。",
            "符胆与天地柱需要在符箓商人处购买并学习后才可使用。",
            "天地柱直接影响符箓的威力。",
            "每次绘制符箓，需要放置一张符纸与墨条，然后点击画笔按钮即可绘制符箓。"
    };
    private static final String[] MATERIALS = {
            "符纸是由黄纸与朱砂在工作台处制作，而黄纸则是由三张纸与黄色染料在工作台处制作。",
            "墨则相对复杂很多。",
            "您需要先制作一个烧灰架，把原木烧制成烟灰，并收集起来；",
            "然后您需要下矿挖掘一些辰砂，并在磨盘处研磨成朱砂；",
            "接下来您需要猎杀一些妖兽，并使用妖兽材料在大铁锅或砂锅处，制作成墨的料坯；",
            "最后料坯就可以在工作台处制作为墨条。"
    };

    private DrawingTableRuntime() {
    }

    /** 每次自然右键覆盖共享职业快照；潜行而未开窗时也保留该来源事件。 */
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand()) return;
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof DrawingTableBlockEntity table) {
            table.getPersistentData().putDouble("RuneLEVEL", Professions.level(player, Profession.RUNE));
            table.changed();
        }
    }

    public static void button(DrawingTableBlockEntity table, ServerPlayer player, int button) {
        if (button < 6) select(table, player, button % 3, button >= 3);
        else if (button == 6) draw(table, player);
        else {
            for (String line : button == 7 ? USAGE : MATERIALS) message(player, line);
        }
    }

    private static void select(DrawingTableBlockEntity table, ServerPlayer player, int category, boolean up) {
        FuluKnowledge knowledge = Fulu.knowledge(player);
        List<Integer> choices = switch (category) {
            case 0 -> knowledge.heads();
            case 1 -> knowledge.pillars();
            default -> knowledge.cores();
        };
        CompoundTag state = table.getPersistentData();
        String field = SELECTIONS[category];
        int position = state.getInt(field + "weizhi");
        if (choices.isEmpty()) message(player, EMPTY_MESSAGES[category]);
        else {
            position += up ? 1 : -1;
            if (up && position > choices.size()) position = 1;
            else if (!up && position < 1) position = choices.size();
            state.putInt(field, choices.get(position - 1));
            refresh(table);
        }
        state.putInt(field + "weizhi", position);
        table.changed();
    }

    /** 先清旧预览，再取有序表中符头和符胆的首个匹配；天地柱只决定品阶。 */
    public static void refresh(DrawingTableBlockEntity table) {
        CompoundTag state = table.getPersistentData();
        state.putInt("needmotiao", 0);
        state.putString("fuluname", "");
        DrawingRecipes.find(state.getInt("futou"), state.getInt("fudan")).ifPresent(recipe -> {
            state.putInt("needmotiao", recipe.ink());
            state.putString("fuluname", recipe.item().toString());
        });
    }

    private static void draw(DrawingTableBlockEntity table, ServerPlayer player) {
        ItemStack previous = table.getItem(2);
        if (previous.getCount() >= 64) {
            message(player, "§4请先取走绘制好的符箓。");
            return;
        }
        if (!table.getItem(0).is(BuiltInRegistries.ITEM.get(id("fulufuzhi")))) {
            message(player, "§4缺少符纸！");
            return;
        }
        CompoundTag state = table.getPersistentData();
        Optional<DrawingRecipes.Recipe> match = DrawingRecipes.find(state.getInt("futou"), state.getInt("fudan"));
        if (match.isEmpty()) return;
        DrawingRecipes.Recipe recipe = match.get();
        player.displayClientMessage(Component.literal("符箓：").append(DrawingRecipes.name(recipe.item())), false);
        int grade = state.getInt("tiandizhu");
        ItemStack ink = grade >= 1 && grade <= 4 ? new ItemStack(BuiltInRegistries.ITEM.get(id(String.format(Locale.ROOT, "motiao_%02d", grade)))) : ItemStack.EMPTY;
        if (ink.isEmpty()) grade = 0;
        if (state.getInt("RuneLEVEL") + 1 < grade) {
            message(player, "§4您的画符等级不足以绘制此等级的符箓！您可以降低天地柱等级或多画一些低级符箓提升自身等级。");
            return;
        }
        if (!table.getItem(1).is(ink.getItem())) {
            player.displayClientMessage(Component.literal("§4需要[").append(ink.getHoverName()).append("]"), false);
            return;
        }
        if (table.getItem(1).getCount() < recipe.ink()) {
            message(player, "§4墨的数量不足！");
            return;
        }
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(recipe.item()));
        CompoundTag data = new CompoundTag();
        data.putInt("fululevel", grade);
        data.putString("fululeveltxt", grade == 0 ? "" : knowledgeKey("tiandizhu", grade));
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        if (!previous.isEmpty()) {
            if (previous.is(result.getItem()) && previous.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("fululevel") == grade) {
                result.setCount(previous.getCount() + 1);
            } else ItemHandlerHelper.giveItemToPlayer(player, previous.copy());
        }
        table.setItem(2, result);
        int experience = (grade * 2) * (grade * 2) + 4;
        Professions.addExperience(player, Profession.RUNE, experience);
        message(player, "§6画符经验增减量为" + experience);
        table.setItem(0, table.getItem(0).copyWithCount(table.getItem(0).getCount() - 1));
        table.setItem(1, table.getItem(1).copyWithCount(table.getItem(1).getCount() - recipe.ink()));
        table.changed();
        player.containerMenu.broadcastChanges();
    }

    public static String knowledgeKey(String category, int number) {
        if (number == 0) return "";
        return "txt.yvanchuxiuzhen.huafu_" + category + "_" + String.format(Locale.ROOT, "%02d", number);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }
}
