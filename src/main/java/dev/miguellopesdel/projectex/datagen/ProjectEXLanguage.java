package dev.miguellopesdel.projectex.datagen;

import dev.miguellopesdel.projectex.Matter;
import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.block.ProjectEXBlocks;
import dev.miguellopesdel.projectex.item.ProjectEXItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Carries over the names from the 1.12 en_us.lang file. The keys changed shape: the old
 * {@code tile.projectex.collector.basic.name} is now {@code block.projectex.basic_collector}.
 */
public class ProjectEXLanguage extends LanguageProvider {
	private static final String[] DISPLAY_NAMES = {
			"Basic", "Dark Matter", "Red Matter", "Magenta Matter", "Pink Matter", "Purple Matter",
			"Violet Matter", "Blue Matter", "Cyan Matter", "Green Matter", "Lime Matter",
			"Yellow Matter", "Orange Matter", "White Matter", "Fading Matter", "The Final"
	};

	public ProjectEXLanguage(PackOutput output) {
		super(output, ProjectEX.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add("itemGroup." + ProjectEX.MOD_ID, "ProjectEX");

		// Names the guide book that GuideME hands out for this mod's guide.
		add("projectex.guide_name", "ProjectEX Guide");
		add("projectex.guide_tooltip", "Collectors, relays, power flowers and EMC links");

		for (Matter matter : Matter.VALUES) {
			String name = DISPLAY_NAMES[matter.ordinal()];
			// The final tier has no MK number, the other fifteen are MK1 to MK15.
			String mk = matter == Matter.FINAL ? "" : " [MK" + (matter.ordinal() + 1) + "]";

			addBlock(ProjectEXBlocks.COLLECTOR.get(matter), name + " Collector" + mk);
			addBlock(ProjectEXBlocks.RELAY.get(matter), name + " Relay" + mk);
			addBlock(ProjectEXBlocks.POWER_FLOWER.get(matter), name + " Power Flower Bonsai Pot" + mk);
			addItem(ProjectEXItems.COMPRESSED_COLLECTOR.get(matter), "Compressed " + name + " Collector" + mk);

			if (matter.hasMatterItem) {
				addItem(ProjectEXItems.MATTER.get(matter), name);
			}
		}

		addBlock(ProjectEXBlocks.ENERGY_LINK, "Basic Energy EMC Link");
		addBlock(ProjectEXBlocks.PERSONAL_LINK, "Personal EMC Link");
		addBlock(ProjectEXBlocks.REFINED_LINK, "Refined EMC Link");
		addBlock(ProjectEXBlocks.COMPRESSED_REFINED_LINK, "Compressed Refined EMC Link");
		addBlock(ProjectEXBlocks.STONE_TABLE, "Stone Transmutation Table");
		addBlock(ProjectEXBlocks.ALCHEMY_TABLE, "Alchemy Table");

		addItem(ProjectEXItems.CLAY_MATTER, "Clay Matter");
		addItem(ProjectEXItems.FINAL_STAR_SHARD, "Final Star Shard");
		addItem(ProjectEXItems.FINAL_STAR, "Final Star");

		// ProjectE writes its own Klein Star tiers this way, and these continue that ladder.
		String[] starNames = {"Ein", "Zwei", "Drei", "Vier", "Sphere", "Omega"};

		for (int i = 0; i < starNames.length; i++) {
			addItem(ProjectEXItems.MAGNUM_STAR.get(i), "Magnum Star " + starNames[i]);
			addItem(ProjectEXItems.COLOSSAL_STAR.get(i), "Colossal Star " + starNames[i]);
		}

		addItem(ProjectEXItems.ARCANE_TABLET, "Arcane Transmutation Tablet");

		add("item.projectex.final_star.pedestal", "Copies items dropped on the pedestal into an adjacent inventory");

		// Shared by every transmutation panel.
		add("gui.projectex.table.burn", "Sell what you are holding. Shift: fill or empty an EMC item");
		add("gui.projectex.table.learn", "Learn");
		add("gui.projectex.table.unlearn", "Unlearn");

		add("gui.projectex.stone_table.cant_use", "This table does not handle this item");

		// The tablet's crafting helpers.
		add("gui.projectex.arcane_tablet.rotate", "Rotate. Shift: anticlockwise");
		add("gui.projectex.arcane_tablet.balance", "Balance. Shift: spread");
		add("gui.projectex.arcane_tablet.clear", "Clear");

		add("projectex.general.search_type", "Search Type");
		add("projectex.general.search_type.normal", "Normal");
		add("projectex.general.search_type.autoselected", "Auto-selected");
		add("projectex.general.search_type.normal_jei_sync", "Normal (JEI Sync)");
		add("projectex.general.search_type.autoselected_jei_sync", "Auto-selected (JEI Sync)");

		add("block.projectex.energy_link.tooltip", "You can use this block to add EMC to your Transmutation Table using Collectors.");
		add("block.projectex.personal_link.tooltip", "Same as Basic Energy EMC Link, but also allows to import and export items.");
		add("block.projectex.refined_link.tooltip", "Same as Personal EMC Link, but has 1 input slot and 9 output slots. Designed to be used with Refined Storage-like systems. It also learns items from input slots.");
		add("block.projectex.compressed_refined_link.tooltip", "Same as Refined EMC Link, but has 54 output slots.");
		add("block.projectex.stone_table.tooltip", "A Transmutation Table you can place on walls and ceilings.");
		add("block.projectex.alchemy_table.tooltip", "Turns one item into another by spending EMC. Feed it from a Collector or a Relay.");

		add("block.projectex.collector.tooltip", "Server TPS friendly. Generates EMC only once a second.");
		add("block.projectex.collector.emc_produced", "Produced EMC: %s/s");
		add("block.projectex.relay.tooltip", "Server TPS friendly. Transfers EMC only once a second.");
		add("block.projectex.relay.max_transfer", "Max EMC Transfer: %s/s");
		add("block.projectex.relay.relay_bonus", "Relay Bonus: %s/s");
		add("item.projectex.compressed_collector.tooltip", "Compressed, Crafting Material");
	}
}
