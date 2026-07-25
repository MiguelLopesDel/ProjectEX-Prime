package dev.miguellopesdel.projectex.blockentity;

import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.utils.EMCHelper;
import java.math.BigInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;

/**
 * Base of the EMC links: turns items and EMC into its owner's transmutation balance, and back
 * into items.
 *
 * <p>Both directions are exposed as an item handler, so hoppers, pipes and storage networks can
 * drive a link on their own; the mod's own interface only adds picking what the output slots
 * make.
 */
public class TileLink extends EmcStorageBlockEntity {
	public final OwnerEmcBuffer ownerBuffer = new OwnerEmcBuffer();

	private final LinkItemHandler items;
	private LazyOptional<IItemHandler> itemCapability;

	public TileLink(BlockEntityType<?> type, BlockPos pos, BlockState state, int inputSlots, int outputSlots) {
		super(type, pos, state);
		items = new LinkItemHandler(this, inputSlots, outputSlots);
	}

	public LinkItemHandler items() {
		return items;
	}

	/** The owner's EMC balance, or null while they are offline. */
	@Nullable
	public EmcAccount account() {
		return EmcAccount.of(level, ownerBuffer.owner);
	}

	/**
	 * Whether eating an item also teaches it to the owner. Only the refined links do, which is
	 * what separates them from the personal link.
	 */
	protected boolean learnsItems() {
		return false;
	}

	public void setOwner(LivingEntity entity) {
		ownerBuffer.setOwner(entity);
		setChanged();
	}

	@Override
	protected void onSecond() {
		consumeInputs();

		if (ownerBuffer.hasPending() && ownerBuffer.deposit(level, 0L)) {
			setChanged();
		}
	}

	/**
	 * Eats whatever was fed into the input slots and pays for it. What it pays is the item's
	 * sell value, so ProjectE's covalence loss setting applies here as it does to a transmutation
	 * table; paying the full value would make a link a way around a pack's economy.
	 *
	 * <p>When the owner is offline the EMC waits in the buffer, but the item is still eaten, so
	 * an automated feed never backs up.
	 */
	private void consumeInputs() {
		EmcAccount account = account();

		for (int i = 0; i < items.inputCount(); i++) {
			ItemStack stack = items.getInput(i);

			if (stack.isEmpty()) {
				continue;
			}

			long value = EMCHelper.getEmcSellValue(stack);

			if (value <= 0L) {
				continue;
			}

			if (account != null) {
				if (learnsItems()) {
					account.learn(stack);
				}

				account.deposit(BigInteger.valueOf(value).multiply(BigInteger.valueOf(stack.getCount())));
			} else {
				ownerBuffer.add(value * stack.getCount());
			}

			items.clearInput(i);
			setChanged();
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		items.save(tag);
		tag.putString("OwnerName", ownerBuffer.ownerName);
		return tag;
	}

	@Override
	public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
		return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void setChanged() {
		super.setChanged();

		// The screen draws the templates, so the client needs them even while nothing is
		// affordable and the slots themselves are empty.
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		ownerBuffer.load(tag);
		items.load(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ownerBuffer.save(tag);
		items.save(tag);
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER) {
			if (itemCapability == null || !itemCapability.isPresent()) {
				itemCapability = LazyOptional.of(() -> items);
			}

			return itemCapability.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();

		if (itemCapability != null) {
			itemCapability.invalidate();
			itemCapability = null;
		}
	}

	/** A link is a one way door for EMC: it goes in and straight to the owner, never back out. */
	@Override
	public long getStoredEmc() {
		return 0L;
	}

	@Override
	public long getMaximumEmc() {
		return Long.MAX_VALUE;
	}

	@Override
	public long extractEmc(long emc, EmcAction action) {
		return emc < 0L ? insertEmc(-emc, action) : 0L;
	}

	@Override
	public long insertEmc(long emc, EmcAction action) {
		if (emc <= 0L) {
			return 0L;
		}

		if (action.execute()) {
			ownerBuffer.add(emc);
			setChanged();
		}

		return emc;
	}
}
