package dev.miguellopesdel.projectex.blockentity;

import dev.miguellopesdel.projectex.recipe.AlchemyTableRecipe;
import dev.miguellopesdel.projectex.recipe.ProjectEXRecipeTypes;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Turns one item into another by spending EMC.
 *
 * <p>It has no EMC of its own to speak of: it asks for exactly what the step in front of it costs
 * and nothing more, so a collector feeding a table that has nothing to do is not quietly filling a
 * buffer. That is what {@link #getMaximumEmc()} returning zero when there is no recipe means.
 *
 * <p>Unlike everything else in this mod it runs every tick rather than once a second, because a
 * progress bar that moves in ten steps is not a progress bar.
 */
public class TileAlchemyTable extends BlockEntity implements IEmcStorage {
	private static final int INPUT = 0;
	private static final int OUTPUT = 1;

	/** How much of a step's cost the table is allowed to hold, so it can work without stuttering. */
	private static final long BUFFER = 8L;

	private final EmcStorageCapability emcStorage = new EmcStorageCapability(this);
	private LazyOptional<IItemHandler> itemHandler;

	private long storedEMC;
	private int progress;

	private long cost;
	private int duration;

	/** The recipe for whatever is in the input slot, kept so the list is not searched every tick. */
	@Nullable
	private AlchemyTableRecipe recipe;
	private ItemStack recipeFor = ItemStack.EMPTY;

	public final ItemStackHandler items = new ItemStackHandler(2) {
		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
			return slot == INPUT && hasRecipe(stack);
		}

		@Override
		@Nonnull
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			// The output slot is an exit. A pipe pushing into it would jam the table with something
			// it cannot make.
			return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
		}

		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};

	public TileAlchemyTable(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.ALCHEMY_TABLE.get(), pos, state);
	}

	public void tick() {
		if (level == null || level.isClientSide()) {
			return;
		}

		cost = 0L;
		duration = 0;

		AlchemyTableRecipe step = currentRecipe();

		if (step == null) {
			return;
		}

		ItemStack input = items.getStackInSlot(INPUT);
		cost = step.cost(input);
		duration = step.duration();

		if (storedEMC < cost) {
			return;
		}

		if (++progress < duration) {
			setChanged();
			return;
		}

		storedEMC -= cost;
		progress = 0;

		input.shrink(1);
		items.setStackInSlot(INPUT, input.isEmpty() ? ItemStack.EMPTY : input);

		ItemStack result = step.output().copy();
		ItemStack output = items.getStackInSlot(OUTPUT);

		if (output.isEmpty()) {
			items.setStackInSlot(OUTPUT, result);
		} else {
			output.grow(result.getCount());
		}

		setChanged();
	}

	/**
	 * The step the table can work on right now, or nothing when the input has no recipe or the
	 * output slot is holding something else.
	 */
	@Nullable
	private AlchemyTableRecipe currentRecipe() {
		ItemStack input = items.getStackInSlot(INPUT);

		if (input.isEmpty()) {
			progress = 0;
			return null;
		}

		if (!ItemStack.isSameItemSameTags(recipeFor, input)) {
			recipe = findRecipe(input);
			recipeFor = input.copy();
			progress = 0;
		}

		if (recipe == null) {
			return null;
		}

		ItemStack output = items.getStackInSlot(OUTPUT);

		if (output.isEmpty()) {
			return recipe;
		}

		if (!ItemStack.isSameItemSameTags(output, recipe.output())
				|| output.getCount() + recipe.output().getCount() > output.getMaxStackSize()) {
			return null;
		}

		return recipe;
	}

	@Nullable
	private AlchemyTableRecipe findRecipe(ItemStack input) {
		if (level == null) {
			return null;
		}

		for (AlchemyTableRecipe candidate : level.getRecipeManager().getAllRecipesFor(ProjectEXRecipeTypes.ALCHEMY_TABLE.get())) {
			if (candidate.input().test(input)) {
				return candidate;
			}
		}

		return null;
	}

	/** Used by the input slot to refuse items the table has no step for. */
	private boolean hasRecipe(ItemStack stack) {
		return !stack.isEmpty() && findRecipe(stack) != null;
	}

	public int progress() {
		return progress;
	}

	public int duration() {
		return duration;
	}

	public long cost() {
		return cost;
	}

	@Override
	public long getStoredEmc() {
		return storedEMC;
	}

	@Override
	public long getMaximumEmc() {
		return cost * BUFFER;
	}

	@Override
	public long insertEmc(long emc, EmcAction action) {
		if (emc < 0L) {
			return extractEmc(-emc, action);
		}

		long inserted = Math.min(emc, Math.max(0L, getMaximumEmc() - storedEMC));

		if (action.execute() && inserted > 0L) {
			storedEMC += inserted;
			setChanged();
		}

		return inserted;
	}

	@Override
	public long extractEmc(long emc, EmcAction action) {
		// What goes in is spent here. Letting it back out would make the table a battery that
		// happens to transmute, and a way to move EMC through a wall of them.
		return 0L;
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		LazyOptional<T> emc = emcStorage.get(cap);

		if (emc.isPresent()) {
			return emc;
		}

		if (cap == ForgeCapabilities.ITEM_HANDLER) {
			if (itemHandler == null || !itemHandler.isPresent()) {
				itemHandler = LazyOptional.of(() -> items);
			}

			return itemHandler.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		emcStorage.invalidate();

		if (itemHandler != null) {
			itemHandler.invalidate();
			itemHandler = null;
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		storedEMC = tag.getLong("StoredEMC");
		progress = tag.getInt("Progress");
		items.deserializeNBT(tag.getCompound("Items"));
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putLong("StoredEMC", storedEMC);
		tag.putInt("Progress", progress);
		tag.put("Items", items.serializeNBT());
	}
}
