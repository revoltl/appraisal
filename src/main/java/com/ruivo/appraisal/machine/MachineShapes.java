package com.ruivo.appraisal.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 机器的粗略碰撞与选择形状。只用少量包围盒，避免复杂 VoxelShape 拖慢碰撞、
 * 射线与光照查询。
 */
public final class MachineShapes {

    public static final VoxelShape APPRAISAL_MACHINE = Shapes.or(
            Block.box(0, 0, 0, 16, 12, 16),
            Block.box(1, 12, 1, 15, 27, 15)
    );

    public static final VoxelShape MODEL_TRAINER = Shapes.or(
            Block.box(0, 0, 0, 16, 13, 16),
            Block.box(2, 13, 2, 14, 26, 14)
    );

    public static final VoxelShape DECAY_GENERATOR = Shapes.or(
            Block.box(0, 0, 0, 16, 13, 16),
            Block.box(4, 13, 4, 12, 14, 12)
    );

    public static final VoxelShape NEUTRON_GUIDE_TOWER = Shapes.or(
            Block.box(0, 0, 0, 16, 13, 16),
            Block.box(6, 13, 6, 10, 32, 10)
    );

    private MachineShapes() {
    }
}
