package com.ruivo.appraisal.compat.ironsspellbooks;

import com.ruivo.appraisal.compat.OreIntegration;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

/**
 * Iron 的法术与魔法书的矿石增补。秘银矿石需要下界合金镐，
 * 奥术残骸在下界，按稀有材料给钻石镐等级。
 */
public final class IronsSpellbooksIntegration extends OreIntegration {

    public IronsSpellbooksIntegration() {
        super("irons_spellbooks", OutputKind.BLOCK, Map.of(
                Blocks.STONE, List.of(
                        new OreEntry("irons_spellbooks:mithril_ore", 5.0D, 3)
                ),
                Blocks.DEEPSLATE, List.of(
                        new OreEntry("irons_spellbooks:deepslate_mithril_ore", 5.0D, 3)
                ),
                Blocks.NETHERRACK, List.of(
                        new OreEntry("irons_spellbooks:arcane_debris", 2.0D, 2)
                )
        ));
    }
}
