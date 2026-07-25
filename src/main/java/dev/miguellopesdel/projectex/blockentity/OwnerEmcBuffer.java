package dev.miguellopesdel.projectex.blockentity;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Power flowers and links both pay EMC to whoever placed them, and both have to cope with that
 * player being offline. Holding the owner and the pending EMC in one place keeps the two from
 * disagreeing about the rules, in particular about syncing the player afterwards.
 *
 * <p>This is a field rather than a superclass because a power flower is not an EMC storage,
 * while a link is.
 */
public class OwnerEmcBuffer {
	public UUID owner = Util.NIL_UUID;
	public String ownerName = "";

	/** EMC earned while the owner was offline. BigInteger because late tiers overflow a long. */
	private BigInteger pending = BigInteger.ZERO;

	public void setOwner(LivingEntity entity) {
		owner = entity.getUUID();
		ownerName = entity.getScoreboardName();
	}

	public void add(long emc) {
		if (emc > 0L) {
			pending = pending.add(BigInteger.valueOf(emc));
		}
	}

	public boolean hasPending() {
		return !pending.equals(BigInteger.ZERO);
	}

	/**
	 * Hands {@code generated} plus anything buffered to the owner, or buffers it all if they are
	 * away.
	 *
	 * @return true when the buffer changed and the block entity needs saving
	 */
	public boolean deposit(Level level, long generated) {
		if (level.getServer() == null) {
			return false;
		}

		ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
		IKnowledgeProvider provider = player == null ? null
				: player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElse(null);

		if (provider == null) {
			if (generated <= 0L) {
				return false;
			}

			add(generated);
			return true;
		}

		BigInteger payout = pending.add(BigInteger.valueOf(generated));

		if (payout.equals(BigInteger.ZERO)) {
			return false;
		}

		provider.setEmc(provider.getEmc().add(payout));
		provider.syncEmc(player);

		boolean hadPending = hasPending();
		pending = BigInteger.ZERO;
		return hadPending;
	}

	public void save(CompoundTag tag) {
		tag.putUUID("Owner", owner);
		tag.putString("OwnerName", ownerName);
		tag.putString("StoredEMC", pending.toString());
	}

	public void load(CompoundTag tag) {
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : Util.NIL_UUID;
		ownerName = tag.getString("OwnerName");
		String emc = tag.getString("StoredEMC");
		pending = emc.isEmpty() || emc.equals("0") ? BigInteger.ZERO : new BigInteger(emc);
	}
}
