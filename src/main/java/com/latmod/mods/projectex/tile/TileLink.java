package com.latmod.mods.projectex.tile;

import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Base of the EMC links: accepts EMC from any side and deposits it into its owner's
 * transmutation knowledge, buffering it while the owner is offline.
 *
 * <p>The item input and output sides of the links live in their menus and are part of the GUI
 * phase of the port; see the README.
 */
public class TileLink extends BlockEntity implements IEmcStorage {
	public UUID owner = Util.NIL_UUID;
	public String ownerName = "";
	public int tick = 0;
	public BigInteger storedEMC = BigInteger.ZERO;
	private LazyOptional<IEmcStorage> emcStorageCapability;

	public TileLink(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : Util.NIL_UUID;
		ownerName = tag.getString("OwnerName");
		tick = tag.getByte("Tick") & 0xFF;
		String emc = tag.getString("StoredEMC");
		storedEMC = emc.isEmpty() || emc.equals("0") ? BigInteger.ZERO : new BigInteger(emc);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putUUID("Owner", owner);
		tag.putString("OwnerName", ownerName);
		tag.putByte("Tick", (byte) tick);
		tag.putString("StoredEMC", storedEMC.toString());
	}

	public void tick() {
		if (level == null || level.getServer() == null) {
			return;
		}

		tick++;

		if (tick < 20) {
			return;
		}

		tick = 0;

		if (storedEMC.equals(BigInteger.ZERO)) {
			return;
		}

		ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
		IKnowledgeProvider provider = player == null ? null
				: player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElse(null);

		if (provider != null) {
			provider.setEmc(provider.getEmc().add(storedEMC));
			storedEMC = BigInteger.ZERO;
			setChanged();
			provider.syncEmc(player);
		}
	}

	@Override
	public long getStoredEmc() {
		return 0L;
	}

	@Override
	public long getMaximumEmc() {
		return Long.MAX_VALUE;
	}

	@Override
	public long extractEmc(long emc, EmcAction action) {
		return emc < 0L ? insertEmc(-emc, action) : 0L;
	}

	@Override
	public long insertEmc(long emc, EmcAction action) {
		if (emc <= 0L) {
			return 0L;
		}

		if (action.execute()) {
			storedEMC = storedEMC.add(BigInteger.valueOf(emc));
			setChanged();
		}

		return emc;
	}

	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (cap == PECapabilities.EMC_STORAGE_CAPABILITY) {
			if (emcStorageCapability == null || !emcStorageCapability.isPresent()) {
				emcStorageCapability = LazyOptional.of(() -> this);
			}

			return emcStorageCapability.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();

		if (emcStorageCapability != null) {
			emcStorageCapability.invalidate();
			emcStorageCapability = null;
		}
	}
}
