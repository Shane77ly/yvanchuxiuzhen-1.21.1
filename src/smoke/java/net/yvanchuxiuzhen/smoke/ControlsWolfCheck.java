package net.yvanchuxiuzhen.smoke;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import org.lwjgl.glfw.GLFW;

/** 用改绑后的键码经过客户端分派和实际网络，再以服务端玩家喂食驯服狼。 */
final class ControlsWolfCheck extends SystemCheck {
    private static final InputConstants.Key CTRL_TEST = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F22);
    private static final InputConstants.Key ALT_TEST = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F23);
    private Binding ctrl;
    private Binding alt;

    ControlsWolfCheck(List<String> report, String levelId) {
        super("独立修饰键与狼食", report, levelId);
    }

    private record Binding(KeyMapping mapping, KeyModifier modifier, InputConstants.Key key) {
        void restore() {
            mapping.setKeyModifierAndCode(modifier, key);
        }
    }

    private Binding rebind(String name, int defaultCode, InputConstants.Key replacement) {
        KeyMapping key = Arrays.stream(minecraft.options.keyMappings).filter(k -> k.getName().equals(name)).findFirst().orElseThrow();
        expect(name + "默认键码", defaultCode, key.getDefaultKey().getValue());
        Binding old = new Binding(key, key.getKeyModifier(), key.getKey());
        key.setKeyModifierAndCode(KeyModifier.NONE, replacement);
        return old;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            ctrl = rebind("key.yvanchuxiuzhen.ctrlanjian", GLFW.GLFW_KEY_LEFT_CONTROL, CTRL_TEST);
            alt = rebind("key.yvanchuxiuzhen.altanjian", GLFW.GLFW_KEY_F19, ALT_TEST);
            KeyMapping.resetMapping();
            KeyMapping.set(CTRL_TEST, true);
            return true;
        }
        // 每个输入边沿都要等服务端实际收到，不能用客户端的 isDown 替代。
        boolean wantCtrl = step == 1 || step == 2;
        boolean wantAlt = step == 2 || step == 3;
        boolean[] states = new boolean[2];
        onServer(player -> {
            states[0] = ModifierKeys.ctrl(player);
            states[1] = ModifierKeys.alt(player);
        });
        boolean matched = states[0] == wantCtrl && states[1] == wantAlt;
        if (!matched && local < 100) return false;
        expectTrue("服务端输入步骤" + step + " 预期Ctrl/Alt=" + wantCtrl + "/" + wantAlt
                + " 实际=" + states[0] + "/" + states[1], matched);
        if (matched && step < 4) {
            switch (step) {
                case 1 -> KeyMapping.set(ALT_TEST, true);
                case 2 -> KeyMapping.set(CTRL_TEST, false);
                case 3 -> KeyMapping.set(ALT_TEST, false);
            }
            return true;
        }
        KeyMapping.set(CTRL_TEST, false);
        KeyMapping.set(ALT_TEST, false);
        ctrl.restore();
        alt.restore();
        KeyMapping.resetMapping();
        onServer(this::checkWolves);
        finish();
        return true;
    }

    private void checkWolves(ServerPlayer player) {
        Item raw = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "shicai_060"));
        Item cooked = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "food_084"));
        InteractionHand hand = InteractionHand.OFF_HAND;
        ItemStack saved = player.getItemInHand(hand);
        GameType mode = player.gameMode.getGameModeForPlayer();
        try {
            player.setGameMode(GameType.SURVIVAL);
            Wolf first = wolf(player);
            Wolf second = wolf(player);
            checkHealing(player, hand, first, raw, 1, "小肉");
            checkHealing(player, hand, first, cooked, 4, "烤小肉");
            checkHealing(player, hand, first, Items.BEEF, 6, "原版生牛肉");
            first.setHealth(first.getMaxHealth());
            second.setHealth(second.getMaxHealth());
            feedLast(player, hand, first, raw, "满血成年狼吃小肉");
            feedLast(player, hand, second, cooked, "满血成年狼吃烤小肉");
            expectTrue("两狼均进入繁殖状态且可配对", first.isInLove() && second.isInLove() && first.canMate(second));
        } finally {
            player.setItemInHand(hand, saved);
            player.setGameMode(mode);
        }
    }

    private static Wolf wolf(ServerPlayer player) {
        Wolf wolf = EntityType.WOLF.create(player.serverLevel());
        wolf.setPos(player.position());
        wolf.tame(player);
        return wolf;
    }

    private void checkHealing(ServerPlayer player, InteractionHand hand, Wolf wolf, Item food, int gain, String label) {
        wolf.setHealth(2);
        feedLast(player, hand, wolf, food, label);
        expect(label + "最后一块的治疗量", gain, wolf.getHealth() - 2);
        expectTrue(label + "残血喂食不进入繁殖状态", !wolf.isInLove());
    }

    private void feedLast(ServerPlayer player, InteractionHand hand, Wolf wolf, Item food, String label) {
        player.setItemInHand(hand, new ItemStack(food, 1));
        boolean accepted = player.interactOn(wolf, hand).consumesAction();
        expectTrue(label + "单件被接受并耗尽", accepted && player.getItemInHand(hand).isEmpty());
    }
}
