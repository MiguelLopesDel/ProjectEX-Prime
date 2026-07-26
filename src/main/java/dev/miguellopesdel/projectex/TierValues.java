package dev.miguellopesdel.projectex;

import com.google.common.math.LongMath;

/**
 * The tunable numbers of one tier, as an immutable snapshot.
 *
 * <p>These used to be mutable fields on {@link Matter} that the config event wrote to while
 * block entities were reading them on the server thread. Snapshotting instead means a config
 * reload swaps one reference and a tick either sees the old values or the new ones, never a
 * half-written mix.
 */
public record TierValues(long collectorOutput, long relayBonus, long relayTransfer) {
	public static TierValues defaultsOf(Matter matter) {
		return new TierValues(matter.defaultCollectorOutput, matter.defaultRelayBonus, matter.defaultRelayTransfer);
	}

	/** A power flower is worth 18 collectors and 30 relays of its tier. */
	public long powerFlowerOutput() {
		// A pack can set either of these as high as a long goes, and eighteen of one plus thirty of
		// the other would then wrap. A flower generating a negative amount would silently stop.
		return LongMath.saturatedAdd(LongMath.saturatedMultiply(collectorOutput, 18L),
				LongMath.saturatedMultiply(relayBonus, 30L));
	}
}
