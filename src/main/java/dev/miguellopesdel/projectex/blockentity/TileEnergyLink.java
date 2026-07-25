package dev.miguellopesdel.projectex.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEnergyLink extends TileLink {
	public TileEnergyLink(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.ENERGY_LINK.get(), pos, state);
	}
}
