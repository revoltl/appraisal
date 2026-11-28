package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.recipe.NeutronGuideRecipe;
import com.ruivo.appraisal.recipe.WeightedResult;
import com.ruivo.appraisal.registry.ModBlockEntities;
import com.ruivo.appraisal.registry.ModRecipes;
import com.ruivo.appraisal.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Optional;

/**
 * 中子引导塔。四个独立槽位，每个槽位放一个输入物，按配方权重产出一件产物。
 * 玩家通过四角交互，管道和漏斗通过能力输入输入物、提取产物。
 */
public class NeutronGuideTowerBlockEntity extends BlockEntity {

    public static final int SLOTS = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    };

    private final int[] progress = new int[SLOTS];
    private final int[] total = new int[SLOTS];

    private final IItemHandler automation = new IItemHandler() {
        @Override
        public int getSlots() {
            return SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isInput(stack)) {
                return stack;
            }
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return stack;
            }
            if (!simulate) {
                inventory.setStackInSlot(slot, stack.copyWithCount(1));
            }
            return stack.getCount() <= 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack existing = inventory.getStackInSlot(slot);
            if (existing.isEmpty() || !isProduct(existing)) {
                return ItemStack.EMPTY;
            }
            return inventory.extractItem(slot, Math.min(amount, 1), simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isInput(stack);
        }
    };

    public NeutronGuideTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEUTRON_GUIDE_TOWER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty() || isProduct(stack)) {
                progress[slot] = 0;
                continue;
            }
            Optional<RecipeHolder<NeutronGuideRecipe>> found = level.getRecipeManager()
                    .getRecipeFor(ModRecipes.NEUTRON_GUIDE_TYPE.get(), new SingleRecipeInput(stack), level);
            if (found.isEmpty()) {
                progress[slot] = 0;
                continue;
            }
            NeutronGuideRecipe recipe = found.get().value();
            total[slot] = recipe.time();
            progress[slot]++;
            if (progress[slot] >= recipe.time()) {
                inventory.setStackInSlot(slot, recipe.rollResult(level.random));
                progress[slot] = 0;
            }
        }
    }

    public boolean isInput(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return false;
        }
        return level.getRecipeManager().getRecipeFor(
                ModRecipes.NEUTRON_GUIDE_TYPE.get(), new SingleRecipeInput(stack), level).isPresent();
    }

    public boolean isProduct(ItemStack stack) {
        if (stack.is(ModTags.Items.NEUTRON_GUIDE_OUTPUTS)) {
            return true;
        }
        if (level == null) {
            return false;
        }
        for (RecipeHolder<NeutronGuideRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.NEUTRON_GUIDE_TYPE.get())) {
            for (WeightedResult result : holder.value().results()) {
                if (stack.is(result.item())) {
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getProgress(int slot) {
        return progress[slot];
    }

    public int getTotal(int slot) {
        return total[slot];
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.NEUTRON_GUIDE_TOWER.get(), (tower, side) -> tower.automation);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putIntArray("progress", progress);
        tag.putIntArray("total", total);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        int[] savedProgress = tag.getIntArray("progress");
        int[] savedTotal = tag.getIntArray("total");
        for (int index = 0; index < SLOTS; index++) {
            progress[index] = index < savedProgress.length ? savedProgress[index] : 0;
            total[index] = index < savedTotal.length ? savedTotal[index] : 0;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
