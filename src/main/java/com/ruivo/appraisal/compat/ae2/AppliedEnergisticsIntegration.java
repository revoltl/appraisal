package com.ruivo.appraisal.compat.ae2;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * 应用能源的赛特斯石英水晶增补。AE2 1.21.1 没有原矿，
 * 水晶按紫水晶的概率出现，鉴定等级比紫水晶高一级。
 */
public final class AppliedEnergisticsIntegration extends OreIntegration {

    public AppliedEnergisticsIntegration() {
        super("ae2", OutputKind.ITEM, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("ae2:certus_quartz_crystal", 6.0D, 1)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("ae2:certus_quartz_crystal", 12.0D, 1)
                )
        ));
    }
}
