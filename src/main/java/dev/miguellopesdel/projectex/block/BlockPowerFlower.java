package dev.miguellopesdel.projectex.block;

import dev.miguellopesdel.projectex.Matter;
import dev.miguellopesdel.projectex.blockentity.TilePowerFlower;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

import java.util.List;

public class BlockPowerFlower extends Block implements EntityBlock {
	public static final VoxelShape SHAPE = Shapes.or(
			box(0, 0, 0, 16, 1, 16),
			box(3.5, 4, 6.5, 12.5, 13, 9.5),
			box(6.5, 1, 6.5, 9.5, 16, 9.5),
			box(6.5, 4, 3.5, 9.5, 13, 12.5),
			box(6.5, 7, 0.5, 9.5, 10, 15.5),
			box(3.5, 7, 3.5, 12.5, 10, 12.5),
			box(0.5, 7, 6.5, 15.5, 10, 9.5)
	);

	public final Matter matter;

	public BlockPowerFlower(Matter matter) {
		super(Properties.of().mapColor(MapColor.STONE).strength(1F).sound(SoundType.STONE).noOcclusion());
		this.matter = matter;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TilePowerFlower(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (l, pos, s, blockEntity) -> {
			if (blockEntity instanceof TilePowerFlower powerFlower) {
				powerFlower.tick();
			}
		};
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TilePowerFlower powerFlower) {
			player.displayClientMessage(Component.literal(powerFlower.ownerName), true);
		}

		return super.use(state, level, pos, player, hand, hit);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		if (entity != null && level.getBlockEntity(pos) instanceof TilePowerFlower powerFlower) {
			powerFlower.owner = entity.getUUID();
			powerFlower.ownerName = entity.getScoreboardName();
			powerFlower.setChanged();
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, level, list, flag);
		list.add(Component.translatable("block.projectex.collector.tooltip").withStyle(ChatFormatting.GRAY));
		list.add(Component.translatable("block.projectex.collector.emc_produced",
				TransmutationEMCFormatter.formatEMC(matter.powerFlowerOutput()).copy().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
	}
}
