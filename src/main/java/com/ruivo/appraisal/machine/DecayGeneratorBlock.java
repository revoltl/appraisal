package com.ruivo.appraisal.machine;

import com.mojang.serialization.MapCodec;
import com.ruivo.appraisal.plugin.PluginItem;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.ruivo.appraisal.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * 衰变发电机方块。中心区域放衰变输入物、取衰变产物，四个角放插件。
 * 插件槽只接受玩家操作，管道和漏斗碰不到。
 */
public class DecayGeneratorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DecayGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(DecayGeneratorBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return MachineShapes.DECAY_GENERATOR;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DecayGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof DecayGeneratorBlockEntity generator) {
                generator.tick(lvl, pos, st);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DecayGeneratorBlockEntity generator)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        double localX = hit.getLocation().x - pos.getX();
        double localZ = hit.getLocation().z - pos.getZ();

        if (isCenter(localX, localZ)) {
            if (!stack.isEmpty() && generator.isDecayInput(stack)
                    && generator.getInventory().getStackInSlot(DecayGeneratorBlockEntity.SLOT_CENTER).isEmpty()) {
                generator.getInventory().setStackInSlot(DecayGeneratorBlockEntity.SLOT_CENTER,
                        stack.copyWithCount(1));
                stack.shrink(1);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
                return ItemInteractionResult.CONSUME;
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int slot = DecayGeneratorBlockEntity.PLUGIN_START + cornerIndex(localX, localZ);

        if (stack.getCapability(Capabilities.EnergyStorage.ITEM) != null) {
            if (!generator.getInventory().getStackInSlot(slot).isEmpty()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            generator.getInventory().setStackInSlot(slot, stack.copyWithCount(1));
            stack.shrink(1);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.5F, 1.0F);
            return ItemInteractionResult.CONSUME;
        }

        if (!(stack.getItem() instanceof PluginItem plugin) || !stack.is(ModTags.Items.PLUGINS)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!generator.getInventory().getStackInSlot(slot).isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int limit = plugin.installLimit();
        if (limit != Integer.MAX_VALUE && generator.countPlugin(plugin) + 1 > limit) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        generator.getInventory().setStackInSlot(slot, stack.copyWithCount(1));
        stack.shrink(1);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.5F, 1.0F);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DecayGeneratorBlockEntity generator)) {
            return InteractionResult.PASS;
        }

        double localX = hit.getLocation().x - pos.getX();
        double localZ = hit.getLocation().z - pos.getZ();

        if (isCenter(localX, localZ)) {
            ItemStack stored = generator.getInventory().getStackInSlot(DecayGeneratorBlockEntity.SLOT_CENTER);
            if (generator.isDecayOutput(stored)) {
                ItemStack extracted = generator.getInventory().extractItem(
                        DecayGeneratorBlockEntity.SLOT_CENTER, 1, false);
                if (!extracted.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(extracted);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.PASS;
        }

        int slot = DecayGeneratorBlockEntity.PLUGIN_START + cornerIndex(localX, localZ);
        ItemStack plugin = generator.getInventory().getStackInSlot(slot);
        if (!plugin.isEmpty()) {
            ItemStack extracted = generator.getInventory().extractItem(slot, 1, false);
            if (!extracted.isEmpty()) {
                player.getInventory().placeItemBackInInventory(extracted);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean isCenter(double x, double z) {
        return x > 0.25D && x < 0.75D && z > 0.25D && z < 0.75D;
    }

    private static int cornerIndex(double x, double z) {
        return (x < 0.5D ? 0 : 1) + (z < 0.5D ? 0 : 1) * 2;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        ItemStack drop = new ItemStack(this);
        if (blockEntity instanceof DecayGeneratorBlockEntity generator) {
            drop.set(DataComponents.BLOCK_ENTITY_DATA,
                    CustomData.of(generator.saveWithFullMetadata(level.registryAccess())));
        }
        popResource(level, pos, drop);
    }
}
