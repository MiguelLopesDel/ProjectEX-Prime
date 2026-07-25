package dev.miguellopesdel.projectex.blockentity;

import dev.miguellopesdel.projectex.ProjectEXConfig;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * The inventory of an EMC link, and the whole of its item behaviour.
 *
 * <p>The two halves work in opposite directions. Input slots hold real items that the link eats
 * once a second, paying their EMC to the owner. Output slots hold a template, not stock: the
 * count reported for a template is however many the owner can currently afford, so a pipe or an
 * item conduit sees a chest that refills itself as EMC comes in.
 *
 * <p>Because all of that is expressed as an item handler, hoppers, pipes and storage networks
 * drive a link without any of the mod's own interface being involved.
 */
public class LinkItemHandler implements IItemHandlerModifiable {
	private final TileLink link;
	private final NonNullList<ItemStack> inputs;
	private final NonNullList<ItemStack> outputs;

	public LinkItemHandler(TileLink link, int inputSlots, int outputSlots) {
		this.link = link;
		this.inputs = NonNullList.withSize(inputSlots, ItemStack.EMPTY);
		this.outputs = NonNullList.withSize(outputSlots, ItemStack.EMPTY);
	}

	public int inputCount() {
		return inputs.size();
	}

	public ItemStack getInput(int index) {
		return inputs.get(index);
	}

	public void clearInput(int index) {
		inputs.set(index, ItemStack.EMPTY);
	}

	/** The template of an output slot, always a single item; only the menu sets these. */
	public ItemStack getTemplate(int index) {
		return outputs.get(index);
	}

	public void setTemplate(int index, ItemStack stack) {
		outputs.set(index, stack.isEmpty() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(stack, 1));
		link.setChanged();
	}

	@Override
	public int getSlots() {
		return inputs.size() + outputs.size();
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		if (slot < inputs.size()) {
			return inputs.get(slot);
		}

		ItemStack template = outputs.get(slot - inputs.size());
		int affordable = affordable(template, template.getMaxStackSize());
		return affordable <= 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(template, affordable);
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		if (slot < inputs.size()) {
			inputs.set(slot, stack);
			link.setChanged();
		}
	}

	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (slot >= inputs.size() || stack.isEmpty() || !IEMCProxy.INSTANCE.hasValue(stack)) {
			return stack;
		}

		ItemStack present = inputs.get(slot);
		int room = stack.getMaxStackSize();

		if (!present.isEmpty()) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, present)) {
				return stack;
			}

			room -= present.getCount();
		}

		if (room <= 0) {
			return stack;
		}

		int accepted = Math.min(room, stack.getCount());

		if (!simulate) {
			if (present.isEmpty()) {
				inputs.set(slot, ItemHandlerHelper.copyStackWithSize(stack, accepted));
			} else {
				present.grow(accepted);
			}

			link.setChanged();
		}

		return accepted >= stack.getCount() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - accepted);
	}

	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (slot < inputs.size() || amount <= 0) {
			return ItemStack.EMPTY;
		}

		ItemStack template = outputs.get(slot - inputs.size());
		long value = valueOf(template);

		if (value <= 0L) {
			return ItemStack.EMPTY;
		}

		int count = affordable(template, Math.min(amount, template.getMaxStackSize()));

		if (count <= 0) {
			return ItemStack.EMPTY;
		}

		if (!simulate) {
			EmcAccount account = link.account();

			// The owner may have logged out or spent the EMC between the count and the charge.
			if (account == null || !account.spend(BigInteger.valueOf(value).multiply(BigInteger.valueOf(count)))) {
				return ItemStack.EMPTY;
			}
		}

		return ItemHandlerHelper.copyStackWithSize(template, count);
	}

	@Override
	public int getSlotLimit(int slot) {
		return slot < inputs.size() ? 64 : (int) Math.min(Integer.MAX_VALUE, ProjectEXConfig.COMMON.emcLinkMaxOut.get());
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return slot < inputs.size() && IEMCProxy.INSTANCE.hasValue(stack);
	}

	/** How many of the template the owner's balance covers, capped by the config and by {@code limit}. */
	private int affordable(ItemStack template, int limit) {
		long value = valueOf(template);

		if (value <= 0L) {
			return 0;
		}

		long maxOut = ProjectEXConfig.COMMON.emcLinkMaxOut.get();

		if (maxOut <= 0L) {
			return 0;
		}

		EmcAccount account = link.account();

		if (account == null) {
			return 0;
		}

		BigInteger count = account.balance().divide(BigInteger.valueOf(value));
		return count.min(BigInteger.valueOf(Math.min(limit, maxOut))).intValueExact();
	}

	private long valueOf(ItemStack stack) {
		return stack.isEmpty() ? 0L : IEMCProxy.INSTANCE.getValue(stack);
	}

	public void save(CompoundTag tag) {
		tag.put("Inputs", saveList(inputs));
		tag.put("Outputs", saveList(outputs));
	}

	public void load(CompoundTag tag) {
		loadList(tag.getList("Inputs", Tag.TAG_COMPOUND), inputs);
		loadList(tag.getList("Outputs", Tag.TAG_COMPOUND), outputs);
	}

	private static ListTag saveList(NonNullList<ItemStack> stacks) {
		ListTag list = new ListTag();

		for (int i = 0; i < stacks.size(); i++) {
			if (!stacks.get(i).isEmpty()) {
				CompoundTag entry = stacks.get(i).save(new CompoundTag());
				entry.putByte("Slot", (byte) i);
				list.add(entry);
			}
		}

		return list;
	}

	private static void loadList(ListTag list, NonNullList<ItemStack> stacks) {
		stacks.clear();

		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			int slot = entry.getByte("Slot") & 0xFF;

			if (slot < stacks.size()) {
				stacks.set(slot, ItemStack.of(entry));
			}
		}
	}
}
