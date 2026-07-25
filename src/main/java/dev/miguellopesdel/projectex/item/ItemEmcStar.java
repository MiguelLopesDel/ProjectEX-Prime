package dev.miguellopesdel.projectex.item;

import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.capability.EmcHolderItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.items.IBarHelper;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * A Klein Star that holds more.
 *
 * <p>ProjectE's stars stop at the Omega. These carry on from there, twelve of them, each holding
 * four times the one before it, split into six Magnum and six Colossal.
 *
 * <p>In 1.12 this was two classes, where the Colossal one reached its capacity by adding six to
 * its tier's ordinal and indexing the same table. The capacity is a plain constructor argument
 * here, so the ladder is written once, in {@link ProjectEXItems}, and nothing has to be read
 * backwards to know what a star holds.
 */
public class ItemEmcStar extends ItemPE implements IItemEmcHolder, IBarHelper {
	private final long capacity;
	private final boolean foil;

	public ItemEmcStar(Properties properties, long capacity, boolean foil) {
		super(properties.stacksTo(1));
		this.capacity = capacity;
		this.foil = foil;

		// This is what makes the stack answer to PECapabilities.EMC_HOLDER_ITEM_CAPABILITY, and
		// so what lets a ProjectE relay, condenser or collector charge and drain it.
		addItemCapability(EmcHolderItemCapabilityWrapper::new);
	}

	@Override
	public boolean isFoil(@Nonnull ItemStack stack) {
		return foil;
	}

	@Override
	public long insertEmc(@Nonnull ItemStack stack, long emc, IEmcStorage.EmcAction action) {
		if (emc < 0L) {
			return extractEmc(stack, -emc, action);
		}

		long inserted = Math.min(emc, getNeededEmc(stack));

		if (action.execute()) {
			ItemPE.addEmcToStack(stack, inserted);
		}

		return inserted;
	}

	@Override
	public long extractEmc(@Nonnull ItemStack stack, long emc, IEmcStorage.EmcAction action) {
		if (emc < 0L) {
			return insertEmc(stack, -emc, action);
		}

		long extracted = Math.min(emc, getStoredEmc(stack));

		if (action.execute()) {
			ItemPE.removeEmc(stack, extracted);
		}

		return extracted;
	}

	@Override
	public long getStoredEmc(@Nonnull ItemStack stack) {
		return ItemPE.getEmc(stack);
	}

	@Override
	public long getMaximumEmc(@Nonnull ItemStack stack) {
		return capacity;
	}

	@Override
	public boolean isBarVisible(@Nonnull ItemStack stack) {
		return getStoredEmc(stack) > 0L;
	}

	@Override
	public int getBarWidth(@Nonnull ItemStack stack) {
		return getScaledBarWidth(stack);
	}

	@Override
	public int getBarColor(@Nonnull ItemStack stack) {
		return getColorForBar(stack);
	}

	@Override
	public float getWidthForBar(@Nonnull ItemStack stack) {
		return Mth.clamp((float) ((double) getStoredEmc(stack) / capacity), 0.0F, 1.0F);
	}
}
