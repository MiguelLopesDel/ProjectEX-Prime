package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.block.BlockRelay;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffers EMC and forwards it to adjacent non-relay storages, up to its tier's transfer rate.
 */
public class TileRelay extends BlockEntity implements IEmcStorage {
	public int tick = 0;
	public long storedEMC = 0L;
	private LazyOptional<IEmcStorage> emcStorageCapability;

	public TileRelay(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.RELAY.get(), pos, state);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		tick = tag.getByte("Tick") & 0xFF;
		storedEMC = tag.getLong("StoredEMC");
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putByte("Tick", (byte) tick);
		tag.putLong("StoredEMC", storedEMC);
	}

	public void tick() {
		if (level == null || storedEMC <= 0L) {
			return;
		}

		tick++;

		if (tick < 20) {
			return;
		}

		tick = 0;

		if (!(getBlockState().getBlock() instanceof BlockRelay relay)) {
			return;
		}

		List<IEmcStorage> targets = new ArrayList<>(1);

		for (Direction direction : ProjectEX.DIRECTIONS) {
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
			IEmcStorage storage = blockEntity == null ? null
					: blockEntity.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, direction.getOpposite()).orElse(null);

			if (storage != null && !storage.isRelay() && storage.insertEmc(1L, EmcAction.SIMULATE) > 0L) {
				targets.add(storage);
			}
		}

		if (!targets.isEmpty() && storedEMC >= targets.size()) {
			long share = Math.min(storedEMC / targets.size(), relay.matter.relayTransfer);

			for (IEmcStorage storage : targets) {
				long inserted = storage.insertEmc(share, EmcAction.EXECUTE);

				if (inserted > 0L) {
					storedEMC -= inserted;
					setChanged();

					if (storedEMC < share) {
						break;
					}
				}
			}
		}
	}

	public void addBonus() {
		if (getBlockState().getBlock() instanceof BlockRelay relay) {
			insertEmc(relay.matter.relayBonus, EmcAction.EXECUTE);
		}
	}

	@Override
	public long getStoredEmc() {
		return storedEMC;
	}

	@Override
	public long getMaximumEmc() {
		return Long.MAX_VALUE;
	}

	@Override
	public long extractEmc(long emc, EmcAction action) {
		long extracted = Math.min(storedEMC, emc);

		if (extracted < 0L) {
			return insertEmc(-extracted, action);
		} else if (action.execute()) {
			storedEMC -= extracted;
		}

		return extracted;
	}

	@Override
	public long insertEmc(long emc, EmcAction action) {
		if (emc < 0L) {
			return extractEmc(-emc, action);
		}

		if (action.execute()) {
			storedEMC += emc;
		}

		return emc;
	}

	@Override
	public boolean isRelay() {
		return true;
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (cap == PECapabilities.EMC_STORAGE_CAPABILITY) {
			if (emcStorageCapability == null || !emcStorageCapability.isPresent()) {
				emcStorageCapability = LazyOptional.of(() -> this);
			}

			return emcStorageCapability.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();

		if (emcStorageCapability != null) {
			emcStorageCapability.invalidate();
			emcStorageCapability = null;
		}
	}
}
