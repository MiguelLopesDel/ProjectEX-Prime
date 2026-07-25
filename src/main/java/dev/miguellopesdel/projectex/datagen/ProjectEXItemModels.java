package dev.miguellopesdel.projectex.datagen;

import dev.miguellopesdel.projectex.Matter;
import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.item.ProjectEXItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ProjectEXItemModels extends ItemModelProvider {
	public ProjectEXItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, ProjectEX.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		for (Matter matter : Matter.VALUES) {
			blockItem(matter.id + "_collector");
			blockItem(matter.id + "_relay");
			blockItem(matter.id + "_power_flower");

			// A compressed collector is a crafting item, but it keeps the look of the collector
			// it is made of.
			withExistingParent(matter.id + "_compressed_collector", modLoc("block/" + matter.id + "_collector"));

			if (matter.hasMatterItem) {
				flatItem(matter.id + "_matter", "item/matter/" + matter.id);
			}
		}

		blockItem("energy_link");
		blockItem("personal_link");
		blockItem("refined_link");
		blockItem("compressed_refined_link");
		blockItem("stone_table");
		blockItem("alchemy_table");

		flatItem(ProjectEXItems.CLAY_MATTER.getId().getPath(), "item/matter/clay");
		flatItem(ProjectEXItems.FINAL_STAR_SHARD.getId().getPath(), "item/final_star_shard");
		flatItem(ProjectEXItems.FINAL_STAR.getId().getPath(), "item/final_star");
		flatItem(ProjectEXItems.ARCANE_TABLET.getId().getPath(), "item/arcane_tablet");
		flatItem(ProjectEXItems.KNOWLEDGE_SHARING_BOOK.getId().getPath(), "item/knowledge_sharing_book");

		for (RegistryObject<Item> star : ProjectEXItems.MAGNUM_STAR) {
			flatItem(star.getId().getPath(), "item/" + star.getId().getPath());
		}

		for (RegistryObject<Item> star : ProjectEXItems.COLOSSAL_STAR) {
			flatItem(star.getId().getPath(), "item/" + star.getId().getPath());
		}
	}

	private void blockItem(String name) {
		withExistingParent(name, modLoc("block/" + name));
	}

	private void flatItem(String name, String texture) {
		withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc(texture));
	}
}
