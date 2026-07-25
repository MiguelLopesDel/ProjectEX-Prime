package dev.miguellopesdel.projectex.net;

import dev.miguellopesdel.projectex.ProjectEX;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * The mod's one channel, for the two things vanilla's slot clicks cannot carry: a button in the
 * transmutation panel, and JEI's transfer arrow over the Arcane Tablet.
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
		CHANNEL.registerMessage(1, PacketTabletRecipe.class,
				PacketTabletRecipe::encode, PacketTabletRecipe::new, PacketTabletRecipe::handle);
	}
}
