package net.yvanchuxiuzhen.system.world.portal;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.yvanchuxiuzhen.platform.ModId;

/** 六组天道维度门户的来源身份、目标维度、POI 与临时区块票据。 */
public enum DimensionPortalSpec {
    TIANQVE("tianqve"),
    XIULUO("xiuluo"),
    RENJIAN("renjian"),
    YAOHUN("yaohun"),
    WUGUI("wugui"),
    DILING("diling");

    private static final Map<String, DimensionPortalSpec> BY_BLOCK = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(spec -> spec.portalId, Function.identity()));

    private final String dimensionName;
    private final String portalId;
    private final ResourceKey<Level> dimension;
    private final ResourceKey<PoiType> poiKey;
    private final TicketType<BlockPos> ticketType;

    DimensionPortalSpec(String dimensionName) {
        this.dimensionName = dimensionName;
        this.portalId = "dimension" + dimensionName + "_portal";
        ResourceLocation dimensionId = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimension" + dimensionName);
        this.dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
        this.poiKey = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, portalId));
        this.ticketType = TicketType.create(portalId, Vec3i::compareTo, 300);
    }

    public static DimensionPortalSpec fromPortalId(String id) {
        DimensionPortalSpec spec = BY_BLOCK.get(id);
        if (spec == null) {
            throw new IllegalArgumentException("未知天道门户：" + id);
        }
        return spec;
    }

    public String dimensionName() {
        return dimensionName;
    }

    public String portalId() {
        return portalId;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public ResourceKey<PoiType> poiKey() {
        return poiKey;
    }

    public TicketType<BlockPos> ticketType() {
        return ticketType;
    }
}
