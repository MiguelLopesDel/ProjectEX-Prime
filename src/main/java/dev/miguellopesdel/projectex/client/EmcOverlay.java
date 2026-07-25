package dev.miguellopesdel.projectex.client;

import dev.miguellopesdel.projectex.Knowledge;
import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.ProjectEXConfig;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Draws the player's EMC on screen, and how fast it is moving.
 *
 * <p>The rate is the point. A balance on its own says nothing about whether the collectors are
 * keeping up with what is being spent; a number next to it that reads {@code +2.4M/s} does. It is
 * averaged over the last five seconds so that a single expensive craft does not make it swing.
 *
 * <p>Nothing is sent for this. ProjectE already syncs the balance to the client, so the counter
 * only samples what is already there.
 */
@Mod.EventBusSubscriber(modid = ProjectEX.MOD_ID, value = Dist.CLIENT)
public final class EmcOverlay {
	private static final int SECOND = 20;
	private static final int SAMPLES = 5;

	private static final BigInteger[] deltas = new BigInteger[SAMPLES];

	private static BigInteger emc = BigInteger.ZERO;
	private static BigInteger previous = BigInteger.ZERO;
	private static BigInteger rate = BigInteger.ZERO;
	private static boolean sampled;
	private static int ticks;

	static {
		Arrays.fill(deltas, BigInteger.ZERO);
	}

	private EmcOverlay() {
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null) {
			reset();
			return;
		}

		IKnowledgeProvider knowledge = Knowledge.of(player);

		if (knowledge == null) {
			return;
		}

		emc = knowledge.getEmc();

		if (++ticks < SECOND) {
			return;
		}

		ticks = 0;

		// The first sample only establishes where the balance started. Counting it as a change
		// would report the player's entire savings as one second of income.
		if (!sampled) {
			previous = emc;
			sampled = true;
			return;
		}

		System.arraycopy(deltas, 1, deltas, 0, deltas.length - 1);
		deltas[deltas.length - 1] = emc.subtract(previous);
		previous = emc;

		BigInteger total = BigInteger.ZERO;

		for (BigInteger delta : deltas) {
			total = total.add(delta);
		}

		rate = total.divide(BigInteger.valueOf(deltas.length));
	}

	private static void reset() {
		emc = BigInteger.ZERO;
		previous = BigInteger.ZERO;
		rate = BigInteger.ZERO;
		sampled = false;
		ticks = 0;
		Arrays.fill(deltas, BigInteger.ZERO);
	}

	/**
	 * Forge draws these two lists in the corners whenever the debug screen is not up, so the
	 * counter is visible in normal play and gets out of the way when F3 is.
	 */
	@SubscribeEvent
	public static void onOverlayText(CustomizeGuiOverlayEvent.DebugText event) {
		EnumScreenPosition position = ProjectEXConfig.CLIENT.emcScreenPosition.get();

		if (position == EnumScreenPosition.DISABLED || emc.signum() <= 0) {
			return;
		}

		StringBuilder line = new StringBuilder("EMC: ").append(TransmutationEMCFormatter.formatEMC(emc).getString());

		if (rate.signum() != 0) {
			line.append(rate.signum() > 0 ? ChatFormatting.GREEN + "+" : ChatFormatting.RED + "-")
					.append(TransmutationEMCFormatter.formatEMC(rate.abs()).getString())
					.append("/s");
		}

		(position == EnumScreenPosition.TOP_LEFT ? event.getLeft() : event.getRight()).add(line.toString());
	}
}
