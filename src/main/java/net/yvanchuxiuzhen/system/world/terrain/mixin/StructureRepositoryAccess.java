package net.yvanchuxiuzhen.system.world.terrain.mixin;

import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 地府首次生成前保留原生结构缓存的优先级，包括未写盘的结构方块模板。 */
@Mixin(StructureTemplateManager.class)
public interface StructureRepositoryAccess {
    @Accessor("structureRepository")
    Map<ResourceLocation, Optional<StructureTemplate>> yvanchuxiuzhen$structureRepository();
}
