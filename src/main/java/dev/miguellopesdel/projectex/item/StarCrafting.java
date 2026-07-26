package dev.miguellopesdel.projectex.item;

import com.google.common.math.LongMath;
import dev.miguellopesdel.projectex.ProjectEX;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Carries stored EMC across a star craft.
 *
 * <p>Every star is four of the star below it, and a crafting recipe cannot see what its ingredients
 * hold, so without this the four stars that went in would have their contents thrown away. The
 * ingredients are read through the capability rather than by type, because the first Magnum star is
 * made of ProjectE's own Klein Stars and those have to count too.
 *
 * <p>Only this mod's stars are filled. ProjectE decides for itself what its own crafts do with what
 * they consume, and a mod that adds an EMC holding item decides for its own.
 */
@Mod.EventBusSubscriber(modid = ProjectEX.MOD_ID)
public final class StarCrafting {
	private StarCrafting() {
	}

	@SubscribeEvent
	public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
		ItemStack crafted = event.getCrafting();

		if (!(crafted.getItem() instanceof ItemEmcStar star)) {
			return;
		}

		Container matrix = event.getInventory();
		long carried = 0L;

		for (int i = 0; i < matrix.getContainerSize(); i++) {
			ItemStack ingredient = matrix.getItem(i);
			IItemEmcHolder holder = ingredient.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).orElse(null);

			if (holder != null) {
				// The ingredients are on their way out, so what they hold is only read, never taken:
				// draining them would fight whatever the crafting screen does with them next.
				carried = LongMath.saturatedAdd(carried, holder.getStoredEmc(ingredient));
			}
		}

		if (carried > 0L) {
			star.insertEmc(crafted, carried, IEmcStorage.EmcAction.EXECUTE);
		}
	}
}
