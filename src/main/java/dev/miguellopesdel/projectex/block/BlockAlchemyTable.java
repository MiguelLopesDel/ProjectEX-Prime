package dev.miguellopesdel.projectex.block;

import dev.miguellopesdel.projectex.blockentity.ProjectEXBlockEntities;
import dev.miguellopesdel.projectex.blockentity.TileAlchemyTable;
import dev.miguellopesdel.projectex.gui.ContainerAlchemyTable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

import java.util.List;

public class BlockAlchemyTable extends Block implements EntityBlock {
	/** The table top and the four legs the model draws, so the corners are not solid. */
	private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D);

	/** Where the model puts the four candles, in sixteenths of a block. */
	private static final double[][] FLAMES = {{2.5D, 2.5D}, {13.5D, 13.5D}, {13.5D, 2.5D}, {2.5D, 13.5D}};

	public BlockAlchemyTable() {
		super(Properties.of().mapColor(MapColor.STONE).strength(2.0F).sound(SoundType.STONE).noOcclusion());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileAlchemyTable(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || type != ProjectEXBlockEntities.ALCHEMY_TABLE.get()) {
			return null;
		}

		return (tickLevel, pos, tickState, blockEntity) -> ((TileAlchemyTable) blockEntity).tick();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
				&& level.getBlockEntity(pos) instanceof TileAlchemyTable table) {
			NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
					(windowId, inventory, opener) -> new ContainerAlchemyTable(windowId, inventory, table),
					Component.translatable(getDescriptionId())), buffer -> buffer.writeBlockPos(pos));
		}

		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, level, list, flag);
		list.add(Component.translatable("block.projectex.alchemy_table.tooltip").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		for (double[] flame : FLAMES) {
			level.addParticle(ParticleTypes.FLAME,
					pos.getX() + flame[0] / 16.0D, pos.getY() + 1.15D, pos.getZ() + flame[1] / 16.0D,
					0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileAlchemyTable table) {
			IItemHandler items = table.items;

			for (int slot = 0; slot < items.getSlots(); slot++) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items.getStackInSlot(slot));
			}
		}

		super.onRemove(state, level, pos, newState, moving);
	}
}
