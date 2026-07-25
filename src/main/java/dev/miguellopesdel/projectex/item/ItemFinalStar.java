package dev.miguellopesdel.projectex.item;

import dev.miguellopesdel.projectex.ProjectEXConfig;
import dev.miguellopesdel.projectex.blockentity.PersistentItems;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.capability.EmcHolderItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The end of the EMC ladder: a star that is always full and never empties.
 *
 * <p>It reports a quadrillion EMC and hands out however much is asked of it, so anything that
 * draws on a Klein Star can be powered from one and never run dry. Nothing can charge it, because
 * there is nothing to charge.
 *
 * <p>On a Dark Matter Pedestal it does something else entirely: it copies items dropped on the
 * pedestal into an adjacent inventory. That is the mod's item duplicator, and everything about it
 * is config-driven, including turning it off.
 */
public class ItemFinalStar extends ItemPE implements IItemEmcHolder, IPedestalItem {
	private static final long STORED = 1_000_000_000_000_000L;

	public ItemFinalStar(Properties properties) {
		super(properties.stacksTo(1));
		addItemCapability(EmcHolderItemCapabilityWrapper::new);
	}

	@Override
	public long insertEmc(@Nonnull ItemStack stack, long emc, IEmcStorage.EmcAction action) {
		return 0L;
	}

	@Override
	public long extractEmc(@Nonnull ItemStack stack, long emc, IEmcStorage.EmcAction action) {
		return Math.max(emc, 0L);
	}

	@Override
	public long getStoredEmc(@Nonnull ItemStack stack) {
		return STORED;
	}

	@Override
	public long getMaximumEmc(@Nonnull ItemStack stack) {
		return Long.MAX_VALUE;
	}

	@Override
	public <PEDESTAL extends BlockEntity & IDMPedestal> boolean updateInPedestal(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull PEDESTAL pedestal) {
		int interval = ProjectEXConfig.COMMON.finalStarUpdateInterval.get();

		if (level.isClientSide() || interval <= 0) {
			return false;
		}

		// Pedestals of different mods, and of different positions, would otherwise all fire on
		// the same tick. Spreading them by position keeps a room full of them cheap.
		if (level.getGameTime() % interval != Math.floorMod(pos.hashCode(), interval)) {
			return false;
		}

		List<ItemEntity> dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(0.0D, 1.0D, 0.0D));

		if (dropped.isEmpty()) {
			return false;
		}

		ItemStack copy = copyOf(dropped.get(level.random.nextInt(dropped.size())).getItem());

		if (copy.isEmpty()) {
			return false;
		}

		// Up is where the item to copy sits, so it is the one direction that cannot be the target.
		for (Direction direction : Direction.values()) {
			if (direction == Direction.UP) {
				continue;
			}

			BlockEntity neighbour = level.getBlockEntity(pos.relative(direction));

			if (neighbour == null) {
				continue;
			}

			IItemHandler handler = neighbour.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).orElse(null);

			if (handler != null) {
				ItemHandlerHelper.insertItem(handler, copy, false);
				return true;
			}
		}

		return false;
	}

	/** A full stack of the dropped item, stripped down to what the config allows it to carry. */
	private static ItemStack copyOf(ItemStack dropped) {
		if (dropped.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (!ProjectEXConfig.COMMON.finalStarCopyAnyItem.get() && !IEMCProxy.INSTANCE.hasValue(dropped)) {
			return ItemStack.EMPTY;
		}

		ItemStack copy = ProjectEXConfig.COMMON.finalStarCopyNbt.get()
				? dropped.copy()
				: PersistentItems.normalize(dropped);

		if (copy.isEmpty()) {
			return ItemStack.EMPTY;
		}

		copy.setCount(copy.getMaxStackSize());
		return copy;
	}

	@Override
	@Nonnull
	public List<Component> getPedestalDescription() {
		return List.of(Component.translatable("item.projectex.final_star.pedestal").withStyle(net.minecraft.ChatFormatting.BLUE));
	}
}
