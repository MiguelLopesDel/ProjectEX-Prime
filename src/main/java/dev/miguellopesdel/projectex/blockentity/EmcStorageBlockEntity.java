package dev.miguellopesdel.projectex.blockentity;

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
 * A block on the one second beat that also speaks EMC to its neighbours.
 */
public abstract class EmcStorageBlockEntity extends ProjectEXBlockEntity implements IEmcStorage {
	private final EmcStorageCapability emcStorage = new EmcStorageCapability(this);

	protected EmcStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		LazyOptional<T> emc = emcStorage.get(cap);
		return emc.isPresent() ? emc : super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		emcStorage.invalidate();
	}
}
