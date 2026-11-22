package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.system.book.BookQuizQuestions;
import net.yvanchuxiuzhen.system.book.client.BookQuizScreen;
import net.yvanchuxiuzhen.system.book.content.BookMenus;
import net.yvanchuxiuzhen.system.book.content.BookQuizMenu;
import net.yvanchuxiuzhen.system.book.data.BookQuizData;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 显式打开答题页后，经四个客户端按钮核对普通物品归属、原组件保留与真实保存重进。 */
final class BookQuizOwnershipCheck extends SystemCheck {
    private static final int[] QUESTIONS = {4, 1, 5, 2};
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ItemStack> completed = new ArrayList<>();
    private ItemStack candidate;
    private ItemStack openingStack;
    private int selected;
    private int originalQuestion;
    private boolean hadProgress;
    private double originalProgress;

    BookQuizOwnershipCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("答题通用归属", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step >= 1 && step <= 12) {
            int option = (step - 1) / 3;
            return switch ((step - 1) % 3) {
                case 0 -> action(local, player -> stage(player, option));
                case 1 -> {
                    if (local < 8) yield false;
                    if (!opened(option)) yield true;
                    onServer(player -> {
                        player.setItemInHand(InteractionHand.MAIN_HAND, player.getInventory().removeItemNoUpdate(9 + option));
                        player.getInventory().setItem(9 + option, openingStack.copy());
                        player.inventoryMenu.broadcastChanges();
                    });
                    if (option == 0) screenshots.take("book_quiz_generic_owner.png");
                    buttons().get(option).onPress();
                    yield true;
                }
                default -> action(local, player -> verifyCompleted(player, option));
            };
        }
        return switch (step) {
            case 0 -> action(local, this::prepare);
            case 13 -> { leaveWorld(); yield true; }
            case 14 -> reopened(local);
            case 15 -> inWorld(local);
            case 16 -> action(local, player -> {
                for (int option = 0; option < completed.size(); option++) {
                    ItemStack saved = player.getInventory().getItem(9 + option);
                    expectTrue("选项" + (option + 1) + "普通物品全部组件及数量真实保存重进",
                            ItemStack.matches(completed.get(option), saved));
                    expectTrue("选项" + (option + 1) + "重进后物品组件双端一致",
                            ItemStack.matches(saved, minecraft.player.getInventory().getItem(9 + option)));
                    verifyOwner(player, saved, "选项" + (option + 1) + "重进");
                }
                expect("重进后答题计数仍为零", 0, player.getPersistentData().getDouble("daduishuliang"));
                cleanup(player);
                finish();
            });
            default -> throw new IllegalStateException("未知答题通用归属检查步骤 " + step);
        };
    }

    private boolean action(int local, Consumer<ServerPlayer> action) {
        if (local < 8) return false;
        onServer(action);
        return true;
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot).copy());
        }
        selected = player.getInventory().selected;
        originalQuestion = player.getData(BookQuizData.QUESTION);
        hadProgress = player.getPersistentData().contains("daduishuliang");
        originalProgress = player.getPersistentData().getDouble("daduishuliang");
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
    }

    private void stage(ServerPlayer player, int option) {
        candidate = sample(option);
        openingStack = new ItemStack(Items.PAPER);
        openingStack.set(DataComponents.CUSTOM_NAME, Component.literal("开窗时的原主手"));
        player.setItemInHand(InteractionHand.MAIN_HAND, openingStack.copy());
        player.getInventory().setItem(9 + option, candidate.copy());
        player.setData(BookQuizData.QUESTION, QUESTIONS[option]);
        player.getPersistentData().putDouble("daduishuliang", 10);
        player.inventoryMenu.broadcastChanges();
        BookMenus.openQuiz(player);
    }

    private boolean opened(int option) {
        boolean opened = minecraft.screen instanceof BookQuizScreen
                && minecraft.player.containerMenu instanceof BookQuizMenu;
        expectTrue("选项" + (option + 1) + "显式打开原生答题菜单", opened);
        if (!opened) {
            onServer(this::cleanup);
            finish();
            return false;
        }
        expect("中文答题标题", "书籍答题", minecraft.screen.getTitle().getString());
        expect("当前题四个按钮中文", BookQuizQuestions.get(QUESTIONS[option]).options(),
                buttons().stream().map(button -> button.getMessage().getString()).toList());
        return true;
    }

    private List<Button> buttons() {
        return minecraft.screen.children().stream().filter(child -> child instanceof Button)
                .map(child -> (Button) child).toList();
    }

    private void verifyCompleted(ServerPlayer player, int option) {
        String label = "选项" + (option + 1);
        ItemStack actual = player.getMainHandItem();
        verifyOwner(player, actual, label);
        CompoundTag originalData = data(candidate);
        CompoundTag remainingData = data(actual);
        for (String key : List.of("belongToMasterUUID", "belongToName")) {
            originalData.remove(key);
            remainingData.remove(key);
        }
        expect(label + "保留原自定义数据与嵌套字段", originalData, remainingData);
        ItemStack originalComponents = candidate.copy();
        originalComponents.set(DataComponents.CUSTOM_DATA, actual.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY));
        expectTrue(label + "保留物品身份数量名称耐久及其他组件", ItemStack.matches(originalComponents, actual));
        expectTrue(label + "普通物品没有新增功法或技能实例", Techniques.book(actual).isEmpty() && Skills.book(actual).isEmpty());
        expectTrue(label + "开窗时原主手未被绑定", ItemStack.matches(openingStack, player.getInventory().getItem(9 + option)));
        expectTrue(label + "只改变当前主手且不新增物品", player.getInventory().items.stream().filter(stack -> !stack.isEmpty()).count()
                == option + 2);
        expectTrue(label + "普通栈归属与其他组件双端一致", ItemStack.matches(actual, minecraft.player.getMainHandItem()));
        expectTrue(label + "完成后关闭菜单并同步清零", !(player.containerMenu instanceof BookQuizMenu)
                && player.getPersistentData().getDouble("daduishuliang") == 0
                && BookQuizData.state(minecraft.player).progress() == 0);
        completed.add(actual.copy());
        player.getInventory().setItem(9 + option, actual.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
    }

    private void verifyOwner(ServerPlayer player, ItemStack stack, String label) {
        CompoundTag data = data(stack);
        expect(label + "通用UUID属于答题玩家", player.getStringUUID(), data.getString("belongToMasterUUID"));
        expect(label + "通用显示名属于答题玩家", player.getDisplayName().getString(), data.getString("belongToName"));
    }

    private static ItemStack sample(int option) {
        if (option == 0) return new ItemStack(Items.STICK, 7);
        ItemStack stack = switch (option) {
            case 1 -> new ItemStack(Items.PAPER, 11);
            case 2 -> new ItemStack(Items.DIAMOND, 3);
            case 3 -> new ItemStack(Items.IRON_SWORD);
            default -> throw new IllegalArgumentException("未知普通物品样本 " + option);
        };
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("保留原名" + option));
        if (option == 3) stack.setDamageValue(5);
        CompoundTag data = new CompoundTag();
        data.putString("unrelated_text", "保留原数据");
        data.putDouble("unrelated_number", 17.5);
        CompoundTag nested = new CompoundTag();
        nested.putInt("marker", option);
        data.put("unrelated_nested", nested);
        if (option >= 2) {
            data.putString("belongToMasterUUID", "11111111-2222-3333-4444-555555555555");
            data.putString("belongToName", "既有归属");
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setData(BookQuizData.QUESTION, originalQuestion);
        if (hadProgress) player.getPersistentData().putDouble("daduishuliang", originalProgress);
        else player.getPersistentData().remove("daduishuliang");
        BookQuizData.publish(player);
        player.inventoryMenu.broadcastChanges();
    }
}
