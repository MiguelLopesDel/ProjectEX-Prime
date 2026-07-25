package com.latmod.mods.projectex.block;

import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import javax.annotation.Nullable;

import java.util.List;

/**
 * A transmutation table you can place on any face of a block. Opening it hands off to
 * ProjectE's own transmutation screen.
 */
public class BlockStoneTable extends Block {
	public static final VoxelShape[] SHAPE = {
			box(0, 0, 0, 16, 4, 16),
			box(0, 12, 0, 16, 16, 16),
			box(0, 0, 0, 16, 16, 4),
			box(0, 0, 12, 16, 16, 16),
			box(0, 0, 0, 4, 16, 16),
			box(12, 0, 0, 16, 16, 16)
	};

	public BlockStoneTable() {
		super(Properties.of().mapColor(MapColor.STONE).strength(1F).sound(SoundType.STONE).noOcclusion());
		registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.FACING, Direction.DOWN));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE[state.getValue(BlockStateProperties.FACING).ordinal()];
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(BlockStateProperties.FACING, ctx.getClickedFace().getOpposite());
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			NetworkHooks.openScreen(serverPlayer, new TransmutationMenuProvider(), buffer -> buffer.writeEnum(InteractionHand.OFF_HAND));
		}

		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, level, list, flag);
		list.add(Component.translatable("block.projectex.stone_table.tooltip").withStyle(ChatFormatting.GRAY));
	}

	private static class TransmutationMenuProvider implements MenuProvider {
		@Override
		public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
			return new TransmutationContainer(windowId, playerInventory);
		}

		@Override
		public Component getDisplayName() {
			return PELang.TRANSMUTATION_TRANSMUTE.translate();
		}
	}
}
