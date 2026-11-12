package net.yvanchuxiuzhen.system.equipment.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/** 临时法宝保留使用栈引用；最后一件消耗后单独保留来源仍可读取的物品标签及空栈状态。 */
public record FabaoTempData(ItemStack item, boolean consumed) {
    public static final FabaoTempData EMPTY = new FabaoTempData(ItemStack.EMPTY, false);
    public static final Codec<FabaoTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(FabaoTempData::item),
            Codec.BOOL.fieldOf("consumed").forGetter(FabaoTempData::consumed)
    ).apply(instance, FabaoTempData::new));
}
