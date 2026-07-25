package dev.miguellopesdel.projectex.gui;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The tablet's three by three grid, kept in the tablet's own tag.
 *
 * <p>1.12 kept it in ProjectE's transmutation inputs instead, so the grid came along with the
 * player rather than with the tablet. That is not portable to 1.20.1: those nine slots are eight
 * inputs and the target lock, so whatever sat in the bottom right corner of the grid was also what
 * the transmutation table was locked to, and ProjectE now syncs them with packets of its own that
 * writing behind its back would leave stale.
 *
 * <p>Each tablet therefore carries its own grid. Two tablets are two grids, and a tablet handed to
 * someone else arrives with whatever was left in it.
 */
public class ArcaneTabletGrid implements CraftingContainer {
	private static final String TAG = "Grid";
	private static final int WIDTH = 3;
	private static final int HEIGHT = 3;

	private final ContainerArcaneTablet menu;
	private final NonNullList<ItemStack> items = NonNullList.withSize(WIDTH * HEIGHT, ItemStack.EMPTY);

	ArcaneTabletGrid(ContainerArcaneTablet menu, ItemStack tablet) {
		this.menu = menu;

		CompoundTag tag = tablet.getTagElement(TAG);

		if (tag != null) {
			ContainerHelper.loadAllItems(tag, items);
		}
	}

	/**
	 * Writes the grid back into the tablet. Only the server does this: the client is told what its
	 * slots hold by the container, and writing the tag there as well would only fight the next
	 * update to arrive.
	 */
	private void save() {
		if (menu.player.level().isClientSide()) {
			return;
		}

		ItemStack tablet = menu.tablet();

		if (tablet.isEmpty()) {
			return;
		}

		if (isEmpty()) {
			// An empty grid leaves no trace, so a tablet that has been tidied up is a plain tablet
			// again rather than one carrying nine empty slots around.
			CompoundTag tag = tablet.getTag();

			if (tag != null) {
				tag.remove(TAG);

				if (tag.isEmpty()) {
					tablet.setTag(null);
				}
			}

			return;
		}

		tablet.addTagElement(TAG, ContainerHelper.saveAllItems(new CompoundTag(), items, true));
	}

	@Override
	public void setChanged() {
		save();
		menu.slotsChanged(this);
	}

	@Override
	public int getContainerSize() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getItem(int index) {
		return index < 0 || index >= items.size() ? ItemStack.EMPTY : items.get(index);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack removed = ContainerHelper.takeItem(items, index);
		save();
		return removed;
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack removed = ContainerHelper.removeItem(items, index, count);

		if (!removed.isEmpty()) {
			setChanged();
		}

		return removed;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		items.set(index, stack);
		setChanged();
	}

	@Override
	public void clearContent() {
		items.clear();
		save();
	}

	@Override
	public boolean stillValid(Player player) {
		return menu.stillValid(player);
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public List<ItemStack> getItems() {
		return items;
	}

	@Override
	public void fillStackedContents(StackedContents contents) {
		for (ItemStack stack : items) {
			contents.accountSimpleStack(stack);
		}
	}
}
