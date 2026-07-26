package dev.miguellopesdel.projectex.blockentity;

import dev.miguellopesdel.projectex.Knowledge;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.math.BigInteger;
import java.util.UUID;

/**
 * The EMC balance a link spends from and pays into.
 *
 * <p>This exists so the links do not talk to {@link IKnowledgeProvider} directly: whether the
 * owner is logged in decides where their balance lives, and nothing that spends EMC should have
 * to care. Online it is their capability, offline it is their save file.
 */
public interface EmcAccount {
	BigInteger balance();

	/** @return false when the balance was not enough, in which case nothing is spent */
	boolean spend(BigInteger amount);

	void deposit(BigInteger amount);

	/** Teaches the owner an item, so that what a link eats becomes transmutable. */
	void learn(ItemInfo item);

	/** Teaches an item as EMC stores it, never as the stack that happened to arrive. */
	default void learn(ItemStack stack) {
		learn(PersistentItems.infoOf(stack));
	}

	@Nullable
	static EmcAccount of(Level level, UUID owner) {
		if (level == null || level.getServer() == null || Util.NIL_UUID.equals(owner)) {
			return null;
		}

		MinecraftServer server = level.getServer();
		ServerPlayer player = server.getPlayerList().getPlayer(owner);

		if (player == null) {
			return new OfflineAccount(server, owner);
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
		public void learn(ItemInfo item) {
			Knowledge.teach(player, provider, item);
		}
	}

	/**
	 * The owner is away, so their balance is edited in their save file. Changes land in memory
	 * and are written once a tick, so a fast pipe pulling from a link does not turn into a disk
	 * write per item.
	 */
	record OfflineAccount(MinecraftServer server, UUID owner) implements EmcAccount {
		@Override
		public BigInteger balance() {
			return OfflineEmcStore.balanceOf(server, owner);
		}

		@Override
		public boolean spend(BigInteger amount) {
			BigInteger balance = balance();

			if (balance.compareTo(amount) < 0) {
				return false;
			}

			return OfflineEmcStore.setBalance(server, owner, balance.subtract(amount));
		}

		@Override
		public void deposit(BigInteger amount) {
			OfflineEmcStore.setBalance(server, owner, balance().add(amount));
		}

		/**
		 * Unlike the online path this cannot ask a pack for permission: {@code
		 * PlayerAttemptLearnEvent} needs the player it is about, and that player is not here. A pack
		 * that vetoes an item still keeps it out of a link owned by someone logged in.
		 */
		@Override
		public void learn(ItemInfo item) {
			OfflineEmcStore.learn(server, owner, item);
		}
	}
}
