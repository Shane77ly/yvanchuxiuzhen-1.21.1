package net.yvanchuxiuzhen.system.material;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.material.content.JadeRoughItem;
import net.yvanchuxiuzhen.system.material.content.MaterialAnalysisMenu;
import net.yvanchuxiuzhen.system.material.content.MaterialMenus;
import net.yvanchuxiuzhen.system.material.content.MoltenMetalItem;
import net.yvanchuxiuzhen.system.material.content.PeachFoodItem;
import net.yvanchuxiuzhen.system.material.content.MaterialOreBlock;
import net.yvanchuxiuzhen.system.material.data.MaterialData;
import net.yvanchuxiuzhen.system.material.network.MaterialAnalysisButtonPayload;
import net.yvanchuxiuzhen.system.material.network.MaterialAnalysisViewPayload;

/**
 * 材料系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class MaterialSetup {
    private static final ContentTables CONTENT = new ContentTables("material")
            .blockFactory("material:ore_with_fallback", MaterialOreBlock::createOre)
            .itemFactory("material:peach_food", PeachFoodItem::new);

    static {
        for (int number = 161; number <= 169; number++) {
            int jade = number;
            CONTENT.itemFactory("material:jade_" + number,
                    (properties, tooltip) -> new JadeRoughItem(properties, tooltip, jade));
        }
        for (int first : new int[]{201, 211}) {
            for (int index = 0; index < 9; index++) {
                int number = first + index;
                int level = Math.min(40, 3 + 5 * index);
                CONTENT.itemFactory("material:molten_metal_" + number,
                        (properties, tooltip) -> new MoltenMetalItem(properties, tooltip, number, level));
            }
        }
    }

    private MaterialSetup() {
    }

    public static void register(IEventBus modBus) {
        MaterialData.register(modBus);
        MaterialMenus.register(modBus);
        CONTENT.register(modBus);
        modBus.addListener(MaterialSetup::registerPayloads);
        NeoForge.EVENT_BUS.addListener(MaterialSetup::onCrafted);
        NeoForge.EVENT_BUS.addListener(MaterialSetup::onItemFished);
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) -> {
            if (event.getEntity() instanceof ServerPlayer player
                    && BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString().equals("yvanchuxiuzhen:qiancailubook")) {
                player.openMenu(new SeamlessMenuProvider((id, inventory, owner) -> new MaterialAnalysisMenu(id, inventory, player.blockPosition()),
                        Component.literal("QiancailuGui")), player.blockPosition());
            }
        });
    }

    /** 来源在玩家取出合成结果时写入材料等级和品阶，保留结果上的其他组件。 */
    private static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(result.getItem());
        if (!id.getNamespace().equals(ModId.VALUE)) {
            return;
        }
        int level = switch (id.getPath()) {
            case "cailiao_241", "cailiao_191" -> 5;
            case "cailiao_192" -> 10;
            case "cailiao_193" -> 15;
            case "cailiao_194" -> 20;
            case "cailiao_195" -> 25;
            case "cailiao_196" -> 30;
            case "cailiao_197" -> 35;
            case "cailiao_198" -> 40;
            case "cailiao_199" -> 45;
            default -> 0;
        };
        if (level != 0) {
            MaterialLibrary.setInstance(result, level, id.getPath().equals("cailiao_241") ? 2 : 4);
        }
    }

    /** 来源 {@code FishingProcedure}：玩家钓鱼成功时按 1–100 掷骰额外获得炼器材料。 */
    private static void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        int roll = Mth.nextInt(player.getRandom(), 1, 100);
        String reward = switch (roll) {
            case 1, 2, 3, 4 -> "cailiao_021";
            case 5, 6 -> "cailiao_022";
            case 7 -> player.getRandom().nextDouble() < 0.5 ? "cailiao_027" : "cailiao_031";
            case 8 -> player.getRandom().nextDouble() < 0.5 ? "cailiao_023" : "cailiao_032";
            default -> "";
        };
        if (!reward.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, reward))));
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(MaterialAnalysisButtonPayload.TYPE, MaterialAnalysisButtonPayload.STREAM_CODEC, MaterialAnalysisButtonPayload::handle)
                .playToClient(MaterialAnalysisViewPayload.TYPE, MaterialAnalysisViewPayload.STREAM_CODEC, MaterialAnalysisViewPayload::handle);
    }
}
