package com.latmod.mods.projectex.block;

import com.latmod.mods.projectex.tile.TileLinkMK2;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

import java.util.List;

public class BlockLinkMK2 extends BlockLink {
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileLinkMK2(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, level, list, flag);
		list.add(Component.translatable("block.projectex.refined_link.tooltip").withStyle(ChatFormatting.GRAY));
	}
}
