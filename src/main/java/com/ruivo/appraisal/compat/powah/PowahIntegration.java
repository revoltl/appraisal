package com.ruivo.appraisal.compat.powah;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * 能源之力的矿石增补。晶质铀矿石需要铁镐，致密矿脉更稀有。
 */
public final class PowahIntegration extends OreIntegration {

    public PowahIntegration() {
        super("powah", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("powah:uraninite_ore", 8.0D, 1),
                        new OreEntry("powah:uraninite_ore_poor", 6.0D, 1),
                        new OreEntry("powah:uraninite_ore_dense", 4.0D, 1)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("powah:deepslate_uraninite_ore", 8.0D, 1),
                        new OreEntry("powah:deepslate_uraninite_ore_poor", 6.0D, 1),
                        new OreEntry("powah:deepslate_uraninite_ore_dense", 4.0D, 1)
                )
        ));
    }
}
