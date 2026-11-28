package net.yvanchuxiuzhen.system.economy.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 玩家商贸、商店列表与拍卖定位会话，强类型持久化并同步给本人客户端。 */
public record CommerceSession(
        ShopCatalogDefinition shopCatalog,
        int shopPage,
        String storeName,
        String storeOwner,
        String storeDescription,
        List<String> shopNameTexts,
        List<String> shopPriceTexts,
        int playerChoice,
        String bankTest1,
        String bankTest2,
        int auctionBlockX,
        int auctionBlockY,
        int auctionBlockZ) {

    public static final CommerceSession EMPTY = new CommerceSession(
            ShopCatalogDefinition.EMPTY, 0, "", "", "",
            Collections.nCopies(12, ""), Collections.nCopies(12, ""),
            0, "", "", 0, 0, 0);

    public static final Codec<CommerceSession> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ShopCatalogDefinition.CODEC.fieldOf("shop_catalog").forGetter(CommerceSession::shopCatalog),
            Codec.INT.fieldOf("shop_page").forGetter(CommerceSession::shopPage),
            Codec.STRING.fieldOf("store_name").forGetter(CommerceSession::storeName),
            Codec.STRING.fieldOf("store_owner").forGetter(CommerceSession::storeOwner),
            Codec.STRING.fieldOf("store_description").forGetter(CommerceSession::storeDescription),
            Codec.STRING.listOf().fieldOf("shop_name_texts").forGetter(CommerceSession::shopNameTexts),
            Codec.STRING.listOf().fieldOf("shop_price_texts").forGetter(CommerceSession::shopPriceTexts),
            Codec.INT.fieldOf("player_choice").forGetter(CommerceSession::playerChoice),
            Codec.STRING.fieldOf("bank_test_1").forGetter(CommerceSession::bankTest1),
            Codec.STRING.fieldOf("bank_test_2").forGetter(CommerceSession::bankTest2),
            Codec.INT.fieldOf("auction_x").forGetter(CommerceSession::auctionBlockX),
            Codec.INT.fieldOf("auction_y").forGetter(CommerceSession::auctionBlockY),
            Codec.INT.fieldOf("auction_z").forGetter(CommerceSession::auctionBlockZ)
    ).apply(instance, CommerceSession::new));

    public static final StreamCodec<ByteBuf, CommerceSession> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public String shopNameText(int index) {
        return index >= 0 && index < shopNameTexts.size() ? shopNameTexts.get(index) : "";
    }

    public String shopPriceText(int index) {
        return index >= 0 && index < shopPriceTexts.size() ? shopPriceTexts.get(index) : "";
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private ShopCatalogDefinition shopCatalog;
        private int shopPage;
        private String storeName;
        private String storeOwner;
        private String storeDescription;
        private final List<String> shopNameTexts;
        private final List<String> shopPriceTexts;
        private int playerChoice;
        private String bankTest1;
        private String bankTest2;
        private int auctionBlockX;
        private int auctionBlockY;
        private int auctionBlockZ;

        private Builder(CommerceSession source) {
            this.shopCatalog = source.shopCatalog();
            this.shopPage = source.shopPage();
            this.storeName = source.storeName();
            this.storeOwner = source.storeOwner();
            this.storeDescription = source.storeDescription();
            this.shopNameTexts = new ArrayList<>(source.shopNameTexts());
            while (this.shopNameTexts.size() < 12) this.shopNameTexts.add("");
            this.shopPriceTexts = new ArrayList<>(source.shopPriceTexts());
            while (this.shopPriceTexts.size() < 12) this.shopPriceTexts.add("");
            this.playerChoice = source.playerChoice();
            this.bankTest1 = source.bankTest1();
            this.bankTest2 = source.bankTest2();
            this.auctionBlockX = source.auctionBlockX();
            this.auctionBlockY = source.auctionBlockY();
            this.auctionBlockZ = source.auctionBlockZ();
        }

        public Builder shopCatalog(ShopCatalogDefinition value) { this.shopCatalog = value; return this; }
        public Builder shopPage(int value) { this.shopPage = value; return this; }
        public Builder storeName(String value) { this.storeName = value; return this; }
        public Builder storeOwner(String value) { this.storeOwner = value; return this; }
        public Builder storeDescription(String value) { this.storeDescription = value; return this; }
        public Builder shopNameText(int index, String value) { this.shopNameTexts.set(index, value); return this; }
        public Builder shopPriceText(int index, String value) { this.shopPriceTexts.set(index, value); return this; }
        public Builder playerChoice(int value) { this.playerChoice = value; return this; }
        public Builder bankTest1(String value) { this.bankTest1 = value; return this; }
        public Builder bankTest2(String value) { this.bankTest2 = value; return this; }
        public Builder auctionBlockPos(int x, int y, int z) {
            this.auctionBlockX = x;
            this.auctionBlockY = y;
            this.auctionBlockZ = z;
            return this;
        }

        public CommerceSession build() {
            return new CommerceSession(shopCatalog, shopPage, storeName, storeOwner, storeDescription,
                    List.copyOf(shopNameTexts), List.copyOf(shopPriceTexts), playerChoice, bankTest1, bankTest2,
                    auctionBlockX, auctionBlockY, auctionBlockZ);
        }
    }
}
