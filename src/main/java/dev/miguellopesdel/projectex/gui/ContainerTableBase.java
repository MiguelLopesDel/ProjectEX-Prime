package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.Knowledge;
import dev.miguellopesdel.projectex.blockentity.PersistentItems;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.MathUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import java.math.BigInteger;

/**
 * What every screen that spends and earns EMC shares: selling an item, buying one back, and the
 * learn and unlearn buttons.
 *
 * <p>In 1.12 the client ran all of this too, optimistically, and then told the server what it had
 * done. Here only the server runs it. The screen asks, by way of {@link
 * dev.miguellopesdel.projectex.net.PacketTableAction}, and reads the answer off the balance and the
 * knowledge that ProjectE already syncs. That costs a tick of latency and removes every way a
 * client could disagree with the server about what it owns.
 */
public abstract class ContainerTableBase extends AbstractContainerMenu {
	public static final int BURN = 1;
	public static final int TAKE_STACK = 2;
	public static final int TAKE_ONE = 3;
	public static final int BURN_ALT = 4;
	public static final int LEARN = 5;
	public static final int UNLEARN = 6;

	public final Player player;
	public final IKnowledgeProvider knowledge;

	protected ContainerTableBase(MenuType<?> type, int windowId, Inventory playerInventory) {
		super(type, windowId);
		player = playerInventory.player;
		knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY)
				.orElseThrow(() -> new IllegalStateException("player has no ProjectE knowledge"));
	}

	/** Lets a screen refuse items it does not deal in; the Stone Table has a whitelist. */
	public boolean isItemValid(ItemInfo item) {
		return true;
	}

	/**
	 * Runs one press of a panel button. Returns whether anything changed, which is what tells the
	 * packet handler to push the container back to the client.
	 */
	public boolean clickGuiSlot(@Nullable ItemInfo type, int mode) {
		if (!(player instanceof ServerPlayer)) {
			return false;
		}

		ItemStack carried = getCarried();

		return switch (mode) {
			case BURN -> sellCarried(carried);
			// Shift over the burn slot charges or drains a Klein Star instead of selling it, which
			// is the only way to get EMC back out of one by hand.
			case BURN_ALT -> transferHeldEmc(carried) || sellCarried(carried);
			case TAKE_STACK -> take(type, true);
			case TAKE_ONE -> take(type, false);
			case LEARN -> learn(carried);
			case UNLEARN -> !carried.isEmpty() && Knowledge.forget(player, knowledge, carried);
			default -> false;
		};
	}

	/**
	 * Sells a stack: the player learns it, and is paid what the table pays, which is less than the
	 * item is worth when the pack charges covalence loss.
	 */
	protected boolean sell(ItemStack stack) {
		if (stack.isEmpty() || !EMCHelper.doesItemHaveEmc(stack)) {
			return false;
		}

		ItemInfo item = PersistentItems.infoOf(stack);

		if (!isItemValid(item) || !Knowledge.teach(player, knowledge, ItemInfo.fromStack(stack)).known()) {
			return false;
		}

		addEmc(BigInteger.valueOf(EMCHelper.getEmcSellValue(item)).multiply(BigInteger.valueOf(stack.getCount())));
		return true;
	}

	private boolean sellCarried(ItemStack carried) {
		if (!sell(carried)) {
			return false;
		}

		setCarried(ItemStack.EMPTY);
		return true;
	}

	private boolean learn(ItemStack carried) {
		return !carried.isEmpty()
				&& EMCHelper.doesItemHaveEmc(carried)
				&& isItemValid(PersistentItems.infoOf(carried))
				&& Knowledge.teach(player, knowledge, carried) == Knowledge.Result.LEARNED;
	}

	/**
	 * Buys an item the player knows, either one onto the cursor or a full stack into the inventory,
	 * and never more than the balance covers.
	 *
	 * <p>1.12 trusted the button for this and did not check knowledge at all, because the buttons
	 * were built from the knowledge list to begin with. They still are, but the check is made here
	 * as well, where it is the server making it.
	 */
	private boolean take(@Nullable ItemInfo type, boolean fullStack) {
		if (type == null || !knowledge.hasKnowledge(type) || !isItemValid(type)) {
			return false;
		}

		long value = EMCHelper.getEmcValue(type);

		if (value <= 0L) {
			return false;
		}

		ItemStack prototype = type.createStack();

		if (prototype.isEmpty()) {
			return false;
		}

		BigInteger price = BigInteger.valueOf(value);
		long affordable = MathUtils.clampToLong(knowledge.getEmc().divide(price));

		if (affordable <= 0L) {
			return false;
		}

		if (fullStack) {
			int amount = (int) Math.min(prototype.getMaxStackSize(), affordable);
			prototype.setCount(amount);
			removeEmc(price.multiply(BigInteger.valueOf(amount)));
			player.getInventory().placeItemBackInInventory(prototype);
			return true;
		}

		ItemStack carried = getCarried();

		if (carried.isEmpty()) {
			removeEmc(price);
			setCarried(prototype);
			return true;
		}

		if (!ItemStack.isSameItemSameTags(carried, prototype) || carried.getCount() >= carried.getMaxStackSize()) {
			return false;
		}

		removeEmc(price);
		carried.grow(1);
		setCarried(carried);
		return true;
	}

	/**
	 * Empties an EMC holding item into the balance, or fills it from the balance when it is already
	 * empty. One button, because which of the two the player wants is never in doubt.
	 */
	private boolean transferHeldEmc(ItemStack carried) {
		IItemEmcHolder holder = carried.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).orElse(null);

		if (holder == null) {
			return false;
		}

		long stored = holder.getStoredEmc(carried);

		if (stored > 0L) {
			long extracted = holder.extractEmc(carried, stored, IEmcStorage.EmcAction.EXECUTE);

			if (extracted <= 0L) {
				return false;
			}

			addEmc(BigInteger.valueOf(extracted));
		} else {
			long inserted = holder.insertEmc(carried, MathUtils.clampToLong(knowledge.getEmc()), IEmcStorage.EmcAction.EXECUTE);

			if (inserted <= 0L) {
				return false;
			}

			removeEmc(BigInteger.valueOf(inserted));
		}

		setCarried(carried);
		return true;
	}

	protected void addEmc(BigInteger amount) {
		setEmc(knowledge.getEmc().add(amount));
	}

	protected void removeEmc(BigInteger amount) {
		setEmc(knowledge.getEmc().subtract(amount).max(BigInteger.ZERO));
	}

	private void setEmc(BigInteger amount) {
		knowledge.setEmc(amount);

		if (player instanceof ServerPlayer serverPlayer) {
			knowledge.syncEmc(serverPlayer);
		}
	}

	/**
	 * Shift clicking anything sells it. There is nowhere else for an item to go in a transmutation
	 * screen, and it is what the 1.12 tables did.
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);

		if (!slot.hasItem() || !slot.mayPickup(player) || !sell(slot.getItem())) {
			return ItemStack.EMPTY;
		}

		slot.set(ItemStack.EMPTY);
		slot.setChanged();

		// The whole stack went at once, so there is nothing left for vanilla to shift click again.
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
}
