package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;
import net.yvanchuxiuzhen.system.xiulian.api.BirthItems;

/** 通过真实客户端右键检查灵盘首次进入追踪状态、状态同步与引导粒子画面。 */
final class Ling11GuideCheck extends SystemCheck {
    private final Screenshots screenshots;
    private final List<ItemStack> originalInventory = new ArrayList<>();
    private ServerLevel originalLevel;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private int originalSelected;
    private boolean prepared;

    Ling11GuideCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("灵盘首次指引", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (local < 10) return false;
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
            }
            case 2 -> {
                if (local < 10) return false;
                onServer(this::checkServerState);
                ItemStack clientStack = minecraft.player.getMainHandItem();
                expect("本人客户端收到首次追踪状态", "追踪", state(clientStack));
                expectTrue("灵盘物品状态双端一致", ItemStack.matches(serverItemSnapshot, clientStack));
                screenshots.take("xiulian-ling11-guidance.png");
                onServer(this::restore);
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private ItemStack serverItemSnapshot;

    private void prepare(ServerPlayer player) {
        Optional<Vec3> center = WorldStructures.teleportArrayCenter(player.serverLevel());
        boolean validCenter = center.isPresent() && !isSourceEmptySentinel(center.orElseThrow());
        expectTrue("主世界有非零全局传送阵坐标", player.level().dimension().equals(Level.OVERWORLD) && validCenter);
        if (!validCenter || !player.level().dimension().equals(Level.OVERWORLD)) {
            finish();
            return;
        }

        originalLevel = player.serverLevel();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        Vec3 look = center.orElseThrow().subtract(player.getEyePosition());
        float yaw = (float) Math.toDegrees(Math.atan2(-look.x(), look.z()));
        float pitch = (float) -Math.toDegrees(Math.atan2(look.y(), Math.sqrt(look.x() * look.x() + look.z() * look.z())));
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), yaw, pitch);
        originalSelected = player.getInventory().selected;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            originalInventory.add(player.getInventory().getItem(slot).copy());
        }

        player.getInventory().clearContent();
        ItemStack compass = new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "ling_11")));
        player.getInventory().setItem(0, compass);
        player.getInventory().selected = 0;
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        prepared = true;
    }

    private void checkServerState(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        expect("服务端首次右键写入追踪状态", "追踪", state(stack));
        serverItemSnapshot = stack.copy();
    }

    private void restore(ServerPlayer player) {
        if (!prepared) return;
        player.getInventory().clearContent();
        for (int slot = 0; slot < originalInventory.size(); slot++) {
            player.getInventory().setItem(slot, originalInventory.get(slot));
        }
        player.getInventory().selected = originalSelected;
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.teleportTo(originalLevel, originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
    }

    private static String state(ItemStack stack) {
        return BirthItems.spiritDisc(stack).state();
    }

    private static boolean isSourceEmptySentinel(Vec3 center) {
        return center.x() + center.y() + center.z() == 0;
    }
}
