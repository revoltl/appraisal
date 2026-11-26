package com.ruivo.appraisal.compat.immersiveengineering;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * 沉浸工程的矿石增补。铝土矿是石镐，铅、镍、银、铀是铁镐。
 */
public final class ImmersiveEngineeringIntegration extends OreIntegration {

    public ImmersiveEngineeringIntegration() {
        super("immersiveengineering", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("immersiveengineering:ore_aluminum", 10.0D, 0),
                        new OreEntry("immersiveengineering:ore_lead", 8.0D, 1),
                        new OreEntry("immersiveengineering:ore_nickel", 8.0D, 1),
                        new OreEntry("immersiveengineering:ore_silver", 7.0D, 1),
                        new OreEntry("immersiveengineering:ore_uranium", 5.0D, 1)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("immersiveengineering:deepslate_ore_aluminum", 10.0D, 0),
                        new OreEntry("immersiveengineering:deepslate_ore_lead", 8.0D, 1),
                        new OreEntry("immersiveengineering:deepslate_ore_nickel", 8.0D, 1),
                        new OreEntry("immersiveengineering:deepslate_ore_silver", 7.0D, 1),
                        new OreEntry("immersiveengineering:deepslate_ore_uranium", 5.0D, 1)
                )
        ));
    }
}
