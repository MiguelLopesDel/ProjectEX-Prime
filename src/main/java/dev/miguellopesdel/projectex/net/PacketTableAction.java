package dev.miguellopesdel.projectex.net;

import dev.miguellopesdel.projectex.gui.ContainerTableBase;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;

import java.util.function.Supplier;

/**
 * One press of a button in the transmutation panel.
 *
 * <p>The panel is drawn from the player's knowledge rather than from slots, so its buttons are not
 * slot clicks and vanilla carries nothing for them. What travels is the button's meaning, never its
 * outcome: the server decides whether the player can afford it, is allowed to learn it, or knows it
 * at all, and answers with the usual container update.
 */
public class PacketTableAction {
	private final int mode;
	@Nullable
	private final ItemInfo type;

	public PacketTableAction(int mode, @Nullable ItemInfo type) {
		this.mode = mode;
		this.type = type;
	}

	public PacketTableAction(FriendlyByteBuf buffer) {
		mode = buffer.readByte();
		CompoundTag tag = buffer.readNbt();
		type = tag == null ? null : ItemInfo.read(tag);
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeByte(mode);
		buffer.writeNbt(type == null ? null : type.write(new CompoundTag()));
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayer player = context.get().getSender();

			if (player != null && player.containerMenu instanceof ContainerTableBase table
					&& table.clickGuiSlot(type, mode)) {
				table.broadcastChanges();
			}
		});

		context.get().setPacketHandled(true);
	}
}
