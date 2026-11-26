package com.ruivo.appraisal.compat.create;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * 机械动力的锌矿石增补。锌矿石需要铁镐，产物等级为配方等级加一。
 */
public final class CreateIntegration extends OreIntegration {

    public CreateIntegration() {
        super("create", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("create:zinc_ore", 8.0D, 1)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("create:deepslate_zinc_ore", 8.0D, 1)
                )
        ));
    }
}
