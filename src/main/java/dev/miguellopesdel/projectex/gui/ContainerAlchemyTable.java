package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.block.BlockAlchemyTable;
import dev.miguellopesdel.projectex.blockentity.TileAlchemyTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Two slots and two bars.
 *
 * <p>The bars travel as a fill from 0 to 255 rather than as the real numbers behind them: a
 * container's synced fields are shorts, and a recipe's cost is a long. The screen only has 22 pixels
 * to draw either one in, so nothing is lost.
 */
public class ContainerAlchemyTable extends AbstractContainerMenu {
	public static final int BAR_STEPS = 255;

	private static final int INPUT = 0;
	private static final int OUTPUT = 1;

	private final TileAlchemyTable table;

	private final DataSlot progress = DataSlot.standalone();
	private final DataSlot charge = DataSlot.standalone();

	public ContainerAlchemyTable(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		this(windowId, playerInventory, blockEntity(playerInventory, buffer.readBlockPos()));
	}

	public ContainerAlchemyTable(int windowId, Inventory playerInventory, TileAlchemyTable table) {
		super(ProjectEXMenus.ALCHEMY_TABLE.get(), windowId);
		this.table = table;

		addSlot(new SlotItemHandler(table.items, INPUT, 44, 35));
		addSlot(new SlotItemHandler(table.items, OUTPUT, 116, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
		}

		addDataSlot(progress);
		addDataSlot(charge);
	}

	private static TileAlchemyTable blockEntity(Inventory playerInventory, BlockPos pos) {
		return playerInventory.player.level().getBlockEntity(pos) instanceof TileAlchemyTable table ? table : null;
	}

	public int progressFill() {
		return progress.get();
	}

	public int chargeFill() {
		return charge.get();
	}

	@Override
	public void broadcastChanges() {
		progress.set(fill(table.progress(), table.duration()));
		charge.set(fill(table.getStoredEmc(), table.cost()));
		super.broadcastChanges();
	}

	private static int fill(long amount, long total) {
		if (total <= 0L || amount <= 0L) {
			return 0;
		}

		return (int) Math.min(BAR_STEPS, amount * BAR_STEPS / total);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);

		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();

		if (index <= OUTPUT) {
			if (!moveItemStackTo(stack, OUTPUT + 1, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, INPUT, INPUT + 1, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return original;
	}

	@Override
	public boolean stillValid(Player player) {
		return table != null && !table.isRemoved()
				&& table.getBlockState().getBlock() instanceof BlockAlchemyTable
				&& player.distanceToSqr(table.getBlockPos().getCenter()) <= 64.0D;
	}
}
