package dev.miguellopesdel.projectex;

import dev.miguellopesdel.projectex.blockentity.PersistentItems;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import moze_intel.projecte.emc.nbt.NBTManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

/**
 * Teaching an item is three things happening together: ProjectE reduces it to what EMC actually
 * prices, a pack gets the chance to refuse, and the client has to be told. Every place in this mod
 * that learns or forgets something goes through here so all three keep happening.
 */
public final class Knowledge {
	private Knowledge() {
	}

	/** What became of an attempt to teach an item. */
	public enum Result {
		/** A pack, or ProjectE itself, would not have it. */
		REFUSED,
		ALREADY_KNOWN,
		LEARNED;

		/** Whether the player knows the item now, however it got there. */
		public boolean known() {
			return this != REFUSED;
		}
	}

	@Nullable
	public static IKnowledgeProvider of(Player player) {
		return player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElse(null);
	}

	public static Result teach(Player player, IKnowledgeProvider provider, ItemStack stack) {
		return teach(player, provider, ItemInfo.fromStack(stack));
	}

	/**
	 * Teaches an item as EMC stores it. The event sees the item the player actually offered, so a
	 * pack can refuse a specific enchanted or damaged stack, but what gets learned is the reduced
	 * form, which is the only thing knowledge can hold.
	 */
	public static Result teach(Player player, IKnowledgeProvider provider, ItemInfo source) {
		ItemInfo reduced = NBTManager.getPersistentInfo(source);

		if (provider.hasKnowledge(reduced)) {
			return Result.ALREADY_KNOWN;
		}

		if (MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(player, source, reduced))) {
			return Result.REFUSED;
		}

		if (!provider.addKnowledge(reduced)) {
			return Result.REFUSED;
		}

		sync(player, provider, reduced, true);
		return Result.LEARNED;
	}

	public static boolean forget(Player player, IKnowledgeProvider provider, ItemStack stack) {
		ItemInfo reduced = PersistentItems.infoOf(stack);

		if (!provider.removeKnowledge(reduced)) {
			return false;
		}

		sync(player, provider, reduced, false);
		return true;
	}

	private static void sync(Player player, IKnowledgeProvider provider, ItemInfo item, boolean learned) {
		if (player instanceof ServerPlayer serverPlayer) {
			provider.syncKnowledgeChange(serverPlayer, item, learned);
		}
	}
}
