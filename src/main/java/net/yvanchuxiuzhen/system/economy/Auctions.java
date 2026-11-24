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
                CompoundTag state = controller.state();
                state.putInt("paimaifangzhi", 1);
                state.putString("paimaihuiname", token.getDisplayName().getString());
                state.putString("paimaihanguuid", data.auctionUuid());
                state.putString("paimaimoshi", "大量npc");
                state.putString("paimaidengji", "筑基期拍卖会");
                state.putString("paimaihefaxing", "正规拍卖");
                state.putInt("paimaiitemnumber", 9);
                controller.changed();
                message(player, "可以在其它位置放置座位", true);
            } else if (count > 1) {
                seat(player, target, AuctionSeatRequestEvent.Operation.CREATE);
                if (player.level().getBlockEntity(data.seat(1)) instanceof EconomyBlockEntity controller) {
                    coordinates(controller.state(), count, target);
                    controller.state().putInt("paimaifangzhi", count);
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
        if (!data.auctionUuid().equals(controller.state().getString("paimaihanguuid"))) {
            message(player, "§4此令牌不是这个拍卖方块的令牌，无法收起拍卖方块", false);
        } else if (controller.state().getBoolean("tuoguan") && !player.isCreative()) {
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
        CompoundTag state = block.state();
        switch (operation) {
            case 0 -> {
                if (player.isCreative()) state.putString("paimaimoshi", state.getString("paimaimoshi").equals("全自动") ? "自营" : "全自动");
                else {
                    message(player, "§4只能在创造模式下，修改此选项。", false);
                    message(player, "§4选择全自动后，拍卖行将进入完全自动模式，自动补充物品，自动拍卖。同时，所获得的收益就不再分配给放置拍卖行的人了。", false);
                }
            }
            case 1 -> state.putString("paimaimoshi", switch (state.getString("paimaimoshi")) { case "仅玩家" -> "少量npc"; case "少量npc" -> "大量npc"; default -> "仅玩家"; });
            case 2 -> state.putString("paimaidengji", switch (state.getString("paimaidengji")) { case "筑基期拍卖会" -> "结丹期拍卖会"; case "结丹期拍卖会" -> "元婴期拍卖会"; default -> "筑基期拍卖会"; });
            case 3 -> state.putString("paimaihefaxing", state.getString("paimaihefaxing").equals("正规拍卖") ? "匿名拍卖" : "正规拍卖");
            case 4 -> state.putInt("paimaiitemnumber", state.getInt("paimaiitemnumber") > 5 ? state.getInt("paimaiitemnumber") - 1 : 20);
            case 5 -> state.putInt("paimaiitemnumber", state.getInt("paimaiitemnumber") < 20 ? state.getInt("paimaiitemnumber") + 1 : 5);
            default -> { return; }
        }
        block.rearmAuction();
        block.changed();
    }

    public static void tick(ServerLevel level, EconomyBlockEntity block) {
        CompoundTag state = block.state();
        state.putInt("paimaitick", (state.getInt("paimaitick") + 1) % 3);
        List<ServerPlayer> seated = new ArrayList<>();
        for (int index = 2; index <= state.getInt("paimaifangzhi"); index++) {
            Vec3 center = Vec3.atLowerCornerOf(coordinates(state, index));
            for (Player player : level.getEntitiesOfClass(Player.class, new AABB(center, center).inflate(0.5))) if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atLowerCornerOf(block.getBlockPos()).add(0.5, 1, 0.5));
                CompoundTag session = EconomyMenus.snapshot(serverPlayer);
                session.putInt("paiblockX", block.getBlockPos().getX());
                session.putInt("paiblockY", block.getBlockPos().getY());
                session.putInt("paiblockZ", block.getBlockPos().getZ());
                serverPlayer.setData(CommerceData.SESSION, session);
                seated.add(serverPlayer);
            }
        }
        if (seated.isEmpty()) block.rearmAuction();
        if (!seated.isEmpty() && state.getInt("paimaicolddown") <= 0 && state.getInt("paimaijindu") == 0 && !block.auctionRejected()) {
            AuctionNpcRequestEvent request = new AuctionNpcRequestEvent(level);
            NeoForge.EVENT_BUS.post(request);
            List<CompoundTag> npcs = eligibleNpcs(state, request.definitions());
            List<String> goods = goods(state);
            boolean missingNpc = npcCount(state) > 0 && npcs.isEmpty();
            if (goods.isEmpty() || missingNpc) {
                block.rejectAuction();
                String reason = goods.isEmpty() && missingNpc ? "缺少拍卖商品和符合本场等级的NPC" : goods.isEmpty() ? "缺少拍卖商品" : "缺少符合本场等级的NPC";
                for (ServerPlayer player : seated) message(player, "§4本场拍卖未启动：" + reason + "。", false);
            } else {
                state.putInt("paimaijindu", 1);
            }
        }
        if (state.getInt("paimaicolddown") > 0) state.putInt("paimaicolddown", state.getInt("paimaicolddown") - 1);
        else switch (state.getInt("paimaijindu")) {
            case 1 -> {
                state.putInt("paimaijindu", 2);
                if (state.getBoolean("tuoguan")) for (int index = 1; index <= state.getInt("paimaiitemnumber"); index++) state.getCompound("paiitem" + index).putString("shujvwenben", "asdasdasdasd");
                int count = npcCount(state);
                if (count > 0) state.putInt("paimainpcnumber", count);
                AuctionNpcRequestEvent request = new AuctionNpcRequestEvent(level);
                NeoForge.EVENT_BUS.post(request);
                List<CompoundTag> npcs = eligibleNpcs(state, request.definitions());
                for (int index = 1; index < count; index++) state.put("noplayercharacter" + index, npcs.get(level.random.nextInt(npcs.size())).copy());
                List<String> goods = goods(state);
                AuctionGoods.fill(block, goods, level.random);
                state.putInt("commodityweizhi", 0);
            }
            case 2 -> {
                int index = state.getInt("commodityweizhi") + 1;
                state.putInt("commodityweizhi", index);
                block.setItem(20, block.getItem(index - 1).copyWithCount(1));
                state.putInt("paimaijindu", 3);
            }
            case 3 -> { willingness(level, block); state.putInt("paimaijindu", 4); state.putInt("paitick", 0); }
            case 4 -> {
                state.putInt("paitick", state.getInt("paitick") + 1);
                if (state.getBoolean("tiaoxin") && state.getInt("paitick") >= 1) {
                    provoke(state);
                    state.putInt("paitick", 0);
                    state.putBoolean("tiaoxin", false);
                    state.putInt("tiaoxinnumber", 0);
                    state.putInt("paimaijindu", 3);
                } else if (!state.getBoolean("tiaoxin") && state.getInt("paitick") >= 2) state.putInt("paitick", 0);
            }
            case 5 -> {
                for (int index = 1; index <= state.getInt("paimainpcnumber"); index++) {
                    CompoundTag npc = state.getCompound("noplayercharacter" + index);
                    for (String key : List.of("xinlijiaMax", "xinlijiaMin", "yiyvan", "chujiacishu", "chujiaqvanzhong")) npc.putInt(key, 0);
                    npc.putBoolean("fangqi", false);
                    npc.putBoolean("tiaoxin", false);
                    state.put("noplayercharacter" + index, npc);
                }
            }
            default -> { }
        }
        block.changed();
    }

    private static int npcCount(CompoundTag state) { return state.getString("paimaimoshi").equals("少量npc") ? 9 : state.getString("paimaimoshi").equals("大量npc") ? 45 : 0; }

    private static List<CompoundTag> eligibleNpcs(CompoundTag state, CompoundTag definitions) {
        int minimum = state.getString("paimaidengji").equals("筑基期拍卖会") ? 8 : state.getString("paimaidengji").equals("结丹期拍卖会") ? 18 : 28;
        List<CompoundTag> result = new ArrayList<>();
        for (int index = 1; index <= definitions.getInt("npcmaxnumber"); index++) {
            CompoundTag npc = definitions.getCompound("noplayercharacter" + index).copy();
            if (npc.getInt("npclevel") >= minimum && npc.getInt("npclevel") <= minimum + 14) { npc.putInt("weizhi", index); result.add(npc); }
        }
        return result;
    }

    private static List<String> goods(CompoundTag state) {
        int start = state.getString("paimaidengji").equals("筑基期拍卖会") ? 0 : state.getString("paimaidengji").equals("结丹期拍卖会") ? 1001 : 2000;
        int minimum = start == 0 ? 1 : start == 1001 ? 1000 : 2001;
        int end = start;
        String key = "auction.itemdata.goods" + end;
        while (!CommerceCatalog.translated(key).equals(key) && CommerceCatalog.translated(key).length() > 17) { end++; key = "auction.itemdata.goods" + end; }
        List<String> result = new ArrayList<>();
        for (int index = minimum; index <= end; index++) {
            String name = "auction.itemdata.goods" + index;
            String value = CommerceCatalog.translated(name);
            if (value.equals(name)) return List.of();
            result.add(value);
        }
        return result;
    }

    private static void willingness(ServerLevel level, EconomyBlockEntity block) {
        CompoundTag state = block.state();
        ItemStack item = block.getItem(20);
        AuctionLots.Entry goods = item.getOrDefault(CommerceData.AUCTION_LOTS.get(), AuctionLots.EMPTY).lot(state.getInt("commodityweizhi"));
        double base = 0, requiredLevel = 0, requiredQuality = 0, maximum = 0, minimum = 0;
        boolean proceed = false, existing = false;
        for (int index = 1; index <= state.getInt("paimainpcnumber"); index++) {
            CompoundTag npc = state.getCompound("noplayercharacter" + index);
            if (npc.getBoolean("fangqi")) continue;
            for (int need = 1; need <= 3; need++) {
                String tag = npc.getString("veryneed" + need).toLowerCase(Locale.ENGLISH).replace("forge:", ModId.VALUE + ":");
                ResourceLocation id = ResourceLocation.tryParse(tag);
                if (id == null || !item.is(ItemTags.create(id))) continue;
                base = need == 1 ? 15 : need == 2 ? 10 : 5;
                proceed = !tag.equals(ModId.VALUE + ":book") || npc.getString("veryneed" + need + "type").equals(goods.type());
                if (npc.getInt("veryneed" + need + "pinjie") >= 1) requiredQuality = npc.getInt("veryneed" + need + "pinjie");
                requiredLevel = npc.getInt("veryneed" + need + "level");
                maximum = goods.startingPrice() * (need == 1 ? 6 : need == 2 ? 4 : 3);
                minimum = goods.startingPrice() * (need == 1 ? 4 : need == 2 ? 3 : 2);
                break;
            }
            if (!proceed) continue;
            double emotion = npc.getInt("qingxv");
            maximum += emotion / 10;
            minimum += emotion / 10;
            npc.putInt("xinlijiaMax", (int) maximum);
            npc.putInt("xinlijiaMin", (int) minimum);
            CompoundTag bidders = state.getCompound("jingpainpc");
            if (state.getInt("jingjianpcnumber") < 1) for (int previous = 1; previous <= state.getInt("jingjianpcnumber"); previous++) if (bidders.getInt("npcweizhi" + previous) == index) { existing = true; break; }
            if (!existing) { state.putInt("jingjianpcnumber", state.getInt("jingjianpcnumber") + 1); bidders.putInt("npcweizhi" + state.getInt("jingjianpcnumber"), index); }
            if (!npc.getBoolean("tiaoxin") && Mth.nextInt(level.random, (int) minimum, (int) maximum) >= state.getInt("commoditymoney")) {
                double willingness = base + goods.level() - requiredLevel - emotion / 5;
                if (state.getInt("commodityupnumber") >= 1) willingness -= npc.getInt(state.getCompound("chujianpc").getString("npcweizhi" + state.getInt("commodityupnumber") + "uuid") + "like") / 5.0;
                if (item.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao")))) willingness += (Materials.instance(item).quality() - requiredQuality) * 5;
                npc.putInt("yiyvan", (int) (willingness + npc.getInt("chujiaqvanzhong")));
            } else {
                int roll = level.random.nextInt(10);
                String line = LINES.getAsJsonArray("Fangqijingjiaduihua").get(roll < 5 ? roll : 5).getAsString();
                dialogue(state, "§6" + npc.getString("npcname") + ":§f" + line);
                npc.putBoolean("fangqi", true);
            }
            state.put("noplayercharacter" + index, npc);
            state.put("jingpainpc", bidders);
            if (npc.getBoolean("tiaoxin")) { state.putBoolean("tiaoxin", true); state.putInt("tiaoxinnumber", index); }
        }
        wrapDialogue(state);
    }

    private static void provoke(CompoundTag state) {
        CompoundTag npc = state.getCompound("noplayercharacter" + state.getInt("tiaoxinnumber"));
        CompoundTag bids = state.getCompound("chujianpc");
        CompoundTag previous = state.getCompound("noplayercharacter" + bids.getInt("npcweizhi" + state.getInt("commodityupnumber")));
        if (!state.getString("paimaihefaxing").equals("匿名拍卖")) {
            int relation = (int) Math.round(npc.getInt(bids.getString("npcweizhi" + (state.getInt("commodityupnumber") - 1) + "uuid") + "like") / 10.0);
            if (relation >= -10 && relation <= 10) {
                int group = npc.getInt("npclevel") > previous.getInt("npclevel") ? 0 : npc.getInt("npclevel") == previous.getInt("npclevel") ? 1 : 2;
                String text = LINES.getAsJsonArray("Beitiaoxinhuayv").get(group * 21 + 10 - relation).getAsString();
                if (group == 0 && relation == -4) text += (double) state.getInt("commoditymoney") + "§f你可莫要哭啊。";
                if (group == 2 && (relation == -4 || relation == -5)) text += state.getInt("commoditymoney");
                dialogue(state, "§6" + npc.getString("npcname") + ":" + text);
            } else dialogue(state, "§6" + npc.getString("npcname") + ":");
            wrapDialogue(state);
        }
        int number = state.getInt("commodityupnumber") + 1;
        state.putInt("commodityupnumber", number);
        bids.putString("npcweizhi" + number + "uuid", npc.getString("npcuuid"));
        bids.putInt("npcweizhi" + number, state.getInt("tiaoxinnumber"));
        int increase = bids.getInt("npcweizhi" + (number - 1) + "jiage") * 2;
        bids.putInt("npcweizhi" + number + "jiage", increase);
        state.putInt("commoditymoney", state.getInt("commoditymoney") + increase);
        npc.putInt(previous.getString("npcuuid") + "like", npc.getInt(previous.getString("npcuuid") + "like") - 10);
        npc.putInt("qingxv", npc.getInt("qingxv") - 8);
        npc.putInt("chujiacishu", npc.getInt("chujiacishu") + 1);
        npc.putInt("chujiaqvanzhong", npc.getInt("chujiaqvanzhong") - 5);
        state.put("noplayercharacter" + state.getInt("tiaoxinnumber"), npc);
    }

    private static void dialogue(CompoundTag state, String line) { int number = state.getInt("duihuanumber") + 1; state.putInt("duihuanumber", number); state.putString("duihua" + number, line); }
    private static void wrapDialogue(CompoundTag state) {
        for (int index = 1; index <= 20; index++) state.putString("xianshiwenben" + index, "");
        int row = 1;
        for (int previous = state.getInt("duihuanumber"); row < 20; previous--) {
            String line = state.getString("duihua" + previous);
            for (int start = 0; start < line.length(); start += 10) state.putString("xianshiwenben" + row++, line.substring(start, Math.min(start + 10, line.length())));
            if (previous <= 1) break;
        }
    }

    private static void seat(ServerPlayer player, BlockPos pos, AuctionSeatRequestEvent.Operation operation) { NeoForge.EVENT_BUS.post(new AuctionSeatRequestEvent(player.serverLevel(), player, pos, operation)); }
    private static boolean isToken(ItemStack item) { return BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals("yvanchuxiuzhen:ling_20"); }
    private static AuctionTokenData tokenData(ItemStack item) { return item.getOrDefault(CommerceData.AUCTION_TOKEN.get(), AuctionTokenData.EMPTY); }
    private static BlockPos coordinates(CompoundTag data, int index) { return new BlockPos(data.getInt("paiblockX" + index), data.getInt("paiblockY" + index), data.getInt("paiblockZ" + index)); }
    private static void coordinates(CompoundTag data, int index, BlockPos pos) { data.putInt("paiblockX" + index, pos.getX()); data.putInt("paiblockY" + index, pos.getY()); data.putInt("paiblockZ" + index, pos.getZ()); }
    private static void message(ServerPlayer player, String text, boolean actionbar) { player.displayClientMessage(Component.literal(text), actionbar); }
}
