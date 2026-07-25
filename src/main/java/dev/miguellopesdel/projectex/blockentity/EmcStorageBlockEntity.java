package dev.miguellopesdel.projectex.blockentity;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wires up ProjectE's EMC storage capability, including invalidating it when the block goes
 * away. Every block that speaks EMC to its neighbours needs exactly this, and used to carry its
 * own copy of it.
 */
public abstract class EmcStorageBlockEntity extends ProjectEXBlockEntity implements IEmcStorage {
	private LazyOptional<IEmcStorage> emcStorage;

	protected EmcStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (cap == PECapabilities.EMC_STORAGE_CAPABILITY) {
			if (emcStorage == null || !emcStorage.isPresent()) {
				emcStorage = LazyOptional.of(() -> this);
			}

			return emcStorage.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();

		if (emcStorage != null) {
			emcStorage.invalidate();
			emcStorage = null;
		}
	}
}
