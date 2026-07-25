package dev.miguellopesdel.projectex.block;

import dev.miguellopesdel.projectex.blockentity.TileLink;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import javax.annotation.Nullable;

public abstract class BlockLink extends Block implements EntityBlock {
	protected BlockLink() {
		super(Properties.of().mapColor(MapColor.STONE).strength(5F).sound(SoundType.STONE).requiresCorrectToolForDrops());
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (l, pos, s, blockEntity) -> {
			if (blockEntity instanceof TileLink link) {
				link.tick();
			}
		};
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileLink link) {
			player.displayClientMessage(Component.literal(link.ownerName), true);
		}

		return super.use(state, level, pos, player, hand, hit);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		if (entity != null && level.getBlockEntity(pos) instanceof TileLink link) {
			link.owner = entity.getUUID();
			link.ownerName = entity.getScoreboardName();
			link.setChanged();
		}
	}
}
