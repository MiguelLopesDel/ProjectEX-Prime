package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.blockentity.EmcAccount;
import dev.miguellopesdel.projectex.blockentity.LinkItemHandler;
import dev.miguellopesdel.projectex.blockentity.TileLink;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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
		if (slotId >= firstOutputSlot && slotId < firstOutputSlot + (items.getSlots() - items.inputCount())) {
			ItemStack carried = getCarried();

			if (!carried.isEmpty()) {
				if (IEMCProxy.INSTANCE.hasValue(carried)) {
					items.setTemplate(slotId - firstOutputSlot, carried);

					// Setting a template teaches the item, so it can be produced from EMC alone.
					EmcAccount account = link.account();

					if (account != null) {
						account.learn(carried);
					}
				}

				return;
			}

			if (clickType == ClickType.PICKUP && button == 1) {
				items.setTemplate(slotId - firstOutputSlot, ItemStack.EMPTY);
				return;
			}
		}

		super.clicked(slotId, button, clickType, player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);

		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		int inventoryStart = items.getSlots();

		if (index < inventoryStart) {
			// Out of the link and into the player, which for an output slot spends EMC.
			if (!moveItemStackTo(stack, inventoryStart, slots.size(), true)) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, stack);
		} else if (!moveItemStackTo(stack, 0, items.inputCount(), false)) {
			return ItemStack.EMPTY;
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
