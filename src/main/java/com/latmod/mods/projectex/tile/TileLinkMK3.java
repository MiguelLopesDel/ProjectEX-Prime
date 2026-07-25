package com.latmod.mods.projectex.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileLinkMK3 extends TileLink {
	public TileLinkMK3(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.COMPRESSED_REFINED_LINK.get(), pos, state);
	}
}
