package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentSword;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 血刀的命中流血与潜行采集自身精血；数值和条件沿用来源过程。 */
public final class Fabao016Item extends ContentSword {
    public Fabao016Item(LegacyTier tier, Properties properties, List<Component> tooltip) {
        super(tier, properties, tooltip, false);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof ServerPlayer player) {
            applyBleed(target, player);
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer && serverPlayer.isShiftKeyDown()) {
            collectBlood(serverLevel, serverPlayer);
        }
        return result;
    }

    private static void applyBleed(LivingEntity target, ServerPlayer attacker) {
        if (attacker.isCreative()) {
            MobEffectInstance current = target.getEffect(StatusEffects.holder("effect_004"));
            int duration = current == null ? 200 : 200 + current.getDuration();
            int amplifier = current == null ? 0 : current.getAmplifier() + 1;
            StatusEffects.add(target, "effect_004", duration, amplifier);
            return;
        }
        if (target instanceof Player) {
            return;
        }
        // 来源过程读取未赋值的 ItemStack.EMPTY，其 lianqizong.item_level 默认值为 0。
        double levelNumber = target.getPersistentData().getDouble("LEVELnumber");
        if (levelNumber <= 1) {
            StatusEffects.add(target, "effect_004", 600, (int) (1 - levelNumber));
        }
    }

    private static void collectBlood(ServerLevel level, ServerPlayer player) {
        double cost = 10 + Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 3;
        if (player.getHealth() > cost) {
            player.setHealth((float) (player.getHealth() - cost));
            level.sendParticles(Particles.type("xvedi"), player.getX(), player.getY() + 1, player.getZ(), 15, .2, .2, .2, 1);
            ItemStack essence = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "jingxve")));
            essence.set(DataComponents.CUSTOM_NAME, Component.literal(player.getDisplayName().getString() + "的精血"));
            CompoundTag data = essence.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            data.putString("belongToMasterUUID", player.getStringUUID());
            data.putString("belongToName", player.getDisplayName().getString());
            data.putDouble("playerlevelnum", Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
            data.putString("playerlevel", Cultivation.get(player, CultivationText.LEVEL_NAME));
            essence.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            ItemHandlerHelper.giveItemToPlayer(player, essence);
        } else if (player.getMaxHealth() < cost) {
            player.displayClientMessage(Component.literal("§4你的血量上限过低，请穿戴高级盔甲或使用高级功法或为血量分配属性点，来提高血量上限。"), false);
        } else {
            player.displayClientMessage(Component.literal("§4你现在身受重伤，体内没有足够精血。"), false);
        }
    }
}
