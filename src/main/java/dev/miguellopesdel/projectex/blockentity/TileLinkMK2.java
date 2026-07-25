package dev.miguellopesdel.projectex.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileLinkMK2 extends TileLink {
	public TileLinkMK2(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.REFINED_LINK.get(), pos, state, 1, 9);
	}

	@Override
	protected boolean learnsItems() {
		return true;
	}
}
