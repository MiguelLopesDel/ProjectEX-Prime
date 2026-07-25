package dev.miguellopesdel.projectex;

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
		return collectorOutput * 18L + relayBonus * 30L;
	}
}
