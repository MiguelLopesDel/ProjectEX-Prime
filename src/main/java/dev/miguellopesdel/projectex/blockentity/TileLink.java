package dev.miguellopesdel.projectex.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base of the EMC links: takes EMC from any side and pays it to its owner, holding it while the
 * owner is offline.
 *
 * <p>The item input and output sides of the links are part of the GUI phase of the port; see
 * the README.
 */
public class TileLink extends EmcStorageBlockEntity {
	public final OwnerEmcBuffer ownerBuffer = new OwnerEmcBuffer();

	public TileLink(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public void setOwner(LivingEntity entity) {
		ownerBuffer.setOwner(entity);
		setChanged();
	}

	@Override
	protected void onSecond() {
		if (ownerBuffer.hasPending() && ownerBuffer.deposit(level, 0L)) {
			setChanged();
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		ownerBuffer.load(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ownerBuffer.save(tag);
	}

	/** A link is a one way door: EMC goes in and straight to its owner, never back out. */
	@Override
	public long getStoredEmc() {
		return 0L;
	}

	@Override
	public long getMaximumEmc() {
		return Long.MAX_VALUE;
	}

	@Override
	public long extractEmc(long emc, EmcAction action) {
		return emc < 0L ? insertEmc(-emc, action) : 0L;
	}

	@Override
	public long insertEmc(long emc, EmcAction action) {
		if (emc <= 0L) {
			return 0L;
		}

		if (action.execute()) {
			ownerBuffer.add(emc);
			setChanged();
		}

		return emc;
	}
}
