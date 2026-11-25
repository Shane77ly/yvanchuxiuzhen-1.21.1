package net.yvanchuxiuzhen.system.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlockEntity;
import net.yvanchuxiuzhen.system.world.api.StructureReleasedEvent;

/** 任务伪装方块配置与交互的唯一写入者；经济会话通过经济系统公开入口更新。 */
public final class DisguisedQuestRuntime {
    private DisguisedQuestRuntime() {
    }

    public static void structureReleased(StructureReleasedEvent event) {
        ServerLevel level = event.level();
        if (event.structureName().equals("花繁城")) {
            BlockPos pos = new BlockPos(-114, 64, -277);
            level.setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                    "renwuweizhuangfangkuai")).defaultBlockState(), 3);
            if (level.getBlockEntity(pos) instanceof DisguisedQuestBlockEntity block) {
                block.setItem(0, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "book_022"))));
                block.setShowParticles(true);
            }
        }
        QuestStructureData.apply(level, event.structureName());
    }

    public static void interact(ServerPlayer player, DisguisedQuestBlockEntity block) {
        CompoundTag journal = DialogueActions.journal(player);
        for (int index = 1; index <= 10; index++) {
            String script = block.renwulist(index);
            if (script.isEmpty()) script = block.tasklist(index);
            if (script.isEmpty()) return;
            script = Component.translatable(script).getString();
            String id = script.split("%")[0].split("/")[1];
            if (journal.getString(id + "UUID").equals(id) && DialogueActions.finished(player, id)) continue;
            String information = block.shopInformation(index);
            if (!information.isEmpty()) {
                EconomyMenus.setShopInformation(player, information);
                EconomyMenus.setProductList(player, block.productList(index));
            }
            DialogueActions.request(player, script);
            return;
        }
    }

    public static void tick(ServerLevel level, DisguisedQuestBlockEntity block) {
        BlockPos pos = block.getBlockPos();
        if (block.showParticles()) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
                    3, .5, .5, .5, 0);
        }
        int last = block.refreshTick();
        long now = level.getDayTime();
        if (last + 24000 <= now || last > now || last == 0) {
            block.setRefreshDue(true);
            block.setRefreshTick((int) now);
        }
    }
}
