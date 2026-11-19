package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.guchong.content.TaoguanMenu;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 通过客户端物品与菜单操作装入已捕获的飞蝗。 */
final class RenjianCapturedJarDriver {
    private static final ResourceLocation JAR = id("taoguan_01item");
    private static final ResourceLocation BLOOD = id("jingxve");
    private static final ResourceLocation FLY = id("guchong_03item");
    private static final InteractionHand HAND = InteractionHand.MAIN_HAND;
    private final List<String> report;
    private final Screenshots screenshots;
    private Site site;
    private Action action = Action.NONE;
    private int nextMove;
    private int nextInput;

    RenjianCapturedJarDriver(List<String> report, Screenshots screenshots) {
        this.report = report;
        this.screenshots = screenshots;
    }

    void beforeSave(ServerPlayer player, RenjianWildJarWitness jar, int tick) {
        action = Action.NONE;
        if (!jar.packed()) {
            if (player.containerMenu instanceof TaoguanMenu menu) {
                if (jar.menuVerified()) action = Action.CLOSE;
                else if (!menu.getSlot(0).hasItem()) action = Action.LOAD;
            } else action = Action.OPEN;
            return;
        }
        if (jar.fed()) return;
        if (site == null) {
            if (tick < nextMove) return;
            site = findSite(player);
            nextMove = tick + 20;
            if (site == null) return;
            Vec3 stand = site.stand();
            player.teleportTo(player.serverLevel(), stand.x, stand.y, stand.z, player.getYRot(), player.getXRot());
            report.add("INFO REPLAY_JAR_SITE pos=" + site.floor().above() + " stand=" + stand);
            return;
        }
        Vec3 center = Vec3.atCenterOf(site.floor().above());
        if (animals(player, center, 8).isEmpty()) {
            action = jar.placed() ? Action.FEED : Action.PLACE;
        } else if (!jar.placed() && tick >= nextInput) {
            site = null;
        }
    }

    void input(Minecraft minecraft, int tick) {
        if (tick < nextInput || action == Action.NONE || minecraft.player == null || !minecraft.player.isAlive()
                || minecraft.gameMode == null) return;
        var player = minecraft.player;
        if (action == Action.LOAD || action == Action.CLOSE) {
            if (!(player.containerMenu instanceof TaoguanMenu menu)) return;
            if (action == Action.CLOSE && item(menu.getSlot(0).getItem()).equals(FLY)) {
                if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen) || screen.getMenu() != menu) return;
                screenshots.take("renjian-locust-jar-01-menu.png");
                player.closeContainer();
            } else if (action == Action.LOAD && !menu.getSlot(0).hasItem()) {
                var slot = menu.slots.stream().filter(s -> s.container == player.getInventory()
                        && s.getContainerSlot() == 5 && item(s.getItem()).equals(FLY)).findFirst().orElse(null);
                if (slot == null) return;
                minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slot.index, 0, ClickType.QUICK_MOVE, player);
            }
            nextInput = tick + 8;
            return;
        }
        if (minecraft.screen != null) return;
        switch (action) {
            case OPEN -> {
                if (!select(minecraft, 1, JAR)) return;
                player.input.shiftKeyDown = true;
                player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
                minecraft.gameMode.useItem(player, HAND);
                player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                player.input.shiftKeyDown = false;
                nextInput = tick + 12;
            }
            case PLACE, FEED -> {
                boolean feed = action == Action.FEED;
                if (!select(minecraft, feed ? 2 : 1, feed ? BLOOD : JAR)) return;
                BlockPos pos = feed ? site.floor().above() : site.floor();
                RenjianReplayPlayer.look(minecraft, Vec3.atBottomCenterOf(pos).add(0, feed ? 0.3 : 0.98, 0));
                if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
                        || !hit.getBlockPos().equals(pos) || (!feed && hit.getDirection() != Direction.UP)) return;
                minecraft.gameMode.useItemOn(player, HAND, hit);
                player.swing(HAND);
                nextInput = tick + 12;
            }
            default -> throw new IllegalStateException("未知飞蝗回放动作 " + action);
        }
    }

    private static Site findSite(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        for (int radius : new int[] {4, 8, 12, 16, 20, 24}) {
            for (int angle = 0; angle < 8; angle++) {
                int x = origin.getX() + (int) Math.round(radius * Math.cos(angle * Math.PI / 4));
                int z = origin.getZ() + (int) Math.round(radius * Math.sin(angle * Math.PI / 4));
                if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) continue;
                BlockPos place = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
                BlockPos floor = place.below();
                if (!level.isPositionEntityTicking(place) || !level.isEmptyBlock(place) || !level.isEmptyBlock(place.above())
                        || !(level.getBlockState(floor).is(Blocks.GRASS_BLOCK) || level.getBlockState(floor).is(Blocks.DIRT))) continue;
                Vec3 center = Vec3.atCenterOf(place);
                if (!animals(player, center, 11).isEmpty()) continue;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos stand = place.relative(direction, 2);
                    Vec3 feet = Vec3.atBottomCenterOf(stand);
                    if (RenjianReplayPlayer.safe(level, player, feet) && level.isEmptyBlock(place.relative(direction))
                            && level.isEmptyBlock(place.relative(direction).above())) return new Site(floor, feet);
                }
            }
        }
        return null;
    }

    private static List<Animal> animals(ServerPlayer player, Vec3 center, double radius) {
        String team = Relations.team(player);
        return player.serverLevel().getEntitiesOfClass(Animal.class, new AABB(center, center).inflate(radius),
                animal -> animal.isAlive() && !Relations.entityTeam(animal).equals(team));
    }

    private static boolean select(Minecraft minecraft, int slot, ResourceLocation expected) {
        if (!item(minecraft.player.getInventory().getItem(slot)).equals(expected)) return false;
        minecraft.player.getInventory().selected = slot;
        return true;
    }

    private static ResourceLocation item(ItemStack stack) { return BuiltInRegistries.ITEM.getKey(stack.getItem()); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path); }

    private enum Action { NONE, OPEN, LOAD, CLOSE, PLACE, FEED }
    private record Site(BlockPos floor, Vec3 stand) { }
}
