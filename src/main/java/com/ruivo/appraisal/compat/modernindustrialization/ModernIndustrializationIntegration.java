package com.ruivo.appraisal.compat.modernindustrialization;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * 现代工业化的矿石增补。矿石统一是石镐，深板岩变种只覆盖部分矿物。
 */
public final class ModernIndustrializationIntegration extends OreIntegration {

    public ModernIndustrializationIntegration() {
        super("modern_industrialization", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("modern_industrialization:antimony_ore", 7.0D, 0),
                        new OreEntry("modern_industrialization:bauxite_ore", 10.0D, 0),
                        new OreEntry("modern_industrialization:iridium_ore", 3.0D, 0),
                        new OreEntry("modern_industrialization:lead_ore", 8.0D, 0),
                        new OreEntry("modern_industrialization:lignite_coal_ore", 10.0D, 0),
                        new OreEntry("modern_industrialization:monazite_ore", 5.0D, 0),
                        new OreEntry("modern_industrialization:nickel_ore", 8.0D, 0),
                        new OreEntry("modern_industrialization:platinum_ore", 3.0D, 0),
                        new OreEntry("modern_industrialization:quartz_ore", 7.0D, 0),
                        new OreEntry("modern_industrialization:salt_ore", 8.0D, 0),
                        new OreEntry("modern_industrialization:tin_ore", 9.0D, 0),
                        new OreEntry("modern_industrialization:titanium_ore", 4.0D, 0),
                        new OreEntry("modern_industrialization:tungsten_ore", 4.0D, 0),
                        new OreEntry("modern_industrialization:uranium_ore", 5.0D, 0)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("modern_industrialization:deepslate_antimony_ore", 7.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_bauxite_ore", 10.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_lead_ore", 8.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_lignite_coal_ore", 10.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_monazite_ore", 5.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_nickel_ore", 8.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_salt_ore", 8.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_tin_ore", 9.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_tungsten_ore", 4.0D, 0),
                        new OreEntry("modern_industrialization:deepslate_uranium_ore", 5.0D, 0)
                )
        ));
    }
}
