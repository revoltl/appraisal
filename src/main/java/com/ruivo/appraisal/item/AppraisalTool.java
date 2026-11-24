package com.ruivo.appraisal.item;

import net.minecraft.world.item.ItemStack;

/**
 * 鉴定工具的能力描述。Tooltip、等级计算与连锁鉴定都通过这个接口读取，
 * 新增工具只要实现它，不需要改动这三处。
 */
public interface AppraisalTool {

    int appraisalLevel(ItemStack stack);

    default boolean supportsRightClickAppraisal() {
        return true;
    }

    default boolean supportsChainedAppraisal() {
        return true;
    }
}
