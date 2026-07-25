package dev.miguellopesdel.projectex.block;

import dev.miguellopesdel.projectex.Matter;
import dev.miguellopesdel.projectex.ProjectEXConfig;
import dev.miguellopesdel.projectex.blockentity.TileCollector;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import javax.annotation.Nullable;

import java.util.List;

public class BlockCollector extends Block implements EntityBlock {
	public final Matter matter;

	public BlockCollector(Matter matter) {
		super(Properties.of().mapColor(MapColor.STONE).strength(3.5F).sound(SoundType.STONE).requiresCorrectToolForDrops());
		this.matter = matter;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileCollector(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : (l, pos, s, blockEntity) -> {
			if (blockEntity instanceof TileCollector collector) {
				collector.tick();
			}
		};
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, level, list, flag);
		list.add(Component.translatable("block.projectex.collector.tooltip").withStyle(ChatFormatting.GRAY));
		list.add(Component.translatable("block.projectex.collector.emc_produced",
				TransmutationEMCFormatter.formatEMC(ProjectEXConfig.valuesOf(matter).collectorOutput()).copy().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
	}
}
