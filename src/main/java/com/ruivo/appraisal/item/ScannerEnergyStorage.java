package com.ruivo.appraisal.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 电动扫描仪的物品能量视图，读写扫描仪自己的能量组件。
 */
public final class ScannerEnergyStorage implements IEnergyStorage {

    private final ItemStack stack;

    public ScannerEnergyStorage(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int stored = ElectricScannerItem.getEnergy(stack);
        int accepted = Math.min(ElectricScannerItem.capacity(stack) - stored, maxReceive);
        if (!simulate && accepted > 0) {
            ElectricScannerItem.setEnergy(stack, stored + accepted);
        }
        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int stored = ElectricScannerItem.getEnergy(stack);
        int extracted = Math.min(stored, maxExtract);
        if (!simulate && extracted > 0) {
            ElectricScannerItem.setEnergy(stack, stored - extracted);
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return ElectricScannerItem.getEnergy(stack);
    }

    @Override
    public int getMaxEnergyStored() {
        return ElectricScannerItem.capacity(stack);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
