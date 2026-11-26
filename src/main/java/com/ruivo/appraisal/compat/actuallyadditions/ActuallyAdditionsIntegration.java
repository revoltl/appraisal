package com.ruivo.appraisal.compat.actuallyadditions;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * 实用拓展的黑石英矿石增补。黑石英矿石需要石镐，产物等级等于配方等级。
 */
public final class ActuallyAdditionsIntegration extends OreIntegration {

    public ActuallyAdditionsIntegration() {
        super("actuallyadditions", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("actuallyadditions:black_quartz_ore", 8.0D, 0)
                )
        ));
    }
}
