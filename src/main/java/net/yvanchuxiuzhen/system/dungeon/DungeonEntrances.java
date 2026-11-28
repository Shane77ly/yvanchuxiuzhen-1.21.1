package net.yvanchuxiuzhen.system.dungeon;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonData;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonPlayerData;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonWorldData;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 天道副本的默认定义、冷却与门票入场，按来源先扣票后作最终准入判断。 */
public final class DungeonEntrances {
    private DungeonEntrances() {
    }

    public static void initialize(DungeonBlockEntity block) {
        block.setDungeonName("花妖连战");
        block.setTemplateId("fuben002");
        block.setTextLine(1, "这是一个放置传送门就自动生成的");
        block.setTextLine(2, "副本。");
        block.setRewardSummary("无呀");
        block.setDefinitionId(DungeonDefinitions.DEFAULT_ID);
        block.setMinLevel(0);
        block.setMaxLevel(40);
        block.setMaxPeople(10);
        block.setPreview(5);
        block.setCooldownMax(240);
        block.changed();
    }

    public static void tick(DungeonBlockEntity block) {
        if (block.onCooldown()) {
            block.setCooldownTicks(block.cooldownTicks() + 1);
            if (block.cooldownTicks() >= block.cooldownMax()) {
                block.setOnCooldown(false);
                block.setCooldownTicks(0);
            }
        } else if (block.cooldownTicks() != 0) {
            block.setCooldownTicks(0);
        }
        block.changed();
    }

    public static void enter(ServerPlayer leader, DungeonBlockEntity block) {
        ServerLevel origin = leader.serverLevel();
        boolean refused = false;
        int people = 0;
        if (!block.onCooldown()) {
            if (!Relations.temporaryTeam(leader).isEmpty()) {
                for (ServerPlayer member : nearby(leader, block.getBlockPos(), 16)) {
                    people++;
                    double level = Cultivation.get(member, CultivationValue.LEVEL_NUMBER);
                    if (level < block.minLevel() || level > block.maxLevel()) {
                        leader.displayClientMessage(Component.literal("§4您的队伍中有人的等级不在承受等级区间，无法进入！"), false);
                        refused = true;
                        break;
                    }
                }
            }
            if (people > block.maxPeople()) {
                leader.displayClientMessage(Component.literal("§4您的队伍人数超过承受人数上限，无法进入！"), false);
                refused = true;
            }
            ItemStack ticket = block.getItem(0);
            if (!ticket.isEmpty() && !pay(leader, ticket)) {
                leader.displayClientMessage(Component.literal("§4您没有足够的" + ticket.getDisplayName().getString() + "，无法进入！"), false);
                refused = true;
            }
        } else {
            leader.displayClientMessage(Component.literal("§4副本冷却中，请静待" + (block.cooldownMax() - block.cooldownTicks()) + "秒"), false);
            refused = true;
        }
        if (refused) return;
        block.setOnCooldown(true);
        block.changed();
        if (!DungeonPlayers.supported(origin.dimension())) {
            leader.displayClientMessage(Component.literal("§4当前维度并不支持此功能"), false);
            return;
        }
        DungeonPlayerData state = leader.getData(DungeonData.PLAYER)
                .withReturnPos(block.getBlockPos())
                .withControllerPos(BlockPos.containing(block.entranceControllerX(), block.entranceControllerY(), block.entranceControllerZ()))
                .withEntryPos(block.entrancePlayerX(), block.entrancePlayerY(), block.entrancePlayerZ());
        if (block.getItem(1).isEmpty()) {
            state = state.withNeedGenerate(true);
            block.setItem(1, new ItemStack(Items.FLINT));
            if (block.templateId().equals("fuben002")) {
                double x = DungeonWorldData.get(leader.server).instanceX();
                block.setEntranceControllerPos(x, 98, -100);
                block.setEntrancePlayerPos(x + 16, 100, -96);
            }
        }
        state = state.withEnteredInfo(block.dungeonName(), block.definitionId(), block.templateId(), true, 6);
        // 来源妖魂分支重复判断主世界，妖魂进入时保留此前的回程维度字段。
        if (!origin.dimension().location().getPath().equals("dimensionyaohun")) {
            state = state.withReturnDimension(origin.dimension().location().toString());
        }
        leader.setData(DungeonData.PLAYER, state);
        for (ServerPlayer member : nearby(leader, block.getBlockPos(), 16)) {
            member.setData(DungeonData.PLAYER, member.getData(DungeonData.PLAYER)
                    .withPreviousGameMode(modeName(member.gameMode.getGameModeForPlayer())));
            member.setGameMode(GameType.ADVENTURE);
            DungeonPlayers.travel(member, DungeonPlayers.PAINTING, -32, 100, -32);
        }
        BlockPos floor = BlockPos.containing(leader.getX(), leader.getY() - 1, leader.getZ());
        if (origin.isEmptyBlock(floor)) origin.setBlock(floor, DungeonPlayers.block("tiandaowhiteblock").defaultBlockState(), 3);
        leader.closeContainer();
        block.changed();
    }

    private static boolean pay(ServerPlayer player, ItemStack ticket) {
        String identity = ticket.getOrDefault(DungeonData.TICKET_ID.get(), "");
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        int total = 0;
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) if (matches(inventory.getStackInSlot(slot), ticket, identity)) total += inventory.getStackInSlot(slot).getCount();
        }
        if (total < ticket.getCount()) return false;
        if (identity.isEmpty()) {
            player.getInventory().clearOrCountMatchingItems(stack -> stack.is(ticket.getItem()), ticket.getCount(), player.inventoryMenu.getCraftSlots());
        } else if (inventory instanceof IItemHandlerModifiable mutable) {
            int remaining = ticket.getCount();
            for (int slot = 0; slot < mutable.getSlots(); slot++) {
                ItemStack stack = mutable.getStackInSlot(slot).copy();
                if (!matches(stack, ticket, identity)) continue;
                int taken = Math.min(stack.getCount(), remaining);
                stack.shrink(taken);
                mutable.setStackInSlot(slot, stack);
                remaining -= taken;
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    private static boolean matches(ItemStack stack, ItemStack ticket, String identity) {
        return stack.is(ticket.getItem()) && (identity.isEmpty() || stack.getOrDefault(DungeonData.TICKET_ID.get(), "").equals(identity));
    }

    static List<ServerPlayer> nearby(ServerPlayer leader, BlockPos pos, double radius) {
        Vec3 center = Vec3.atLowerCornerOf(pos);
        return leader.serverLevel().getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(radius), player -> Relations.isFriendly(leader, player)).stream()
                .sorted(Comparator.comparingDouble(player -> player.distanceToSqr(center))).toList();
    }

    private static String modeName(GameType mode) {
        return switch (mode) { case ADVENTURE -> "冒险"; case SURVIVAL -> "生存"; case CREATIVE -> "创造"; case SPECTATOR -> "旁观"; };
    }
}
