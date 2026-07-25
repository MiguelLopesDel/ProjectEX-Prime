package dev.miguellopesdel.projectex.blockentity;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.emc.nbt.NBTManager;
import net.minecraft.world.item.ItemStack;

/**
 * Strips an item stack down to the part of it that EMC actually knows about.
 *
 * <p>A link produces items out of nothing but EMC, so whatever it remembers as a template has to
 * be the plain item, not the particular copy the player happened to be holding. Without this a
 * damaged, enchanted or otherwise tagged stack would become the template and then come back out
 * of the link over and over with all of it intact, at the price of the plain item.
 *
 * <p>Which tags survive is not this mod's decision: ProjectE runs the stack through the same
 * processors it uses to price it, so an item whose NBT it charges for keeps that NBT and
 * everything else is dropped.
 */
public final class PersistentItems {
	private PersistentItems() {
	}

	/** The item as ProjectE's knowledge stores it. */
	public static ItemInfo infoOf(ItemStack stack) {
		return NBTManager.getPersistentInfo(ItemInfo.fromStack(stack));
	}

	/** The item as a link should keep it: one of it, and only the NBT that was paid for. */
	public static ItemStack normalize(ItemStack stack) {
		return stack.isEmpty() ? ItemStack.EMPTY : infoOf(stack).createStack();
	}
}
