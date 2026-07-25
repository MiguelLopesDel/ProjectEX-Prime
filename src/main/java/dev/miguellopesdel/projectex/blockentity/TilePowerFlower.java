package dev.miguellopesdel.projectex.blockentity;

import dev.miguellopesdel.projectex.ProjectEXConfig;
import dev.miguellopesdel.projectex.block.BlockPowerFlower;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generates EMC straight into its owner's transmutation knowledge. It is not an EMC storage:
 * nothing can pull from it or push into it, which is why it does not extend
 * {@link EmcStorageBlockEntity}.
 */
public class TilePowerFlower extends ProjectEXBlockEntity {
	public final OwnerEmcBuffer ownerBuffer = new OwnerEmcBuffer();

	public TilePowerFlower(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.POWER_FLOWER.get(), pos, state);
	}

	public void setOwner(LivingEntity entity) {
		ownerBuffer.setOwner(entity);
		setChanged();
	}

	@Override
	protected void onSecond() {
		if (!(getBlockState().getBlock() instanceof BlockPowerFlower powerFlower)) {
			return;
		}

		long generated = ProjectEXConfig.valuesOf(powerFlower.matter).powerFlowerOutput();

		if (ownerBuffer.deposit(level, generated)) {
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
}
