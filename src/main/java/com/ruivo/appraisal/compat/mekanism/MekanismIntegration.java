package com.ruivo.appraisal.compat.mekanism;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * 通用机械的矿石增补。矿石全部是石镐，产物等级等于配方等级。
 */
public final class MekanismIntegration extends OreIntegration {

    public MekanismIntegration() {
        super("mekanism", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("mekanism:tin_ore", 15.0D, 0),
                        new OreEntry("mekanism:osmium_ore", 12.0D, 0),
                        new OreEntry("mekanism:uranium_ore", 8.0D, 0),
                        new OreEntry("mekanism:fluorite_ore", 10.0D, 0),
                        new OreEntry("mekanism:lead_ore", 10.0D, 0)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("mekanism:deepslate_tin_ore", 15.0D, 0),
                        new OreEntry("mekanism:deepslate_osmium_ore", 12.0D, 0),
                        new OreEntry("mekanism:deepslate_uranium_ore", 8.0D, 0),
                        new OreEntry("mekanism:deepslate_fluorite_ore", 10.0D, 0),
                        new OreEntry("mekanism:deepslate_lead_ore", 10.0D, 0)
                )
        ));
    }
}
