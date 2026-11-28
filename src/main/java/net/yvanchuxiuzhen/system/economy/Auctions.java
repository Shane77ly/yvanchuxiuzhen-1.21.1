package net.yvanchuxiuzhen.system.economy;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.AuctionNpcRequestEvent;
import net.yvanchuxiuzhen.system.economy.api.AuctionSeatRequestEvent;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.content.CommerceMenu;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntity;
import net.yvanchuxiuzhen.system.economy.data.AuctionLots;
import net.yvanchuxiuzhen.system.economy.data.AuctionTokenData;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.economy.data.EconomyBlockState.AuctionBid;
import net.yvanchuxiuzhen.system.economy.data.EconomyBlockState.AuctionNpcState;
import net.yvanchuxiuzhen.system.economy.data.EconomyBlockState.AuctionState;
import net.yvanchuxiuzhen.system.economy.data.EconomyBlockState.NeedSlot;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 拍卖令牌、座椅和来源20刻状态机；缺失商品或合格NPC时在启动前一次反馈。 */
public final class Auctions {
    private static final JsonObject LINES = CommerceCatalog.load("auction_lines");
    private Auctions() { }

    public static void place(ServerPlayer player, ItemStack token, BlockPos clicked, Direction face) {
        AuctionTokenData data = tokenData(token);
        if (data.auctionUuid().isEmpty()) {
            StringBuilder identity = new StringBuilder();
            for (int index = 0; index < 12; index++) identity.append("qwertyuiopasdfghjklmnbvcxz".charAt(player.getRandom().nextInt(26)));
            data = data.withUuid(identity.toString());
        }
        String clickedId = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(clicked).getBlock()).getPath();
        BlockPos target = clicked.above();
        if (!clickedId.equals("paimaiblock") && !clickedId.equals("paimaichair") && face == Direction.UP
                && player.level().isEmptyBlock(target) && data.placedCount() < 11) {
            int count = data.placedCount() + 1;
            data = data.addSeat(count, target);
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", count == 1 ? "paimaiblock" : "paimaichair"));
            player.level().setBlock(target, block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                    player.getDirection().getOpposite()), Block.UPDATE_ALL);
            if (count == 1 && player.level().getBlockEntity(target) instanceof EconomyBlockEntity controller) {
                AuctionState state = controller.state().auction();
                state.placedCount = 1;
                state.auctionName = token.getDisplayName().getString();
                state.auctionUuid = data.auctionUuid();
                state.auctionMode = "大量npc";
                state.auctionTier = "筑基期拍卖会";
                state.auctionLegality = "正规拍卖";
                state.auctionItemCount = 9;
                controller.changed();
                message(player, "可以在其它位置放置座位", true);
            } else if (count > 1) {
                seat(player, target, AuctionSeatRequestEvent.Operation.CREATE);
                if (player.level().getBlockEntity(data.seat(1)) instanceof EconomyBlockEntity controller) {
                    AuctionState state = controller.state().auction();
                    state.seats.put(count, target);
                    state.placedCount = count;
                    controller.changed();
                }
                message(player, "已放置" + (count - 1) + "个座位", true);
            }
        }
        token.set(CommerceData.AUCTION_TOKEN.get(), data);
        player.getCooldowns().addCooldown(token.getItem(), 10);
    }

    public static void interact(ServerPlayer player, EconomyBlockEntity controller) {
        ItemStack token = player.getMainHandItem();
        if (!isToken(token)) { EconomyMenus.open(player, controller.getBlockPos(), CommerceMenu.Kind.AUCTION_SETTINGS); return; }
        AuctionTokenData data = tokenData(token);
        AuctionState state = controller.state().auction();
        if (!data.auctionUuid().equals(state.auctionUuid)) {
            message(player, "§4此令牌不是这个拍卖方块的令牌，无法收起拍卖方块", false);
        } else if (state.managed && !player.isCreative()) {
            message(player, "§4必须要创造模式才可收起此拍卖行", false);
        } else {
            for (int index = 2; index <= data.placedCount(); index++) {
                BlockPos chair = data.seat(index);
                player.level().setBlock(chair, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                seat(player, chair, AuctionSeatRequestEvent.Operation.REMOVE);
            }
            player.level().setBlock(controller.getBlockPos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            token.set(CommerceData.AUCTION_TOKEN.get(), data.resetPlacements());
        }
    }

    public static void chair(ServerPlayer player, BlockPos pos) {
        ItemStack token = player.getMainHandItem();
        if (isToken(token)) {
            AuctionTokenData data = tokenData(token);
            seat(player, pos, AuctionSeatRequestEvent.Operation.REMOVE);
            player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            token.set(CommerceData.AUCTION_TOKEN.get(), data.removeChair(pos));
        } else {
            seat(player, pos, AuctionSeatRequestEvent.Operation.RIDE);
            EconomyMenus.open(player, pos, CommerceMenu.Kind.AUCTION);
        }
    }

    public static void closed(ServerPlayer player, BlockPos chair) {
        if (!player.isPassenger()) return;
        player.stopRiding();
        for (Direction direction : List.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH)) {
            BlockPos exit = chair.relative(direction);
            if (player.level().canSeeSkyFromBelowWater(exit)) {
                player.connection.teleport(exit.getX(), exit.getY(), exit.getZ(), player.getYRot(), player.getXRot());
                break;
            }
        }
    }

    public static void button(ServerPlayer player, BlockPos pos, int operation) {
        if (!(player.level().getBlockEntity(pos) instanceof EconomyBlockEntity block) || !block.auction()) return;
        AuctionState state = block.state().auction();
        switch (operation) {
            case 0 -> {
                if (player.isCreative()) state.auctionMode = state.auctionMode.equals("全自动") ? "自营" : "全自动";
                else {
                    message(player, "§4只能在创造模式下，修改此选项。", false);
                    message(player, "§4选择全自动后，拍卖行将进入完全自动模式，自动补充物品，自动拍卖。同时，所获得的收益就不再分配给放置拍卖行的人了。", false);
                }
            }
            case 1 -> state.auctionMode = switch (state.auctionMode) { case "仅玩家" -> "少量npc"; case "少量npc" -> "大量npc"; default -> "仅玩家"; };
            case 2 -> state.auctionTier = switch (state.auctionTier) { case "筑基期拍卖会" -> "结丹期拍卖会"; case "结丹期拍卖会" -> "元婴期拍卖会"; default -> "筑基期拍卖会"; };
            case 3 -> state.auctionLegality = state.auctionLegality.equals("正规拍卖") ? "匿名拍卖" : "正规拍卖";
            case 4 -> state.auctionItemCount = state.auctionItemCount > 5 ? state.auctionItemCount - 1 : 20;
            case 5 -> state.auctionItemCount = state.auctionItemCount < 20 ? state.auctionItemCount + 1 : 5;
            default -> { return; }
        }
        block.rearmAuction();
        block.changed();
    }

    public static void tick(ServerLevel level, EconomyBlockEntity block) {
        AuctionState state = block.state().auction();
        state.auctionTick = (state.auctionTick + 1) % 3;
        List<ServerPlayer> seated = new ArrayList<>();
        for (int index = 2; index <= state.placedCount; index++) {
            BlockPos seatPos = state.seats.get(index);
            if (seatPos == null) continue;
            Vec3 center = Vec3.atLowerCornerOf(seatPos);
            for (Player player : level.getEntitiesOfClass(Player.class, new AABB(center, center).inflate(0.5))) if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atLowerCornerOf(block.getBlockPos()).add(0.5, 1, 0.5));
                serverPlayer.setData(CommerceData.SESSION, EconomyMenus.snapshot(serverPlayer).toBuilder()
                        .auctionBlockPos(block.getBlockPos().getX(), block.getBlockPos().getY(), block.getBlockPos().getZ())
                        .build());
                seated.add(serverPlayer);
            }
        }
        if (seated.isEmpty()) block.rearmAuction();
        if (!seated.isEmpty() && state.cooldown <= 0 && state.stage == 0 && !block.auctionRejected()) {
            AuctionNpcRequestEvent request = new AuctionNpcRequestEvent(level);
            NeoForge.EVENT_BUS.post(request);
            List<AuctionNpcState> npcs = eligibleNpcs(state, request.definitions());
            List<AuctionGoodDefinition> goods = goods(state);
            boolean missingNpc = npcCount(state) > 0 && npcs.isEmpty();
            if (goods.isEmpty() || missingNpc) {
                block.rejectAuction();
                String reason = goods.isEmpty() && missingNpc ? "缺少拍卖商品和符合本场等级的NPC" : goods.isEmpty() ? "缺少拍卖商品" : "缺少符合本场等级的NPC";
                for (ServerPlayer player : seated) message(player, "§4本场拍卖未启动：" + reason + "。", false);
            } else {
                state.stage = 1;
            }
        }
        if (state.cooldown > 0) state.cooldown -= 1;
        else switch (state.stage) {
            case 1 -> {
                state.stage = 2;
                if (state.managed) for (int index = 1; index <= state.auctionItemCount; index++) state.itemData.put(index, "asdasdasdasd");
                int count = npcCount(state);
                if (count > 0) state.npcCount = count;
                AuctionNpcRequestEvent request = new AuctionNpcRequestEvent(level);
                NeoForge.EVENT_BUS.post(request);
                List<AuctionNpcState> npcs = eligibleNpcs(state, request.definitions());
                for (int index = 1; index < count; index++) state.npcs.put(index, copyNpc(npcs.get(level.random.nextInt(npcs.size()))));
                List<AuctionGoodDefinition> goods = goods(state);
                AuctionGoods.fill(block, goods, level.random);
                state.commoditySlot = 0;
            }
            case 2 -> {
                int index = state.commoditySlot + 1;
                state.commoditySlot = index;
                block.setItem(20, block.getItem(index - 1).copyWithCount(1));
                state.stage = 3;
            }
            case 3 -> { willingness(level, block); state.stage = 4; state.subTick = 0; }
            case 4 -> {
                state.subTick += 1;
                if (state.provoked && state.subTick >= 1) {
                    provoke(state);
                    state.subTick = 0;
                    state.provoked = false;
                    state.provokedNpcIndex = 0;
                    state.stage = 3;
                } else if (!state.provoked && state.subTick >= 2) state.subTick = 0;
            }
            case 5 -> {
                for (int index = 1; index <= state.npcCount; index++) {
                    AuctionNpcState npc = state.npcs.get(index);
                    if (npc != null) npc.resetForRound();
                }
            }
            default -> { }
        }
        block.changed();
    }

    private static int npcCount(AuctionState state) { return state.auctionMode.equals("少量npc") ? 9 : state.auctionMode.equals("大量npc") ? 45 : 0; }

    private static List<AuctionNpcState> eligibleNpcs(AuctionState state, CompoundTag definitions) {
        int minimum = state.auctionTier.equals("筑基期拍卖会") ? 8 : state.auctionTier.equals("结丹期拍卖会") ? 18 : 28;
        List<AuctionNpcState> result = new ArrayList<>();
        for (int index = 1; index <= definitions.getInt("npcmaxnumber"); index++) {
            CompoundTag raw = definitions.getCompound("noplayercharacter" + index);
            int level = raw.getInt("npclevel");
            if (level >= minimum && level <= minimum + 14) {
                result.add(parseNpc(raw));
            }
        }
        return result;
    }

    private static AuctionNpcState parseNpc(CompoundTag raw) {
        AuctionNpcState npc = new AuctionNpcState();
        npc.uuid = raw.getString("npcuuid");
        npc.name = raw.getString("npcname");
        npc.level = raw.getInt("npclevel");
        npc.emotion = raw.getInt("qingxv");
        for (int need = 1; need <= 3; need++) {
            npc.needs.add(new NeedSlot(
                    raw.getString("veryneed" + need),
                    raw.getString("veryneed" + need + "type"),
                    raw.getInt("veryneed" + need + "pinjie"),
                    raw.getInt("veryneed" + need + "level")));
        }
        for (String key : raw.getAllKeys()) {
            if (key.endsWith("like") && key.length() > 4) {
                npc.likes.put(key.substring(0, key.length() - 4), raw.getInt(key));
            }
        }
        return npc;
    }

    private static AuctionNpcState copyNpc(AuctionNpcState src) {
        AuctionNpcState copy = new AuctionNpcState();
        copy.uuid = src.uuid;
        copy.name = src.name;
        copy.level = src.level;
        copy.emotion = src.emotion;
        copy.needs.addAll(src.needs);
        copy.likes.putAll(src.likes);
        return copy;
    }

    private static List<AuctionGoodDefinition> goods(AuctionState state) {
        int tier = state.auctionTier.equals("筑基期拍卖会") ? 1
                : state.auctionTier.equals("结丹期拍卖会") ? 2 : 3;
        return AuctionGoods.byTier(tier);
    }

    private static void willingness(ServerLevel level, EconomyBlockEntity block) {
        AuctionState state = block.state().auction();
        ItemStack item = block.getItem(20);
        AuctionLots.Entry goods = item.getOrDefault(CommerceData.AUCTION_LOTS.get(), AuctionLots.EMPTY).lot(state.commoditySlot);
        double base = 0, requiredLevel = 0, requiredQuality = 0, maximum = 0, minimum = 0;
        boolean proceed = false, existing = false;
        for (int index = 1; index <= state.npcCount; index++) {
            AuctionNpcState npc = state.npcs.get(index);
            if (npc == null || npc.abandoned) continue;
            for (int need = 1; need <= Math.min(3, npc.needs.size()); need++) {
                NeedSlot slot = npc.needs.get(need - 1);
                String tag = slot.tag().toLowerCase(Locale.ENGLISH).replace("forge:", ModId.VALUE + ":");
                ResourceLocation id = ResourceLocation.tryParse(tag);
                if (id == null || !item.is(ItemTags.create(id))) continue;
                base = need == 1 ? 15 : need == 2 ? 10 : 5;
                proceed = !tag.equals(ModId.VALUE + ":book") || slot.type().equals(goods.type());
                if (slot.quality() >= 1) requiredQuality = slot.quality();
                requiredLevel = slot.level();
                maximum = goods.startingPrice() * (need == 1 ? 6 : need == 2 ? 4 : 3);
                minimum = goods.startingPrice() * (need == 1 ? 4 : need == 2 ? 3 : 2);
                break;
            }
            if (!proceed) continue;
            double emotion = npc.emotion;
            maximum += emotion / 10;
            minimum += emotion / 10;
            npc.maxPrice = (int) maximum;
            npc.minPrice = (int) minimum;
            if (state.bidderCount < 1 && state.bidderIndices.contains(index)) existing = true;
            if (!existing) {
                state.bidderCount += 1;
                state.bidderIndices.add(index);
            }
            if (!npc.provoked && Mth.nextInt(level.random, (int) minimum, (int) maximum) >= state.commodityPrice) {
                double willingness = base + goods.level() - requiredLevel - emotion / 5;
                if (state.commodityUpCount >= 1 && state.commodityUpCount <= state.bids.size()) {
                    String upUuid = state.bids.get(state.commodityUpCount - 1).uuid();
                    willingness -= npc.likes.getOrDefault(upUuid, 0) / 5.0;
                }
                if (item.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao")))) willingness += (Materials.instance(item).quality() - requiredQuality) * 5;
                npc.willingness = (int) (willingness + npc.bidWeight);
            } else {
                int roll = level.random.nextInt(10);
                String line = LINES.getAsJsonArray("Fangqijingjiaduihua").get(roll < 5 ? roll : 5).getAsString();
                dialogue(state, "§6" + npc.name + ":§f" + line);
                npc.abandoned = true;
            }
            if (npc.provoked) { state.provoked = true; state.provokedNpcIndex = index; }
        }
        wrapDialogue(state);
    }

    private static void provoke(AuctionState state) {
        AuctionNpcState npc = state.npcs.getOrDefault(state.provokedNpcIndex, new AuctionNpcState());
        AuctionBid currentBid = state.commodityUpCount >= 1 && state.commodityUpCount <= state.bids.size()
                ? state.bids.get(state.commodityUpCount - 1) : new AuctionBid(0, "", 0);
        AuctionNpcState previous = state.npcs.getOrDefault(currentBid.npcIndex(), new AuctionNpcState());
        if (!state.auctionLegality.equals("匿名拍卖")) {
            int prevIdx = state.commodityUpCount - 1;
            String prevLikeUuid = prevIdx >= 1 && prevIdx <= state.bids.size() ? state.bids.get(prevIdx - 1).uuid() : "";
            int relation = (int) Math.round(npc.likes.getOrDefault(prevLikeUuid, 0) / 10.0);
            if (relation >= -10 && relation <= 10) {
                int group = npc.level > previous.level ? 0 : npc.level == previous.level ? 1 : 2;
                String text = LINES.getAsJsonArray("Beitiaoxinhuayv").get(group * 21 + 10 - relation).getAsString();
                if (group == 0 && relation == -4) text += (double) state.commodityPrice + "§f你可莫要哭啊。";
                if (group == 2 && (relation == -4 || relation == -5)) text += state.commodityPrice;
                dialogue(state, "§6" + npc.name + ":" + text);
            } else dialogue(state, "§6" + npc.name + ":");
            wrapDialogue(state);
        }
        int number = state.commodityUpCount + 1;
        state.commodityUpCount = number;
        int increase = currentBid.price() * 2;
        while (state.bids.size() < number) state.bids.add(new AuctionBid(0, "", 0));
        state.bids.set(number - 1, new AuctionBid(state.provokedNpcIndex, npc.uuid, increase));
        state.commodityPrice += increase;
        npc.likes.merge(previous.uuid, -10, Integer::sum);
        npc.emotion -= 8;
        npc.bidCount += 1;
        npc.bidWeight -= 5;
    }

    private static void dialogue(AuctionState state, String line) {
        state.dialogues.add(line);
    }

    private static void wrapDialogue(AuctionState state) {
        for (int index = 1; index <= 20; index++) state.setDisplayLine(index, "");
        int row = 1;
        for (int previous = state.dialogues.size(); row < 20 && previous >= 1; previous--) {
            String line = state.dialogues.get(previous - 1);
            for (int start = 0; start < line.length(); start += 10) state.setDisplayLine(row++, line.substring(start, Math.min(start + 10, line.length())));
        }
    }

    private static void seat(ServerPlayer player, BlockPos pos, AuctionSeatRequestEvent.Operation operation) { NeoForge.EVENT_BUS.post(new AuctionSeatRequestEvent(player.serverLevel(), player, pos, operation)); }
    private static boolean isToken(ItemStack item) { return BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals("yvanchuxiuzhen:ling_20"); }
    private static AuctionTokenData tokenData(ItemStack item) { return item.getOrDefault(CommerceData.AUCTION_TOKEN.get(), AuctionTokenData.EMPTY); }
    private static void message(ServerPlayer player, String text, boolean actionbar) { player.displayClientMessage(Component.literal(text), actionbar); }
}
