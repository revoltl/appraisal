package com.ruivo.appraisal.appraisal;

import com.ruivo.appraisal.item.AppraisalTool;
import com.ruivo.appraisal.registry.ModAttachments;
import com.ruivo.appraisal.registry.ModEffects;
import com.ruivo.appraisal.registry.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * 鉴定等级的计算。有效等级由玩家基础等级、手持工具等级、列文虎克附魔与门捷列夫之洞察
 * 相加得到。
 *
 * <p>工具与附魔都从实际使用的那件物品读取。主手鉴定与副手鉴定各自按手里的工具计算，
 * 不需要额外的换手补偿。</p>
 */
public final class AppraisalLevels {

    private AppraisalLevels() {
    }

    public static int of(Player player, ItemStack tool) {
        int base = player.getData(ModAttachments.APPRAISAL_LEVEL.get());

        int toolLevel = tool.getItem() instanceof AppraisalTool appraisalTool
                ? appraisalTool.appraisalLevel(tool)
                : 0;

        int enchantLevel = 0;
        if (tool.getItem() instanceof AppraisalTool) {
            Holder<Enchantment> enchantment = player.level().registryAccess()
                    .holderOrThrow(ModEnchantments.LEEUWENHOEK);
            enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, tool);
        }

        int effectLevel = 0;
        MobEffectInstance instance = player.getEffect(ModEffects.MENDELEEV_INSIGHT);
        if (instance != null) {
            effectLevel = instance.getAmplifier() + 1;
        }

        return base + toolLevel + enchantLevel + effectLevel;
    }
}
