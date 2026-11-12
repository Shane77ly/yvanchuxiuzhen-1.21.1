package net.yvanchuxiuzhen.system.lianqi;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableBlockEntity;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableMenu;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 修理台按已有需求缓存决定刷新或消费，材料依等级和玩家原生四十一格顺序选择。 */
public final class RepairTableRuntime {
    private static final TagKey<Item> FLYING = tag("flyfabao");
    private static final TagKey<Item> MATERIALS = tag("cailiao");
    private static final TagKey<Item> FLAMES = tag("huoyan");
    private static final List<TagKey<Item>> EQUIPMENT = List.of(tag("wuqi"), tag("fabao"), tag("toukui"),
            tag("xiongjia"), tag("kuzi"), tag("zhanxve"));

    private RepairTableRuntime() {
    }

    /** 自然入口沿来源右键事件使用正在使用的手；事件本身不取消后续物品行为。 */
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (event.getEntity() instanceof ServerPlayer player
                && event.getLevel().getBlockEntity(event.getPos()) instanceof RepairTableBlockEntity table) {
            player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new RepairTableMenu(id, inventory, table),
                    table.getDisplayName()), event.getPos());
        }
    }

    public static void opened(RepairTableBlockEntity table) {
        if (table.getItem(0).isEmpty()) {
            table.getPersistentData().putString("needcailiao", "请把需要修理的物品放入上面的物品槽位中。");
            table.changed();
        }
    }

    public static void button(ServerPlayer player, RepairTableBlockEntity table, int button) {
        if (button == 1) {
            player.displayClientMessage(Component.literal("请把需要修理的物品放进中间的物品槽中，修理台会自动检测所需的材料。"), false);
            player.displayClientMessage(Component.literal("修理台会优先消耗背包中符合需求等级区间内，等级最低的材料，且永远不会消耗上品以及极品材料。"), false);
            player.displayClientMessage(Component.literal("飞行法器的血量，就是其耐久，可以在修理台内修理。"), false);
            return;
        }
        ItemStack stack = table.getItem(0);
        boolean flying = stack.is(FLYING);
        if (!flying && EQUIPMENT.stream().noneMatch(stack::is)) return;
        int cached = table.getPersistentData().getInt("needcailiaolevel");
        if (cached < 1 || !flying && cached != Equipment.refiningLevel(stack)) {
            refresh(table);
            return;
        }
        refresh(table);
        MaterialInstance material = consumeMaterial(player, table.getPersistentData().getInt("needcailiaolevel"));
        if (material == null) {
            table.getPersistentData().putString("needcailiao", "您的身上没有携带对应等级的材料，无法修理。");
        } else {
            if (flying) Equipment.repairFlyingInstrument(stack, 100);
            else stack.setDamageValue((int) (stack.getDamageValue() - Math.pow(material.level(), 2) * 20));
            table.getPersistentData().putString("needcailiao", "修理一次！");
            ServerLevel level = player.serverLevel();
            BlockPos pos = table.getBlockPos();
            LianqiDelays.run(40, () -> {
                if (level.getBlockEntity(pos) instanceof RepairTableBlockEntity current) {
                    current.getPersistentData().putString("needcailiao", "");
                    refresh(current);
                }
            });
        }
        table.changed();
    }

    /** 刷新不清除无级或不支持物品的旧需求；满耐久分支才清空需求和类型。 */
    public static void refresh(RepairTableBlockEntity table) {
        ItemStack stack = table.getItem(0);
        CompoundTag data = table.getPersistentData();
        if (stack.is(FLYING)) {
            if (Equipment.flyingInstrumentNeedsRepair(stack)) {
                int minimum = BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(
                        ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "flyinginstrument_0801")) ? 11 : 1;
                data.putString("needcailiao", "每修理100点耐久，需要任意" + minimum + "到" + (minimum + 4) + "级材料一个。");
                data.putInt("needcailiaolevel", minimum);
                data.putString("xiulileixing", "飞行法器");
            } else clearRequirement(data);
        } else if (EQUIPMENT.stream().anyMatch(stack::is)) {
            int level = Equipment.refiningLevel(stack);
            if (level < 1) {
                data.putString("needcailiao", "请放置有等级的武器、法宝、护甲。");
            } else if (stack.getDamageValue() > 0) {
                data.putString("needcailiao", "需要" + level + "到" + ((long) level + 4) + "级的材料才可修复");
                data.putInt("needcailiaolevel", level);
                data.putString("xiulileixing", "武器法宝护甲");
            } else clearRequirement(data);
        } else data.putString("needcailiao", "此物无法由修理台修理。");
        table.changed();
    }

    private static void clearRequirement(CompoundTag data) {
        data.putString("needcailiao", "无需修理。");
        data.putInt("needcailiaolevel", 0);
        data.putString("xiulileixing", "");
    }

    private static MaterialInstance consumeMaterial(ServerPlayer player, int minimum) {
        for (int offset = 0; offset < 5; offset++) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack candidate = player.getInventory().getItem(slot);
                MaterialInstance instance = Materials.instance(candidate);
                if (candidate.is(MATERIALS) && !candidate.is(FLAMES)
                        && instance.level() == (double) minimum + offset && instance.quality() <= 2) {
                    candidate.shrink(1);
                    return instance;
                }
            }
        }
        return null;
    }

    private static TagKey<Item> tag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
