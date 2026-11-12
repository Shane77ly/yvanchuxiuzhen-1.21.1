package net.yvanchuxiuzhen.system.equipment.data;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.equipment.api.AccessoryInventory;

/**
 * 四个饰品的服务端持久引用。来源首刻把原对象置为数量1后交给菜单，菜单的原位数量修改因此可被保存字段读到；
 * 替换槽对象则在关闭时回写。公开 API 只返回副本，客户端不能提交整份附件。
 */
public final class AccessoryData {
    public static final Codec<AccessoryData> CODEC = AccessoryInventory.CODEC.xmap(AccessoryData::new, AccessoryData::snapshot);
    private final List<ItemStack> items;

    public AccessoryData() {
        this(AccessoryInventory.EMPTY);
    }

    public AccessoryData(AccessoryInventory snapshot) {
        this(snapshot.head(), snapshot.necklace(), snapshot.belt(), snapshot.hand());
    }

    public AccessoryData(ItemStack head, ItemStack necklace, ItemStack belt, ItemStack hand) {
        items = List.of(head, necklace, belt, hand);
    }

    public ItemStack item(int slot) {
        return items.get(slot);
    }

    public AccessoryInventory snapshot() {
        return new AccessoryInventory(items.get(0), items.get(1), items.get(2), items.get(3));
    }
}
