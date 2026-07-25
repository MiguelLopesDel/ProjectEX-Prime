package dev.miguellopesdel.projectex.datagen;

import dev.miguellopesdel.projectex.Matter;
import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.block.ProjectEXBlocks;
import dev.miguellopesdel.projectex.item.ProjectEXItems;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

/**
 * In 1.12 these recipes were built in code at registry time; they are data now, so the same
 * chains are emitted from here instead.
 */
public class ProjectEXRecipes extends RecipeProvider {
	public ProjectEXRecipes(PackOutput output) {
		super(output);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		links(consumer);
		stoneTable(consumer);
		matter(consumer);
		machines(consumer);
	}

	private void links(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.ENERGY_LINK.get())
				.pattern("LMH")
				.pattern("SRS")
				.pattern("HML")
				.define('S', Tags.Items.STONE)
				.define('R', PEItems.RED_MATTER.get())
				.define('L', PEItems.LOW_COVALENCE_DUST.get())
				.define('M', PEItems.MEDIUM_COVALENCE_DUST.get())
				.define('H', PEItems.HIGH_COVALENCE_DUST.get())
				.group("projectex:link")
				.unlockedBy("has_red_matter", has(PEItems.RED_MATTER.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.PERSONAL_LINK.get())
				.pattern("RBR")
				.pattern("BCB")
				.pattern("RBR")
				.define('B', ProjectEXBlocks.ENERGY_LINK.get())
				.define('R', PEBlocks.RED_MATTER.asItem())
				.define('C', PEBlocks.CONDENSER_MK2.asItem())
				.group("projectex:link")
				.unlockedBy("has_energy_link", has(ProjectEXBlocks.ENERGY_LINK.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.REFINED_LINK.get())
				.pattern("LLL")
				.pattern("LLL")
				.pattern("LLL")
				.define('L', ProjectEXBlocks.PERSONAL_LINK.get())
				.group("projectex:link")
				.unlockedBy("has_personal_link", has(ProjectEXBlocks.PERSONAL_LINK.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.COMPRESSED_REFINED_LINK.get())
				.pattern("LLL")
				.pattern("LLL")
				.define('L', ProjectEXBlocks.REFINED_LINK.get())
				.group("projectex:link")
				.unlockedBy("has_refined_link", has(ProjectEXBlocks.REFINED_LINK.get()))
				.save(consumer);
	}

	private void stoneTable(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.STONE_TABLE.get())
				.pattern("SSS")
				.pattern("STS")
				.pattern("SSS")
				.define('S', Ingredient.of(Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS, Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS))
				.define('T', Ingredient.of(PEBlocks.TRANSMUTATION_TABLE.asItem(), PEItems.PHILOSOPHERS_STONE.get()))
				.group("projectex:stone_table")
				.unlockedBy("has_philosophers_stone", has(PEItems.PHILOSOPHERS_STONE.get()))
				.save(consumer);
	}

	/**
	 * Matter is crafted from the previous tier surrounded by aeternalis fuel, in two layouts so
	 * the recipe works whichever way round the player builds it.
	 */
	private void matter(Consumer<FinishedRecipe> consumer) {
		Item fuel = PEItems.AETERNALIS_FUEL.get();

		for (Matter matter : Matter.VALUES) {
			if (!matter.hasMatterItem) {
				continue;
			}

			Matter previous = matter.getPrevious();
			Item result = ProjectEXItems.MATTER.get(matter).get();
			Item input = previous.hasMatterItem ? ProjectEXItems.MATTER.get(previous).get() : previous.getCraftingItem().get();

			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
					.pattern("FFF")
					.pattern("MMM")
					.pattern("FFF")
					.define('F', fuel)
					.define('M', input)
					.group("projectex:matter")
					.unlockedBy("has_previous_matter", has(input))
					.save(consumer, id("matter/" + matter.id + "_h"));

			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
					.pattern("FMF")
					.pattern("FMF")
					.pattern("FMF")
					.define('F', fuel)
					.define('M', input)
					.group("projectex:matter")
					.unlockedBy("has_previous_matter", has(input))
					.save(consumer, id("matter/" + matter.id + "_v"));
		}
	}

	private void machines(Consumer<FinishedRecipe> consumer) {
		for (Matter matter : Matter.VALUES) {
			ItemLike collector = ProjectEXBlocks.COLLECTOR.get(matter).get();
			ItemLike relay = ProjectEXBlocks.RELAY.get(matter).get();
			ItemLike compressed = ProjectEXItems.COMPRESSED_COLLECTOR.get(matter).get();

			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, compressed)
					.pattern("CCC")
					.pattern("CCC")
					.pattern("CCC")
					.define('C', collector)
					.group("projectex:compressed_collector")
					.unlockedBy("has_collector", has(collector))
					.save(consumer, id("compressed_collector/" + matter.id));

			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.POWER_FLOWER.get(matter).get())
					.pattern("CEC")
					.pattern("RRR")
					.pattern("RRR")
					.define('C', compressed)
					.define('E', ProjectEXBlocks.ENERGY_LINK.get())
					.define('R', relay)
					.group("projectex:power_flower")
					.unlockedBy("has_compressed_collector", has(compressed))
					.save(consumer, id("power_flower/" + matter.id));

			// The first tier is crafted from its own material rather than upgraded from a
			// previous tier, and that recipe is part of the item phase of the port.
			Matter previous = matter.getPrevious();

			if (previous == null) {
				continue;
			}

			Item upgrade = matter.getCraftingItem().get();

			ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, collector)
					.requires(ProjectEXBlocks.COLLECTOR.get(previous).get())
					.requires(upgrade)
					.group("projectex:collector")
					.unlockedBy("has_previous_collector", has(ProjectEXBlocks.COLLECTOR.get(previous).get()))
					.save(consumer, id("collector/" + matter.id));

			ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, relay)
					.requires(ProjectEXBlocks.RELAY.get(previous).get())
					.requires(upgrade)
					.group("projectex:relay")
					.unlockedBy("has_previous_relay", has(ProjectEXBlocks.RELAY.get(previous).get()))
					.save(consumer, id("relay/" + matter.id));
		}
	}

	private static ResourceLocation id(String path) {
		return new ResourceLocation(ProjectEX.MOD_ID, path);
	}
}
