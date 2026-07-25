package dev.miguellopesdel.projectex;

import dev.miguellopesdel.projectex.client.EnumScreenPosition;
import dev.miguellopesdel.projectex.client.EnumSearchType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Forge's annotation driven config is gone, so the 1.12 config classes are rebuilt on
 * {@link ForgeConfigSpec}. The values themselves are unchanged.
 */
@Mod.EventBusSubscriber(modid = ProjectEX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ProjectEXConfig {
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;

	static {
		Pair<Common, ForgeConfigSpec> common = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON = common.getLeft();
		COMMON_SPEC = common.getRight();

		Pair<Client, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT = client.getLeft();
		CLIENT_SPEC = client.getRight();
	}

	private static List<StoneTableEntry> stoneTableWhitelistCache;

	private ProjectEXConfig() {
	}

	public static class Common {
		public final ForgeConfigSpec.BooleanValue finalStarCopyAnyItem;
		public final ForgeConfigSpec.BooleanValue finalStarCopyNbt;
		public final ForgeConfigSpec.IntValue finalStarUpdateInterval;
		public final ForgeConfigSpec.LongValue emcLinkMaxOut;
		public final ForgeConfigSpec.BooleanValue enableStoneTableWhitelist;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> stoneTableWhitelist;

		public final Map<Matter, TierEntry> tiers = new EnumMap<>(Matter.class);

		private Common(ForgeConfigSpec.Builder builder) {
			builder.push("general");

			// 1.12's blacklist_power_flower_from_watch lived here. ProjectE reads what the Watch of
			// Flowing Time skips from the projecte:blacklist/time_watch tag now, so power flowers
			// are added to that tag instead and a pack changes the tag rather than a config value.

			finalStarCopyAnyItem = builder
					.comment("If set to false, the Final Star will only copy items that have an EMC value.")
					.define("final_star_copy_any_item", true);

			finalStarCopyNbt = builder
					.comment("If set to false, copied items lose their NBT.")
					.define("final_star_copy_nbt", false);

			finalStarUpdateInterval = builder
					.comment("Set to 0 to completely disable item copying.")
					.defineInRange("final_star_update_interval", 20, 0, Integer.MAX_VALUE);

			emcLinkMaxOut = builder
					.comment("Max item count an EMC link will output.",
							"0 disables item exporting from links and makes refined ones useless.",
							"Reduce this if you are having problems with auto-crafting or similar things.")
					.defineInRange("emc_link_max_out", 2000000000L, 0L, Long.MAX_VALUE);

			enableStoneTableWhitelist = builder
					.comment("The whitelist for the Stone Table is ignored unless this is set to true.")
					.define("enable_stone_table_whitelist", false);

			stoneTableWhitelist = builder
					.comment("Items the Stone Table accepts. The 1.12 ore dictionary entries are item tags now:",
							"prefix an entry with # for a tag, otherwise it is an item id.",
							"Example: #forge:ingots or minecraft:flint")
					.defineList("stone_table_whitelist", defaultStoneTableWhitelist(), o -> o instanceof String);

			builder.pop();

			builder.comment("EMC output of each tier. Power flower output is derived: collector_output * 18 + relay_bonus * 30.")
					.push("tiers");

			for (Matter matter : Matter.VALUES) {
				builder.push(matter.id);
				tiers.put(matter, new TierEntry(builder, matter));
				builder.pop();
			}

			builder.pop();
		}
	}

	/** The config spec entries of one tier. The values they hold end up in a {@link TierValues}. */
	public static class TierEntry {
		public final ForgeConfigSpec.LongValue collectorOutput;
		public final ForgeConfigSpec.LongValue relayBonus;
		public final ForgeConfigSpec.LongValue relayTransfer;

		private TierEntry(ForgeConfigSpec.Builder builder, Matter matter) {
			collectorOutput = builder.defineInRange("collector_output", matter.defaultCollectorOutput, 0L, Long.MAX_VALUE);
			relayBonus = builder.defineInRange("relay_bonus", matter.defaultRelayBonus, 0L, Long.MAX_VALUE);
			relayTransfer = builder.defineInRange("relay_transfer", matter.defaultRelayTransfer, 1L, Long.MAX_VALUE);
		}
	}

	public static class Client {
		public final ForgeConfigSpec.EnumValue<EnumScreenPosition> emcScreenPosition;
		public final ForgeConfigSpec.EnumValue<EnumSearchType> searchType;

		private Client(ForgeConfigSpec.Builder builder) {
			builder.push("general");

			emcScreenPosition = builder
					.comment("Where the personal EMC counter is drawn on screen.")
					.defineEnum("emc_screen_position", EnumScreenPosition.TOP_LEFT);

			searchType = builder
					.comment("Behaviour of the search bar in the transmutation interfaces.")
					.defineEnum("search_type", EnumSearchType.NORMAL);

			builder.pop();
		}
	}

	/**
	 * The values the blocks actually read. Replaced wholesale on a config reload rather than
	 * mutated in place, so a tick sees one consistent set of numbers.
	 */
	private static volatile Map<Matter, TierValues> tierValues = defaultTierValues();

	public static TierValues valuesOf(Matter matter) {
		return tierValues.get(matter);
	}

	private static Map<Matter, TierValues> defaultTierValues() {
		Map<Matter, TierValues> map = new EnumMap<>(Matter.class);

		for (Matter matter : Matter.VALUES) {
			map.put(matter, TierValues.defaultsOf(matter));
		}

		return map;
	}

	/**
	 * Rebuilds the snapshot when the config loads or is reloaded, so changes take effect without
	 * a restart.
	 */
	@SubscribeEvent
	public static void onConfigLoad(ModConfigEvent event) {
		if (event.getConfig().getSpec() != COMMON_SPEC) {
			return;
		}

		Map<Matter, TierValues> values = new EnumMap<>(Matter.class);

		for (Matter matter : Matter.VALUES) {
			TierEntry entry = COMMON.tiers.get(matter);
			values.put(matter, new TierValues(entry.collectorOutput.get(), entry.relayBonus.get(), entry.relayTransfer.get()));
		}

		tierValues = Map.copyOf(values);
		stoneTableWhitelistCache = null;
	}

	public static boolean isStoneTableWhitelisted(ItemStack stack) {
		if (!COMMON.enableStoneTableWhitelist.get()) {
			return true;
		}

		if (stoneTableWhitelistCache == null) {
			List<StoneTableEntry> entries = new ArrayList<>();

			for (String s : COMMON.stoneTableWhitelist.get()) {
				String trimmed = s.trim();

				if (trimmed.isEmpty()) {
					continue;
				}

				if (trimmed.startsWith("#")) {
					ResourceLocation id = ResourceLocation.tryParse(trimmed.substring(1));

					if (id != null) {
						entries.add(new StoneTableEntry(TagKey.create(Registries.ITEM, id), null));
					}
				} else {
					ResourceLocation id = ResourceLocation.tryParse(trimmed);
					Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);

					if (item != null) {
						entries.add(new StoneTableEntry(null, item));
					}
				}
			}

			stoneTableWhitelistCache = entries;
		}

		for (StoneTableEntry entry : stoneTableWhitelistCache) {
			if (entry.matches(stack)) {
				return true;
			}
		}

		return false;
	}

	private record StoneTableEntry(TagKey<Item> tag, Item item) {
		boolean matches(ItemStack stack) {
			return tag == null ? stack.is(item) : stack.is(tag);
		}
	}

	private static List<String> defaultStoneTableWhitelist() {
		return List.of(
				"#forge:ingots",
				"#forge:gems",
				"#forge:dusts",
				"#forge:nuggets",
				"#forge:storage_blocks",
				"#forge:ores",

				"#forge:stone",
				"#forge:cobblestone",
				"#forge:sand",
				"#minecraft:dirt",
				"#forge:gravel",
				"#forge:obsidian",
				"#forge:netherrack",
				"#forge:end_stones",

				"minecraft:coal",
				"minecraft:charcoal",
				"minecraft:flint",
				"minecraft:clay_ball",

				"#forge:dyes",
				"#forge:string",
				"#forge:leather",
				"#forge:crops/sugarcane",
				"#forge:feathers",
				"#forge:gunpowder",
				"minecraft:wheat_seeds",

				"#" + ItemTags.LOGS.location(),
				"#" + ItemTags.SAPLINGS.location(),
				"#forge:glass/colorless",

				"#forge:ender_pearls",
				"minecraft:blaze_rod",
				"minecraft:ghast_tear"
		);
	}
}
