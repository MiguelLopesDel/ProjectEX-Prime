package com.latmod.mods.projectex;

import com.latmod.mods.projectex.item.ProjectEXItems;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import javax.annotation.Nullable;

import java.util.function.Supplier;

/**
 * The 16 tiers of collectors, relays and power flowers.
 *
 * <p>In 1.12 this was split in two enums: {@code EnumTier} for the 16 machine tiers and
 * {@code EnumMatter} for the 12 matter items, which were block/item metadata. Metadata is
 * gone, so every tier is now its own block and every matter its own item, and a single enum
 * carries both.
 */
public enum Matter implements StringRepresentable {
	BASIC("basic", false, 4L, 1L, 64L, () -> Items.DIAMOND_BLOCK),
	DARK("dark", false, 12L, 3L, 192L, () -> PEItems.DARK_MATTER.get()),
	RED("red", false, 40L, 10L, 640L, () -> PEItems.RED_MATTER.get()),
	MAGENTA("magenta", true, 160L, 40L, 2560L, null),
	PINK("pink", true, 640L, 150L, 10240L, null),
	PURPLE("purple", true, 2560L, 750L, 40960L, null),
	VIOLET("violet", true, 10240L, 3750L, 163840L, null),
	BLUE("blue", true, 40960L, 15000L, 655360L, null),
	CYAN("cyan", true, 163840L, 60000L, 2621440L, null),
	GREEN("green", true, 655360L, 240000L, 10485760L, null),
	LIME("lime", true, 2621440L, 960000L, 41943040L, null),
	YELLOW("yellow", true, 10485760L, 3840000L, 167772160L, null),
	ORANGE("orange", true, 41943040L, 15360000L, 671088640L, null),
	WHITE("white", true, 167772160L, 61440000L, 2684354560L, null),
	FADING("fading", true, 671088640L, 245760000L, 10737418240L, null),
	FINAL("final", false, 1000000000000L, 1000000000000L, Long.MAX_VALUE, () -> ProjectEXItems.FINAL_STAR_SHARD.get());

	public static final Matter[] VALUES = values();

	public final String id;
	public final boolean hasMatterItem;
	public final long defaultCollectorOutput;
	public final long defaultRelayBonus;
	public final long defaultRelayTransfer;

	/** Overwritten from the config on load, which is why these are not final. */
	public long collectorOutput;
	public long relayBonus;
	public long relayTransfer;

	@Nullable
	private final Supplier<Item> craftingItem;

	Matter(String id, boolean hasMatterItem, long collectorOutput, long relayBonus, long relayTransfer, @Nullable Supplier<Item> craftingItem) {
		this.id = id;
		this.hasMatterItem = hasMatterItem;
		this.defaultCollectorOutput = collectorOutput;
		this.defaultRelayBonus = relayBonus;
		this.defaultRelayTransfer = relayTransfer;
		this.collectorOutput = collectorOutput;
		this.relayBonus = relayBonus;
		this.relayTransfer = relayTransfer;
		this.craftingItem = craftingItem;
	}

	/**
	 * The item this tier is crafted from: a vanilla or ProjectE item for the first tiers,
	 * the matter item of the same name for the rest.
	 */
	public Supplier<Item> getCraftingItem() {
		// Kept as a supplier so the item registry is not touched during class loading.
		return craftingItem == null ? ProjectEXItems.MATTER.get(this) : craftingItem;
	}

	@Nullable
	public Matter getPrevious() {
		return this == BASIC ? null : VALUES[ordinal() - 1];
	}

	/** A power flower is worth 18 collectors and 30 relays of the same tier. */
	public long powerFlowerOutput() {
		return collectorOutput * 18L + relayBonus * 30L;
	}

	@Override
	public String getSerializedName() {
		return id;
	}
}
