package dev.miguellopesdel.projectex.blockentity;

import com.google.common.math.LongMath;
import dev.miguellopesdel.projectex.ProjectEXConfig;
import dev.miguellopesdel.projectex.block.BlockCollector;
import moze_intel.projecte.gameObjs.block_entities.RelayMK1BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generates EMC once a second and pushes it into any adjacent EMC storage, paying relays their
 * bonus on the way.
 */
public class TileCollector extends EmcStorageBlockEntity {
	private long storedEMC;

	public TileCollector(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.COLLECTOR.get(), pos, state);
	}

	@Override
	protected void onSecond() {
		if (!(getBlockState().getBlock() instanceof BlockCollector collector)) {
			return;
		}

		// A collector with no neighbour to give to piles up, and a pack is free to configure an
		// output large enough that piling up would wrap.
		storedEMC = LongMath.saturatedAdd(storedEMC, ProjectEXConfig.valuesOf(collector.matter).collectorOutput());

		long transferred = EmcDistributor.distribute(level, worldPosition, storedEMC,
				storage -> true, Long.MAX_VALUE, neighbour -> {
					// ProjectE's relays award their bonus per tick, so a collector running on a
					// one second beat has to hand over a whole second's worth at once.
					if (neighbour instanceof RelayMK1BlockEntity relay) {
						for (int i = 0; i < INTERVAL; i++) {
							relay.addBonus();
						}

						relay.setChanged();
					} else if (neighbour instanceof TileRelay relay) {
						relay.addBonus();
						relay.setChanged();
					}
				});

		storedEMC -= transferred;
		setChanged();
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

	/** A collector produces EMC, it never accepts any. */
	@Override
	public long insertEmc(long emc, EmcAction action) {
		return 0L;
	}
}
