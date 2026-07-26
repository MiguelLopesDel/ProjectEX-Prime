package dev.miguellopesdel.projectex.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Everything in this mod works on a one second beat rather than every tick, which is what makes
 * a room full of collectors cheap. This owns that cadence so the block entities only say what
 * happens on the beat.
 *
 * <p>The countdown starts at a value derived from the position, so the collectors in a chunk do
 * not all fire on the same game tick and spike the server once a second.
 */
public abstract class ProjectEXBlockEntity extends BlockEntity {
	public static final int INTERVAL = 20;

	private int cooldown;

	protected ProjectEXBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		cooldown = Math.floorMod(pos.hashCode(), INTERVAL) + 1;
	}

	public final void tick() {
		if (level == null) {
			return;
		}

		onTick();

		if (--cooldown > 0) {
			return;
		}

		cooldown = INTERVAL;
		onSecond();
	}

	/**
	 * Called every tick on the server, for the rare piece of work that cannot wait for the beat.
	 * Anything put here runs twenty times as often, so it had better be cheap.
	 */
	protected void onTick() {
	}

	/** Called once per second on the server. */
	protected abstract void onSecond();

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		if (tag.contains("Cooldown")) {
			cooldown = tag.getByte("Cooldown") & 0xFF;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putByte("Cooldown", (byte) cooldown);
	}
}
