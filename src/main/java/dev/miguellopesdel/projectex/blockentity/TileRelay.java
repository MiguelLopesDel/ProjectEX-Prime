package dev.miguellopesdel.projectex.blockentity;

import com.google.common.math.LongMath;
import dev.miguellopesdel.projectex.ProjectEXConfig;
import dev.miguellopesdel.projectex.TierValues;
import dev.miguellopesdel.projectex.block.BlockRelay;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Buffers EMC and forwards it to adjacent non-relay storages, capped by its tier's transfer
 * rate. Relays skip each other so a wall of them cannot loop EMC between themselves.
 */
public class TileRelay extends EmcStorageBlockEntity {
	private long storedEMC;

	public TileRelay(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.RELAY.get(), pos, state);
	}

	@Override
	protected void onSecond() {
		if (storedEMC <= 0L || !(getBlockState().getBlock() instanceof BlockRelay relay)) {
			return;
		}

		TierValues values = ProjectEXConfig.valuesOf(relay.matter);

		long transferred = EmcDistributor.distribute(level, worldPosition, storedEMC,
				storage -> !storage.isRelay(), values.relayTransfer(), neighbour -> {
				});

		if (transferred > 0L) {
			storedEMC -= transferred;
			setChanged();
		}
	}

	public void addBonus() {
		if (getBlockState().getBlock() instanceof BlockRelay relay) {
			insertEmc(ProjectEXConfig.valuesOf(relay.matter).relayBonus(), IEmcStorage.EmcAction.EXECUTE);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		storedEMC = tag.getLong("StoredEMC");
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putLong("StoredEMC", storedEMC);
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
		if (emc < 0L) {
			return insertEmc(-emc, action);
		}

		long extracted = Math.min(storedEMC, emc);

		if (action.execute()) {
			storedEMC -= extracted;
			setChanged();
		}

		return extracted;
	}

	@Override
	public long insertEmc(long emc, EmcAction action) {
		if (emc < 0L) {
			return extractEmc(-emc, action);
		}

		// Saturating, because a relay with nowhere to push accumulates without limit. Wrapping
		// would take it negative and it would then refuse to do anything ever again.
		long inserted = LongMath.saturatedAdd(storedEMC, emc) - storedEMC;

		if (action.execute() && inserted > 0L) {
			storedEMC += inserted;
			setChanged();
		}

		return inserted;
	}

	@Override
	public boolean isRelay() {
		return true;
	}
}
