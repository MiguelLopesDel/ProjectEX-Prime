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

	/** EMC from items eaten since the last payout. Saved, so unloading mid second loses nothing. */
	private long earned;

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

	/**
	 * Input is the one thing here that cannot wait for the beat. A slot holds sixty four, so eating
	 * once a second would cap a refined link at sixty four items a second, and a refined link exists
	 * to be fed by a storage system. 1.12 ate every tick for the same reason.
	 */
	@Override
	protected void onTick() {
		consumeInputs();
	}

	@Override
	protected void onSecond() {
		payIn();

		if (ownerBuffer.hasPending() && ownerBuffer.deposit(level, 0L)) {
			setChanged();
		}
	}

	/**
	 * Hands a whole second of eaten items over in one go. Paying per stack instead would set the
	 * owner's balance, and send them a packet saying so, once for every slot that had something in
	 * it, twenty times a second.
	 */
	private void payIn() {
		if (earned <= 0L) {
			return;
		}

		EmcAccount account = account();

		if (account != null) {
			account.deposit(BigInteger.valueOf(earned));
		} else {
			ownerBuffer.add(earned);
		}

		earned = 0L;
		setChanged();
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
		EmcAccount account = null;
		boolean lookedUp = false;
		boolean ate = false;

		for (int i = 0; i < items.inputCount(); i++) {
			ItemStack stack = items.getInput(i);

			if (stack.isEmpty()) {
				continue;
			}

			long value = EMCHelper.getEmcSellValue(stack);

			if (value <= 0L) {
				continue;
			}

			// Looked up lazily: an idle link runs this every tick and should not be searching the
			// player list for an owner it has no reason to talk to yet.
			if (!lookedUp) {
				account = account();
				lookedUp = true;
			}

			if (account != null && learnsItems()) {
				account.learn(stack);
			}

			earned += value * stack.getCount();
			items.clearInput(i);
			ate = true;
		}

		if (ate) {
			// Only the chunk needs telling. What a client draws from a link is its templates, and
			// those have not changed; a block update per eaten stack, twenty times a second, would
			// be a packet storm carrying nothing.
			super.setChanged();
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
		earned = tag.getLong("Earned");
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		ownerBuffer.save(tag);
		items.save(tag);
		tag.putLong("Earned", earned);
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
