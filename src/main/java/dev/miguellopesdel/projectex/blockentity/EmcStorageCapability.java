package dev.miguellopesdel.projectex.blockentity;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Hands out ProjectE's EMC storage capability, and lets go of it when the block does.
 *
 * <p>This is held rather than inherited because the block entities that need it do not share a
 * parent: most of them run on the mod's one second beat, and the Alchemy Table has to count every
 * tick to move a progress bar.
 */
public final class EmcStorageCapability {
	private final IEmcStorage storage;
	private LazyOptional<IEmcStorage> handle;

	public EmcStorageCapability(IEmcStorage storage) {
		this.storage = storage;
	}

	public <T> LazyOptional<T> get(Capability<T> capability) {
		if (capability != PECapabilities.EMC_STORAGE_CAPABILITY) {
			return LazyOptional.empty();
		}

		if (handle == null || !handle.isPresent()) {
			handle = LazyOptional.of(() -> storage);
		}

		return handle.cast();
	}

	public void invalidate() {
		if (handle != null) {
			handle.invalidate();
			handle = null;
		}
	}
}
