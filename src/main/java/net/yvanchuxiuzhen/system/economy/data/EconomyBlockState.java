package net.yvanchuxiuzhen.system.economy.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;

/** 经济方块（玩家商铺与拍卖控制台）强类型状态载体，取代旧版套壳 {@code CompoundTag}。 */
public final class EconomyBlockState {
    public static final Codec<EconomyBlockState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShopState.CODEC.fieldOf("shop").forGetter(s -> s.shop),
            AuctionState.CODEC.fieldOf("auction").forGetter(s -> s.auction)
    ).apply(instance, EconomyBlockState::new));

    private final ShopState shop;
    private final AuctionState auction;

    public EconomyBlockState() {
        this(new ShopState(), new AuctionState());
    }

    private EconomyBlockState(ShopState shop, AuctionState auction) {
        this.shop = shop;
        this.auction = auction;
    }

    public ShopState shop() {
        return shop;
    }

    public AuctionState auction() {
        return auction;
    }

    /** 玩家商铺运行时状态。 */
    public static final class ShopState {
        public static final Codec<ShopState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("tiandao").forGetter(s -> s.tiandao),
                Codec.STRING.fieldOf("owner_uuid").forGetter(s -> s.ownerUuid),
                Codec.STRING.fieldOf("direction").forGetter(s -> s.direction),
                Codec.INT.fieldOf("refresh_interval").forGetter(s -> s.refreshInterval),
                Codec.INT.fieldOf("refresh_record").forGetter(s -> s.refreshRecord),
                Codec.INT.fieldOf("remaining_stock").forGetter(s -> s.remainingStock),
                Codec.INT.fieldOf("stock_limit").forGetter(s -> s.stockLimit),
                Codec.INT.fieldOf("per_player_limit").forGetter(s -> s.perPlayerLimit),
                Codec.INT.fieldOf("stones_needed").forGetter(s -> s.stonesNeeded),
                Codec.INT.fieldOf("stored_stones").forGetter(s -> s.storedStones),
                Codec.STRING.fieldOf("refresh_countdown_text").forGetter(s -> s.refreshCountdownText),
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("player_purchases").forGetter(s -> s.playerPurchases)
        ).apply(instance, ShopState::new));

        public boolean tiandao;
        public String ownerUuid = "";
        public String direction = "";
        public int refreshInterval;
        public int refreshRecord;
        public int remainingStock;
        public int stockLimit;
        public int perPlayerLimit;
        public int stonesNeeded;
        public int storedStones;
        public String refreshCountdownText = "";
        public final Map<String, Integer> playerPurchases = new LinkedHashMap<>();

        public ShopState() {
        }

        private ShopState(boolean tiandao, String ownerUuid, String direction, int refreshInterval, int refreshRecord,
                          int remainingStock, int stockLimit, int perPlayerLimit, int stonesNeeded, int storedStones,
                          String refreshCountdownText, Map<String, Integer> playerPurchases) {
            this.tiandao = tiandao;
            this.ownerUuid = ownerUuid;
            this.direction = direction;
            this.refreshInterval = refreshInterval;
            this.refreshRecord = refreshRecord;
            this.remainingStock = remainingStock;
            this.stockLimit = stockLimit;
            this.perPlayerLimit = perPlayerLimit;
            this.stonesNeeded = stonesNeeded;
            this.storedStones = storedStones;
            this.refreshCountdownText = refreshCountdownText;
            this.playerPurchases.putAll(playerPurchases);
        }
    }

    /** 拍卖 NPC 需求条目。 */
    public record NeedSlot(String tag, String type, int quality, int level) {
        public static final Codec<NeedSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("tag").forGetter(NeedSlot::tag),
                Codec.STRING.fieldOf("type").forGetter(NeedSlot::type),
                Codec.INT.fieldOf("quality").forGetter(NeedSlot::quality),
                Codec.INT.fieldOf("level").forGetter(NeedSlot::level)
        ).apply(instance, NeedSlot::new));
    }

    /** 拍卖行参拍 NPC 运行时状态。 */
    public static final class AuctionNpcState {
        public static final Codec<AuctionNpcState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("uuid").forGetter(n -> n.uuid),
                Codec.STRING.fieldOf("name").forGetter(n -> n.name),
                Codec.INT.fieldOf("level").forGetter(n -> n.level),
                Codec.INT.fieldOf("emotion").forGetter(n -> n.emotion),
                NeedSlot.CODEC.listOf().fieldOf("needs").forGetter(n -> n.needs),
                Codec.INT.fieldOf("max_price").forGetter(n -> n.maxPrice),
                Codec.INT.fieldOf("min_price").forGetter(n -> n.minPrice),
                Codec.INT.fieldOf("willingness").forGetter(n -> n.willingness),
                Codec.INT.fieldOf("bid_count").forGetter(n -> n.bidCount),
                Codec.INT.fieldOf("bid_weight").forGetter(n -> n.bidWeight),
                Codec.BOOL.fieldOf("abandoned").forGetter(n -> n.abandoned),
                Codec.BOOL.fieldOf("provoked").forGetter(n -> n.provoked),
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("likes").forGetter(n -> n.likes)
        ).apply(instance, AuctionNpcState::new));

        public String uuid = "";
        public String name = "";
        public int level;
        public int emotion;
        public final List<NeedSlot> needs = new ArrayList<>();
        public int maxPrice;
        public int minPrice;
        public int willingness;
        public int bidCount;
        public int bidWeight;
        public boolean abandoned;
        public boolean provoked;
        public final Map<String, Integer> likes = new LinkedHashMap<>();

        public AuctionNpcState() {
        }

        private AuctionNpcState(String uuid, String name, int level, int emotion, List<NeedSlot> needs,
                                int maxPrice, int minPrice, int willingness, int bidCount, int bidWeight,
                                boolean abandoned, boolean provoked, Map<String, Integer> likes) {
            this.uuid = uuid;
            this.name = name;
            this.level = level;
            this.emotion = emotion;
            this.needs.addAll(needs);
            this.maxPrice = maxPrice;
            this.minPrice = minPrice;
            this.willingness = willingness;
            this.bidCount = bidCount;
            this.bidWeight = bidWeight;
            this.abandoned = abandoned;
            this.provoked = provoked;
            this.likes.putAll(likes);
        }

        public void resetForRound() {
            maxPrice = 0;
            minPrice = 0;
            willingness = 0;
            bidCount = 0;
            bidWeight = 0;
            abandoned = false;
            provoked = false;
        }
    }

    /** 拍卖出价记录。 */
    public record AuctionBid(int npcIndex, String uuid, int price) {
        public static final Codec<AuctionBid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("npc_index").forGetter(AuctionBid::npcIndex),
                Codec.STRING.fieldOf("uuid").forGetter(AuctionBid::uuid),
                Codec.INT.fieldOf("price").forGetter(AuctionBid::price)
        ).apply(instance, AuctionBid::new));
    }

    /** 拍卖控制台基础配置分组。 */
    public record AuctionConfig(
            int placedCount,
            String name,
            String uuid,
            String mode,
            String tier,
            String legality,
            int itemCount,
            boolean managed) {
        public static final AuctionConfig DEFAULT = new AuctionConfig(0, "", "", "", "", "", 0, false);

        public static final Codec<AuctionConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("placed_count").forGetter(AuctionConfig::placedCount),
                Codec.STRING.fieldOf("name").forGetter(AuctionConfig::name),
                Codec.STRING.fieldOf("uuid").forGetter(AuctionConfig::uuid),
                Codec.STRING.fieldOf("mode").forGetter(AuctionConfig::mode),
                Codec.STRING.fieldOf("tier").forGetter(AuctionConfig::tier),
                Codec.STRING.fieldOf("legality").forGetter(AuctionConfig::legality),
                Codec.INT.fieldOf("item_count").forGetter(AuctionConfig::itemCount),
                Codec.BOOL.fieldOf("managed").forGetter(AuctionConfig::managed)
        ).apply(instance, AuctionConfig::new));
    }

    /** 拍卖控制台运行时进度分组（与配置一起保证 RecordCodec 字段数在十六以内）。 */
    public record AuctionRuntimeCounters(
            int tick,
            int cooldown,
            int stage,
            int npcCount,
            int commoditySlot,
            int commodityPrice,
            int commodityUpCount,
            int subTick,
            boolean provoked,
            int provokedNpc) {
        public static final Codec<AuctionRuntimeCounters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("tick").forGetter(AuctionRuntimeCounters::tick),
                Codec.INT.fieldOf("cooldown").forGetter(AuctionRuntimeCounters::cooldown),
                Codec.INT.fieldOf("stage").forGetter(AuctionRuntimeCounters::stage),
                Codec.INT.fieldOf("npc_count").forGetter(AuctionRuntimeCounters::npcCount),
                Codec.INT.fieldOf("commodity_slot").forGetter(AuctionRuntimeCounters::commoditySlot),
                Codec.INT.fieldOf("commodity_price").forGetter(AuctionRuntimeCounters::commodityPrice),
                Codec.INT.fieldOf("commodity_up_count").forGetter(AuctionRuntimeCounters::commodityUpCount),
                Codec.INT.fieldOf("sub_tick").forGetter(AuctionRuntimeCounters::subTick),
                Codec.BOOL.fieldOf("provoked").forGetter(AuctionRuntimeCounters::provoked),
                Codec.INT.fieldOf("provoked_npc").forGetter(AuctionRuntimeCounters::provokedNpc)
        ).apply(instance, AuctionRuntimeCounters::new));
    }

    /** 拍卖控制台运行时状态（拆分子分组，确保 Codec 字段数量受控且强类型）。 */
    public static final class AuctionState {
        public static final Codec<AuctionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                AuctionConfig.CODEC.fieldOf("config").forGetter(s -> new AuctionConfig(
                        s.placedCount, s.auctionName, s.auctionUuid, s.auctionMode, s.auctionTier, s.auctionLegality,
                        s.auctionItemCount, s.managed)),
                AuctionRuntimeCounters.CODEC.fieldOf("counters").forGetter(s -> new AuctionRuntimeCounters(
                        s.auctionTick, s.cooldown, s.stage, s.npcCount, s.commoditySlot, s.commodityPrice,
                        s.commodityUpCount, s.subTick, s.provoked, s.provokedNpcIndex)),
                Codec.unboundedMap(Codec.STRING, BlockPos.CODEC).fieldOf("seats").forGetter(s -> encodeSeatMap(s.seats)),
                Codec.STRING.listOf().fieldOf("dialogues").forGetter(s -> List.copyOf(s.dialogues)),
                Codec.STRING.listOf().fieldOf("display_lines").forGetter(s -> List.copyOf(s.displayLines)),
                Codec.INT.listOf().fieldOf("bidders").forGetter(s -> List.copyOf(s.bidderIndices)),
                AuctionBid.CODEC.listOf().fieldOf("bids").forGetter(s -> List.copyOf(s.bids)),
                Codec.unboundedMap(Codec.STRING, AuctionNpcState.CODEC).fieldOf("npcs").forGetter(s -> encodeNpcMap(s.npcs)),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("item_data").forGetter(s -> encodeStringMap(s.itemData))
        ).apply(instance, (cfg, counters, seats, dialogues, displayLines, bidders, bids, npcs, itemData) ->
                new AuctionState(cfg, counters.tick(), counters.cooldown(), counters.stage(), counters.npcCount(),
                        counters.commoditySlot(), counters.commodityPrice(), counters.commodityUpCount(),
                        counters.subTick(), counters.provoked(), counters.provokedNpc(),
                        seats, dialogues, displayLines, bidders, bids, npcs, itemData)));

        public int placedCount;
        public String auctionName = "";
        public String auctionUuid = "";
        public String auctionMode = "";
        public String auctionTier = "";
        public String auctionLegality = "";
        public int auctionItemCount;
        public boolean managed;
        public int auctionTick;
        public int cooldown;
        public int stage;
        public int npcCount;
        public int commoditySlot;
        public int commodityPrice;
        public int commodityUpCount;
        public int subTick;
        public boolean provoked;
        public int provokedNpcIndex;
        public int bidderCount;
        public final Map<Integer, BlockPos> seats = new LinkedHashMap<>();
        public final List<String> dialogues = new ArrayList<>();
        public final List<String> displayLines = new ArrayList<>();
        public final List<Integer> bidderIndices = new ArrayList<>();
        public final List<AuctionBid> bids = new ArrayList<>();
        public final Map<Integer, AuctionNpcState> npcs = new LinkedHashMap<>();
        public final Map<Integer, String> itemData = new LinkedHashMap<>();

        public AuctionState() {
            for (int i = 0; i < 20; i++) displayLines.add("");
        }

        private AuctionState(AuctionConfig cfg, int tick, int cooldown, int stage, int npcCount, int slot, int price,
                             int upCount, int subTick, boolean provoked, int provokedNpc,
                             Map<String, BlockPos> seats, List<String> dialogues, List<String> displayLines,
                             List<Integer> bidders, List<AuctionBid> bids, Map<String, AuctionNpcState> npcs,
                             Map<String, String> itemData) {
            this.placedCount = cfg.placedCount();
            this.auctionName = cfg.name();
            this.auctionUuid = cfg.uuid();
            this.auctionMode = cfg.mode();
            this.auctionTier = cfg.tier();
            this.auctionLegality = cfg.legality();
            this.auctionItemCount = cfg.itemCount();
            this.managed = cfg.managed();
            this.auctionTick = tick;
            this.cooldown = cooldown;
            this.stage = stage;
            this.npcCount = npcCount;
            this.commoditySlot = slot;
            this.commodityPrice = price;
            this.commodityUpCount = upCount;
            this.subTick = subTick;
            this.provoked = provoked;
            this.provokedNpcIndex = provokedNpc;
            seats.forEach((k, v) -> this.seats.put(Integer.parseInt(k), v));
            this.dialogues.addAll(dialogues);
            this.displayLines.addAll(displayLines);
            while (this.displayLines.size() < 20) this.displayLines.add("");
            this.bidderIndices.addAll(bidders);
            this.bidderCount = bidders.size();
            this.bids.addAll(bids);
            npcs.forEach((k, v) -> this.npcs.put(Integer.parseInt(k), v));
            itemData.forEach((k, v) -> this.itemData.put(Integer.parseInt(k), v));
        }

        private static <V> Map<String, V> encodeMap(Map<Integer, V> source) {
            Map<String, V> result = new LinkedHashMap<>();
            source.forEach((k, v) -> result.put(Integer.toString(k), v));
            return result;
        }

        private static Map<String, BlockPos> encodeSeatMap(Map<Integer, BlockPos> source) {
            return encodeMap(source);
        }

        private static Map<String, AuctionNpcState> encodeNpcMap(Map<Integer, AuctionNpcState> source) {
            return encodeMap(source);
        }

        private static Map<String, String> encodeStringMap(Map<Integer, String> source) {
            return encodeMap(source);
        }

        public String displayLine(int index1To20) {
            return index1To20 >= 1 && index1To20 <= displayLines.size() ? displayLines.get(index1To20 - 1) : "";
        }

        public void setDisplayLine(int index1To20, String text) {
            if (index1To20 >= 1 && index1To20 <= displayLines.size()) displayLines.set(index1To20 - 1, text);
        }
    }
}
