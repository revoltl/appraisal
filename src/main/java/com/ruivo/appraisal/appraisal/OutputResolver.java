package com.ruivo.appraisal.appraisal;

import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把逻辑产出转换为可放入槽位或可丢到世界中的形态。
 */
public final class OutputResolver {

    private OutputResolver() {
    }

    /**
     * 合并相同物品，并按各自的堆叠上限拆分为多个 ItemStack。
     */
    public static List<ItemStack> toStacks(List<AppraisalDrop> drops) {
        Map<Holder<Item>, Integer> counts = new LinkedHashMap<>();
        for (AppraisalDrop drop : drops) {
            counts.merge(drop.item(), drop.count(), Integer::sum);
        }

        List<ItemStack> stacks = new ArrayList<>();
        counts.forEach((item, total) -> {
            int remaining = total;
            int limit = new ItemStack(item.value()).getMaxStackSize();
            while (remaining > 0) {
                int size = Math.min(remaining, limit);
                stacks.add(new ItemStack(item.value(), size));
                remaining -= size;
            }
        });
        return stacks;
    }

    /**
     * 世界交互的结算。
     *
     * <p>鉴定成功时原方块必定被消耗：第一个方块类产物替换目标方块，没有方块类产物时
     * 目标方块被移除。其余产物一律掉落。这样同一处方块不可能被反复鉴定。</p>
     */
    public static void applyToWorld(ServerLevel level, BlockPos pos, List<AppraisalDrop> drops) {
        boolean blockPlaced = false;
        for (AppraisalDrop drop : drops) {
            int remaining = drop.count();
            if (!blockPlaced && drop.kind() == OutputKind.BLOCK && drop.item().value() instanceof BlockItem blockItem) {
                level.setBlock(pos, blockItem.getBlock().defaultBlockState(), Block.UPDATE_ALL);
                blockPlaced = true;
                remaining--;
            }
            while (remaining > 0) {
                int size = Math.min(remaining, new ItemStack(drop.item().value()).getMaxStackSize());
                Block.popResource(level, pos, new ItemStack(drop.item().value(), size));
                remaining -= size;
            }
        }
        if (!blockPlaced) {
            level.removeBlock(pos, false);
        }
    }
}
