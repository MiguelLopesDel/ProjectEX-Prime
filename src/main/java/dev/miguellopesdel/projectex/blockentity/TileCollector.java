package dev.miguellopesdel.projectex.blockentity;

import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.block.BlockCollector;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.gameObjs.block_entities.RelayMK1BlockEntity;
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
 * Generates EMC every second and pushes it into any adjacent EMC storage, giving relays their
 * bonus while doing so.
 */
public class TileCollector extends BlockEntity implements IEmcStorage {
	public int tick = 0;
	public long storedEMC = 0L;
	private LazyOptional<IEmcStorage> emcStorageCapability;

	public TileCollector(BlockPos pos, BlockState state) {
		this(ProjectEXBlockEntities.COLLECTOR.get(), pos, state);
	}

	protected TileCollector(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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
		if (level == null) {
			return;
		}

		tick++;

		if (tick < 20) {
			return;
		}

		tick = 0;

		if (!(getBlockState().getBlock() instanceof BlockCollector collector)) {
			return;
		}

		storedEMC += collector.matter.collectorOutput;

		List<IEmcStorage> targets = new ArrayList<>(1);

		for (Direction direction : ProjectEX.DIRECTIONS) {
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
			IEmcStorage storage = blockEntity == null ? null
					: blockEntity.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, direction.getOpposite()).orElse(null);

			if (storage != null && storage.insertEmc(1L, EmcAction.SIMULATE) > 0L) {
				targets.add(storage);

				// ProjectE's own relays only gain their bonus one tick at a time, so a collector
				// feeding them has to award the whole second's worth by hand.
				if (blockEntity instanceof RelayMK1BlockEntity relay) {
					for (int i = 0; i < 20; i++) {
						relay.addBonus();
					}

					relay.setChanged();
				} else if (blockEntity instanceof TileRelay relay) {
					relay.addBonus();
					relay.setChanged();
				}
			}
		}

		if (!targets.isEmpty() && storedEMC >= targets.size()) {
			long share = storedEMC / targets.size();

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
		return 0L;
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
