package net.yvanchuxiuzhen.system.creature;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou001Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou002Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.fulu.api.CorpseReleaseEvent;
import net.yvanchuxiuzhen.system.fulu.api.CorpseTalismans;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 妖兽尸体交互与封存释放：封存只搬运来源列出的字段，恢复后仍为死亡实体。 */
final class TalismanCorpseSealing {
    private static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));
    private static final Set<String> PRIOR_ITEMS = Set.of("fabao_015", "duedan", "tiandao_np_csetitem");
    private static final String[] NUMBERS = {
            "caijinumber", "LEVELnumber", "experience_Max", "experience", "health_Max", "health_Rec",
            "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att",
            "mana_Def", "soul_Att", "soul_Def", "speed_number"
    };
    private static final String[] STRINGS = {
            "LEVELname", "health_Max_LEVEL", "health_Rec_LEVEL", "power_Max_LEVEL", "power_Rec_LEVEL",
            "soul_Max_LEVEL", "soul_Rec_LEVEL", "attack_Num_LEVEL", "attack_Def_LEVEL", "mana_Att_LEVEL",
            "mana_Def_LEVEL", "soul_Att_LEVEL", "soul_Def_LEVEL", "speed_LEVEL"
    };
    private static final String[] CHARACTER_NUMBERS = {"character_def", "character_att", "character_interval", "character_resist"};

    private TalismanCorpseSealing() { }

    static void register() {
        NeoForge.EVENT_BUS.addListener(TalismanCorpseSealing::interact);
        NeoForge.EVENT_BUS.addListener(TalismanCorpseSealing::release);
    }

    private static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != player.getUsedItemHand()) return;
        ResourceLocation held = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem());
        if (held.getNamespace().equals(ModId.VALUE) && PRIOR_ITEMS.contains(held.getPath())) return;
        Entity corpse = event.getTarget();
        if (!corpse.getType().is(YAOSHOU) || corpse.isAlive()) return;
        if (player.isShiftKeyDown() && CorpseTalismans.hasEmptySeal(player)) {
            CorpseTalismans.seal(player, capture(corpse));
            corpse.discard();
        } else {
            YaoshouCorpseHarvest.harvest(player.serverLevel(), corpse);
        }
    }

    private static CompoundTag capture(Entity corpse) {
        CompoundTag data = new CompoundTag();
        copyState(corpse.getPersistentData(), data);
        LivingEntity living = (LivingEntity) corpse;
        data.putDouble("entitydaxiao", living.getAttribute(YaoshouAttributes.SIZE).getBaseValue());
        data.putString("yaoshouid", BuiltInRegistries.ENTITY_TYPE.getKey(corpse.getType()).toString());
        // 来源从妖兽读取仅附着玩家的能力，实际收纳团队为空串。
        data.putString("team", "");
        data.putString("yaoshouname", corpse.getDisplayName().getString().replace("[", "").replace("]", ""));
        data.putDouble("health_Num", 0);
        if ((corpse instanceof Yaoshou001Entity || corpse instanceof Yaoshou002Entity)
                && living.getAttribute(YaoshouAttributes.SKIN).getBaseValue() >= 1) {
            data.putDouble("yaoshoupifuxianshi", living.getAttribute(YaoshouAttributes.SKIN).getBaseValue());
        }
        return data;
    }

    private static void release(CorpseReleaseEvent event) {
        CompoundTag data = event.corpse();
        String identifier = data.getString("yaoshouid");
        Entity corpse = BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse(identifier)).orElseThrow().value().create(event.level());
        if (corpse == null) return;
        BlockPos pos = event.clickedPos();
        corpse.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, event.level().random.nextFloat() * 360, 0);
        copyState(data, corpse.getPersistentData());
        LivingEntity living = (LivingEntity) corpse;
        if (identifier.equals(ModId.VALUE + ":yaoshou_001") || identifier.equals(ModId.VALUE + ":yaoshou_002")) {
            living.getAttribute(YaoshouAttributes.SKIN).setBaseValue(data.getDouble("yaoshoupifuxianshi"));
        }
        living.getAttribute(YaoshouAttributes.SIZE).setBaseValue(data.getDouble("entitydaxiao"));
        corpse.getPersistentData().putString("yaoshouid", identifier);
        Relations.setTeam(corpse, data.getString("team"));
        corpse.setCustomName(Component.literal(data.getString("yaoshouname")));
        corpse.getPersistentData().putString("yaoshouname", data.getString("yaoshouname"));
        living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
        corpse.getPersistentData().putDouble("health_Num", 0);
        living.setHealth(0);
        if (corpse instanceof Mob mob) {
            mob.finalizeSpawn(event.level(), event.level().getCurrentDifficultyAt(corpse.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        }
        event.level().addFreshEntity(corpse);
    }

    private static void copyState(CompoundTag source, CompoundTag target) {
        for (String key : NUMBERS) target.putDouble(key, source.getDouble(key));
        for (String key : STRINGS) target.putString(key, source.getString(key));
        if (!source.getString("character").isEmpty()) {
            target.putString("character", source.getString("character"));
            for (String key : CHARACTER_NUMBERS) target.putDouble(key, source.getDouble(key));
        }
    }
}
