package com.ruivo.appraisal.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 存储槽区间守卫。
 *
 * <p>菜单槽位的注册顺序是输入槽、二十七个存储槽、九个插件槽、玩家背包。
 * 存储槽必须从下标一开始，否则前几个存储槽会被 quickMoveStack 当成普通槽位，
 * 走 moveItemStackTo，而只读容器不会扣减，取出就变成了复制。</p>
 */
class StorageSlotRangeTest {

    @Test
    void storageSlotsSitBetweenInputAndPluginSlots() {
        assertEquals(0, AppraisalMachineMenu.INPUT_SLOT_INDEX);
        assertEquals(1, AppraisalMachineMenu.STORAGE_SLOT_START);
        assertEquals(27, AppraisalMachineMenu.STORAGE_SLOT_COUNT);
        assertEquals(28, AppraisalMachineMenu.STORAGE_SLOT_END);
        assertEquals(28, AppraisalMachineMenu.PLUGIN_SLOT_START);
        assertEquals(9, AppraisalMachineMenu.PLUGIN_SLOT_END - AppraisalMachineMenu.PLUGIN_SLOT_START);
        assertEquals(37, AppraisalMachineMenu.PLAYER_SLOT_START);
    }

    @Test
    void storageRangeDoesNotOverlapPluginRange() {
        assertTrue(AppraisalMachineMenu.STORAGE_SLOT_END <= AppraisalMachineMenu.PLUGIN_SLOT_START);
    }

    @Test
    void storageRangeDoesNotUseTheOldWrongStart() {
        // 负控：旧写法把输入槽加插件数量当作存储起点，前九个存储槽因此没被覆盖。
        int oldWrongStart = 1 + AppraisalMachineBlockEntity.PLUGIN_COUNT;
        assertNotEquals(oldWrongStart, AppraisalMachineMenu.STORAGE_SLOT_START);
    }
}
