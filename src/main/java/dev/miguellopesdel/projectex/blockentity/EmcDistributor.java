package dev.miguellopesdel.projectex.blockentity;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Collectors and relays both push EMC into whichever neighbours will take it, splitting what
 * they hold evenly. They differ only in which neighbours they accept and how much each may get,
 * so the algorithm lives here once instead of being copied into both, where the two copies
 * would eventually drift apart.
 */
public final class EmcDistributor {
	private static final Direction[] DIRECTIONS = Direction.values();

	private EmcDistributor() {
	}

	/**
	 * @param accepts   which neighbouring storages are valid targets
	 * @param perTarget ceiling on how much a single target may receive this round
	 * @param onTarget  called once per accepted neighbour, before anything is transferred
	 * @return how much EMC was actually handed over
	 */
	public static long distribute(Level level, BlockPos pos, long available, Predicate<IEmcStorage> accepts,
			long perTarget, Consumer<BlockEntity> onTarget) {
		if (available <= 0L) {
			return 0L;
		}

		List<IEmcStorage> targets = new ArrayList<>(DIRECTIONS.length);

		for (Direction direction : DIRECTIONS) {
			BlockEntity neighbour = level.getBlockEntity(pos.relative(direction));

			if (neighbour == null) {
				continue;
			}

			IEmcStorage storage = neighbour.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, direction.getOpposite()).orElse(null);

			if (storage == null || !accepts.test(storage) || storage.insertEmc(1L, IEmcStorage.EmcAction.SIMULATE) <= 0L) {
				continue;
			}

			targets.add(storage);
			onTarget.accept(neighbour);
		}

		if (targets.isEmpty() || available < targets.size()) {
			return 0L;
		}

		long share = Math.min(available / targets.size(), perTarget);
		long transferred = 0L;

		for (IEmcStorage target : targets) {
			long inserted = target.insertEmc(share, IEmcStorage.EmcAction.EXECUTE);
			transferred += inserted;

			if (available - transferred < share) {
				break;
			}
		}

		return transferred;
	}
}
