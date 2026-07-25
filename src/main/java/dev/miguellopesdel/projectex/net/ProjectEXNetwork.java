package dev.miguellopesdel.projectex.net;

import dev.miguellopesdel.projectex.ProjectEX;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * The mod's one channel. Only the transmutation panel needs it: everything else the screens do is
 * a slot click, which vanilla already carries.
 */
public final class ProjectEXNetwork {
	private static final String VERSION = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ProjectEX.MOD_ID, "main"))
			.networkProtocolVersion(() -> VERSION)
			.clientAcceptedVersions(VERSION::equals)
			.serverAcceptedVersions(VERSION::equals)
			.simpleChannel();

	private ProjectEXNetwork() {
	}

	public static void register() {
		CHANNEL.registerMessage(0, PacketTableAction.class,
				PacketTableAction::encode, PacketTableAction::new, PacketTableAction::handle);
	}
}
