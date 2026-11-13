package net.yvanchuxiuzhen.system.guchong.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 单虫物品只在方块上方为空且点击顶面时释放；已认主分支恢复来源明确写入的字段。 */
public final class GuchongReleaseItem extends ContentItem {
    private static final String[] RATINGS = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
            "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"};
    private final String entityId;

    public GuchongReleaseItem(Properties properties, List<Component> tooltip, String entityId) {
        super(properties, tooltip);
        this.entityId = entityId;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos().above();
        if (context.getClickedFace() != Direction.UP || !level.isEmptyBlock(pos)) return InteractionResult.SUCCESS;
        ItemStack stack = context.getItemInHand();
        CompoundTag saved = GuchongJarData.read(stack);
        GuchongEntity insect = (GuchongEntity) GuchongSetup.entityType(entityId).get().create(level);
        insect.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, level.random.nextFloat() * 360F, 0);
        if (saved.getBoolean("guchongxvnfu")) {
            CompoundTag data = insect.getPersistentData();
            data.putString("LEVELname", saved.getString("LEVELname"));
            data.putDouble("LEVELnumber", saved.getDouble("LEVELnumber"));
            data.putBoolean("guchongxvnfu", true);
            insect.setCustomName(Component.literal(stack.getDisplayName().getString()));
            insect.tame(context.getPlayer());
            Relations.setTeam(insect, Relations.team(context.getPlayer()));
            for (String key : RATINGS) {
                data.putString(key + "_LEVEL", saved.getString(key + "_LEVEL"));
                if (!key.equals("speed")) data.putDouble(key, saved.getDouble(key));
            }
            data.putDouble("speed_number", saved.getDouble("speed_number"));
            insect.getAttribute(Attributes.MAX_HEALTH).setBaseValue(saved.getDouble("health_Max"));
            insect.setHealth((float) saved.getDouble("health_Num"));
            insect.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(saved.getDouble("attack_Num"));
            insect.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(saved.getDouble("speed_number"));
        }
        GuchongJarRuntime.finishSpawn(level, insect);
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
