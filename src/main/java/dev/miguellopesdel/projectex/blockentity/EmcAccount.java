package dev.miguellopesdel.projectex.blockentity;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.math.BigInteger;
import java.util.UUID;

/**
 * The EMC balance a link spends from and pays into.
 *
 * <p>This exists so the links do not talk to {@link IKnowledgeProvider} directly. In 1.12 the
 * mod carried its own reader for the save files of offline players, so that automation kept
 * producing while its owner was away. That reader is not ported yet, so right now an account
 * only exists while its owner is online. When it comes back it becomes a second implementation
 * of this interface, not a change to the links.
 */
public interface EmcAccount {
	BigInteger balance();

	/** @return false when the balance was not enough, in which case nothing is spent */
	boolean spend(BigInteger amount);

	void deposit(BigInteger amount);

	/** Teaches the owner an item, so that what a link eats becomes transmutable. */
	void learn(ItemStack stack);

	@Nullable
	static EmcAccount of(Level level, UUID owner) {
		if (level == null || level.getServer() == null) {
			return null;
		}

		ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);

		if (player == null) {
			return null;
		}

		IKnowledgeProvider provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElse(null);
		return provider == null ? null : new OnlineAccount(player, provider);
	}

	record OnlineAccount(ServerPlayer player, IKnowledgeProvider provider) implements EmcAccount {
		@Override
		public BigInteger balance() {
			return provider.getEmc();
		}

		@Override
		public boolean spend(BigInteger amount) {
			BigInteger balance = provider.getEmc();

			if (balance.compareTo(amount) < 0) {
				return false;
			}

			provider.setEmc(balance.subtract(amount));
			provider.syncEmc(player);
			return true;
		}

		@Override
		public void deposit(BigInteger amount) {
			provider.setEmc(provider.getEmc().add(amount));
			provider.syncEmc(player);
		}

		@Override
		public void learn(ItemStack stack) {
			if (provider.addKnowledge(stack)) {
				provider.sync(player);
			}
		}
	}
}
