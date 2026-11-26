package com.ruivo.appraisal.compat.rftools;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * RF 工具箱的空间碎片矿。主世界、下界、末地各有一种，统一是铁镐。
 */
public final class RFToolsIntegration extends OreIntegration {

    public RFToolsIntegration() {
        super("rftoolsbase", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("rftoolsbase:dimensionalshard_overworld", 5.0D, 1)
                ),
                Blocks.NETHERRACK, List.of(
                        new OreEntry("rftoolsbase:dimensionalshard_nether", 5.0D, 1)
                ),
                Blocks.END_STONE, List.of(
                        new OreEntry("rftoolsbase:dimensionalshard_end", 5.0D, 1)
                )
        ));
    }
}
