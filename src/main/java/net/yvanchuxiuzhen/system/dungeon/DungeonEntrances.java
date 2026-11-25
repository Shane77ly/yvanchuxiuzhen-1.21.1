package net.yvanchuxiuzhen.system.dungeon;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.yvanchuxiuzhen.system.dungeon.data.DungeonWorldData;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 天道副本的默认定义、冷却与门票入场，按来源先扣票后作最终准入判断。 */
public final class DungeonEntrances {
    private DungeonEntrances() {
    }

    public static void initialize(DungeonBlockEntity block) {
        CompoundTag data = block.state();
        data.putString("fubenname", "花妖连战");
        data.putString("fubenid", "fuben002");
        data.putString("text01", "这是一个放置传送门就自动生成的");
        data.putString("text02", "副本。");
        data.putString("fubenjiangli", "无呀");
        data.putString("fubenshujv", DungeonSourceData.DEFAULT);
        data.putDouble("fubenminlevel", 0);
        data.putDouble("fubenmaxlevel", 40);
        data.putDouble("fubenpeoplemax", 10);
        data.putDouble("fubenyvlan", 5);
        data.putDouble("fubenjieshujishiMax", 240);
        block.changed();
    }

    public static void tick(DungeonBlockEntity block) {
        CompoundTag data = block.state();
        if (data.getBoolean("fubenbegin")) {
            data.putDouble("fubenjieshujishi", data.getDouble("fubenjieshujishi") + 1);
            if (data.getDouble("fubenjieshujishi") >= data.getDouble("fubenjieshujishiMax")) {
                data.putBoolean("fubenbegin", false);
                data.putDouble("fubenjieshujishi", 0);
            }
        } else if (data.getDouble("fubenjieshujishi") != 0) data.putDouble("fubenjieshujishi", 0);
        block.changed();
    }

    public static void enter(ServerPlayer leader, DungeonBlockEntity block) {
        CompoundTag entrance = block.state();
        ServerLevel origin = leader.serverLevel();
        boolean refused = false;
        int people = 0;
        if (!entrance.getBoolean("fubenbegin")) {
            if (!Relations.temporaryTeam(leader).isEmpty()) {
                for (ServerPlayer member : nearby(leader, block.getBlockPos(), 16)) {
                    people++;
                    double level = Cultivation.get(member, CultivationValue.LEVEL_NUMBER);
                    if (level < entrance.getDouble("fubenminlevel") || level > entrance.getDouble("fubenmaxlevel")) {
                        leader.displayClientMessage(Component.literal("§4您的队伍中有人的等级不在承受等级区间，无法进入！"), false);
                        refused = true;
                        break;
                    }
                }
            }
            if (people > entrance.getDouble("fubenpeoplemax")) {
                leader.displayClientMessage(Component.literal("§4您的队伍人数超过承受人数上限，无法进入！"), false);
                refused = true;
            }
            ItemStack ticket = block.getItem(0);
            if (!ticket.isEmpty() && !pay(leader, ticket)) {
                leader.displayClientMessage(Component.literal("§4您没有足够的" + ticket.getDisplayName().getString() + "，无法进入！"), false);
                refused = true;
            }
        } else {
            leader.displayClientMessage(Component.literal("§4副本冷却中，请静待" + (entrance.getDouble("fubenjieshujishiMax") - entrance.getDouble("fubenjieshujishi")) + "秒"), false);
            refused = true;
        }
        if (refused) return;
        entrance.putBoolean("fubenbegin", true);
        block.changed();
        if (!DungeonPlayers.supported(origin.dimension())) {
            leader.displayClientMessage(Component.literal("§4当前维度并不支持此功能"), false);
            return;
        }
        CompoundTag state = leader.getData(DungeonData.PLAYER);
        DungeonPlayers.coordinates(state, "chuansong", block.getBlockPos().getX(), block.getBlockPos().getY(), block.getBlockPos().getZ());
        for (String prefix : new String[]{"fubenzuobiao", "fubenplayer"}) {
            for (String axis : new String[]{"X", "Y", "Z"}) state.putDouble(prefix + axis, entrance.getDouble(prefix + axis));
        }
        if (block.getItem(1).isEmpty()) {
            state.putBoolean("needfubenshengcheng", true);
            block.setItem(1, new ItemStack(Items.FLINT));
            if (entrance.getString("fubenid").equals("fuben002")) {
                double x = DungeonWorldData.get(leader.server).instanceX();
                DungeonPlayers.coordinates(entrance, "fubenzuobiao", x, 98, -100);
                DungeonPlayers.coordinates(entrance, "fubenplayer", x + 16, 100, -96);
            }
        }
        for (String key : new String[]{"fubenname", "fubenshujv", "fubenid"}) state.putString(key, entrance.getString(key));
        state.putBoolean("fubenstate", true);
        state.putDouble("daojishi", 6);
        // 来源妖魂分支重复判断主世界，妖魂进入时保留此前的回程维度字段。
        if (!origin.dimension().location().getPath().equals("dimensionyaohun")) state.putString("chuansongid", origin.dimension().location().toString());
        for (ServerPlayer member : nearby(leader, block.getBlockPos(), 16)) {
            member.getData(DungeonData.PLAYER).putString("gamemoshi", modeName(member.gameMode.getGameModeForPlayer()));
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
