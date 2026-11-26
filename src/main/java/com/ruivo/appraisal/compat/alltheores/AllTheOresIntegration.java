package com.ruivo.appraisal.compat.alltheores;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * All the Ores 的矿石增补。权重按各矿石的生成丰度换算，丰度取自模组自带的
 * placed_feature 与 configured_feature：count 乘以 size，再除以十取整。
 * 产物等级为配方等级加挖掘等级：木镐零、石镐零、铁镐一、钻石镐二、下界合金三。
 */
public final class AllTheOresIntegration extends OreIntegration {

    public AllTheOresIntegration() {
        super("alltheores", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("alltheores:aluminum_ore", 10.0D, 0),
                        new OreEntry("alltheores:tin_ore", 10.0D, 0),
                        new OreEntry("alltheores:cinnabar_ore", 8.0D, 1),
                        new OreEntry("alltheores:fluorite_ore", 8.0D, 1),
                        new OreEntry("alltheores:salt_ore", 8.0D, 0),
                        new OreEntry("alltheores:lead_ore", 7.0D, 0),
                        new OreEntry("alltheores:nickel_ore", 7.0D, 1),
                        new OreEntry("alltheores:osmium_ore", 7.0D, 2),
                        new OreEntry("alltheores:silver_ore", 7.0D, 0),
                        new OreEntry("alltheores:zinc_ore", 7.0D, 0),
                        new OreEntry("alltheores:uranium_ore", 5.0D, 1),
                        new OreEntry("alltheores:iridium_ore", 4.0D, 2),
                        new OreEntry("alltheores:platinum_ore", 2.0D, 2),
                        new OreEntry("alltheores:peridot_ore", 2.0D, 0),
                        new OreEntry("alltheores:ruby_ore", 2.0D, 0),
                        new OreEntry("alltheores:sapphire_ore", 2.0D, 0),
                        new OreEntry("alltheores:sulfur_ore", 2.0D, 1)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("alltheores:deepslate_aluminum_ore", 10.0D, 0),
                        new OreEntry("alltheores:deepslate_tin_ore", 10.0D, 0),
                        new OreEntry("alltheores:deepslate_cinnabar_ore", 8.0D, 1),
                        new OreEntry("alltheores:deepslate_fluorite_ore", 8.0D, 1),
                        new OreEntry("alltheores:deepslate_salt_ore", 8.0D, 0),
                        new OreEntry("alltheores:deepslate_lead_ore", 7.0D, 0),
                        new OreEntry("alltheores:deepslate_nickel_ore", 7.0D, 1),
                        new OreEntry("alltheores:deepslate_osmium_ore", 7.0D, 2),
                        new OreEntry("alltheores:deepslate_silver_ore", 7.0D, 0),
                        new OreEntry("alltheores:deepslate_zinc_ore", 7.0D, 0),
                        new OreEntry("alltheores:deepslate_uranium_ore", 5.0D, 1),
                        new OreEntry("alltheores:deepslate_iridium_ore", 4.0D, 2),
                        new OreEntry("alltheores:deepslate_platinum_ore", 2.0D, 2),
                        new OreEntry("alltheores:deepslate_peridot_ore", 2.0D, 0),
                        new OreEntry("alltheores:deepslate_ruby_ore", 2.0D, 0),
                        new OreEntry("alltheores:deepslate_sapphire_ore", 2.0D, 0),
                        new OreEntry("alltheores:deepslate_sulfur_ore", 2.0D, 1)
                ),
                Blocks.NETHERRACK, List.of(
                        new OreEntry("alltheores:nether_aluminum_ore", 10.0D, 0),
                        new OreEntry("alltheores:nether_tin_ore", 10.0D, 0),
                        new OreEntry("alltheores:nether_cinnabar_ore", 8.0D, 1),
                        new OreEntry("alltheores:nether_fluorite_ore", 8.0D, 1),
                        new OreEntry("alltheores:nether_salt_ore", 8.0D, 0),
                        new OreEntry("alltheores:nether_lead_ore", 7.0D, 0),
                        new OreEntry("alltheores:nether_nickel_ore", 7.0D, 1),
                        new OreEntry("alltheores:nether_osmium_ore", 7.0D, 2),
                        new OreEntry("alltheores:nether_silver_ore", 7.0D, 0),
                        new OreEntry("alltheores:nether_zinc_ore", 7.0D, 0),
                        new OreEntry("alltheores:nether_uranium_ore", 5.0D, 1),
                        new OreEntry("alltheores:nether_iridium_ore", 4.0D, 2),
                        new OreEntry("alltheores:nether_platinum_ore", 2.0D, 2),
                        new OreEntry("alltheores:nether_peridot_ore", 2.0D, 0),
                        new OreEntry("alltheores:nether_ruby_ore", 2.0D, 0),
                        new OreEntry("alltheores:nether_sapphire_ore", 2.0D, 0),
                        new OreEntry("alltheores:nether_sulfur_ore", 2.0D, 1)
                ),
                Blocks.END_STONE, List.of(
                        new OreEntry("alltheores:end_aluminum_ore", 10.0D, 0),
                        new OreEntry("alltheores:end_tin_ore", 10.0D, 0),
                        new OreEntry("alltheores:end_cinnabar_ore", 8.0D, 1),
                        new OreEntry("alltheores:end_fluorite_ore", 8.0D, 1),
                        new OreEntry("alltheores:end_salt_ore", 8.0D, 0),
                        new OreEntry("alltheores:end_lead_ore", 7.0D, 0),
                        new OreEntry("alltheores:end_nickel_ore", 7.0D, 1),
                        new OreEntry("alltheores:end_osmium_ore", 7.0D, 2),
                        new OreEntry("alltheores:end_silver_ore", 7.0D, 0),
                        new OreEntry("alltheores:end_zinc_ore", 7.0D, 0),
                        new OreEntry("alltheores:end_uranium_ore", 5.0D, 1),
                        new OreEntry("alltheores:end_iridium_ore", 4.0D, 2),
                        new OreEntry("alltheores:end_platinum_ore", 2.0D, 2),
                        new OreEntry("alltheores:end_peridot_ore", 2.0D, 0),
                        new OreEntry("alltheores:end_ruby_ore", 2.0D, 0),
                        new OreEntry("alltheores:end_sapphire_ore", 2.0D, 0),
                        new OreEntry("alltheores:end_sulfur_ore", 2.0D, 1)
                )
        ));
    }
}
