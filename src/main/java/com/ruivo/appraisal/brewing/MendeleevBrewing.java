package com.ruivo.appraisal.brewing;

import com.ruivo.appraisal.registry.ModItems;
import com.ruivo.appraisal.registry.ModPotions;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/**
 * 门捷列夫药水的酿造链，全部走原版药水映射，JEI 的酿造分类可以直接读取。
 * 只保留基础药水的酿造，萤石粉升级链已经移除，强效药水仅用于作弊或数据包。
 */
public final class MendeleevBrewing {

    private MendeleevBrewing() {
    }

    public static void register(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(
                Potions.AWKWARD,
                ModItems.INSIGHT_ORB.get(),
                ModPotions.MENDELEEV);
    }
}
