package com.ruivo.appraisal.compat.mysticalagriculture;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * 神秘农业的矿石增补。下级精华是石镐，活化水晶是铁镐，离魂矿在下界且需要钻石镐。
 */
public final class MysticalAgricultureIntegration extends OreIntegration {

    public MysticalAgricultureIntegration() {
        super("mysticalagriculture", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("mysticalagriculture:inferium_ore", 8.0D, 0),
                        new OreEntry("mysticalagriculture:prosperity_ore", 6.0D, 1)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("mysticalagriculture:deepslate_inferium_ore", 8.0D, 0),
                        new OreEntry("mysticalagriculture:deepslate_prosperity_ore", 6.0D, 1)
                ),
                Blocks.NETHERRACK, List.of(
                        new OreEntry("mysticalagriculture:soulium_ore", 4.0D, 2)
                )
        ));
    }
}
