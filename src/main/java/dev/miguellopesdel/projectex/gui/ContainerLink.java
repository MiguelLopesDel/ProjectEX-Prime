package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.Knowledge;
import dev.miguellopesdel.projectex.blockentity.LinkItemHandler;
import dev.miguellopesdel.projectex.blockentity.TileLink;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerAttemptCondenserSetEvent;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.emc.nbt.NBTManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.SlotItemHandler;

/**
 * The menu of an EMC link.
 *
 * <p>Both halves of the link are real slots here, unlike 1.12 where the output side was drawn
 * as custom widgets. The output slots are backed by the same item handler that hoppers and
 * storage networks see, so what the player takes out and what a pipe pulls out go through one
 * path.
 *
 * <p>Clicking an output slot while holding an item sets what that slot produces, which is the
 * one thing automation cannot do on its own and the reason this screen exists.
 */
public class ContainerLink extends AbstractContainerMenu {
	public final TileLink link;
	private final LinkItemHandler items;
	private final int firstOutputSlot;

	public ContainerLink(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		this(windowId, playerInventory, blockEntity(playerInventory, buffer.readBlockPos()));
	}

	public ContainerLink(int windowId, Inventory playerInventory, TileLink link) {
		super(ProjectEXMenus.LINK.get(), windowId);
		this.link = link;
		this.items = link.items();

		int inputs = items.inputCount();
		int outputs = items.getSlots() - inputs;
		firstOutputSlot = inputs;

		Layout layout = Layout.of(inputs, outputs);

		for (int i = 0; i < inputs; i++) {
			addSlot(new SlotItemHandler(items, i, layout.inputX(i), layout.inputY(i)));
		}

		for (int i = 0; i < outputs; i++) {
			addSlot(new SlotItemHandler(items, inputs + i, layout.outputX(i), layout.outputY(i)));
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, layout.inventoryY() + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(playerInventory, column, 8 + column * 18, layout.inventoryY() + 58));
		}
	}

	private static TileLink blockEntity(Inventory playerInventory, BlockPos pos) {
		return playerInventory.player.level().getBlockEntity(pos) instanceof TileLink link ? link : null;
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickType, Player player) {
		// Holding an item over an output slot picks what that slot makes, rather than trying to
		// put the item in a slot that only ever hands items out.
		if (slotId >= firstOutputSlot && slotId < firstOutputSlot + items.outputCount()) {
			ItemStack carried = getCarried();

			if (!carried.isEmpty()) {
				setTemplate(player, slotId - firstOutputSlot, carried);
				return;
			}

			// Shift and right click clears the template. Plain right click is left alone so it
			// keeps taking half a stack, which is what every other slot in the game does.
			if (clickType == ClickType.QUICK_MOVE && button == 1) {
				items.setTemplate(slotId - firstOutputSlot, ItemStack.EMPTY);
				return;
			}
		}

		super.clicked(slotId, button, clickType, player);
	}

	/**
	 * Picks what an output slot makes, and teaches the item while doing so.
	 *
	 * <p>The stack is first reduced to what EMC actually knows about, through the same processors
	 * ProjectE prices it with. Otherwise a damaged or tagged item could become the template and
	 * then come back out of the link with all of it intact, for the price of the plain item.
	 *
	 * <p>Both of ProjectE's veto events get a say, so a pack that stops an item being learned or
	 * being locked into a condenser stops it here too. Unlike 1.12, an item the player is not
	 * allowed to learn cannot be set either: setting it anyway left the link producing exactly
	 * what the pack had just refused to teach.
	 */
	private void setTemplate(Player player, int index, ItemStack carried) {
		// Knowledge lives on the server, and it sends the template back to the screen.
		if (player.level().isClientSide() || !IEMCProxy.INSTANCE.hasValue(carried)) {
			return;
		}

		IKnowledgeProvider knowledge = Knowledge.of(player);

		if (knowledge == null) {
			return;
		}

		ItemInfo source = ItemInfo.fromStack(carried);
		ItemInfo reduced = NBTManager.getPersistentInfo(source);

		if (!Knowledge.teach(player, knowledge, source).known()) {
			return;
		}

		if (!MinecraftForge.EVENT_BUS.post(new PlayerAttemptCondenserSetEvent(player, source, reduced))) {
			items.setTemplate(index, reduced.createStack());
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);

		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		int inventoryStart = items.getSlots();

		if (index >= inventoryStart) {
			moveItemStackTo(slot.getItem(), 0, items.inputCount(), false);
			return ItemStack.EMPTY;
		}

		// Vanilla's shift click moves items by mutating the stack a slot hands back, which only
		// works when that stack is the one the inventory stores. An output slot computes its
		// stack from the owner's balance every time it is asked, so mutating it moves nothing
		// and charges nothing. Extraction has to be asked for explicitly instead.
		ItemStack available = items.extractItem(index, slot.getItem().getMaxStackSize(), true);

		if (available.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack remainder = available.copy();

		if (!moveItemStackTo(remainder, inventoryStart, slots.size(), true)) {
			return ItemStack.EMPTY;
		}

		int moved = available.getCount() - remainder.getCount();

		if (moved > 0) {
			// Charges the EMC, and can still come back empty if the balance moved meanwhile.
			items.extractItem(index, moved, false);
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return link != null && !link.isRemoved() && player.distanceToSqr(link.getBlockPos().getCenter()) <= 64.0D;
	}

	/**
	 * Slot positions, taken from the 1.12 screen: the input slots and the player inventory come
	 * from its container, the output positions from where it placed its output buttons.
	 */
	private record Layout(int inputX, int inputY, int inputColumns, int outputX, int outputY, int outputColumns, int inventoryY) {
		static Layout of(int inputs, int outputs) {
			if (inputs > 1) {
				// Personal link: six by three inputs, a single output on the right.
				return new Layout(8, 17, 6, 152, 35, 1, 84);
			}

			if (outputs > 9) {
				// Compressed refined link: one input above a nine by six output grid.
				return new Layout(8, 17, 1, 8, 41, 9, 162);
			}

			// Refined link: one input beside a three by three output grid.
			return new Layout(35, 35, 1, 89, 17, 3, 84);
		}

		int inputX(int index) {
			return inputX + (index % inputColumns) * 18;
		}

		int inputY(int index) {
			return inputY + (index / inputColumns) * 18;
		}

		int outputX(int index) {
			return outputX + (index % outputColumns) * 18;
		}

		int outputY(int index) {
			return outputY + (index / outputColumns) * 18;
		}
	}
}
