package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.block.BlockPowerFlower;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Generates EMC straight into its owner's transmutation knowledge. While the owner is offline
 * the EMC accumulates here and is handed over on their next login.
 */
public class TilePowerFlower extends BlockEntity {
	public UUID owner = Util.NIL_UUID;
	public String ownerName = "";
	public int tick = 0;
	public BigInteger storedEMC = BigInteger.ZERO;

	public TilePowerFlower(BlockPos pos, BlockState state) {
		super(ProjectEXBlockEntities.POWER_FLOWER.get(), pos, state);
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

		if (!(getBlockState().getBlock() instanceof BlockPowerFlower powerFlower)) {
			return;
		}

		long generated = powerFlower.matter.powerFlowerOutput();

		ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
		IKnowledgeProvider provider = player == null ? null
				: player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElse(null);

		if (provider == null) {
			storedEMC = storedEMC.add(BigInteger.valueOf(generated));
			setChanged();
			return;
		}

		provider.setEmc(provider.getEmc().add(BigInteger.valueOf(generated)).add(storedEMC));

		if (!storedEMC.equals(BigInteger.ZERO)) {
			storedEMC = BigInteger.ZERO;
			setChanged();
		}

		provider.syncEmc(player);
	}
}
